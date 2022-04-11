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
package ru.inforion.lab403.elfloader2

import ru.inforion.lab403.common.extensions.uint
import java.nio.ByteBuffer
import java.nio.ByteOrder

interface IElfDataTypes {
    val data: ByteBuffer

    val ptrSize: UInt

    val byte: UByte
    val half: UShort
    val word: UInt
    val sword: Int
    val xword: ULong
    val sxword: Long
    val addr: ULong
    val off: ULong

    val wordpref: ULong
    val swordpref: Long

    var position: Int
        get() = data.position()
        set(value) { data.position(value) }

    val uint get() = data.uint

    fun get(value: ByteArray) = data.get(value)
    fun get(value: ByteArray, offset: Int, length: Int) = data.get(value, offset, length)
    fun get(offset: Int, allocSize: Int, readSize: Int): ByteArray {
        return ByteArray(allocSize).also {
            position = offset
            data.get(it, 0, readSize)
        }
    }

    var order: ByteOrder get() = data.order(); set(value) { data.order(value)}
    fun wrap(byteArray: ByteArray): ByteBuffer = ByteBuffer.wrap(byteArray).apply { order(order) }
}