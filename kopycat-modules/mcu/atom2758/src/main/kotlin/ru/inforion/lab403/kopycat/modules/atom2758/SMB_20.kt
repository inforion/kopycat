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
package ru.inforion.lab403.kopycat.modules.atom2758

import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.QWORD
import ru.inforion.lab403.kopycat.modules.common.pci.PciDevice

class SMB_20(parent: Module, name: String, val func: Int) :
    PciDevice(parent, name, 0x8086, 0x1F15) {

    companion object {
        const val BUS_SIZE = 0x400

        const val BUS_MEM_INDEX_FUNC0 = 12
        const val BUS_MEM_INDEX_FUNC1 = 13
    }

    val mem = ports.Port("mem")

    private val busIndex = when (func) {
        0 -> BUS_MEM_INDEX_FUNC0
        1 -> BUS_MEM_INDEX_FUNC1
        else -> error("Unsupported SMB_20 function=$func")
    }

    // Registers in PCI Configuration Space

    // QWORD?
    val SMTBAR0 = PCI_BAR(0x10, DWORD, "SMTBAR0", BUS_SIZE, BRIDGE.MEMORY_AREA, busIndex, CONFIG)

    val BAR_14 = PCI_BAR(0x14, DWORD, "BAR_14")
    val BAR_18 = PCI_BAR(0x18, DWORD, "BAR_18")
    val BAR_1C = PCI_BAR(0x1C, DWORD, "BAR_1C")
    val BAR_20 = PCI_BAR(0x20, DWORD, "BAR_20")
    val BAR_24 = PCI_BAR(0x24, DWORD, "BAR_24")

    val REG_EA = PCI_CONF_FUNC_WR(0xEA, DWORD, "REG_EA", level = CONFIG)

    val AERCAPHDR = PCI_CONF_FUNC_WR(0x100, DWORD, "AERCAPHDR", level = CONFIG)
    val ERRUNCSTS = PCI_CONF_FUNC_WR(0x104, DWORD, "ERRUNCSTS", level = CONFIG)
    val ERRUNCMSK = PCI_CONF_FUNC_WR(0x108, DWORD, "ERRUNCMSK", level = CONFIG)
    val ERRUNCSEV = PCI_CONF_FUNC_WR(0x10C, DWORD, "ERRUNCSEV", level = CONFIG)
    val ERRCORSTS = PCI_CONF_FUNC_WR(0x110, DWORD, "ERRCORSTS", level = CONFIG)
    val ERRCORMSK = PCI_CONF_FUNC_WR(0x114, DWORD, "ERRCORMSK", level = CONFIG)
    val AERCAPCTL = PCI_CONF_FUNC_WR(0x118, DWORD, "AERCAPCTL", level = CONFIG)

    // Registers in Memory Space

    val GCTL = Register(mem, 0x000u, DWORD, "GCTL")

    val MDBA = ByteAccessRegister(mem, 0x100u, QWORD, "MDBA")

    val MCTRL = Register(mem, 0x108u, DWORD, "MCTRL")
    val MSTS = Register(mem, 0x10Cu, DWORD, "MSTS")
    val MDS = Register(mem, 0x110u, DWORD, "MDS")
    val RPOLICY = Register(mem, 0x114u, DWORD, "RPOLICY")
}