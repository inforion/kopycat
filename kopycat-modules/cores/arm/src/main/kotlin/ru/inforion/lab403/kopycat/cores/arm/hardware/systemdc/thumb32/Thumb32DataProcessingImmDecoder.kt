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
package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb32

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.ThumbExpandImm
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class Thumb32DataProcessingImmDecoder(
        cpu: AARMCore,
        val constructor: (
                cpu: AARMCore,
                opcode: ULong,
                cond: Condition,
                setFlags: Boolean,
                rd: ARMRegister,
                rn: ARMRegister,
                imm32:Immediate<AARMCore>,
                size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: ULong): AARMInstruction {
        val cond = find { it.opcode == data[31..28].int } ?: Condition.AL
        val rd = gpr(data[11..8].int)
        val rn = gpr(data[19..16].int)
        val setFlags = data[20] == 1uL
        val i = data[26]
        val imm3 = data[14..12]
        val imm8 = data[7..0]
        val imm32 = Immediate<AARMCore>(ThumbExpandImm((i shl 11) + (imm3 shl 8) + imm8))
        if (rd.desc.id in 15..13 || rn.desc.id in 15..13) throw Unpredictable
        return constructor(core, data, cond, setFlags, rd, rn, imm32, 4)
    }
}