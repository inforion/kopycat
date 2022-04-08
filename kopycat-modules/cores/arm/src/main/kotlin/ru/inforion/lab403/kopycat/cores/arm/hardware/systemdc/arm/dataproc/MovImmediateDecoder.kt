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
package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.dataproc

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.arm.ARMExpandImm_C
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.logic.immediate.MOVi
import ru.inforion.lab403.kopycat.cores.arm.operands.isProgramCounter
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


object MovImmediateDecoder {
    class A1(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: ULong): AARMInstruction {
            val cond = cond(data)
            val rd = gpr(data[15..12].int)
            val (imm32, carry) = ARMExpandImm_C(data[11..0], core.cpu.flags.c.int)
            val imm = imm(imm32, true)
            val setflags = data[20] == 1uL
            return MOVi(core, data, cond, setflags, carry.truth, rd, imm, 4)
        }
    }

    class A2(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: ULong): AARMInstruction {
            val cond = cond(data)
            val rd = gpr(data[15..12].int)
            val imm4 = data[19..16]
            val imm12 = data[11..0]
            val imm = imm(cat(imm4.int, imm12.int, 11).ulong_z, true)
            if (rd.isProgramCounter(core)) throw ARMHardwareException.Unpredictable
            return MOVi(core, data, cond, false, false, rd, imm, 4)
        }
    }
}