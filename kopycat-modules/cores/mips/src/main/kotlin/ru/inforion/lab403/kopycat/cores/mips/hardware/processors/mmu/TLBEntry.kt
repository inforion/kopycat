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
import ru.inforion.lab403.common.optional.Optional
import ru.inforion.lab403.common.optional.emptyOpt
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class TLBEntry(
    val index: Int,
    val pageMask: ULong,
    val entryHi: ULong,
    val entryLo0: UInt,
    val entryLo1: UInt,
    val mmid: ULong,
    val ae: Boolean,
    mips64segmask: Optional<ULong> = emptyOpt(),
) : ICoreUnit {
    override val name: String = "TLBEntry"

    private fun pfn(entrylo: UInt, mips64: Boolean) = if (!mips64) {
        entrylo[29..6].ulong_z // or entrylo[63..32] shl 24
    } else {
        (entrylo[59..6].ulong_z and Mask.inv()) shl 12
    }

    val Mask = pageMask[63..13]
    val G = entryLo0[0] and entryLo1[0]

    fun ASID(mi: Boolean) = when {
        mi -> mmid
        ae -> entryHi[9..0]
        else -> entryHi[7..0]
    }

    val EHINV = entryHi[10].truth

    val VPN2 = if (mips64segmask.isEmpty) {
        entryHi[31..13]
    } else {
        (entryHi mask 63..13) and mips64segmask.get
    }

    val PFN0 = pfn(entryLo0, mips64segmask.isPresent)
    val C0 = entryLo0[5..3]
    val D0 = entryLo0[2]
    val V0 = entryLo0[1]

    val PFN1 = pfn(entryLo1, mips64segmask.isPresent)
    val C1 = entryLo1[5..3]
    val D1 = entryLo1[2]
    val V1 = entryLo1[1]

    val XI1 = entryLo1[62]
    val XI0 = entryLo0[62]
    val RI1 = entryLo1[63]
    val RI0 = entryLo0[63]

    fun isValidForAddress(core: MipsCore, addr: ULong): Boolean {
        val wantedMMId = if (core.cop.regs.Config5.mi) {
            core.cop.regs.MMID.mmid
        } else {
            core.cop.regs.EntryHi.ASID
        }

        val mask = pageMask or ubitMask64(13)
        var wanted = addr and mask.inv()
        val vpn = VPN2 and mask.inv()

        if (core.is64bit) {
            wanted = wanted and core.segmask
        }

        return (G.truth || ASID(core.cop.regs.Config5.mi) == wantedMMId) && vpn == wanted && !EHINV
    }

    override fun toString() =
        "%s(%04X Lo0=%s Lo1=%s Hi=%s PM=%s Mask=%s VPN2=%s PFN0=%s PFN1=%s)"
            .format(
                name,
                index,
                entryLo0.hex8,
                entryLo1.hex8,
                entryHi.hex8,
                pageMask.hex8,
                Mask.hex8,
                VPN2.hex8,
                PFN0.hex8,
                PFN1.hex8
            )

    override fun serialize(ctxt: GenericSerializer) : Map<String, Any> = mapOf(
        "index" to index,
        "pageMask" to pageMask,
        "entryHi" to entryHi,
        "entryLo0" to entryLo0,
        "entryLo1" to entryLo1,
        "mmid" to mmid,
        "ae" to ae,
    )

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) { }
}
