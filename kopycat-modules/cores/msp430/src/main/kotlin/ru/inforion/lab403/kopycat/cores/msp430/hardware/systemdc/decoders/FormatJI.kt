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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.msp430.constructorCond
import ru.inforion.lab403.kopycat.cores.msp430.enums.Condition
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Immediate
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core



class FormatJI(core: MSP430Core, val construct:  constructorCond) : MSP430Decoder(core) {
    override fun decode(s: Long): AMSP430Instruction {
        val imm = MSP430Immediate(WORD, signext(s[9..0], 10).asLong, true)
        val cond = find<Condition> { it.opcode == s[12..10].asInt }
        return construct(core, 2, cond!!, arrayOf(imm))
    }
}