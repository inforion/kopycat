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
package ru.inforion.lab403.elfloader.processors.arm

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.elfloader.*
import ru.inforion.lab403.elfloader.enums.ElfOSABI.*
import ru.inforion.lab403.elfloader.enums.ElfObjectSize.*
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderFlag.*
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderType.*
import ru.inforion.lab403.elfloader.exceptions.EBadSection
import ru.inforion.lab403.elfloader.exceptions.EBadSegment
import ru.inforion.lab403.elfloader.exceptions.EDecodeFault
import ru.inforion.lab403.elfloader.processors.AElfDecoder
import ru.inforion.lab403.elfloader.processors.arm.enums.ArmDynamicSectionTag.*
import ru.inforion.lab403.elfloader.processors.arm.enums.ArmRelocationType.*
import ru.inforion.lab403.elfloader.processors.arm.enums.ArmSectionType.*
import ru.inforion.lab403.elfloader.processors.arm.enums.ArmFlags.*
import ru.inforion.lab403.elfloader.processors.arm.enums.ArmSegmentType.*
import ru.inforion.lab403.elfloader.processors.arm.enums.ArmRelocationType
import ru.inforion.lab403.elfloader.processors.arm.enums.ArmSegmentType




 
class ElfDecoderArm (file: ElfFile) : AElfDecoder(file) {
    companion object {
        private val log = logger()
    }

    override val prefix = "ARM"
    override val pltHeadSize = 20
    override val pltEntrySize = 12

    //Thumb code flag
    val thumb: Boolean by lazy { file.entry[0] == 1L }

    //ABI version
    val abi: Int by lazy {
        val data = (file.flags and EF_ARM_ABIMASK.id) shr 24
        if (data > 5)
            TODO("ABI version $data isn't implemented")
        else if (data < 5)
            log.warning { "Obsolete ABI version $data" }
        data
    }

    //BE8-code (armv6)
    val be8: Boolean by lazy { file.flags and EF_ARM_ABI_FLOAT_HARD.id != 0 }

    //GCC-generated info
    val gcc: Int by lazy { file.flags and EF_ARM_GCCMASK.id }

    //Hard float or Soft float (emulation)
    val armhf: Boolean by lazy { file.flags and EF_ARM_ABI_FLOAT_HARD.id != 0 }

    //TODO: to HashTable
    var symbolTableSize : Long? = null
    var preEmptionMap : Long? = null
    var preEmptionMapOffset : Int? = null


    //TODO: Change to header as parameter
    //TODO: Header incapsulation
    override fun checkHeader() {
        if (file.objectSize != CLASS_32.id)
            throw EDecodeFault("Only ELF32 supported for ARM architecture (ARM64 isn't implemented)")

        if (thumb)
            TODO("Be careful - thumb mode was never tested before and may be unimplemented")

        log.fine { "Thumb mode $thumb" }

        if (file.entry[1..0] == 0b10L)
            throw EDecodeFault("Reserved combination of entry point lsb")

        if (file.eh_osabi in ELFOSABI_LOPROC.id..ELFOSABI_HIPROC.id)
            if (file.eh_osabi == 64.toByte()) TODO("ELFOSABI_ARM_AEABI isn't implemented")
            else log.warning { "Unknown platform-specific OS/ABI: 0x${file.eh_osabi.hex2}" }
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

        if ((file.flags and EF_ARM_ABI_FLOAT_HARD.id == 1) && (file.flags and EF_ARM_ABI_FLOAT_SOFT.id == 1))
            throw EDecodeFault("Can't be both of EF_ARM_ABI_FLOAT_HARD and EF_ARM_ABI_FLOAT_SOFT simultaneously")

    }

    //TODO: return Boolean as result of check
    override fun checkSectionType(type: Int) {
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

    override fun checkSectionFlags(flags: Int) {
        if (flags and 0x20000000 != 0)
            throw EDecodeFault("SHF_ARM_NOREAD flag seems to be deprecated") //SHF_ARM_NOREAD - No use to make enum class
        else
            throw EDecodeFault("Unknown section flag: 0x${flags.hex8}")
    }

    override fun checkSectionName(name: String, type: Int, flags: Int) : Boolean {
        val invalid = when (name) {
            ".ARM.preemptmap" -> ((type != SHT_ARM_PREEMPTMAP.id) || (flags != SHF_ALLOC.id))
            ".ARM.attributes" -> ((type != SHT_ARM_ATTRIBUTES.id) || (flags != 0))
            ".ARM.debug_overlay" -> ((type != SHT_ARM_DEBUGOVERLAY.id) || (flags != 0))
            ".ARM.overlay_table" -> TODO("See DBGOVL for details")
            else -> if (name.startsWith(".ARM.exidx"))
                ((type != SHT_ARM_EXIDX.id) || ((flags != SHF_ALLOC.id) && (flags != SHF_LINK_ORDER.id)))
            else if (name.startsWith(".ARM.extab"))
                ((type != SHT_PROGBITS.id) || (flags != SHF_ALLOC.id))
            else {
                log.warning { "Non standard section name: $name" }
                false
            }
        }
        //Kostil.kt
        when (name) {
            ".ARM.preemptmap" -> TODO("Check section destination")
            ".ARM.attributes" -> Unit //Now no needed
            ".ARM.debug_overlay" -> TODO("Check section destination")
            else -> if (name.startsWith(".ARM.exidx"))
                TODO("Check section destination")
            else if (name.startsWith(".ARM.extab"))
                TODO("Check section destination")
        }

        return invalid
    }

    override fun checkSymbolBinding(bind: Int) {
        throw EDecodeFault("ARM not provides platform-specific symbol bindings")
    }

    override fun checkSymbolType(type: Int) {
        throw EDecodeFault("ARM not provides platform-specific symbols")
    }

    //TODO: change name!!!
    override fun checkSegmentType(type: Int) {
        when (type) {
            PT_ARM_ARCHEXT.id -> Unit
            PT_ARM_EXIDX.id -> Unit
            else -> throw EBadSegment("Unknown platform-specific segment type: 0x${type.hex8}")
        }
        log.warning { "ARM platform-specific segment 0x${type.hex8} may be linker-dependent" }
    }

    override fun checkSegmentFlags(flags: Int) = Unit

    override fun parseDynamic(hm: HashMap<Int, Long>, tag: Int, ptr: Long) {
        when (tag) {
            DT_ARM_SYMTABSZ.id -> symbolTableSize = ptr
            DT_ARM_PREEMPTMAP.id -> {
                preEmptionMap = ptr
                TODO("Pre-emption map isn't implemented")
            }
            else -> throw EDecodeFault("Unknown platform-specific dynamic tag: 0x${tag.hex8}")
        }
    }

    override fun applyStaticRelocation(rel: ElfRel, vaddr: Long, symbol: Long, got: Long?, data: Long): Long {
        val A = if (rel.withAddend) rel.addend.toLong() else data
        val S = symbol
        val T = thumb.toInt()

        return when (rel.type) {

            R_ARM_COPY.id -> 0

            R_ARM_JUMP_SLOT.id -> {
                val a = if (rel.withAddend) A else 0L
                (S + a) or T.toLong()
            }

            R_ARM_GLOB_DAT.id -> (S + A) or T.toLong()

            R_ARM_RELATIVE.id -> data

            R_ARM_ABS32.id -> S
            //R_ARM_REL32.id -> (S + A) or T

            else -> TODO("Not implemented type: ${rel.type} (${getRelocationNameById(rel.type)})")
        }
    }

    override fun isLoadableSection(type: Int, access: ElfAccess) : Boolean {
        return super.isLoadableSection(type, access) || (type == SHT_ARM_EXIDX.id && access.isLoad)
    }

    override fun isLoadableSegment(type: Int) : Boolean {
        return super.isLoadableSegment(type) || (type == PT_ARM_ARCHEXT.id ||type == PT_ARM_EXIDX.id)
    }

    override fun getProgramHeaderTypeNameById(type: Int): String = ArmSegmentType.getNameById(type)
    override fun getRelocationNameById(type: Int): String = ArmRelocationType.values().first{ it -> it.id == type}.name
}