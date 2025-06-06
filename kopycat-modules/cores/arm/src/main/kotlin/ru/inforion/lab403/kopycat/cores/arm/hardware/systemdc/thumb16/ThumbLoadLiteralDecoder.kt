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
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition.UN
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload.LDRL
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ThumbLoadLiteralDecoder(cpu: AARMCore) : ADecoder<AARMInstruction>(cpu) {
    private fun decodeT1(data: ULong): AARMInstruction {
        val rt = gpr(data[10..8].int)
        val imm32 = imm(data[7..0] shl 2, false)
        return LDRL(core, data, UN, true, rt, imm32, 2)
    }

    private fun decodeT2(data: ULong): AARMInstruction = TODO()

    override fun decode(data: ULong): AARMInstruction {
        return if (data[15..11] == 0b01001uL) {
            decodeT1(data)
        } else {
            decodeT2(data)
        }
    }
}