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
package ru.inforion.lab403.kopycat.cores.x86.instructions.sse

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger

class Pshufb(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
    ASSEInstruction(core, opcode, prefs, *operands) {
    override val mnem = "pshufb"

    override fun executeSSEInstruction() {
        if (op1.dtyp.bits == 64) {
            // With 64 bit operands

            val dest = op1.value(core)
            val src = op2.value(core)
            var result = 0uL

            for (i in 0..7) {
                if (src[i * 8 + 7].untruth) {
                    val idx = src[i * 8 + 2 ..i * 8].int
                    result = result.insert(dest[idx * 8 + 7..idx * 8], i * 8 + 7..i * 8)
                }
            }

            op1.value(core, result)
        } else {
            // With 128 bit operands

            val dest = op1.extValue(core)
            val src = op2.extValue(core)
            var result = BigInteger.ZERO

            for (i in 0..15) {
                if (src[i * 8 + 7].untruth) {
                    val idx = src[i * 8 + 3 ..i * 8].int
                    result = result.insert(dest[idx * 8 + 7..idx * 8], i * 8 + 7..i * 8)
                }
            }

            op1.extValue(core, result)
        }
    }
}
