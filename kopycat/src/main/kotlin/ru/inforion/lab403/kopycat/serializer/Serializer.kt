/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.serializer

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
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
        @Transient val log = logger()

        const val stateJsonPath = "state.json"
        const val metaJsonPath = "meta.json"
        const val waitingObjectsRetries = 50

        private fun Any.classname() = javaClass.simpleName

        private fun <T> ByteBuffer.safeByteBufferAction(action: (ByteBuffer) -> T): T {
            val position = position()
            val limit = limit()

            val result = action(this)

            position(position)
            limit(limit)

            return result
        }

        private fun ZipOutputStream.writeJsonEntry(filename: String, json: String) {
            putNextEntry(ZipEntry(filename))
            write(json.toByteArray())
        }

        private fun ZipOutputStream.writeBinaryEntry(filename: String, data: ByteBuffer) {
            putNextEntry(ZipEntry(filename))
            data.safeByteBufferAction { writeFrom(it) }
        }

        private fun ZipFile.readBinaryEntry(filename: String, output: ByteBuffer): Boolean {
            val entry = getEntry(filename) ?: return false
            getInputStream(entry).use { stream -> stream.readInto(output) }
            return true
        }

        private fun ZipFile.readJsonEntry(filename: String): String {
            val entry = getEntry(filename)
            return getInputStream(entry).use { it.readBytes().toString(Charsets.UTF_8) }
        }

        private fun ZipFile.isFileExists(filename: String) = getEntry(filename) != null

        fun createTemporaryFileFromSnapshot(data: ByteArray): File {
            val formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss")
            val suffix = "%s".format(LocalDateTime.now().format(formatter))
            return File.createTempFile("snp_", suffix).apply {
                deleteOnExit()
                writeBytes(data)
            }
        }

        fun getMetaInfo(path: String): MetaInfo? = ZipFile(path).use {
            if (it.isFileExists(metaJsonPath)) it.readJsonEntry(metaJsonPath)
                    .runCatching { parseJson<MetaInfo>() }
                    .onFailure { log.warning { "Can't get MetaInfo of '$path' -> $it" } }
                    .getOrNull() else null
        }
    }

    // java by default generate date format that can be only read on non-windows system.... have fun
    private var zipOut: ZipOutputStream? = null
    private var zipFile: ZipFile? = null
    private var cache = HashMap<String, Array<ByteArray?>>()
    private var json: String? = null
    private var snapshot: Map<String, Any>? = null

    private val jsonMapper = jsonParser()

    private val serializedObjects = mutableMapOf<Any, String>()
    private val deserializedObjects = mutableMapOf<String, Any>()
    private val prefixes = mutableListOf("root")
    private val prefix: String
        get() = prefixes.joinToString(".")

    fun <T> withPrefix(pref: String, block: () -> T): T {
        prefixes.add(pref)
        try {
            val result = block()
            prefixes.removeLast()
            return result
        }
        catch (ex: Exception) {
            prefixes.removeLast()
            throw ex
        }
    }

    fun isSerialized(obj: Any) = obj in serializedObjects

    fun getSerializedPrefix(obj: Any) = serializedObjects[obj]
    fun getDeserializedObject(pref: String) = deserializedObjects[pref]

    fun addSerializedObject(obj: Any) {
        serializedObjects[obj] = prefix
    }

    fun addDeserializedObject(obj: Any) {
        deserializedObjects[prefix] = obj
    }

    private val waitingObjects = mutableListOf<() -> Unit>()

    fun waitingObject(block: () -> Unit) = waitingObjects.add(block)

    private fun processWaitingObjects() {
        repeat(waitingObjectsRetries) {
            val copy = waitingObjects.toMutableList()
            waitingObjects.clear()

            copy.forEach { func -> func() }

            if (waitingObjects.isEmpty())
                return
        }

        throw GeneralException("Can't serialize objects: $waitingObjects")
    }

    fun serialize(path: String, comment: String? = null, entry: Long = 0): Boolean {
        log.finest { "${target.classname()} serialization started" }
        val time = measureTimeMillis {
            // subtle closing required
            File(path).parentFile.mkdirs()
            ZipOutputStream(FileOutputStream(path)).use {
                zipOut = it
                try {
                    snapshot = target.serialize(this)
                    processWaitingObjects()
                } catch (error: NotSerializableObjectException) {
                    log.warning { "Can't serialize object due to ${error.message}" }
                    return false
                }

                json = snapshot!!.writeJson(jsonMapper).apply {
                    it.writeJsonEntry(stateJsonPath, this)
                }

                // TODO: Добавить возможность добавления произвольных данных
                MetaInfo(entry, comment).writeJson(jsonMapper).apply {
                    it.writeJsonEntry(metaJsonPath, this)
                }
            }

            // {RU}
            // Когда делается serialize в переменную snapshot собираются данные и они сохраняются в том формате как
            // их туда добавляли (то есть Int, Long, Enum и т.п.), а когда делается deserialize эта переменная
            // перетирается загруженной с помощью mapper'a, там все String. Поэтому если потом сделать restore,
            // то для него будут объекты другие (int, long, enum), а для deserialize - все String.
            // Чтобы в snapshot всегда хранились String в value необходимо сделать mapper.readValue(). Тогда
            // десериализация для deserialize и restore будет одинакова.
            // {RU}
            //
            // re-read snapshot to make snapshot identical for restore and deserialize
            // if we don't re-read it then in snapshot after serialize remains cached value that
            // differ from deserialized values
            snapshot = json!!.parseJson(jsonMapper)

            // Set ZIP-file to made restore possible of binary files
            zipFile = ZipFile(path)
        }
        log.fine { "${target.classname()} was serialized for $time ms" }
        return true
    }

    fun deserialize(path: String): Serializer<T> {
        log.finest { "${target.classname()} deserialization started" }
        val time = measureTimeMillis {
            zipFile = ZipFile(path)
            zipFile!!.let { zip ->
                json = zip.readJsonEntry(stateJsonPath)
                snapshot = json!!.parseJson(jsonMapper)
                target.deserialize(this, snapshot!!)
                processWaitingObjects()
            }
        }
        log.fine { "${target.classname()} was deserialized for $time ms" }

        return this
    }

    fun deserialize(data: ByteArray) = deserialize(createTemporaryFileFromSnapshot(data).absolutePath)

    fun restore(): Serializer<T> {
        log.finest { "${target.classname()} restoration started" }
        val time = measureTimeMillis {
            if (json == null) {
                if (zipFile != null) {
                    zipFile!!.let { zip ->
                        json = zip.readJsonEntry(stateJsonPath)
                        snapshot = json!!.parseJson(jsonMapper)
                        log.info { "Snapshot loaded from zip" }
                    }
                } else log.severe { "Restore failed. No last deserialized state." }
            }
            target.restore(this, snapshot!!)
            processWaitingObjects()
        }
        log.fine { "${target.classname()} was restored for $time ms" }
        return this
    }

    fun isBinaryExists(name: String) = zipFile!!.isFileExists(name)

    fun storeBinary(name: String, output: ByteBuffer): Map<String, Any> {
        zipOut!!.writeBinaryEntry(name, output)
        return storeByteBuffer(output, false)
    }

    fun loadBinary(snapshot: Map<String, Any?>, name: String, output: ByteBuffer): Boolean {
        zipFile!!.let {
            if (it.readBinaryEntry(name, output)) {
                loadByteBuffer(snapshot, name, output, false)
                return true
            }
            return false
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
            loadByteBuffer(snapshot, name, output, false)
        }
    }
}