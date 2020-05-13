package ru.inforion.lab403.kopycat.serializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.system.measureTimeMillis



class Serializer<T: ISerializable>(val target: T, val suppressWarnings: Boolean) {

    companion object {
        val log = logger()

        fun createTemporaryFileFromSnapshot(data: ByteArray): File {
            val formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss")
            val suffix = "%s".format(LocalDateTime.now().format(formatter))
            return File.createTempFile("snp_", suffix).apply {
                deleteOnExit()
                writeBytes(data)
            }
        }

        fun getPluginFromSnapshot(path: String): Pair<String, String> {
            val comp = Component(null, "dummy")
            Serializer(comp, false).deserialize(path)
            return Pair(comp.snapshotPlugin, comp.snapshotLibrary)
        }

        fun getPluginFromSnapshot(data: ByteArray) =
                getPluginFromSnapshot(createTemporaryFileFromSnapshot(data).absolutePath)
    }

    // java by default generate date format that can be only read on non-windows system.... have fun
    private var zipOut: ZipOutputStream? = null
    private var zipFile: ZipFile? = null
    private var cache = HashMap<String, Array<ByteArray?>>()
    private var json: String? = null
    private var snapshot: Map<String, Any>? = null

    private val mapper = jacksonObjectMapper().apply {
        configure(JsonParser.Feature.ALLOW_COMMENTS, true)
        configure(SerializationFeature.INDENT_OUTPUT, true)
    }

    fun serialize(path: String): Boolean {
        log.fine { "================================================================================================" }
        log.fine { "============================= ${target.javaClass.simpleName} serialization started =============================" }
        log.fine { "================================================================================================" }
        val time = measureTimeMillis {
            // subtle closing required
            File(path).parentFile.mkdirs()
            ZipOutputStream(FileOutputStream(path)).use {
                zipOut = it
                try {
                    snapshot = target.serialize(this)
                } catch (error: NotSerializableObjectException) {
                    log.warning { "Can't serialize object due to ${error.message}" }
                    return false
                }
                json = mapper.writeValueAsString(snapshot)
                it.putNextEntry(ZipEntry("state.json"))
                it.write(json!!.toByteArray())
            }
        }
        log.fine { "==================== ${target.javaClass.simpleName} was serialized for $time ms ==================== " }
        return true
    }

    fun deserialize(path: String): Serializer<T> {
        log.fine { "================================================================================================" }
        log.fine { "============================= ${target.javaClass.simpleName} deserialization started =============================" }
        log.fine { "================================================================================================" }
        val time = measureTimeMillis {
            zipFile = ZipFile(path)
            zipFile!!.let { zip ->
                val entry = zip.getEntry("state.json")
                zip.getInputStream(entry).use { stream ->
                    json = stream.readBytes().toString(Charsets.UTF_8)
                    snapshot = mapper.readValue<HashMap<String, Any>>(json!!)
                    target.deserialize(this, snapshot!!)
                }
            }
        }
        log.fine { "==================== ${target.javaClass.simpleName} was deserialized for $time ms ==================== " }

        return this
    }

    fun deserialize(data: ByteArray) = deserialize(createTemporaryFileFromSnapshot(data).absolutePath)

    fun restore(): Serializer<T> {
        log.fine { "================================================================================================" }
        log.fine { "============================= ${target.javaClass.simpleName} restoration started =============================" }
        log.fine { "================================================================================================" }
        val time = measureTimeMillis {
            if (json == null) {
                if (zipFile != null) {
                    zipFile!!.let { zip ->
                        val entry = zip.getEntry("state.json")
                        zip.getInputStream(entry).use { stream ->
                            json = stream.readBytes().toString(Charsets.UTF_8)
                            snapshot = mapper.readValue<HashMap<String, Any>>(json!!)
                            log.info { "Snapshot loaded from zip" }
                        }
                    }
                }
                else
                    log.severe { "Restore failed. No last deserialized state." }
            }
            target.restore(this, snapshot!!["components"] as Map<String, Any>)
        }
        log.fine { "==================== ${target.javaClass.simpleName} was restored for $time ms ==================== " }
        return this
    }

    fun isBinaryExists(name: String): Boolean = zipFile!!.getEntry(name) != null

    fun storeBinaries(vararg values: Pair<String, ByteBuffer>): Map<String, Any> =
            values.fold(emptyMap()) { acc, (name, buffer) ->
                acc + storeBinary(name, buffer)
            }

    fun storeBinary(name: String, output: ByteBuffer): Map<String, Any> {
        zipOut!!.let { zip ->
            val zipEntry = ZipEntry(name)
            zip.putNextEntry(zipEntry)
            safeByteBufferAction(output) {
                zip.writeFrom(output)
            }
        }
        return storeByteBufferData(name, output)
    }

    fun loadBinary(snapshot: Map<String, Any?>, name: String, output: ByteBuffer): Boolean {
        zipFile!!.let { zip ->
            val entry = zip.getEntry(name) ?: return false
            zip.getInputStream(entry).use { stream -> stream.readInto(output) }
            restoreByteBufferData(snapshot, name, output)
            return true
        }
    }

    fun restoreBinary(snapshot: Map<String, Any?>, name: String, output: ByteBuffer, dirtyPages: Set<Int>, pageSize: Int) {
        zipFile!!.let { zip ->
            val entry = zip.getEntry(name)
            zip.getInputStream(entry).use { stream ->
                var pos = 0L

                val fixedPageSize = if (output.limit() < pageSize) output.limit() else pageSize
                val pageCount = output.limit() / pageSize + if ((output.limit() % pageSize) > 0) 1 else 0

                if (name !in cache) {
                    cache[name] = Array<ByteArray?>(pageCount) { null }
                    log.info { "Created cache entry for %s".format(name) }
                }

                val cacheEntry = cache[name]!!

                val dis = DataInputStream(stream)
                for (page in dirtyPages.sorted()) {
                    val index = page / fixedPageSize
                    var cachedData = cacheEntry[index]
                    if (cachedData == null) {
//                    log.info { "Loading page from zip %08X".format(page) }
                        pos += dis.skip(page - pos)
                        val len = minOf(fixedPageSize.asLong, output.limit() - pos)
                        cachedData = ByteArray(len.asInt)
                        dis.readFully(cachedData, 0, len.asInt)
                        pos += len
                        cacheEntry[index] = cachedData
                    }
                    output.position(page)
                    output.put(cachedData)
                    // System.arraycopy(cachedData, 0, byteArray, page, pageSize)
                }
            }
            restoreByteBufferData(snapshot, name, output)
        }
    }

    fun storeValues(vararg values: Pair<String, Any>) = values.associate { (name, value) ->
        name to when (value) {
            is Float -> value.ieee754()
            is Double -> value.ieee754()
            else -> value
        }
    }

    fun loadHex(snapshot: Map<String, Any?>, key: String, default: Long) =
            loadValue(snapshot, key, default.hex8).hexAsULong

    fun <T> safeByteBufferAction(buffer: ByteBuffer, action: () -> T): T {
        val position = buffer.position()
        val limit = buffer.limit()

        val result = run(action)

        buffer.position(position)
        buffer.limit(limit)

        return result
    }

    fun storeByteBufferData(id: String, buffer: ByteBuffer) = storeValues(
            "${id}_buffer_pos" to buffer.position(),
            "${id}_buffer_lim" to buffer.limit()
    )

    fun restoreByteBufferData(snapshot: Map<String, Any?>, id: String, buffer: ByteBuffer) {
        val position: Int = loadValue(snapshot, "${id}_buffer_pos", 0)
        val limit: Int = loadValue(snapshot, "${id}_buffer_lim", 0)
        buffer.position(position)
        buffer.limit(limit)
    }
}