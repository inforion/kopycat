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
package ru.inforion.lab403.elfloader.headers

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.elfloader.exceptions.EBadStringTable
import ru.inforion.lab403.elfloader.ElfFile
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderType
import java.nio.ByteBuffer


class ElfSectionHeader private constructor(
        val elfFile: ElfFile,
        val ind: Int,
        val nameOffset: Int,
        val type: Int,
        val flags: Int,
        val addr: Long,
        val offset: Int,
        val size: Int,
        val link: Int,
        val info: Int,
        val addralign: Int,
        val entsize: Int
) {
    companion object {
        private val log = logger(WARNING)

        const val unimplementedFlagsMask = 0x0FFFFD88 - 0x80 // 0x80 -> ARM_EXIDX

        fun ByteBuffer.elfSectionHeader(elfFile: ElfFile, ind: Int, shoff: Int, shentsize: Short): ElfSectionHeader {
            position(shoff + ind * shentsize)
            return ElfSectionHeader(elfFile, ind, int, int, int, int.asULong, int, int, int, int, int, int)
        }
    }

    val name by lazy {
        elfFile.sectionStringTable[nameOffset]
                ?: elfFile.middleString(elfFile.sectionStringTable, nameOffset)
                ?: throw EBadStringTable("Not found offset in string table: 0x${nameOffset.hex8}")
    }

    init {
        require(nameOffset >= 0)
        require(type >= 0)
        require(flags >= 0)
        require(offset >= 0)
        require(size >= 0)
        require(link >= 0)
        require(info >= 0)
        require(addralign >= 0)
        require(entsize >= 0)

//        when(type) {
//            SHT_NULL.id -> log.info("Inactive section")
//            SHT_PROGBITS.id -> log.info("Program determined-only section")
//            SHT_SYMTAB.id -> log.info("Symbol table")
//            SHT_STRTAB.id -> log.info("String table")
//            SHT_RELA.id -> log.info("Relocation section")
//            SHT_DYNAMIC.id -> log.info("Dynamic section")
//            SHT_HASH.id -> log.info("Symbol hash table")
//            SHT_NOTE.id -> log.info("Dynamic linking information")
//            SHT_NOBITS.id -> log.info("Note section")
//            SHT_REL.id -> log.info("Section holds no space in file")
//            SHT_SHLIB.id -> throw Exception("Unspecified section type \"SHT_SHLIB\"")
//            SHT_DYNSYM.id -> log.info("Dynamic linking symbol table")
//            SHT_GNU_verdef.id -> log.info("Symbol definitions")
//            SHT_GNU_verneed.id -> log.info("Symbol requirements")
//            SHT_GNU_versym.id -> log.info("Symbol version table")
//            in SHT_LOPROC.id..SHT_HIPROC.id -> Unit //ElfFile routine
//            in SHT_LOUSER.id..SHT_HIUSER.id -> TODO("Check, what this kind of sections do")
//            else -> throw Exception("Unknown section type ${type.hex8}")
//        }

        if (flags and unimplementedFlagsMask != 0) {
            val fl = Integer.toBinaryString(flags)
            val unfl = Integer.toBinaryString(flags and unimplementedFlagsMask)
            log.severe { "Other section flags isn't implemented: $fl [${unfl}]" }
        }

        log.finer {
            val typeString = ElfSectionHeaderType.nameById(type)
            val flagsString = Integer.toBinaryString(flags)
            "Section flags=$flagsString " +
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