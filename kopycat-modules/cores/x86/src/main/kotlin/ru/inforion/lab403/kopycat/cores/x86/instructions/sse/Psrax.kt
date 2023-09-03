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
import java.math.BigInteger

class Psrax(
    core: x86Core,
    opcode: ByteArray,
    prefs: Prefixes,
    private val variant: Datatype,
    vararg operands: AOperand<x86Core>
) : ASSEInstruction(core, opcode, prefs, *operands) {
    override val mnem = "psra" + when (variant) {
        Datatype.WORD -> "w"
        else -> "d"
    }

    private fun ULong.rotate(n: Int) = if (n > variant.bits - 1 && this[variant.bits - 1].truth /* sign */) {
        0xFFFF_FFFFuL /* should be enough considering vpsraq is not implemented */
    } else {
        (this ushr n).signext(variant.bits - n - 1)
    }

    override fun executeSSEInstruction() {
        val a1 = op1.extValue(core)
        val a2 = when (op2.dtyp) {
            Datatype.XMMWORD -> op2.extValue(core).coerceAtMost(variant.bits.bigint).int
            else -> op2.value(core).coerceAtMost(variant.bits.ulong_z).int
        }

        var result = BigInteger.ZERO
        for (i in 0 until op1.dtyp.bits / variant.bits) {
            val range = (i + 1) * variant.bits - 1..i * variant.bits
            result = result.insert(a1[range].ulong.rotate(a2), range)
        }

        op1.extValue(core, result)
    }
}
