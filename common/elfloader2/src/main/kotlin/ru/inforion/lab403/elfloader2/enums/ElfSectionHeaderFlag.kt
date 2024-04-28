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
package ru.inforion.lab403.elfloader2.enums

enum class ElfSectionHeaderFlag(val mask: ULong) {
    SHF_WRITE(0x1u),
    SHF_ALLOC(0x2u),
    SHF_EXECINSTR(0x4u),
    SHF_MERGE(0x10u),
    SHF_STRINGS(0x20u),
    SHF_INFO_LINK(0x40u),
    SHF_LINK_ORDER(0x80u),

    SHF_GROUP(0x200u),

    SHF_MASKPROC(0xF0000000u)
}