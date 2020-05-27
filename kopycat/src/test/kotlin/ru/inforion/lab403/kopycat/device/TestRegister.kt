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
package ru.inforion.lab403.kopycat.device

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.device.TestGPR as eGPR

abstract class TestRegister(reg: Int, access: AOperand.Access = ANY)
    : ARegister<TestCore>(reg, access, DWORD) {
    companion object {
        fun gpr(id: Int): TestRegister = when(id) {
                eGPR.r0.id -> GPR.r0
                eGPR.r1.id -> GPR.r1
                eGPR.pc.id -> GPR.pc
                else -> throw GeneralException("Unknown GPR id = $id")
        }
    }

    sealed class GPR(id: Int) : TestRegister(id) {
        override fun value(core: TestCore, data: Long) = core.cpu.regs.writeIntern(reg, data)
        override fun value(core: TestCore): Long = core.cpu.regs.readIntern(reg)
        object r0 : GPR(eGPR.r0.id)
        object r1 : GPR(eGPR.r1.id)
        object pc : GPR(eGPR.pc.id)
    }

    override fun toString(): String = eGPR.from(reg).name.toLowerCase()
}
