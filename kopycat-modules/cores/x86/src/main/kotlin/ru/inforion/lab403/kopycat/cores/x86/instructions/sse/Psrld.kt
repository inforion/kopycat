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

class Psrld(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>) :
    ASSEInstruction(core, opcode, prefs, *operands) {

    override val mnem = "psrld"

    override fun executeSSEInstruction() {
        val count = if (op2.dtyp == Datatype.XMMWORD) {
            op2.extValue(core)[63..0].ulong
        } else {
            op2.value(core)
        }

        if (count > 31uL) {
            if (op1.dtyp == Datatype.XMMWORD) {
                op1.extValue(core, BigInteger.ZERO)
            } else {
                op1.value(core, 0uL)
            }
            return
        }

        val src = if (op1.dtyp == Datatype.XMMWORD) {
            op1.extValue(core)
        } else {
            op1.value(core).bigint
        }

        var result = BigInteger.ZERO

        for (i in 0 until op1.dtyp.bytes / 4) {
            val range = (i + 1) * 32 - 1..i * 32
            result = result.insert(src[range] ushr count.int, range)
        }

        if (op1.dtyp == Datatype.XMMWORD) {
            op1.extValue(core, result)
        } else {
            op1.value(core, result.ulong)
        }
    }
}
