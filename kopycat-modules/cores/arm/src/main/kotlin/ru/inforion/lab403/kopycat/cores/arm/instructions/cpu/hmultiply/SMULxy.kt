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
package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.hmultiply

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.SInt
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class SMULxy(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val rd: ARMRegister,
             val ra: ARMRegister,
             val rm: ARMRegister,
             val rn: ARMRegister,
             private val nHigh: Boolean,
             private val mHigh: Boolean):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, rm, rn) {
    override val mnem = "SMUL${if(mHigh) "T" else "B"}${if(mHigh) "T" else "B"}"

    override fun execute() {
        val operand1 = if(nHigh) rn.ssext(core)[31..16] else rn.ssext(core)[15..0]
        val operand2 = if(mHigh) rm.ssext(core)[31..16] else rm.ssext(core)[15..0]
        val result = SInt(operand1, 16) * SInt(operand2, 16)
        rd.value(core, result[31..0])
    }
}