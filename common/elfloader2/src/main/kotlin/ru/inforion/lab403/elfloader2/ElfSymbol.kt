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
package ru.inforion.lab403.elfloader2

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.elfloader2.enums.SectionHeaderNumber.*
import ru.inforion.lab403.elfloader2.enums.ElfSymbolTableBind.*
import ru.inforion.lab403.elfloader2.enums.ElfSymbolTableBind.Companion.elfSymbolTableBind
import ru.inforion.lab403.elfloader2.enums.ElfSymbolTableType.*
import ru.inforion.lab403.elfloader2.enums.ElfSymbolTableType.Companion.elfSymbolTableType
import ru.inforion.lab403.elfloader2.enums.SectionHeaderNumber.Companion.sectionHeaderNumber
import ru.inforion.lab403.elfloader2.exceptions.EBadSymbol


class ElfSymbol(
        val elfFile: ElfFile,
        val ind: Int,
        val stringTable: ElfStringTable,
        val st_name: UInt,
        var st_value: ULong,
        val st_size: ULong,
        val st_info: UByte,
        val st_other: UByte,
        val st_shndx_value: UShort
) {
    companion object {
        val log = logger(WARNING)

        fun IElfDataTypes.elfSymbol(
            elfFile: ElfFile,
            stringTable: ElfStringTable,
            ind: Int,
            offset: ULong,
            entrySize: ULong
        ): ElfSymbol {
            val pos = offset + ind.uint * entrySize
            position = pos.requireInt
            return when (this) {
                is ElfDataTypes32 -> ElfSymbol(
                    elfFile,
                    ind,
                    stringTable,
                    st_name = word,
                    st_value = addr,
                    st_size = wordpref,
                    st_info = byte,
                    st_other = byte,
                    st_shndx_value = half
                )
                is ElfDataTypes64 -> ElfSymbol(
                    elfFile,
                    ind,
                    stringTable,
                    st_name = word,
                    st_info = byte,
                    st_other = byte,
                    st_shndx_value = half,
                    st_value = addr,
                    st_size = wordpref
                )
                else -> throw NotImplementedError("Unknown ElfDataType: $this")
            }
        }
    }

    fun copy(elfFile: ElfFile = this.elfFile,
             ind: Int = this.ind,
             stringTable: ElfStringTable = this.stringTable,
             st_name: UInt = this.st_name,
             st_value: ULong = this.st_value,
             st_size: ULong = this.st_size,
             st_info: UByte = this.st_info,
             st_other: UByte = this.st_other,
             st_shndx_value: UShort = this.st_shndx_value) =
        ElfSymbol(elfFile, ind, stringTable, st_name, st_value, st_size, st_info, st_other, st_shndx_value)

    val name = stringTable[st_name]
    val complexName get() = when (st_type) {
        STT_SECTION -> {
            require(st_shndx == SHN_NORESERVE) { "Unknown case" }
            elfFile.sections[st_shndx_value.int_z].name
        }
        else -> name
    }

    val st_bind_value = st_info ushr 4
    val st_bind = st_bind_value.elfSymbolTableBind { throw EBadSymbol("Unknown symbol binding: $this") }
    val isLocal get() = st_bind == STB_LOCAL
    val isGlobal get() = st_bind == STB_GLOBAL
    val isWeak get() = st_bind == STB_WEAK
//    val isProc get() = infoBind == STB_PROC // collision

    val st_type_value = (st_info and 0xfu).uint_z
    val st_type = st_type_value.elfSymbolTableType { throw EBadSymbol("Unknown symbol type: $this") }
    val isNoType get() = st_type == STT_NOTYPE
    val isObject get() = st_type == STT_OBJECT
    val isFunc get() = st_type == STT_FUNC
    val isSection get() = st_type == STT_SECTION
    val isFile get() = st_type == STT_FILE
//    val isProc get() = infoType == STT_PROC // collision

    val st_shndx get() = st_shndx_value.sectionHeaderNumber
    val isUndef get() = st_shndx == SHN_UNDEF
    val isAbs get() = st_shndx == SHN_ABS
    val isBefore get() = st_shndx == SHN_BEFORE
    val isAfter get() = st_shndx == SHN_AFTER
    val isCommon get() = st_shndx == SHN_COMMON
    val isReserve get() = st_shndx_value in SHN_RESERVE.range
//    val isProc get() = st_shndx_value in SHN_PROC.range // collision

    override fun toString() = "[${ind.str.padEnd(3)}] ${name.field(30)} =${st_value.hex16} :${st_size.toString().padEnd(6)} " +
            "${st_bind.shortName.padEnd(10)} ${st_type.shortName.padEnd(11)} " +
            if(st_shndx == SHN_NORESERVE) "$st_shndx_value" else st_shndx.shortName

    init {

        when (st_bind) {
            STB_LOCAL -> log.finer { "Local symbol" }
            STB_GLOBAL -> log.finer { "Global symbol" }
            STB_WEAK -> log.finer { "Weak symbol" }
            STB_PROC -> elfFile.decoder.checkSymbolBinding(st_bind_value)
        }

        when (st_type) {
            STT_NOTYPE -> log.finer { "Symbol type isn't specified" }
            STT_OBJECT -> log.finer { "Symbol is a data object" }
            STT_FUNC -> log.finer { "Symbol is a function" }
            STT_SECTION -> log.finer { "Symbol is a section" }
            STT_FILE -> {
                if (st_bind != STB_LOCAL || st_shndx != SHN_ABS)
                    throw EBadSymbol("Not a normal symbol of type STT_FILE")
                log.finer { "Symbol is a source file" }
            }
            STT_PROC -> {
                TODO("There should be call of decoder")
                //decoder.checkSymbolType(infoType)
            }
            else -> {
                TODO("Unknown st_type: $st_type")
            }
        }

        if (st_other != 0u.ubyte)
            log.warning { "Field 'other' isn't zero -> $st_other" }

        when (st_shndx) {
            SHN_ABS -> log.finer { "Symbol holds an absolute value" }
            SHN_COMMON -> log.finer { "Symbol gives alignment constraints" }
            SHN_UNDEF -> Unit
            else -> log.finer { "Symbol section index is ${st_shndx_value.hex4}" }
        }

        // AT LEAST: there is different meaning for relocatable files (p. 1-20)
        log.fine { "Symbol ind=$ind name='$name' value=0x${st_value.hex8} size=$st_size (0x${st_size.hex8})" }
    }

}