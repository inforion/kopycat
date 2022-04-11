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
@file:Suppress("unused")

package ru.inforion.lab403.elfloader2.processors.arm

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.optional.Optional
import ru.inforion.lab403.common.optional.emptyOpt
import ru.inforion.lab403.common.optional.opt
import ru.inforion.lab403.elfloader2.*
import ru.inforion.lab403.elfloader2.enums.ELFOSABI.*
import ru.inforion.lab403.elfloader2.enums.ELFCLASS.*
import ru.inforion.lab403.elfloader2.enums.ElfSectionHeaderFlag.*
import ru.inforion.lab403.elfloader2.enums.ElfSectionHeaderType.*
import ru.inforion.lab403.elfloader2.exceptions.EBadSection
import ru.inforion.lab403.elfloader2.exceptions.EBadSegment
import ru.inforion.lab403.elfloader2.exceptions.EDecodeFault
import ru.inforion.lab403.elfloader2.processors.AElfDecoder
import ru.inforion.lab403.elfloader2.processors.arm.enums.ArmDynamicSectionTag.*
import ru.inforion.lab403.elfloader2.processors.arm.enums.ArmFlags.*
import ru.inforion.lab403.elfloader2.processors.arm.enums.ArmRelocationType.*
import ru.inforion.lab403.elfloader2.processors.arm.enums.ArmRelocationType.Companion.Field.*
import ru.inforion.lab403.elfloader2.processors.arm.enums.ArmSectionType.*
import ru.inforion.lab403.elfloader2.processors.arm.enums.ArmSegmentType.*
import ru.inforion.lab403.elfloader2.processors.arm.enums.ArmRelocationType.Companion.armRelocation
import ru.inforion.lab403.elfloader2.processors.arm.enums.ArmSegmentType

class ElfDecoderArm (file: ElfFile) : AElfDecoder(file) {
    companion object {
        private val log = logger()
    }

    override val prefix = "ARM"
//    override val pltHeadSize = 20
//    override val pltEntrySize = 12

    //Thumb code flag
    val thumb by lazy { file.elfHeader.e_entry[0] == 1uL }

    //ABI version
    val abi by lazy {
        val data = (file.elfHeader.e_flags and EF_ARM_ABIMASK.id) ushr 24
        if (data > 5u)
            TODO("ABI version $data isn't implemented")
        else if (data < 5u)
            log.warning { "Obsolete ABI version $data" }
        data
    }

    //BE8-code (armv6)
    val be8 by lazy { file.elfHeader.e_flags and EF_ARM_ABI_FLOAT_HARD.id != 0u }

    //GCC-generated info
    val gcc by lazy { file.elfHeader.e_flags and EF_ARM_GCCMASK.id }

    //Hard float or Soft float (emulation)
    val armhf by lazy { file.elfHeader.e_flags and EF_ARM_ABI_FLOAT_HARD.id != 0u }

    //TODO: to HashTable
    var symbolTableSize: Optional<ULong> = emptyOpt()
    var preEmptionMap: Optional<ULong> = emptyOpt()
    var preEmptionMapOffset : Int? = null


    //TODO: Change to header as parameter
    //TODO: Header incapsulation
    override fun checkHeader() {
        if (file.elfHeader.e_ident_class != ELFCLASS32)
            throw EDecodeFault("Only ELF32 supported for ARM architecture (ARM64 isn't implemented)")

        if (thumb)
            TODO("Be careful - thumb mode was never tested before and may be unimplemented")

        log.fine { "Thumb mode $thumb" }

        if (file.elfHeader.e_entry[1..0] == 0b10uL)
            throw EDecodeFault("Reserved combination of entry point lsb")

        if (file.elfHeader.e_ident_osabi == ELFOSABI_PROC)
            if (file.elfHeader.e_ident_osabi_value.int_z == 64) TODO("ELFOSABI_ARM_AEABI isn't implemented")
            else log.warning { "Unknown platform-specific OS/ABI: 0x${file.elfHeader.e_ident_osabi_value.hex2}" }
    }

    override fun checkFlags() {
        /*abi = (flags and EF_ARM_ABIMASK.id) shr 24
        if (abi > 5)
            TODO("ABI version $abi isn't implemented")
        else if (abi < 5)
            log.warning("Obsolete ABI version $abi")

        be8 = flags and EF_ARM_ABI_FLOAT_HARD.id != 0

        gcc = flags and EF_ARM_GCCMASK.id


        armhf = (flags and EF_ARM_ABI_FLOAT_HARD.id != 0)
        */
        log.fine { "GCC info is 0x${gcc.hex8}" }

        if (file.elfHeader.e_flags and EF_ARM_ABI_FLOAT_HARD.id == 1u && file.elfHeader.e_flags and EF_ARM_ABI_FLOAT_SOFT.id == 1u)
            throw EDecodeFault("Can't be both of EF_ARM_ABI_FLOAT_HARD and EF_ARM_ABI_FLOAT_SOFT simultaneously")

    }

    //TODO: return Boolean as result of check
    override fun checkSectionType(type: UInt) {
        //Kostil.kt
        when(type) {
            SHT_ARM_EXIDX.id -> TODO("ARM platform-specific sections still isn't implemented: 0x${type.hex8}")
            SHT_ARM_PREEMPTMAP.id -> TODO("ARM platform-specific sections still isn't implemented: 0x${type.hex8}")
            SHT_ARM_ATTRIBUTES.id -> Unit //Now no needed
            SHT_ARM_DEBUGOVERLAY.id -> TODO("ARM platform-specific sections still isn't implemented: 0x${type.hex8}")
            SHT_ARM_OVERLAYSECTION.id -> TODO("ARM platform-specific sections still isn't implemented: 0x${type.hex8}")
            else -> throw EBadSection("Unknown platform-specific section type: 0x${type.hex8}")
        }

    }

    override fun checkSectionFlags(flags: ULong) {
        if (flags and 0x20000000u != 0uL)
            throw EDecodeFault("SHF_ARM_NOREAD flag seems to be deprecated") //SHF_ARM_NOREAD - No use to make enum class
        else
            throw EDecodeFault("Unknown section flag: 0x${flags.hex8}")
    }

    override fun checkSectionName(name: String, type: UInt, flags: ULong) : Boolean {
        val invalid = when (name) {
            ".ARM.preemptmap" -> ((type != SHT_ARM_PREEMPTMAP.id) || flags != SHF_ALLOC.mask)
            ".ARM.attributes" -> ((type != SHT_ARM_ATTRIBUTES.id) || flags != 0uL)
            ".ARM.debug_overlay" -> ((type != SHT_ARM_DEBUGOVERLAY.id) || flags != 0uL)
            ".ARM.overlay_table" -> TODO("See DBGOVL for details")
            else -> when {
                name.startsWith(".ARM.exidx") ->
                    type != SHT_ARM_EXIDX.id || flags != SHF_ALLOC.mask && flags != SHF_LINK_ORDER.mask
                name.startsWith(".ARM.extab") ->
                    type != SHT_PROGBITS.low || flags != SHF_ALLOC.mask
                else -> {
                    log.warning { "Non standard section name: $name" }
                    false
                }
            }
        }
        //Kostil.kt
        when (name) {
            ".ARM.preemptmap" -> TODO("Check section destination")
            ".ARM.attributes" -> Unit //Now no needed
            ".ARM.debug_overlay" -> TODO("Check section destination")
            else -> when {
                name.startsWith(".ARM.exidx") -> TODO("Check section destination")
                name.startsWith(".ARM.extab") -> TODO("Check section destination")
            }
        }

        return invalid
    }

    override fun checkSymbolBinding(bind: UInt): Unit =
        throw EDecodeFault("ARM not provides platform-specific symbol bindings")

    override fun checkSymbolType(type: Int): Unit = throw EDecodeFault("ARM not provides platform-specific symbols")

    //TODO: change name!!!
    override fun checkSegmentType(type: UInt) {
        when (type) {
            PT_ARM_ARCHEXT.id -> Unit
            PT_ARM_EXIDX.id -> Unit
            else -> throw EBadSegment("Unknown platform-specific segment type: 0x${type.hex8}")
        }
        log.warning { "ARM platform-specific segment 0x${type.hex8} may be linker-dependent" }
    }

    override fun checkSegmentFlags(flags: UInt) = Unit

    override fun parseDynamic(hm: MutableMap<ULong, ULong>, tag: ULong, ptr: ULong) {
        when (tag) {
            DT_ARM_SYMTABSZ.id -> symbolTableSize = ptr.opt
            DT_ARM_PREEMPTMAP.id -> {
                preEmptionMap = ptr.opt
                TODO("Pre-emption map isn't implemented")
            }
            else -> throw EDecodeFault("Unknown platform-specific dynamic tag: 0x${tag.hex8}")
        }
    }

    override fun applyStaticRelocationRemoveMe(rel: ElfRel, vaddr: ULong, symbol: ULong, got: Optional<ULong>, data: ULong): ULong {
        TODO("Remove me")
//        val A = if (rel.withAddend) rel.r_addend.ulong else data
//        val S = symbol
//        val T = thumb.int.ulong_z
//
//        return when (rel.type.uint) {
//
//            R_ARM_COPY.id -> 0u
//
//            R_ARM_JUMP_SLOT.id -> {
//                val a = if (rel.withAddend) A else 0u
//                (S + a) or T
//            }
//
//            R_ARM_GLOB_DAT.id -> (S + A) or T
//
//            R_ARM_RELATIVE.id -> data
//
//            R_ARM_ABS32.id -> S
//            //R_ARM_REL32.id -> (S + A) or T
//
//            else -> TODO("Not implemented type: ${rel.type} (${getRelocationNameById(rel.type.uint)})")
//        }
    }
    override fun relocationSize(type: ULong) = 0 //type.armRelocation.size

    override fun isLoadableSection(type: UInt, access: ElfAccess) = type == SHT_ARM_EXIDX.id

    override fun isLoadableSegment(type: UInt) : Boolean {
        return type == PT_ARM_ARCHEXT.id ||type == PT_ARM_EXIDX.id
    }

    override fun isSymbolUndefined(sym: ElfSymbol) = sym.isUndef && sym.ind != 0

    override fun readData(region: ElfRegion, rel: ElfRel): ULong {
        val offset = region.toBufferOffset(rel.r_offset).requireInt
        val buffer = region.buffer
        return with(buffer) {
            position(offset)
            when (rel.type.armRelocation.field) {
                none -> 0u
                word32 -> int.ulong_z
                half16 -> short.ulong_z
                byte8 -> byte.ulong_z
                arm_bl_blx, arm_bl, arm_blx, arm_b_bl -> (int.ulong_z[23..0] shl 2).signextRenameMeAfter(25)
                arm_add_sub,
                arm_ldr_str,
                arm_ldr_r_pc,
                arm_swi,
                thumb_bl_pair,
                thumb_blx_pair,
                thumb_ldr_str,
                thumb_ldr_r_pc,
                thumb_swi,
                amp_vcall -> TODO()
            }
        }
    }

    private inline fun verify(value: ULong, mask: ULong) = require(value and mask == value) { "Verification failed" }

    override fun writeData(region: ElfRegion, rel: ElfRel, value: ULong) {
        val offset = region.toBufferOffset(rel.r_offset).requireInt
        val buffer = region.buffer
        with (buffer) {
            position(offset)
            when (rel.type.armRelocation.field) {
                none -> 0u
                word32 -> putInt(value.int)
                half16 -> putShort(value.short)
                byte8 -> put(value.byte)
                arm_bl_blx, arm_bl, arm_blx, arm_b_bl -> {
                    verify(value, 0x03FFFFFEu)
                    int.insert(value.int ushr 2, 23..0).also {
                        position(offset)
                        putInt(it)
                    }
                }
                arm_add_sub,
                arm_ldr_str,
                arm_ldr_r_pc,
                arm_swi,
                thumb_bl_pair,
                thumb_blx_pair,
                thumb_ldr_str,
                thumb_ldr_r_pc,
                thumb_swi,
                amp_vcall -> TODO()
            }
        }
    }

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
        val B = S
        val GOTS = S
        val GOT_ORG = got

        return when (rel.type.armRelocation) {
            R_ARM_NONE -> null
            R_ARM_PC24 -> S - P + A
            R_ARM_ABS32 -> S + A
            R_ARM_REL32 -> S - P + A
            R_ARM_LDR_PC_G0 -> S - P + A
            R_ARM_ABS16 -> S + A
            R_ARM_ABS12 -> S + A
            R_ARM_THM_ABS5 -> S + A
            R_ARM_ABS8 -> S + A
            R_ARM_SBREL32 -> TODO("S - B + A")
            R_ARM_THM_CALL -> S - P + A
            R_ARM_THM_PC8 -> S - P + A
            R_ARM_AMP_VCALL9 -> TODO("Obsolete")
            R_ARM_SWI24 -> S + A
            R_ARM_THM_SWI8 -> S + A
            R_ARM_XPC25 -> S - P + A
            R_ARM_THM_XPC22 -> S - P + A
            R_ARM_TLS_DTPMOD32 -> TODO()
            R_ARM_TLS_DTPOFF32 -> TODO()
            R_ARM_TLS_TPOFF32 -> TODO()
            R_ARM_COPY -> TODO()
            R_ARM_GLOB_DAT -> TODO()
            R_ARM_JUMP_SLOT -> TODO()
            R_ARM_RELATIVE -> TODO()
            R_ARM_GOTOFF -> TODO()
            R_ARM_BASE_PREL -> B + A - P
            R_ARM_GOT_BREL -> GOTS + A - GOT_ORG.get
            R_ARM_PLT32 -> S + A - P
            R_ARM_CALL -> S + A - P
            R_ARM_JUMP24 -> S + A - P
            R_ARM_THM_JUMP24 -> S + A - P
            R_ARM_BASE_ABS -> TODO()
            R_ARM_ALU_PCREL_7_0 -> (S - P + A) and 0x000000FFu
            R_ARM_ALU_PCREL_15_8 -> (S - P + A) and 0x0000FF00u
            R_ARM_ALU_PCREL_23_15 -> (S - P + A) and 0x00FF0000u
            R_ARM_LDR_SBREL_11_0_NC -> TODO("(S - B + A) and 0x00000FFFu")
            R_ARM_ALU_SBREL_19_12_NC -> TODO("(S - B + A) and 0x000FF000u")
            R_ARM_ALU_SBREL_27_20_CK -> TODO("(S - B + A) and 0x0FF00000u")
            R_ARM_TARGET1 -> TODO()
            R_ARM_SBREL31 -> TODO()
            R_ARM_V4BX -> TODO()
            R_ARM_TARGET2 -> TODO()
            R_ARM_PREL31 -> TODO()
        }

    }


    override fun getProgramHeaderTypeNameById(type: UInt): String = ArmSegmentType.getNameById(type)
    override fun getRelocationNameById(type: UInt): String = type.ulong_z.armRelocation.name

    override fun isGOTRelated(type: ULong) = type.armRelocation == R_ARM_GOT_BREL
}