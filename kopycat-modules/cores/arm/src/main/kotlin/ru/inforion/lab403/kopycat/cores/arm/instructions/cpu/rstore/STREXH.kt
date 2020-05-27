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
package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



// See A8.8.215
class STREXH(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val rn: ARMRegister,
             val rd: ARMRegister,
             val rt: ARMRegister,
             val imm32: Immediate<AARMCore>):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rt, rn) {

    override val mnem = "STREXH$mcnd"

    override fun execute() {
        val address = rn.value(core)
        // TODO: Single core - no need
        if (/*ExclusiveMonitorsPass(address, 2)*/ true) {
            core.outb(address, rt.value(core) like Datatype.WORD)
            rd.value(core,0L)
        }
        else
            rd.value(core, 1L)
    }
}