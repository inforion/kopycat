/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.mips16.branch

import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.common.extensions.set
import ru.inforion.lab403.kopycat.cores.mips.enums.InstructionSet
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction16
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by shiftdj on 21.06.2021.
 */

// jalr ra, rx
class jalr(
        core: MipsCore,
        data: ULong,
        val rx: MipsRegister) : AMipsInstruction16(core, data, Type.JUMP, rx)  {

    override val mnem = "jalr"

    override fun execute() {
        core.reg(31, (core.pc + 4u) set 0)
        val address = rx.value(core)
        core.cpu.branchCntrl.schedule(address clr 0, changeIsa = InstructionSet.from(address))
    }
}