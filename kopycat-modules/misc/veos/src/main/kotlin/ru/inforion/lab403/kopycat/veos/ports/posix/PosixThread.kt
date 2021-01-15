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
package ru.inforion.lab403.kopycat.veos.ports.posix

import ru.inforion.lab403.kopycat.modules.memory.VirtualMemory
import ru.inforion.lab403.kopycat.veos.kernel.Process
import ru.inforion.lab403.kopycat.veos.kernel.System
import java.util.concurrent.LinkedBlockingQueue


// TODO: PosixThread used in Unix subsystem consider to rename it
class PosixThread constructor(
        sys: System,
        id: Int,
        memory: VirtualMemory
): Process(sys, id, memory) {

    var parentProcess: PosixThread? = null
    val childProcesses = mutableListOf<PosixThread>()
    val exitedProcesses = LinkedBlockingQueue<PosixThread>()

    override fun exit() {
        super.exit()
        parentProcess?.exitedProcesses?.add(this)
    }

    override fun segfault() {
        super.segfault()
        parentProcess?.exitedProcesses?.add(this)
    }
}
