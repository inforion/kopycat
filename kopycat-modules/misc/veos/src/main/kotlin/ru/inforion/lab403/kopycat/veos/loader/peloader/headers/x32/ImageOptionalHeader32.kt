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
package ru.inforion.lab403.kopycat.veos.loader.peloader.headers.x32

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.int_z
import ru.inforion.lab403.common.extensions.short_z
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.veos.loader.peloader.headers.ImageDataDirectory
import ru.inforion.lab403.kopycat.veos.loader.peloader.interfaces.ImageOptionalHeader
import java.nio.ByteBuffer

class ImageOptionalHeader32(input: ByteBuffer) : ImageOptionalHeader {

    override val magic = input.short.int_z
    override val majorLinkerVersion = input.get().short_z
    override val minorLinkerVersion = input.get().short_z
    override val sizeOfCode = input.int.ulong_z
    override val sizeOfInitializedData = input.int.ulong_z
    override val sizeOfUninitializedData = input.int.ulong_z
    override val addressOfEntryPoint = input.int.ulong_z
    override val baseOfCode = input.int.ulong_z
    override val baseOfData = input.int.ulong_z
    override val imageBase = input.int.ulong_z
    override val sectionAlignment = input.int.ulong_z
    override val fileAlignment = input.int.ulong_z
    override val majorOperatingSystemVersion = input.short.int_z
    override val minorOperatingSystemVersion = input.short.int_z
    override val majorImageVersion = input.short.int_z
    override val minorImageVersion = input.short.int_z
    override val majorSubsystemVersion = input.short.int_z
    override val minorSubsystemVersion = input.short.int_z
    override val win32VersionValue = input.int.ulong_z
    override val sizeOfImage = input.int.ulong_z
    override val sizeOfHeaders = input.int.ulong_z
    override val checkSum = input.int.ulong_z
    override val subsystem = input.short.int_z
    override val dllCharacteristics = input.short.int_z
    override val SizeOfStackReserve = input.int.ulong_z
    override val SizeOfStackCommit = input.int.ulong_z
    override val SizeOfHeapReserve = input.int.ulong_z
    override val SizeOfHeapCommit = input.int.ulong_z
    override val LoaderFlags = input.int.ulong_z
    override val NumberOfRvaAndSizes = input.int.ulong_z
    override val DataDirectory = Array(minOf(16uL, NumberOfRvaAndSizes).int) { ImageDataDirectory(input) }


    init {
        require(magic == 0x10B) { "Optional header signature check failed" }

        require(boundImport.virtualAddress == 0uL) { "Bound import isn't implemented yet" }
        require(delayImport.virtualAddress == 0uL) { "Delay import isn't implemented yet" }
    }
}