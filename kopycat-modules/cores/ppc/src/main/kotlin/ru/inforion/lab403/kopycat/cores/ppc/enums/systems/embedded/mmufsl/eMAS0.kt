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



enum class eMAS0(val bit: Int) {

    //31..30 (32..33 in PPC notation) - reserved

    TLBSELHigh(29),     //TLB select
    TLBSELLow(28),

    ESELHigh(27),       //Entry select
    ESELLow(16),

    //15..12 (48..51 in PPC notation) - reserved

    NVHigh(11),         //Next victim
    NVLow(0);

    companion object {
        val TLBSEL = TLBSELHigh.bit..TLBSELLow.bit
        val ESEL = ESELHigh.bit..ESELLow.bit
        val NV = NVHigh.bit..NVLow.bit
    }

}