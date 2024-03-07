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
package ru.inforion.lab403.kopycat.runtime.analyzer.stack

import ru.inforion.lab403.common.extensions.long
import ru.inforion.lab403.kopycat.cores.arm.hardware.processors.ARMv6CPU
import ru.inforion.lab403.kopycat.runtime.analyzer.stack.StackAnalyzerCore
import ru.inforion.lab403.kopycat.runtime.analyzer.stack.StackAnalyzerRegsData
import kotlin.math.abs

class ARMv6StackAnalyzerCore(override val cpu: ARMv6CPU) : StackAnalyzerCore {
    override val sp: ULong
        get() = cpu.regs.sp.value
    override val pc: ULong
        get() = cpu.pc
    override val ra: ULong
        get() = cpu.regs.lr.value

    override val STACK_MAX_GROW_ADDRESS: ULong = 0x0uL
    override val STACK_MIN_GROW_ADDRESS: ULong = 0xFFFF_FFFF_FFFF_FFFFuL

    override fun isCallPerhaps(current: StackAnalyzerRegsData, previous: StackAnalyzerRegsData): Boolean {
        // TODO: написать, почему 32 или сделать нормально как-то
        // 32 is just random number idk (4 bytes per command and 8, just random number)
//        if (previous.ra != current.ra) {
//            print("current.ra=${current.ra.hex}, abs(previous.pc.long - current.ra.long): ${abs(previous.pc.long - current.ra.long)}\n")
//            print("previous.pc=${previous.pc.hex}, current.pc=${current.pc.hex}\n\n")
//        }
        return previous.ra != current.ra && abs(previous.pc.long - current.ra.long) < 32
    }

    override fun isReturnPerhaps(current: StackAnalyzerRegsData, previous: StackAnalyzerRegsData): Boolean {
        return previous.ra != current.ra && previous.sp != current.sp
    }
}