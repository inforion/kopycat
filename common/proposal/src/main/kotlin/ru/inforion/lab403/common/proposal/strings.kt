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
package ru.inforion.lab403.common.proposal

import java.io.File

operator fun String.times(n: Int) = repeat(n)

fun String.toFile() = File(this)

fun String.splitTrim(regex: Regex) = split(regex).map { it.trim() }

fun String.splitTrim(vararg delimiters: String, ignoreCase: Boolean = false) =
        split(*delimiters, ignoreCase = ignoreCase).map { it.trim() }

fun String.splitWhitespaces() = splitTrim(Regex("\\s+"))

fun String.substringBetween(start: String, end: String): String {
    val startIndex = indexOf(start)
    require(startIndex >= 0)
    val endIndex = indexOf(end)
    require(endIndex >= 0)
    return substring(startIndex + 1, endIndex)
}