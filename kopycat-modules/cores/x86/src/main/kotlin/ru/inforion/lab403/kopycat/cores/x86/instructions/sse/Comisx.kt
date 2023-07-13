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

class Comisx(core: x86Core, opcode: ByteArray, prefs: Prefixes, u: Boolean, vararg operands: AOperand<x86Core>) :
    ASSEInstruction(core, opcode, prefs, *operands) {

    override val mnem = (if (u) "u" else "") + if (prefs.operandOverride) {
        "comisd"
    } else {
        "comiss"
    }

    override fun executeSSEInstruction() {
        // TODO: MXCSR

        val (v1, v2) = if (prefs.operandOverride) {
            val a1 = op1.extValue(core)[63..0].ulong.ieee754()
            val a2 = op2.value(core).ieee754()

            a1 to a2
        } else {
            val a1 = op1.extValue(core)[31..0].uint.ieee754().double
            val a2 = op2.value(core)[31..0].uint.ieee754().double

            a1 to a2
        }

        // TODO: raise #I

        core.cpu.flags.apply {
            when {
                v1.isNaN() || v2.isNaN() -> {
                    zf = true
                    pf = true
                    cf = true
                }
                v1 < v2 -> {
                    zf = false
                    pf = false
                    cf = true
                }
                v1 > v2 -> {
                    zf = false
                    pf = false
                    cf = false
                }
                v1 == v2 -> {
                    zf = true
                    pf = false
                    cf = false
                }
            }

            of = false
            af = false
            sf = false
        }
    }
}
