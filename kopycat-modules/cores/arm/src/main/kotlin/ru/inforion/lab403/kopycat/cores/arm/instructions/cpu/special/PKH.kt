/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class PKH(cpu: AARMCore,
          opcode: ULong,
          cond: Condition,
          val rn: ARMRegister,
          val rd: ARMRegister,
          private val shiftT: SRType,
          private val shiftN: ULong,
          private val tbForm: Boolean,
          val rm: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rd, rm) {
    override val mnem = "PKH$mcnd"

    override fun execute() {
        val operand2 = Shift(rm.value(core), 32, shiftT, shiftN.int, core.cpu.flags.c.int)
        rd.bits(core,15..0,  if(tbForm) operand2[15..0] else rn.bits(core, 15..0))
        rd.bits(core,31..16, if(tbForm) rn.bits(core, 31..16) else operand2[31..16])
    }
}