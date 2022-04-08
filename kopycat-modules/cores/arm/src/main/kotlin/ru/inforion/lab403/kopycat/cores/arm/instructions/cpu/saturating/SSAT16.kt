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
package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.saturating

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.arm.SInt
import ru.inforion.lab403.kopycat.cores.arm.SignedSatQ
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class SSAT16(cpu: AARMCore,
             opcode: ULong,
             cond: Condition,
             val rd: ARMRegister,
             private val saturateTo: Immediate<AARMCore>,
             val rn: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, saturateTo, rn) {
    override val mnem = "SSAT16$mcnd"

    override fun execute() {
        val (result1, sat1) = SignedSatQ(SInt(rn.bits(core, 15..0), 32).int, saturateTo.value.int)
        val (result2, sat2) = SignedSatQ(SInt(rn.bits(core, 31..16), 32).int, saturateTo.value.int)
        rd.bits(core, 15..0, result1.ulong_z.signextRenameMeAfter( 15))
        rd.bits(core, 31..16, result2.ulong_z.signextRenameMeAfter(15))
        if(sat1 || sat2) FlagProcessor.processSatFlag(core)
    }
}