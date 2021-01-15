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
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


// PUSH (multiple registers), see A8.8.133
/** TODO: Merge with or replace [PUSH] */
class PUSHmr(cpu: AARMCore,
             opcode: Long,
             cond: Condition,
             val registers: ARMRegisterList,
             val unalignedAllowed: Boolean,
             size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, registers, size = size) {

    override val mnem = "PUSH$mcnd"

    override fun execute() {
        var address = core.cpu.regs.sp.value - 4 * registers.count

        registers.forEach {
            // Skipping these lines
            // if i == 13 && i != LowestSetBit(registers) then // Only possible for encoding A1
            //      MemA[address,4] = bits(32) UNKNOWN;
            // Because of UNKNOWN
            core.outl(address like DWORD, it.value(core))
            address += 4
        }

        core.cpu.regs.sp.value -= 4 * registers.count
    }
}