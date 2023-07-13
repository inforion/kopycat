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
import ru.inforion.lab403.kopycat.cores.x86.enums.StringPrefix
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Cvttsx2si(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>) :
    ASSEInstruction(core, opcode, prefs, *operands) {

    private val string: StringPrefix

    init {
        string = prefs.string
        prefs.string = StringPrefix.NO
    }

    override val mnem = when (string) {
        StringPrefix.REPNZ -> "cvttsd2si"
        StringPrefix.REPZ -> "cvttss2si"
        else -> TODO("cvtts*2si variants")
    }

    override fun executeSSEInstruction() {
        // TODO: MXCSR

        val a2 = when (op2.dtyp) {
            Datatype.XMMWORD -> op2.extValue(core).ulong
            else -> op2.value(core)
        }

        val (truncated, infinite, outOfRange) = when (string) {
            StringPrefix.REPNZ -> {
                // cvttsd2si
                val f = kotlin.math.truncate(a2.ieee754())
                Triple(
                    f.ulong,
                    f.isInfinite() || f.isNaN(),
                    op1.dtyp == Datatype.DWORD && (f <= INT_MIN || f >= INT_MAX) ||
                            op1.dtyp == Datatype.QWORD && (f <= LONG_MIN || f >= LONG_MAX),
                )
            }
            StringPrefix.REPZ -> {
                // cvttss2si
                val f = kotlin.math.truncate(a2[31..0].uint.ieee754())
                Triple(
                    f.ulong,
                    f.isInfinite() || f.isNaN(),
                    op1.dtyp == Datatype.DWORD && (f <= INT_MIN || f >= INT_MAX) ||
                            op1.dtyp == Datatype.QWORD && (f <= LONG_MIN || f >= LONG_MAX),
                )
            }
            else -> TODO("cvtts*2si variants")
        }

        if (infinite || outOfRange) {
            op1.value(
                core,
                when (op1.dtyp) {
                    Datatype.DWORD -> 0x8000_0000uL
                    else -> 0x8000_0000_0000_0000uL
                }
            )

            // TODO: set mxcsr, raise exception
            return
        }

        op1.value(core, truncated)
    }
}
