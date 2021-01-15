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
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.isProgramCounter
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class STRi(cpu: AARMCore,
           opcode: Long,
           cond: Condition,
           val index: Boolean,
           val add: Boolean,
           val wback: Boolean,
           val rn: ARMRegister,
           val rt: ARMRegister,
           val imm: Immediate<AARMCore>,
           size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rt, rn, imm, size = size) {

    override val mnem = "STR$mcnd"

    override fun execute() {
        val offsetAddress = rn.value(core) + if (add) imm.zext else -imm.zext
        val address = if (index) offsetAddress else rn.value(core)
        core.outl(address like Datatype.DWORD, if (rt.isProgramCounter(core)) core.cpu.PCStoreValue() else rt.value(core))
        if (wback) rn.value(core, offsetAddress)
    }
}