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
package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.media

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.UInt
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import java.lang.Math.abs



class USADA8(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val rd: ARMRegister,
             val ra: ARMRegister,
             val rm: ARMRegister,
             val rn: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rd, ra, rm, rn) {

    override val mnem = "USADA8$mcnd"

    override fun execute() {
        TODO()
        val absDiff1 = abs(UInt(rn.value(core)[7..0], 32) - UInt(rm.value(core)[7..0], 32))
        val absDiff2 = abs(UInt(rn.value(core)[15..8], 32) - UInt(rm.value(core)[15..8], 32))
        val absDiff3 = abs(UInt(rn.value(core)[23..16], 32) - UInt(rm.value(core)[23..16], 32))
        val absDiff4 = abs(UInt(rn.value(core)[31..24], 32) - UInt(rm.value(core)[31..24], 32))
        val result = UInt(ra.value(core), 32) + absDiff1 + absDiff2 + absDiff3 + absDiff4

        rd.value(core, result[31..0])
    }
}
