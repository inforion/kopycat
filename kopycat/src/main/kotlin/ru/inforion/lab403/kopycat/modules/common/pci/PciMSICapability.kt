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

import ru.inforion.lab403.common.extensions.truth
import ru.inforion.lab403.common.extensions.untruth
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.field

/**
 * "Возможность" PCI использовать MSI
 *
 * @param pci PCI устройство, к которому подключается capability
 * @param name название модуля
 * @param base адрес начала capability
 * @param next оффсет до следующей capability
 */
class PciMSICapability(pci: PciDevice, name: String, base: ULong, next: ULong) : PciCapability(pci, name, base, next) {
    override val size: ULong
        get() = 18u

    /** Capability ID */
    @Suppress("unused")
    private val CAP_MID = CapabilityIDClass("CAP_MID", 0x05)

    /** Message Control (MSI) */
    inner class MCClass(name: String) : ByteAccessRegister(pci.ports.pci, base + 2u, Datatype.WORD, name) {
        /** MSI Enable */
        var MSIE by bit(0)

        /** 64-bit Address Capable */
        var C64 by bit(7)

        /** Multiple Message Capable */
        var MMC by field(3..1)

        // /** Multiple Message Enable */
        // var MME by field(6..4)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            C64 = 1
            MMC = 0u
            return super.read(ea, ss, size)
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val oldMSIE = MSIE
            super.write(ea, ss, size, value)

            if (oldMSIE.untruth && MSIE.truth) {
                log.warning {
                    "${pci.name}: enabled MSI"
                }
            }
        }
    }

    /** Message Control (MSI) */
    val CAP_MC = MCClass("CAP_MC")

    /** Message Address (MSI) */
    val CAP_MA = ByteAccessRegister(pci.ports.pci, base + 4u, Datatype.QWORD, "CAP_MA")

    /** Message Data (MSI) */
    inner class MDClass(name: String) : Module.ByteAccessRegister(pci.ports.pci, base + 12u, Datatype.WORD, name) {
        var vector by field(7..0)
        // private var deliveryMode by field(10..8)
        // private var triggerLevel by bit(14)
        // private var triggerMode by bit(15)
    }

    /** Message Data (MSI) */
    val CAP_MD = MDClass("CAP_MD")
}
