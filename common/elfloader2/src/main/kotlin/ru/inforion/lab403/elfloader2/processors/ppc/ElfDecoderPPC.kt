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
package ru.inforion.lab403.elfloader2.processors.ppc

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.optional.Optional
import ru.inforion.lab403.elfloader2.ElfFile
import ru.inforion.lab403.elfloader2.ElfRegion
import ru.inforion.lab403.elfloader2.ElfRel
import ru.inforion.lab403.elfloader2.enums.ELFCLASS
import ru.inforion.lab403.elfloader2.exceptions.EDecodeFault
import ru.inforion.lab403.elfloader2.processors.AElfDecoder
import ru.inforion.lab403.elfloader2.processors.ppc.PPCReloactionType.*
import ru.inforion.lab403.elfloader2.processors.ppc.PPCReloactionType.Companion.Field.*
import ru.inforion.lab403.elfloader2.processors.ppc.PPCReloactionType.Companion.ppcRelocation
import ru.inforion.lab403.elfloader2.requireInt


class ElfDecoderPPC(file: ElfFile) : AElfDecoder(file) {

    companion object {
        private val log = logger()
    }

    override val prefix = "PPC"
//    override val pltHeadSize = 0
//    override val pltEntrySize = 0

    var symbolTableSize : Long? = null

    override fun checkHeader() {
        if (file.elfHeader.e_ident_class != ELFCLASS.ELFCLASS32)
            throw EDecodeFault("Only ELF32 supported for PPC architecture (should be PPC64 instead)")
    }

    override fun checkFlags() = Unit

    override fun checkSectionType(type: UInt) = Unit

    override fun checkSectionFlags(flags: ULong) = TODO("Not implemented")

    override fun checkSectionName(name: String, type: UInt, flags: ULong) = TODO("Not implemented")

    override fun checkSymbolBinding(bind: UInt) = TODO("Not implemented")

    override fun checkSymbolType(type: Int) = Unit

    override fun checkSegmentType(type: UInt) = TODO("Not implemented")

    override fun checkSegmentFlags(flags: UInt): Unit = TODO("Not implemented")

    override fun parseDynamic(hm: MutableMap<ULong, ULong>, tag: ULong, ptr: ULong): Unit = TODO("Not implemented")

    override fun applyStaticRelocationRemoveMe(rel: ElfRel, vaddr: ULong, symbol: ULong, got: Optional<ULong>, data: ULong) =
        TODO("Not implemented")


    override fun readData(region: ElfRegion, rel: ElfRel): ULong {
        val offset = region.toBufferOffset(rel.r_offset).requireInt
        val buffer = region.buffer
        return with (buffer) {
            position(offset)
            when (rel.type.ppcRelocation.field) {
                none -> 0u
                word32 -> int.ulong_z
                word30 -> int.ulong_z ushr 2
                low24, low24v -> (int.ulong_z ushr 1) mask 24
                low14, low14v -> (int.ulong_z ushr 1) mask 14
                half16, half16v-> short.ulong_z
            }
        }
    }

    private inline fun verify(value: ULong, size: Int) = require(value mask size == value) { "Verification failed" }

    override fun writeData(region: ElfRegion, rel: ElfRel, value: ULong) {
        val offset = region.toBufferOffset(rel.r_offset).requireInt
        val buffer = region.buffer
        with (buffer) {
            position(offset)
            when (val type = rel.type.ppcRelocation.field) {
                none -> {}
                word32 -> putInt(value.int)
                word30 -> {
                    int.insert(value.int, 31..2).also {
                        position(offset)
                        putInt(it)
                    }
                }
                low24, low24v -> {
                    if (type.verify)
                        verify(value, 24)
                    int.insert(value.int, 25..2).also {
                        position(offset)
                        putInt(it)
                    }
                }
                low14, low14v -> {
                    if (type.verify)
                        verify(value, 14)
                    int.insert(value.int, 15..2).also {
                        position(offset)
                        putInt(it)
                    }
                }
                half16, half16v-> {
                    if (type.verify)
                        verify(value, 16)
                    putShort(value.short)
                }
            }
        }
    }

    // Exactly as in documentation
    private inline fun lo(x: ULong) = x and 0xFFFFu
    private inline fun hi(x: ULong) = (x ushr 16) and 0xFFFFu
    private inline fun ha(x: ULong) = ((x ushr 16) + if ((x and 0x8000u).truth) 1u else 0u ) and 0xFFFFu

    override fun relocationValue(
        relocations: List<ElfRel>,
        region: ElfRegion,
        rel: ElfRel,
        got: Optional<ULong>,
        baseAddress: ULong
    ): ULong? {
        val S = rel.symbol.st_value
        val A = rel.r_addend // if (rel.withAddend) rel.r_addend else data
        val P = rel.r_offset
        val G = S // At this stage, symbol should contain address inside .got section
        val L = S // TODO: pass PLT from arguments

        return when (rel.type.ppcRelocation) {
            R_PPC_NONE -> null
            R_PPC_ADDR32 -> S + A
            R_PPC_ADDR24 -> (S + A) ushr 2
            R_PPC_ADDR16 -> S + A
            R_PPC_ADDR16_LO -> lo(S + A)
            R_PPC_ADDR16_HI -> hi(S + A)
            R_PPC_ADDR16_HA -> ha(S + A)
            R_PPC_ADDR14 -> (S + A) ushr 2
            R_PPC_ADDR14_BRTAKEN -> (S + A) ushr 2
            R_PPC_ADDR14_BRNTAKEN -> (S + A) ushr 2
            R_PPC_REL24 -> (S + A - P) ushr 2
            R_PPC_REL14 -> (S + A - P) ushr 2
            R_PPC_REL14_BRTAKEN -> (S + A - P) ushr 2
            R_PPC_REL14_BRNTAKEN -> (S + A - P) ushr 2
            R_PPC_GOT16 -> G + A
            R_PPC_GOT16_LO -> lo(G + A)
            R_PPC_GOT16_HI -> hi(G + A)
            R_PPC_GOT16_HA -> ha(G + A)
            R_PPC_PLTREL24 -> (L + A - P) ushr 2
            R_PPC_COPY -> null
            R_PPC_GLOB_DAT -> S + A
            R_PPC_JMP_SLOT -> TODO()
            R_PPC_RELATIVE -> TODO("B + A")
            R_PPC_LOCAL24PC -> TODO()
            R_PPC_UADDR32 -> S + A
            R_PPC_UADDR16 -> S + A
            R_PPC_REL32 -> S + A - P
            R_PPC_PLT32 -> L + A
            R_PPC_PLTREL32 -> L + A - P
            R_PPC_PLT16_LO -> lo(L + A)
            R_PPL_PLT16_HI -> hi(L + A)
            R_PPC_PLT16_HA -> ha(L + A)
            R_PPC_SDAREL16 -> TODO("S + A - _SDA_BASE_")
            R_PPC_SECTOFF -> TODO("R + A")
            R_PPC_SECTOFF_LO -> TODO("lo(R + A)")
            R_PPC_SECTOFF_HI -> TODO("hi(R + A)")
            R_PPC_SECTOFF_HA -> TODO("ha(R + A)")
            R_PPC_ADDR30 -> (S + A - P) ushr 2
        }
    }

    override fun getProgramHeaderTypeNameById(type: UInt): String = TODO("NI")

    override fun getRelocationNameById(type: UInt): String = type.ulong_z.ppcRelocation.name

}