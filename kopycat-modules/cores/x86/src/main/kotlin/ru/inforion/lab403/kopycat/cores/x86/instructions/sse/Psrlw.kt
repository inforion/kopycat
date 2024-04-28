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

class Psrlw(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>) :
    ASSEInstruction(core, opcode, prefs, *operands) {

    override val mnem = "psrlw"

    override fun executeSSEInstruction() {
        val a1 = op1.extValue(core)
        val a2 = op2.value(core).coerceAtMost(32uL).int

        // 66 0F 71 /2 ib PSRLW xmm1, imm8

        //DEST[127:0] := LOGICAL_RIGHT_SHIFT_WORDS(DEST, imm8)
        //DEST[MAXVL-1:128] (Unmodified)

        op1.extValue(
            core,
            0.bigint
                .insert(a1[15..0].ulong ushr a2, 15..0)
                .insert(a1[31..16].ulong ushr a2, 31..16)

                .insert(a1[47..32].ulong ushr a2, 47..32)
                .insert(a1[63..48].ulong ushr a2, 63..48)

                .insert(a1[79..64].ulong ushr a2, 79..64)
                .insert(a1[95..80].ulong ushr a2, 95..80)

                .insert(a1[111..96].ulong ushr a2, 111..96)
                .insert(a1[127..112].ulong ushr a2, 127..112),
        )
    }
}