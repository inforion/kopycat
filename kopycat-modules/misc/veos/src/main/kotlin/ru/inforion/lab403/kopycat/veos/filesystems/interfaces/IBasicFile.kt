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
package ru.inforion.lab403.kopycat.veos.filesystems.interfaces

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable
import ru.inforion.lab403.kopycat.veos.filesystems.attributes.PosixVeosFileAttributes
import java.nio.file.attribute.PosixFileAttributes

interface IBasicFile: IAutoSerializable, IConstructorSerializable {
    fun open(fd: Int) = Unit

    fun close() = Unit

    fun read(data: ByteArray): Int = throw NotImplementedError("read() not implemented for $this")

    fun write(data: ByteArray): Unit = throw NotImplementedError("write() not implemented for $this")

    fun write(string: String): Unit = write(string.bytes)

    fun write(byte: Int): Unit = write(byteArrayOf(byte.byte))

    fun write(char: Char): Unit = write(char.int_z8)

    fun available() = 0

    fun readable() = true

    fun writable() = true

    fun attributes(): PosixFileAttributes = throw NotImplementedError("attributes() not implemented for $this")
}