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
package ru.inforion.lab403.elfloader2.headers

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.elfloader2.*
import ru.inforion.lab403.elfloader2.enums.ElfProgramHeaderFlag.*
import ru.inforion.lab403.elfloader2.enums.ElfProgramHeaderType


class ElfProgramHeader constructor(
    val ind: Int,
    val p_type_value: UInt,
    val p_offset: ULong,
    val p_vaddr: ULong,
    val p_paddr: ULong,
    val p_filesz: ULong,
    val p_memsz: ULong,
    val p_flags: UInt,
    val p_align: ULong) {

    companion object {
        private val log = logger(WARNING)

        fun IElfDataTypes.elfProgramHeader(ind: Int, offset: ULong, entrySize: UShort): ElfProgramHeader {
            val pos = offset + ind.uint * entrySize
            position = pos.requireInt
            return when (this) {
                is ElfDataTypes32 -> ElfProgramHeader(ind, p_type_value = word, p_offset = off, p_vaddr = addr, p_paddr = addr,
                    p_filesz = wordpref, p_memsz = wordpref, p_flags = word, p_align = wordpref)
                is ElfDataTypes64 -> ElfProgramHeader(ind, p_type_value = word, p_flags = word, p_offset = off, p_vaddr = addr,
                    p_paddr = addr, p_filesz = wordpref, p_memsz = wordpref, p_align = wordpref)
                else -> throw NotImplementedError("Unknown ElfDataType: $this")
            }
        }
    }

    val p_type = ElfProgramHeaderType.castOrThrow(p_type_value)

    val fileRange = p_vaddr until (p_vaddr + p_filesz)
    val memRange = p_vaddr until (p_vaddr + p_memsz)
    val maxRange = p_vaddr until (p_vaddr + maxOf(p_filesz, p_memsz))


    init {
        //Mask of unimplemented flags
        if (p_flags and 0xFFFFFFF8u != 0u)
            TODO("Other segment flags isn't implemented: ${p_flags.binary}")

        val flagR = if (p_flags and PF_R.mask != 0u) "r" else "-"
        val flagW = if (p_flags and PF_W.mask != 0u) "w" else "-"
        val flagX = if (p_flags and PF_X.mask != 0u) "x" else "-"

        log.info {
            val name = p_type.shortName
            "Program [$ind] ${name.stretch(16)} $flagR$flagW$flagX off=0x${p_offset.hex8} " +
                    "vaddr=0x${p_vaddr.hex8} paddr=0x${p_paddr.hex8} " +
                    "filesz=0x${p_filesz.hex8} memsz=0x${p_memsz.hex8} " +
                    "align=$p_align"
        }
    }
}