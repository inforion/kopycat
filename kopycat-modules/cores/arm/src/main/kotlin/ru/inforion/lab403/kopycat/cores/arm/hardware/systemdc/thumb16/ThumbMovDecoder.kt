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
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

object ThumbMovDecoder {
    class ImmT1(cpu: AARMCore,
                private val constructor: (
                        cpu: AARMCore,
                        opcode: Long,
                        cond: Condition,
                        setFlags: Boolean,
                        carry: Boolean,
                        rd: ARMRegister,
                        imm32: Immediate<AARMCore>,
                        size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val rd = gpr(data[10..8].asInt)
            val setFlag = !core.cpu.InITBlock()
            val imm32 = Immediate<AARMCore>(data[7..0])
            return constructor(core, data, Condition.AL, setFlag, core.cpu.flags.c, rd, imm32, 2)
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
            val m = data[6..3].asInt
            val d = ((data[7] shl 3) + data[2..0]).asInt
            val rd = gpr(d)
            val rm = gpr(m)
            if (d == 15 && core.cpu.InITBlock() && !core.cpu.LastInITBlock()) throw Unpredictable
            return constructor(core, data, Condition.AL, false, rd, rm, rm, SRType.SRType_LSL, 0, 2)
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
            val rd = gpr(data[2..0].asInt)
            val rm = gpr(data[5..3].asInt)
            if(core.cpu.InITBlock()) throw Unpredictable
            return constructor(core, data, Condition.AL, true, rd, rd, rm, SRType.SRType_LSL, 0, 2)
        }
    }
}