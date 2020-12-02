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
package ru.inforion.lab403.kopycat.modules.cores.arm1176jzs

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.modules.cores.AARMv6Core

class ARM1176JZS constructor(parent: Module, name: String, frequency: Long, ipc: Double):
        AARMv6Core(parent, name, frequency, ipc) {

    inner class Buses: ModuleBuses(this) {
        val phys = Bus("phys")
        val virt = Bus("virt")
    }

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem")
    }

    override val buses = Buses()
    override val ports = Ports()

    init {
        cpu.ports.mem.connect(buses.virt)
        mmu.ports.inp.connect(buses.virt)

        mmu.ports.outp.connect(buses.phys)
        ports.mem.connect(buses.phys)
    }
}