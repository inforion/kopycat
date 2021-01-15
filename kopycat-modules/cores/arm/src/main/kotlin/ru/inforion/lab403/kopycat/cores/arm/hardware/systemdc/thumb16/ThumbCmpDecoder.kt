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
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.SRType.SRType_LSL
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

object ThumbCmpDecoder {
    class ImmT1(cpu: AARMCore,
                val constructor: (
                        cpu: AARMCore,
                        opcode: Long,
                        cond: Condition,
                        rn: ARMRegister,
                        imm32: Immediate<AARMCore>,
                        size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val rn = gpr(data[10..8].asInt)
            val imm32 = Immediate<AARMCore>(data[7..0])
            return constructor(core, data, Condition.AL, rn, imm32, 2)
        }
    }
    class RegT1(cpu: AARMCore,
                val constructor: (
                        cpu: AARMCore,
                        opcode: Long,
                        cond: Condition,
                        setFlags: Boolean,
                        rd: ARMRegister,
                        rn: ARMRegister,
                        rm: ARMRegister,
                        shiftT: SRType,
                        shiftN: Int,
                        size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val rn = gpr(data[2..0].asInt)
            val rm = gpr(data[5..3].asInt)
            return constructor(core, data, Condition.AL, false, rn, rn, rm, SRType_LSL, 0, 2)
        }
    }
    class RegT2(cpu: AARMCore,
                val constructor: (
                        cpu: AARMCore,
                        opcode: Long,
                        cond: Condition,
                        setFlags: Boolean,
                        rd: ARMRegister,
                        rn: ARMRegister,
                        rm: ARMRegister,
                        shiftT: SRType,
                        shiftN: Int,
                        size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val m = data[6..3].asInt
            val n = ((data[7] shl 3) + data[2..0]).asInt
            val rn = gpr(n)
            val rm = gpr(m)
            if(n < 8 && m < 8) throw Unpredictable
            if(n == 15 || m == 15) throw Unpredictable
            return constructor(core, data, Condition.AL, false, rn, rn, rm, SRType_LSL, 0, 2)
        }
    }
}