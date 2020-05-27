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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.CTRLR.cr0
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRDW.esp
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.cs
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.eflags
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Ret(core: x86Core, operand: AOperand<x86Core>, opcode: ByteArray, prefs: Prefixes, val isFar: Boolean):
        AX86Instruction(core, Type.RET, opcode, prefs, operand) {
    override val mnem = "ret"

    override fun execute() {
        if (!isFar) {
            val tmp = x86utils.pop(core, prefs.opsize, prefs, offset = op1.ssext(core))
            val ip = x86Register.gpr(prefs.opsize, x86GPR.EIP)
            ip.value(core, tmp)
        } else {
            val pe = cr0.pe(core)
            val vm = eflags.vm(core)

            //real-address or virtual-8086 mode
            if (!pe || (pe && vm)) {
                TODO()
            }

            if (pe && !vm) {
                // point shifted after CS is take from stack: DO NOT USE offset here!
                val tmpip = x86utils.pop(core, prefs.opsize, prefs)
                val tmpcs = x86utils.pop(core,  prefs.opsize, prefs) and 0xFFFFL
                // stack shift by immediate value
                esp.plus(core, op1.ssext(core))
                val ip = x86Register.gpr(prefs.opsize, x86GPR.EIP)
                ip.value(core, tmpip)
                cs.value(core, tmpcs)
            }
        }
    }
}