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
package ru.inforion.lab403.elfloader.processors.mips.enums

enum class MipsSectionFlags(val id: Int) {
    SHF_MIPS_GPREL      (0x10000000),          // This section must be in the global data area.
    SHF_MIPS_MERGE      (0x20000000),          // This section should be merged.
    SHF_MIPS_ADDR       (0x40000000),          // This section contains address data of size implied by section element size.
    SHF_MIPS_NOSTRIP    (0x08000000),          // This section may not be stripped.
    SHF_MIPS_LOCAL      (0x04000000),          // This section is local to threads.
    SHF_MIPS_NAMES      (0x02000000),          // Linker should generate implicit weak names for this section.
    SHF_MIPS_NODUPES    (0x01000000),          // Section contais text/data which may be replicated in other sections.  Linker should retain only one copy.
    SHF_MIPS_STRING     (0x80000000.toInt());     // This section contains string data.
}