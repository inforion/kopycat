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

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.FWRBank
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * https://www.felixcloutier.com/x86/fxam
 */
class Fxam(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>) :
    AFPUInstruction(core, opcode, prefs, *operands) {
    override val mnem = "fxam"

    override fun executeFPUInstruction() {
        // stack is empty
        if(core.fpu.fwr.FPUTagWord[core.fpu.fwr.FPUStatusWord.top.int] == FWRBank.TagValue.Empty) {
            core.fpu.fwr.FPUStatusWord.c0 = true
            core.fpu.fwr.FPUStatusWord.c2 = false
            core.fpu.fwr.FPUStatusWord.c3 = true
            return
        }

        val op = op1.extValue(core).longDouble(core.fpu.fwr.FPUControlWord)
        core.fpu.fwr.FPUStatusWord.c0 = false
        core.fpu.fwr.FPUStatusWord.c2 = false
        core.fpu.fwr.FPUStatusWord.c3 = false
        when (op) {
            LongDouble.zero(core.fpu.fwr.FPUControlWord) -> {
                core.fpu.fwr.FPUStatusWord.c3 = true
            }
            // TODO: check for NaN, Unsupported, Infinity, Denormal number
            // assume normal finite number
            else -> {
                core.fpu.fwr.FPUStatusWord.c2 = true
            }
        }
        // false for positive number and zero, true for negative
        core.fpu.fwr.FPUStatusWord.c1 = op < LongDouble.zero(core.fpu.fwr.FPUControlWord)
    }
}