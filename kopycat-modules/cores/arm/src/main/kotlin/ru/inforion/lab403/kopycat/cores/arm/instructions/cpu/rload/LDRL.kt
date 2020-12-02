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

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.Align
import ru.inforion.lab403.kopycat.cores.arm.ROR
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unknown
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.isProgramCounter
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet.ARM



class LDRL(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val add: Boolean,
           val rt: ARMRegister,
           val imm32: Immediate<AARMCore>,
           size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rt, imm32, size = size) {
    override val mnem = "LDR$mcnd"

    override fun execute() {
        val base = Align(core.cpu.pc, 4)
        val address = base + if (add) imm32.zext else -imm32.zext
        val data = core.inl(address like Datatype.DWORD)
        if (rt.isProgramCounter(core)) { // PC
            if (address[1..0] == 0b00L) core.cpu.LoadWritePC(data)
            else throw Unpredictable
        } else if (core.cpu.UnalignedSupport() || address[1..0] == 0b00L) {
            rt.value(core, data)
        } else {
            if (core.cpu.CurrentInstrSet() == ARM) {
                rt.value(core, ROR(data, 32, 8 * address[1..0].asInt))
            } else {
                throw Unknown
            }
        }
    }
}