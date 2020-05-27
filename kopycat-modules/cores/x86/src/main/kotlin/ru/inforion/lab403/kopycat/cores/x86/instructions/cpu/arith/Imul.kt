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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.arith

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRBL.al
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.eax
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRW.ax
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Imul(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "imul"

    override fun execute() {
        when (operands.size) {
            1 -> {
                when (op1.dtyp) {
                    Datatype.BYTE -> {
                        core.cpu.regs.ax = al.ssext(core) * op1.ssext(core)
                    }
                    Datatype.WORD -> {
                        val result = ax.ssext(core) * op1.ssext(core)
                        core.cpu.regs.ax = result
                        core.cpu.regs.dx = result ushr 16
                    }
                    Datatype.DWORD -> {
                        val result = eax.ssext(core) * op1.ssext(core)
                        core.cpu.regs.eax = result
                        core.cpu.regs.edx = result ushr 32
                    }
                    else -> throw GeneralException("Incorrect datatype")
                }
                FlagProcessor.processOneOpImulFlag()
            }
            2 -> {
                val result = op1.ssext(core) * op2.ssext(core)
                FlagProcessor.processTwoThreeOpImulFlag()
                op1.value(core, result)
            }
            3 -> {
                val result = op2.ssext(core) * op3.ssext(core)
                FlagProcessor.processTwoThreeOpImulFlag()
                op1.value(core, result)
            }
        }
    }
}