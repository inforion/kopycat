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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Psrad(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>) :
    ASSEInstruction(core, opcode, prefs, *operands) {

    override val mnem = "psrad"

    private fun ULong.rotate(n: Int): ULong = if (this[31].truth) {
        val ones = if (n == 32) 0xFFFF_FFFFuL else (1uL shl n) - 1uL
        (this ushr n) or (ones shl (32 - n))
    } else {
        this ushr n
    }

    override fun executeSSEInstruction() {
        if (op1.dtyp.bits == 64) {
            // NP 0F 72 /4 ib1 PSRAD mm, imm8
            // NP 0F E2 /r1 PSRAD mm, mm/m64

            val a1 = op1.value(core)
            val a2 = op2.value(core).coerceAtMost(32uL).int

            op1.value(
                core,
                0uL
                    .insert(a1[31..0].rotate(a2), 31..0)
                    .insert(a1[63..32].rotate(a2), 63..32),
            )
        } else {
            // 66 0F E2 /r PSRAD xmm1, xmm2/m128
            // 66 0F 72 /4 ib PSRAD xmm1, imm8

            val a1 = op1.extValue(core)
            val a2 = if (op2.dtyp == Datatype.XMMWORD) {
                op2.extValue(core).coerceAtMost(32.bigint).int
            } else {
                op2.value(core).coerceAtMost(32uL).int
            }

            op1.extValue(
                core,
                0.bigint
                    .insert(a1[31..0].ulong.rotate(a2), 31..0)
                    .insert(a1[63..32].ulong.rotate(a2), 63..32)
                    .insert(a1[95..64].ulong.rotate(a2), 95..64)
                    .insert(a1[127..96].ulong.rotate(a2), 127..96),
            )
        }
    }
}
