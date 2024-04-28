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
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import java.util.logging.Level.*

/**
 * Performance Monitoring Counter
 */
class PMC(parent: Module, name: String) : Module(parent, name) {
    companion object {
        const val BUS_SIZE = 512
        const val BUS_INDEX = 1
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", BUS_SIZE)
    }

    override val ports = Ports()

    private val PM1_STS_EN = ByteAccessRegister(ports.mem, 0x00u, DWORD, "PM1_STS_EN", level = CONFIG)
    private val PM1_CNT = ByteAccessRegister(ports.mem, 0x04u, DWORD, "PM1_CNT", level = CONFIG)
    private val PM1_TMR = ByteAccessRegister(ports.mem, 0x08u, DWORD, "PM1_TMR", level = CONFIG)

    private val REG_10 = Register(ports.mem, 0x10u, DWORD, "REG_10", level = CONFIG)

    private val GPE0a_STS = ByteAccessRegister(ports.mem, 0x20u, DWORD, "GPE0a_STS", level = CONFIG)
    private val GPE0a_EN = ByteAccessRegister(ports.mem, 0x28u, DWORD, "GPE0a_EN", level = CONFIG)

    private val REG_24 = ByteAccessRegister(ports.mem, 0x24u, DWORD, "REG_24", level = CONFIG)

    private val SMI_EN = ByteAccessRegister(ports.mem, 0x30u, DWORD, "SMI_EN", level = CONFIG)
    private val SMI_STS = ByteAccessRegister(ports.mem, 0x34u, DWORD, "SMI_STS", level = CONFIG)

    private val ALT_GPIO_SMI = ByteAccessRegister(ports.mem, 0x38u, DWORD, "ALT_GPIO_SMI", level = CONFIG)

    private val UPRWC = ByteAccessRegister(ports.mem, 0x3Cu, DWORD, "UPRWC", level = CONFIG)

    private val GPE_CTRL = ByteAccessRegister(ports.mem, 0x40u, DWORD, "GPE_CTRL", level = CONFIG)

    private val ETR = ByteAccessRegister(ports.mem, 0x48u, DWORD, "ETR", level = CONFIG)

    private val PM2A_CNT_BLK = ByteAccessRegister(ports.mem, 0x50u, DWORD, "PM2A_CNT_BLK", level = CONFIG)

    private val TCO_RLD = ByteAccessRegister(ports.mem, 0x60u, DWORD, "TCO_RLD", level = CONFIG)
    private val TCO_STS = ByteAccessRegister(ports.mem, 0x64u, DWORD, "TCO_STS", level = CONFIG)
    private val TCO1_CNT = ByteAccessRegister(ports.mem, 0x68u, DWORD, "TCO1_CNT", level = CONFIG)

    private val TCO_TMR = ByteAccessRegister(ports.mem, 0x70u, DWORD, "TCO_TMR", level = CONFIG)
}