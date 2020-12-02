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
package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.exceptions

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.DecodeImmShift
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.exceptions.SUBPCLR
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


// See B9.3.20
object SubstractExceptionReturn {

    class A1(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            TODO("Not implemented")
        }
    }

    class A2(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
        override fun decode(data: Long): AARMInstruction {
            val cond = cond(data)
            val opc = data[24..21]
            val rn = gpr(data[19..16].toInt())
            val rm = gpr(data[3..0].toInt())
            val registerForm = true
            val type = data[6..5]
            val imm5 = data[11..7]
            val (shiftT, shiftN) = DecodeImmShift(type, imm5)
            return SUBPCLR(
                    core,
                    data,
                    cond,
                    opc,
                    rn,
                    rm,
                    registerForm,
                    shiftT,
                    shiftN.asInt,
                    imm(0L, false),
                    4
            )
        }
    }


}