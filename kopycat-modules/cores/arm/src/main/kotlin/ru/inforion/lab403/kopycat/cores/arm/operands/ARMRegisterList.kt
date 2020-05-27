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
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.hardware.registers.GPRBank
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.modules.cores.AARMCore

class ARMRegisterList(val cpu: AARMCore, val opcode: Long, val rbits: Long):
        AOperand<AARMCore>(Type.CUSTOM, Access.ANY, Controls.VOID, WRONGI, Datatype.DWORD),
        Iterable<ARMRegister> {

    private val regs = (0..15)
            .filter { rbits[it] == 1L }
            .map { GPRBank.Operand(it) }
            .toTypedArray()

    override fun equals(other: Any?): Boolean =
            other is ARMImmediateCarry &&
                    other.type == Type.CUSTOM &&
                    other.dtyp == dtyp &&
                    other.opcode == opcode

    override fun hashCode(): Int {
        var result = type.hashCode()
        result += 31 * result + opcode.hashCode()
        result += 31 * result + dtyp.ordinal
        result += 31 * result + specflags.hashCode()
        return result
    }

    val bitCount: Int = regs.size
    val lowestSetBit: Int = regs.minBy { it.reg }!!.reg

    override operator fun iterator() = regs.iterator()

    override fun toString(): String = "{${regs.joinToString()}}"

    override fun value(core: AARMCore): Long =
            throw UnsupportedOperationException("Can't read value of registers list operand")

    override fun value(core: AARMCore, data: Long): Unit =
            throw UnsupportedOperationException("Can't write value to registers list operand")
}