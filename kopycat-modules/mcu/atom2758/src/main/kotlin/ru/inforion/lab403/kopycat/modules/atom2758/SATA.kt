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
package ru.inforion.lab403.kopycat.modules.atom2758

import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.modules.common.pci.PciDevice
import java.util.logging.Level.CONFIG

class SATA(parent: Module, name: String, val num: Int) :
    PciDevice(parent, name, 0x8086, 0x1F20 or num, classCode = 0x010601) {

//https://wiki.osdev.org/AHCI

    companion object {
        const val BUS_SIZE = 0x1100
        const val BUS_MEM_INDEX_2 = 15
        const val BUS_MEM_INDEX_3 = 16
    }

    val mem = ports.Slave("mem", BUS_SIZE)

    private val busIndex = when (num) {
        2 -> BUS_MEM_INDEX_2
        3 -> BUS_MEM_INDEX_3
        else -> error("Unknown SATA device = $num")
    }

    val BAR_10 = PCI_BAR(0x10, DWORD, "BAR_10")
    val BAR_14 = PCI_BAR(0x14, DWORD, "BAR_14")
    val BAR_18 = PCI_BAR(0x18, DWORD, "BAR_18")
    val BAR_1C = PCI_BAR(0x1C, DWORD, "BAR_1C")
    val BAR_20 = PCI_BAR(0x20, DWORD, "BAR_20")

    val ABAR = PCI_BAR(0x24, DWORD,"ABAR", BUS_SIZE, BRIDGE.MEMORY_AREA, busIndex, CONFIG)

    val PTIM = PCI_CONF_FUNC_WR(0x40, WORD, "PTIM", level = CONFIG)
    val STIM = PCI_CONF_FUNC_WR(0x42, WORD, "STIM", level = CONFIG)
    val D1TIM = PCI_CONF_FUNC_WR(0x44, BYTE, "D1TIM", level = CONFIG)

    val Synchronous_DMA_Control = PCI_CONF_FUNC_WR(0x48, BYTE, "Synchronous_DMA_Control", level = CONFIG)
    val Synchronous_DMA_Timing = PCI_CONF_FUNC_WR(0x4A, WORD, "Synchronous_DMA_Timing", level = CONFIG)

    val IIOC = PCI_CONF_FUNC_WR(0x54, DWORD, "IIOC", level = CONFIG)
    val PID = PCI_CONF_FUNC_WR(0x70, WORD, "PID", level = CONFIG)
    val PC = PCI_CONF_FUNC_WR(0x72, WORD, "PC", level = CONFIG)
    val PMCS = PCI_CONF_FUNC_WR(0x74, WORD, "PMCS", level = CONFIG)

    val MID = PCI_CONF_FUNC_WR(0x80, WORD, "MID", level = CONFIG)
    val MC = PCI_CONF_FUNC_WR(0x82, WORD, "MC", level = CONFIG)
    val MA = PCI_CONF_FUNC_WR(0x84, DWORD, "MA", level = CONFIG)
    val MD = PCI_CONF_FUNC_WR(0x88, WORD, "MD", level = CONFIG)
    val MAP = PCI_CONF_FUNC_WR(0x90, WORD, "MAP", level = CONFIG)
    val PCS = PCI_CONF_FUNC_WR(0x92, WORD, "PCS", level = CONFIG)

    val TM = PCI_CONF_FUNC_WR(0x94, DWORD, "TM", level = CONFIG)
    val TM2 = PCI_CONF_FUNC_WR(0x98, DWORD, "TM2", level = CONFIG)

    val SATAGC = PCI_CONF_FUNC_WR(0x9C, DWORD, "SATAGC", level = CONFIG)

    val SIRI = PCI_CONF_FUNC_WR(0xA0, BYTE, "SIRI", level = CONFIG)
    val SIRD = PCI_CONF_FUNC_WR(0xA4, DWORD, "SIRD", level = CONFIG)

    val SATACR0 = PCI_CONF_FUNC_WR(0xA8, DWORD, "SATACR0", level = CONFIG)
    val SATACR1 = PCI_CONF_FUNC_WR(0xAC, DWORD, "SATACR1", level = CONFIG)

    val FLRCID = PCI_CONF_FUNC_WR(0xB0, WORD, "FLRCID", level = CONFIG)
    val FLRCAP = PCI_CONF_FUNC_WR(0xB2, WORD, "FLRCAP", level = CONFIG)
    val FLRCTL = PCI_CONF_FUNC_WR(0xB4, WORD, "FLRCTL", level = CONFIG)

    val ATC = PCI_CONF_FUNC_WR(0xC0, DWORD, "ATC", level = CONFIG)
    val ATS = PCI_CONF_FUNC_WR(0xC4, DWORD, "ATS", level = CONFIG)

    val SP = PCI_CONF_FUNC_WR(0xD0, DWORD, "SP", level = CONFIG)
    val BFCS = PCI_CONF_FUNC_WR(0xE0, DWORD, "BFCS", level = CONFIG)
    val BFTD1 = PCI_CONF_FUNC_WR(0xE4, DWORD, "BFTD1", level = CONFIG)
    val BFTD2 = PCI_CONF_FUNC_WR(0xE8, DWORD, "BFTD2", level = CONFIG)
    val MFID = PCI_CONF_FUNC_WR(0xF8, DWORD, "MFID", level = CONFIG)

    val PCMDIDEBA = PCI_CONF_FUNC_WR(0xFC, DWORD, "PCMDIDEBA", level = CONFIG)
    val SCMDIDEBA = PCI_CONF_FUNC_WR(0x100, DWORD, "SCMDIDEBA", level = CONFIG)
    val PCTLIDEBA = PCI_CONF_FUNC_WR(0x104, DWORD, "PCTLIDEBA", level = CONFIG)
    val SCTLIDEBA = PCI_CONF_FUNC_WR(0x108, DWORD, "SCTLIDEBA", level = CONFIG)



    val GHC_CAP = ByteAccessRegister(mem, 0x00u, DWORD, "GHC_CAP")

    inner class GHC_CLASS : ByteAccessRegister(mem, 0x04u, DWORD, "GHC") {
        var AE by bit(31)
        var IE by bit(1)
        var HR by bit(0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            HR = 0  // set reset done
            return super.read(ea, ss, size)
        }
    }

    val GHC = GHC_CLASS()

    val REG_08 = ByteAccessRegister(mem, 0x08u, DWORD, "REG_08")

    val GHC_PI = object : ByteAccessRegister(mem, 0x0Cu, DWORD, "GHC_PI") {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            data = 1u  // configure only zero port
            return super.read(ea, ss, size)
        }
    }

    val AREA_10 = Void(mem, 0x10u, 0xC7u, "AREA_10")

    val SFM = Register(mem, 0xC8u, WORD, "SFM")

    val REG_D0 = Register(mem, 0xD0u, DWORD, "REG_D0")
    val REG_D4 = Register(mem, 0xD4u, DWORD, "REG_D4")
    val REG_D8 = Register(mem, 0xD8u, DWORD, "REG_D4")

    inner class HBA_PORT(val index: Int) {
        private fun address(offset: Int) = 0x100u + index.ulong_z * 0x80u + offset.ulong_z

        open inner class HBA_BYTES_REG(offset: Int, name: String, datatype: Datatype = DWORD) :
            ByteAccessRegister(mem, address(offset), datatype, name)

        open inner class HBA_UNIFIED_REG(offset: Int, name: String, datatype: Datatype = DWORD) :
            Register(mem, address(offset), datatype, name)

        inner class SIG_CLASS(offset: Int) : HBA_UNIFIED_REG(offset, "SIG") {
            override fun read(ea: ULong, ss: Int, size: Int): ULong {
                data = 0xEB140101u
                return super.read(ea, ss, size)
            }
        }

        inner class SSTS_CLASS(offset: Int) : HBA_UNIFIED_REG(offset, "SSTS") {
            override fun read(ea: ULong, ss: Int, size: Int): ULong {
                data = data clr 0
                return super.read(ea, ss, size)
            }
        }

        val CLB = HBA_BYTES_REG(0x00, "CLB", QWORD)
        val FB = HBA_BYTES_REG(0x08, "FB", QWORD)
        val IS = HBA_UNIFIED_REG(0x10, "IS")
        val IE = HBA_UNIFIED_REG(0x14, "IE")
        val CMD = HBA_UNIFIED_REG(0x18, "CMD")
        val RSV0 = HBA_UNIFIED_REG(0x1C, "RSV0")
        val TFD = HBA_UNIFIED_REG(0x20, "TFD")
        val SIG = SIG_CLASS(0x24)
        val SSTS = SSTS_CLASS(0x28)
        val SCTL = HBA_UNIFIED_REG(0x2C, "SCTL")
        val SERR = HBA_UNIFIED_REG(0x30, "SERR")
        val SACT = HBA_UNIFIED_REG(0x34, "SACT")
        val CI = HBA_UNIFIED_REG(0x38, "CI")
        val SNTF = HBA_UNIFIED_REG(0x3C, "SNTF")
        val FBS = HBA_UNIFIED_REG(0x40, "FBS")
        val RSV1 = Array(11) { HBA_UNIFIED_REG(0x44 + it * 4, "RSV1${it + 1}") }
        val VENDOR = Array(4) { HBA_UNIFIED_REG(0x70 + it * 4, "VENDOR${it}") }
    }

    val PORTS = Array(32) { HBA_PORT(it) }
}