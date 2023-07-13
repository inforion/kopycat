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
package ru.inforion.lab403.kopycat.experimental.x86.funUtils.abi

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.StackAllocation
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class x64AbiWindows(override val x86: x86Core) : x86IAbi {
    companion object {
        const val SHADOW_STACK_SIZE = 0x20uL
    }

    override inline fun getRegisterArgsAmount() = 4;
    override fun argRegister(i: Int): ARegistersBankNG<x86Core>.Register? = when (i) {
        0 -> x86.cpu.regs.rcx
        1 -> x86.cpu.regs.rdx
        2 -> x86.cpu.regs.r8
        3 -> x86.cpu.regs.r9
        else -> null
    }

    /**
     * With shadow stack skip
     */
    override fun setArgument(i: Int, value: ULong) {
        argRegister(i)?.also {
            it.value = value
        } ?: setTopStack(i, value)
    }

    /**
     * With shadow stack skip
     */
    override fun getArgument(i: Int): ULong =
        argRegister(i)?.value ?: getTopStack(i)


    override inline fun getStackAlignment() = Datatype.QWORD

    /**
     * With shadow stack space!
     *
     * See [Microsoft x64 Stack usage](https://learn.microsoft.com/en-us/cpp/build/stack-usage?view=msvc-170)
     */
    override fun allocArgsOnStack(argsAmount: Int): StackAllocation {
        val size = maxOf(
            argsAmount * getStackAlignment().bytes,
            SHADOW_STACK_SIZE.int
        ).ulong_z
        return allocOnStack(size)
    }

    override inline fun getResult(): ULong = x86.cpu.regs.rax.value
    override inline fun setResult(value: ULong) {
        x86.cpu.regs.rax.value = value;
    }
}