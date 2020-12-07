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
package ru.inforion.lab403.elfloader.enums

import ru.inforion.lab403.common.extensions.*


 
enum class ElfProgramHeaderType(val id: Int) {
    PT_NULL         (0),            // Unused segment
    PT_LOAD         (1),            // Loadable segment
    PT_DYNAMIC      (2),            // Dynamic linking information
    PT_INTERP       (3),            // Location and size of path to interpreter
    PT_NOTE         (4),            // Location and size of auxiliary information
    PT_SHLIB        (5),            // Unspecified
    PT_PHDR         (6),            // Location and size of program header table itself
    PT_TLS          (7),            // Thread-local storage segment
    PT_GNU_EH_FRAME (0x6474E550),   // GCC .eh_frame_hdr segment
    PT_GNU_STACK    (0x6474E551),   // GNU stack
    PT_GNU_RELRO    (0x6474E552),   // Read-only after relocation
    PT_LOPROC       (0x70000000),   // Processor-specific
    PT_HIPROC       (0x7fffffff);

    companion object {
        fun getNameById(id: Int) = find<ElfProgramHeaderType> { it.id == id }?.name

        fun isProcSpecific(id: Int) = id in PT_LOPROC.id..PT_HIPROC.id
    }
}