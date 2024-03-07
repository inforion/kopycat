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

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.runtime.funcall.StackAllocation
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.interfaces.IValuable
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class x64AbiWindows(override val core: x86Core) : x86IAbi {
    companion object {
        const val SHADOW_STACK_SIZE = 0x20uL
    }

    private val rcxValuable by lazy { x86IAbi.RegisterValuable(core, x86GPR.RCX) }
    private val rdxValuable by lazy { x86IAbi.RegisterValuable(core, x86GPR.RDX) }
    private val r8Valuable by lazy { x86IAbi.RegisterValuable(core, x86GPR.R8) }
    private val r9Valuable by lazy { x86IAbi.RegisterValuable(core, x86GPR.R9) }

    override fun getRegisterArgsAmount() = 4

    override fun argRegister(i: Int): IValuable? = when (i) {
        0 -> rcxValuable
        1 -> rdxValuable
        2 -> r8Valuable
        3 -> r9Valuable
        else -> null
    }

    /**
     * With shadow stack skip
     */
    override fun setArgument(i: Int, value: ULong) {
        argRegister(i)?.also {
            it.data = value
        } ?: setTopStack(
            i /*no need to subtract with getRegisterArgsAmount due to shadow stack*/,
            value
        )
    }

    /**
     * With shadow stack skip
     */
    override fun getArgument(i: Int): ULong =
        argRegister(i)?.data ?: getTopStack(i)

    /**
     * With shadow stack space!
     *
     * See [Microsoft x64 Stack usage](https://learn.microsoft.com/en-us/cpp/build/stack-usage?view=msvc-170)
     */
    override fun allocArgsOnStack(argsAmount: Int): StackAllocation {
        val size = maxOf(
            argsAmount * getStackAlignmentSize(),
            SHADOW_STACK_SIZE.int
        ).ulong_z
        return allocOnStack(size)
    }

    override fun getResult(i: Int): ULong {
        require(i == 0) { "Only index 0 is available (passed i=${i})" }
        return core.cpu.regs.rax.value
    }

    override fun setResult(i: Int, value: ULong) {
        require(i == 0) { "Only index 0 is available (passed i=${i})" }
        core.cpu.regs.rax.value = value
    }
}