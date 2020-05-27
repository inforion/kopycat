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
package ru.inforion.lab403.kopycat.cores.x86.enums


enum class ExcCode(val code: Int, val hasError: Boolean = false) {
    DivisionByZero(0x00),
    Debug(0x01),
    NMI(0x02),
    Breakpoint(0x03),
    Overflow(0x04),
    BoundRangeExceeded(0x05),
    InvalidOpcode(0x06),
    DeviceNotAvailable(0x07),
    DoubleFault(0x08, true),
    CoprocessorSegmentOverrun(0x09),
    InvalidTSS(0x0A, true),
    SegmentNotPresent(0x0B, true),
    StackSegmentFault(0x0C, true),
    GeneralProtectionFault(0x0D, true),
    PageFault(0x0E, true),
    Reserved(0x0F),
    AlignmentCheck(0x11),
    MachineCheck(0x12),
    SIMDFloatingPointException(0x13),
    VirtualizationException(0x14),
    SecurityException(0x1E),

    FpuException(0xFF)
}

