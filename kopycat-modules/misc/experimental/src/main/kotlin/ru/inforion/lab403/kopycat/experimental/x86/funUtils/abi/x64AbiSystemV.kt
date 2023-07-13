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

import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.StackAllocation
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class x64AbiSystemV(override val x86: x86Core) : x86IAbi {
    override fun getRegisterArgsAmount() = 6
    override fun argRegister(i: Int): ARegistersBankNG<x86Core>.Register? = when (i) {
        0 -> x86.cpu.regs.rdi
        1 -> x86.cpu.regs.rsi
        2 -> x86.cpu.regs.rdx
        3 -> x86.cpu.regs.rcx
        4 -> x86.cpu.regs.r8
        5 -> x86.cpu.regs.r9
        else -> null
    }

    override inline fun getStackAlignment(): Datatype = Datatype.QWORD

    override fun allocArgsOnStack(argsAmount: Int): StackAllocation {
        if (argsAmount <= getRegisterArgsAmount()) {
            return StackAllocation.createEmpty(x86.cpu.regs.rsp.value)
        }

        return allocOnStack((argsAmount - getRegisterArgsAmount()).ulong_z)
    }

    override inline fun getResult(): ULong = x86.cpu.regs.rax.value

    override inline fun setResult(value: ULong) {
        x86.cpu.regs.rax.value = value;
    }
}
