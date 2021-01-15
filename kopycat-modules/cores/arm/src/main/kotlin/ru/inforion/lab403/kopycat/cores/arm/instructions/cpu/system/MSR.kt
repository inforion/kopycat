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
package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.system

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


// See B9.3.11
// TODO: collapse with MSRsl
class MSR(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val imm32: Immediate<AARMCore>,
          val mask: Int,
          val write_spr: Boolean):
        AARMInstruction(cpu, Type.VOID, cond, opcode, imm32) {
    override val mnem = "MSR$mcnd"

    override fun execute() {
        if (write_spr)
            core.cpu.SPSRWriteByInstr(imm32.value, mask)
        else {
            // Does not affect execution state bits other than E
            core.cpu.CPSRWriteByInstr(imm32.value, mask, false)
            if (core.cpu.sregs.cpsr.m == 0b11010L && core.cpu.sregs.cpsr.j && core.cpu.sregs.cpsr.t)
                throw ARMHardwareException.Unpredictable
        }
    }
}