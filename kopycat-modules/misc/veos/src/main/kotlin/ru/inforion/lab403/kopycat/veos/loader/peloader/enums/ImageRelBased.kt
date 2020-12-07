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
package ru.inforion.lab403.kopycat.veos.loader.peloader.enums



enum class ImageRelBased(val value: Int) {
    ABSOLUTE (0),
    HIGH(1),
    LOW(2),
    HIGHLOW(3),
    HIGHADJ(4),
    MIPS_JMPADDR(5),
    ARM_MOV32A(5), /* yes, 5 too */
    ARM_MOV32(5), /* yes, 5 too */
    SECTION(6),
    REL(7),
    ARM_MOV32T(7), /* yes, 7 too */
    THUMB_MOV32(7), /* yes, 7 too */
    MIPS_JMPADDR16(9),
    IA64_IMM64(9), /* yes, 9 too */
    DIR64(10),
    HIGH3ADJ(11);

    companion object {
        fun fromValue(value: Int) = values().first { it.value == value }
    }
}