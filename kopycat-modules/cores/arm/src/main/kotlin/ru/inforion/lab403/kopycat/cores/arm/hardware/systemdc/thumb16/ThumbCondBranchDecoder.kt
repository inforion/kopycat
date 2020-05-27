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
package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

object ThumbCondBranchDecoder {
    class T1(cpu: AARMCore,
             private val constructor: (
                     cpu: AARMCore,
                     opcode: Long,
                     cond: Condition,
                     imm32: Immediate<AARMCore>,
                     size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            // to make cache accessible
            val cond = find<Condition> { it.opcode == data[11..8].asInt } ?: Condition.AL
            val imm32 = Immediate<AARMCore>(signext(data[7..0] shl 1, 9).asLong)
            return constructor(core, data, cond, imm32, 2)

//            val cond = find<Condition> { it.opcode == data[11..8].asInt } ?: Condition.AL
//            return if (core.cpu.ConditionPassed(cond)) {
//                val imm32 = Immediate<AARMCore>(signext(data[7..0] shl 1, 9).asLong)
//                if (core.cpu.InITBlock()) throw Unpredictable
//                constructor(core, data, cond, imm32, 2)
//            } else
//                NOP(core, data, cond, 2)
        }
    }

    class T2(cpu: AARMCore,
             private val constructor: (
                     cpu: AARMCore,
                     opcode: Long,
                     cond: Condition,
                     imm32: Immediate<AARMCore>,
                     size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val imm32 = Immediate<AARMCore>(signext(data[10..0] shl 1, 12).asLong)
            if(core.cpu.InITBlock() && !core.cpu.LastInITBlock()) throw Unpredictable
            return constructor(core, data, Condition.AL, imm32, 2)
        }
    }
}