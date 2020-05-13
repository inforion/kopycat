@file:Suppress("UNCHECKED_CAST")

package ru.inforion.lab403.kopycat.serializer

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import ru.inforion.lab403.kopycat.serializer.Serializer.Companion.log
import java.util.*

fun <T : ISerializable?> Sequence<T>.deserialize(ctxt: GenericSerializer, snapshot: Any?) {
    val map = snapshot as ArrayList<Map<String, Any>>
    if (map.count() != count())
        throw IllegalStateException("Snapshot size != required size (${map.count()} != ${count()})")
    forEachIndexed { i, v -> v?.deserialize(ctxt, map[i]) }
}

fun <T : ISerializable?> Array<T>.deserialize(ctxt: GenericSerializer, snapshot: Any?) {
    if (snapshot != null) {
        val map = snapshot as ArrayList<Map<String, Any>>
        if (map.count() != count())
            throw IllegalStateException("Snapshot size != required size (${map.count()} != ${count()})")
        forEachIndexed { i, v -> v?.deserialize(ctxt, map[i]) }
    } else log.severe { "No snapshot data for object ${this.javaClass.name} -> won't be deserialized!" }
}

fun <T : ISerializable?> ArrayList<T>.deserialize(ctxt: GenericSerializer, snapshot: Any?) {
    val map = snapshot as ArrayList<Map<String, Any>>
    if (map.count() != count())
        throw IllegalStateException("Snapshot size != required size (${map.count()} != ${count()})")
    forEachIndexed { i, v -> v?.deserialize(ctxt, map[i]) }
}

fun <T : ISerializable?> Sequence<T>.restore(ctxt: GenericSerializer, snapshot: Any?) {
    val map = snapshot as ArrayList<Map<String, Any>>
    forEachIndexed { i, v -> v?.restore(ctxt, map[i]) }
}

fun <T : ISerializable?> Array<T>.restore(ctxt: GenericSerializer, snapshot: Any?) {
    val map = snapshot as ArrayList<Map<String, Any>>
    forEachIndexed { i, v -> v?.restore(ctxt, map[i]) }
}

fun <T : ISerializable?> ArrayList<T>.restore(ctxt: GenericSerializer, snapshot: Any?) {
    val map = snapshot as ArrayList<Map<String, Any>>
    forEachIndexed { i, v -> v?.restore(ctxt, map[i]) }
}

// =========================================== SIMPLE TYPES ============================================================

@Suppress("NOTHING_TO_INLINE")
inline fun <V>Array<*>.checkSnapshotAs(snapshot: Any?): ArrayList<V>? {
    if (snapshot != null) {
        snapshot as ArrayList<V>
        if (snapshot.count() != count())
            throw IllegalStateException("Snapshot size != required size (${snapshot.count()} != ${count()})")
        return snapshot
    } else {
        log.severe { "No snapshot data for object ${this.javaClass.name} -> won't be deserialized!" }
        return null
    }
}

@Suppress("UNUSED_PARAMETER")
fun <T>Array<T>.deserialize(ctxt: GenericSerializer, snapshot: Any?) {
    checkSnapshotAs<T>(snapshot)?.forEachIndexed { i, v -> this[i] = v }
}

@Suppress("UNUSED_PARAMETER")
inline fun <T, V>Array<T>.deserialize(ctxt: GenericSerializer, snapshot: Any?, transform: (V) -> T) {
    checkSnapshotAs<V>(snapshot)?.forEachIndexed { i, v -> this[i] = transform(v) }
}

fun <T>Array<T>.restore(ctxt: GenericSerializer, snapshot: Any?) = deserialize(ctxt, snapshot)
inline fun <T, V>Array<T>.restore(ctxt: GenericSerializer, snapshot: Any?, transform: (V) -> T) = deserialize(ctxt, snapshot, transform)


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
 * @param default значение по умолчанию (необязательный параметр)
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
 * @param default default value (optional)
 * @return [T] decoded variable from snapshot or default value
 * {EN}
 */
inline fun <reified T> loadValue(snapshot: Map<String, Any?>, key: String, default: T? = null): T {
    val value = snapshot[key]
    if (value == null) {
        log.warning { "Can't load field: '$key' -> using default value = $default" }
        if (default == null)
            throw IllegalArgumentException("Can't load field: '$key' -> no default value")
        return default
    }

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
    if (value::class == T::class)
        return value as T

    if (value is Int) {
        // try to decode value
        return when (T::class) {
            Byte::class -> value.toByte() as T
            Int::class -> value.toInt() as T
            Short::class -> value.toShort() as T
            Long::class -> value.toLong() as T
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
            else -> throw IllegalArgumentException("Don't know how to cast class ${T::class}")
        }
    }

    throw IllegalArgumentException("Can't cast value `$value` to ${T::class}")
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
inline fun <reified T: Enum<T>>loadEnum(snapshot: Map<String, Any?>, key: String, default: T? = null): T {
    val value = loadValue(snapshot, key, default?.toString())
    return enumValueOf(value)
}