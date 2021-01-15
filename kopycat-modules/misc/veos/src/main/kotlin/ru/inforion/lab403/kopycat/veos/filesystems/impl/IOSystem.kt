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
package ru.inforion.lab403.kopycat.veos.filesystems.impl

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONotFoundError
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONotReadyError
import ru.inforion.lab403.kopycat.veos.filesystems.StandardStreamFile
import ru.inforion.lab403.kopycat.veos.filesystems.interfaces.IBasicFile
import ru.inforion.lab403.kopycat.veos.kernel.IdAllocator
import ru.inforion.lab403.kopycat.veos.kernel.System

class IOSystem(val sys: System): IAutoSerializable {
    companion object {
        val log = logger()
    }

    // Reserve -1 for bad descriptor
    private val descriptors = IdAllocator(end = 0xFFFF_FFFF) // TODO: reset

    private val openedFiles = mutableMapOf<Int, IBasicFile>()

    private val nonBlockingFiles = mutableSetOf<Int>()

    fun register(file: IBasicFile): Int {
        val fd = descriptors.allocate()
        openedFiles[fd] = file
        return fd
    }

    fun reserve(file: IBasicFile, fd: Int) {
        descriptors.reserve(fd)
        openedFiles[fd] = file
    }

    /**
     * This is internal use only method
     *
     * See [close] instead
     */
    private fun remove(fd: Int): IBasicFile {
        descriptors.free(fd)
        val removed = openedFiles.remove(fd) ?: throw IONotFoundError(fd)
        return removed
    }

    fun descriptorOrNull(fd: Int) = openedFiles[fd]

    fun descriptor(fd: Int) = descriptorOrNull(fd) ?: throw IONotFoundError(fd)

    fun find(predicate: (IBasicFile) -> Boolean) = openedFiles.values.find { predicate(it) }

    fun isOpen(fd: Int) = fd in openedFiles

    fun isTerm(fd: Int) = descriptor(fd) is StandardStreamFile

    fun isNonBlocking(fd: Int) = fd in nonBlockingFiles

    fun setNonBlock(fd: Int, state: Boolean) = if (state) nonBlockingFiles.add(fd) else nonBlockingFiles.remove(fd)

    fun reset() {
        openedFiles.onEach { (_, file) -> file.close() }.clear()
        descriptors.reset()
    }

    fun read(fd: Int, len: Int): ByteArray {
        val file = descriptor(fd)
        if (file.available() == 0 && isNonBlocking(fd))
            throw IONotReadyError(fd)
        val data = ByteArray(len)
        val count = file.read(data)
        if (count == -1)
            return byteArrayOf()
        return data.copyOfRange(0, count)
    }

    fun write(fd: Int, data: ByteArray) = descriptor(fd).write(data)

    fun close(fd: Int) = remove(fd).close()

    fun available(fd: Int) = descriptor(fd).available()

    fun readable(fd: Int) = descriptor(fd).readable()

    fun writable(fd: Int) = descriptor(fd).writable()
}