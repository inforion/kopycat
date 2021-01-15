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
package ru.inforion.lab403.elfloader.headers

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.elfloader.exceptions.EBadSegment
import ru.inforion.lab403.elfloader.assertMajorBit
import ru.inforion.lab403.elfloader.enums.ElfProgramHeaderFlag.*
import ru.inforion.lab403.elfloader.enums.ElfProgramHeaderType
import java.nio.ByteBuffer



class ElfProgramHeader private constructor(input: ByteBuffer, val ind: Int) {

    companion object {
        private val log = logger(FINER)

        fun fromPosition(input: ByteBuffer, phoff: Int, phentsize: Short, ind: Int): ElfProgramHeader {
            input.position(phoff + ind * phentsize)
            return ElfProgramHeader(input, ind)
        }
    }

    val type = input.int
    val offset = input.int
    val vaddr = input.int.toULong()
    val paddr = input.int.toULong()
    val filesz = input.int
    val memsz = input.int
    val flags = input.int
    val align = input.int

    init {
        //Overflow assertions...
        //Yeah, still love JVM
        assertMajorBit(type)
        assertMajorBit(offset)
        // assertMajorBit(vaddr)
        // assertMajorBit(paddr)
        assertMajorBit(filesz)
        assertMajorBit(memsz)
        assertMajorBit(flags)
        assertMajorBit(align)

        if (find<ElfProgramHeaderType> { it.id == type } == null && !ElfProgramHeaderType.isProcSpecific(type))
            throw EBadSegment("Unknown segment type 0x${type.hex8}")

//        when (type) {
//            PT_NULL.id -> log.info("Unused segment")
//            PT_LOAD.id -> log.info("Loadable segment")
//            PT_DYNAMIC.id -> log.info("Dynamic linking information")
//            PT_INTERP.id -> log.info("Location and size of path to interpreter")
//            PT_NOTE.id -> log.info("Location and size of auxiliary information")
//            PT_SHLIB.id -> throw Exception("Unspecified segment type \"PT_SHLIB\"")
//            PT_PHDR.id -> log.info("Location and size of program header table itself")
//            PT_TLS.id -> log.info("Thread-local storage segment")
//            in PT_GNU_EH_FRAME.id..PT_GNU_RELRO.id -> log.info("GNU GCC segment")
//            in PT_LOPROC.id..PT_HIPROC.id -> Unit   //ElfFile routine
//            else -> throw Exception("Unknown segment type ${type.hex8}")
//        }

        //Mask of unimplemented flags
        if (flags and 0xFFFFFFF8.toInt() != 0)
            TODO("Other segment flags isn't implemented: ${flags.sbits}")

        val flagR = if (flags and PF_R.id != 0) "r" else "-"
        val flagW = if (flags and PF_W.id != 0) "w" else "-"
        val flagX = if (flags and PF_X.id != 0) "x" else "-"

        log.info {
            val name = ElfProgramHeaderType.getNameById(type) ?: "unknown"
            "[$ind] ${name.stretch(16)} $flagR$flagW$flagX off=0x${offset.hex8} " +
                    "vaddr=0x${vaddr.hex8} paddr=0x${paddr.hex8} " +
                    "filesz=0x${filesz.hex8} memsz=0x${memsz.hex8} " +
                    "align=$align"
        }
    }
}