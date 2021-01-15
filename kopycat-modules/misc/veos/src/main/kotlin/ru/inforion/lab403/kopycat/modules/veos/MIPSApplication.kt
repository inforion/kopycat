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
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.modules.cores.MipsDebugger
import ru.inforion.lab403.kopycat.veos.UnixVEOS


class MIPSApplication constructor(
        parent: Module?,
        name: String,
        dir: String,
        exec: String,
        args: String = "",
        ldPreload: String = ""
): AApplication<MipsCore, UnixVEOS<MipsCore>>(parent, name, exec, args, ldPreload) {

    val mips = MipsCore(
            this,
            "mips",
            frequency = 50.MHz,
            multiplier = 9,
            ArchitectureRevision = 1,
            countOfShadowGPR = 0,
            ipc = 1.0,
            PABITS = 32,
            PRId = 0x55ABCC01, // PRId from my imagination
            useMMU = false
    )

    val dbg = MipsDebugger(this, "dbg")
    val trc = ComponentTracer<MipsCore>(this, "trc")

    override val veos = UnixVEOS<MipsCore>(this, "veos").apply {
        conf.rootDirectory = dir
    }

    init {
        dbg.ports.reader.connect(mips.buses.virtual)
        dbg.ports.breakpoint.connect(mips.buses.virtual)

        mips.ports.mem.connect(buses.mem)

        veos.ports.mem.connect(buses.mem)

        trc.addTracer(veos)

        buses.connect(trc.ports.trace, dbg.ports.trace)
    }
}