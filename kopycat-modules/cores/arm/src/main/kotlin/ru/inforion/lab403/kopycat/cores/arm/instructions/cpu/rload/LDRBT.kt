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

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class LDRBT(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            private val postIndex: Boolean,
            val add: Boolean,
            val rn: ARMRegister,
            val rt: ARMRegister,
            val offset: AOperand<AARMCore>):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rt, offset) {

    // A8-425 ARM DDI 0406C.b ID072512

    override val mnem = "LDRT$mcnd"

    override fun execute() {
        if (core.cpu.CurrentModeIsHyp()) throw ARMHardwareException.Unpredictable
        val offsetAddress = rn.value(core) + if (add) offset.value(core) else -offset.value(core)
        val address = if (postIndex) rn.value(core) else offsetAddress
        rt.value(core, core.inb(address like Datatype.DWORD))
        if (postIndex) rn.value(core, offsetAddress)
    }
}