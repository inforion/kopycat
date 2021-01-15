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
package ru.inforion.lab403.kopycat.library

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactory
import ru.inforion.lab403.kopycat.library.builders.api.InputParameterInfo
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.settings
import java.io.File

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
fun convertParameterType(name: String, value: Any?, type: String): Any? {
    if (value == null)
        return null

    if (value == "null")
        return null

    return when (type) {
        "String" -> when (value) {
            is String -> value
            else -> value.toString()
        }
        "char" -> when (value) {
            is Char -> value
            is String -> value.toCharArray().first()
            else -> null
        }
        "int" -> when (value) {
            is Int -> value
            is String -> when {
                value.startsWith("0x") -> value.removePrefix("0x").hexAsInt
                else -> value.replace(" ", "").toInt()
            }
            is Long -> value.toInt()
            else -> null
        }
        "long" -> when (value) {
            is Long -> value
            is String -> when {
                value.startsWith("0x") -> value.removePrefix("0x").hexAsULong
                else -> value.replace(" ", "").toLong()
            }
            is Int -> value.toLong()
            else -> null
        }
        "float" -> when (value) {
            is Float -> value
            is Double -> value.toFloat()
            is Int -> value.toFloat()
            is Long -> value.toFloat()
            is String -> value.toFloat()
            else -> null
        }
        "double" -> when (value) {
            is Double -> value
            is Float -> value.toDouble()
            is Int -> value.toDouble()
            is Long -> value.toDouble()
            is String -> value.toDouble()
            else -> null
        }
        "boolean" -> when (value) {
            is Boolean -> value
            is String -> value.toBoolean()
            else -> null
        }
        "File" -> when (value) {
            is File -> value
            is String -> File(value)
            else -> null
        }
        "Resource" -> when (value) {
            is Resource -> value
            is String -> Resource(value)
            else -> null
        }
        "byte[]" -> when (value) {
            is ByteArray -> value
            is String -> value.unhexlify()
            else -> null
        }
        else -> throw IllegalArgumentException("Can't convert to type $type. " +
                "Use on of available types: ${settings.availableTypes.joinToString(separator = ", ")}")
    }.sure { "Can't parse $name as $type value = $value" }
}

/**
 * {EN}Create from [parameters] array of parameters for [factory] constructor.{EN}
 */
fun parseParametersAsMap(
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

/**
 * {EN}
 * Parse input line to map of parameters
 * @line Input line with parameters (for example "arg0=100,arg1=0x200,arg2=/path/to/something")
 * @return Map with parameters (for example "arg0" to "100", "arg1" to "0x200", "arg2" to "/path/to/something"
 * {EN}
 */
fun parseParametersAsString(line: String): Map<String, Any> = line
        .split(",")
        .filter { it.isNotBlank() }
        .associate {
            val tmp = it.split("=")
            val name = tmp[0]
            val value = tmp[1]
            name to value
        }
