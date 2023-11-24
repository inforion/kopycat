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
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit

abstract class ATLBEntry(
    val index: Int,
    val pageMask: ULong,
    val entryHi: ULong,
    val entryLo0: UInt,
    val entryLo1: UInt
) : ICoreUnit {

    override val name: String = "TLBEntry"

    abstract val VPN2 : ULong

    val Mask = pageMask[28..13]
    val G = entryLo0[0] and entryLo1[0]
    val ASID = entryHi[7..0]

    val PFN0 = entryLo0[29..6]
    val C0 = entryLo0[5..3]
    val D0 = entryLo0[2]
    val V0 = entryLo0[1]

    val PFN1 = entryLo1[29..6]
    val C1 = entryLo1[5..3]
    val D1 = entryLo1[2]
    val V1 = entryLo1[1]


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

    override fun serialize(ctxt: GenericSerializer) = mapOf(
        "index" to index.toString(),
        "pageMask" to pageMask.hex8,
        "entryHi" to entryHi.hex8,
        "entryLo0" to entryLo0.hex8,
        "entryLo1" to entryLo1.hex8
    )

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {}

    companion object {
        @Suppress("UNUSED_PARAMETER")
        fun createFromSnapshot(ctxt: GenericSerializer, snapshot: Map<String, String>) = TLBEntry32(
            (snapshot["index"] as String).intByDec,
            (snapshot["pageMask"] as String).ulongByHex,
            (snapshot["entryHi"] as String).ulongByHex,
            (snapshot["entryLo0"] as String).uintByHex,
            (snapshot["entryLo1"] as String).uintByHex)
    }
}