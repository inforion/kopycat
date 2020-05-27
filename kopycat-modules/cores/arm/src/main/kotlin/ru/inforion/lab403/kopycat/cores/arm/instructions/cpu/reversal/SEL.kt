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
package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.reversal

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class SEL(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rn: ARMRegister,
          val rd: ARMRegister,
          val rm: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rd, rm) {
    override val mnem = "SEL$mcnd"

    override fun execute() {
        rd.bits(core,7..0,   if(core.cpu.status.ge[0] == 1L) rn.bits(core,7..0)   else rm.bits(core,7..0))
        rd.bits(core,15..8,  if(core.cpu.status.ge[1] == 1L) rn.bits(core,15..8)  else rm.bits(core,15..8))
        rd.bits(core,23..16, if(core.cpu.status.ge[2] == 1L) rn.bits(core,23..16) else rm.bits(core,23..16))
        rd.bits(core,31..24, if(core.cpu.status.ge[3] == 1L) rn.bits(core,31..24) else rm.bits(core,31..24))
    }
}