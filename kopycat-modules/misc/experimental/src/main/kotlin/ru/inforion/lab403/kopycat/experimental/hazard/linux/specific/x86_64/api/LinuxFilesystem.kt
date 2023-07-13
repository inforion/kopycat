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
package ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.api

import ru.inforion.lab403.common.extensions.chunks
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.api.interfaces.LinuxFilpApi
import ru.inforion.lab403.kopycat.experimental.hazard.linux.specific.x86_64.api.interfaces.LinuxVfsRWApi
import ru.inforion.lab403.kopycat.experimental.linux.common.buildLinuxFileControl
import java.io.InputStream
import java.io.OutputStream


class LinuxFilesystem<T>(
    val raw: T
) where T : LinuxVfsRWApi, T : LinuxFilpApi {
    fun fileRead(fileName: String): ByteArray {
        val filePointer = raw.filpOpen(fileName, buildLinuxFileControl { RDONLY }, 0b110_110_110)

        var result = ByteArray(0x0)
        val bucketSize = 1024uL
        var currentSize = bucketSize
        var fileIterator = 0x0uL
        while (currentSize == bucketSize) {
            raw.vfsRead(filePointer, bucketSize, fileIterator).also {
                fileIterator = it.fileIterator
                currentSize = it.resultSize
                result += it.data.slice(0 until it.resultSize.int)
            }
        }
        raw.filpClose(filePointer)
        return result
    }

    fun fileRead(fileName: String, stream: OutputStream) {
        val filePointer = raw.filpOpen(fileName, buildLinuxFileControl { RDONLY }, 0b110_110_110)

        val bucketSize = 1024uL
        var currentSize = bucketSize
        var fileIterator = 0x0uL
        while (currentSize == bucketSize) {
            raw.vfsRead(filePointer, bucketSize, fileIterator).also {
                fileIterator = it.fileIterator
                currentSize = it.resultSize
                stream.write(it.data.sliceArray(0 until it.resultSize.int))
            }
        }
        raw.filpClose(filePointer)
    }

    private fun flipOpenWriteCreate(fileName: String) = raw.filpOpen(
        fileName,
        buildLinuxFileControl { WRONLY; CREAT; TRUNC }, 0b111_111_111
    )

    fun fileWrite(fileName: String, content: ByteArray) {
        val filePointer = flipOpenWriteCreate(fileName)

        var fileIterator = 0x0uL
        content.chunks(1024).forEach { chunk ->
            raw.vfsWrite(filePointer, chunk, fileIterator).also {
                fileIterator = it.fileIterator
            }
        }

        raw.filpClose(filePointer)
    }

    fun fileWrite(fileName: String, stream: InputStream) {
        val filePointer = flipOpenWriteCreate(fileName)

        var fileIterator = 0x0uL
        do {
            val buffer = stream.readNBytes(1024)
            if (buffer.isNotEmpty()) {
                raw.vfsWrite(filePointer, buffer, fileIterator).also {
                    fileIterator = it.fileIterator
                }
            } else {
                break
            }
        } while (buffer.isNotEmpty())

        raw.filpClose(filePointer)
    }
}
