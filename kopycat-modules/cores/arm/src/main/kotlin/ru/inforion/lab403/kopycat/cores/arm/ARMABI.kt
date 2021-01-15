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
package ru.inforion.lab403.kopycat.cores.arm

import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABIBase
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class ARMABI(core: AARMCore, bigEndian: Boolean): ABI<AARMCore>(core, 32, bigEndian) {

    @DontAutoSerialize
    override val regArguments = listOf(
            core.cpu.regs.r0.id,
            core.cpu.regs.r1.id,
            core.cpu.regs.r2.id,
            core.cpu.regs.r3.id)

    override val minimumStackAlignment: Int = 4

    override val gprDatatype = Datatype.values().first { it.bits == this.core.cpu.regs.bits }
    override val sizetDatatype = Datatype.DWORD
    override fun register(index: Int) = core.cpu.regs[index].toOperand()
    override val registerCount = core.cpu.count()

    override fun createContext() = ARMContext(this)

    override val pc get() = core.cpu.regs.pc.toOperand()
    override val sp get() = core.cpu.regs.sp.toOperand()
    override val ra get() = core.cpu.regs.lr.toOperand()
    override val rv get() = core.cpu.regs.r0.toOperand()

    // 0 - int
    // 2,3 - long
    //
    var argOffset = 0

    // 0 -(+1)> 1 -(/2)> 0 -(*2)> 0
    // 1 -(+1)> 2 -(/2)> 1 -(*2)> 2
    // 2 -(+1)> 3 -(/2)> 1 -(*2)> 2
    // 3 -(+1)> 4 -(/2)> 2 -(*2)> 4 [stack]
    fun alignedLongIndex(index: Int) = (((index + 1) / 2) * 2)

    override fun getArg(index: Int, type: Datatype, alignment: Int, instance: ABIBase): Long {
        val realIndex = index + argOffset
        val alignedIndex = if (type.bits > bits) alignedLongIndex(realIndex) else realIndex
        return if (alignedIndex < regArguments.size) {
            if (type.bits > bits) {
                argOffset++
                if (index == 1)
                    argOffset++

                val low = instance.readRegister(regArguments[alignedIndex])
                val high = instance.readRegister(regArguments[alignedIndex + 1])
                (high shl 32) or low
            } else {
                instance.readRegister(regArguments[alignedIndex]) like type
            }
        } else {
            instance.readStack(stackArgsOffset + (realIndex - regArguments.size) * alignment, type)
        }
    }

    override fun getArgs(args: Iterable<Datatype>, instance: ABIBase): Array<Long> {
        argOffset = 0
        return super.getArgs(args, instance)
    }
}