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
package ru.inforion.lab403.elfloader2.processors.mips

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.optional.Optional
import ru.inforion.lab403.common.optional.emptyOpt
import ru.inforion.lab403.common.optional.opt
import ru.inforion.lab403.elfloader2.*
import ru.inforion.lab403.elfloader2.exceptions.EDecodeFault
import ru.inforion.lab403.elfloader2.enums.ElfSectionHeaderType.*
import ru.inforion.lab403.elfloader2.processors.AElfDecoder
import ru.inforion.lab403.elfloader2.processors.mips.enums.MipsDynamicSectionTag.*
import ru.inforion.lab403.elfloader2.processors.mips.enums.MipsSectionType.*
import ru.inforion.lab403.elfloader2.processors.mips.enums.MipsRelocationType
import ru.inforion.lab403.elfloader2.processors.mips.enums.MipsRelocationType.*
import ru.inforion.lab403.elfloader2.processors.mips.enums.MipsRelocationType.Companion.Field.*
import ru.inforion.lab403.elfloader2.processors.mips.enums.MipsRelocationType.Companion.mipsRelocation
import ru.inforion.lab403.elfloader2.processors.mips.enums.MipsSegmentType
import ru.inforion.lab403.elfloader2.processors.mips.enums.MipsSegmentType.*


class ElfDecoderMips(file: ElfFile) : AElfDecoder(file) {

    companion object {
        private val log = logger()

        const val GP_ADDEND = 0x7FF0
    }

    override val prefix = "MIPS"
//    override val pltHeadSize = 20
//    override val pltEntrySize = 12

    //ABI version
    var abi = 0
        private set

    //GCC-generated info
    var gcc = 0
        private set

    var symbolTableSize : Optional<ULong> = emptyOpt()


    //TODO: Change to header as parameter
    //TODO: Header incapsulation
    override fun checkHeader() { }

    override fun checkFlags() {
        abi = 0
        gcc = 0
    }

    //TODO: return Boolean as result of check
    override fun checkSectionType(type: UInt) = Unit

    override fun checkSectionFlags(flags: ULong) {
        if (flags and 0x10000000u != 0uL) // TODO: SHF_MIPS_GPREL as part of enum
            log.warning { "Warning! SHF_MIPS_GPREL flag is not implemented" }
    }

    override fun checkSectionName(name: String, type: UInt, flags: ULong) = when (name) {
        ".MIPS.abiflags" -> type != SHT_MIPS_ABIFLAGS.id
        ".MIPS.stubs" -> type != SHT_PROGBITS.low
        else -> {
            log.warning { "Non standard section name: $name" }
            false
        }
    }

    //FIXME: SYMBOL TABLE SHOULD CALL THEM
    override fun checkSymbolBinding(bind: UInt) = Unit

    override fun checkSymbolType(type: Int) = Unit

    //TODO: change name!!!
    override fun checkSegmentType(type: UInt) {
        when (type) {
            PT_MIPS_REGINFO.id   -> {}
            PT_MIPS_RTPROC.id    -> {}
            PT_MIPS_OPTIONS.id	 -> {}
            PT_MIPS_ABIFLAGS.id  -> {}
            else -> throw EDecodeFault("Unknown platform-specific segment type: 0x${type.hex8}")
        }
        log.warning { "MIPS platform-specific segment 0x${type.hex8} may be linker-dependent" }
    }

    override fun checkSegmentFlags(flags: UInt) = Unit

    override fun parseDynamic(hm: MutableMap<ULong, ULong>, tag: ULong, ptr: ULong) {
        when (tag) {
            DT_MIPS_RLD_VERSION.id -> { /* uclibc skips it */ }
            DT_MIPS_FLAGS.id -> { /* uclibc skips it */ }
            DT_MIPS_BASE_ADDRESS.id -> {  /* uclibc skips it */ }
            DT_MIPS_LOCAL_GOTNO.id -> hm[DT_MIPS_LOCAL_GOTNO.id] = ptr
            DT_MIPS_SYMTABNO.id -> symbolTableSize = ptr.opt
            DT_MIPS_UNREFEXTNO.id -> {  /* uclibc skips it */ }
            DT_MIPS_GOTSYM.id -> hm[DT_MIPS_GOTSYM.id] = ptr
            DT_MIPS_PLTGOT_GNU.id -> log.severe { "DT_MIPS_PLTGOT_GNU: Don't know what to do with it, hope it will work..." }
            DT_MIPS_RLD_MAP.id -> log.warning { "DT_MIPS_RLD_MAP: We don't use DEBUG dynamic tags" }
            DT_MIPS_RLD_MAP_REL.id -> log.warning { "DT_MIPS_RLD_MAP2: I swear, we don't use them" }
            else -> throw EDecodeFault("Unknown platform-specific dynamic tag: 0x${tag.hex8}")
        }
    }

    override fun applyStaticRelocationRemoveMe(rel: ElfRel, vaddr: ULong, symbol: ULong, got: Optional<ULong>, data: ULong): ULong {
        //I changed it in order of new relocation signature
        val S = symbol //file.symbolTable!![rel.sym].value
        val A = rel.r_addend

        return when (rel.type) {
            R_MIPS_32.id -> {
                log.warning { "Relocation type: R_MIPS_32 (R_MIPS_ADD)" }
                S + A
            }
            R_MIPS_26.id -> {
                log.warning { "Relocation type: R_MIPS_26" }
                (A + S) ushr 2
            }
            R_MIPS_JUMP_SLOT.id -> {
                log.severe { "Relocation type: R_MIPS_JUMP_SLOT -> Ignore runtime relocations" }
                0uL
            }
            R_MIPS_REL32.id -> {
                data
            }

            else -> TODO("Not implemented type: ${rel.type} (${MipsRelocationType.getNameById(rel.type.uint)})")
        }
    }

    inline fun List<ElfRel>.findCorrespondingRelocation(rel: ElfRel): ElfRel {
//        val type = when (rel.type.mipsRelocation) {
//            R_MIPS_LO16 -> R_MIPS_HI16
//            R_MIPS_HI16 -> R_MIPS_LO16
//            R_MIPS_GOT16 -> R_MIPS_LO16
//            else -> throw Exception("Wrong usage")
//        }
        require(rel.type.mipsRelocation in setOf(R_MIPS_HI16, R_MIPS_GOT16)) { "Unreachable code: $rel" }
        return first { it.type.mipsRelocation == R_MIPS_LO16 && it.ind == rel.ind + 1 }
    }

    private val ahls = mutableMapOf<Int, ULong>()

    private fun getAHL(addend: ULong, relocations: List<ElfRel>, region: ElfRegion, rel: ElfRel): ULong {
        return ahls[rel.ind] ?: let {
            val coRel = relocations.findCorrespondingRelocation(rel) /*?: run {
                require(rel.type.mipsRelocation == R_MIPS_LO16) { "Hi-reloc without Lo-reloc" }
                return addend
            }*/
//            require(coRel.ind == rel.ind + 1) {
//                "Not comes together"
//            }
            val coAddend = readData(region, coRel)
            ((addend shl 16) or coAddend).also {
                ahls[coRel.ind] = it // Next relocation should be
            }
//            val (ahi, alo) = when (rel.type.mipsRelocation) {
//                R_MIPS_LO16 -> coAddend to addend
//                R_MIPS_GOT16, R_MIPS_HI16 -> addend to coAddend
//                else -> throw Exception("Wrong usage")
//            }
//             ((ahi shl 16) or alo).also { ahls[rel.ind + 1] = it }
        }
    }

    override fun readData(region: ElfRegion, rel: ElfRel): ULong {
        val offset = region.toBufferOffset(rel.r_offset).requireInt
        val buffer = region.buffer
        return with (buffer) {
            position(offset)
            when (rel.type.mipsRelocation.field) {
                none -> 0u
                tWord32 -> int.ulong_z
                tArg26 -> TODO()
                vHalf16 -> TODO()
                tHi16, tLo16, ttvHi16, ttvLo16, vRel16-> short.ulong_z
                vLit16 -> TODO()
                vPC16 -> TODO()
            }
        }
    }

    inline fun verify(value: ULong) = require(value ushr 16 == 0uL) { "Verification failed" }

    override fun writeData(region: ElfRegion, rel: ElfRel, value: ULong) {
        val offset = region.toBufferOffset(rel.r_offset).requireInt
        val buffer = region.buffer
        with (buffer) {
            position(offset)
            when (rel.type.mipsRelocation.field) {
                none -> {}
                tWord32 -> putInt(value.int)
                tArg26 -> TODO()
                vHalf16 -> TODO()
                tHi16, tLo16 -> putShort(value.short)
                ttvHi16, ttvLo16-> {
                    if (rel.symbol.name == "_gp_disp")
                        verify(value)
                    putShort(value.short)
                }
                vRel16 -> {
                    verify(value)
                    putShort(value.short)
                }
                vLit16 -> TODO()
                vPC16 -> TODO()
            }
        }
    }
//    override fun relocationSize(type: ULong) = type.mipsRelocation.size.id
    var GP_VALUE: ULong? = null

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
        val L = S // TODO: pass PLT from arguments
        val GOT = got
        val external = rel.symbol.isUndef || rel.symbol.isCommon
        val local = !external
        val _gp_disp = rel.symbol.name == "_gp_disp"
        return when (rel.type.mipsRelocation) {
            R_MIPS_NONE -> null
            R_MIPS_16 -> S + A.signextRenameMeAfter(15)
            R_MIPS_32 -> S + A
            R_MIPS_REL32 -> TODO()
            R_MIPS_26 -> TODO()
            R_MIPS_HI16 -> {
                val AHL = getAHL(A, relocations, region, rel)
                val value = when {
                    _gp_disp -> {
                        val GP = GOT.get + GP_ADDEND
                        (AHL + GP - P).also { GP_VALUE = it }
                    }
                    else -> AHL + S
                }
                (value - (value mask 16).signextRenameMeAfter(15)) ushr 16
            }
            R_MIPS_LO16 -> {
                val AHL = getAHL(A, relocations, region, rel)
                when {
                    _gp_disp -> {
                        val GP = GOT.get + GP_ADDEND
                        AHL + GP - P + 4u
                    }
                    else -> AHL + S
                }
            }
            R_MIPS_GPREL16 -> TODO()
            R_MIPS_LITERAL -> TODO()
            R_MIPS_GOT16,
            R_MIPS_CALL16 -> {
                if (local && rel.type.mipsRelocation == R_MIPS_GOT16)
                    getAHL(A, relocations, region, rel)
                val G = S - GP_VALUE!!
                require((G mask 16).signextRenameMeAfter(15) == G) { "Conversion failed" }
                G mask 16
            }
            R_MIPS_PC16 -> TODO()
            R_MIPS_GPREL32 -> TODO()
            R_MIPS_JALR -> null
            else -> TODO()
        }
    }
    override fun isLoadableSection(type: UInt, access: ElfAccess) = type == SHT_MIPS_ABIFLAGS.id

    override fun isLoadableSegment(type: UInt) : Boolean =  (type == PT_MIPS_ABIFLAGS.id)

    override fun getProgramHeaderTypeNameById(type: UInt): String = MipsSegmentType.getNameById(type)

    override fun getRelocationNameById(type: UInt): String = type.ulong_z.mipsRelocation.name

    override fun isGOTRelated(type: ULong) = type.mipsRelocation in
            setOf(R_MIPS_GOT16, R_MIPS_CALL16, R_MIPS_GOT_HI16, R_MIPS_CALL_HI16, R_MIPS_GOT_LO16, R_MIPS_CALL_LO16)
}