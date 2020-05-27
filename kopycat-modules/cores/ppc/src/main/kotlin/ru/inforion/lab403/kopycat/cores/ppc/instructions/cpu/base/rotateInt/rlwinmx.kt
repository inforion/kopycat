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
package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.rotateInt

import ru.inforion.lab403.common.extensions.bitMask
import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.instructions.rotl32
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCVariable
import ru.inforion.lab403.kopycat.modules.cores.PPCCore




//Rotate left word immediate then AND with mask
class rlwinmx(core: PPCCore, val shift: Int, val maskFst: Int, val maskSnd: Int, val record: Boolean, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "rlwinm${if (record) "." else ""}"

    private val result = PPCVariable(Datatype.DWORD)

    override fun execute() {
        val r = op1.value(core) rotl32 shift
        //WARNING: In documentation MASK(x, y), where x > y.
        //But in PPC msb is zero. So MASK(MB, ME) switches to MASK(ME, MB)
        val m = when {
            maskFst < maskSnd + 1 -> bitMask((31 - maskFst)..(31 - maskSnd))
            maskFst == maskSnd + 1 -> {
                bitMask(32)
            }
            else -> {
                bitMask((32 - maskSnd)..(32 - maskFst)).inv() mask 32
            }
        }


        result.value(core, r and m)

        op2.value(core, result)

        if (record)
            FlagProcessor.processCR0(core, result)
    }
}