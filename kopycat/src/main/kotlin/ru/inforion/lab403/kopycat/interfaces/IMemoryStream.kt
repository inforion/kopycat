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
package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype


interface IMemoryStream {
    /**
     * Starting position of the start of stream
     */
    var mark: ULong

    /**
     * Current position of the of stream at the moment.
     * The value of position is incremented with each time you
     * reading stream by the corresponding number of bytes
     */
    var position: ULong

    /**
     * Value of last reading result/
     */
    val last: Int

    /**
     * Mov position to next item and read it
     */
    fun read(datatype: Datatype): ULong

    /**
     * Mov position to next item and write it
     */
    fun write(datatype: Datatype, data: ULong)

    /**
     * Read data from the top of stream without moving position
     */
    fun peek(datatype: Datatype): ULong

    fun peekOpcode(): Int = peekByte().int
    fun peekByte(): ULong = peek(Datatype.BYTE)
    fun peekWord(): ULong = peek(Datatype.WORD)
    fun peekDword(): ULong = peek(Datatype.DWORD)
    fun peekQword(): ULong = peek(Datatype.QWORD)

    /**
     * Read data and move the stream position
     */
    fun readOpcode(): Int = readByte().int

    fun readByte(): ULong = read(Datatype.BYTE)
    fun readWord(): ULong = read(Datatype.WORD)
    fun readDword(): ULong = read(Datatype.DWORD)
    fun readQword(): ULong = read(Datatype.QWORD)

    /**
     * Write data and move the stream position
     */
    fun writeByte(data: ULong) = write(Datatype.BYTE, data)
    fun writeWord(data: ULong) = write(Datatype.WORD, data)
    fun writeDword(data: ULong) = write(Datatype.DWORD, data)
    fun writeQword(data: ULong) = write(Datatype.QWORD, data)

    /**
     * Rewind the stream start position
     * I.e. position = mark.
     */
    fun rewind()

    /**
     * A value that is equal to: position - mark
     */
    val offset: Int

    /**
     * Return byte array with last read values from 0..offset
     */
    val data: ByteArray
}