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
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger

class Psubusx(
    core: x86Core,
    opcode: ByteArray,
    prefs: Prefixes,
    private val b: Boolean,
    vararg operands: AOperand<x86Core>
): ASSEInstruction(core, opcode, prefs, *operands) {
    override val mnem = "psubus" + if (b) "b" else "w"

    private fun saturateToUnsignedX(l: BigInteger, r: BigInteger) = if (r > l) BigInteger.ZERO else l - r

    override fun executeSSEInstruction() {
        val a1 = when (op2.dtyp) {
            Datatype.XMMWORD -> op1.extValue(core)
            else -> op1.value(core).bigint
        }

        val a2 = when (op2.dtyp) {
            Datatype.XMMWORD -> op2.extValue(core)
            else -> op2.value(core).bigint
        }

        val l = if (b) 8 else 16
        val result = (0 until op2.dtyp.bytes).fold(BigInteger.ZERO) { acc, i ->
            val range = (i + 1) * l - 1..i * l
            acc.insert(saturateToUnsignedX(a1[range], a2[range]), range)
        }

        when (op2.dtyp) {
            Datatype.XMMWORD -> op1.extValue(core, result)
            else -> op1.value(core, result.ulong)
        }
    }
}
