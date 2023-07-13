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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Cmpxchg(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "cmpxchg"

    override fun execute() {
        val temp = op1.value(core)
        val fullAcc = core.cpu.regs.gpr(x86GPR.RAX, Datatype.QWORD).value

        val acc = Variable<x86Core>(fullAcc, op1.dtyp)
        FlagProcessor.processAddSubCmpFlag(
            core,
            Variable<x86Core>(0u, op1.dtyp).apply { value(core, acc.value(core) - temp) },
            acc,
            op1,
            isSubtract = true,
        )

        if (core.cpu.flags.zf) {
            op1.value(core, op2)
        } else {
            if (op1.dtyp == Datatype.DWORD) {
                core.cpu.regs.gpr(x86GPR.RAX, Datatype.QWORD).value = temp[op1.dtyp.msb..op1.dtyp.lsb]
            } else {
                core.cpu.regs.gpr(x86GPR.RAX, Datatype.QWORD).value = fullAcc.insert(
                    temp,
                    op1.dtyp.msb..op1.dtyp.lsb,
                )
            }
            op1.value(core, temp)
        }
    }
}