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
package ru.inforion.lab403.kopycat.cores.arm.operands

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ARMRegisterList constructor(regs: List<ARMRegister>):
        AOperand<AARMCore>(Type.CUSTOM, Access.ANY, Controls.VOID, WRONGI, DWORD),
        Iterable<ARMRegister> {

    private val regs = regs.associateBy { it.desc.id }

    override fun equals(other: Any?): Boolean =
            other is ARMRegisterList &&
                    other.type == Type.CUSTOM &&
                    other.regs == regs

    override fun hashCode(): Int {
        var result = type.hashCode()
        result += 31 * result + regs.hashCode()
        return result
    }

    val count = regs.size
    val lowest = regs.minBy { it.desc.id }!!
    val highest = regs.maxBy { it.desc.id }!!

    operator fun get(index: Int) = regs[index]

    override operator fun iterator() = regs.values.iterator()

    operator fun contains(value: ARMRegister) = value.desc.id in regs

    override fun toString() = "{${joinToString()}}"

    override fun value(core: AARMCore): Long =
            throw UnsupportedOperationException("Can't read value of registers list operand")

    override fun value(core: AARMCore, data: Long): Unit =
            throw UnsupportedOperationException("Can't write value to registers list operand")

    fun load(core: AARMCore, start: Long) {
        var address = start

        forEach {
            val value = core.inl(address like DWORD)

            if (it.isProgramCounter(core)) {
                core.cpu.LoadWritePC(value)
            } else {
                it.value(core, value)
            }

            address += 4
        }
    }

    fun store(core: AARMCore, start: Long, rn: ARMRegister, wback: Boolean) {
        var address = start

        forEach {
            if (it == rn && wback && it != lowest) {
                throw ARMHardwareException.Unknown
            }

            core.outl(address like DWORD, it.value(core))
            address += 4
        }
    }

    fun hasProgramCounter(core: AARMCore) = core.cpu.regs.pc.id in regs
    fun hasStackPointer(core: AARMCore) = core.cpu.regs.sp.id in regs
}