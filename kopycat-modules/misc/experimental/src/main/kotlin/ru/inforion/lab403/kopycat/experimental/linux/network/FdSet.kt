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
package ru.inforion.lab403.kopycat.experimental.linux.network

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.truth
import ru.inforion.lab403.common.extensions.ushr
import ru.inforion.lab403.kopycat.interfaces.IReadWrite
import ru.inforion.lab403.kopycat.interfaces.inq
import ru.inforion.lab403.kopycat.interfaces.outq

class FdSet(private val rw: IReadWrite, private val addr: ULong) {
    fun zero() {
        for (i in 0uL until 16uL) {
            rw.outq(addr + i * 8uL, 0uL)
        }
    }

    fun set(fd: ULong) {
        val addr = addr + (fd ushr 5) * 8uL
        val fdsBits = rw.inq(addr) or (1uL shl fd[4..0].int)
        rw.outq(addr, fdsBits)
    }

    fun dump() = sequence {
        for (i in 0uL until 16uL) {
            val fdsBits = rw.inq(addr + i * 8uL)
            for (j in 0uL until 64uL) {
                if (fdsBits[j.int].truth) {
                    yield((i shl 5) or j)
                }
            }
        }
    }.toSet()
}