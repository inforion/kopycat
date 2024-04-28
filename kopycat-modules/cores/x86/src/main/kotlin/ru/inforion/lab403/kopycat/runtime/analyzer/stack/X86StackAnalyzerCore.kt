/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.long
import ru.inforion.lab403.kopycat.cores.x86.enums.SSR
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import kotlin.math.abs

class x86StackAnalyzerCore(override val cpu: x86CPU) : StackAnalyzerCore {
    private val spReg get() = cpu.regs.gpr(x86GPR.RSP, x86utils.modeToDatatype(cpu.mode))

    override val sp: ULong
        get() = spReg.value

    override val pc: ULong
        get() = cpu.core.pc

    override val ra: ULong
        get() = runCatching {
            cpu.core.read(spReg.value, SSR.SS.id, x86utils.modeToDatatype(cpu.mode).bytes)
        }.getOrElse {
            if (it !is x86HardwareException) {
                throw it
            }

            return STACK_MIN_GROW_ADDRESS
        }

    override val ring: Int
        get() = cpu.sregs.cs.cpl.int

    override val time: ULong
        get() = cpu.core.info.totalExecuted

    override val STACK_MAX_GROW_ADDRESS: ULong = 0x0uL
    override val STACK_MIN_GROW_ADDRESS: ULong = 0xFFFF_FFFF_FFFF_FFFFuL

    /**
     * Задаёт максимальное расстояние от PC call ... до начала следующей инструкции
     */
    private val MAX_CALL_INSTRUCTION_DELTA = 0x20

    override fun isCallPerhaps(current: StackAnalyzerRegsData, previous: StackAnalyzerRegsData): Boolean {

        return previous.sp != current.sp &&
                abs(previous.pc.long - current.ra.long) < MAX_CALL_INSTRUCTION_DELTA
    }

    override fun isReturnPerhaps(current: StackAnalyzerRegsData, previous: StackAnalyzerRegsData): Boolean {
        return previous.ra == current.pc &&
                previous.sp != current.sp
    }
}