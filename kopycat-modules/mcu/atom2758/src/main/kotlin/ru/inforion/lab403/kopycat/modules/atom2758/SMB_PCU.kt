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

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.modules.PCI_IO_AREA
import ru.inforion.lab403.kopycat.modules.PCI_MEM_AREA
import ru.inforion.lab403.kopycat.modules.common.pci.PciAbstract
import ru.inforion.lab403.kopycat.modules.common.pci.PciDevice
import java.util.logging.Level.CONFIG

class SMB_PCU(parent: Module, name: String) : PciDevice(parent, name, 0x8086, 0x1F3C) {
    companion object {
        const val BUS_SIZE = 32

        const val BUS_MEM_INDEX = 10
        const val BUS_IO_INDEX = 11
    }

    val mem = ports.Slave("mem", BUS_SIZE)
    val io = ports.Slave("io", BUS_SIZE)

    val SMB_Config_MBARL = PCI_BAR(0x10, DWORD, "SMB_Config_MBARL", 0x20, PCI_MEM_AREA, BUS_MEM_INDEX, CONFIG)
    val SMB_Config_MBARH = PCI_BAR(0x14, DWORD, "SMB_Config_MBARH", level = CONFIG)

    val SMB_Config_IOBAR = PCI_BAR(0x20, DWORD, "SMB_Config_IOBAR", 0x20, PCI_IO_AREA, BUS_IO_INDEX, CONFIG)

    val BAR_18 = PCI_BAR(0x18, DWORD, "BAR_18")
    val BAR_1C = PCI_BAR(0x1C, DWORD, "BAR_1C")
    val BAR_24 = PCI_BAR(0x24, DWORD, "BAR_24")

    val SMB_Config_HCFG = object : PCI_CONF_FUNC_WR(0x40, DWORD, "SMB_Config_HCFG", level = CONFIG) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong = 0u
    }

    private val SMB_Mem_HSTS = Register(mem, 0x00u, WORD, "SMB_Mem_HSTS")
    private val SMB_Mem_HCTL = Register(mem, 0x02u, BYTE, "SMB_Mem_HCTL")
    private val SMB_Mem_HCMD = Register(mem, 0x03u, BYTE, "SMB_Mem_HCMD")
    private val SMB_Mem_TSA = Register(mem, 0x04u, BYTE, "SMB_Mem_TSA")
    private val SMB_Mem_HD0 = Register(mem, 0x05u, BYTE, "SMB_Mem_HD0")
    private val SMB_Mem_HD1 = Register(mem, 0x06u, BYTE, "SMB_Mem_HD1")
    private val SMB_Mem_HBD = Register(mem, 0x07u, BYTE, "SMB_Mem_HBD")
    private val SMB_Mem_PEC = Register(mem, 0x08u, BYTE, "SMB_Mem_PEC")
    private val SMB_Mem_SADDR = Register(mem, 0x09u, BYTE, "SMB_Mem_SADDR")
    private val SMB_Mem_AUXS = Register(mem, 0x0Cu, BYTE, "SMB_Mem_AUXS")
    private val SMB_Mem_AUXC = Register(mem, 0x0Du, BYTE, "SMB_Mem_AUXC")
    private val SMB_Mem_SMLC = Register(mem, 0x0Eu, BYTE, "SMB_Mem_SMLC")
    private val SMB_Mem_SMBC = Register(mem, 0x0Fu, BYTE, "SMB_Mem_SMBC")
    private val SMB_Mem_SSTS = Register(mem, 0x10u, BYTE, "SMB_Mem_SSTS")
    private val SMB_Mem_SCMD = Register(mem, 0x11u, BYTE, "SMB_Mem_SCMD")
    private val SMB_Mem_NDA = Register(mem, 0x14u, BYTE, "SMB_Mem_NDA")
    private val SMB_Mem_NDLB = Register(mem, 0x16u, BYTE, "SMB_Mem_NDLB")
    private val SMB_Mem_NDHB = Register(mem, 0x17u, BYTE, "SMB_Mem_NDHB")

    private val SMB_Mem_HSTS_io = Register(io, 0x00u, WORD, "SMB_Mem_HSTS_io")
    private val SMB_Mem_HCTL_io = Register(io, 0x02u, BYTE, "SMB_Mem_HCTL_io")
    private val SMB_Mem_HCMD_io = Register(io, 0x03u, BYTE, "SMB_Mem_HCMD_io")
    private val SMB_Mem_TSA_io = Register(io, 0x04u, BYTE, "SMB_Mem_TSA_io")
    private val SMB_Mem_HD0_io = Register(io, 0x05u, BYTE, "SMB_Mem_HD0_io")
    private val SMB_Mem_HD1_io = Register(io, 0x06u, BYTE, "SMB_Mem_HD1_io")
    private val SMB_Mem_HBD_io = Register(io, 0x07u, BYTE, "SMB_Mem_HBD_io")
    private val SMB_Mem_PEC_io = Register(io, 0x08u, BYTE, "SMB_Mem_PEC_io")
    private val SMB_Mem_SADDR_io = Register(io, 0x09u, BYTE, "SMB_Mem_SADDR_io")
    private val SMB_Mem_AUXS_io = Register(io, 0x0Cu, BYTE, "SMB_Mem_AUXS_io")
    private val SMB_Mem_AUXC_io = Register(io, 0x0Du, BYTE, "SMB_Mem_AUXC_io")
    private val SMB_Mem_SMLC_io = Register(io, 0x0Eu, BYTE, "SMB_Mem_SMLC_io")
    private val SMB_Mem_SMBC_io = Register(io, 0x0Fu, BYTE, "SMB_Mem_SMBC_io")
    private val SMB_Mem_SSTS_io = Register(io, 0x10u, BYTE, "SMB_Mem_SSTS_io")
    private val SMB_Mem_SCMD_io = Register(io, 0x11u, BYTE, "SMB_Mem_SCMD_io")
    private val SMB_Mem_NDA_io = Register(io, 0x14u, BYTE, "SMB_Mem_NDA_io")
    private val SMB_Mem_NDLB_io = Register(io, 0x16u, BYTE, "SMB_Mem_NDLB_io")
    private val SMB_Mem_NDHB_io = Register(io, 0x17u, BYTE, "SMB_Mem_NDHB_io")
}