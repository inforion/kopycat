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
import ru.inforion.lab403.common.optional.emptyOpt
import ru.inforion.lab403.common.optional.opt
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.AddressTranslator
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.exceptions.CrossPageAccessException
import ru.inforion.lab403.kopycat.cores.mips.Microarchitecture
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.mmu.TLBEntry
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.mmu.TLBEntry32
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.mmu.TLBEntry64
import ru.inforion.lab403.kopycat.cores.mips.hardware.registers.CPRBank
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.serializer.loadValue
import java.nio.ByteOrder

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

    private val TLB : Array<TLBEntry> = when (mips.cpu.mode) {
        MipsCPU.Mode.R32 -> Array(tlbEntries) { TLBEntry32.create(mips) }
        MipsCPU.Mode.R64 -> Array(tlbEntries) { TLBEntry64.create(mips) }
    }

    private var randomTlbIdx = mips.cop.regs.Wired.value.int

    fun getFreeTlbIndex(): Int {
        if (randomTlbIdx == 0 || randomTlbIdx == tlbEntries) {
            randomTlbIdx = mips.cop.regs.Wired.value[15..0].int
        }
        return randomTlbIdx++
    }

    fun writeTlbEntry(index: Int, pageMask: ULong, entryHi: ULong, entryLo0: UInt, entryLo1: UInt): TLBEntry {
        val entry = TLBEntry(
            index,
            pageMask,
            entryHi,
            entryLo0,
            entryLo1,
            mips.cop.regs.MMID.mmid,
            mips.cop.regs.Config4.ae,
            if (mips.is64bit) mips.segmask.opt else emptyOpt(),
        )
        TLB[index] = entry
        return entry
    }

    fun writeTlbEntry(index: Int, vAddress: ULong, pAddress: ULong, size: Int): TLBEntry {
        val entry = when (mips.cpu.mode) {
            MipsCPU.Mode.R32 -> TLBEntry32.create(
                mips,
                index,
                vAddress,
                pAddress,
                size,
            )
            MipsCPU.Mode.R64 -> TLBEntry64.create(
                mips,
                index,
                vAddress,
                pAddress,
                size,
            )
        }
        TLB[index] = entry
        return entry
    }

    fun readTlbEntry(index: Int): TLBEntry = TLB[index]

    private fun adex(ea: ULong, access: AccessAction) = when (access) {
        AccessAction.LOAD, AccessAction.FETCH -> MipsHardwareException.AdEL(mips.pc, ea)
        AccessAction.STORE -> MipsHardwareException.AdES(mips.pc, ea)
    }

    private fun isAccessModeMapped(ea: ULong, access: AccessAction, am: ULong, eu: Boolean) = if (mips.cop.regs.Status.ERL) {
        if (eu) {
            false
        } else {
            // Kernel mode
            ((0x7000_0000 shl am.int) < 0)
        }
    } else if (mips.cop.regs.Status.EXL /* || mips.cop.regs.Status.ERL */) {
        // Effective KSU = 0; kernel mode
        ((0x7000_0000 shl am.int) < 0)
    } else {
        when (CPRBank.ProcessorMode.fromKSU(mips.cop.regs.Status)) {
            CPRBank.ProcessorMode.Kernel -> ((0x7000_0000 shl am.int) < 0)
            CPRBank.ProcessorMode.Supervisor -> {
                val mask = 0xc038_0000.int
                if ((mask shl am.int) < 0) {
                    throw adex(ea, access)
                }
                (((mask shl 8) shl am.int) < 0)
            }
            else -> {
                val mask = 0xe418_0000.int
                if ((mask shl am.int) < 0) {
                    throw adex(ea, access)
                }
                (((mask shl 8) shl am.int) < 0)
            }
        }
    }

    private fun getSegPhysicalAddress(
        ea: ULong,
        size: Int,
        access: AccessAction,
        am: ULong,
        eu: Boolean,
        segmask: ULong,
        physicalBase: ULong,
    ) = if (isAccessModeMapped(ea, access, am, eu)) {
        tlbFindAddress(ea, size, access)
    } else {
        val boundary = segmask + 1uL
        if (ea / boundary != (ea + size - 1u) / boundary) {
            throw CrossPageAccessException(
                core.pc,
                ea,
                segmask.inv(),
                order = if (mips.cpu.bigEndianCPU.truth) {
                    ByteOrder.BIG_ENDIAN
                } else {
                    ByteOrder.LITTLE_ENDIAN
                },
            )
        }
        physicalBase or (ea and segmask)
    }

    private fun getSegctlPhysicalAddress(
        ea: ULong,
        size: Int,
        access: AccessAction,
        segctl: CPRBank.SEGCTL_CFG,
        segmask: ULong,
    ) = getSegPhysicalAddress(
        ea,
        size,
        access,
        segctl.AM,
        segctl.EN.truth,
        segmask,
        (segctl.PA shl 29) and segmask.inv(),
    )

    fun tlbFindForAddress(ea: ULong) = TLB.find { it.isValidForAddress(mips, ea) }

    // R4K
    private fun tlbFindAddress(ea: ULong, size: Int, access: AccessAction): ULong {
        val tlb = tlbFindForAddress(ea) ?: throw MipsHardwareException.TLBMiss(access, mips.pc, ea)
        val mask = tlb.pageMask or ubitMask64(13)

        val highestMaskBit = 63 - mask.countLeadingZeroBits()
        val n = ea[highestMaskBit].truth

        if (size > 1) {
            val end = ea + size - 1uL
            val nEnd = end[highestMaskBit].truth

            if (!tlb.isValidForAddress(mips, end) || n != nEnd) {
                throw CrossPageAccessException(
                    core.pc,
                    ea,
                    (mask clr highestMaskBit).inv(),
                    order = if (mips.cpu.bigEndianCPU.truth) {
                        ByteOrder.BIG_ENDIAN
                    } else {
                        ByteOrder.LITTLE_ENDIAN
                    },
                )
            }
        }

        if ((!n || tlb.V1.untruth) && (n || tlb.V0.untruth)) {
            throw MipsHardwareException.TLBInvalid(access, mips.pc, ea)
        }

        if (access == AccessAction.FETCH && (n && tlb.XI1.truth || !n && tlb.XI0.truth)) {
            if (mips.cop.regs.PageGrain.IEC) {
                throw MipsHardwareException.TLBXI(mips.pc, ea)
            } else {
                throw MipsHardwareException.TLBInvalid(access, mips.pc, ea)
            }
        }

        if (access == AccessAction.LOAD && (n && tlb.RI1.truth || !n && tlb.RI0.truth)) {
            if (mips.cop.regs.PageGrain.IEC) {
                throw MipsHardwareException.TLBRI(mips.pc, ea)
            } else {
                throw MipsHardwareException.TLBInvalid(access, mips.pc, ea)
            }
        }

        return if (access != AccessAction.STORE || (n && tlb.D1.truth || !n && tlb.D0.truth)) {
            if (n) {
                tlb.PFN1
            } else {
                tlb.PFN0
            } or (ea and (mask ushr 1))
        } else {
            throw MipsHardwareException.TLBModified(mips.pc, ea)
        }
    }

    private fun ULong.signextIf64Bit() = if (mips.is64bit) {
        this signext 31
    } else {
        this
    }

    private val kseg1 = 0xA000_0000uL.signextIf64Bit()
    private val kseg2 = 0xC000_0000uL.signextIf64Bit()
    private val kseg3 = 0xE000_0000uL.signextIf64Bit()

    override fun translate(ea: ULong, ss: Int, size: Int, LorS: AccessAction): ULong {
        return if (ea <= 0x7FFF_FFFFuL) {
            // useg
            getSegctlPhysicalAddress(
                ea,
                size,
                LorS,
                if (ea >= 0x4000_0000uL) {
                    mips.cop.regs.SegCtl2.cfgLo
                } else {
                    mips.cop.regs.SegCtl2.cfgHi
                },
                0x3FFF_FFFFuL,
            )
        } else if (mips.is64bit && ea < 0x4000_0000_0000_0000uL) {
            // xuseg
            if (mips.cop.regs.Status.UX && ea <= 0x3FFF_FFFF_FFFF_FFFFuL and mips.segmask) {
                tlbFindAddress(ea, size, LorS)
            } else {
                throw adex(ea, LorS)
            }
        } else if (mips.is64bit && ea < 0x8000_0000_0000_0000uL) {
            // xsseg
            // TODO: check permissions
            if (mips.cop.regs.Status.SX && ea <= 0x7FFF_FFFF_FFFF_FFFFuL and mips.segmask) {
                tlbFindAddress(ea, size, LorS)
            } else {
                throw adex(ea, LorS)
            }
        } else if (mips.is64bit && ea < 0xC000_0000_0000_0000uL) {
            // xkphys

            val pamask = when {
                mips.cop.regs.Config3.LPA && mips.cop.regs.PageGrain.ELPA -> ubitMask64(mips.PABITS - 1 .. 0)
                mips.is64bit -> ubitMask64(35..0)
                else -> ubitMask64(31..0)
            }

            if (ea and 0x07FF_FFFF_FFFF_FFFFuL <= pamask) {
                val am = if (mips.cop.regs.SegCtl2.XR[ea[61..59].int] != 0uL) {
                    mips.cop.regs.SegCtl1.XAM
                } else {
                    0uL
                }

                val permitted = when (am) {
                    0uL, 1uL, 6uL -> mips.cop.regs.Status.KX
                    2uL, 5uL -> mips.cop.regs.Status.SX
                    else -> mips.cop.regs.Status.UX
                }

                if (permitted) {
                    getSegPhysicalAddress(ea, size, LorS, am, false, pamask, 0uL)
                } else {
                    throw adex(ea, LorS)
                }
            } else {
                throw adex(ea, LorS)
            }
        } else if (mips.is64bit && ea < 0xFFFF_FFFF_8000_0000uL) {
            // xkseg
            // TODO: check permissions
            if (mips.cop.regs.Status.KX && ea <= 0xFFFF_FFFF_7FFF_FFFFuL and mips.segmask) {
                tlbFindAddress(ea, size, LorS)
            } else {
                throw adex(ea, LorS)
            }
        } else if (ea < kseg1) {
            // kseg0
            getSegctlPhysicalAddress(ea, size, LorS, mips.cop.regs.SegCtl1.cfgHi, 0x1FFF_FFFFuL)
        } else if (ea < kseg2) {
            // kseg1
            getSegctlPhysicalAddress(ea, size, LorS, mips.cop.regs.SegCtl1.cfgLo, 0x1FFF_FFFFuL)
        } else if (ea < kseg3) {
            // kseg2
            getSegctlPhysicalAddress(ea, size, LorS, mips.cop.regs.SegCtl0.cfgHi, 0x1FFF_FFFFuL)
        } else {
            // kseg3
            if (mips.microarchitecture == Microarchitecture.cnMips &&
                ea in 0xFFFF_FFFF_FFFF_8000uL..0xFFFF_FFFF_FFFF_BFFFuL) {
                // CVMSEG

                val enabled = if (mips.cop.regs.Status.EXL || mips.cop.regs.Status.ERL) {
                    // Effective KSU = 0; kernel mode
                    mips.cop.regs.CvmMemCtl?.CVMSEGENAK
                } else {
                    when (CPRBank.ProcessorMode.fromKSU(mips.cop.regs.Status)) {
                        CPRBank.ProcessorMode.Kernel -> mips.cop.regs.CvmMemCtl?.CVMSEGENAK
                        CPRBank.ProcessorMode.Supervisor -> mips.cop.regs.CvmMemCtl?.CVMSEGENAS
                        CPRBank.ProcessorMode.User -> mips.cop.regs.CvmMemCtl?.CVMSEGENAU
                        else -> false
                    }
                }

                if (enabled == true) {
                    return ea
                }
            }
            getSegctlPhysicalAddress(ea, size, LorS, mips.cop.regs.SegCtl0.cfgLo, 0x1FFF_FFFFuL)
        }
    }

    fun invalidateCache() {

    }

    override fun reset() {
        invalidateCache()
    }

    override fun serialize(ctxt: GenericSerializer) = mapOf(
        "TLB" to TLB.map { it.serialize(ctxt) },
        "randomTlbIdx" to randomTlbIdx,
    )

    // TODO: make checked cast
    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        randomTlbIdx = loadValue(snapshot, "randomTlbIdx") { 0 }

        val snapshotTlbs = snapshot["TLB"] as ArrayList<Map<String, Any>>
        TLB.indices.forEach { i ->
            val tlb = snapshotTlbs[i]
            TLB[i] = TLBEntry(
                loadValue<Int>(tlb, "index"),
                loadValue<ULong>(tlb, "pageMask"),
                loadValue<ULong>(tlb, "entryHi"),
                loadValue<UInt>(tlb, "entryLo0"),
                loadValue<UInt>(tlb, "entryLo1"),
                loadValue<ULong>(tlb, "mmid"),
                loadValue<Boolean>(tlb, "ae"),
                if (mips.is64bit) mips.segmask.opt else emptyOpt(),
            )
        }
    }
}
