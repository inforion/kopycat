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
@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.serializer

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.optional.Optional
import ru.inforion.lab403.common.reflection.*
import ru.inforion.lab403.common.utils.DynamicClassLoader
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable
import ru.inforion.lab403.kopycat.interfaces.IOnlyAlias
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import ru.inforion.lab403.kopycat.serializer.Serializer.Companion.log
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Constructor
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField

fun <T : ISerializable?> Sequence<T>.deserialize(ctxt: GenericSerializer, snapshot: Any?) {
    val map = snapshot as ArrayList<Map<String, Any>>
    check(map.count() == count()) { "Snapshot size != required size (${map.count()} != ${count()})" }
    forEachIndexed { i, v -> v?.deserialize(ctxt, map[i]) }
}

fun <T : ISerializable?> Array<T>.deserialize(ctxt: GenericSerializer, snapshot: Any?) {
    if (snapshot != null) {
        val map = snapshot as ArrayList<Map<String, Any>>
        check(map.count() == count()) { "Snapshot size != required size (${map.count()} != ${count()})" }
        forEachIndexed { i, v -> v?.deserialize(ctxt, map[i]) }
    } else log.severe { "No snapshot data for object ${this.javaClass.name} -> won't be deserialized!" }
}

fun <T : ISerializable?> ArrayList<T>.deserialize(ctxt: GenericSerializer, snapshot: Any?) {
    val map = snapshot as ArrayList<Map<String, Any>>
    check(map.count() == count()) { "Snapshot size != required size (${map.count()} != ${count()})" }
    forEachIndexed { i, v -> v?.deserialize(ctxt, map[i]) }
}

// =========================================== SIMPLE TYPES ============================================================

@Suppress("NOTHING_TO_INLINE")
inline fun <V> Array<*>.checkSnapshotAs(snapshot: Any?): ArrayList<V>? = if (snapshot != null) {
    snapshot as ArrayList<V>
    check(snapshot.count() == count()) { "Snapshot size != required size (${snapshot.count()} != ${count()})" }
    snapshot
} else {
    log.severe { "No snapshot data for object ${this.javaClass.name} -> won't be deserialized!" }
    null
}

@Suppress("UNUSED_PARAMETER")
fun <T> Array<T>.deserialize(ctxt: GenericSerializer, snapshot: Any?) {
    checkSnapshotAs<T>(snapshot)?.forEachIndexed { i, v -> this[i] = v }
}

@Suppress("UNUSED_PARAMETER")
inline fun <T, V> Array<T>.deserialize(ctxt: GenericSerializer, snapshot: Any?, transform: (V) -> T) {
    checkSnapshotAs<V>(snapshot)?.forEachIndexed { i, v -> this[i] = transform(v) }
}

fun <T> Array<T>.restore(ctxt: GenericSerializer, snapshot: Any?) = deserialize(ctxt, snapshot)

inline fun <T, V> Array<T>.restore(ctxt: GenericSerializer, snapshot: Any?, transform: (V) -> T) =
    deserialize(ctxt, snapshot, transform)


/**
 * {RU}
 * Этот метод служит для загрузки примитивов из снимка состояния
 * Доступные примитивы:
 * [Boolean], [Byte], [Int], [Short], [Long], [Float], [Double]
 *
 * ВНИМАНИЕ!
 * НЕ используйте этот метод для загрузки переменных типа [Enum]!
 * Используйте метод [loadEnum]
 * ВНИМАНИЕ!
 *
 * @param snapshot снимок состояния, из которого будет загружена переменная
 * @param key ключ, по которому будет браться переменная из снимка состояния
 * @return [T] декодированная переменная из снимка или значение по умолчанию
 * {RU}
 *
 * {EN}
 * This method loads a primitive from a snapshot
 * Available primitives:
 * [Boolean], [Byte], [Int], [Short], [Long], [Float], [Double]
 *
 * WARNING!
 * DON'T use this method for load [Enum] values!
 * Use [loadEnum] method
 * WARNING!
 *
 * @param snapshot image of current state
 * @param key the key by which the variable from the snapshot will be taken
 * @return [T] decoded variable from snapshot or default value
 * {EN}
 */
@Suppress("SSBasedInspection")
inline fun <reified T> loadValue(snapshot: Map<String, Any?>, key: String): T {
    val value = snapshot[key]

    require(value != null) { "Can't load field: '$key' -> no default value" }

    // check if can decode as IEEE754 value
    if (T::class == Float::class && value is Int)
        return value.ieee754() as T

    if (T::class == Double::class) {
        if (value is Int)
            return value.toULong().ieee754() as T

        if (value is Long)
            return value.ieee754() as T
    }

    // common way
    if (value::class.isSubclassOf(T::class))
        return value as T

    if (value is Int) {
        // try to decode value
        return when (T::class) {
            Byte::class -> value.toByte() as T
            Int::class -> value.toInt() as T
            Short::class -> value.toShort() as T
            Long::class -> value.toLong() as T
            UByte::class -> value.toUByte() as T
            UShort::class -> value.toUShort() as T
            UInt::class -> value.toUInt() as T
            ULong::class -> value.toULong() as T
            else -> throw IllegalArgumentException("Don't know how to cast class ${T::class}")
        }
    }

    if (value is Long) {
        // try to decode value
        return when (T::class) {
            Byte::class -> value.toByte() as T
            Int::class -> value.toInt() as T
            Short::class -> value.toShort() as T
            Long::class -> value.toLong() as T
            UByte::class -> value.toUByte() as T
            UShort::class -> value.toUShort() as T
            UInt::class -> value.toUInt() as T
            ULong::class -> value.toULong() as T
            else -> throw IllegalArgumentException("Don't know how to cast class ${T::class}")
        }
    }

    if (value is Float) {
        // try to decode value
        return when (T::class) {
            Byte::class -> value.toInt().toByte() as T
            Int::class -> value.toInt() as T
            Short::class -> value.toInt().toShort() as T
            Long::class -> value.toLong() as T

            UByte::class -> value.toInt().toUByte() as T
            UShort::class -> value.toInt().toUShort() as T
            UInt::class -> value.toUInt() as T
            ULong::class -> value.toULong() as T

            Float::class -> value as T
            Double::class -> value.toDouble() as T
            else -> throw IllegalArgumentException("Don't know how to cast class ${T::class}")
        }
    }

    if (value is Double) {
        // try to decode value
        return when (T::class) {
            Byte::class -> value.toInt().toByte() as T
            Int::class -> value.toInt() as T
            Short::class -> value.toInt().toShort() as T
            Long::class -> value.toLong() as T

            UByte::class -> value.toInt().toUByte() as T
            UShort::class -> value.toInt().toUShort() as T
            UInt::class -> value.toUInt() as T
            ULong::class -> value.toULong() as T

            Float::class -> value.toFloat() as T
            Double::class -> value as T
            else -> throw IllegalArgumentException("Don't know how to cast class ${T::class}")
        }
    }

    // something goes wrong (only string can convert to other classes)
    if (value is String) {
        // try to decode value
        return when (T::class) {
            Float::class -> value.toFloat() as T
            Double::class -> value.toDouble() as T
            Boolean::class -> value.toBoolean() as T
            Byte::class -> value.toByte() as T
            Int::class -> value.toInt() as T
            Short::class -> value.toShort() as T
            Long::class -> value.toLong() as T
            UByte::class -> value.toUByte() as T
            UShort::class -> value.toUShort() as T
            UInt::class -> value.toUInt() as T
            ULong::class -> value.toULong() as T
            ByteArray::class -> value.unhexlify() as T
            else -> throw IllegalArgumentException("Don't know how to cast class ${T::class}")
        }
    }

    throw IllegalArgumentException("Can't cast value `$value` of class `${value::class}` to ${T::class}")
}

/**
 * {EN}
 * Default null value not used for optimization purpose (because can't inline nullable)
 *
 * @see [loadValue]
 *
 * @param snapshot image of current state
 * @param key the key by which the variable from the snapshot will be taken
 * @param default default value (optional)
 *
 * @return [T] decoded variable from snapshot or default value
 * {EN}
 */
inline fun <reified T> loadValue(snapshot: Map<String, Any?>, key: String, default: () -> T): T {
    if (key !in snapshot) {
        val defaultValue = default()
        log.warning { "Can't load field: '$key' -> using default value = $defaultValue" }
        return default()
    }

    return loadValue(snapshot, key)
}

/**
 * {RU}
 * Этот метод служит для загрузки [Enum] значения из снимка состояния
 *
 * @param snapshot снимок состояния, из которого будет загружена переменная
 * @param key ключ, по которому будет браться переменная из снимка состояния
 * @param default значение по умолчанию (необязательный параметр)
 * @return [T] декодированная переменная из снимка или значение по умолчанию
 * {RU}
 *
 * {EN}
 * This method loads a [Enum] value from a snapshot
 *
 * @param snapshot image of current state
 * @param key the key by which the variable from the snapshot will be taken
 * @param default default value (optional)
 * @return [T] decoded enum value from snapshot or default value
 * {EN}
 */
inline fun <reified T : Enum<T>> loadEnum(snapshot: Map<String, Any?>, key: String, default: T? = null): T {
    val value = loadValue(snapshot, key) {
        require(default != null) { "Can't load field: '$key' -> no default value" }
        default.toString()
    }
    return enumValueOf(value)
}


fun loadHex(snapshot: Map<String, Any?>, key: String, default: ULong): ULong =
    loadValue(snapshot, key) { default.hex8 }.ulongByHex


internal fun convertValue(value: Any?) = when (value) {
    is Float -> value.ieee754AsUnsigned()
    is Double -> value.ieee754AsUnsigned()
    is ByteArray -> value.hexlify()
    else -> value
}


fun storeValues(vararg values: Pair<String, Any>) =
    values.associate { (name, value) -> name to convertValue(value) } as Map<String, Any>

fun storeValues(values: Map<String, Any>) =
    values.mapValues { (_, value) -> convertValue(value) } as Map<String, Any>

fun storeByteBuffer(buffer: ByteBuffer, needStoreArray: Boolean = false): Map<String, Any?> {
    val result = storeValues(
        "position" to buffer.position(),
        "limit" to buffer.limit(),
        "bigEndian" to (buffer.order() == ByteOrder.BIG_ENDIAN)
    )

    if (needStoreArray)
        return result + storeValues("array" to buffer.array().hexlify())

    return result
}

fun loadByteBuffer(snapshot: Map<String, Any?>, key: String, buffer: ByteBuffer, needLoadArray: Boolean = false) {
    val data = snapshot[key] as Map<String, Any>

    val position: Int = loadValue(data, "position")
    val limit: Int = loadValue(data, "limit")
    val bigEndian: Boolean = loadValue(data, "bigEndian")

    if (needLoadArray) {
        val array: ByteArray = loadValue(data, "array")
        buffer.rewind()
        buffer.put(array)
    }

    buffer.position(position)
    buffer.limit(limit)
    if (bigEndian) buffer.order(ByteOrder.BIG_ENDIAN) else buffer.order(ByteOrder.LITTLE_ENDIAN)
}

internal inline fun <reified T : Any> T.forEachClass(block: (KClass<*>) -> Boolean) {
    val queue = LinkedList<KClass<*>>()
    queue.offer(this::class)
    while (!queue.isEmpty()) {
        val it = queue.poll()
        if (block(it))
            it.superclasses.forEach { cls -> if (cls != Any::class) queue.offer(cls) }
    }
}


internal fun Any.classname() = javaClass.simpleName

internal fun <T> ByteBuffer.safeByteBufferAction(action: (ByteBuffer) -> T): T {
    val position = position()
    val limit = limit()

    val result = action(this)

    position(position)
    limit(limit)

    return result
}

internal fun ZipOutputStream.writeJsonEntry(filename: String, json: String) {
    putNextEntry(ZipEntry(filename))
    write(json.toByteArray())
}

internal fun ZipOutputStream.writeBinaryEntry(filename: String, data: ByteBuffer) {
    putNextEntry(ZipEntry(filename))
    data.safeByteBufferAction { writeBufferData(it) }
}

internal fun ZipFile.readBinaryEntry(filename: String, output: ByteBuffer): Boolean {
    val entry = getEntry(filename) ?: return false
    getInputStream(entry).use { stream -> stream.readBufferData(output) }
    return true
}

internal fun ZipFile.readJsonEntry(filename: String): String {
    val entry = getEntry(filename)
    return getInputStream(entry).use { it.readBytes().toString(Charsets.UTF_8) }
}

internal fun ZipFile.isFileExists(filename: String) = getEntry(filename) != null