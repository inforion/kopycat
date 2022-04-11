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
package ru.inforion.lab403.elfloader2.enums


enum class ElfProgramHeaderType(val low: UInt, val high: UInt = low) {
    PT_NULL         (0u),            // Unused segment
    PT_LOAD         (1u),            // Loadable segment
    PT_DYNAMIC      (2u),            // Dynamic linking information
    PT_INTERP       (3u),            // Location and size of path to interpreter
    PT_NOTE         (4u),            // Location and size of auxiliary information
    PT_SHLIB        (5u),            // Unspecified
    PT_PHDR         (6u),            // Location and size of program header table itself
    PT_TLS          (7u),            // Thread-local storage segment
    PT_GNU_EH_FRAME (0x6474E550u),   // GCC .eh_frame_hdr segment
    PT_GNU_STACK    (0x6474E551u),   // GNU stack
    PT_GNU_RELRO    (0x6474E552u),   // Read-only after relocation
    PT_GNU_PROPERTY (0x6474E553u),   // ???
    PT_PROC         (0x70000000u, 0x7fffffffu);   // Processor-specific

    val range = low..high
    val shortName get() = name.removePrefix("PT_")

    companion object {
        fun cast(id: UInt, onFail: (UInt) -> ElfProgramHeaderType) = values().find { id in it.range } ?: onFail(id)
        fun castOrThrow(id: UInt) = cast(id) { throw NotImplementedError("Unknown program header type: $it") }
    }
}