/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.modules.examples

import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.modules.syscores.sha1.hardware.SHA1Core
import ru.inforion.lab403.kopycat.modules.memory.RAM

class SHA1 constructor(parent: Module?, name: String, address: ULong, size: Int): Module(parent, name) {
    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem")
    }

    override val buses = Buses()

    val sha1 = SHA1Core(this, "core")
    val sram = RAM(this, "sram", size)
    val dbg = Debugger(this, "dbg")

    init {
        sha1.ports.mem.connect(buses.mem)
        sram.ports.mem.connect(buses.mem, address)

        dbg.ports.reader.connect(buses.mem)
        dbg.ports.breakpoint.connect(buses.mem)
    }
}