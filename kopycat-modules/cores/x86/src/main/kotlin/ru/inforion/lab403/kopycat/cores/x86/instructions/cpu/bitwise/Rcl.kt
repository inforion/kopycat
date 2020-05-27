/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.bitwise

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.cores.x86.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Rcl(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "rcl"

    override val cfChg = true
    override val ofChg = true

    override fun execute() {
        // http://stackoverflow.com/questions/10395071/what-is-the-difference-between-rcr-and-ror
        val maskCf = if (core.cpu.flags.cf) 1L.shl(op1.dtyp.bits) else 0L
        val a1 = op1.value(core) or maskCf
        val a2 = (op2.value(core) % op1.dtyp.bits).toInt()
        val lsb = op1.dtyp.bits - a2 + 1
        val msb = op1.dtyp.bits
        val lowPart = a1[msb..lsb]
        val res = a1.shl(a2).insert(lowPart, a2 - 1..0)
        val result = Variable<x86Core>(0, op1.dtyp)
        result.value(core, res)
        FlagProcessor.processRotateFlag(core, result, op2, true, a1[op1.dtyp.bits - a2] == 1L)
        op1.value(core, result)
    }
}