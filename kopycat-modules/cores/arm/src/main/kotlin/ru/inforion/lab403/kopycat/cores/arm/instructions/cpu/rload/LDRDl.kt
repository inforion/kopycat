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
package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rload

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.Align
import ru.inforion.lab403.kopycat.cores.arm.HaveLPAE
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class LDRDl(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val add: Boolean,
            val rt1: ARMRegister,
            private val rt2: ARMRegister,
            val imm32: Immediate<AARMCore>):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rt1, rt2, imm32) {
    override val mnem = "LDRD$mcnd"

    override fun execute() {
        val address = Align(core.cpu.pc, 4) + if (add) imm32.value else -imm32.value

        if(HaveLPAE() && address[2..0] == 0L)
            throw GeneralException("Not implemented!")
        else {
            rt1.value(core, core.inl((address + 0) like Datatype.DWORD))
            rt2.value(core, core.inl((address + 4) like Datatype.DWORD))
        }
    }
}