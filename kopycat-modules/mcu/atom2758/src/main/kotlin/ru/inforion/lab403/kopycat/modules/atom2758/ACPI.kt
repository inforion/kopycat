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

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import java.util.logging.Level.CONFIG
import java.util.logging.Level.FINE

class ACPI(parent: Module, name: String) : Module(parent, name) {
    companion object {
        const val BUS_SIZE = 128
        const val BUS_INDEX = 0
    }

    inner class Ports : ModulePorts(this) {
        val io = Slave("io", BUS_SIZE)
    }

    override val ports = Ports()

    private val PM1_STS_EN = ByteAccessRegister(ports.io, 0x00u, DWORD, "PM1_STS_EN", level = CONFIG)
    private val PM1_CNT = ByteAccessRegister(ports.io, 0x04u, DWORD, "PM1_CNT", level = CONFIG)

    private val PM1_TMR = object : ByteAccessRegister(ports.io, 0x08u, DWORD, "PM1_TMR", level = FINE) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            data = core.clock.totalTicks / 10u  // real freq 3_579_545 Hz
            return super.read(ea, ss, size)
        }
    }

    private val GPE0a_STS = ByteAccessRegister(ports.io, 0x20u, DWORD, "GPE0a_STS", level = CONFIG)
    private val GPE0a_EN = ByteAccessRegister(ports.io, 0x28u, DWORD, "GPE0a_EN", level = CONFIG)

    private val SMI_EN = ByteAccessRegister(ports.io, 0x30u, DWORD, "SMI_EN", level = CONFIG)
    private val SMI_STS = ByteAccessRegister(ports.io, 0x34u, DWORD, "SMI_STS", level = CONFIG)

    private val ALT_GPIO_SMI = ByteAccessRegister(ports.io, 0x38u, DWORD, "ALT_GPIO_SMI", level = CONFIG)

    private val UPRWC = ByteAccessRegister(ports.io, 0x3Cu, DWORD, "UPRWC", level = CONFIG)

    private val GPE_CTRL = ByteAccessRegister(ports.io, 0x40u, DWORD, "GPE_CTRL", level = CONFIG)

    private val PM2A_CNT_BLK = ByteAccessRegister(ports.io, 0x50u, DWORD, "PM2A_CNT_BLK", level = CONFIG)

    private val TCO_RLD = ByteAccessRegister(ports.io, 0x60u, DWORD, "TCO_RLD", level = CONFIG)
    private val TCO_STS = ByteAccessRegister(ports.io, 0x64u, DWORD, "TCO_STS", level = CONFIG)
    private val TCO1_CNT = ByteAccessRegister(ports.io, 0x68u, DWORD, "TCO1_CNT", level = CONFIG)

    private val TCO_TMR = ByteAccessRegister(ports.io, 0x70u, DWORD, "TCO_TMR", level = CONFIG)
}