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
package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.arithmInt

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCVariable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Divide word unsigned
class divwux(core: PPCCore, val overflow: Boolean, val record: Boolean, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "divwu${if (overflow) "o" else ""}${if (record) "." else ""}"

    private val result = PPCVariable(Datatype.DWORD)

    override fun execute() {
        val opA = op2.value(core)
        val opB = op3.value(core)

        val inv = (opB == 0L)
        if (!inv) {
            result.value(core, opA / opB)

            op1.value(core, result)

            if (record)
                FlagProcessor.processCR0(core, result)
        }
        if (overflow)
            FlagProcessor.processOverflowDiv(core, inv)
    }
}