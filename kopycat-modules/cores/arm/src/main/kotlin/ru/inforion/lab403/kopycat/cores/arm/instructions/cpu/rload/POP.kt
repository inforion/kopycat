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
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unknown
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.cores.arm.operands.isProgramCounter
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class POP(cpu: AARMCore,
          opcode: Long,
          cond: Condition,
          val rn: ARMRegister,
          private val unalignedAllowed: Boolean,
          val registers: ARMRegisterList,
          size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, registers, size = size) {
    override val mnem = "POP$mcnd"

    override fun execute() {
        var address = rn.value(core)

        // Выравниваем стэк до вызова LoadWritePC(), иначе не заработают прерывания
        if(registers.contains(rn)) throw Unknown
        else rn.value(core, rn.value(core) + 4 * registers.count)

        // There is difference from datasheet (all registers save in common loop) -> no LoadWritePC called
        registers.forEachIndexed { _, reg ->
            if (reg.isProgramCounter(core)) {
                if (unalignedAllowed) {
                    if (address[1..0] == 0L)
                        core.cpu.LoadWritePC(core.inl(address like Datatype.DWORD))
                    else
                        throw Unpredictable
                } else {
                    core.cpu.LoadWritePC(core.inl(address like Datatype.DWORD))
                }
            } else {
                reg.value(core, core.inl(address like Datatype.DWORD))
                address += 4
            }
        }
    }
}