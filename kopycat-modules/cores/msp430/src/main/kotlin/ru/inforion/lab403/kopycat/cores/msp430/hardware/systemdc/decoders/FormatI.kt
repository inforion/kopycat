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
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.WRITE
import ru.inforion.lab403.kopycat.cores.msp430.MSP430Operand
import ru.inforion.lab403.kopycat.cores.msp430.constructor
import ru.inforion.lab403.kopycat.cores.msp430.enums.MSP430GPR
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Displacement
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Immediate
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Memory
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Register
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core



class FormatI(core: MSP430Core, val construct:  constructor) : MSP430Decoder(core) {

    override fun decode(s: Long): AMSP430Instruction {
        val aSrc = s[5..4].asInt
        val aDst = s[7].asInt
        val dtype = if (s[6] == 1L) BYTE else WORD

        val src = s[11..8].asInt
        val dest = s[3..0].asInt
        val word1 = s[31..16]
        val word2 = s[47..32]

        val imm1 = MSP430Immediate(dtype, word1, true)
        val imm2 = MSP430Immediate(dtype, word2, true)

        val fstImm = ((aSrc == 0b01) and (src != MSP430GPR.r3.id)) or ((aSrc == 0b11) and (src == MSP430GPR.r0.id))
        val size = 2 + (if (fstImm) 2 else 0) + (if (aDst == 1) 2 else 0)

        val op1 = decodeFirstOp(aSrc, src, word1, dtype, size.toLong())

        val imm1PC = MSP430Immediate(dtype, word1 - size + Datatype.WORD.bytes, true)
        val imm2PC = MSP430Immediate(dtype, word2 - size + Datatype.WORD.bytes, true)

        val op2 : MSP430Operand = when(aDst) {
            0 -> MSP430Register.gpr(dtype, dest)
            1 -> {
                when (dest) {
                    0 -> MSP430Displacement(dtype, MSP430Register.gpr(WORD, dest), if (fstImm) imm2PC else imm1PC, WRITE, 0)
                    2 -> MSP430Memory(dtype, WRITE, if (fstImm) word2 else word1)
                    else -> MSP430Displacement(dtype, MSP430Register.gpr(WORD, dest), if (fstImm) imm2 else imm1, WRITE, 0)
                }


            }
            else -> throw GeneralException("Incorrect optype")
        }



        return construct(core, size, arrayOf(op1, op2))
    }
}