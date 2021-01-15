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
package ru.inforion.lab403.elfloader.processors.mips

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.elfloader.exceptions.EDecodeFault
import ru.inforion.lab403.elfloader.ElfAccess
import ru.inforion.lab403.elfloader.ElfFile
import ru.inforion.lab403.elfloader.ElfRel
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderType.*
import ru.inforion.lab403.elfloader.processors.AElfDecoder
import ru.inforion.lab403.elfloader.processors.mips.enums.MipsDynamicSectionTag.*
import ru.inforion.lab403.elfloader.processors.mips.enums.MipsSectionType.*
import ru.inforion.lab403.elfloader.processors.mips.enums.MipsRelocationType
import ru.inforion.lab403.elfloader.processors.mips.enums.MipsRelocationType.*
import ru.inforion.lab403.elfloader.processors.mips.enums.MipsSegmentType
import ru.inforion.lab403.elfloader.processors.mips.enums.MipsSegmentType.*


class ElfDecoderMips(file: ElfFile) : AElfDecoder(file) {

    companion object {
        private val log = logger()
    }

    override val prefix = "MIPS"
    override val pltHeadSize = 20
    override val pltEntrySize = 12

    //ABI version
    var abi = 0
        private set

    //GCC-generated info
    var gcc = 0
        private set

    var symbolTableSize : Long? = null


    //TODO: Change to header as parameter
    //TODO: Header incapsulation
    override fun checkHeader() { }

    override fun checkFlags() {
        abi = 0
        gcc = 0
    }

    //TODO: return Boolean as result of check
    override fun checkSectionType(type: Int) {
    }

    override fun checkSectionFlags(flags: Int) {
        if (flags and 0x10000000 != 0) // TODO: SHF_MIPS_GPREL as part of enum
            log.warning { "Warning! SHF_MIPS_GPREL flag is not implemented" }
    }

    override fun checkSectionName(name: String, type: Int, flags: Int) : Boolean {
        return when (name) {
            ".MIPS.abiflags" -> (type != SHT_MIPS_ABIFLAGS.id)
            ".MIPS.stubs" -> (type != SHT_PROGBITS.id)
            else -> {
                log.warning { "Non standard section name: $name" }
                false
            }
        }
    }

    //FIXME: SYMBOL TABLE SHOULD CALL THEM
    override fun checkSymbolBinding(bind: Int) {
    }

    override fun checkSymbolType(type: Int) {
    }

    //TODO: change name!!!
    override fun checkSegmentType(type: Int) {
        when (type) {
            PT_MIPS_REGINFO.id   -> {}
            PT_MIPS_RTPROC.id    -> {}
            PT_MIPS_OPTIONS.id	 -> {}
            PT_MIPS_ABIFLAGS.id  -> {}
            else -> throw EDecodeFault("Unknown platform-specific segment type: 0x${type.hex8}")
        }
        log.warning { "MIPS platform-specific segment 0x${type.hex8} may be linker-dependent" }
    }

    override fun checkSegmentFlags(flags: Int) { }

    override fun parseDynamic(hm: HashMap<Int, Long>, tag: Int, ptr: Long) {
        when (tag) {
            DT_MIPS_RLD_VERSION.id -> { /* uclibc skips it */ }
            DT_MIPS_FLAGS.id -> { /* uclibc skips it */ }
            DT_MIPS_BASE_ADDRESS.id -> {  /* uclibc skips it */ }
            DT_MIPS_LOCAL_GOTNO.id -> hm[DT_MIPS_LOCAL_GOTNO.id] = ptr
            DT_MIPS_SYMTABNO.id -> symbolTableSize = ptr
            DT_MIPS_UNREFEXTNO.id -> {  /* uclibc skips it */ }
            DT_MIPS_GOTSYM.id -> hm[DT_MIPS_GOTSYM.id] = ptr
            DT_MIPS_PLTGOT_GNU.id -> log.severe { "DT_MIPS_PLTGOT_GNU: Don't know what to do with it, hope it will work..." }
            DT_MIPS_RLD_MAP.id -> log.warning { "DT_MIPS_RLD_MAP: We don't use DEBUG dynamic tags" }
            DT_MIPS_RLD_MAP_REL.id -> log.warning { "DT_MIPS_RLD_MAP2: I swear, we don't use them" }
            else -> throw EDecodeFault("Unknown platform-specific dynamic tag: 0x${tag.hex8}")
        }
    }

    override fun applyStaticRelocation(rel: ElfRel, vaddr: Long, symbol: Long, got: Long?, data: Long): Long{
        //I changed it in order of new relocation signature
        val S = symbol //file.symbolTable!![rel.sym].value
        val A = rel.addend

        return when (rel.type) {
            R_MIPS_32.id -> {
                log.warning { "Relocation type: R_MIPS_32 (R_MIPS_ADD)" }
                S + A
            }
            R_MIPS_26.id -> {
                log.warning { "Relocation type: R_MIPS_26" }
                (A + S) shr 2
            }
            R_MIPS_JUMP_SLOT.id -> {
                log.severe { "Relocation type: R_MIPS_JUMP_SLOT -> Ignore runtime relocations" }
                0
            }
            R_MIPS_REL32.id -> {
                data
            }

            else -> TODO("Not implemented type: ${rel.type} (${MipsRelocationType.getNameById(rel.type)})")
        }
    }

    override fun isLoadableSection(type: Int, access: ElfAccess) : Boolean =
            ((type == SHT_MIPS_ABIFLAGS.id)) && (access.isLoad)

    override fun isLoadableSegment(type: Int) : Boolean =  (type == PT_MIPS_ABIFLAGS.id)

    override fun getProgramHeaderTypeNameById(type: Int): String = MipsSegmentType.getNameById(type)

    override fun fixPaddr(addr: Long): Long {
        return addr
        //return if (addr in 0x80000000..0xC0000000) addr and 0x1FFF_FFFF else addr
    }

    override fun getRelocationNameById(type: Int): String = MipsRelocationType.values().first { it.id == type }.name

}