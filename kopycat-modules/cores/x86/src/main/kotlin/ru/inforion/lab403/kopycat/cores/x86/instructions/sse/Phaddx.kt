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
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger

class Phaddx(
    core: x86Core,
    opcode: ByteArray,
    prefs: Prefixes,
    private val typ: Datatype,
    vararg operands: AOperand<x86Core>
) : ASSEInstruction(core, opcode, prefs, *operands) {
    override val mnem = "phadd" + when (typ) {
        Datatype.WORD -> "w"
        Datatype.DWORD -> "d"
        else -> throw NotImplementedError("phadd* variant")
    }

    override fun executeSSEInstruction() {
        // NP 0F 38 01 /r1 PHADDW mm1, mm2/m64
        // 66 0F 38 01 /r PHADDW xmm1, xmm2/m128
        // NP 0F 38 02 /r PHADDD mm1, mm2/m64
        // 66 0F 38 02 /r PHADDD xmm1, xmm2/m128

        val (src, dest) = if (op1.dtyp == Datatype.MMXWORD) {
            op1.value(core).bigint to op2.value(core).bigint
        } else {
            op1.extValue(core) to op2.extValue(core)
        }

        val typbits = typ.bits
        val opbits = op1.dtyp.bits

        val result = (0 until opbits / typbits).asIterable().fold(BigInteger.ZERO) { acc, i ->
            val rangeL = (typbits * 2 * (i + 1) - 1) % opbits..(typbits * (2 * i + 1)) % opbits
            val rangeR = (typbits + typbits * 2 * i - 1) % opbits..(typbits * 2 * i) % opbits
            val rangeDest = (i + 1) * typbits - 1..i * typbits
            val a = if (i < opbits / typbits / 2) src else dest
            acc.insert(a[rangeL] + a[rangeR], rangeDest)
        }

        if (op1.dtyp == Datatype.MMXWORD) {
            op1.value(core, result[63..0].ulong)
        } else {
            op1.extValue(core, result)
        }
    }
}
