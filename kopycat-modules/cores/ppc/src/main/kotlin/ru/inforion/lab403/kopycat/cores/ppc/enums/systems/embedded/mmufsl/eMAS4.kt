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


 
enum class eMAS4(val bit: Int) {

    TLBSELDHigh(30),
    TLBSELDLow(29),

    TIDSELDHigh(17),
    TIDSELDLow(16),

    TSIZEDHigh(11),
    TSIZEDLow(8),

    X0D(6),
    X1D(5),
    WD(4),
    ID(3),
    MD(2),
    GD(1),
    ED(0);

    companion object {
        val TLBSELD = TLBSELDHigh.bit..TLBSELDLow.bit
        val TIDSELD = TIDSELDHigh.bit..TIDSELDLow.bit
        val TSIZED = TSIZEDHigh.bit..TSIZEDLow.bit
    }

}