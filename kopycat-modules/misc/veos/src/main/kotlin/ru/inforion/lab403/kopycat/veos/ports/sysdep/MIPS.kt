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

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.kopycat.veos.kernel.System
import ru.inforion.lab403.kopycat.veos.ports.signal.sigaction
import ru.inforion.lab403.kopycat.veos.ports.stat.stat64_mips
import ru.inforion.lab403.kopycat.veos.ports.stat.stat

object MIPS : ASystemDep(
        SOCK_STREAM = 2,
        RLIMIT_NOFILE = 5,
        FIONREAD = 0x467F,
        SOL_SOCKET = 0xFFFF,
        SO_REUSEADDR = 4,
        O_CREAT = 0x0100,
        O_EXCL = 0x0400,
        O_TRUNC = 0x0200,
        O_APPEND = 0x0008,
        O_NONBLOCK = 0x0080,
        O_LARGEFILE = 0x2000,
        SIGSET_NWORDS = 128 / (8 * 4)
) {
    override fun toSigaction(sys: System, address: Long) = sigaction(sys, address,
            handlerOffset = 4,
            flagsOffset = 0,
            restorerOffset = 24,
            maskOffset = 8
    )

    override fun toStat(sys: System, address: Long, stat: stat) {
        TODO("Not yet implemented")
    }

    override fun toStat64(sys: System, address: Long, stat: stat) = with(stat64_mips(sys, address)) {
        st_dev = stat.st_dev
        st_ino = stat.st_ino
        st_mode = stat.st_mode.asInt
        st_nlink = stat.st_nlink
        st_uid = stat.st_uid.asInt
        st_gid = stat.st_gid.asInt
        st_rdev = stat.st_rdev
        st_size = stat.st_size
        st_blksize = stat.st_blksize
        st_blocks = stat.st_blocks
        st_atime = stat.st_atime
        st_mtime = stat.st_mtime
        st_ctime = stat.st_ctime
    }
}