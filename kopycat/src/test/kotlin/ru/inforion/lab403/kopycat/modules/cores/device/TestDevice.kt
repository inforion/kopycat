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
package ru.inforion.lab403.kopycat.modules.cores.device

import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.modules.memory.RAM

class TestDevice(parent: Module?, name: String): Module(parent, name) {
    inner class Buses : ModuleBuses(this) { val mem = Bus("mem") }
    inner class Ports : ModulePorts(this) { val mem = Proxy("mem") }
    private val testCore = TestCore(this, "Test core")
    private val dbg = TestDebugger(this, "Test Debugger")
    override val ports = Ports()
    override val buses = Buses()
    private val sram = RAM(this, "sram", 0x1000_0000)
    init {
        testCore.ports.mem.connect(buses.mem)
        dbg.ports.reader.connect(testCore.buses.mem)
        ports.mem.connect(buses.mem)
        sram.ports.mem.connect(buses.mem)
    }
}