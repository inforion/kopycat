/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
@file:Suppress("UNCHECKED_CAST")

package ru.inforion.lab403.kopycat.serializer

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.json.fromJson
import ru.inforion.lab403.common.json.toJson
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.optional.Optional
import ru.inforion.lab403.common.optional.opt
import ru.inforion.lab403.common.reflection.*
import ru.inforion.lab403.common.utils.DynamicClassLoader
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable
import ru.inforion.lab403.kopycat.interfaces.IOnlyAlias
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import java.io.DataInputStream
import java.io.EOFException
import java.io.File
import java.lang.Exception
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.time.measureTimedValue


class Serializer<T : ISerializable> constructor(
    val target: T,
    val suppressWarnings: Boolean,
    val ignoreMissingSnapshot: Boolean = true
) {

    companion object {
        @Transient
        val log = logger()

        const val stateJsonPath = "state.json"
        const val metaJsonPath = "meta.json"
        const val waitingObjectsRetries = 50

        fun getMetaInfo(path: String): MetaInfo? = ZipFile(path).use {
            if (it.isFileExists(metaJsonPath)) it.readJsonEntry(metaJsonPath)
                .runCatching { fromJson<MetaInfo>() }
                .onFailure { log.warning { "Can't get MetaInfo of '$path' -> $it" } }
                .getOrNull() else null
        }

        private val primitiveTypes = listOf(
            Float::class,
            Double::class,

            Byte::class,
            Short::class,
            Int::class,
            Integer::class,
            Long::class,

            BigInteger::class,

            UByte::class,
            UShort::class,
            UInt::class,
            ULong::class,

            Boolean::class,

            String::class,

            Enum::class,

            ByteArray::class,
            ArrayList::class,

            HashMap::class,

            Optional::class
        )

        private val nonSerializablePrimitiveTypes = listOf(
            Float::class,
            Double::class,

            Byte::class,
            Short::class,
            Int::class,
            Long::class,

            BigInteger::class,

            UByte::class,
            UShort::class,
            UInt::class,
            ULong::class,

            Boolean::class,

            String::class,

            Enum::class,

            Optional::class
        )

        private val nonDeserializableContainers = arrayOf(
            emptyMap<Any, Any>()::class,
            Collections.singletonMap(0, 0)::class,
            emptySet<Any>()::class,
            Collections.singleton(0)::class
        )
    }

    var doRestore: Boolean = false
        private set

    // java by default generates date in format that cannot be read on Windows... have fun
    private var zipOut: ZipOutputStream? = null
    // keeping path because otherwise snapshot file is held by an open stream
    private var zipFilePath: String? = null
    private var cache = dictionary<String, Array<ByteArray?>>()
    private var json: String? = null
    private var snapshot: Map<String, Any>? = null

    private val serializedObjects = mutableMapOf<Any, String>()
    private val deserializedObjects = mutableMapOf<String, Any>()
    private val prefixes = mutableListOf("root")
    private val prefix get() = prefixes.joinToString(".")

    fun <T> withPrefix(pref: String, block: () -> T): T {
        prefixes.add(pref)
        try {
            val result = block()
            prefixes.removeLast()
            return result
        } catch (ex: Exception) {
            prefixes.removeLast()
            throw ex
        }
    }

    private fun isSerialized(obj: Any) = obj in serializedObjects

    private fun getSerializedPrefix(obj: Any) = serializedObjects[obj]
    private fun getDeserializedObject(pref: String) = deserializedObjects[pref]

    fun addSerializedObject(obj: Any): Boolean {
        if (obj in serializedObjects && serializedObjects[obj] != prefix)
            return false
        serializedObjects[obj] = prefix
        return true
    }

    fun addDeserializedObject(obj: Any) {
        deserializedObjects[prefix] = obj
    }

    private val waitingObjects = LinkedList<() -> Unit>()

    private fun newWaitingObject(block: () -> Unit) = waitingObjects.add(block)

    private fun executeWaitingObject() = waitingObjects.pop().also { it() }

    private fun processWaitingObjects() {
        var increase = 0
        while (waitingObjects.isNotEmpty()) {
            val count = waitingObjects.size

            executeWaitingObject()

            if (waitingObjects.size >= count) increase++

            check(increase < waitingObjectsRetries) {
                "Waiting objects count constantly increased during processing, something wrong with serialization"
            }
        }
    }

    fun serialize(file: File, comment: String? = null, entry: ULong = 0u): Boolean {
        log.finest { "Save target ${target.classname()} state to $file" }

        serializedObjects.clear()

        val (_, time) = measureTimedValue {
            // subtle closing required
            file.outputStream().toZipOutputStream().use {
                zipOut = it
                try {
                    snapshot = target.serialize(this)
                    processWaitingObjects()
                } catch (error: NotSerializableObjectException) {
                    log.warning { "Can't serialize object due to ${error.message}" }
                    return false
                }

                json = snapshot!!.toJson().apply {
                    it.writeJsonEntry(stateJsonPath, this)
                }

                // TODO: Добавить возможность добавления произвольных данных
                MetaInfo(entry, comment).toJson().apply {
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
            snapshot = json!!.fromJson()

            // Set ZIP-file to made restore possible of binary files
            zipFilePath = file.absolutePath
        }
        log.fine { "Target ${target.classname()} was saved for $time" }
        return true
    }

    fun deserialize(file: File): Serializer<T> {
        log.finest { "Load target ${target.classname()} from $file" }

        val (_, time) = measureTimedValue {
            deserializedObjects.clear()
            zipFilePath = file.absolutePath
            val zip = ZipFile(zipFilePath!!) // can't be merged with prev.
            json = zip.readJsonEntry(stateJsonPath)
            snapshot = json!!.fromJson()
            doRestore = false
            target.deserialize(this, snapshot!!)
            processWaitingObjects()
        }
        log.fine { "Target ${target.classname()} was loaded for $time from $file" }

        return this
    }

    fun restore(): Serializer<T> {
        log.finest { "Restore target ${target.classname()}" }

        val (_, time) = measureTimedValue {
            deserializedObjects.clear()
            if (json == null || snapshot == null) {
                requireNotNull(zipFilePath) { "Restore failed. No last deserialized state." }.also { zipPath ->
                    json = ZipFile(zipPath).readJsonEntry(stateJsonPath)
                    snapshot = json!!.fromJson()
                }
            }
            doRestore = true
            target.deserialize(this, snapshot!!)
            processWaitingObjects()
        }
        log.fine { "Target ${target.classname()} was restored from last saved state for $time" }

        return this
    }

    fun isBinaryExists(name: String) = ZipFile(zipFilePath!!).isFileExists(name)

    fun storeBinary(name: String, output: ByteBuffer): Map<String, Any?> {
        zipOut!!.writeBinaryEntry(name, output)
        return storeByteBuffer(output, false)
    }

    fun loadBinary(snapshot: Map<String, Any?>, name: String, output: ByteBuffer): Boolean = zipFilePath!!.let {
        if (ZipFile(it).readBinaryEntry(name, output)) {
            loadByteBuffer(snapshot, name, output, false)
            return true
        }
        return false
    }

    fun restoreBinary(
        snapshot: Map<String, Any?>,
        name: String,
        output: ByteBuffer,
        dirtyPages: Set<UInt>,
        pageSize: Int
    ) = zipFilePath!!.let { zipPath ->
        val zip = ZipFile(zipPath)
        val entry = zip.getEntry(name)
        zip.getInputStream(entry).use { stream ->
            var pos = 0L

            val fixedPageSize = if (output.limit() < pageSize) output.limit() else pageSize
            val pageCount = output.limit() / pageSize + if ((output.limit() % pageSize) > 0) 1 else 0

            val cacheEntry = cache.getOrPut(name) { Array(pageCount) { null } }

            val dis = DataInputStream(stream)
            dirtyPages.sorted().forEach { page ->
                val index = (page / fixedPageSize.uint).int
                var cachedData = cacheEntry[index]
                if (cachedData == null) {
                    pos += dis.skip(page.int - pos)
                    val len = minOf(fixedPageSize.long_s, output.limit() - pos)
                    cachedData = ByteArray(len.int)

                    try { dis.readFully(cachedData, 0, len.int) }
                    catch (e: EOFException) {
                        log.severe {
                            "[$name] Unable to restore page at 0x${page.hex} with size 0x${pageSize.hex}. " +
                                    "Broken snapshot?"
                        }
                        throw e;
                    }

                    pos += len
                    cacheEntry[index] = cachedData
                }
                output.position(page.int)
                output.put(cachedData)
            }
        }

        loadByteBuffer(snapshot, name, output, false)
    }

    private val subtypes = mutableMapOf<Pair<KProperty1<*, *>, KClass<*>>, Boolean>()

    private fun KProperty1<*, *>.isSubtypeOfCached(kClass: KClass<*>) =
        subtypes.getOrPut(this to kClass) { isSubtypeOf(kClass) }

//    private fun KProperty1<*, *>.isSubtypeOfCached(kClass: KClass<*>) = isSubtypeOf(kClass)

    private inline val <T : Any> KClass<T>.serializableMemberProperties: List<KProperty1<T, *>>
        get() = memberProperties.filter {
            val toSkip = it.findAnnotation<DontAutoSerialize>() != null
                    || !it.isVariable && nonSerializablePrimitiveTypes.any { ac -> it.isSubtypeOfCached(ac) } // is primitive val
                    || it.isSubtypeOfCached(Component::class) // is Component
                    || it.isSubtypeOfCached(Module.Area::class)
                    || it.isSubtypeOfCached(Module.Register::class)
                    || it.javaField == null // is getter without backing field
                    || allSuperclasses.find { sc -> sc.java == it.javaField?.declaringClass } != null // is declared in this class
            !toSkip
        }


    private fun serializePrimitive(value: Any?): Any? = when (value) {
        is Float -> value.ieee754AsUnsigned()
        is Double -> value.ieee754AsUnsigned()
        is BigInteger -> value.toString()
        is Number -> value
        is UByte -> value
        is UShort -> value
        is UInt -> value
        is ULong -> value
        is String -> value
        is Boolean -> value
        is Optional<*> -> serializeItem(value.orNull, "Optional")
        is Enum<*> -> value.name
        is ArrayList<*> -> value.mapIndexed { i, it -> serializeItem(it, i.toString()) }
        is ByteArray -> value.mapIndexed { i, it -> serializeItem(it, i.toString()) }
        is HashMap<*, *> -> value.entries.mapIndexed { i, it ->
            serializeItem(it.key, "$i.key") to serializeItem(it.value, "$i.value")
        }
        null -> value
        else -> throw IllegalArgumentException("Wrong primitive type ${value::class.java.name}")
    }

    @Suppress("DefaultTypecastExtensions")
    @PublishedApi
    internal fun deserializePrimitive(value: Any?, cls: Class<*>): Any? {
        if (value == null)
            return null

        return when (cls) {
            Float::class.java -> {
                check(value is Int) { "value should be a Int" }
                value.ieee754()
            }
            Double::class.java -> when (value) {
                is Int -> value.ulong_z.ieee754()
                is Long -> value.ieee754()
                is UInt -> value.ulong_z.ieee754()
                is ULong -> value.ieee754()
                else -> throw IllegalArgumentException("value should be a Long or Int")
            }
            java.lang.Byte::class.java -> {
                check(value is Int) { "value should be a Int" }
                value.byte
            }
            Byte::class.java -> {
                check(value is Int) { "value should be a Int" }
                value.byte
            }
            Short::class.java -> {
                check(value is Int) { "value should be a Int" }
                value.short
            }
            Integer::class.java -> {
                check(value is Int) { "value should be a Int" }
                value.toInt()
            }
            Int::class.java -> {
                check(value is Int) { "value should be a Int" }
                value
            }
            Long::class.java -> when (value) {
                is Int -> value.long_z
                is Long -> value
                else -> throw IllegalArgumentException("value should be a Long or Int")
            }
            UByte::class.java -> {
                check(value is Int) { "value should be a Int" }
                value.ubyte
            }
            UShort::class.java -> {
                check(value is Int) { "value should be a Int" }
                value.ushort
            }
            UInt::class.java -> when (value) {
                is Int -> value.uint
                is Long -> value.ulong
                is UInt -> value
                is ULong -> value.uint
                else -> throw IllegalArgumentException("value should be a Long or Int")
            }
            ULong::class.java -> when (value) {
                is ULong -> value
                is Int -> value.ulong_s
                is Long -> value.ulong
                else -> throw IllegalArgumentException("value should be a Long or Int")
            }
            BigInteger::class.java -> BigInteger(value as String)
            java.lang.Boolean::class.java -> {
                check(value is Boolean) { "value should be a Boolean" }
                value
            }
            Boolean::class.java -> {
                check(value is Boolean) { "value should be a Boolean" }
                value
            }
            String::class.java -> {
                check(value is String) { "value should be a String" }
                value
            }
            ByteArray::class.java -> {
                check(value is Iterable<*>)
                value.mapIndexed { i, it -> deserializeItem(it as Map<String, Any>, i.toString()) as Byte }
                    .toByteArray()
            }
            Optional::class.java -> {
                deserializeItem(value as Map<String, Any>, "Optional").opt
            }
            ArrayList::class.java -> {
                check(value is Iterable<*>)
                value.mapIndexed { i, it -> deserializeItem(it as Map<String, Any>, i.toString()) }
                    .toCollection(ArrayList())
            }
            HashMap::class.java -> {
                check(value is Iterable<*>)
                HashMap((value as Iterable<Map<*, *>>).mapIndexed { i, it ->
                    deserializeItem(it["first"] as Map<String, Any>, "$i.key") to
                            deserializeItem(it["second"] as Map<String, Any>, "$i.value")
                }.toMap())
            }
            else -> { // Enums
                val constants = cls.enumConstants.sure { "Wrong primitive type $cls" }
                (constants as Array<Enum<*>>).first { it.name == value }
            }
        }
    }

    inline fun <reified T : Any> deserializePrimitive(value: Any?) =
        deserializePrimitive(value, T::class.java).sure { "Can't deserialize null-value" } as T

    // For collections
    fun serializeItem(
        value: Any?,
        name: String,
        allowWithoutCtor: Boolean = false
    ): Map<String, Any?> =
        if (value != null && isSerialized(value)) {
            mapOf("alias" to getSerializedPrefix(value))
        } else when (value) {
            is IOnlyAlias -> mutableMapOf<String, Any?>().also {
                newWaitingObject { it.putAll(serializeItem(value, name)) }
            }
            is IConstructorSerializable -> {
                // TODO: Check that it is not object!
                val ctor = withPrefix(name) { value.serializeConstructor(this) }
                if (ctor == null) {
                    mutableMapOf<String, Any?>().also {
                        newWaitingObject { it.putAll(serializeItem(value, name)) }
                    }
                } else {
                    withPrefix(name) {
                        mapOf(
                            "snapshot" to value.serialize(this),
                            "class" to value::class.java.name,
                            "ctor" to ctor
                        )
                    }
                }
            }
            is ISerializable -> {
                require(allowWithoutCtor) {
                    "Can't serialize class ${value::class.simpleName} in $name without constructor (make it IConstructorSerializable)"
                }
                withPrefix(name) {
                    mapOf(
                        "snapshot" to value.serialize(this),
                        "class" to value::class.java.name,
                        "ctor" to null
                    )
                }
            }
            else -> {
                withPrefix(name) {
                    mapOf(
                        "value" to serializePrimitive(value),
                        "class" to if (value != null) value::class.java.name else "null"
                    )
                }
            }
        }

    private fun deserializeConstructor(ctor: KFunction<*>, args: Collection<Any>): Any? {
        val arguments = (ctor.parameters zip args).associate { (parameter, arg) ->
            val name = parameter.name.sure { "Constructor parameter must have name: $parameter" }
            parameter to deserializeItem(arg.cast(), name)
        }
        return ctor.callBy(arguments)
    }

    fun deserializeItem(value: Map<String, Any>, name: String): Any? = when {
        "alias" in value -> {
            val key = value["alias"] as String
            getDeserializedObject(key) ?: throw CantSerializeException(key)
        }
        "snapshot" in value -> {
            val snapshot = value["snapshot"] as Map<String, Any>
            val className = value["class"] as String
            val args = value["ctor"] as ArrayList<Any>

            val cls = Class.forName(className, true, DynamicClassLoader)
            val ctor = cls.kotlin.primaryConstructor
                .sure { "Only kotlin classes can be constructor serializable" }
                .also { it.isAccessible = true }

            withPrefix(name) {
                val result = deserializeConstructor(ctor, args)
                (result as ISerializable).also { it.deserialize(this, snapshot) }
            }
        }
        "value" in value -> {
            val className = value["class"] as String
            if (className != "null" || value["value"] != null) {
                val cls = Class.forName(className, true, DynamicClassLoader)
                withPrefix(name) { deserializePrimitive(value["value"], cls) }
            } else null
        }
        else -> throw IllegalArgumentException("Unknown structure")
    }


    private fun <R, T> serializePrimitiveProperty(receiver: R, item: KProperty1<R, T>): Any? =
        serializePrimitive(item.getWithAccess(receiver))

    private fun <R, T> deserializePrimitiveProperty(value: Any?, item: KProperty1<R, T>): Any? =
        deserializePrimitive(value, (item.returnType.classifier!! as KClass<*>).java)

    private fun <T> Any.asIterable() = this as Iterable<T>

    private fun <T> serializeIterable(value: Iterable<T>) =
        value.mapIndexed { i, it -> serializeItem(it, i.toString()) }

    private fun deserializeMutableCollection(value: MutableCollection<*>, snapshot: Any) {
        value.clear()
        val v = (snapshot as Iterable<*>).mapIndexed { i, it -> deserializeItem(it.cast(), i.toString()) }
        value.addAll(v as Iterable<Nothing>)
    }

    private fun <T> serializeArray(value: Array<T>) = value.mapIndexed { i, it ->
        serializeItem(it, i.toString(), true)
    }

    private fun <T> deserializeArray(value: Array<T>, snapshot: Any) {
        snapshot
            .asIterable<Map<String, Any>>()
            .forEachIndexed { i, it ->
                // required to separate ISerializable from IAutoSerializable and Primitives
                if ("ctor" in it && it["ctor"] == null)
                    (value[i] as ISerializable).deserialize(this, it["snapshot"].cast())
                else
                    (value as Array<Any?>)[i] = deserializeItem(it, i.toString())
        }
    }

    private fun serializeUIntArray(value: UIntArray) = value.map { serializePrimitive(it) }

    private fun deserializeUIntArray(value: UIntArray, snapshot: Any) {
        snapshot.asIterable<Any>().forEachIndexed { i, it -> value[i] = deserializePrimitive(it) }
    }

    private fun <K, V> serializeMutableMap(value: Map<K, V>) = value.entries.mapIndexed { i, it ->
        serializeItem(it.key, "$i.key") to serializeItem(it.value, "$i.value")
    }

    private fun <K, V> deserializeMutableMap(value: MutableMap<K, V>, snapshot: Any) {
        value.clear()
        val v = snapshot
            .asIterable<Map<*, *>>()
            .mapIndexed { i, it ->
                val k = deserializeItem(it["first"].cast(), "$i.key")
                val v = deserializeItem(it["second"].cast(), "$i.value")
                k to v
        } as Iterable<Pair<Nothing, Nothing>>
        value.putAll(v)
    }

    private fun serializeReentrantLock(value: ReentrantLock) = check(!value.isLocked) { "ReentrantLock is locked" }

    private fun deserializeReentrantLock(value: ReentrantLock, snapshot: Any) {
        check(!value.isLocked) { "ReentrantLock is locked" }
    }

    // TODO: ReentrantLock?
    private fun <R, T> serializeSpecialProperty(receiver: R, item: KProperty1<R, T>): Any? =
        when (val value = item.getWithAccess(receiver)) {
            null -> null
            is Array<*> -> serializeArray(value)
            is UIntArray -> serializeUIntArray(value)
            is Iterable<*> -> serializeIterable(value)
            is MutableMap<*, *> -> serializeMutableMap(value)
            is ReentrantLock -> serializeReentrantLock(value)
            is LongArray -> { }
            else -> throw IllegalArgumentException("Can't serialize property ${item.name} of type ${item.returnType}")
        }

    private fun <R, T> deserializeSpecialProperty(snapshot: Any, receiver: R, item: KProperty1<R, T>) {
        val value = item.getWithAccess(receiver)

        if (nonDeserializableContainers.any { it.isInstance(value) })
            return

        try {
            when (value) {
                is Array<*> -> deserializeArray(value, snapshot)
                is UIntArray -> deserializeUIntArray(value, snapshot)
                is ArrayList<*> -> deserializeMutableCollection(value, snapshot)
                is List<*>, is Set<*> -> {
                    // is immutable collection, and so we don't need to deserialize it
                }
                is MutableCollection<*> -> deserializeMutableCollection(value, snapshot)
                is MutableMap<*, *> -> deserializeMutableMap(value, snapshot)
                is ReentrantLock -> deserializeReentrantLock(value, snapshot)
                is LongArray -> { }
                else -> throw IllegalArgumentException("Can't deserialize property ${item.name} of type ${item.returnType}")
            }
        } catch (ex: CantSerializeException) {
            newWaitingObject { deserializeSpecialProperty(snapshot, receiver, item) }
        }
    }

    private fun <R, T>KProperty1<R, T>.isPrimitive(variableAllowed: Boolean): Boolean =
        variableAllowed && isVariable && primitiveTypes.any { isSubtypeOfCached(it) }

    private fun <R, T> serializeProperty(
        receiver: R,
        item: KProperty1<R, T>,
        variableAllowed: Boolean
    ): Any? = when {
        item.isSubtypeOfCached(ISerializable::class) -> (item.getWithAccess(receiver) as ISerializable?)?.serialize(this)
        item.isPrimitive(variableAllowed) -> serializePrimitiveProperty(receiver, item)
        else -> serializeSpecialProperty(receiver, item)
    }

    private fun <R, T> deserializeProperty(
        snapshot: Any?,
        receiver: R,
        item: KProperty1<R, T>,
        variableAllowed: Boolean
    ) {
        val isNull = item.getWithAccess(receiver) == null
        when {
            item.isSubtypeOfCached(ISerializable::class) && !isNull -> {
                (item.getWithAccess(receiver) as ISerializable?)?.deserialize(this, (snapshot ?: let {
                    log.warning { "Snapshot for property '${item.name}' of '$receiver' is null" }
                    mapOf<String, Any>()
                }) as Map<String, Any>)
            }
            item.isPrimitive(variableAllowed) -> {
                val value = deserializePrimitiveProperty(snapshot, item) as T
                // TODO: can't un init lateinit var
                if (!item.isLateinit || item.returnType.isMarkedNullable || value != null) {
                    try {
                        item.setWithAccess(receiver, value)
                    } catch (ex: Exception) {
                        println("Can't deserialize $item (value is $value)")
                        ex.printStackTrace()
                    }
                }
            }
            isNull -> check(snapshot == null) { "Not implemented null -> not null" }
            else -> when {
                snapshot != null -> deserializeSpecialProperty(snapshot, receiver, item)
                else -> if (ignoreMissingSnapshot) {
                    log.severe { "Data in snapshot for field $item not found so it will be left in initial state" }
                } else {
                    error("Data in snapshot not found for field $item")
                }
            }
        }
    }

    internal fun <T : Any, R : Any> serializeObject(receiver: T, cls: KClass<R>): Map<String, Any?> {
        if (!addSerializedObject(receiver))
            return mapOf()
        val result = cls.serializableMemberProperties.associate {
            it.name to withPrefix(it.name) {
                val delegate = it.getDelegateWithAccess(receiver)
                if (delegate != null) {
                    if (delegate is ISerializable) {
                        delegate.serialize(this)
                    } else {
                        serializeProperty(receiver, it as KProperty1<T, *>, false)
                    }
                } else {
                    serializeProperty(receiver, it as KProperty1<T, *>, true)
                }
            }
        }
        return storeValues(result as Map<String, Any>)
    }

    internal fun <T : Any, R : Any> deserializeObject(snapshot: Map<String, Any>, receiver: T, cls: KClass<R>) {
        addDeserializedObject(receiver)
        if (snapshot.isEmpty())
            return
        cls.serializableMemberProperties.forEach {
            withPrefix(it.name) {
                val concreteSnapshot = snapshot[it.name]
                val delegate = it.getDelegateWithAccess(receiver)
                if (delegate != null) {
                    if (delegate is ISerializable) {
                        delegate.deserialize(this, snapshot[it.name] as Map<String, Any>)
                    } else {
                        deserializeProperty(concreteSnapshot, receiver, it as KProperty1<T, *>, false)
                    }
                } else deserializeProperty(concreteSnapshot, receiver, it as KProperty1<T, *>, true)
            }
        }
    }
}