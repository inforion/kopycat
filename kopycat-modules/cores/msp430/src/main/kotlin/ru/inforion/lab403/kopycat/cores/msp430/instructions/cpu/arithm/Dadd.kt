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
package ru.inforion.lab403.kopycat.cores.msp430.instructions.cpu.arithm

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.msp430.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.msp430.instructions.AMSP430Instruction
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Variable
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core


class Dadd(core: MSP430Core, size: Int, vararg operands: AOperand<MSP430Core>) :
    AMSP430Instruction(core, Type.VOID, size, *operands) {
    override val mnem = "dadd"

    private val result = MSP430Variable(op1.dtyp)

    override fun execute() {
        val valFir = op1.value(core)
        val valSec = op2.value(core)
        var valRes: ULong = 0u
        var carry = core.cpu.flags.c.ulong
        for (i in 0 until op1.dtyp.bytes) {
            val lsb = i * 4
            val msb = i + 3
            val sum = valFir[msb..lsb] + valSec[msb..lsb] + carry
            val part = if (sum[3..1] > 0b100u) {
                carry = 1u
                sum - 10u
            } else {
                carry = 0u
                sum
            }
            valRes = valRes.insert(part, msb..lsb)
        }
        result.value(core, valRes)
        FlagProcessor.processDaddFlag(core, result, carry.truth)
        op2.value(core, result)
    }
}