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
package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.trap

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.instructions.ssext
import ru.inforion.lab403.kopycat.cores.ppc.instructions.usext
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Trap word immediate
class twi(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "twi"

    override fun execute() {
        val extImm = data.ssext(15)
        val extUImm = data.usext(15)
        val a = op2.ssext(core)
        val ua = op2.value(core)

        val to = op1.value(core)
        if ((to[4].toBool() && (a < extImm))
                || (to[3].toBool() && (a > extImm))
                || (to[2].toBool() && (a == extImm))
                || (to[1].toBool() && (ua < extUImm))
                || (to[0].toBool() && (ua > extUImm)))
            TODO("It's a trap")
        TODO("Isn't fully implemented")
    }
}