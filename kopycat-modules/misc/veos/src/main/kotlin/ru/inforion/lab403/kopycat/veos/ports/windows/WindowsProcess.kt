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
package ru.inforion.lab403.kopycat.veos.ports.windows

import ru.inforion.lab403.kopycat.modules.memory.VirtualMemory
import ru.inforion.lab403.kopycat.veos.kernel.Process
import ru.inforion.lab403.kopycat.veos.kernel.System


class WindowsProcess(
        sys: System,
        id: Int,
        memory: VirtualMemory // TODO: Ugly, make default & move outside ctor, or lateinit var
) : Process(sys, id, memory) {

    var segmentFS = 0uL

    override fun initProcess(entryPoint: ULong) {
        super.initProcess(entryPoint)

        segmentFS = memory.allocateByAlignment("fs", 0x1000)!!.first

        // TODO: as structure, in Windows Process (Thread)
        sys.abi.writePointer(segmentFS + 0x4u, stackBottom)
        sys.abi.writePointer(segmentFS + 0x18u, segmentFS) // Linear address of TEB
    }

}