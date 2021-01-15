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
package ru.inforion.lab403.elfloader

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.extensions.byte
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderIndex.*
import ru.inforion.lab403.elfloader.enums.ElfSymbolTableBind.*
import ru.inforion.lab403.elfloader.enums.ElfSymbolTableType.*
import ru.inforion.lab403.elfloader.exceptions.EBadStringTable
import ru.inforion.lab403.elfloader.exceptions.EBadSymbol
import java.nio.ByteBuffer


class ElfSymbol(
        val elfFile: ElfFile,
        input: ByteBuffer,
        offset: Int,
        stringTable: Map<Int, String>,
        val ind: Int
) {
    companion object {
        val log = logger(WARNING)
    }

    // Index in symbol's string table
    val nameOffset: Int
    val name: String

    // Value of associated symbol
    var value: Long

    // Associated size
    val size: Int

    // Type and binding attributes
    val infoBind: Int
    val infoType: Int

    // Always 0
    val other: Byte

    // Relevant section header table index
    val shndx: Short

    init {
        input.position(offset)

        nameOffset = input.int
        value = input.int.toULong()
        size = input.int
        val info = input.byte
        other = input.byte
        shndx = input.short

        // Overflow assertions...
        // Yeah, still love JVM
        assertMajorBit(nameOffset)
        assertMajorBit(size)
        assertMajorBit(other)

        // name = input.loadString(strtblOffset + nameOffset)
        name = stringTable[nameOffset]
                ?: elfFile.middleString(stringTable, nameOffset)
                ?: throw EBadStringTable("Not found offset in string table: 0x${nameOffset.hex8}")

        infoBind = info.toInt() ushr 4
        when (infoBind) {
            STB_LOCAL.id -> log.finer { "Local symbol" }
            STB_GLOBAL.id -> log.finer { "Global symbol" }
            STB_WEAK.id -> log.finer { "Weak symbol" }
            in STB_LOPROC.id..STB_HIPROC.id -> elfFile.decoder.checkSymbolBinding(infoBind)
            else -> throw EBadSymbol("Unknown symbol binding $infoBind")
        }

        infoType = info.toInt() and 0xf
        when (infoType) {
            STT_NOTYPE.id -> log.finer { "Symbol type isn't specified" }
            STT_OBJECT.id -> log.finer { "Symbol is a data object" }
            STT_FUNC.id -> log.finer { "Symbol is a function" }
            STT_SECTION.id -> log.finer { "Symbol is a section" }
            STT_FILE.id -> {
                if (infoBind != STB_LOCAL.id || shndx != SHN_ABS.id)
                    throw EBadSymbol("Not a normal symbol of type STT_FILE")
                log.finer { "Symbol is a source file" }
            }
            in STT_HIPROC.id..STT_LOPROC.id -> {
                TODO("There should be call of decoder")
                //decoder.checkSymbolType(infoType)
            }
            else -> throw EBadSymbol("Unknown symbol type")
        }

        if (other.toInt() != 0)
            log.warning { "Field 'other' isn't zero -> $other" }

        when (shndx) {
            SHN_ABS.id -> log.finer { "Symbol holds an absolute value" }
            SHN_COMMON.id -> log.finer { "Symbol gives alignment constraints" }
            SHN_UNDEF.id -> Unit
            else -> log.finer { "Symbol section index is $shndx" }
        }

        // AT LEAST: there is different meaning for relocatable files (p. 1-20)
        log.fine { "Symbol ind=$ind name='$name' value=0x${value.hex8} size=$size (0x${size.hex8})" }
    }

}