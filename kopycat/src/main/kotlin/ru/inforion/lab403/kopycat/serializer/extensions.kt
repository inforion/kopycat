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
@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.serializer

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.proposal.*
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.auxiliary.ClassSearcher
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
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.*
import kotlin.reflect.full.*
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


fun loadHex(snapshot: Map<String, Any?>, key: String, default: Long) =
        loadValue(snapshot, key) { default.hex8 }.hexAsULong


fun storeValues(vararg values: Pair<String, Any>) = values.associate { (name, value) ->
    name to when (value) {
        is Float -> value.ieee754()
        is Double -> value.ieee754()
        is ByteArray -> value.hexlify()
        else -> value
    }
}

fun storeByteBuffer(buffer: ByteBuffer, needStoreArray: Boolean = false): Map<String, Any> {
    val result = storeValues(
            "position" to buffer.position(),
            "limit" to buffer.limit(),
            "bigEndian" to (buffer.order() == ByteOrder.BIG_ENDIAN))

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

val nonSerializablePrimitiveTypes = listOf(
        Float::class,
        Double::class,
        Byte::class,
        Byte::class,
        Short::class,
        Int::class,
        Long::class,
        Boolean::class,
        String::class,
        Enum::class
)

inline val <T : Any> KClass<T>.serializableMemberProperties: List<KProperty1<T, *>>
    get() = this.memberProperties.filter {
        val toSkip = it.findAnnotation<DontAutoSerialize>() != null
                || !it.isVariable && nonSerializablePrimitiveTypes.any { ac -> it.isSubtypeOf(ac) } // is primitive val
                || it.isSubtypeOf(Component::class) // is Component
                || it.javaField == null // is getter without backing field
                || this.allSuperclasses.find { sc -> sc.java == it.javaField?.declaringClass } != null // is declared in this class
        !toSkip
    }


fun serializePrimitive(ctxt: GenericSerializer, value: Any?): Any? = when (value) {
    is Float -> value.ieee754()
    is Double -> value.ieee754()
    is Number -> value
    is String -> value
    is Boolean -> value
    is Enum<*> -> value.name
    is ArrayList<*> -> value.mapIndexed { i, it -> serializeItem(ctxt, it, i.toString()) }
    is ByteArray -> value.mapIndexed { i, it -> serializeItem(ctxt, it, i.toString()) }
    is HashMap<*, *> -> value.entries.mapIndexed { i, it ->
        serializeItem(ctxt, it.key, "$i.key") to serializeItem(ctxt, it.value, "$i.value")
    }
    null -> value
    else -> throw IllegalArgumentException("Wrong primitive type ${value::class.java.name}")
}

fun deserializePrimitive(ctxt: GenericSerializer, value: Any?, cls: Class<*>): Any? {
    if (value == null)
        return null

    return when (cls) {
        Float::class.java -> {
            check(value is Int) { "value should be a Int" }
            value.ieee754()
        }
        Double::class.java -> {
            when (value) {
                is Int -> value.asULong.ieee754()
                is Long -> value.ieee754()
                else -> throw IllegalArgumentException("value should be a Long or Int")
            }
        }
        Byte::class.java -> {
            check(value is Int) { "value should be a Int" }
            value.toByte()
        }
        Short::class.java -> {
            check(value is Int) { "value should be a Int" }
            value.toShort()
        }
        Int::class.java -> {
            check(value is Int) { "value should be a Int" }
            value
        }
        Long::class.java -> {
            when (value) {
                is Int -> value.asULong
                is Long -> value
                else -> throw IllegalArgumentException("value should be a Long or Int")
            }
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
            value.mapIndexed { i, it -> deserializeItem(ctxt, it as Map<String, Any>, i.toString()) as Byte }.toByteArray()
        }
        ArrayList::class.java -> {
            check(value is Iterable<*>)
            value.mapIndexed { i, it -> deserializeItem(ctxt, it as Map<String, Any>, i.toString()) }.toCollection(ArrayList())
        }
        HashMap::class.java -> {
            check(value is Iterable<*>)
            HashMap((value as Iterable<Map<*, *>>).mapIndexed { i, it ->
                deserializeItem(ctxt, it["first"] as Map<String, Any>, "$i.key") to
                        deserializeItem(ctxt, it["second"] as Map<String, Any>, "$i.value")
            }.toMap())
        }
        else -> { // Enums
            val constants = cls.enumConstants.sure { "Wrong primitive type $cls" }
            (constants as Array<Enum<*>>).first { it.name == value }
        }
    }

}

// For collections
fun serializeItem(ctxt: GenericSerializer, value: Any?, name: String): Map<String, Any?> =
        if (value != null && ctxt.isSerialized(value)) {
            mapOf("alias" to ctxt.getSerializedPrefix(value))
        } else when (value) {
            is IOnlyAlias -> {
                val alias = mutableMapOf<String, Any?>()
                ctxt.waitingObject {
                    alias.putAll(serializeItem(ctxt, value, name))
                }
                alias
            }
            is IConstructorSerializable -> {
                val ctor = ctxt.withPrefix(name) {
                    value.serializeConstructor(ctxt)
                }
                if (ctor == null) {

                    val alias = mutableMapOf<String, Any?>()
                    ctxt.waitingObject {
                        alias.putAll(serializeItem(ctxt, value, name))
                    }
                    alias
                } else {
                    ctxt.withPrefix(name) {
                        mapOf(
                                "snapshot" to value.serialize(ctxt),
                                "class" to value::class.java.name,
                                "ctor" to ctor
                        )
                    }
                }
            }
            is ISerializable -> throw IllegalArgumentException("Can't serialize class ${value::class.simpleName} in $name without constructor (make it IConstructorSerializable)")
            else -> {
                val cls = when {
                    value == null -> "null"
                    value::class.javaPrimitiveType != null -> value::class.javaPrimitiveType!!.name
                    else -> value::class.java.name
                }
                ctxt.withPrefix(name) {
                    mapOf(
                            "value" to serializePrimitive(ctxt, value),
                            "class" to cls
                    )
                }
            }
        }

private val primitiveTypes = listOf(
        Float::class,
        Double::class,
        Byte::class,
        Byte::class,
        Short::class,
        Int::class,
        Long::class,
        Boolean::class,
        String::class,
        Enum::class,
        ByteArray::class,
        ArrayList::class,
        HashMap::class
)

fun primitiveType(name: String) = primitiveTypes.find { name == it.java.name }?.java

fun deserializeConstructor(ctxt: GenericSerializer, ctors: Array<Constructor<*>>, args: ArrayList<Any>): Any? {
    ctors.forEach { ctor ->
        try {
            ctor.isAccessible = true
            val arguments = (args zip ctor.parameters).map {
                deserializeItem(ctxt, it.first as Map<String, Any>, it.second.name!!)
            }.toTypedArray()
            return ctor.newInstance(*arguments)
        } catch (ex: IllegalArgumentException) {

        } catch (ex: ClassCastException) {

        }
    }

    throw IllegalStateException("Can't find appropriate constructor")
}

fun deserializeItem(ctxt: GenericSerializer, value: Map<String, Any>, name: String): Any? = when {
    "alias" in value -> {
        val key = value["alias"] as String
        ctxt.getDeserializedObject(key) ?: throw CantSerializeException(key)
    }
    "snapshot" in value -> {
        val snapshot = value["snapshot"] as Map<String, Any>
        val className = value["class"] as String
        val ctor = value["ctor"] as ArrayList<Any>

        val cls = ClassSearcher.find(className).sure { "Class $className not found" }
        ctxt.withPrefix(name) {
            val result = deserializeConstructor(ctxt, cls.constructors, ctor)
            (result as ISerializable).apply { deserialize(ctxt, snapshot) }
        }
    }
    "value" in value -> {
        val className = value["class"] as String
        val cls = primitiveType(className) ?: ClassSearcher.find(className).sure { "Class $className not found" }

        ctxt.withPrefix(name) {
            deserializePrimitive(ctxt, value["value"], cls)
        }
    }
    else -> throw IllegalArgumentException("Unknown structure")
}


fun <R, T> serializePrimitiveProperty(ctxt: GenericSerializer, receiver: R, item: KProperty1<R, T>): Any? =
        serializePrimitive(ctxt, item.getWithAccess(receiver))

fun <R, T> deserializePrimitiveProperty(ctxt: GenericSerializer, value: Any?, item: KProperty1<R, T>): Any? =
        deserializePrimitive(ctxt, value, (item.returnType.classifier!! as KClass<*>).java)


// TODO: ReentrantLock?
fun <R, T> serializeSpecialProperty(ctxt: GenericSerializer, receiver: R, item: KProperty1<R, T>): Any {
    val value = item.getWithAccess(receiver)
    // This case for object with container type or types that should not be nullable
    // so was decided to throw exception, but if you find some crappy cases with nullable
    // container you could change it
    require(value != null) { "Object should not be null for property ${item.name}" }
    return when (value) {
        is Array<*> -> value.mapIndexed { i, it -> serializeItem(ctxt, it, i.toString()) }
        is Iterable<*> -> value.mapIndexed { i, it -> serializeItem(ctxt, it, i.toString()) }
        is MutableMap<*, *> -> {
            value.entries.mapIndexed { i, it ->
                serializeItem(ctxt, it.key, "$i.key") to serializeItem(ctxt, it.value, "$i.value")
            }
        }
        is ReentrantLock -> if (value.isLocked) throw IllegalStateException("ReentrantLock is locked") else Unit
        else -> throw IllegalArgumentException("Can't serialize property ${item.name} of type ${item.returnType}")
    }
}

val nonDeserializableContainers = arrayOf(
        emptyMap<Any, Any>()::class,
        Collections.singletonMap(0, 0)::class,
        emptySet<Any>()::class,
        Collections.singleton(0)::class
)


fun <R, T> deserializeSpecialProperty(ctxt: GenericSerializer, snapshot: Any, receiver: R, item: KProperty1<R, T>) {
    val value = item.getWithAccess(receiver)

    if (nonDeserializableContainers.any { it.isInstance(value) })
        return

    try {
        when (value) {
            is Array<*> -> {
                (snapshot as Iterable<*>).forEachIndexed { i, it ->
                    (value as Array<Any?>)[i] = deserializeItem(ctxt, it as Map<String, Any>, i.toString())
                }
            }
            is ArrayList<*> -> {
                value.clear()
                value.addAll((snapshot as Iterable<*>).mapIndexed { i, it ->
                    deserializeItem(ctxt, it as Map<String, Any>, i.toString())
                } as Iterable<Nothing>)
            }
            is List<*>, is Set<*> -> {
                // is immutable collection and so we don't need to deserialize it
            }
            is MutableCollection<*> -> {
                value.clear()
                value.addAll((snapshot as Iterable<*>).mapIndexed { i, it ->
                    deserializeItem(ctxt, it as Map<String, Any>, i.toString())
                } as Iterable<Nothing>)
            }
            is MutableMap<*, *> -> {
                value.clear()
                value.putAll((snapshot as Iterable<Map<*, *>>).mapIndexed { i, it ->
                    deserializeItem(ctxt, it["first"] as Map<String, Any>, "$i.key") to
                            deserializeItem(ctxt, it["second"] as Map<String, Any>, "$i.value")
                } as Iterable<Pair<Nothing, Nothing>>)
            }
            is ReentrantLock -> if (value.isLocked) throw IllegalStateException("ReentrantLock is locked")
            else -> throw IllegalArgumentException("Can't deserialize property ${item.name} of type ${item.returnType}")
        }
    } catch (ex: CantSerializeException) {
        ctxt.waitingObject {
            deserializeSpecialProperty(ctxt, snapshot, receiver, item)
        }
    }
}

fun <R, T> serializeProperty(
        ctxt: GenericSerializer,
        receiver: R,
        item: KProperty1<R, T>,
        variableAllowed: Boolean
): Any? = when {
    item.isSubtypeOf(ISerializable::class) -> {
        (item.getWithAccess(receiver) as ISerializable?)?.serialize(ctxt)
    }
    variableAllowed && item.isVariable &&
            primitiveTypes.any { item.isSubtypeOf(it) } -> {
        serializePrimitiveProperty(ctxt, receiver, item)
    }
    else -> serializeSpecialProperty(ctxt, receiver, item)
}

fun <R, T> deserializeProperty(
        ctxt: GenericSerializer,
        snapshot: Any?,
        receiver: R,
        item: KProperty1<R, T>,
        variableAllowed: Boolean) {

    val isNull = item.getWithAccess(receiver) == null
    when {
        item.isSubtypeOf(ISerializable::class) && !isNull -> {
            (item.getWithAccess(receiver) as ISerializable?)?.deserialize(ctxt, snapshot as Map<String, Any>)
        }
        variableAllowed && item.isVariable && primitiveTypes.any { item.isSubtypeOf(it) } -> {
            val value = deserializePrimitiveProperty(ctxt, snapshot, item) as T
            if (!item.isLateinit || item.returnType.isMarkedNullable || value != null) // TODO: can't un init lateinit var
                item.setWithAccess(receiver, value)
        }
        isNull -> {
            check(snapshot == null) { "Not implemented null -> not null" }
        }
        else -> deserializeSpecialProperty(ctxt, snapshot!!, receiver, item)
    }
}

inline fun <T : Any, R : Any> serializeObject(ctxt: GenericSerializer, receiver: T, cls: KClass<R>): Map<String, Any?> {
    ctxt.addSerializedObject(receiver)
    val result = cls.serializableMemberProperties.map {
        val result = ctxt.withPrefix(it.name) {
            val delegate = it.getDelegateWithAccess(receiver)
            if (delegate != null) {
                if (delegate is ISerializable) {
                    delegate.serialize(ctxt)
                } else {
                    serializeProperty(ctxt, receiver, it as KProperty1<T, *>, false)
                }
            } else {
                serializeProperty(ctxt, receiver, it as KProperty1<T, *>, true)
            }
        }
        it.name to result
    }.toTypedArray()
    return storeValues(*(result as Array<Pair<String, Any>>))
}

inline fun <T : Any, R : Any> deserializeObject(
        ctxt: GenericSerializer,
        snapshot: Map<String, Any>,
        receiver: T,
        cls: KClass<R>) {
    ctxt.addDeserializedObject(receiver)
    cls.serializableMemberProperties.forEach {
        ctxt.withPrefix(it.name) {
            val concreteSnapshot = snapshot[it.name]
            val delegate = it.getDelegateWithAccess(receiver)
            if (delegate != null) {
                if (delegate is ISerializable) {
                    delegate.deserialize(ctxt, snapshot[it.name] as Map<String, Any>)
                } else {
                    deserializeProperty(ctxt, concreteSnapshot, receiver, it as KProperty1<T, *>, false)
                }
            } else deserializeProperty(ctxt, concreteSnapshot, receiver, it as KProperty1<T, *>, true)
        }
    }
}

inline fun <reified T : Any> T.serializeRecursive(ctxt: GenericSerializer): Map<String, Any?> {
    val result = mutableMapOf<String, Any?>()
    forEachClass {
        if (it.isSubclassOf(IAutoSerializable::class)) {
            if (it.starProjectedType != IAutoSerializable::class.starProjectedType) {
                result += serializeObject(ctxt, this, it)
                true
            } else {
                false
            }
        } else if (it.isSubclassOf(ISerializable::class)) {
            if (it.starProjectedType != ISerializable::class.starProjectedType) {
                val mt = MethodType.methodType(Map::class.java, ctxt::class.java)
                val ctor = MethodHandles.Lookup::class.java.getDeclaredConstructor(Class::class.java)
                ctor.isAccessible = true
                try {
                    result += ctor.newInstance(it.java)
                            .findSpecial(it.java, "serialize", mt, it.java)
                            .invoke(this, ctxt) as Map<String, Any>
                } catch (ex: IllegalAccessException) {

                }
            }
            false
        } else throw IllegalArgumentException("Non-serializable class: $it")
    }
    return result
}


inline fun <reified T : Any> T.deserializeRecursive(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
    forEachClass {
        if (it.isSubclassOf(IAutoSerializable::class)) {
            if (it.starProjectedType != IAutoSerializable::class.starProjectedType) {
                deserializeObject(ctxt, snapshot, this, it)
                true
            } else {
                false
            }
        } else if (it.isSubclassOf(ISerializable::class)) {
            if (it.starProjectedType != ISerializable::class.starProjectedType) {
                // Void, Unit and "java void" all are different types!?!?
                val baseFunc = it.java.methods.first { method -> method.name == "deserialize" }
                val mt = MethodType.methodType(baseFunc.returnType, baseFunc.parameterTypes[0], baseFunc.parameterTypes[1])

                val ctor = MethodHandles.Lookup::class.java.getDeclaredConstructor(Class::class.java)
                ctor.isAccessible = true
                try {
                    ctor.newInstance(it.java)
                            .findSpecial(it.java, "deserialize", mt, it.java)
                            .invoke(this, ctxt, snapshot)
                } catch (ex: IllegalAccessException) {

                }
            }
            false
        } else throw IllegalArgumentException("Non-serializable class: $it")
    }
}
