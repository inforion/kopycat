/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.unaryMinus
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unknown
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.isProgramCounter
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.interfaces.*


class LDRr(cpu: AARMCore,
           opcode: ULong,
           cond: Condition,
           val index: Boolean,
           val add: Boolean,
           val wback: Boolean,
           val rt: ARMRegister,
           val rn: ARMRegister,
           val rm: ARMRegister,
           val shiftT: SRType,
           val shiftN: Int,
           size: Int): AARMInstruction(cpu, Type.VOID, cond, opcode, rt, rn, rm, size = size) {
    override val mnem = "LDR$mcnd"

    override fun execute() {
        val offset = Shift(rm.value(core), 32, shiftT, shiftN, core.cpu.flags.c.int)
        val offsetAddress = rn.value(core) + if(add) offset else -offset
        val address = if(index) offsetAddress else rn.value(core)
        val data = core.inl(address like Datatype.DWORD)
        if(wback) rn.value(core, offsetAddress)
        if (rt.isProgramCounter(core))
            if (address[1..0] == 0b00uL) core.cpu.LoadWritePC(data)
            else throw Unpredictable
        else if (core.cpu.UnalignedSupport() || address[1..0] == 0b00uL) rt.value(core, data)
        else throw Unknown
    }
}