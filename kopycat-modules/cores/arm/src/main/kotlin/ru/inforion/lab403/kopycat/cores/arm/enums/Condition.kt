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
package ru.inforion.lab403.kopycat.cores.arm.enums

enum class Condition(val opcode: Int) {
    EQ(0b0000),
    NE(0b0001),
    CS(0b0010),
    CC(0b0011),
    MI(0b0100),
    PL(0b0101),
    VS(0b0110),
    VC(0b0111),
    HI(0b1000),
    LS(0b1001),
    GE(0b1010),
    LT(0b1011),
    GT(0b1100),
    LE(0b1101),
    AL(0b1110),
    UN(0b1111)
}