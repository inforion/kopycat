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
package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.special

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


// See A8.8.111
class MSR(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val imm32: Immediate<AARMCore>,
          mask: Int,
          write_spr: Boolean):
        AARMInstruction(cpu, Type.VOID, cond, opcode, imm32) {
    override val mnem = "MSR$mcnd"

    val writeNZCVQ = mask[3].toBool()
    val writeG = mask[2].toBool()

    override fun execute() {
        if(writeNZCVQ){
            core.cpu.flags.n = imm32.value[31] == 1L
            core.cpu.flags.z = imm32.value[30] == 1L
            core.cpu.flags.c = imm32.value[29] == 1L
            core.cpu.flags.v = imm32.value[28] == 1L
            core.cpu.status.q = imm32.value[27] == 1L
        }
        if(writeG)
            core.cpu.status.ge = imm32.value[19..16]
    }
}