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
package ru.inforion.lab403.kopycat.cores.v850es.enums


enum class CONDITION(val bits: Int) {
    BGE(0b1110),
    BGT(0b1111),
    BLE(0b0111),
    BLT(0b0110),

    BH(0b1011),
    BL(0b0001),
    BNH(0b0011),
    BNL(0b1001),

    BE(0b0010),
    BNE(0b1010),

    BC(0b0001),
    BN(0b0100),
    BNC(0b1001),
    BNV(0b1000),
    BNZ(0b1010),
    BP(0b1100),
    BR(0b0101),
    BSA(0b1101),
    BV(0b0000),
    BZ(0b0010),

    NONE(0b0000)
}