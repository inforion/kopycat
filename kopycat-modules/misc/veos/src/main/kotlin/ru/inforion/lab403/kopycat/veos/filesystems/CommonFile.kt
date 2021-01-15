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
package ru.inforion.lab403.kopycat.veos.filesystems

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.toFile
import ru.inforion.lab403.common.proposal.attributes
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.veos.exceptions.io.IOFileExists
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONoSuchFileOrDirectory
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONotFoundError
import ru.inforion.lab403.kopycat.veos.filesystems.interfaces.IRandomAccessFile
import java.io.File
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFileAttributes


class CommonFile(val path: String, private val flags: AccessFlags) : IRandomAccessFile {

    private var desc = -1

    private var shares = 1

    init {
        when {
            File(path).exists() -> if (flags.create && flags.exclusive) throw IOFileExists(path)
            !flags.create -> throw IONoSuchFileOrDirectory(path)
        }
    }

    @DontAutoSerialize
    private val file = RandomAccessFile(path, "rw").apply {
        if (flags.truncate) setLength(0)
        if (flags.append && !flags.readable) seek(length())
    }

    override fun read(data: ByteArray): Int {
        // REVIEW: use new different exception and translate it into EBADF
        if (!flags.readable) throw IONotFoundError(desc)
        return file.read(data)
    }

    override fun write(data: ByteArray) {
        // REVIEW: use new different exception and translate it into EBADF
        if (!flags.writable) throw IONotFoundError(desc)
        if (flags.append)
            file.seek(file.length())
        file.write(data)
    }

    override fun seek(position: Long) = file.seek(position)

    override fun tell() = file.filePointer

    override fun size(): Long = file.length()

    override fun available() = (file.length() - file.filePointer).asInt

    override fun readable() = available() > 0

    override fun writable() = true

    override fun share(): Unit = run { shares++ }

    override fun open(fd: Int) = run { desc = fd }

    override fun close() {
        shares--
        if (shares == 0) {
            desc = -1
            file.close()
        }
    }

    override fun attributes() = path.toFile().attributes()
}