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
package ru.inforion.lab403.kopycat.veos.ports.sysdep

import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.veos.kernel.System
import ru.inforion.lab403.kopycat.veos.ports.signal.sigaction
import ru.inforion.lab403.kopycat.veos.ports.stat.stat

abstract class ASystemDep(
        val SOCK_STREAM: Int = 1,
        val RLIMIT_NOFILE: Int = 7,
        val FIONREAD: Int = 0x541B,
        val SOL_SOCKET: Int = 1,
        val SO_REUSEADDR: Int = 2,

        val O_CREAT: Int = 0x40, /*       00000100 */
        val O_EXCL: Int = 0x80, /*        00000200 */
        val O_TRUNC: Int = 0x200, /*      00001000 */
        val O_APPEND: Int = 0x400, /*     00002000 */
        val O_NONBLOCK: Int = 0x800, /*   00004000 */
        val O_LARGEFILE: Int = 0x8000, /* 00100000 */

        val SIGSET_NWORDS: Int = 64 / (8 * 4 /* sizeof(unsigned long) */)
) {
    companion object {
        val System.deps get() = when(abi.core) {
            is AARMCore -> ARM
            is MipsCore -> MIPS
            else -> throw NotImplementedError("Architecture: ${abi.core}")
        }
    }

    abstract fun toSigaction(sys: System, address: Long): sigaction

    abstract fun toStat(sys: System, address: Long, stat: stat)

    abstract fun toStat64(sys: System, address: Long, stat: stat)
}