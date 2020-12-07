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

import ru.inforion.lab403.kopycat.veos.loader.peloader.enums.ImageRelBased
import ru.inforion.lab403.kopycat.veos.loader.peloader.structs.PESection
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder


class PELoader(input: ByteBuffer) {

    companion object {
        fun fromPath(path: String): PELoader {
            val arr = File(path).readBytes()
            val buf = ByteBuffer.wrap(arr)
            buf.order(ByteOrder.LITTLE_ENDIAN)
            return PELoader(buf)
        }
    }

    private val peFile = PEFile(input)

    var baseAddress = peFile.imageNTHeader.optionalHeader.imageBase
    val entryPoint get() = peFile.imageNTHeader.optionalHeader.addressOfEntryPoint + baseAddress

    // TODO: array?
    val sections get() = peFile.sectionHeaders.map { it.toPESection(baseAddress) }

    // TODO: rebased
    val imports get() = peFile.importDescriptors

    val relocations get() = peFile.baseRelocations

    // TODO: array?
    val relocatedSections: List<PESection> get() {
        if (baseAddress != peFile.imageNTHeader.optionalHeader.imageBase) {

            relocations.forEach {
                it.block.forEach {
                    when (it.type) {
                        ImageRelBased.ABSOLUTE -> { /* Nothing to do*/
                        }
                        ImageRelBased.HIGHLOW -> TODO("To implement")
                        else -> TODO("Not implemented")
                    }
//                    println("${it.type} ${it.offset.toString(16)}")
                }
            }
        }

        return sections
    }
}

