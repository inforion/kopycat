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


 
enum class ElfSectionHeaderIndex(val id: Short) {
    SHN_UNDEF(0),
    SHN_LORESERVE(0xFF00.toShort()),
    SHN_LOPROC(0xFF00.toShort()),
    SHN_HIPROC(0xFF1F.toShort()),
    SHN_ABS(0xFFF1.toShort()),
    SHN_COMMON(0xFFF2.toShort()),
    SHN_HIRESERVE(0xFFFF.toShort());
}