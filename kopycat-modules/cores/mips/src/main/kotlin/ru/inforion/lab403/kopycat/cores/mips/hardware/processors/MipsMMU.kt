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
@file:Suppress("MemberVisibilityCanBePrivate")

package ru.inforion.lab403.kopycat.cores.mips.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * Stub for MIPS address translation
 */
class MipsMMU(parent: Module, name: String, widthOut: ULong, val tlbEntries: Int = 32) :
        AddressTranslator(parent, name, widthOut = widthOut) {

    class TLBEntry(
            val index: Int,
            val pageMask: UInt,
            val entryHi: UInt,
            val entryLo0: UInt,
            val entryLo1: UInt
    ) : ICoreUnit {

        override val name: String = "TLBEntry"

        constructor() : this(0, 0u, 0u, 0u, 0u)

        val Mask = pageMask[31..13]
        val VPN2 = entryHi[31..13]
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
            "%s(%04X Lo0=%08X Lo1=%08X Hi=%08X PM=%08X Mask=%08X VPN2=%08X PFN0=%08X PFN1=%08X)"
                    .format(name, index, entryLo0, entryLo1, entryHi, pageMask, Mask, VPN2, PFN0, PFN1)

        override fun serialize(ctxt: GenericSerializer) = mapOf(
                "index" to index.toString(),
                "pageMask" to pageMask.hex8,
                "entryHi" to entryHi.hex8,
                "entryLo0" to entryLo0.hex8,
                "entryLo1" to entryLo1.hex8)

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {}

        companion object {
            val MASK_RANGE = 31..13
            val VPN2_RANGE = 31..13
            val ASID_RANGE = 7..0

            val PFN_RANGE = 29..6
            val C_RANGE = 5..3
            val D_RANGE = 2
            val V_RANGE = 1
            val G_RANGE = 0

            @Suppress("UNUSED_PARAMETER")
            fun createFromSnapshot(ctxt: GenericSerializer, snapshot: Map<String, String>) = TLBEntry(
                    (snapshot["index"] as String).intByDec,
                    (snapshot["pageMask"] as String).uintByHex,
                    (snapshot["entryHi"] as String).uintByHex,
                    (snapshot["entryLo0"] as String).uintByHex,
                    (snapshot["entryLo1"] as String).uintByHex)

            fun createTlbEntry(entryId: Int, vAddress: ULong, pAddress: ULong, size: Int, pabits: Int): TLBEntry {
                val (mask, evenOddBit) = when (size) {
                    0x0000_1000 -> 0b0000000000000000u to 12
                    0x0000_4000 -> 0b0000000000000011u to 14
                    0x0001_0000 -> 0b0000000000001111u to 16
                    0x0004_0000 -> 0b0000000000111111u to 18
                    0x0010_0000 -> 0b0000000011111111u to 20
                    0x0040_0000 -> 0b0000001111111111u to 22
                    0x0100_0000 -> 0b0000111111111111u to 24
                    0x0400_0000 -> 0b0011111111111111u to 26
                    0x1000_0000 -> 0b1111111111111111u to 28
                    else -> throw GeneralException("Unsupported page size")
                }

                val page = pAddress.uint ushr evenOddBit

                val msb = pabits - 1 - 12
                val lsb = evenOddBit - 12
                require(page[(msb - lsb)..0] == page) { "Can't encode value" }
                val pfn0 = page shl lsb
                val pfn1 = (page + 1u) shl lsb

                val pageMask = insert(mask, MASK_RANGE)

                val entryHi = insert(vAddress[31..13].uint and inv(mask), VPN2_RANGE)
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
                return TLBEntry(entryId, pageMask, entryHi, entryLo0, entryLo1)
            }
        }
    }

    private val mips get() = core as MipsCore

    private val TLB = Array(tlbEntries) { TLBEntry() }

    private var currentTlbIndex = 0

    fun getFreeTlbIndex(): Int {
        if (currentTlbIndex == tlbEntries) {
            currentTlbIndex = mips.cop.regs.Wired.value.int
        }
        return currentTlbIndex++
    }

    fun writeTlbEntry(index: Int, pageMask: UInt, entryHi: UInt, entryLo0: UInt, entryLo1: UInt): TLBEntry {
        val entry = TLBEntry(index, pageMask, entryHi, entryLo0, entryLo1)
        TLB[index] = entry
        return entry
    }

    fun writeTlbEntry(index: Int, vAddress: ULong, pAddress: ULong, size: Int): TLBEntry {
        val entry = TLBEntry.createTlbEntry(index, vAddress, pAddress, size, mips.PABITS)
        TLB[index] = entry
        return entry
    }

    fun readTlbEntry(index: Int): TLBEntry = TLB[index]

    override fun translate(ea: ULong, ss: Int, size: Int,  LorS: AccessAction): ULong {
        if (ea in 0x8000_0000u..0x9FFF_FFFFu || ea in 0xA000_0000u..0xBFFF_FFFFu)
            return ea and 0x1FFF_FFFFu

        var pAddr = 0u
        var found = false
        val pfn: UInt
        val v: UInt
        val d: UInt

        val entryHiASID = mips.cop.regs.EntryHi.value[7..0].uint

        for (i in TLB.indices) {
            val invMask = inv(TLB[i].Mask)
            val vpn = TLB[i].VPN2 and invMask
            val eaPage = ea[31..13].uint and invMask
            val isGlobal = TLB[i].G == 1u
            val isIdEquals = TLB[i].ASID == entryHiASID
            if ((vpn == eaPage) && (isGlobal || isIdEquals)) {

                val evenOddBit = when (TLB[i].Mask) {
                    0b0000000000000000u -> 12
                    0b0000000000000011u -> 14
                    0b0000000000001111u -> 16
                    0b0000000000111111u -> 18
                    0b0000000011111111u -> 20
                    0b0000001111111111u -> 22
                    0b0000111111111111u -> 24
                    0b0011111111111111u -> 26
                    0b1111111111111111u -> 28
                    else -> error("Wrong TLB[$i] mask=${TLB[i].Mask}")
                }
                if (ea[evenOddBit] == 0uL) {
                    pfn = TLB[i].PFN0
                    v = TLB[i].V0
                    d = TLB[i].D0
                } else {
                    pfn = TLB[i].PFN1
                    v = TLB[i].V1
                    d = TLB[i].D1
                }
                if (v == 0u)
                    throw MipsHardwareException.TLBInvalid(LorS, core.pc, ea)
                if (d == 0u && LorS == AccessAction.STORE)
                    throw MipsHardwareException.TLBModified(core.pc, ea)

//                if (found) {
//                    throw GeneralException("[${core.pc.hex8}] Double MMU TLB match for ${ea.hex8}")
//                }

                val msb = mips.PABITS - 1 - 12
                val lsb = evenOddBit - 12
                val page = pfn[msb..lsb]
                val offset = ea[evenOddBit - 1..0].uint

                pAddr = (page shl evenOddBit) or offset
                found = true
                break
            }
        }

        if (!found)
            throw MipsHardwareException.TLBMiss(LorS, core.pc, ea)

        return pAddr.ulong_z
    }

    fun invalidateCache() {

    }

    override fun reset() {
        invalidateCache()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf("name" to name, "TLB" to TLB.map { tlbEntry -> tlbEntry.serialize(ctxt) })
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        (snapshot["TLB"] as ArrayList<Map<String, String>>).forEachIndexed { i, map ->
            TLB[i] = TLBEntry.createFromSnapshot(ctxt, map)
        }
    }
}