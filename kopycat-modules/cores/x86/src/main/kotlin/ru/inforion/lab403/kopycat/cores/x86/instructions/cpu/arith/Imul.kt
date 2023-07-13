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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Imul(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "imul"

    private fun twoThreeOperand(l: AOperand<x86Core>, r: AOperand<x86Core>) = if (l.dtyp == Datatype.QWORD)
        (l.ssext(core).bigint * r.ssext(core).bigint) to Datatype.XMMWORD
    else
        (l.usext(core) * r.usext(core)).bigint to when (l.dtyp) {
            Datatype.DWORD -> Datatype.QWORD
            Datatype.WORD -> Datatype.DWORD
            else -> Datatype.WORD
        }

    override fun execute() {
        when (opcount) {
            1 -> {
                val (result, extSize) = when (op1.dtyp) {
                    Datatype.BYTE -> {
                        core.cpu.regs.ax.value = core.cpu.regs.al.toOperand().usext(core) * op1.usext(core)
                        core.cpu.regs.ax.value.bigint to Datatype.WORD
                    }
                    Datatype.WORD -> {
                        val result = core.cpu.regs.ax.toOperand().usext(core) * op1.usext(core)
                        core.cpu.regs.ax.value = result
                        core.cpu.regs.dx.value = result ushr 16
                        result.bigint to Datatype.DWORD
                    }
                    Datatype.DWORD -> {
                        val result = core.cpu.regs.eax.toOperand().usext(core) * op1.usext(core)
                        core.cpu.regs.eax.value = result
                        core.cpu.regs.edx.value = result ushr 32
                        result.bigint to Datatype.QWORD
                    }
                    Datatype.QWORD -> {
                        val result = core.cpu.regs.rax.toOperand().ssext(core).bigint * op1.ssext(core).bigint
                        core.cpu.regs.rax.value = result.ulong
                        core.cpu.regs.rdx.value = (result ushr 64).ulong
                        result to Datatype.XMMWORD
                    }
                    else -> throw GeneralException("Incorrect datatype")
                }
                FlagProcessor.processImulFlag(
                    core,
                    result,
                    op1,
                    extSize,
                )
            }
            2 -> {
                val (result, extSize) = twoThreeOperand(op1, op2)
                FlagProcessor.processImulFlag(
                    core,
                    result,
                    op1,
                    extSize,
                )
                op1.value(core, result.ulong)
            }
            3 -> {
                val (result, extSize) = twoThreeOperand(op2, op3)
                FlagProcessor.processImulFlag(
                    core,
                    result,
                    op2,
                    extSize,
                )
                op1.value(core, result.ulong)
            }
        }
    }
}