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



enum class eMAS1(val bit: Int) {

    V(31),          // TLB valid bit

    IPROT(30),      // Invalid protect

    TIDHigh(29),    // Translation identity
    TIDLow(16),

    //15..13 (48..50 in PPC notation) - reserved

    TS(12),         // Translation space

    TSIZEHigh(11),  // Translation size
    TSIZELow(8);

    //7..0 (56..63 in PPC notation) - reserved

    companion object {
        val TID = TIDHigh.bit..TIDLow.bit
        val TSIZE = TSIZEHigh.bit..TSIZELow.bit
    }
}