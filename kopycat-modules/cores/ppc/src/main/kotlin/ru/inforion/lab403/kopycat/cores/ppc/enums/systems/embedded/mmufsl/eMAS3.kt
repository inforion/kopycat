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
package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl



enum class eMAS3(val bit: Int) {

    RPNLHigh(31),       // Real page number
    RPNLLow(12),

    //11..10 (52..53 in PPC notation) - reserved

    U0(9),              // User bits
    U1(8),
    U2(7),
    U3(6),

    // Permissions bits
    UX(5),              // User execute
    SX(4),              // Supervisor execute
    UW(3),              // User write
    SW(2),              // Supervisor write
    UR(1),              // User read
    SR(0);              // Supervisor read

    companion object {
        val RPNL = RPNLHigh.bit..RPNLLow.bit
    }

}