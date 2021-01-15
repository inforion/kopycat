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
package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.enums.ShiftType
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.interfaces.ITableEntry
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



abstract class ADecoder<out T: AARMInstruction>(val core: AARMCore): ITableEntry {
    abstract fun decode(data: Long): T

    protected fun list(rbits: Long): ARMRegisterList {
        val regs = (0..15).filter { rbits[it] == 1L }.map { core.cpu.regs[it].toOperand() }
        return ARMRegisterList(regs)
    }

    protected fun sp() = core.cpu.regs.sp.toOperand()

    protected fun gpr(id: Int) = core.cpu.regs[id].toOperand()

    protected fun imm(data: Long, signed: Boolean) = ARMImmediate(data, signed, DWORD, WRONGI)

    protected fun shiftImm(opcode: Long): ARMImmediateShift {
        val rm = gpr(opcode[3..0].asInt)
        val imm = imm(opcode[11..7], false)
        val type = first<ShiftType> { it.id == opcode[6..5] }
        return ARMImmediateShift(rm, imm, type)
    }

    protected fun shiftReg(opcode: Long): ARMRegisterShift {
        val rs = gpr(opcode[11..8].asInt)
        val rm = gpr(opcode[3..0].asInt)
        val type = first<ShiftType> { it.id == opcode[6..5] }
        return ARMRegisterShift(rs, rm, type)
    }

    protected fun carry(opcode: Long): ARMImmediateCarry {
        val imm8 = opcode[7..0]
        val rimm = opcode[11..8].asInt
        val shifter = imm8 rotr32 (2 * rimm)
        return ARMImmediateCarry(rimm, shifter)
    }

    protected fun cond(opcode: Long) = find<Condition> { it.opcode == opcode[31..28].asInt } ?: Condition.AL
}
