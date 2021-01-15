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
package ru.inforion.lab403.kopycat.cores.arm.instructions.cpu.exceptions

import ru.inforion.lab403.common.extensions.WRONGI
import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.toInt
import ru.inforion.lab403.kopycat.cores.arm.AddWithCarry
import ru.inforion.lab403.kopycat.cores.arm.SRType
import ru.inforion.lab403.kopycat.cores.arm.Shift
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMImmediate
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMVariable
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.Immediate
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


// See B9.3.20
class SUBPCLR(cpu: AARMCore,
              opcode: Long,
              cond: Condition,
              val opc: Long,
              val rn: ARMRegister,
              val rm: ARMRegister,
              val registerForm: Boolean,
              val shiftT: SRType,
              val shiftN: Int,
              val imm32: Immediate<AARMCore>,
              size: Int):
        AARMInstruction(cpu, Type.VOID, cond, opcode, rn, rm, imm32, size = size) {

    override val mnem = when(opc) {
        0b0000L -> "ANDS"
        0b0001L -> "EORS"
        0b0010L -> "SUBS"
        0b0011L -> "RSBS"
        0b0100L -> "ADDS"
        0b0101L -> "ADCS"
        0b0110L -> "SBCS"
        0b0111L -> "RSCS"
        0b1000L -> "ORRS"
        0b1101L -> if (registerForm) "MOVS" else throw GeneralException("Deprecated")
        0b1110L -> "BICS"
        0b1111L -> "MVN"
        else -> throw GeneralException("Unknown opcode: ${opc.hex}")
    }

    val result = ARMVariable(DWORD)

    override fun execute() {
        if (core.cpu.CurrentModeIsHyp())
            throw ARMHardwareException.Undefined
        else if (core.cpu.CurrentModeIsUserOrSystem())
            throw ARMHardwareException.Unpredictable
        else {
            val operand2 = if (registerForm)
                ARMImmediate(Shift(rm.value(core), 32, shiftT, shiftN, core.cpu.flags.c.asInt), false, DWORD, WRONGI)
            else
                imm32
            when (opc) {
                0b0000L -> result.and(core, rn, operand2) // AND
                0b0001L -> result.xor(core, rn, operand2) // EOR
                0b0010L -> result.value(core, AddWithCarry(rn.dtyp.bits, rn.value(core), operand2.inv(core), 1).first) // SUB
                0b0011L -> result.value(core, AddWithCarry(rn.dtyp.bits, rn.inv(core), operand2.value(core), 1).first) // RSB
                0b0100L -> result.value(core, AddWithCarry(rn.dtyp.bits, rn.value(core), operand2.value(core), 0).first) // ADD
                0b0101L -> result.value(core, AddWithCarry(rn.dtyp.bits, rn.value(core), operand2.value(core), core.cpu.sregs.apsr.c.toInt()).first) // ADC
                0b0110L -> result.value(core, AddWithCarry(rn.dtyp.bits, rn.value(core), operand2.inv(core), core.cpu.sregs.apsr.c.toInt()).first) // SBC
                0b0111L -> result.value(core, AddWithCarry(rn.dtyp.bits, rn.inv(core), operand2.value(core), core.cpu.sregs.apsr.c.toInt()).first) // RSC
                0b1000L -> result.or(core, rn, operand2) // ORR
                0b1101L -> // MOV, if NOT(register_form)
                    // Otherwise, ASR, LSL, LSR, ROR, or RRX, and
                    // DecodeImmShift() decodes the different shifts
                    result.value(core, operand2)
                0b1110L -> result.value(core, rn.value(core) and operand2.inv(core)) // BIC
                0b1111L -> result.value(core, operand2.inv(core)) // MVN
            }
            core.cpu.CPSRWriteByInstr(core.cpu.sregs.spsr.value, 0b1111, true)
            // Return to Hyp mode in ThumbEE is UNPREDICTABLE
            if (core.cpu.sregs.cpsr.m == 0b11010L && core.cpu.sregs.cpsr.j && core.cpu.sregs.cpsr.t)
                throw ARMHardwareException.Unpredictable
            else
                core.cpu.BranchWritePC(result.value(core))
        }
    }
}