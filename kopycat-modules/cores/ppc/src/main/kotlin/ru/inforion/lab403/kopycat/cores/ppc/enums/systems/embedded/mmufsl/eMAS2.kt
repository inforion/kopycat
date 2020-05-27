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



//WARNING: 64 bit by PowerISA V2.05
enum class eMAS2(val bit: Int) {

    EPNHigh(31),    // Effective page number
    EPNLow(12),

    //11..8 (52..55 in PPC notation) - reserved

    ACMHigh(7),     // Alternate coherency mode
    ACMLow(6),

    // Category: VLE
    VLE(5),         // VLE mode

    W(4),           // Write through

    I(3),           // Caching inhibited

    M(2),           // Memory coherence required

    G(1),           // Guarded

    E(0);           // Endianness (0 - big-endian, 1 - little-endian)

    companion object {
        val EPN = EPNHigh.bit..EPNLow.bit
        val ACM = ACMHigh.bit..ACMLow.bit
    }
}