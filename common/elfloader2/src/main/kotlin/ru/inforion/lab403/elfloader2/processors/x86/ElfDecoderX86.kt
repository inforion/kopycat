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
package ru.inforion.lab403.elfloader2.processors.x86

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.optional.Optional
import ru.inforion.lab403.elfloader2.*
import ru.inforion.lab403.elfloader2.exceptions.EDecodeFault
import ru.inforion.lab403.elfloader2.enums.ELFDATA.*
import ru.inforion.lab403.elfloader2.enums.ELFCLASS.*
import ru.inforion.lab403.elfloader2.enums.ElfSymbolTableType
import ru.inforion.lab403.elfloader2.processors.AElfDecoder
import ru.inforion.lab403.elfloader2.processors.x86.enum.X86RelocationType
import ru.inforion.lab403.elfloader2.processors.x86.enum.X86RelocationType.*
import ru.inforion.lab403.elfloader2.processors.x86.enum.X86RelocationType.Companion.x86relocation


class ElfDecoderX86(file: ElfFile) : AElfDecoder(file) {
    companion object {
        private val log = logger()
    }

    override val prefix: String = "386"
//    override val pltHeadSize: Int = 16
//    override val pltEntrySize: Int = 16

    override fun checkHeader() {
        if (file.elfHeader.e_ident_class != ELFCLASS32)
            throw EDecodeFault("Only ELF32 supported for x86 architecture (should be x86_64 instead)")
        if (file.elfHeader.e_ident_data != ELFDATA2LSB)
            throw EDecodeFault("x86 architecture supports only LSB")
    }
    override fun checkFlags() = log.severe {"X86-specific flags isn't implemented"}

    override fun checkSectionType(type: UInt): Unit = TODO("There is no any X86-specific sections")
    override fun checkSectionFlags(flags: ULong): Unit = TODO("There is no any X86-specific section flags")
    override fun checkSectionName(name: String, type: UInt, flags: ULong) = TODO("There is no any X86-specific section names")
    override fun checkSymbolBinding(bind: UInt) = TODO("There is no any X86-specific sections")
    override fun checkSymbolType(type: Int) = TODO("There is no any X86-specific sections")
    override fun checkSegmentType(type: UInt) = TODO("There is no any X86-specific segments")
    override fun checkSegmentFlags(flags: UInt) = TODO("There is no any X86-specific segment flags")
    override fun parseDynamic(hm: MutableMap<ULong, ULong>, tag: ULong, ptr: ULong) = TODO("There is no any X86-specific dynamic tags")

    override fun applyStaticRelocationRemoveMe(rel: ElfRel, vaddr: ULong, symbol: ULong, got: Optional<ULong>, data: ULong): ULong {
        val S = symbol
        val A = if (rel.withAddend) rel.r_addend else data
        val P = vaddr

        return when (rel.type) {
            //None
            R_386_NONE.id -> data

            //S + A
            R_386_32.id -> S + A

            //S + A - P
            R_386_PC32.id -> S - P + A

            /*
            //G + A - P
            R_386_GOT32.id -> {

            }
            */

            //L + A - P
            R_386_PLT32.id -> S - P + A //S -> L

            //Copy
            R_386_COPY.id -> 0uL

            //S
            R_386_GLOB_DAT.id -> S

            //S
            R_386_JMP_SLOT.id -> S

            /*
            //B + A
            R_386_RELATIVE.id -> {

            }
            */

            //S + A - GOT
            R_386_GOTOFF.id -> {
                check(got.isPresent) { "GOT relocation without GOT-section" }
                S - got.get + A
            }

            //GOT + A - P
            R_386_GOTPC.id -> {
                check(got.isPresent) { "GOT relocation without GOT-section" }
                got.get + A - P
            }

            else -> TODO("Not implemented type: ${rel.type} (${getRelocationNameById(rel.type.uint)})")
        }
    }

    override fun relocationSize(type: ULong) = type.x86relocation.size
    override fun prepareToRelocation(region: ElfRegion, rel: ElfRel, got: Optional<ULong>) = when (rel.type.x86relocation) {
        R_386_GOT32,
        R_386_GOT32X -> {
            // From /gold/i386.cc of binutils-gdb sources:
            // If the relocation symbol isn't IFUNC,
            // and is local, then we will convert
            // mov foo@GOT(%reg), %reg
            // to
            // lea foo@GOTOFF(%reg), %reg
            // in Relocate::relocate.
            val offset = region.toBufferOffset(rel.r_offset).requireInt
            if (offset >= 2 &&
                rel.symbol.st_type != ElfSymbolTableType.STT_GNU_IFUNC &&
                region.data[offset - 2] == 0x8B.byte) {
                // LD applies 0x8D (LEA) replacement, but IDA uses 0xC7 (MOV)
                val modRM = region.data[offset - 1]
                require(modRM[7..6] == 0b10) { "Unknown case: [${modRM.hex2}] ${rel.r_offset.hex16} ${rel.symbol}" }
                val reg = modRM[5..3]
                region.data[offset - 2] = 0xC7.byte
                region.data[offset - 1] = (0b11_000_000 or reg).byte
            } else TODO()
            Unit
        }
        else -> Unit
    }


    override fun relocationValue(
        relocations: List<ElfRel>,
        region: ElfRegion,
        rel: ElfRel,
        got: Optional<ULong>,
        baseAddress: ULong
    ): ULong? {
        val S = rel.symbol.st_value
        val A = rel.r_addend //if (rel.withAddend) rel.r_addend else data
        val P = rel.r_offset
        val G = S // At this stage, symbol should contain address inside .got section
        val L = S // TODO: pass PLT from arguments
        val GOT = got
        val B = baseAddress

        return when (rel.type.x86relocation) {
            R_386_NONE -> null
            R_386_32 -> S + A
            R_386_PC32 -> S + A - P
            R_386_PLT32 -> L + A - P
            R_386_COPY -> null
            R_386_GLOB_DAT,
            R_386_JMP_SLOT -> S
            R_386_RELATIVE -> B + A
            R_386_GOTOFF -> S + A - GOT.get
            R_386_GOTPC -> GOT.get + A - P
            R_386_GOT32,
            R_386_GOT32X -> G + A
        }
    }


    override fun isLoadableSection(type: UInt, access: ElfAccess) = TODO("There is no any X86-specific sections")
    override fun isLoadableSegment(type: UInt) = TODO("There is no any X86-specific segments")
    override fun getProgramHeaderTypeNameById(type: UInt) = TODO("There is no any X86-specific segments")
    override fun getRelocationNameById(type: UInt) = X86RelocationType.values().first{ it.id == type.ulong_z}.name // TODO: remove it
    override fun isGOTRelated(type: ULong) = type.x86relocation == R_386_GOTPC
}