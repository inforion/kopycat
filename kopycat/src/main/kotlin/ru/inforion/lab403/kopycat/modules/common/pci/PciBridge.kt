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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*

@Suppress("unused", "PropertyName", "ClassName")

open class PciBridge constructor(
    parent: Module,
    name: String,

    vendorId: Int,
    deviceId: Int,

    revisionId: Int = 0,

    classCode: Int = 0,

    bist: Int = 0,

    expRomBase: Int = 0,

    subordinateBusNumber: Int = 0,
    secondaryBusNumber: Int = 0,
    primaryBusNumber: Int = 0,
    ioLimit: UInt = 0u,
    ioBase: UInt = 0u,
    memoryLimit: UInt = 0u,
    memoryBase: UInt = 0u,
    prefetchableMemoryLimit: UInt = 0u,
    prefetchableMemoryBase: UInt = 0u,
    memoryLimitUpper32: UInt = 0u,
    memoryBaseUpper32: UInt = 0u,
    ioLimitUpper16: UInt = 0u,
    ioBaseUpper16: UInt = 0u,
    bridgeControl: UInt = 0u,
    interruptPin: UInt = 0u,
    interruptLine: UInt = 0u,
) : PciAbstract(parent, name, vendorId, deviceId, revisionId, classCode, 0x01, bist) {

    companion object {
        @Transient private val log = logger(FINE)
    }

    val PARAMETER_18 = object : PCI_CONF_FUNC_WR(0x18, DWORD, "PARAMETER_18") {
        var SUBORDINATE_BUS_NUMBER by field(23..16)
        var SECONDARY_BUS_NUMBER by field(15..8)
        var PRIMARY_BUS_NUMBER by field(7..0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            SUBORDINATE_BUS_NUMBER = subordinateBusNumber.ulong_z
            SECONDARY_BUS_NUMBER = secondaryBusNumber.ulong_z
            PRIMARY_BUS_NUMBER = primaryBusNumber.ulong_z
            return super.read(ea, ss, size)
        }
    }

    val PARAMETER_1C = object : PCI_CONF_FUNC_WR(0x1C, DWORD, "PARAMETER_1C") {
        var SECONDARY_STATUS by field(23..16)
        var IO_LIMIT by field(15..8)
        var IO_BASE by field(7..0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            IO_LIMIT = ioLimit.ulong_z
            IO_BASE = ioBase.ulong_z
            return super.read(ea, ss, size)
        }
    }

    val PARAMETER_20 = object : PCI_CONF_FUNC_WR(0x20, DWORD, "PARAMETER_20") {
        var MEMORY_LIMIT by field(31..16)
        var MEMORY_BASE by field(15..0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            MEMORY_LIMIT = memoryLimit.ulong_z
            MEMORY_BASE = memoryBase.ulong_z
            return super.read(ea, ss, size)
        }
    }

    val PARAMETER_24 = object : PCI_CONF_FUNC_WR(0x24, DWORD, "PARAMETER_24") {
        var PREFETCHABLE_MEMORY_LIMIT by field(31..16)
        var PREFETCHABLE_MEMORY_BASE by field(15..0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            PREFETCHABLE_MEMORY_LIMIT = prefetchableMemoryLimit.ulong_z
            PREFETCHABLE_MEMORY_BASE = prefetchableMemoryBase.ulong_z
            return super.read(ea, ss, size)
        }
    }

    val PARAMETER_BASE_UPPER32 = PCI_CONF_FUNC_WR(0x28, DWORD, "PARAMETER_BASE_UPPER32", memoryBaseUpper32.ulong_z)
    val PARAMETER_LIMIT_UPPER32 = PCI_CONF_FUNC_WR(0x2C, DWORD, "PARAMETER_LIMIT_UPPER32", memoryLimitUpper32.ulong_z)

    val PARAMETER_30 = object : PCI_CONF_FUNC_WR(0x30, DWORD, "PARAMETER_30") {
        var IO_LIMIT_UPPER16 by field(31..16)
        var IO_BASE_UPPER16 by field(15..0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            IO_LIMIT_UPPER16 = ioLimitUpper16.ulong_z
            IO_BASE_UPPER16 = ioBaseUpper16.ulong_z
            return super.read(ea, ss, size)
        }
    }

    val PARAMETER_34 = PCI_CONF_FUNC_WR(0x34, DWORD, "PARAMETER_34")

    val RESERVED_38 = PCI_CONF_FUNC_WR(0x38, DWORD, "RESERVED_38")

    val PARAMETER_3C = object : PCI_CONF_FUNC_WR(0x3C, DWORD, "PARAMETER_3C") {
        var BRIDGE_CONTROL by field(31..16)
        var INTERRUPT_PIN by field(15..8)
        var INTERRUPT_LINE by field(7..0)

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            BRIDGE_CONTROL = bridgeControl.ulong_z
            INTERRUPT_PIN = interruptPin.ulong_z
            INTERRUPT_LINE = interruptLine.ulong_z
            return super.read(ea, ss, size)
        }
    }
}