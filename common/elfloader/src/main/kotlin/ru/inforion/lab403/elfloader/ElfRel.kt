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
package ru.inforion.lab403.elfloader

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import java.nio.ByteBuffer


class ElfRel constructor(
        val ind: Int,

        // AT LEAST: for relocatable file it is the byte offset from the beginning of the section to the storage unit
        // Virtual address of the storage unit affected by the relocation
        val vaddr: Long,

        // symbol table index and the type of relocation
        val sym: Int,
        val type: Int,

        val addend: Int,

        val withAddend: Boolean,

        val symtabIndex: Int,
        val sectionIndex: Int,
) {

    companion object {
        @Transient val log = logger(INFO)

        fun ByteBuffer.elfRel(
                ind: Int,
                off: Int,
                size: Int,
                withAddend: Boolean,
                symtabIndex: Int = 0,
                sectionIndex: Int = 0
        ): ElfRel {
            position(off + ind * size)

            val vaddr = int.asULong
            val info = int
            val addend = if (withAddend) int else 0

            val sym = info ushr 8
            val type = info and 0xFF

            return ElfRel(ind, vaddr, sym, type, addend, withAddend, symtabIndex, sectionIndex)
        }
    }

    // Section to which relocation applies

    init {
        log.fine { "Relocation index=$ind address=0x${vaddr.hex8} sym=$sym type=$type addend=$addend" }
    }

}