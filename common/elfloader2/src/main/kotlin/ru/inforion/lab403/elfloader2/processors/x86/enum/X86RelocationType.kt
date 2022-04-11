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
package ru.inforion.lab403.elfloader2.processors.x86.enum

import ru.inforion.lab403.elfloader2.exceptions.EBadRelocation


enum class X86RelocationType(val id : ULong, val size: Int) {
    R_386_NONE(0u, 0),              //None
    R_386_32(1u, 4),                //S + A
    R_386_PC32(2u, 4),              //S + A - P
    R_386_GOT32(3u, 4),             //G + A - P
    R_386_PLT32(4u, 4),             //L + A - P
    R_386_COPY(5u, 0),              //Copy
    R_386_GLOB_DAT(6u, 4),          //S
    R_386_JMP_SLOT(7u, 4),          //S
    R_386_RELATIVE(8u, 4),          //B + A
    R_386_GOTOFF(9u, 4),            //S + A - GOT
    R_386_GOTPC(10u, 4),            //GOT + A - P
    R_386_GOT32X(43u, 4);

    companion object {
        fun ULong.x86relocation(onFail: (ULong) -> X86RelocationType) = values().find { this == it.id } ?: onFail(this)
        val ULong.x86relocation get() = x86relocation { throw EBadRelocation("Unknown relocation type $this") }
    }
}