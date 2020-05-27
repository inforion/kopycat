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

import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unknown
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class LDRSHi(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val index: Boolean,
             val add: Boolean,
             val wback: Boolean,
             val rn: ARMRegister,
             val rt: ARMRegister,
             val imm32: Immediate<AARMCore>,
             size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rt, imm32, size = size) {
    override val mnem = "LDRH$mcnd"

    override fun execute() {
        val offsetAddress = rn.value(core) + if (add) imm32.value else -imm32.value
        val address = if (index) offsetAddress else rn.value(core)
        val data = core.inw(address like Datatype.DWORD)

        if (wback) rn.value(core, offsetAddress)
        if(core.cpu.UnalignedSupport() || address[0] == 0L)
            rt.value(core, signext(data, 16).asLong)
        else throw Unknown
    }
}