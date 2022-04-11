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
package ru.inforion.lab403.elfloader2.headers

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.elfloader2.*
import ru.inforion.lab403.elfloader2.enums.ElfSectionHeaderFlag.*
import ru.inforion.lab403.elfloader2.enums.ElfSectionHeaderType


class ElfSectionHeader private constructor(
    val elfFile: ElfFile,     // We can't pass sectionStringTable directly due to recursive access
    val index: Int,
    val sh_name: UInt,
    val sh_type_value: UInt,
    val sh_flags: ULong, // Elf32_word, Elf64_Xword
    val sh_addr: ULong,
    val sh_offset: ULong,
    val sh_size: ULong, // Elf32_word, Elf64_Xword
    val sh_link: UInt,
    val sh_info: UInt,
    val sh_addralign: ULong,
    val sh_entsize: ULong
) {
    companion object {
        private val log = logger(WARNING)

        val unimplementedFlagsMask = 0x0FFFFD88uL - 0x80uL // 0x80 -> ARM_EXIDX

        fun IElfDataTypes.elfSectionHeader(elfFile: ElfFile, index: Int, offset: ULong, entrySize: UShort): ElfSectionHeader {
            val pos = offset + index.uint * entrySize
            position = pos.requireInt
            return ElfSectionHeader(elfFile, index,
                sh_name = word, sh_type_value = word, sh_flags = wordpref, sh_addr = addr,
                sh_offset = off, sh_size = wordpref, sh_link = word, sh_info = word,
                sh_addralign = wordpref, sh_entsize = wordpref
            )
        }
    }
    val sh_type = ElfSectionHeaderType.castOrThrow(sh_type_value)

    val name by lazy { elfFile.sectionStringTable[sh_name] }

    val isAlloc get() = sh_flags and SHF_ALLOC.mask != 0uL
    val isWrite get() = sh_flags and SHF_WRITE.mask != 0uL
    val isExecInstr get() = sh_flags and SHF_EXECINSTR.mask != 0uL

    init {
        if (sh_flags and unimplementedFlagsMask != 0uL) {
            log.severe { "Other section flags aren't implemented: " +
                    "${sh_flags.binary} [${(sh_flags and unimplementedFlagsMask).binary}]" }
        }

        log.finer {
            val typeString = sh_type.shortName
            "Section flags=${sh_flags.binary} " +
                    "nameoff=0x${sh_name.hex8} " +
                    "type=$typeString" +
                    "address=0x${sh_addr.hex8} " +
                    "size=0x${sh_size.hex8} " +
                    "link=0x${sh_link.hex8} " +
                    "info=0x${sh_info.hex8} " +
                    "align=$sh_addralign " +
                    "entsize=$sh_entsize"
        }
    }


}