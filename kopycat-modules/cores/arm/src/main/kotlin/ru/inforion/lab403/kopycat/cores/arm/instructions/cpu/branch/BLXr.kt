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
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet.ARM



class BLXr(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val rm: ARMRegister,
           size: Int): AARMInstruction(cpu, Type.VOID, cond, opcode, rm, size = size) {
    override val mnem = "BLX$mcnd"

    override fun execute() {
        val pc = core.cpu.pc
        val target = rm.value(core)
        if (core.cpu.CurrentInstrSet() == ARM) {
            val nextInstrAddr = pc - 4
            core.cpu.regs.lr.value = nextInstrAddr
        } else {
            val nextInstrAddr = pc - 2
            core.cpu.regs.lr.value = cat(nextInstrAddr[31..1], 1, 0)
        }
        core.cpu.BXWritePC(target)
    }
}