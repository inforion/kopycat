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
package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.operands.x86FprRegister
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Fdiv(
    core: x86Core,
    opcode: ByteArray,
    prefs: Prefixes,
    val popCount: Int,
    val int: Boolean,
    vararg operands: AOperand<x86Core>
) : AFPUInstruction(core, opcode, prefs, *operands) {
    override val mnem = "f" + (if (int) "i" else "") + "div" + if (popCount != 0) "p" else ""

    companion object {
        fun commonExecute(
            op1: AOperand<x86Core>,
            op2: AOperand<x86Core>,
            core: x86Core,
            int: Boolean,
            popCount: Int,
            r: Boolean = false,
        ) {
            val a1 = op1.extValue(core).longDouble(core.fpu.fwr.FPUControlWord)
            val a2 = if (!int) {
                if (op2 is x86FprRegister) {
                    op2.extValue(core).longDouble(core.fpu.fwr.FPUControlWord)
                } else {
                    op2.longDouble(core, core.fpu.fwr.FPUControlWord)
                }
            } else {
                when (op2.dtyp) {
                    Datatype.WORD -> op2.value(core).short.longDouble(core.fpu.fwr.FPUControlWord)
                    else -> op2.value(core).int.longDouble(core.fpu.fwr.FPUControlWord)
                }
            }

            if (!r && a2.isZero()) {
                throw x86HardwareException.DivisionByZero(core.pc)
            }

            if (r && a1.isZero()) {
                throw x86HardwareException.DivisionByZero(core.pc)
            }

            val res = if (!r) a1 / a2 else a2 / a1
            op1.extValue(core, res.ieee754AsUnsigned())
            core.fpu.pop(popCount)
        }
    }

    override fun executeFPUInstruction() = commonExecute(op1, op2, core, int, popCount)
}
