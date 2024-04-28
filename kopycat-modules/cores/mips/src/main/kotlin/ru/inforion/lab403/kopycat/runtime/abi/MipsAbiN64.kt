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
package ru.inforion.lab403.kopycat.runtime.abi

import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.runtime.funcall.StackAllocation
import ru.inforion.lab403.kopycat.interfaces.IValuable
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class MipsAbiN64 (override val core: MipsCore) : MipsIAbi {
    // TODO: floating point regs
    private val a0Valuable by lazy { MipsIAbi.RegisterValuable(core.cpu.regs.a0) }
    private val a1Valuable by lazy { MipsIAbi.RegisterValuable(core.cpu.regs.a1) }
    private val a2Valuable by lazy { MipsIAbi.RegisterValuable(core.cpu.regs.a2) }
    private val a3Valuable by lazy { MipsIAbi.RegisterValuable(core.cpu.regs.a3) }

    // equivalent to a4 - a7
    private val a4Valuable by lazy { MipsIAbi.RegisterValuable(core.cpu.regs.t0) }
    private val a5Valuable by lazy { MipsIAbi.RegisterValuable(core.cpu.regs.t1) }
    private val a6Valuable by lazy { MipsIAbi.RegisterValuable(core.cpu.regs.t2) }
    private val a7Valuable by lazy { MipsIAbi.RegisterValuable(core.cpu.regs.t3) }

    override fun getRegisterArgsAmount(): Int = 8

    override fun argRegister(i: Int): IValuable? = when (i) {
        0 -> a0Valuable
        1 -> a1Valuable
        2 -> a2Valuable
        3 -> a3Valuable
        4 -> a4Valuable
        5 -> a5Valuable
        6 -> a6Valuable
        7 -> a7Valuable
        else -> null
    }

    override fun allocArgsOnStack(argsAmount: Int): StackAllocation {
        if (argsAmount <= getRegisterArgsAmount()) {
            return StackAllocation.createEmpty(core.cpu.regs.sp.value)
        }

        return allocOnStack(((argsAmount - getRegisterArgsAmount()) * getStackAlignmentSize()).ulong_z)
    }

    override fun getResult(i: Int) = when (i) {
        0 -> core.cpu.regs.v0.value
        1 -> core.cpu.regs.v1.value
        else -> throw IllegalArgumentException("Required index 0 or 1 (got $i)")
    }

    override fun setResult(i: Int, value: ULong) {
        when (i) {
            0 -> {
                core.cpu.regs.v0.value = value
            }

            1 -> {
                core.cpu.regs.v1.value = value
            }

            else -> throw IllegalArgumentException("Required index 0 or 1 (got $i)")
        }
    }

}