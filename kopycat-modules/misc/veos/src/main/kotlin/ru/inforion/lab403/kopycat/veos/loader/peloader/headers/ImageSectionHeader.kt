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
package ru.inforion.lab403.kopycat.veos.loader.peloader.headers

import ru.inforion.lab403.common.extensions.asUInt
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.veos.loader.peloader.string
import ru.inforion.lab403.kopycat.veos.loader.peloader.structs.PECharacteristic
import ru.inforion.lab403.kopycat.veos.loader.peloader.structs.PESection
import java.nio.ByteBuffer

class ImageSectionHeader(private val input: ByteBuffer) {
    private val position = input.position()
    val name = input.string.also { input.position(position + 8) }
    val physicalAddress = input.int.asULong
    val virtualSize = physicalAddress
    val virtualAddress = input.int.asULong
    val sizeOfRawData = input.int.asULong
    val pointerToRawData = input.int.asULong
    val pointerToRelocations = input.int.asULong
    val pointerToLinenumbers = input.int.asULong
    val numberOfRelocations = input.short.asUInt
    val numberOfLinenumbers = input.short.asUInt
    val characteristics = PECharacteristic(input.int.asULong)

    fun containsRva(rva: Long) = (rva >= virtualAddress) && (rva < virtualAddress + sizeOfRawData)
    fun rva2foa(rva: Long) = pointerToRawData + (rva - virtualAddress)

    fun toPESection(base: Long) = PESection(input, this, base)

    init {
        require(pointerToRelocations == 0L) { "Not implemented" }
        require(numberOfRelocations == 0) { "Not implemented" }
    }
}