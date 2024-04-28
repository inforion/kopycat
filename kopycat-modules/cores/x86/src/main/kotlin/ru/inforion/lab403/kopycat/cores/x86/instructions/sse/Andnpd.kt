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

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Andnpd(
    core: x86Core,
    opcode: ByteArray,
    prefs: Prefixes,
    vararg operands: AOperand<x86Core>
) : ASSEInstruction(core, opcode, prefs, *operands) {

    override val mnem = if (prefs.operandOverride) "andnpd" else "andnps"

    override fun executeSSEInstruction() {
        // andnpd:
        // DEST[63:0] := (NOT(DEST[63:0])) BITWISE AND SRC[63:0]
        // DEST[127:64] := (NOT(DEST[127:64])) BITWISE AND SRC[127:64]
        // DEST[MAXVL-1:128] (Unmodified)

        // andnps:
        // DEST[31:0] := (NOT(DEST[31:0])) BITWISE AND SRC[31:0]
        // DEST[63:32] := (NOT(DEST[63:32])) BITWISE AND SRC[63:32]
        // DEST[95:64] := (NOT(DEST[95:64])) BITWISE AND SRC[95:64]
        // DEST[127:96] := (NOT(DEST[127:96])) BITWISE AND SRC[127:96]

        val a1 = op1.extValue(core)
        val a2 = op2.extValue(core)
        op1.extValue(core, a1[127..0].inv() and a2[127..0])
    }
}
