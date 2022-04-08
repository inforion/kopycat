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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Ret(core: x86Core, operand: AOperand<x86Core>, opcode: ByteArray, prefs: Prefixes, val isFar: Boolean):
        AX86Instruction(core, Type.RET, opcode, prefs, operand) {
    override val mnem = "ret"

    override fun execute() {
        if (!isFar) {
            val tmp = x86utils.pop(core, prefs.opsize, prefs, offset = op1.usext(core))
            val ip = core.cpu.regs.gpr(x86GPR.RIP, prefs.opsize)
            ip.value = tmp
        } else {
            val pe = core.cpu.cregs.cr0.pe
            val vm = core.cpu.flags.vm

            //real-address or virtual-8086 mode
            if (!pe || (pe && vm)) {
                val tmpip = x86utils.pop(core, prefs.opsize, prefs)
                val tmpcs = x86utils.pop(core, prefs.opsize, prefs) and 0xFFFFuL

                val ip = core.cpu.regs.gpr(x86GPR.RIP, prefs.opsize)

                core.cpu.sregs.cs.value = tmpcs

                when (prefs.opsize) {
                    DWORD -> ip.value = tmpip
                    WORD -> ip.value = tmpip and 0xFFFFuL
                    else -> error("Wrong operand size for real-address or virtual-8086 mode")
                }
            }

            if (pe && !vm) {
                // point shifted after CS is take from stack: DO NOT USE offset here!
                val tmpip = x86utils.pop(core, prefs.opsize, prefs)
                val tmpcs = x86utils.pop(core, prefs.opsize, prefs) and 0xFFFFuL
                // stack shift by immediate value
                val sp = core.cpu.regs.gpr(x86GPR.RSP, prefs.opsize)
                sp.value += op1.usext(core)
                val ip = core.cpu.regs.gpr(x86GPR.RIP, prefs.opsize)
                ip.value = tmpip
                core.cpu.sregs.cs.value = tmpcs
            }
        }
    }
}