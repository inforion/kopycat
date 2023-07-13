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
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.field

@Suppress("unused", "PropertyName", "ClassName")

open class PciDevice constructor(
    parent: Module,
    name: String,

    vendorId: Int,
    deviceId: Int,

    revisionId: Int = 0,

    classCode: Int = 0,

    bist: Int = 0,

    subsystemId: Int = 0,
    subsystemVendorId: Int = 0,
    expRomBase: Int? = null,

    interruptLine: Int = 0,
    interruptPin: Int = 0,
    minGrant: Int = 0xFF,
    maxLatency: Int = 0xFF
) : PciAbstract(parent, name, vendorId, deviceId, revisionId,
    classCode, 0x00, bist) {

    val CARDBUS_PTR = PCI_CONF_FUNC_WR(0x28, DWORD, "CARDBUS_PTR")

    val SUB_VENDOR_SYSTEM_ID = object : PCI_CONF_FUNC_WR(0x2C, DWORD, "SUB_VENDOR_SYSTEM_ID") {
        var SUBSYSTEM_ID by field(31..16)
        var SUBSYSTEM_VENDOR_ID by field(15..0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            SUBSYSTEM_ID = subsystemId.ulong_z
            SUBSYSTEM_VENDOR_ID = subsystemVendorId.ulong_z
            return super.read(ea, ss, size)
        }
    }

    val EXP_ROM_BASE = object : PCI_CONF_FUNC_WR(0x30, DWORD, "EXP_ROM_BASE", expRomBase?.ulong_z ?: 0uL) {
        override fun read(ea: ULong, ss: Int, size: Int) = if (expRomBase != null)
            super.read(ea, ss, size)
        else
            0uL
    }

    val CAP_PTR = object : PCI_CONF_FUNC_WR(0x34, DWORD, "CAP_PTR") {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            data = capabilities.minOfOrNull { it.base } ?: 0uL
            return super.read(ea, ss, size)
        }
    }

    val RESERVED_38 = PCI_CONF_FUNC_WR(0x38, DWORD, "RESERVED_38")

    // INT_LINE, INT_PIN, MIN_GRANT, MAX_LATENCY
    val PARAMETERS_3C = object : PCI_CONF_FUNC_WR(0x3C, DWORD, "PARAMETERS_3C") {

        var MAX_LATENCY by field(31..24)
        var MIN_GRANT by field(23..16)
        var INTERRUPT_PIN by field(15..8)
        var INTERRUPT_LINE by field(7..0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            MAX_LATENCY = maxLatency.ulong_z
            MIN_GRANT = minGrant.ulong_z
            INTERRUPT_PIN = interruptPin.ulong_z
            INTERRUPT_LINE = interruptLine.ulong_z
            return super.read(ea, ss, size)
        }
    }
}