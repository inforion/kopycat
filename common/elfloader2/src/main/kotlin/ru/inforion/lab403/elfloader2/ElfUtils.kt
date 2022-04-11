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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.elfloader2

import ru.inforion.lab403.common.extensions.INT_MAX
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.stretch
import ru.inforion.lab403.common.extensions.ulong_z

inline val ULong.requireInt: Int get() {
    require(this <= INT_MAX.ulong_z) { "Can't pass ulong directly" }
    return this.int
}

inline val UInt.requireInt: Int get() {
    require(this <= INT_MAX.ulong_z) { "Can't pass uint directly" }
    return this.int
}

inline fun ByteArray.copyOfRange(range: IntRange) = copyOfRange(range.first, range.last)

inline infix fun ULongRange.shiftUp(value: ULong) = (first + value)..(last + value)
inline infix fun ULongRange.shiftDown(value: ULong) = (first - value)..(last - value)

inline val ULongRange.requireIntRange get() = first.requireInt..last.requireInt

fun String.field(size: Int): String {
    val dsize = size/2 - 1
    return if (length <= size) stretch(size) else "${take(dsize)}..${takeLast(dsize)}".stretch(size)
}