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
package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.storeInt

import ru.inforion.lab403.common.extensions.ssext
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.enums.eUISA
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Store multiple word
/*
* WARNING: For the Server environment, the Load/Store Multiple
* instructions are not supported in Little-Endian mode. If
* they are executed in Little-Endian mode, the system
* alignment error handler is invoked.
* PowerISA V2.05, page 56
*/
class stmw(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "stmw"

    override fun execute() {
        //TODO: Displacement?
        val b = if ((op2 as PPCRegister).reg == eUISA.GPR0.id)
            0L
        else
            op2.value(core)
        var ea = b + data.ssext(15)
        var r = (op1 as PPCRegister).reg
        while (r <= 31) {
            core.outl(ea, core.cpu.regs.gpr(r).value(core))
            ++r
            ea += 4
        }

    }
}