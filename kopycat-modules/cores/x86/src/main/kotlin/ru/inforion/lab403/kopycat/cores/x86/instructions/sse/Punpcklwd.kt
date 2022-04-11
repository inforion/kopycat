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
package ru.inforion.lab403.kopycat.cores.x86.instructions.sse

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.proposal.get
import ru.inforion.lab403.common.proposal.insert
import ru.inforion.lab403.common.proposal.set
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger


class Punpcklwd(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>) :
    AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {

    override val mnem = "punpcklwd"

    override fun execute() {
        val dest = op1.extValue(core)
        val src = op2.extValue(core)

        // INTERLEAVE_WORDS (DEST, SRC)
        var result = BigInteger.ZERO
        (0 until (op2.dtyp.bytes / 4)).forEach {
            // DST and SRC word ranges
            val lsbFrom = it*16
            val msbFrom = (it + 1)*16 - 1
            val rangeFrom = msbFrom..lsbFrom

            // Result DST insert range
            val lsb1 = (2*it)*16
            val msb1 = (2*it + 1)*16 - 1
            val range1 = msb1..lsb1

            // Result SRC insert range
            val lsb2 = (2*it + 1)*16
            val msb2 = (2*it + 2)*16 - 1
            val range2 = msb2..lsb2

            result = result.insert(dest[rangeFrom], range1).insert(src[rangeFrom], range2)
        }

        op1.extValue(core, result)
    }
}