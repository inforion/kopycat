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

import ru.inforion.lab403.elfloader.enums.ElfProgramHeaderFlag.*
import ru.inforion.lab403.elfloader.enums.ElfSectionHeaderFlag.*



class ElfAccess(val flags: Int) {
    val isRead = flags and 1 != 0
    val isWrite = flags and 2 != 0
    val isExec = flags and 4 != 0
    val isLoad = flags and 8 != 0

    companion object {
        fun fromSectionHeaderFlags(sflags: Int): ElfAccess {
            val write = if (sflags and SHF_WRITE.id != 0) 2 else 0
            val exec = if (sflags and SHF_EXECINSTR.id != 0) 4 else 0
            val load = if (sflags and SHF_ALLOC.id != 0) 8 else 0
            return ElfAccess(1 or write or exec or load)
        }

        fun fromProgramHeaderFlags(pflags: Int): ElfAccess {
            val read = if (pflags and PF_R.id != 0) 1 else 0
            val write = if (pflags and PF_W.id != 0) 2 else 0
            val exec = if (pflags and PF_X.id != 0) 4 else 0
            return ElfAccess(read or write or exec or 8)
        }
        fun virtual(): ElfAccess = ElfAccess(0b1011)
    }
}