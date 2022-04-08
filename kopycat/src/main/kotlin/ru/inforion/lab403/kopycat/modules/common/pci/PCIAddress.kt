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
package ru.inforion.lab403.kopycat.modules.common.pci

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.modules.*

data class PciAddress constructor(
    val bus: Int,
    val device: Int,
    val func: Int,
    val reg: Int = 0,
    val enabled: Boolean = false
) {
    companion object {
        fun fromBusFuncDeviceReg(value: ULong) = PciAddress(
            value[PCI_BDF_BUS_RANGE].int,
            value[PCI_BDF_DEVICE_RANGE].int,
            value[PCI_BDF_FUNC_RANGE].int,
            value[PCI_BDF_REG_RANGE].int, // and 0xFC TODO: may be last two bits of reg should be 0 from I/O port
            value[PCI_BDF_ENA_BIT].truth
        )

        fun fromOffset(offset: ULong) = fromBusFuncDeviceReg(offset ushr 4)
     }

    val bdf get() = insert(bus, PCI_BDF_BUS_RANGE)
        .insert(device, PCI_BDF_DEVICE_RANGE)
        .insert(func, PCI_BDF_FUNC_RANGE)
        .ulong_z

    /**
     * Prefix to place bus
     */
    val offset get() = (bdf shl 4) + reg

    override fun toString() = "PCI[bus=$bus device=$device func=$func reg=0x${reg.hex}]"
}