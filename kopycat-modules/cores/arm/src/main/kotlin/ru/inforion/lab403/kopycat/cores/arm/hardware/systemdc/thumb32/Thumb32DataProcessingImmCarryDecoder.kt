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
package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb32

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.arm.ThumbExpandImm_C
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class Thumb32DataProcessingImmCarryDecoder(
        cpu: AARMCore,
        val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                setFlags: Boolean,
                rd: ARMRegister,
                rn: ARMRegister,
                imm32: Immediate<AARMCore>,
                carry: Boolean
        ) -> AARMInstruction
) : ADecoder<AARMInstruction>(cpu) {

    override fun decode(data: Long): AARMInstruction {
        val cond = cond(data)

        val i = data[26]
        val setFlags = data[20].toBool()
        val rn = gpr(data[19..16].asInt)
        val imm3 = data[14..12]
        val rd = gpr(data[11..8].asInt)
        val imm8 = data[7..0]

//        if (rd.reg in 15..13) throw ARMHardwareException.Unpredictable
        if ((rd.desc.id == 15 && !setFlags) || rd.desc.id == 13 || rn.desc.id in 15..13) throw Unpredictable

        val (imm32val, carry) = ThumbExpandImm_C((i shl 11) + (imm3 shl 8) + imm8, core.cpu.flags.c.asInt)
        val imm32 = Immediate<AARMCore>(imm32val)

        return constructor(core, data, cond, setFlags, rd, rn, imm32, carry.toBool())
    }
}