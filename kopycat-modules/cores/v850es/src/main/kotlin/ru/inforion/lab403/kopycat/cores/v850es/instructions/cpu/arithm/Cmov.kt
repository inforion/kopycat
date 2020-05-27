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
package ru.inforion.lab403.kopycat.cores.v850es.instructions.cpu.arithm

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.cores.v850es.hardware.flags.FlagCondition
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class Cmov(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "cmov"

    override fun execute() {
        val op1Data = if(op1 is Immediate) op1.ssext(core) else op1.value(core)
        if(FlagCondition.CheckCondition(core, op4.value(core)))
            op3.value(core, op1Data)
        else
            op3.value(core, op2.value(core))
    }
}