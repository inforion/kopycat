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

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException

enum class eMMUCSR0(val bit: Int) {

    //31..23 (32..40 in PPC notation) - reserved

    TLB3_PSHigh(22), //Page size of the TLB3 array
    TLB3_PSLow(19),

    TLB2_PSHigh(18), //Page size of the TLB2 array
    TLB2_PSLow(15),

    TLB1_PSHigh(14), //Page size of the TLB1 array
    TLB1_PSLow(11),

    TLB0_PSHigh(10), //Page size of the TLB0 array
    TLB0_PSLow(7),

    TLB2_FI(6), //TLB invalidate all bit for the TLB2 array
    TLB3_FI(5), //TLB invalidate all bit for the TLB3 array

    //4..3 (57..58 in PPC notation) - reserved
    TLB2_FIextra(4), //Because it is used
    TLB3_FIextra(3), //Because it is used

    TLB0_FI(2), //TLB invalidate all bit for the TLB0 array
    TLB1_FI(1); //TLB invalidate all bit for the TLB1 array

    //0 (63 in PPC notation) - reserved

    companion object {
        fun TLBn_PS(n: Int) = when(n) {
            0 -> TLB0_PSHigh.bit..TLB0_PSLow.bit
            1 -> TLB1_PSHigh.bit..TLB1_PSLow.bit
            2 -> TLB2_PSHigh.bit..TLB2_PSLow.bit
            3 -> TLB3_PSHigh.bit..TLB3_PSLow.bit
            else -> throw GeneralException("Unknown TLB index: $n")
        }

        fun TLBn_FI(n: Int) = when(n) {
            0 -> TLB0_FI.bit
            1 -> TLB1_FI.bit
            2 -> TLB2_FI.bit
            3 -> TLB3_FI.bit
            4 -> TLB2_FIextra.bit
            5 -> TLB3_FIextra.bit
            else -> throw GeneralException("Unknown TLB index: $n")
        }
    }
}