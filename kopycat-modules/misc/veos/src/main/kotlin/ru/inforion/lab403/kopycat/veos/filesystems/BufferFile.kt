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
package ru.inforion.lab403.kopycat.veos.filesystems

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.veos.filesystems.attributes.PosixVeosFileAttributes
import ru.inforion.lab403.kopycat.veos.filesystems.interfaces.IRandomAccessFile
import java.nio.file.attribute.PosixFileAttributes
import kotlin.math.min


class BufferFile(val buffer: ByteArray): IRandomAccessFile {
    var pos = 0

    override fun open(fd: Int) = Unit

    override fun close() = Unit

    override fun read(data: ByteArray): Int {
        val toread = min(data.size, buffer.size - pos)
        buffer.copyInto(data, 0, pos, pos+toread)
        pos += toread
        return toread
    }

    override fun write(data: ByteArray): Unit = throw NotImplementedError("write() not implemented for $this")

    override fun write(string: String): Unit = write(string.bytes)

    override fun write(byte: Int): Unit = write(byteArrayOf(byte.byte))

    override fun write(char: Char): Unit = write(char.int_z8)

    override fun available() = buffer.size - pos

    override fun readable() = true

    override fun writable() = true

    override fun attributes(): PosixFileAttributes = throw NotImplementedError("attributes() not implemented for $this")

    override fun seek(position: ULong) {
        pos = position.int
    }

    override fun tell(): ULong = pos.ulong_s

    override fun size(): ULong = buffer.size.ulong_s

    override fun share(): Unit = throw NotImplementedError("share() not implemented for $this")
}