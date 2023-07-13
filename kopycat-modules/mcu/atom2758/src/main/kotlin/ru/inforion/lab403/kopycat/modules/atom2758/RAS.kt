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
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.modules.common.pci.PciAbstract
import ru.inforion.lab403.kopycat.modules.common.pci.PciDevice
import java.util.logging.Level.CONFIG

/**
 * Intel Atom Processor C2000 Product Family for Microserver
 * 4.11 RAS Register Map, page 107
 */
class RAS(parent: Module, name: String) : PciDevice(parent, name, 0x8086, 0x1F14) {
    val BAR_10 = PCI_BAR(0x10, DWORD, "BAR_10")
    val BAR_14 = PCI_BAR(0x14, DWORD, "BAR_14")
    val BAR_18 = PCI_BAR(0x18, DWORD, "BAR_18")
    val BAR_1C = PCI_BAR(0x1C, DWORD, "BAR_1C")
    val BAR_20 = PCI_BAR(0x20, DWORD, "BAR_20")
    val BAR_24 = PCI_BAR(0x24, DWORD, "BAR_24")

    val REG_3C0 = PCI_CONF_FUNC_WR(0x3C0, DWORD, "REG_3C0", 0u, level = CONFIG)
    val REG_3D0 = PCI_CONF_FUNC_WR(0x3D0, DWORD, "REG_3D0", 0u, level = CONFIG)
    val REG_400 = PCI_CONF_FUNC_WR(0x400, DWORD, "REG_400", 0u, level = CONFIG)
    val RTF_BMBOUND = PCI_CONF_FUNC_WR(0x404, DWORD, "RTF_BMBOUND", 0u, level = CONFIG)
    val RTF_BMBOUNDHI = PCI_CONF_FUNC_WR(0x408, DWORD, "RTF_BMBOUNDHI", 0u, level = CONFIG)
    val RP_BIFCTL = PCI_CONF_FUNC_WR(0x40C, DWORD, "RP_BIFCTL", 0u, level = CONFIG)
    val REG_410 = PCI_CONF_FUNC_WR(0x410, DWORD, "REG_410", 0u, level = CONFIG)
    val REG_414 = PCI_CONF_FUNC_WR(0x414, DWORD, "REG_414", 0u, level = CONFIG)
}