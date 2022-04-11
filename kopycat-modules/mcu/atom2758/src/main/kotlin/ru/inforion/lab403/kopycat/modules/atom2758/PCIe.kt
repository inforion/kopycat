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
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.modules.common.pci.PciDevice
import java.util.logging.Level.CONFIG

class PCIe(parent: Module, name: String, deviceId: Int) : PciDevice(parent, name, 0x8086, deviceId) {
    val BAR_10 = PCI_BAR(0x10, DWORD, "BAR_10")
    val BAR_14 = PCI_BAR(0x14, DWORD, "BAR_14")
    val BAR_18 = PCI_BAR(0x18, DWORD, "BAR_18")
    val BAR_1C = PCI_BAR(0x1C, DWORD, "BAR_1C")
    val BAR_20 = PCI_BAR(0x20, DWORD, "BAR_20")
    val BAR_24 = PCI_BAR(0x24, DWORD, "BAR_24")

    val CLIST_XCAP = PCI_CONF_FUNC_WR(0x40, DWORD, "CLIST_XCAP", 0x00428010u, level = CONFIG)
    val DCAP = PCI_CONF_FUNC_WR(0x44, DWORD, "DCAP", 0x00008000u, level = CONFIG)
    val DCTL_DSTS = PCI_CONF_FUNC_WR(0x48, DWORD, "DCTL_DSTS", 0x00100000u, level = CONFIG)
    val LCAP = PCI_CONF_FUNC_WR(0x4C, DWORD, "LCAP", 0x00310C02u, level = CONFIG)
    val LCTL_LSTS = PCI_CONF_FUNC_WR(0x50, DWORD, "LCTL_LSTS", 0x00010000u, level = CONFIG)
    val SLCAP = PCI_CONF_FUNC_WR(0x54, DWORD, "SLCAP", 0x00040060u, level = CONFIG)
    val SLCTL_SLSTS = PCI_CONF_FUNC_WR(0x58, DWORD, "SLCTL_SLSTS", 0x00000000u, level = CONFIG)
    val RCTL = PCI_CONF_FUNC_WR(0x5C, DWORD, "RCTL", 0x00000000u, level = CONFIG)
    val RSTS = PCI_CONF_FUNC_WR(0x60, DWORD, "RSTS", 0x00000000u, level = CONFIG)
    val DCAP2 = PCI_CONF_FUNC_WR(0x64, DWORD, "DCAP2", 0x00080817u, level = CONFIG)
    val DCTL2_DSTS2 = PCI_CONF_FUNC_WR(0x68, DWORD, "DCTL2_DSTS2", 0x00000000u, level = CONFIG)
    val LCAP2 = PCI_CONF_FUNC_WR(0x6C, DWORD, "LCAP2", 0x00000000u, level = CONFIG)
    val LCTL2_LSTS2 = PCI_CONF_FUNC_WR(0x70, DWORD, "LCTL2_LSTS2", 0x00000000u, level = CONFIG)
    val SLCAP2 = PCI_CONF_FUNC_WR(0x74, DWORD, "SLCAP2", 0x00000000u, level = CONFIG)
    val SLCTL2_SLSTS2 = PCI_CONF_FUNC_WR(0x78, DWORD, "SLCTL2_SLSTS2", 0x00000000u, level = CONFIG)
    val MID_MC = PCI_CONF_FUNC_WR(0x80, DWORD, "MID_MC", 0x00009005u, level = CONFIG)
    val MA = PCI_CONF_FUNC_WR(0x84, DWORD, "MA", 0x00000000u, level = CONFIG)
    val MD = PCI_CONF_FUNC_WR(0x88, DWORD, "MD", 0x00000000u, level = CONFIG)

    val SSVID = PCI_CONF_FUNC_WR(0x8C, DWORD, "SSVID", 0x00000000u, level = CONFIG)

    // Maybe wrong
    val SVCAP = PCI_CONF_FUNC_WR(0x90, DWORD, "SVCAP", 0x0000A00Du, level = CONFIG)
    val SVID = PCI_CONF_FUNC_WR(0x94, DWORD, "SVID", 0x00000000u, level = CONFIG)

    val PMCAP_PMC = PCI_CONF_FUNC_WR(0xA0, DWORD, "PMCAP_PMC", 0xC8030001u, level = CONFIG)
    val PMCS = PCI_CONF_FUNC_WR(0xA4, DWORD, "PMCS", 0x00000008u, level = CONFIG)
    val REG_D8 = PCI_CONF_FUNC_WR(0xD8, DWORD, "REG_D8", 0u, CONFIG)
    val REG_EA = PCI_CONF_FUNC_WR(0xEA, DWORD, "REG_EA", 0u, CONFIG)
    val MANID = PCI_CONF_FUNC_WR(0xF8, DWORD, "MANID", 0x00000F00u, CONFIG)

    val MEM_800 = Memory(ports.pci, 0x200u, 0xC00u, "MEM_800", ACCESS.R_W)

//    val REG_8F8 = PCI_CONF_FUNC_WR(0x8F8, DWORD, "REG_8F8", 0u, CONFIG)
//    val REG_9A4 = PCI_CONF_FUNC_WR(0x9A4, DWORD, "REG_9A4", 0u, CONFIG)
//    val REG_A30 = PCI_CONF_FUNC_WR(0xA30, DWORD, "REG_A30", 0u, CONFIG)
//    val REG_B78 = PCI_CONF_FUNC_WR(0xB78, DWORD, "REG_B78", 0u, CONFIG)
//    val REG_B78 = PCI_CONF_FUNC_WR(0xB80, DWORD, "REG_B78", 0u, CONFIG)
}