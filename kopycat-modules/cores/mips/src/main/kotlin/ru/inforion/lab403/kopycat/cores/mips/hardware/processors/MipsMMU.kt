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
package ru.inforion.lab403.kopycat.cores.mips.hardware.processors

import net.sourceforge.argparse4j.inf.ArgumentParser
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.interfaces.IInteractive
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import java.util.*

/**
 *
 * Stub for MIPS address translation
 */
class MipsMMU(parent: Module, name: String, widthOut: Long, val tlbEntries: Int = 32) :
        AddressTranslator(parent, name, widthOut = widthOut) {

    class TLBEntry(
            val index: Int,
            val pageMask: Long,
            val entryHi: Long,
            val entryLo0: Long,
            val entryLo1: Long) : ICoreUnit {
        override val name: String = "TLBEntry"

        constructor() : this(0, 0, 0, 0, 0)

        val Mask: Int = pageMask[31..13].toInt()
        val VPN2: Int = entryHi[31..13].toInt()
        val G: Int = entryLo0[0].toInt() and entryLo1[0].toInt()
        val ASID: Int = entryHi[7..0].toInt()

        val PFN0: Int = entryLo0[29..6].toInt()
        val C0: Int = entryLo0[5..3].toInt()
        val D0: Int = entryLo0[2].toInt()
        val V0: Int = entryLo0[1].toInt()

        val PFN1: Int = entryLo1[29..6].toInt()
        val C1: Int = entryLo1[5..3].toInt()
        val D1: Int = entryLo1[2].toInt()
        val V1: Int = entryLo1[1].toInt()

        override fun toString(): String {
            return "%s(%04X Lo0=%08X Lo1=%08X Hi=%08X PM=%08X Mask=%08X VPN2=%08X PFN0=%08X PFN1=%08X)"
                    .format(name, index, entryLo0, entryLo1, entryHi, pageMask, Mask, VPN2, PFN0, PFN1)
        }

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
            return mapOf(
                    "index" to index.toString(),
                    "pageMask" to pageMask.hex8,
                    "entryHi" to entryHi.hex8,
                    "entryLo0" to entryLo0.hex8,
                    "entryLo1" to entryLo1.hex8)
        }

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
            fun createFromSnapshot(ctxt: GenericSerializer, snapshot: Map<String, String>): TLBEntry {
                return TLBEntry(
                        (snapshot["index"] as String).toInt(),
                        (snapshot["pageMask"] as String).hexAsULong,
                        (snapshot["entryHi"] as String).hexAsULong,
                        (snapshot["entryLo0"] as String).hexAsULong,
                        (snapshot["entryLo1"] as String).hexAsULong)
            }

            fun createTlbEntry(entryId: Int, vAddress: Long, pAddress: Long, size: Long): TLBEntry {
                if (vAddress != pAddress || vAddress % 0x1000_0000L != 0L || size % 0x1000_0000L != 0L)
                    throw NotImplementedError("Not transparent and small addresses not implemented")
                val index = vAddress / 0x1000_0000L
                val pageMask = insert(0xFFFFL, MASK_RANGE)
                val entryHi = insert(0x10000L * index, VPN2_RANGE)
                        .insert(0, ASID_RANGE)
                val entryLo0 = insert((index * 2) shl 16, PFN_RANGE)
                        .insert(1, C_RANGE)
                        .insert(1, D_RANGE)
                        .insert(1, V_RANGE)
                        .insert(1, G_RANGE)
                val entryLo1 = insert((index * 2 + 1) shl 16, PFN_RANGE)
                        .insert(1, C_RANGE)
                        .insert(1, D_RANGE)
                        .insert(1, V_RANGE)
                        .insert(1, G_RANGE)
                return TLBEntry(entryId, pageMask, entryHi, entryLo0, entryLo1)
            }
        }
    }

    private val mips get() = core as MipsCore

    private val TLB = Array(tlbEntries) { TLBEntry() }

    private var currentTlbIndex = 0

    fun getFreeTlbIndex(): Int {
        if (currentTlbIndex == tlbEntries) {
            currentTlbIndex = mips.cop.regs.Wired.value.asInt
        }
        return currentTlbIndex++
    }

    fun writeTlbEntry(index: Int, pageMask: Long, entryHi: Long, entryLo0: Long, entryLo1: Long): TLBEntry {
        val entry = TLBEntry(index, pageMask, entryHi, entryLo0, entryLo1)
        TLB[index] = entry
        return entry
    }

    fun writeTlbEntry(index: Int, vAddress: Long, pAddress: Long, size: Long): TLBEntry {
        val entry = TLBEntry.createTlbEntry(index, vAddress, pAddress, size)
        TLB[index] = entry
        return entry
    }

    fun readTlbEntry(index: Int): TLBEntry = TLB[index]

    override fun translate(ea: Long, ss: Int, size: Int,  LorS: AccessAction): Long {
        if (ea in 0x8000_0000..0x9FFF_FFFF || ea in 0xA000_0000..0xBFFF_FFFF)
            return ea and 0x1FFF_FFFF

        var pAddr = 0L
        var found = false
        val pfn: Int
        val v: Int
        val d: Int

        val entryHiASID = mips.cop.regs.EntryHi.value[7..0].toInt()

        for (i in 0 until TLB.size) {
            val invMask = TLB[i].Mask.inv()
            val vpn = TLB[i].VPN2 and invMask
            val eaPage = ea[31..13].toInt() and invMask
            val isGlobal = TLB[i].G == 1
            val isIdEquals = TLB[i].ASID == entryHiASID
            if ((vpn == eaPage) && (isGlobal || isIdEquals)) {

                val evenOddBit = when (TLB[i].Mask) {
                    0b0000000000000000 -> 12
                    0b0000000000000011 -> 14
                    0b0000000000001111 -> 16
                    0b0000000000111111 -> 18
                    0b0000000011111111 -> 20
                    0b0000001111111111 -> 22
                    0b0000111111111111 -> 24
                    0b0011111111111111 -> 26
                    0b1111111111111111 -> 28
                    else -> -1
                }
                if (ea[evenOddBit] == 0L) {
                    pfn = TLB[i].PFN0
                    v = TLB[i].V0
                    d = TLB[i].D0
                } else {
                    pfn = TLB[i].PFN1
                    v = TLB[i].V1
                    d = TLB[i].D1
                }
                if (v == 0)
                    throw MipsHardwareException.TLBInvalid(LorS, core.pc, ea)
                if (d == 0 && LorS == AccessAction.STORE)
                    throw MipsHardwareException.TLBModified(core.pc, ea)

//                if (found) {
//                    throw GeneralException("[${core.pc.hex8}] Double MMU TLB match for ${ea.hex8}")
//                }

                val msb = mips.PABITS - 1 - 12
                val lsb = evenOddBit - 12
                val page = pfn[msb..lsb].asULong
                val offset = ea[evenOddBit - 1..0]

                pAddr = (page shl evenOddBit) or offset
                found = true
                break
            }
        }

        if (!found)
            throw MipsHardwareException.TLBMiss(LorS, core.pc, ea)

        return pAddr
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

    override fun command(): String = "mmu"

    override fun configure(parent: ArgumentParser?, useParent: Boolean): ArgumentParser? =
            super.configure(parent, useParent)?.apply {
                subparser("clear_mmu").apply { }
                subparser("add_memory_translator", help = "Translate memory").apply {
                    variable<String>("-v", "--startVaddress", required = true, help = "Segment start address")
                    variable<String>("-p", "--startPaddress", required = true, help = "Segment end address")
                    variable<String>("-s", "--size", required = true, help = "Segment end address")
                }
            }

    override fun process(context: IInteractive.Context): Boolean {
        if (super.process(context))
            return true

        when (context.command()) {
            "clear_mmu" -> {
                TLB.indices.forEach { index -> TLB[index] = TLBEntry() }
                invalidateCache()
                context.result = "MMU cleared"
                context.pop()
                return true
            }
            "add_memory_translator" -> {
                val startVaddressString: String = context["startVaddress"]
                val startPaddressString: String = context["startPaddress"]
                val sizeString: String = context["size"]
                val startVaddress = startVaddressString.toULong(16)
                val startPaddress = startPaddressString.toULong(16)
                val size = sizeString.toULong(16)
                if (startVaddress % 0x1000_0000L != 0L || size % 0x1000_0000L != 0L) {
                    context.result = "Address and size must be multiple 0x1000_0000"
                    context.pop()
                    return true
                }
                if (startVaddress != startPaddress) {
                    context.result = "Start physical must be equal virtual address"
                    context.pop()
                    return true
                }
                invalidateCache()
                val startIndex = (startVaddress / 0x1000_0000).toInt()
                val endIndex = ((startVaddress + size) / 0x1000_0000).toInt()
                (startIndex until endIndex).forEach { index ->
                    val entry = TLBEntry.createTlbEntry(index, index * 0x1000_0000L, index * 0x1000_0000L, 0x1000_0000L)
                    TLB[index] = entry
                }
                context.result = "Created memory translator " +
                        "${startVaddress.hex8} -> ${startPaddress.hex8} (size: ${size.hex8})"
                context.pop()
                return true
            }
        }

        return false
    }
}