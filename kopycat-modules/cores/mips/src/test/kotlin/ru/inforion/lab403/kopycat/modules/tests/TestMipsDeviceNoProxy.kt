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

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.shl
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.BUS30
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.modules.cores.MipsDebugger
import ru.inforion.lab403.kopycat.modules.memory.RAM
import ru.inforion.lab403.kopycat.modules.memory.ROM

class TestMipsDeviceNoProxy(parent: Module?, name: String, val fwMode: Int, fakePrimitivesCount: Int): Module(parent, name) {
    /*
    *   ___________________________
    *  |MIPS CORE _______________  |
    *  |         | MIPS Debugger | |----
    *  |         |_______________| |    |
    *  |___________________________|    |
    *                                   |
    *   _____________________________   |
    *  |Flash                        |  |
    *  |  setFlashPtr <-- 0x00 (Reg) |  |
    *  |  readReg     <-- 0x04 (Reg) ||-|  <-- 0x1800_0000
    *  |  writeReg    <-- 0x08 (Reg) |  |
    *  |_____________________________|  |
    *                                   |
    *   _____                           |
    *  |     |  0x0000_0000             |
    *  | ROM ||-------------------------|
    *  |_____|  (size = 0x50000)        |
    *                                   |
    *   _____                           |
    *  |     |  0x1000_0000             |
    *  | RAM ||-------------------------|
    *  |_____|  size=0x8000000          |
    *                                   |
    *   ______                          |
    *  | Fake |  0x1800_0000            |
    *  | Area ||------------------------|
    *  |______|  (size = 0x1000)        |
    *                                   |
    *      . . . . . . . . . . . . .    |
    *                                   |
    *   ______                          |
    *  | Fake |  0x1801_0000            |
    *  | Reg  ||------------------------|
    *  |______|                         |
    *                                   |
    *      . . . . . . . . . . . . .    |
    */

    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem", BUS30)
    }

    override val buses = Buses()

    val mips = MipsCore(this, "mips", 100.MHz, 1.0,9, 0x55ABCC01L, 30)
    val dbg = MipsDebugger(this, "debugger")
    val flash = Flash(this, "flash")

    val rom = ROM(this, "ROM_CHIP", 0x0005_0000, Resource("binaries/mips.bin"))
    val ram = RAM(this, "RAM_CHIP", 0x0800_0000)

    private fun value(index: Int): Long {
        val v = index.toByte()
        return (v shl 24) or (v shl 16) or (v shl 8) or (v shl 0)
    }

    val fakeAreas = Array(fakePrimitivesCount) { FakeArea(this, "fa$it", 0x1000, value(it)) }

    val fakeRegs = Array(fakePrimitivesCount) { FakeRegister(this, "fr$it", value(it)) }

    override fun reset() {
        super.reset()
        rom.outb(0x308, fwMode.asULong)
    }

    init {
        mips.ports.mem.connect(buses.mem)
        dbg.ports.breakpoint.connect(mips.buses.virtual)
        dbg.ports.reader.connect(mips.buses.virtual)

        flash.ports.mem.connect(buses.mem, 0x1800_0000)
        rom.ports.mem.connect(buses.mem, 0x0000_0000)
        ram.ports.mem.connect(buses.mem, 0x1000_0000)

        fakeAreas.forEachIndexed { index, area -> area.ports.mem.connect(buses.mem, 0x1800_1000 + index * 0x1000L)}
        fakeRegs.forEachIndexed { index, reg -> reg.ports.mem.connect(buses.mem, 0x1801_1000 + index * 0x1000L)}
    }
}