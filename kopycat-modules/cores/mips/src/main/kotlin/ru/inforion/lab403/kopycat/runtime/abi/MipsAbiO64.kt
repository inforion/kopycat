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

import ru.inforion.lab403.kopycat.runtime.funcall.StackAllocation
import ru.inforion.lab403.kopycat.interfaces.IValuable
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class MipsAbiO64(override val core: MipsCore) : MipsIAbi {
    private val a0Valuable by lazy { MipsIAbi.RegisterValuable(core.cpu.regs.a0) }
    private val a1Valuable by lazy { MipsIAbi.RegisterValuable(core.cpu.regs.a1) }
    private val a2Valuable by lazy { MipsIAbi.RegisterValuable(core.cpu.regs.a2) }
    private val a3Valuable by lazy { MipsIAbi.RegisterValuable(core.cpu.regs.a3) }

    override fun getRegisterArgsAmount(): Int = 4

    override fun argRegister(i: Int): IValuable? = when (i) {
        0 -> a0Valuable
        1 -> a1Valuable
        2 -> a2Valuable
        3 -> a3Valuable
        else -> null
    }

    // Maybe move in MipsIAbi
//     override inline fun getStackAlignment() = core.cpu.regs.sp.dtype


    override fun allocArgsOnStack(argsAmount: Int): StackAllocation {
        TODO("Not yet implemented")
        // TODO: если нужен shadow space стека (как  в Windows Calling Convention),
        // то пастить из x64AbiWindows. Если нет -- то x64AbiSystemV :)
    }

    override fun setArgument(i: Int, value: ULong) {
        argRegister(i)?.also {
            it.data = value
        } ?: TODO("Stack arguments not yet implemented")
    }

    override fun getArgument(i: Int): ULong =
        argRegister(i)?.data ?: TODO("Stack arguments not yet implemented")

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