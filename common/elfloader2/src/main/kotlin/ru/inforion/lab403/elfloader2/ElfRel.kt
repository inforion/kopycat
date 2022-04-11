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
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.elfloader2.enums.ELFCLASS


class ElfRel constructor(
    val elfFile: ElfFile,
    val symbolTable: List<ElfSymbol>,
    val ind: Int,

    // AT LEAST: for relocatable file it is the byte offset from the beginning of the section to the storage unit
    // Virtual address of the storage unit affected by the relocation
    var r_offset: ULong,
    val r_info: ULong,
    var r_addend: ULong = 0u,

    val withAddend: Boolean = false,

    val symTabIndex: UInt = 0u,
    val sectionIndex: UInt = 0u) {

    companion object {
        @Transient val log = logger(INFO)

        fun IElfDataTypes.elfRel(
            elfFile: ElfFile,
            symbolTable: List<ElfSymbol>,
            ind: Int,
            off: ULong,
            size: ULong,
            withAddend: Boolean = false,
            symTabIndex: UInt = 0u,
            sectionIndex: UInt = 0u,
        ): ElfRel {
            val pos = off + ind.uint * size
            position = pos.requireInt
            return ElfRel(elfFile, symbolTable, ind, addr, wordpref, if (withAddend) wordpref else 0u, withAddend, symTabIndex, sectionIndex)
        }

        fun IElfDataTypes.elfRela(
            elfFile: ElfFile,
            symbolTable: List<ElfSymbol>,
            ind: Int,
            off: ULong,
            size: ULong,
            symTabIndex: UInt = 0u,
            sectionIndex: UInt = 0u
        ): ElfRel {
            val pos = off + ind.uint * size
            position = pos.requireInt
            return ElfRel(elfFile, symbolTable, ind, addr, wordpref, wordpref, true, symTabIndex, sectionIndex)
        }
        fun info32(sym: ULong, type: ULong) = (sym shl 8) or type
        fun info64(sym: ULong, type: ULong) = (sym shl 32) or type
    }

    fun copy(
        elfFile: ElfFile = this.elfFile,
        symbolTable: List<ElfSymbol> = this.symbolTable,
        ind: Int = this.ind,
        r_offset: ULong = this.r_offset,
        r_info: ULong = this.r_info,
        r_addend: ULong = this.r_addend,
        withAddend: Boolean = this.withAddend,
        symTabIndex: UInt = this.symTabIndex,
        sectionIndex: UInt = this.sectionIndex
    ) = ElfRel(elfFile, symbolTable, ind, r_offset, r_info, r_addend, withAddend, symTabIndex, sectionIndex)


    private fun elf32RSym() = r_info ushr 8
    private fun elf32RType() = r_info mask 8
    private fun elf32RTypeData(): ULong = throw NotImplementedError("Type data isn't exists in Elf32")
    private fun elf32RTypeID(): ULong = throw NotImplementedError("Type ID isn't exists in Elf32")

    private fun elf64RSym() = r_info ushr 32
    private fun elf64RType() = r_info mask 32
    private fun elf64RTypeData() = (r_info shl 32) ushr 40
    private fun elf64RTypeID() = (r_info shl 56) ushr 56

    inline fun switch(op32: () -> ULong, op64: () -> ULong) = when (elfFile.elfHeader.e_ident_class) {
        ELFCLASS.ELFCLASS32 -> op32()
        ELFCLASS.ELFCLASS64 -> op64()
        else -> throw NotImplementedError()
    }


    fun info(sym: ULong, type: ULong) = when (elfFile.elfHeader.e_ident_class) {
        ELFCLASS.ELFCLASS32 -> info32(sym, type)
        ELFCLASS.ELFCLASS64 -> info64(sym, type)
        else -> throw NotImplementedError()
    }

    // symbol table index and the type of relocation
    val sym = switch(::elf32RSym, ::elf64RSym)
    val type = switch(::elf32RType, ::elf64RType)

    // Only in Elf64_Rel / Elf64_Rela
    val typeData get() = switch(::elf32RTypeData, ::elf64RTypeData)
    val typeID get() = switch(::elf32RTypeID, ::elf64RTypeID)

    val typeName = elfFile.decoder.getRelocationNameById(type.uint) // TODO: fix it

    val symbol: ElfSymbol get() = if (sym.int < 0)
        symbolTable.first { it.ind == sym.int }
    else
        symbolTable[sym.requireInt].also { require(it.ind == sym.int) }

    // section field should not be used during linkage
    private val section get() = elfFile.sections[sectionIndex].also { require(it.index == sectionIndex.int) }

    override fun toString(): String {
        val sectionName = "${section.name}+${r_offset.hex8}".field(25)
        val addend = if (withAddend) "+${r_addend.hex16}" else ""
        return "[${ind.str.padEnd(3)}] $sectionName ${symbol.complexName.field(30)} ${typeName.field(16)} " +
                "=${symbol.st_value.hex16}$addend"
    }
    init {
        log.fine { "Relocation index=$ind address=0x${r_offset.hex8} sym=$sym type=$typeName addend=$r_addend" }
    }
}