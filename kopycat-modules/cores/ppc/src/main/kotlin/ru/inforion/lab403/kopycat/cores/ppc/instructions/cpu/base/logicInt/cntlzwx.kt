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
package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.logicInt

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.truth
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.ppc.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCVariable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Count leading zeros word
class cntlzwx(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "cntlzw${if (flag) "." else ""}"

    val rs = PPCRegister.gpr(fieldA)
    val ra = PPCRegister.gpr(fieldB)

    val result = PPCVariable(Datatype.DWORD)

    //Make fun, not var
    private fun countLeadingZeroes(rsv: ULong): ULong {
        for (i in 31 downTo 0)
            if (rsv[i].truth)
                return 31uL - i.uint
        return 32uL
    }

    override fun execute() {
        val rsv = rs.value(core)
        val n = countLeadingZeroes(rsv)

        result.value(core, n)

        ra.value(core, result)

        if (flag)
            FlagProcessor.processCR0(core, result)
    }
}