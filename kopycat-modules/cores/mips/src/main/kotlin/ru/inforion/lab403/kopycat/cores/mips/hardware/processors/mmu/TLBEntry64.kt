/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.cores.mips.hardware.processors.mmu

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.optional.opt
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

object TLBEntry64 {
    val MASK_RANGE = 28..13
    val ASID_RANGE = 7..0

    val PFN_RANGE = 29..6
    val C_RANGE = 5..3
    val D_RANGE = 2
    val V_RANGE = 1
    val G_RANGE = 0

    fun create(
        core: MipsCore,
        entryId: Int,
        vAddress: ULong,
        pAddress: ULong,
        size: Int,
    ): TLBEntry {
        val (mask, evenOddBit) = when (size) {
            0x0000_1000 -> 0b0000000000000000uL to 12 // 4 KB page
            0x0000_4000 -> 0b0000000000000011uL to 14 // 16 KB page
            0x0001_0000 -> 0b0000000000001111uL to 16 // 64 KB page
            0x0004_0000 -> 0b0000000000111111uL to 18 // 256 KB page
            0x0010_0000 -> 0b0000000011111111uL to 20 // 1 MB page
            0x0040_0000 -> 0b0000001111111111uL to 22 // 4 MB page
            0x0100_0000 -> 0b0000111111111111uL to 24 // 16 MB page
            0x0400_0000 -> 0b0011111111111111uL to 26 // 64 MB page
            0x1000_0000 -> 0b1111111111111111uL to 28 // 256 MB page
            else -> throw GeneralException("Unsupported page size")
        }

        val page = pAddress.uint ushr evenOddBit

        val msb = core.PABITS - 1 - 12
        val lsb = evenOddBit - 12
        require(page[(msb - lsb)..0] == page) { "Can't encode value" }
        val pfn0 = page shl lsb
        val pfn1 = (page + 1u) shl lsb

        // с. 48 PRA MIPS64
        val VPN2_RANGE = core.SEGBITS - 1 .. 13
        val pageMask = insert(mask, MASK_RANGE)
        val entryHi = insert(vAddress[VPN2_RANGE] and inv(mask), VPN2_RANGE)
            .insert(0u, ASID_RANGE)

        val entryLo0 = insert(pfn0, PFN_RANGE)
            .insert(1u, C_RANGE)
            .insert(1u, D_RANGE)
            .insert(1u, V_RANGE)
            .insert(1u, G_RANGE)

        val entryLo1 = insert(pfn1, PFN_RANGE)
            .insert(1u, C_RANGE)
            .insert(1u, D_RANGE)
            .insert(1u, V_RANGE)
            .insert(1u, G_RANGE)

        return TLBEntry(
            entryId,
            pageMask,
            entryHi,
            entryLo0,
            entryLo1,
            core.cop.regs.MMID.mmid,
            core.cop.regs.Config4.ae,
            core.segmask.opt,
        )
    }

    fun create(core: MipsCore) = TLBEntry(
        0,
        0u,
        0u,
        0u,
        0u,
        0u,
        core.cop.regs.Config4.ae,
        core.segmask.opt,
    )
}
