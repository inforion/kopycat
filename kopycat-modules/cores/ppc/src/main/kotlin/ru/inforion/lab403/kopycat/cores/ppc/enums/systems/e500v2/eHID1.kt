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
package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.e500v2

enum class eHID1(val bit: Int) {

    PLL_CFGHigh(31),        // Reflected directly from the PLL_CFG input pins (read-only)
    PLL_CFGLow(26),

    //25..18 (38..46 in PPC notation) - reserved

    RFXE(17),               // Read fault exception enable

    //16 (47 in PPC notation) - reserved

    R1DPE(15),              // R1 data bus parity enable
    R2DPE(14),              // R2 data bus parity enable

    ASTME(13),              // Address bus streaming mode enable

    ABE(12),                // Address broadcast enable

    //11 (52 in PPC notation) - reserved

    MPXTT(10),              // MPX re-map transfer type

    //9..8 (54..55 in PPC notation) - reserved

    ATS(7),                 // Atomic status (read-only)

    //6..4 (57..59 in PPC notation) - reserved

    MIDHigh(3),             // Reflected directly from the MID input pins (read-only)
    MIDLow(0);

    companion object {
        val PLL_CFG = PLL_CFGHigh.bit..PLL_CFGLow.bit
        val MID = MIDHigh.bit..MIDLow.bit
    }



}