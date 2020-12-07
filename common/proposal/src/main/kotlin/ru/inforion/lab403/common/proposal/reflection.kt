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
@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package ru.inforion.lab403.common.proposal

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import ru.inforion.lab403.common.extensions.className
import java.lang.reflect.Field
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.superclasses
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaType

/**
 * {EN}
 * Implements 'reverse' [Class.isAssignableFrom] for type of [KProperty1] field
 *
 * @param cls basic Java class
 *
 *
 * @return true if type of field is subtype of specified class [cls]
 * {EN}
 */

fun KType.stringify() = this.toString().className()

fun <T>KCallable<T>.stringify() = buildString {
    val visibility = visibility
    if (visibility != null) {
        visibility.ordinal
        append(visibility.name.toLowerCase())
        append(" ")
    }

    if (this@stringify !is KProperty<*>) {
        append("fun ")
        append(name)
        append("(")
        val params = parameters
                .filter { it.name != null }
                .joinToString(", ") { "${it.name}: ${it.type.stringify()}" }
        append(params)
        append(")")
    } else {
        if (isLateinit) append("lateinit ")
        if (isConst) append("val ") else append("var ")
        append(name)
    }
    append(": ")
    append(returnType.stringify())
}

fun <T: Any> KClass<T>.new(vararg args: Any?) = constructors.first().call(args)

fun <T: Any> KClass<T>.new(vararg args: Any?, predicate: (KFunction<T>) -> Boolean) =
        constructors.first(predicate).call(args)

inline fun <reified T> subtypesScan(classpath: String): Set<Class<out T>> {
    val urls = ClasspathHelper.forPackage(classpath)
    val reflections = Reflections(urls, SubTypesScanner())
    return reflections.getSubTypesOf(T::class.java)
}

inline fun <R, T, D> KProperty1<R, T>.withAccess(block: () -> D): D {
    isAccessible = true
    return block()
}

inline fun <R, T> KProperty1<R, T>.getWithAccess(receiver: R): T = withAccess { get(receiver) }

inline fun <R, T> KProperty1<R, T>.setWithAccess(receiver: R, value: T) = withAccess {
    (this as KMutableProperty1<R, T>).set(receiver, value)
}

inline fun <R, T> KProperty1<*, T>.getDelegateWithAccess(receiver: R) = (this as KProperty1<R, T>).withAccess {
    getDelegate(receiver)
}

inline val <R, T>KProperty1<R, T>.isVariable: Boolean get() = this is KMutableProperty<*>

inline fun <R, T> KProperty1<R, T>.isSubtypeOf(kc: KClass<*>): Boolean = returnType.withNullability(false).isSubtypeOf(kc.starProjectedType)

fun Class<*>.findFieldRecursive(name: String): Field? {
    if (this == Any::class.java)
        return null

    try {
        return getDeclaredField(name)
    }
    catch (ex: NoSuchFieldException) {
        check(superclass != null) {
            "Superclass must not be null"
        }

        val scResult = superclass.findFieldRecursive(name)
        if (scResult != null)
            return scResult

        interfaces.forEach {
            val iResult = it.findFieldRecursive(name)
            if (iResult != null)
                return iResult
        }

        return null
    }
}

inline fun <reified T : Any> T.forEachClass(block: (KClass<*>) -> Boolean) {
    val queue = LinkedList<KClass<*>>()
    queue.offer(this::class)
    while (!queue.isEmpty()) {
        val it = queue.poll()
        if (block(it))
            it.superclasses.forEach { cls -> if (cls != Any::class) queue.offer(cls) }
    }
}