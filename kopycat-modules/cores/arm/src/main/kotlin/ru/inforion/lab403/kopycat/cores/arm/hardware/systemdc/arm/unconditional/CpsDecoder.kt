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
package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.unconditional

import ru.inforion.lab403.common.extensions.truth
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class CpsDecoder (cpu: AARMCore,
                       private val constructor: (
                               cpu: AARMCore,
                               opcode: ULong,
                               cond: Condition,
                               enable: Boolean,
                               disable: Boolean,
                               changemode: Boolean,
                               mode: ULong,
                               affectA: Boolean,
                               affectI: Boolean,
                               affectF: Boolean) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {

    override fun decode(data: ULong): AARMInstruction {
        val mode = data[4..0]
        val imod = data[19..18].int
        val aif = data[8..6]
        val m = data[17].truth

        if (mode != 0uL && !m) throw ARMHardwareException.Unpredictable
        if ((imod[1] == 1 && aif == 0uL) || (imod[1] == 0 && aif != 0uL)) throw ARMHardwareException.Unpredictable
        val enable = imod == 0b10
        val disable = imod == 0b11
        val changemode = m
        val affectA = data[8].truth
        val affectI = data[7].truth
        val affectF = data[6].truth
        if ((imod == 0b00 && !m) || imod == 0b01) throw ARMHardwareException.Unpredictable

        return constructor(core, data, Condition.AL, enable, disable, changemode, mode, affectA, affectI, affectF)
    }
}