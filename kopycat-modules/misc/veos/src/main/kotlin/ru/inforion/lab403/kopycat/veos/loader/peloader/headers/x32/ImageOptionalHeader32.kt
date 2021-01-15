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
package ru.inforion.lab403.kopycat.veos.loader.peloader.headers.x32

import ru.inforion.lab403.common.extensions.asUInt
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.veos.loader.peloader.headers.ImageDataDirectory
import ru.inforion.lab403.kopycat.veos.loader.peloader.interfaces.ImageOptionalHeader
import java.nio.ByteBuffer

class ImageOptionalHeader32(input: ByteBuffer) : ImageOptionalHeader {

    override val magic = input.short.asUInt
    override val majorLinkerVersion = input.get().asUInt.toShort()
    override val minorLinkerVersion = input.get().asUInt.toShort()
    override val sizeOfCode = input.int.asULong
    override val sizeOfInitializedData = input.int.asULong
    override val sizeOfUninitializedData = input.int.asULong
    override val addressOfEntryPoint = input.int.asULong
    override val baseOfCode = input.int.asULong
    override val baseOfData = input.int.asULong
    override val imageBase = input.int.asULong
    override val sectionAlignment = input.int.asULong
    override val fileAlignment = input.int.asULong
    override val majorOperatingSystemVersion = input.short.asUInt
    override val minorOperatingSystemVersion = input.short.asUInt
    override val majorImageVersion = input.short.asUInt
    override val minorImageVersion = input.short.asUInt
    override val majorSubsystemVersion = input.short.asUInt
    override val minorSubsystemVersion = input.short.asUInt
    override val win32VersionValue = input.int.asULong
    override val sizeOfImage = input.int.asULong
    override val sizeOfHeaders = input.int.asULong
    override val checkSum = input.int.asULong
    override val subsystem = input.short.asUInt
    override val dllCharacteristics = input.short.asUInt
    override val SizeOfStackReserve = input.int.asULong
    override val SizeOfStackCommit = input.int.asULong
    override val SizeOfHeapReserve = input.int.asULong
    override val SizeOfHeapCommit = input.int.asULong
    override val LoaderFlags = input.int.asULong
    override val NumberOfRvaAndSizes = input.int.asULong
    override val DataDirectory = Array(minOf(16L, NumberOfRvaAndSizes).toInt()) { ImageDataDirectory(input) }


    init {
        require(magic == 0x10B) { "Optional header signature check failed" }

        require(boundImport.virtualAddress == 0L) { "Bound import isn't implemented yet" }
        require(delayImport.virtualAddress == 0L) { "Delay import isn't implemented yet" }
    }
}