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
package ru.inforion.lab403.kopycat.modules.armtest

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.annotations.DontExportModule
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.cores.ARMv7Core
import ru.inforion.lab403.kopycat.modules.cores.ARMDebugger
import ru.inforion.lab403.kopycat.modules.memory.RAM
import ru.inforion.lab403.kopycat.modules.memory.ROM
import java.io.File

@DontExportModule
class ARMTestUnit(
        parent: Module?,
        name: String,
        romPath: String): Module(parent, name) {

    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem", BUS16)
    }
    override val buses = Buses()

    val arm = ARMv7Core(this, "ARM", 100.MHz, 1.0)
    val sram = RAM(this, "sram", 0x0000_2000)
    val peripheral = RAM(this, "peripheral", 0x0002_5000)
    val rom = ROM(this, "rom", 0x8000, File(romPath))
    val dbg = ARMDebugger(this, "dbg")

    init {
        arm.ports.mem.connect(buses.mem)
        rom.ports.mem.connect(buses.mem, 0x0800_0000)
        sram.ports.mem.connect(buses.mem, 0x2000_0000)
        peripheral.ports.mem.connect(buses.mem, 0x4000_0000)
        dbg.ports.breakpoint.connect(buses.mem)
    }
}