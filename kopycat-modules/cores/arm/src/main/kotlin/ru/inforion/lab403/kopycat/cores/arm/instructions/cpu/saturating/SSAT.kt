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
package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.saturating

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.arm.SInt
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift
import ru.inforion.lab403.kopycat.cores.arm.SignedSatQ
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class SSAT(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val rd: ARMRegister,
           private val shiftT: SRType,
           private val shiftN: Long,
           private val saturateTo: Immediate<AARMCore>,
           val rn: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, saturateTo, rn) {
    override val mnem = "SSAT$mcnd"

    override fun execute() {
        val operand = Shift(rn.value(core), 32, shiftT, shiftN.asInt, core.cpu.flags.c.asInt)
        val (result, sat) = SignedSatQ(SInt(operand, 32).asInt, saturateTo.value.asInt)
        rd.value(core, signext(result.asLong, saturateTo.value.asInt).asLong)
        if(sat) FlagProcessor.processSatFlag(core)
    }
}