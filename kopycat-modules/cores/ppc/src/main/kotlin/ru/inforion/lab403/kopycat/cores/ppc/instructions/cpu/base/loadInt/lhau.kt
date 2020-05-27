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
package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.loadInt

import ru.inforion.lab403.common.extensions.ssext
import ru.inforion.lab403.common.extensions.usext
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Load halfword algebraic
class lhau(core: PPCCore, val condRegField: Long, val length: Boolean, val data: Long, vararg operands: AOperand<PPCCore>):
        APPCInstruction(core, Type.VOID, *operands) {
    override val mnem = "lhau"

    override fun execute() {
        val ea = op2.value(core) + data.ssext(15)
        val mem = core.inw(ea).usext(15)
        op1.value(core, mem)
        op2.value(core, ea)
    }
}