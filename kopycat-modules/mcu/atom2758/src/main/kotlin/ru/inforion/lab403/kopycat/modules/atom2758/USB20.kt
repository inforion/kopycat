/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
@file:Suppress("unused", "SpellCheckingInspection")

package ru.inforion.lab403.kopycat.modules.atom2758

import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.modules.common.pci.PciDevice

class USB20(parent: Module, name: String) : PciDevice(parent, name, 0x8086, 0x1F2C) {
    companion object {
        const val BUS_SIZE = 0x100
        const val BUS_MEM_INDEX = 14
    }

    val mem = ports.Port("mem")

    // QWORD?
    val MBAR = PCI_BAR(0x10, DWORD, "MBAR", BUS_SIZE, BRIDGE.MEMORY_AREA, BUS_MEM_INDEX, CONFIG)

    val BAR_14 = PCI_BAR(0x14, DWORD, "BAR_14")
    val BAR_18 = PCI_BAR(0x18, DWORD, "BAR_18")
    val BAR_1C = PCI_BAR(0x1C, DWORD, "BAR_1C")
    val BAR_20 = PCI_BAR(0x20, DWORD, "BAR_20")
    val BAR_24 = PCI_BAR(0x24, DWORD, "BAR_24")

    val IHFCLK = PCI_CONF_FUNC_WR(0x44, DWORD, "IHFCLK", 0u, level = CONFIG)
    val IHFCLKC = PCI_CONF_FUNC_WR(0x48, DWORD, "IHFCLKC", 0u, level = CONFIG)

    val PM_CID_NEXT_CAP = PCI_CONF_FUNC_WR(0x50, DWORD, "PM_CID_NEXT_CAP", 0u, level = CONFIG)
    val PM_CS = PCI_CONF_FUNC_WR(0x54, DWORD, "PM_CS", 0u, level = CONFIG)
    val DP_CID_NEXT_BASE = PCI_CONF_FUNC_WR(0x58, DWORD, "DP_CID_NEXT_BASE", 0u, level = CONFIG)
    val SBRN_FLA_PWC = PCI_CONF_FUNC_WR(0x60, DWORD, "SBRN_FLA_PWC", 0x30u, level = CONFIG)
    val ULSEC = PCI_CONF_FUNC_WR(0x68, DWORD, "ULSEC", 0u, level = CONFIG)
    val ULSCS = PCI_CONF_FUNC_WR(0x6C, DWORD, "ULSCS", 0u, level = CONFIG)

    val OCMAP = PCI_CONF_FUNC_WR(0x74, DWORD, "OCMAP", 0u, level = CONFIG)

    val RMHWKCTL = PCI_CONF_FUNC_WR(0x7C, DWORD, "RMHWKCTL", 0u, level = CONFIG)

    val REG_80 = PCI_CONF_FUNC_WR(0x80, DWORD, "REG_80", 0u, level = CONFIG)

    val REG_8C = PCI_CONF_FUNC_WR(0x8C, DWORD, "REG_8C", 0u, level = CONFIG)

    val FLR_CID_NEXT_MISC = PCI_CONF_FUNC_WR(0x98, DWORD, "FLR_CID_NEXT_MISC", 0u, level = CONFIG)
    val FLR_CTL_STS_RSVD = PCI_CONF_FUNC_WR(0x9C, DWORD, "FLR_CTL_STS_RSVD", 0u, level = CONFIG)
    val MANID = PCI_CONF_FUNC_WR(0xF8, DWORD, "MANID", 0u, level = CONFIG)


    val CAP_HCIV = ByteAccessRegister(mem, 0x00u, DWORD, "CAP_HCIV", level = CONFIG)
    val HCSPARAMS = ByteAccessRegister(mem, 0x04u, DWORD, "HCSPARAMS", level = CONFIG)
    val HCCPARAMS = ByteAccessRegister(mem, 0x08u, DWORD, "HCCPARAMS", level = CONFIG)
    val CPRD = ByteAccessRegister(mem, 0x0Cu, DWORD, "CPRD", level = CONFIG)

    val USB2CMD = ByteAccessRegister(mem, 0x20u, DWORD, "USB2CMD", level = CONFIG)
    val USB2STS = ByteAccessRegister(mem, 0x24u, DWORD, "USB2STS", level = CONFIG)
    val USB2INTR = ByteAccessRegister(mem, 0x28u, DWORD, "USB2INTR", level = CONFIG)
    val FRINDEX = ByteAccessRegister(mem, 0x2Cu, DWORD, "FRINDEX", level = CONFIG)
    val CTRLDSSEGMENT = ByteAccessRegister(mem, 0x30u, DWORD, "CTRLDSSEGMENT", level = CONFIG)
    val PERIODICICLISTBASE = ByteAccessRegister(mem, 0x34u, DWORD, "PERIODICICLISTBASE", level = CONFIG)
    val ASYNCLISTADDR = ByteAccessRegister(mem, 0x38u, DWORD, "ASYNCLISTADDR", level = CONFIG)

    val CONFIGFLAG = ByteAccessRegister(mem, 0x60u, DWORD, "CONFIGFLAG", level = CONFIG)
    val PORTSC1 = ByteAccessRegister(mem, 0x64u, DWORD, "PORTSC1", level = CONFIG)
    val PORTSC2 = ByteAccessRegister(mem, 0x68u, DWORD, "PORTSC2", level = CONFIG)
    val PORTSC3 = ByteAccessRegister(mem, 0x6Cu, DWORD, "PORTSC3", level = CONFIG)
    val PORTSC4 = ByteAccessRegister(mem, 0x70u, DWORD, "PORTSC4", level = CONFIG)

    val DP_CTRLSTS = ByteAccessRegister(mem, 0xA0u, DWORD, "DP_CTRLSTS", level = CONFIG)
    val DP_USB_PIDs = ByteAccessRegister(mem, 0xA4u, DWORD, "DP_USB_PIDs", level = CONFIG)
    val DP_DATA_BUF_B = ByteAccessRegister(mem, 0xA8u, DWORD, "DP_DATA_BUF_B", level = CONFIG)

    val DP_CFG = ByteAccessRegister(mem, 0xB0u, DWORD, "DP_CFG", level = CONFIG)

    val REG_D0 = ByteAccessRegister(mem, 0xD0u, DWORD, "REG_D0", level = CONFIG)
    val REG_D4 = ByteAccessRegister(mem, 0xD4u, DWORD, "REG_D4", level = CONFIG)
    val REG_D8 = ByteAccessRegister(mem, 0xD8u, DWORD, "REG_D8", level = CONFIG)

    val RMHPORTSTS1 = ByteAccessRegister(mem, 0xF0u, DWORD, "RMHPORTSTS1", level = CONFIG)
}