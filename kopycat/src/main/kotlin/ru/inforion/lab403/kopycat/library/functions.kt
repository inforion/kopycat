/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.library

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.reflection.stringify
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactory
import ru.inforion.lab403.kopycat.library.builders.api.InputParameterInfo
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.settings.availableTypes
import java.io.File
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

internal val KType.name get() = stringify().lowercase()

internal val availableTypeMap = availableTypes.associateBy { it.name }

internal fun String.preparse2Array() =
    trim('[', ']').splitBy(",", trim = true)

/**
 * {EN}
 * Create from [value] a parameter type of [type]
 *
 * @name name of parameter to convert
 * @value value of parameter to convert
 * @type name of type to convert
 *
 * @return converted value with type of [type]
 * {EN}
 */
internal fun convertParameterType(name: String, value: Any?, type: KType): Any? {
    if (value == null)
        return null

    if (value == "null")
        return null

    val klass = type.jvmErasure

    return when (klass) {
        String::class -> when (value) {
            is String -> value
            else -> value.toString()
        }
        Char::class -> when (value) {
            is Char -> value
            is String -> value[0]
            else -> null
        }
        Int::class -> when (value) {
            is Int -> value
            is Long -> value.int
            is String -> value.int
            else -> null
        }
        Long::class -> when (value) {
            is Long -> value
            is Int -> value.long_z
            is String -> value.long
            else -> null
        }
        UInt::class -> when (value) {
            is Int -> value.uint
            is Long -> value.uint
            is String -> value.uint
            else -> null
        }
        ULong::class -> when (value) {
            is Int -> value.ulong_z
            is Long -> value.ulong
            is String -> value.ulong
            else -> null
        }
        Float::class -> when (value) {
            is Float -> value
            is Double -> value.float
            is Int -> value.float
            is Long -> value.float
            is String -> value.float
            else -> null
        }
        Double::class -> when (value) {
            is Double -> value
            is Float -> value.double
            is Int -> value.double
            is Long -> value.double
            is String -> value.double
            else -> null
        }
        Boolean::class -> when (value) {
            is Boolean -> value
            is Int -> value.truth
            is Long -> value.truth
            is String -> value.bool
            else -> null
        }
        Array::class -> when (value) {
            is Array<*> -> value
            is String -> value
                .preparse2Array()
                .map { convertParameterType(name, it, requireNotNull(type.classifier).starProjectedType) }
                .toTypedArray()
            else -> null
        }
        IntArray::class -> when (value) {
            is IntArray -> value
            is String -> value
                .preparse2Array()
                .map { it.int }
                .toIntArray()
            else -> null
        }
        LongArray::class -> when (value) {
            is LongArray -> value
            is String -> value
                .preparse2Array()
                .map { it.long }
                .toLongArray()
            else -> null
        }
        UIntArray::class -> when (value) {
            is UIntArray -> value
            is String -> value
                .preparse2Array()
                .map { it.uint }
                .toUIntArray()
            else -> null
        }
        ULongArray::class -> when (value) {
            is ULongArray -> value
            is String -> value
                .preparse2Array()
                .map { it.ulong }
                .toULongArray()
            else -> null
        }
        File::class -> when (value) {
            is File -> value
            is String -> value.toFile()
            else -> null
        }
        Resource::class -> when (value) {
            is Resource -> value
            is String -> Resource(value)
            else -> null
        }
        ByteArray::class -> when (value) {
            is ByteArray -> value
            is String -> value.unhexlify()
            else -> null
        }
        else -> throw IllegalArgumentException("Can't convert to type $type. " +
                "Use on of available types: ${availableTypes.joinToString(separator = ", ")}")
    }.sure { "Can't parse $name as $type value = $value" }
}

/**
 * {EN}Create from [parameters] array of parameters for [factory] constructor.{EN}
 */
internal fun parseParametersAsMap(
        factory: IModuleFactory,
        parameters: Map<String, InputParameterInfo>
) = factory.parameters.mapNotNull {
    if (it.name in parameters) {
        val value = parameters.getValue(it.name).value
        it.name to convertParameterType(it.name, value, it.type)
    } else when {
        it.default !is Unit -> {
            it.name to convertParameterType(it.name, it.default, it.type)
        }
        it.optional -> null
        else -> throw IllegalArgumentException("Can't parse parameter ${it.name} because it isn't optional, value not specified and default not found!")
    }
}.toMap()

internal val regexSplitCommaOutsideSquareBrackets = ",(?=[^\\]]*(?:\\[|\$))".toRegex()

/**
 * {EN}
 * Parse input line to map of parameters
 * @line Input line with parameters (for example "arg0=100,arg1=0x200,arg2=/path/to/something")
 * @return Map with parameters (for example "arg0" to "100", "arg1" to "0x200", "arg2" to "/path/to/something"
 * {EN}
 */
internal fun parseParametersAsString(line: String): Map<String, Any> = line
        .split(regexSplitCommaOutsideSquareBrackets)
        .filter { it.isNotBlank() }
        .associate {
            val tmp = it.split("=")
            val name = tmp[0]
            val value = tmp[1]
            name to value
        }
