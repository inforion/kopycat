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
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86COP
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86FPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86MMU
import ru.inforion.lab403.kopycat.cores.x86.x86ABI
import ru.inforion.lab403.kopycat.modules.BUS16


class x86Core constructor(parent: Module, name: String, frequency: Long, val generation: Generation, ipc: Double, val useMMU: Boolean = true):
        ACore<x86Core, x86CPU, x86COP>(parent, name, frequency, ipc) {
    enum class Generation { i8086, i186, i286, i386, i486, Am5x86, Pentium }

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem")
        val io = Master("io", BUS16)
    }

    inner class Buses: ModuleBuses(this) {
        val physical = Bus("physical")
        val virtual = Bus("virtual")
    }

    override val ports = Ports()
    override val buses = Buses()

    override val cpu = x86CPU(this, "cpu")
    override val cop = x86COP(this, "cop")
    override val mmu = x86MMU(this, "mmu")
    override val fpu = x86FPU(this, "fpu")

    override fun abi() = x86ABI(this, false)

    init {
        // ToCheck: bad but working solution
        if (useMMU) {
            cpu.ports.mem.connect(buses.virtual)
            mmu.ports.inp.connect(buses.virtual)

            mmu.ports.outp.connect(buses.physical)
            ports.mem.connect(buses.physical)
        }
        else {
            cpu.ports.mem.connect(buses.virtual)
            ports.mem.connect(buses.virtual)
        }
    }
}