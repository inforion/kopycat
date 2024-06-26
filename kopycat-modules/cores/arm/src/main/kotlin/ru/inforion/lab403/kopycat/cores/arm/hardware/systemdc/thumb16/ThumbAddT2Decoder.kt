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
package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.thumb16

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.SRType.SRType_LSL
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ThumbAddT2Decoder(cpu: AARMCore,
                        val constructor: (
                                cpu: AARMCore,
                                opcode: ULong,
                                cond: Condition,
                                setFlags: Boolean,
                                rd: ARMRegister,
                                rn: ARMRegister,
                                rm: ARMRegister,
                                shiftN: Int,
                                shiftT: SRType,
                                size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: ULong): AARMInstruction {
        val dn = data[7]
        val n = ((dn shl 3) + data[2..0]).int
        val m = data[6..3].int
        val rn = gpr(n)
        val rd = gpr(n)
        val rm = gpr(m)
        if (n == 15 && m == 15) throw Unpredictable
        if (n == 15 && core.cpu.InITBlock() && !core.cpu.LastInITBlock()) throw Unpredictable
        return constructor(core, data, Condition.AL, false, rd, rn, rm, 0, SRType_LSL, 2)
    }
}