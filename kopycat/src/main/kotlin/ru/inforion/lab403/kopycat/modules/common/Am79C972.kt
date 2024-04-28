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
package ru.inforion.lab403.kopycat.modules.common

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.modules.PCI_IO_AREA
import ru.inforion.lab403.kopycat.modules.PCI_MEM_AREA
import ru.inforion.lab403.kopycat.modules.common.pci.PciDevice

@Suppress("unused", "PropertyName", "ClassName")
class Am79C972(parent: Module, name: String) : PciDevice(
    parent,
    name,
    0x1022,
    0x2000,
    0x30,
    0x200000,  // ethernet controller
    0,  // disabled
    0x0000,
    0x0000,
    0,
    0,
    1,
    0x06,
    0xFF,
) {
    val CSRIO_BAR = PCI_BAR(0x10, DWORD, "CSRIO_BAR", 0x20, PCI_IO_AREA)
    val CSRMAP_BAR = PCI_BAR(0x14, DWORD, "CSRMAP_BAR", 0x1000, PCI_MEM_AREA)

    override fun reset() {
        super.reset()

        // Required status
        COMMAND_STATUS.STATUS = 0x0290u
    }
}