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
package ru.inforion.lab403.kopycat.cores.msp430.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.msp430.constructor
import ru.inforion.lab403.kopycat.cores.msp430.enums.MSP430GPR
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core




class FormatII(core: MSP430Core, val construct:  constructor) : MSP430Decoder(core) {

    override fun decode(s: Long): AMSP430Instruction {
        val aSrc = s[5..4].asInt
        val dtype = if (s[6] == 1L) BYTE else WORD

        val regInd = s[3..0].asInt
        val nextWord = s[31..16]

        val isImm = ((aSrc == 0b01) and (regInd != MSP430GPR.r3.id)) or ((aSrc == 0b11) and (regInd == MSP430GPR.r0.id))
        val size = if (isImm) 4 else 2

        val op = decodeFirstOp(aSrc, regInd, nextWord, dtype, size.toLong())


        return construct(core, size, arrayOf(op))
    }

}