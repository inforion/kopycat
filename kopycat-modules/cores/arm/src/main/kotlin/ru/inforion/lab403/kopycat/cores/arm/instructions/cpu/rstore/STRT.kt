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

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.isProgramCounter
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.AARMCore.InstructionSet.ARM



class STRT(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val postindex: Boolean,
           val add: Boolean,
           val rn: ARMRegister,
           val rt: ARMRegister,
           val offset: AOperand<AARMCore>):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rt, offset) {
    override val mnem = "STRT$mcnd"

    override fun execute() {
        if (core.cpu.CurrentModeIsHyp()) throw ARMHardwareException.Unpredictable

        val offsetAddress = rn.value(core) + if (add) offset.value(core) else -offset.value(core)
        val address = if (postindex) rn.value(core) else offsetAddress
        val data = if (rt.isProgramCounter(core)) core.cpu.PCStoreValue() else rt.value(core)

        if(core.cpu.UnalignedSupport() || address[1..0] == 0b00L || core.cpu.CurrentInstrSet() == ARM)
            core.outl(address like Datatype.DWORD, data)
        else throw ARMHardwareException.Unknown
        if (postindex) rn.value(core, offsetAddress)
    }
}