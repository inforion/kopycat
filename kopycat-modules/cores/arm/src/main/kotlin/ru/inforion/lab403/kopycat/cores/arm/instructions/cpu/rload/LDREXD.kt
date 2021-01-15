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
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


// See A8.8.77
class LDREXD(cpu: AARMCore,
            opcode: Long,
            cond: Condition,
            val rn: ARMRegister,
            val rt: ARMRegister,
            val rt2: ARMRegister):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rt, rn) {

    override val mnem = "LDREXD$mcnd"

    override fun execute() {
        val address = rn.value(core)
        // LDREXD requires doubleword-aligned address
        if (address[2..0] == 0b000L) throw ARMHardwareException.AligmentFault // TODO: Not implemented - AlignmentFault(address, FALSE)
        // TODO: Single core - no need
        //SetExclusiveMonitors(address,8);
        // See the description of Single-copy atomicity for details of whether
        // the two loads are 64-bit single-copy atomic.
        rt.value(core, core.inl(address))
        rt2.value(core, core.inl(address + 4))
    }
}