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
package ru.inforion.lab403.elfloader.headers

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.elfloader.exceptions.EBadStringTable
import ru.inforion.lab403.elfloader.ElfFile
import ru.inforion.lab403.elfloader.assertMajorBit
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderType
import java.nio.ByteBuffer


class ElfSectionHeader private constructor(
        val elfFile: ElfFile,
        val ind: Int,
        val nameOffset: Int,
        val type: Int,
        val flags: UInt,
        val addr: ULong,
        val offset: Int,
        val size: Int,
        val link: Int,
        val info: Int,
        val addralign: Int,
        val entsize: Int
) {
    companion object {
        private val log = logger(WARNING)

        val unimplementedFlagsMask = 0x0FFFFD88u - 0x80u // 0x80 -> ARM_EXIDX

        fun ByteBuffer.elfSectionHeader(elfFile: ElfFile, ind: Int, shoff: Int, shentsize: Short): ElfSectionHeader {
            position(shoff + ind * shentsize)
            return ElfSectionHeader(elfFile, ind, int, int, uint, int.ulong_z, int, int, int, int, int, int)
        }
    }

    val name by lazy {
        elfFile.sectionStringTable[nameOffset]
                ?: elfFile.middleString(elfFile.sectionStringTable, nameOffset)
                ?: throw EBadStringTable("Not found offset in string table: 0x${nameOffset.hex8}")
    }

    init {
        //TODO: Is it really needed?
        assertMajorBit(nameOffset)
        assertMajorBit(type)
        assertMajorBit(flags)
        assertMajorBit(offset)
        assertMajorBit(size)
        assertMajorBit(link)
        assertMajorBit(info)
        assertMajorBit(addralign)
        assertMajorBit(entsize)

        if (flags and unimplementedFlagsMask != 0u) {
            log.severe { "Other section flags isn't implemented: " +
                    "${flags.binary} [${(flags and unimplementedFlagsMask).binary}]" }
        }

        log.finer {
            val typeString = ElfSectionHeaderType.nameById(type)
            "Section flags=${flags.binary} " +
                    "nameoff=0x${nameOffset.hex8} " +
                    "type=$typeString" +
                    "address=0x${addr.hex8} " +
                    "size=0x${size.hex8} " +
                    "link=0x${link.hex8} " +
                    "info=0x${info.hex8} " +
                    "align=$addralign " +
                    "entsize=$entsize"
        }
    }
}