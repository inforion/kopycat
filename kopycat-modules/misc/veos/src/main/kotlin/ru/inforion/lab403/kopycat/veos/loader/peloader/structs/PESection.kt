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
package ru.inforion.lab403.kopycat.veos.loader.peloader.structs

import ru.inforion.lab403.kopycat.veos.loader.peloader.headers.ImageSectionHeader
import java.nio.ByteBuffer




class PESection(private val input: ByteBuffer, val header: ImageSectionHeader, val base: Long) {

    val name get() = header.name
    val size get() = header.virtualSize
    val start get() = base + header.virtualAddress
    val end get() = base + header.virtualAddress + header.virtualSize - 1
    val rawSize get() = header.sizeOfRawData

    val defaultAlignedSize get() = alignedSize(0x1000)

    fun alignedSize(alignment: Int): Long {
        val floored = (size / alignment) * alignment
        return if (floored == size)
            size
        else
            floored + alignment
    }

    val data: ByteArray get() {
        val result = ByteArray(size.toInt())
        input.position(header.pointerToRawData.toInt())
        val readSize = minOf(rawSize, size).toInt()
        input.get(result, 0, readSize)
        return result
    }
}