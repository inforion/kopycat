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
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger

class Subpd(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>) :
    ASSEInstruction(core, opcode, prefs, *operands) {

    override val mnem = "subpd"

    override fun executeSSEInstruction() {
        val a1 = op1.extValue(core)
        val a2 = op2.extValue(core)

        // DEST[63:0] := DEST[63:0] - SRC[63:0]
        // DEST[127:64] := DEST[127:64] - SRC[127:64]

        op1.extValue(
            core,
            (0 until 2).asIterable().fold(BigInteger.ZERO) { acc, i ->
                val range = 64 * (i + 1) - 1..64 * i
                acc.insert((a1[range].ulong.ieee754() - a2[range].ulong.ieee754()).ieee754AsUnsigned(), range)
            },
        )
    }
}