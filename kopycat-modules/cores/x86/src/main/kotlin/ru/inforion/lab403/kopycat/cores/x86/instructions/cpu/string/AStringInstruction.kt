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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.string

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.StringPrefix
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core


abstract class AStringInstruction(core: x86Core, opcode: ByteArray, prefs: Prefixes, val isRepeOrRepne:Boolean, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {

    protected abstract fun executeStringInstruction()

    final override fun execute() {
        if (prefs.string != StringPrefix.NO) {
            val isRepz = prefs.string == StringPrefix.REPZ
            val isRepnz = prefs.string == StringPrefix.REPNZ
            val counter = core.cpu.regs.gpr(x86GPR.RCX, prefs.addrsize).toOperand()

            val initialCX = counter.value(core)
            val initialSI = core.cpu.regs.gpr(x86GPR.RSI, Datatype.QWORD).value
            val initialDI = core.cpu.regs.gpr(x86GPR.RDI, Datatype.QWORD).value

            while (counter.value(core) != 0uL) {
                // TODO: ServiceInterrupts()
                try {
                    executeStringInstruction()
                } catch (e: x86HardwareException.PageFault) {
                    counter.value(core, initialCX)
                    core.cpu.regs.gpr(x86GPR.RSI, Datatype.QWORD).value = initialSI
                    core.cpu.regs.gpr(x86GPR.RDI, Datatype.QWORD).value = initialDI
                    throw e
                }
                counter.minus(core, 1uL)
//                val zf = counter.value(cpu) == 0L
                if(isRepeOrRepne)
                    if (isRepz && !core.cpu.flags.zf || isRepnz && core.cpu.flags.zf) break
            }
        } else executeStringInstruction()
//        executeStringInstruction()
    }
}