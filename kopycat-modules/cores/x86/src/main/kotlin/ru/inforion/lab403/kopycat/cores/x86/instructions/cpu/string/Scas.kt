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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.string

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.cores.x86.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class Scas(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AStringInstruction(core, opcode, prefs, true, *operands) {
    override val mnem = "scas"

    override val cfChg = true
    override val pfChg = true
    override val afChg = true
    override val zfChg = true
    override val sfChg = true
    override val ofChg = true

    override fun executeStringInstruction() {
        val a1 = op1 as x86Register
        val a2 = op2 as x86Displacement
        val res = a1.value(core) - a2.value(core)
        val result = Variable<x86Core>(0, op1.dtyp)
        result.value(core, res)
        var pos = a2.reg.value(core)
        if (!x86Register.eflags.df(core))
            pos += a2.dtyp.bytes
        else
            pos -= a2.dtyp.bytes
        FlagProcessor.processAddSubCmpFlag(core, result, op1, op2, true)
        a2.reg.value(core, pos)
    }
}