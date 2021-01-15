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
package ru.inforion.lab403.kopycat.veos.ports.signal

import ru.inforion.lab403.common.extensions.asByte
import ru.inforion.lab403.kopycat.veos.api.pointers.IntPointer
import ru.inforion.lab403.kopycat.veos.kernel.System
import ru.inforion.lab403.kopycat.veos.ports.dirent.DIR
import ru.inforion.lab403.kopycat.veos.ports.sysdep.ASystemDep.Companion.deps

class sigset_t(sys: System, address: Long = 0): IntPointer(sys, address) {
    companion object {
        fun allocate(sys: System) = DIR(
                sys,
                // TODO: sizeof(unsigned long)
                sys.allocateClean(sys.deps.SIGSET_NWORDS * sys.sizeOf.int)
        )
    }

    val size = sys.deps.SIGSET_NWORDS

    fun fill() = sys.abi.writeBytes(address, ByteArray(size * sys.sizeOf.int) { (-1).asByte })
}