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
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.mmu.ATLBEntry
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.mmu.TLBEntry32
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.mmu.TLBEntry64
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Stub for MIPS address translation.
 * Описание организации виртуальной памяти в MIPS64 - p.27 PRA (R6).
 *
 * @property TLB - ассоциативная память для сопоставления виртуальных адресов физическим
 * @param tlbEntries Количество записей TLB (или Maximum TLB index + 1); определяется из поля MMUSize регистра C0_Config1
 *
 * Диапазоны полей регистров EntryLo (в частности, PFN) меняются как функция от PABITS. с.149 MIPS64 PRA
 * => размер физического адреса вариативен.
 *
 * Stub for MIPS address translation
 */
class MipsMMU(parent: Module, name: String, widthOut: ULong, val tlbEntries: Int = 32) :
        AddressTranslator(parent, name, widthOut = widthOut) {
    val mips get() = parent as MipsCore

    // if PageMask is not implemented, we think that it is written with the encoding for 4kB page


    private val TLB : Array<ATLBEntry> = when (mips.cpu.mode) {
        MipsCPU.Mode.R32 -> Array(tlbEntries) { TLBEntry32() }
        MipsCPU.Mode.R64 -> Array(tlbEntries) { TLBEntry64() }
    }

    private var currentTlbIndex = 0

    fun getFreeTlbIndex(): Int {
        if (currentTlbIndex == tlbEntries) {
            currentTlbIndex = mips.cop.regs.Wired.value.int
        }
        return currentTlbIndex++
    }

    fun writeTlbEntry(index: Int, pageMask: ULong, entryHi: ULong, entryLo0: UInt, entryLo1: UInt): ATLBEntry {
        val entry = when (mips.cpu.mode) {
            MipsCPU.Mode.R32 -> TLBEntry32(index, pageMask, entryHi, entryLo0, entryLo1)
            MipsCPU.Mode.R64 -> TLBEntry64(
                index,
                pageMask,
                entryHi,
                entryLo0,
                entryLo1,
                VPN2border = (core as MipsCore).SEGBITS!!
            )
        }
        TLB[index] = entry
        return entry
    }

    fun writeTlbEntry(index: Int, vAddress: ULong, pAddress: ULong, size: Int): ATLBEntry {
        val entry = when (mips.cpu.mode) {
            MipsCPU.Mode.R32 -> TLBEntry32.createTlbEntry(index, vAddress, pAddress, size, mips.PABITS)
            MipsCPU.Mode.R64 -> TLBEntry64.createTlbEntry(index, vAddress, pAddress, size, mips.PABITS, mips.SEGBITS!!)
        }
        TLB[index] = entry
        return entry
    }

    fun readTlbEntry(index: Int): ATLBEntry = TLB[index]

    fun translate64(ea: ULong, ss: Int, size: Int, LorS: AccessAction): ULong {
        // unmapped kseg0 и kseg1
        if (
            ea in 0xFFFF_FFFF_8000_0000u..0xFFFF_FFFF_9FFF_FFFFu ||
            ea in 0xFFFF_FFFF_A000_0000u..0xFFFF_FFFF_BFFF_FFFFu
        )
            return ea and 0x0000_0000_1FFF_FFFFu

        val shiftedPabits = 1uL shl mips.PABITS
        if (
            ea in 0xB800_0000_0000_0000u until 0xB800_0000_0000_0000u + shiftedPabits ||
            ea in 0xB000_0000_0000_0000u until 0xB000_0000_0000_0000u + shiftedPabits ||
            ea in 0xA800_0000_0000_0000u until 0xA800_0000_0000_0000u + shiftedPabits ||
            ea in 0xA000_0000_0000_0000u until 0xA000_0000_0000_0000u + shiftedPabits ||
            ea in 0x9800_0000_0000_0000u until 0x9800_0000_0000_0000u + shiftedPabits ||
            ea in 0x9000_0000_0000_0000u until 0x9000_0000_0000_0000u + shiftedPabits ||
            ea in 0x8800_0000_0000_0000u until 0x8800_0000_0000_0000u + shiftedPabits ||
            ea in 0x8000_0000_0000_0000u until 0x8000_0000_0000_0000u + shiftedPabits
        )
            return ea and (0x0000_0000_0000_0000u + shiftedPabits - 1u)

        var pAddr = 0u
        var found = false
        val pfn: UInt
        val v: UInt
        val d: UInt

        val entryHiASID = mips.cop.regs.EntryHi.value[7..0].uint
        // p. 48 PRA.
        for (i in TLB.indices) {
            val invMask = inv(TLB[i].Mask)
            val vpn = TLB[i].VPN2 and invMask
            val eaPage = ea[31..13] and invMask
            val isGlobal = TLB[i].G == 1u

            val isIdEquals = TLB[i].ASID == entryHiASID.ulong_z
            if ((vpn == eaPage) && (isGlobal || isIdEquals)) {

                val evenOddBit = when (TLB[i].Mask) {
                    0b0000000000000000uL -> 12
                    0b0000000000000011uL -> 14
                    0b0000000000001111uL -> 16
                    0b0000000000111111uL -> 18
                    0b0000000011111111uL -> 20
                    0b0000001111111111uL -> 22
                    0b0000111111111111uL -> 24
                    0b0011111111111111uL -> 26
                    0b1111111111111111uL -> 28
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

    fun translate32(ea: ULong, ss: Int, size: Int,  LorS: AccessAction): ULong {
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
            val eaPage = ea[31..13].uint and invMask.uint
            val isGlobal = TLB[i].G == 1u
            val isIdEquals = TLB[i].ASID.uint == entryHiASID
            if ((vpn.uint == eaPage) && (isGlobal || isIdEquals)) {

                val evenOddBit = when (TLB[i].Mask.uint) {
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
    override fun translate(ea: ULong, ss: Int, size: Int,  LorS: AccessAction): ULong = when (mips.cpu.mode) {
        MipsCPU.Mode.R32 -> translate32(ea, ss, size, LorS)
        MipsCPU.Mode.R64 -> translate64(ea, ss, size, LorS)
    }

    fun invalidateCache() {

    }

    override fun reset() {
        invalidateCache()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf("name" to name, "TLB" to TLB.map { tlbEntry -> tlbEntry.serialize(ctxt) })
    }

    // TODO: make checked cast
    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        (snapshot["TLB"] as ArrayList<Map<String, String>>).forEachIndexed { i, map ->
            TLB[i] = ATLBEntry.createFromSnapshot(ctxt, map)
        }
    }
}