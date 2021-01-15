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
package ru.inforion.lab403.kopycat.veos.loader.peloader

import ru.inforion.lab403.kopycat.veos.loader.peloader.headers.*
import java.nio.ByteBuffer

class PEFile(private val input: ByteBuffer) {

    val imageDosHeader = ImageDosHeader(input)

    val imageNTHeader: ImageNTHeader

    val sectionHeaders: Array<ImageSectionHeader>

    val importDescriptors: Array<ImageImportDescriptor>

    val baseRelocations: Array<ImageBaseRelocation>

    fun rva2foa(rva: Long) = sectionHeaders.first { it.containsRva(rva) }.rva2foa(rva)

    init {
        input.position(imageDosHeader.lfanew)

        imageNTHeader = ImageNTHeader(input)

        sectionHeaders = Array(imageNTHeader.fileHeader.numberOfSections) {
            ImageSectionHeader(input)
        }

        require(imageNTHeader.optionalHeader.import.virtualAddress != 0L) { "Imports may be missing" }
        input.position(rva2foa(imageNTHeader.optionalHeader.import.virtualAddress).toInt())
        val importDescriptorList = mutableListOf<ImageImportDescriptor>()
        while (true) {
            val descriptor = ImageImportDescriptor(this, input)
            if (descriptor.originalFirstThunk == 0L)
                break
            importDescriptorList.add(descriptor)
        }
        importDescriptors = importDescriptorList.toTypedArray()

        require(imageNTHeader.optionalHeader.baseReloc.virtualAddress != 0L) { "Relocs may be missing" }
        input.position(rva2foa(imageNTHeader.optionalHeader.baseReloc.virtualAddress).toInt())
        val baseRelocationsList = mutableListOf<ImageBaseRelocation>()
        while (true) {
            val relocation = ImageBaseRelocation(input)
            if (relocation.entriesCount == 0L)
                break
            baseRelocationsList.add(relocation)
        }
        baseRelocations = baseRelocationsList.toTypedArray()
    }



}