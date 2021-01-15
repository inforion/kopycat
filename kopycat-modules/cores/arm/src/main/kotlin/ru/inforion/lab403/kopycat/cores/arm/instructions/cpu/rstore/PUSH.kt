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
package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.rstore

import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.cores.arm.operands.isProgramCounter
import ru.inforion.lab403.kopycat.cores.arm.operands.isStackPointer
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class PUSH(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val rn: ARMRegister,
           private val unalignedAllowed: Boolean,
           val registers: ARMRegisterList,
           size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, registers, size = size) {
    override val mnem = "PUSH$mcnd"

    override fun execute() {
        val newBaseValue = rn.value(core) - 4 * registers.count
        var address = newBaseValue
        registers.forEach { reg ->
            if (reg.isStackPointer(core) && reg != registers.lowest) {  // SP
                throw ARMHardwareException.Unknown
            } else if (reg.isProgramCounter(core)) {  // PC
                if (core.cpu.UnalignedSupport()) {
                    core.outl(address like Datatype.DWORD, core.cpu.PCStoreValue())
                } else {
                    TODO()
                    // MemA[address,4] = PCStoreValue();
                }
            } else {
                if (core.cpu.UnalignedSupport()) {
                    core.outl(address like Datatype.DWORD, reg.value(core))
                } else {
                    TODO()
                    // MemA[address,4] = R[i];
                }
            }
            address += 4
        }
        rn.value(core, newBaseValue)
    }
}