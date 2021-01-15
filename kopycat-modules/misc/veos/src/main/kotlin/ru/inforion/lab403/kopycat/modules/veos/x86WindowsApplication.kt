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
package ru.inforion.lab403.kopycat.modules.veos

import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.common.ComponentTracer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.modules.cores.x86Debugger
import ru.inforion.lab403.kopycat.veos.WindowsVEOS


class x86WindowsApplication constructor(
        parent: Module?,
        name: String,
        dir: String,
        exec: String,
        args: String = ""
): AApplication<x86Core, WindowsVEOS<x86Core>>(parent, name, exec, args) {

    val x86 = x86Core(
            this,
            "x86",
            frequency = 40.MHz,
            generation = x86Core.Generation.i486,
            ipc = 0.388,
            useMMU = false
    ).apply {
        cpu.cregs.vpe = true
        cpu.defaultSize = true
    }

    val dbg = x86Debugger(this, "dbg")
    val trc = ComponentTracer<x86Core>(this, "trc")

    override val veos = WindowsVEOS<x86Core>(this, "veos").apply {
        conf.rootDirectory = dir
    }

    init {
        dbg.ports.reader.connect(x86.buses.virtual)
        dbg.ports.breakpoint.connect(x86.buses.virtual)

        x86.ports.mem.connect(buses.mem)

        veos.ports.mem.connect(buses.mem)

        trc.addTracer(veos)

        buses.connect(trc.ports.trace, dbg.ports.trace)
    }
}