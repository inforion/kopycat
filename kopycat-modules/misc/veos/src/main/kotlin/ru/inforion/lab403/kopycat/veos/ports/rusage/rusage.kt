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
package ru.inforion.lab403.kopycat.veos.ports.rusage

import ru.inforion.lab403.kopycat.veos.api.pointers.IntPointer
import ru.inforion.lab403.kopycat.veos.kernel.System

// TODO: Make from StructPointer
class rusage(sys: System, address: Long) : IntPointer(sys, address) {
    companion object {
//        fun sizeOf(sys: System) = sys.sizeOf.word * 2
//        fun allocate(sys: System) = FILE(sys, sys.allocateClean(sizeOf(sys)))
    }

    // TODO: replace timeval with this class
    inner class timevalInner(val offset: Int): IntPointer(sys, address) {
        var tvSec by field(offset)
        var tvUsec by field(offset + 1)
    }

    var ruUtime = timevalInner(0)
    var ruStime = timevalInner(2)
    // type: long
    var ruMaxrss by field(4)
    var ruIxrss by field(5)
    var ruIdrss by field(6)
    var ruIsrss by field(7)
    var ruMinflt by field(8)
    var ruMajflt by field(9)
    var ruNswap by field(10)
    var ruInblock by field(11)
    var ruOublock by field(12)
    var ruMsgsnd by field(13)
    var ruMsgrcv by field(14)
    var ruNsignals by field(15)
    var ruNvcsw by field(16)
    var ruNivcsw by field(17)
}
