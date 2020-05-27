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
package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.branch

import ru.inforion.lab403.common.extensions.cat
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.Align
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet.ARM
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet.CURRENT



class BLXi(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val imm32: Immediate<AARMCore>,
           private val targetInstrSet: AARMCore.InstructionSet,
           size: Int = 4):
        AARMInstruction(cpu, Type.COND_JUMP, cond, opcode, imm32, size = size) {

    override val mnem = "BL${if (targetInstrSet != CURRENT) "X" else ""}$mcnd"

    override fun execute() {
        val pc = core.cpu.pc

        if (core.cpu.CurrentInstrSet() == ARM) {
            core.cpu.regs.lr.value = pc - 4
        } else {
            core.cpu.regs.lr.value = cat(pc[31..1], 1, 0)
        }

        val targetAddress = if (targetInstrSet == ARM) {
            Align(pc, 4) + imm32.value
        } else {
            pc + imm32.value
        }

        core.cpu.SelectInstrSet(targetInstrSet)
        core.cpu.BranchWritePC(targetAddress)
    }
}