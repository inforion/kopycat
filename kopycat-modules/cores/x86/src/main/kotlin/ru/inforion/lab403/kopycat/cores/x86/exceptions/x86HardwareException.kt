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
package ru.inforion.lab403.kopycat.cores.x86.exceptions

import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.x86.enums.ExcCode


abstract class x86HardwareException(excCode: ExcCode, where: Long, val errorCode: Long = 0): HardwareException(excCode, where) {
    override fun toString(): String = "$prefix: $excCode"

    class DivisionByZero(where: Long) : x86HardwareException(ExcCode.DivisionByZero, where)
    class Debug(where: Long) : x86HardwareException(ExcCode.Debug, where)
    class NMI(where: Long) : x86HardwareException(ExcCode.NMI, where)
    class Breakpoint(where: Long) : x86HardwareException(ExcCode.Breakpoint, where)
    class Overflow(where: Long) : x86HardwareException(ExcCode.Overflow, where)
    class BoundRangeExceeded(where: Long) : x86HardwareException(ExcCode.BoundRangeExceeded, where)
    class InvalidOpcode(where: Long) : x86HardwareException(ExcCode.InvalidOpcode, where)
    class DeviceNotAvailable(where: Long) : x86HardwareException(ExcCode.DeviceNotAvailable, where)
    class DoubleFault(where: Long, errorCode: Long) : x86HardwareException(ExcCode.DoubleFault, where, errorCode)
    class CoprocessorSegmentOverrun(where: Long) : x86HardwareException(ExcCode.CoprocessorSegmentOverrun, where)
    class InvalidTSS(where: Long, errorCode: Long) : x86HardwareException(ExcCode.InvalidTSS, where, errorCode)
    class SegmentNotPresent(where: Long, errorCode: Long) : x86HardwareException(ExcCode.SegmentNotPresent, where, errorCode)
    class StackSegmentFault(where: Long, errorCode: Long) : x86HardwareException(ExcCode.StackSegmentFault, where, errorCode)
    class GeneralProtectionFault(where: Long, errorCode: Long) : x86HardwareException(ExcCode.GeneralProtectionFault, where, errorCode)
    class PageFault(where: Long, val address: Long, I: Int, R: Int, U: Int, W: Int, P: Int) :
            x86HardwareException(ExcCode.PageFault, where,
                    insert(I, 4)
                    .insert(R, 3)
                    .insert(U, 2)
                    .insert(W, 1)
                    .insert(P, 0).toLong())

    class FpuException(where: Long) : x86HardwareException(ExcCode.FpuException, where)
}