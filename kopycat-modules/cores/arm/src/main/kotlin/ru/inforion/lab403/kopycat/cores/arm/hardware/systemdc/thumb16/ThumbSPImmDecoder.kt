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

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

object ThumbSPImmDecoder {
    class T1(cpu: AARMCore,
             val constructor: (
                     cpu: AARMCore,
                     opcode: Long,
                     cond: Condition,
                     setFlags: Boolean,
                     rn: ARMRegister,
                     rd: ARMRegister,
                     imm32: Immediate<AARMCore>,
                     size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val setFlags = false
            val rd = gpr(data[10..8].asInt)
            val imm32 = Immediate<AARMCore>(data[7..0] shl 2)
            return constructor(core, data, Condition.AL, setFlags, rd, gpr(13), imm32, 2)
        }
    }
    class T2(cpu: AARMCore,
             val constructor: (
                     cpu: AARMCore,
                     opcode: Long,
                     cond: Condition,
                     setFlags: Boolean,
                     rn: ARMRegister,
                     rd: ARMRegister,
                     imm32: Immediate<AARMCore>,
                     size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val setFlags = false
            val rd = gpr(13)
            val imm32 = Immediate<AARMCore>(data[6..0] shl 2)
            return constructor(core, data, Condition.AL, setFlags, rd, rd, imm32, 2)
        }
    }
}
