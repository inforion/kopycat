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
package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.branch

import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.enums.GPR
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister.GPR.pc
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class Jmp(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.IND_JUMP, size, *operands) {
    override val mnem = "jmp"
    override val isRet: Boolean get() = (op1 as v850esRegister).reg == GPR.r31.id

    // Format I - reg1, reg2
    override fun execute() {
        val addr = op1.value(core) mask 63..1
        pc.value(core, addr - size)
    }
}