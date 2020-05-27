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

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esMemory
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class Prepare(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "prepare"

    // Format XIII - imm, list, reg
    override fun execute() {
        val listValue = op2.value(core)
        for (i in 0..11){
            if(listValue[i] != 0L){
                val regId = when(i){
                    0 -> 30
                    1 -> 31
                    2 -> 29
                    3 -> 28
                    4 -> 23
                    5 -> 22
                    6 -> 21
                    7 -> 20
                    8 -> 27
                    9 -> 26
                    10 -> 25
                    11 -> 24
                    else -> throw GeneralException("Incorrect list index")
                }
                v850esMemory(Datatype.DWORD, core.cpu.regs.r3StackPointer - 4).value(core, v850esRegister.gpr(regId).value(core))
                core.cpu.regs.r3StackPointer = core.cpu.regs.r3StackPointer - 4
            }
        }
        core.cpu.regs.r3StackPointer = core.cpu.regs.r3StackPointer - (op1.value(core) shl 2)

        if(operands.size == 3 && (op3 is v850esRegister || op3 is Immediate))
            core.cpu.regs.r30ElementPointer = op3.value(core)
    }
}