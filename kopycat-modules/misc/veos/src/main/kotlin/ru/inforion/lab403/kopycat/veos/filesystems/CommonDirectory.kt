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

import ru.inforion.lab403.common.extensions.div
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.proposal.attributes
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONoSuchFileOrDirectory
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONotFoundError
import ru.inforion.lab403.kopycat.veos.filesystems.interfaces.IBasicFile
import java.io.File
import java.nio.file.attribute.BasicFileAttributes


class CommonDirectory(val path: String) : IBasicFile, Iterable<CommonDirectory.Descriptor> {

    companion object {
        @Transient val log = logger()
    }

    @DontAutoSerialize
    private val file = File(path)

    private var desc = -1

    init {
        if (!file.exists()) throw IONoSuchFileOrDirectory(path)
        require(file.isDirectory) { "Can't open directory $path - is not a directory" }
    }

    data class Descriptor(val name: String, val attributes: BasicFileAttributes)

    override fun open(fd: Int) = run { desc = fd }

    override fun read(data: ByteArray) = throw NotImplementedError("Use next() method for CommonDirectory!")

    override fun writable() = false

    override fun attributes() = file.attributes()

    class DirectoryIterator(val file: File, desc: Int) : Iterator<Descriptor>, IAutoSerializable {
        private val dirs = file.list() ?: throw IONotFoundError(desc)

        private var idx = 0

        override fun hasNext() = idx != dirs.size

        override fun next(): Descriptor {
            val name = dirs[idx++]
            val attributes = (file / name).attributes()
            return Descriptor(name, attributes)
        }
    }

    private var directoryIterator: DirectoryIterator? = null

    fun next(): Descriptor? {
        if (directoryIterator == null) {
            directoryIterator = iterator()
        }

        directoryIterator!!.run {
            if (!hasNext()) {
                directoryIterator = null
                return null
            }

            return next()
        }
    }

    override fun iterator() = DirectoryIterator(file, desc)
}