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

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.rotr32
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Type.CUSTOM
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.cores.arm.enums.GPR as eGPR

/**
 * *** opcode-processing operands - Immediate ***
 */

class ARMImmediateCarry(cpu: AARMCore, opcode: Long) : AARMShift(cpu, opcode) {

    val imm8 = opcode[7..0]
    private val rimm = opcode[11..8].asInt
    private val shifterOperand = imm8 rotr32 (2 * rimm)

    override fun toString(): String = "#$shifterOperand"

    override fun equals(other: Any?): Boolean =
            other is ARMImmediateCarry &&
                    other.type == CUSTOM &&
                    other.dtyp == dtyp &&
                    other.opcode == opcode

    override fun hashCode(): Int {
        var result = type.hashCode()
        result += 31 * result + opcode.hashCode()
        result += 31 * result + dtyp.ordinal
        result += 31 * result + specflags.hashCode()
        return result
    }

    override fun value(core: AARMCore): Long = shifterOperand
    override fun carry(): Boolean = if (rimm == 0) cpu.cpu.flags.c else shifterOperand[31] == 1L
}