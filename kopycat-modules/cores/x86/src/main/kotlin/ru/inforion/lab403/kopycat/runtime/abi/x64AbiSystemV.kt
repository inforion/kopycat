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
package ru.inforion.lab403.kopycat.runtime.abi

import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.runtime.funcall.StackAllocation
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class x64AbiSystemV(override val core: x86Core) : x86IAbi {
    override fun getRegisterArgsAmount() = 6

    private val rdiValuable by lazy { x86IAbi.RegisterValuable(core, x86GPR.RDI) }
    private val rsiValuable by lazy { x86IAbi.RegisterValuable(core, x86GPR.RSI) }
    private val rdxValuable by lazy { x86IAbi.RegisterValuable(core, x86GPR.RDX) }
    private val rcxValuable by lazy { x86IAbi.RegisterValuable(core, x86GPR.RCX) }
    private val r8Valuable by lazy { x86IAbi.RegisterValuable(core, x86GPR.R8) }
    private val r9Valuable by lazy { x86IAbi.RegisterValuable(core, x86GPR.R9) }

    override fun argRegister(i: Int) = when (i) {
        0 -> rdiValuable
        1 -> rsiValuable
        2 -> rdxValuable
        3 -> rcxValuable
        4 -> r8Valuable
        5 -> r9Valuable
        else -> null
    }

    override fun allocArgsOnStack(argsAmount: Int): StackAllocation {
        if (argsAmount <= getRegisterArgsAmount()) {
            return StackAllocation.createEmpty(getSPReg().value)
        }

        return allocOnStack(((argsAmount - getRegisterArgsAmount()) * getStackAlignmentSize()).ulong_z)
    }

    override fun getResult(i: Int): ULong {
        require(i == 0) {"Only index 0 is available"}
        return core.cpu.regs.rax.value
    }

    override fun setResult(i: Int, value: ULong) {
        require(i == 0) {"Only index 0 is available"}
        core.cpu.regs.rax.value = value;
    }
}
