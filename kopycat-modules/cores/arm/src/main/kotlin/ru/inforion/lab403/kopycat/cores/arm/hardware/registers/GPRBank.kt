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
package ru.inforion.lab403.kopycat.cores.arm.hardware.registers

import ru.inforion.lab403.kopycat.cores.arm.enums.GPR
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore




class GPRBank : ARegisterBankNG(32) {
    override val name = "General purpose registers"

    class Operand(reg: Int, access: Access = Access.ANY) : ARegister<AARMCore>(reg, access) {
        companion object {
            lateinit var registerNames: List<String>
        }

        override fun toString() = registerNames[reg]

        override fun value(core: AARMCore, data: Long) =
                if(reg == GPR.SPMain.id) {
                    val sp = core.cpu.StackPointerSelect()
                    core.cpu.regs.write(sp, data)
                } else core.cpu.regs.write(reg, data)

        override fun value(core: AARMCore): Long =
                if(reg == GPR.SPMain.id) {
                    val sp = core.cpu.StackPointerSelect()
                    core.cpu.regs.read(sp)
                } else core.cpu.regs.read(reg)
    }
    
    val r0 = Register()
    val r1 = Register()
    val r2 = Register()
    val r3 = Register()
    val r4 = Register()
    val r5 = Register()
    val r6 = Register()
    val r7 = Register()
    val r8 = Register()
    val r9 = Register()
    val r10 = Register()
    val r11 = Register()
    val r12 = Register()
    val spMain = Register()
    val lr = Register()
    val pc = Register()
    val spProcess = Register()

    init {
        initialize()
        Operand.registerNames = registers.map { it.name }
    }
}