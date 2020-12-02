/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
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
package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.msp430.hardware.processors.MSP430COP
import ru.inforion.lab403.kopycat.cores.msp430.hardware.processors.MSP430CPU
import ru.inforion.lab403.kopycat.modules.BUS16


class MSP430Core constructor(parent: Module, name: String, frequency: Long):
        ACore<MSP430Core, MSP430CPU, MSP430COP>(parent, name, frequency, 1.0) {

    inner class Buses: ModuleBuses(this) {
        val mem = Bus("mem", BUS16)
    }
    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem", BUS16)
    }

    override val buses = Buses()
    override val ports = Ports()

    override val cpu = MSP430CPU(this, "cpu")
    override val cop = MSP430COP(this, "cop")
    override val mmu = null
    override val fpu = null

    init {
        cpu.ports.mem.connect(buses.mem)
        ports.mem.connect(buses.mem)
    }
}