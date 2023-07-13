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

import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.field

/**
 * "Возможность" PCI Express
 *
 * @param pci PCI устройство, к которому подключается capability
 * @param name название модуля
 * @param base адрес начала capability
 * @param next оффсет до следующей capability
 */
class PcieCapability(pci: PciDevice, name: String, base: ULong, next: ULong) : PciCapability(pci, name, base, next) {
    override val size: ULong
        get() = 20u

    /** Capability ID */
    @Suppress("unused")
    private val CAP_MID = CapabilityIDClass("CAP_MID", 0x10)

    /** PCI Express Capabilities Register */
    @Suppress("unused")
    private val CAP_VER = object : ByteAccessRegister(pci.ports.pci, base + 2u, Datatype.WORD, "CAP_VER") {
        var version by field(2..0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            version = 1u
            return super.read(ea, ss, size)
        }
    }

    /** Device Capabilities */
    inner class DCClass(name: String) : ByteAccessRegister(pci.ports.pci, base + 4u, Datatype.DWORD, name) {
        /** Role-Based error reporting supported */
        var ROLE_BASED_ERR_REPORT by bit(15)

        override fun reset() {
            super.reset()
            ROLE_BASED_ERR_REPORT = 1
        }
    }

    /** Device Capabilities */
    @Suppress("unused")
    val CAP_DC = DCClass("CAP_DC")

    /** Device Control and Status */
    @Suppress("unused")
    val CAP_DCS = ByteAccessRegister(pci.ports.pci, base + 8u, Datatype.DWORD, name)

    /** Link Capabilities */
    inner class LCClass(name: String) : ByteAccessRegister(pci.ports.pci, base + 12u, Datatype.DWORD, name) {
        /** Maximum Link Speed: 1 = 2.5 GT/s, 2 = 5.0 GT/s, 3 = 8.0 GT/s */
        var MAX_LINK_SPEED by field(3..0)

        /** Maximum Link Width */
        var MAX_LINK_WIDTH by field(9..4)

        /** ASPM Support for L0S state */
        var ASPM_L0S by bit(10)

        override fun reset() {
            super.reset()
            MAX_LINK_SPEED = 1u
            MAX_LINK_WIDTH = 1u
            ASPM_L0S = 1
        }
    }

    /** Link Capabilities */
    @Suppress("unused")
    val CAP_LC = LCClass("CAP_LC")

    /** Link Control and Status Register */
    inner class LCSClass(name: String) : ByteAccessRegister(pci.ports.pci, base + 16u, Datatype.DWORD, name) {
        var LINK_SPEED by field(19..16)
        var LINK_WIDTH by field(25..20)

        override fun reset() {
            super.reset()
            LINK_SPEED = 1u
            LINK_WIDTH = 1u
        }
    }

    /** Link Control and Status Register */
    @Suppress("unused")
    val CAP_LCS = LCSClass("CAP_LCS")
}
