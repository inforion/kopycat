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
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class Trap(core: v850ESCore, size: Int, vararg operands: AOperand<v850ESCore>):
        AV850ESInstruction(core, Type.VOID, size, *operands) {
    override val mnem = "trap"

    // Format X - imm
    override fun execute() {
        // insnSize add in CPU execute
        if(op1.value(core) <= 0x1F) {
            core.cpu.cregs.eipc = core.cpu.regs.pc + size
            core.cpu.cregs.eipsw = core.cpu.cregs.psw

            val prefix = if (op1.value(core) <= 0xF) 0x40L else 0x50
            // Exception code of non-maskable interrupt (NMI)
            val fecc = core.cpu.cregs.ecr[31..16]
            // Exception code of exception or maskable interrupt
            val eicc = prefix + op1.value(core)[3..0]
            core.cpu.cregs.ecr = (fecc shl 16) + eicc

            core.cpu.flags.ep = true
            core.cpu.flags.id = true

            core.cpu.regs.pc = prefix - size
        } else throw GeneralException("Wrong vector value on TRAP operation!")
    }
}