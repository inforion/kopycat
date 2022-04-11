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
package ru.inforion.lab403.elfloader2.processors.x86_64

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.optional.Optional
import ru.inforion.lab403.elfloader2.*
import ru.inforion.lab403.elfloader2.enums.ELFCLASS
import ru.inforion.lab403.elfloader2.enums.ELFDATA
import ru.inforion.lab403.elfloader2.enums.ElfSymbolTableType
import ru.inforion.lab403.elfloader2.exceptions.EDecodeFault
import ru.inforion.lab403.elfloader2.processors.AElfDecoder
import ru.inforion.lab403.elfloader2.processors.x86_64.x86_64RelocationType.*
import ru.inforion.lab403.elfloader2.processors.x86_64.x86_64RelocationType.Companion.x86_64relocation

class ElfDecoderX86_64 (file: ElfFile) : AElfDecoder(file) {
    companion object {
        private val log = logger()
    }
    override val prefix: String = "x86_64"
//    override val pltHeadSize: Int = 16
//    override val pltEntrySize: Int = 16

    override fun checkHeader() {
        if (file.elfHeader.e_ident_class != ELFCLASS.ELFCLASS64)
            throw EDecodeFault("Only ELF64 supported for x86_64 architecture (should be x86 instead)")
        if (file.elfHeader.e_ident_data != ELFDATA.ELFDATA2LSB)
            throw EDecodeFault("x86_64 architecture supports only LSB")
    }

    override fun checkFlags() = log.severe { "X86-specific flags isn't implemented" }

    override fun checkSectionType(type: UInt): Unit = TODO("There is no any X86-specific sections")
    override fun checkSectionFlags(flags: ULong): Unit = TODO("There is no any X86-specific section flags")
    override fun checkSectionName(name: String, type: UInt, flags: ULong) =
        TODO("There is no any X86-specific section names")

    override fun checkSymbolBinding(bind: UInt) = TODO("There is no any X86-specific sections")
    override fun checkSymbolType(type: Int) = TODO("There is no any X86-specific sections")
    override fun checkSegmentType(type: UInt) = TODO("There is no any X86-specific segments")
    override fun checkSegmentFlags(flags: UInt) = TODO("There is no any X86-specific segment flags")
    override fun parseDynamic(hm: MutableMap<ULong, ULong>, tag: ULong, ptr: ULong) =
        TODO("There is no any X86-specific dynamic tags")

    override fun applyStaticRelocationRemoveMe(
        rel: ElfRel,
        vaddr: ULong,
        symbol: ULong,
        got: Optional<ULong>,
        data: ULong
    ): ULong {
        TODO("")
    }

    override fun prepareToRelocation(region: ElfRegion, rel: ElfRel, got: Optional<ULong>) = when (rel.type.x86_64relocation) {
        R_X86_64_GOTPCRELX,
        R_X86_64_REX_GOTPCRELX -> {
            /* Convert "mov foo@GOTPCREL(%rip), %reg" to
                "lea foo(%rip), %reg".  */
            val offset = region.toBufferOffset(rel.r_offset).requireInt

            if (offset >= 2 &&
                rel.symbol.st_type != ElfSymbolTableType.STT_GNU_IFUNC &&
                region.data[offset - 2] == 0x8B.byte) {
                // Use IDA variant of replacement
                val modRM = region.data[offset - 1]
                require(modRM[7..6] == 0b00 && modRM[2..0] == 5) { "Unknown case: [${modRM.hex2}] ${rel.r_offset.hex16} ${rel.symbol}" }
                val reg = modRM[5..3]
                region.data[offset - 2] = 0xC7.byte
                region.data[offset - 1] = (0b11_000_000 or reg).byte
            } else TODO()
            Unit
        }
        else -> Unit
    }

    override fun relocationSize(type: ULong) = type.x86_64relocation.size
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

        return when (rel.type.x86_64relocation) {
            R_X86_64_NONE -> null
            R_X86_64_64 -> S + A
            R_X86_64_PC32 -> S + A - P
            R_X86_64_GOT32 -> G + A
            R_X86_64_PLT32 -> L + A - P
            R_X86_64_COPY -> null
            R_X86_64_GLOB_DAT -> S
            R_X86_64_JUMP_SLOT -> S
            R_X86_64_RELATIVE -> TODO()
            R_X86_64_GOTPCREL -> G + GOT.get + A - P
            R_X86_64_32 -> S + A
            R_X86_64_32S -> S + A
            R_X86_64_16 -> S + A
            R_X86_64_PC16 -> S + A - P
            R_X86_64_8 -> S + A
            R_X86_64_PC8 -> S + A - P
            R_X86_64_DTPMOD64,
            R_X86_64_DTPOFF64,
            R_X86_64_TPOFF64,
            R_X86_64_TLSGD,
            R_X86_64_TLSLD,
            R_X86_64_DTPOFF32,
            R_X86_64_GOTTPOFF,
            R_X86_64_TPOFF32 -> TODO()
            R_X86_64_PC64 -> S + A - P
            R_X86_64_GOTOFF64 -> S + A - GOT.get
            R_X86_64_GOTPC32 -> GOT.get + A - P
            R_X86_64_SIZE32,
            R_X86_64_SIZE64 -> TODO()
            R_X86_64_GOTPC32_TLSDESC,
            R_X86_64_TLSDESC_CALL,
            R_X86_64_TLSDESC -> TODO()
            R_X86_64_IRELATIVE -> TODO()
            R_X86_64_GOTPCRELX,
                // Here we use instruction replacement without GOT,
                // so just return symbol address
            R_X86_64_REX_GOTPCRELX -> S //G + GOT.get + A - P
        }
    }


    override fun isLoadableSection(type: UInt, access: ElfAccess) = TODO("There is no any X86-specific sections")
    override fun isLoadableSegment(type: UInt) = TODO("There is no any X86-specific segments")
    override fun getProgramHeaderTypeNameById(type: UInt) = TODO("There is no any X86-specific segments")
    override fun getRelocationNameById(type: UInt) = type.ulong_z.x86_64relocation.name
}