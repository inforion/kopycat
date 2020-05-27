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
package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.special

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister.CTRLR
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister.GPR
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class Callt(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.IND_CALL, size, *operands) {
    override val mnem = "callt"

    // Format II - imm, reg2
    override fun execute() {
        // size add in CPU execute
        CTRLR.CTPC.value(core, GPR.pc.value(core) + size)
        CTRLR.CTPSW.value(core, CTRLR.PSW.value(core))
        val address = CTRLR.CTBP.value(core) + (op1.value(core) shl 1)
        val base = core.inw(address)
        GPR.pc.value(core, CTRLR.CTBP.value(core) + base - size)
    }
}