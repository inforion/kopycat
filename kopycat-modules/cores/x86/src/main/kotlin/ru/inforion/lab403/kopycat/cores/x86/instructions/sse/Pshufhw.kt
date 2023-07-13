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
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.ushr
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger

class Pshufhw(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>) :
    ASSEInstruction(core, opcode, prefs, *operands) {

    override val mnem = "pshufhw"

    override fun executeSSEInstruction() {
        val src = op2.extValue(core)
        val order = op3.value(core)

        var result = BigInteger.ZERO
        (4 until 8).forEach {
            val lsb = it * 16
            val msb = (it + 1) * 16 - 1
            val range = msb..lsb

            val lsbOrder = (it - 4) * 2
            val msbOrder = (it - 3) * 2 - 1
            val rangeOrder = msbOrder..lsbOrder

            val shiftSize = order[rangeOrder].int * 16
            result = result.insert((src ushr shiftSize)[79..64], range)
        }

        result = result.insert(src[63..0], 63..0)
        op1.extValue(core, result)
    }
}
