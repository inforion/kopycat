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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.string

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.StringPrefix
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.ecx
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.cx
import ru.inforion.lab403.kopycat.modules.cores.x86Core


abstract class AStringInstruction(core: x86Core, opcode: ByteArray, prefs: Prefixes, val isRepeOrRepne:Boolean, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {

    abstract protected fun executeStringInstruction()

    final override fun execute() {
        if (prefs.string != StringPrefix.NO) {
            val isRepz = prefs.string == StringPrefix.REPZ
            val isRepnz = prefs.string == StringPrefix.REPNZ
            val counter = if (prefs.is16BitAddressMode) cx else ecx
            while (counter.value(core) != 0L) {
                // TODO: ServiceInterrupts()
                executeStringInstruction()
                counter.minus(core, 1L)
//                val zf = counter.value(cpu) == 0L
                if(isRepeOrRepne)
                    if (isRepz && !core.cpu.flags.zf || isRepnz && core.cpu.flags.zf) break
            }
        } else executeStringInstruction()
//        executeStringInstruction()
    }
}