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
package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Fucom(core: x86Core, opcode: ByteArray, prefs: Prefixes, private val popNumber: Int, vararg operands: AOperand<x86Core>) :
    AFPUInstruction(core, opcode, prefs, *operands) {
    override val mnem = "fucom" + when (popNumber) {
        1 -> "p"
        2 -> "pp"
        else -> ""
    }

    override fun executeFPUInstruction() {
        val a1 = op1.extValue(core).longDouble(core.fpu.softfloat)
        val a2 = op2.extValue(core).longDouble(core.fpu.softfloat)
        when {
            a1 > a2 -> {
                core.fpu.fwr.FPUStatusWord.c0 = false
                core.fpu.fwr.FPUStatusWord.c2 = false
                core.fpu.fwr.FPUStatusWord.c3 = false
            }
            a1 < a2 -> {
                core.fpu.fwr.FPUStatusWord.c0 = true
                core.fpu.fwr.FPUStatusWord.c2 = false
                core.fpu.fwr.FPUStatusWord.c3 = false
            }
            else -> {
                core.fpu.fwr.FPUStatusWord.c0 = false
                core.fpu.fwr.FPUStatusWord.c2 = false
                core.fpu.fwr.FPUStatusWord.c3 = true
            }
        }
        core.fpu.pop(popNumber)
    }
}
