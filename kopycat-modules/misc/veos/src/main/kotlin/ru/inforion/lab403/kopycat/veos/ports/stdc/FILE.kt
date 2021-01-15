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
package ru.inforion.lab403.kopycat.veos.ports.stdc

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.veos.api.pointers.StructPointer
import ru.inforion.lab403.kopycat.veos.filesystems.impl.FileSystem
import ru.inforion.lab403.kopycat.veos.kernel.System
import ru.inforion.lab403.kopycat.veos.ports.cstdlib.EOF


class FILE constructor(sys: System, address: Long) : StructPointer(sys, address) {
    companion object {
        const val sizeOf = 8

        fun nullPtr(sys: System) = FILE(sys, 0)

        fun allocate(sys: System) = FILE(sys, sys.allocateClean(sizeOf)).apply { ungot = EOF }

        fun open(sys: System, filename: String, type: String) = allocate(sys).also {
            it.fd = sys.filesystem.open(filename, type)
            it.isOpened = true
            it.isEOF = false
            it.isError = false
        }

        fun new(sys: System, fd: Int) = allocate(sys).also {
            it.fd = fd
            it.isOpened = true
        }
    }

    var fd by int(0)
        private set

    var isOpened by bool(4) // TODO: more STDLIB-like implementation
        private set

    var isEOF by bool(5)
        private set

    var isError by bool(6)
        private set

    var ungot by int(7)
        private set

    private val haveUngot get() = ungot != EOF

    val file get() = sys.filesystem.file(fd)

    private inline fun <T>runWithError(block: (FILE) -> T) = runCatching(block).onFailure { isError = true }.getOrThrow()
    private inline fun <T>runWithEOF(block: (FILE) -> T) = runCatching(block).onFailure { isEOF = true }.getOrThrow()

    fun read() = runWithError {
        if (haveUngot) ungot.asInt.also { ungot = EOF } else {
            val data = sys.filesystem.read(fd, 1)
            if (data.isNotEmpty()) data.single().asUInt else {
                EOF.also { isEOF = true }
            }
        }
    }

    fun read(size: Int) = runWithEOF {
            val (newSize, extra) = if (haveUngot)
                size - 1 to byteArrayOf(ungot.asByte).also { ungot = EOF }
            else
                size to byteArrayOf()

            if (size < 0)
                return@runWithEOF extra

            val data = sys.filesystem.read(fd, newSize)
            if (data.isEmpty())
                isEOF = true
            extra + data
        }

    fun unget(character: Int) {
        check(!haveUngot) { "Double unget" }
        ungot = character.asByte.asUInt
    }

    fun write(byte: Int) = runWithError {
        file.write(byte) // IONotFoundError -> EBADF
    }

    fun write(bytes: ByteArray) = runWithError {
        // if closed, nothing to do
        if (isOpened) sys.filesystem.write(fd, bytes) // IONotFoundError -> EBADF
    }

    fun write(string: String) = write(string.convertToBytes())

    fun close() {
        sys.filesystem.close(fd) // IONotFoundError -> EBADF
        isOpened = false
    }

    fun seek(offset: Long, whence: Int) {
        val seek = first<FileSystem.Seek> { it.id == whence }
        sys.filesystem.seek(fd, offset, seek) // IONotFoundError -> EBADF
    }

    fun tell() = sys.filesystem.tell(fd)

    fun flush() {
        file // validate file exists
        // TODO: implement flush for RandomAccessFile
    }

    fun clearError() { isError = false }
    fun clearEOF() { isEOF = false }
}