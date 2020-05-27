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
package ru.inforion.lab403.kopycat.device

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts

class TestCore(parent: Module, name: String, frequency: Long = 77.MHz):
        ACore<TestCore, TestCPU, TestCOP>(parent, name, frequency, 1.0) {
    override fun abi(heap: LongRange, stack: LongRange): ABI<TestCore> =
            throw NotImplementedError("Operating system not supported")

    inner class Ports: ModulePorts(this) {
        val mem = Proxy("mem")
    }

    inner class Buses: ModuleBuses(this) {
        val mem = Bus("mem")
    }

    override val ports = Ports()
    override val buses = Buses()

    override val cpu = TestCPU(this, "cpu")
    override val cop = TestCOP(this, "cop")
    override val mmu = null
    override val fpu = null

    init {
        cpu.ports.mem.connect(buses.mem)
        ports.mem.connect(buses.mem)
    }
}