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
package ru.inforion.lab403.kopycat.modules.tests

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.BUS30
import ru.inforion.lab403.kopycat.modules.memory.RAM
import ru.inforion.lab403.kopycat.modules.memory.ROM

class MemoryChip(
        parent: Module,
        name: String,
        val fwMode: Int
): Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem", BUS30)
    }

    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem", BUS30)
    }

    override val ports = Ports()
    override val buses = Buses()

    val rom = ROM(this, "rom", 0x0005_0000, Resource("binaries/mips.bin"))
    val ram = RAM(this, "ram", 0x0800_0000)

    override fun reset() {
        super.reset()
        rom.outb(0x308, fwMode.asULong)
    }

    init {
        rom.ports.mem.connect(buses.mem)
        ram.ports.mem.connect(buses.mem, 0x1000_0000L)

        ports.mem.connect(buses.mem)
    }
}