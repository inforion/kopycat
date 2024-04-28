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
package ru.inforion.lab403.kopycat.modules.common.piix4


import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.common.pci.PciDevice

class PIIX4_82371AB_PCI(
    parent: Module,
    name: String,
    pciAIrq: Int = IRQ_ROUTING_DISABLE,
    pciBIrq: Int = IRQ_ROUTING_DISABLE,
    pciCIrq: Int = IRQ_ROUTING_DISABLE,
    pciDIrq: Int = IRQ_ROUTING_DISABLE,
) : PciDevice(parent, name, 0x8086, 0x7110) {
    companion object {
        const val IRQ_ROUTING_DISABLE = 0x80
    }

    init {
        // PCI IRQ mapping
        PCI_CONF_FUNC_WR(
            0x60,
            Datatype.DWORD,
            "PIRQRC",
            default = pciAIrq.ulong_z or
                    (pciBIrq.ulong_z shl 8) or
                    (pciCIrq.ulong_z shl 16) or
                    (pciDIrq.ulong_z shl 24),
        )
    }
}
