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
package ru.inforion.lab403.kopycat.modules.common.pci

import ru.inforion.lab403.common.extensions.ushr

/**
 * {EN}
 * Formula to Calculate Memory Address Given the Bus, Device, Function, and Register Offset
 *
 * **Docs**: Accessing PCI Express Configuration Registers Using Intel Chipsets,
 * Conversion Formulas, p. 8
 */
data class PciAddress constructor(
    //val confReg: ULong = 0xE000_0000uL,
    val bus: ULong,
    val device: ULong,
    val func: ULong,
    val reg: ULong
) {
    companion object {
        fun fromBusFuncDeviceReg(value: ULong): PciAddress {
            val bus = value ushr 20
            val device = (value - (bus shl 20)) ushr 15
            val func = (value - (bus shl 20) - (device shl 15)) ushr 12
            val reg = value and 0x00000FFFuL
            return PciAddress(bus, device, func, reg)
        }
    }
}