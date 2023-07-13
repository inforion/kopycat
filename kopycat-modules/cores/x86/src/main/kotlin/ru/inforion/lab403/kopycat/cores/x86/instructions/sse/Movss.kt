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

import ru.inforion.lab403.common.extensions.bigint
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Movss(
    core: x86Core,
    opcode: ByteArray,
    prefs: Prefixes,
    vararg operands: AOperand<x86Core>
) : ASSEInstruction(core, opcode, prefs, *operands) {

    override val mnem = "movss"

    override fun executeSSEInstruction() {
        // F3 0F 10 /r MOVSS xmm1, xmm2
        // F3 0F 10 /r MOVSS xmm1, m32
        // F3 0F 11 /r MOVSS xmm2/m32, xmm1

        if (op1.type == AOperand.Type.REG && op2.type == AOperand.Type.REG) {
            // MOVSS XMM1, XMM2

            // DEST[31:0] := SRC[31:0]
            // DEST[MAXVL-1:32] (Unmodified)

            val a1 = op1.extValue(core)
            val a2 = op2.extValue(core)
            op1.extValue(
                core,
                a1.insert(
                    a2[31..0],
                    31..0,
                )
            )
        } else if (op1.type == AOperand.Type.REG && op2.type != AOperand.Type.REG) {
            // MOVSS XMM1, m32

            // DEST[31:0] := SRC[31:0]
            // DEST[127:32] := 0
            // DEST[MAXVL-1:128] (Unmodified)

            val a2 = op2.value(core)
            op1.extValue(core, a2.bigint)
        } else {
            // MOVSS m32, xmm1

            // DEST[31:0] := SRC[31:0]

            val a2 = op2.extValue(core)
            op1.value(core, a2[31..0].ulong)
        }
    }
}
