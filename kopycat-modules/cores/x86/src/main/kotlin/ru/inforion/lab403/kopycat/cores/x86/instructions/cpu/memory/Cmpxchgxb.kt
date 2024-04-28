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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.bigint
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Cmpxchgxb(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
    AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {

    override val mnem = "cmpxchg${op1.dtyp.bytes}b"

    override fun execute() {
        when (op1.dtyp) {
            Datatype.QWORD -> {
                // cmpxchg8b
                val operand = op1.value(core)
                val eax = core.cpu.regs.gpr(x86GPR.RAX, Datatype.DWORD)
                val edx = core.cpu.regs.gpr(x86GPR.RDX, Datatype.DWORD)

                core.cpu.flags.zf = eax.value == operand[31..0] && edx.value == operand[63..32]

                // IF (EDX:EAX = DEST)
                if (core.cpu.flags.zf) {
                    // DEST := ECX:EBX;
                    op1.value(
                        core,
                        0uL
                            .insert(core.cpu.regs.gpr(x86GPR.RBX, Datatype.DWORD).value, 31..0)
                            .insert(core.cpu.regs.gpr(x86GPR.RCX, Datatype.DWORD).value, 63..32)
                    )
                } else {
                    // EDX:EAX := TEMP64;
                    eax.value = operand[31..0]
                    edx.value = operand[63..32]
                }
            }
            Datatype.XMMWORD -> {
                // cmpxchg16b
                val operand = op1.extValue(core)
                val rax = core.cpu.regs.gpr(x86GPR.RAX, Datatype.QWORD)
                val rdx = core.cpu.regs.gpr(x86GPR.RDX, Datatype.QWORD)

                core.cpu.flags.zf = rax.value == operand[63..0].ulong && rdx.value == operand[127..64].ulong

                // IF (RDX:RAX = TEMP128)
                if (core.cpu.flags.zf) {
                    // DEST := RCX:RBX;
                    op1.extValue(
                        core,
                        0
                            .bigint
                            .insert(core.cpu.regs.gpr(x86GPR.RBX, Datatype.QWORD).value, 63..0)
                            .insert(core.cpu.regs.gpr(x86GPR.RCX, Datatype.QWORD).value, 127..64)
                    )
                } else {
                    // RDX:RAX := TEMP128;
                    rax.value = operand[63..0].ulong
                    rdx.value = operand[127..64].ulong
                }
            }
            else -> TODO("$mnem: don't know what to do with ${op1.dtyp} operand")
        }
    }
}
