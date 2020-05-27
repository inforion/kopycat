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
package ru.inforion.lab403.kopycat.cores.msp430.operands

import ru.inforion.lab403.common.extensions.bitMask
import ru.inforion.lab403.common.extensions.first
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core
import ru.inforion.lab403.kopycat.cores.msp430.enums.MSP430GPR as eGPR



abstract class MSP430Register(
        reg: Int,
        access: AOperand.Access = ANY) :
        ARegister<MSP430Core>(reg, access, WORD) {


    override fun toString(): String = first<eGPR> { it.id == reg }.regName

    companion object {
        val byteMask = bitMask(BYTE.msb..BYTE.lsb)

        fun gpr(dtype : Datatype, id: Int): MSP430Register {

            return when (dtype) {
                BYTE -> when (id) {
                    eGPR.r0.id -> GPRB.r0l
                    eGPR.r1.id -> GPRB.r1l
                    eGPR.r2.id -> GPRB.r2l
                    eGPR.r3.id -> GPRB.r3l
                    eGPR.r4.id -> GPRB.r4l
                    eGPR.r5.id -> GPRB.r5l
                    eGPR.r6.id -> GPRB.r6l
                    eGPR.r7.id -> GPRB.r7l
                    eGPR.r8.id -> GPRB.r8l
                    eGPR.r9.id -> GPRB.r9l
                    eGPR.r10.id -> GPRB.r10l
                    eGPR.r11.id -> GPRB.r11l
                    eGPR.r12.id -> GPRB.r12l
                    eGPR.r13.id -> GPRB.r13l
                    eGPR.r14.id -> GPRB.r14l
                    eGPR.r15.id -> GPRB.r15l
                    else -> throw GeneralException("Unknown GPR id = $id")
                }
                WORD -> when (id) {
                    eGPR.r0.id -> GPR.r0
                    eGPR.r1.id -> GPR.r1
                    eGPR.r2.id -> GPR.r2
                    eGPR.r3.id -> GPR.r3
                    eGPR.r4.id -> GPR.r4
                    eGPR.r5.id -> GPR.r5
                    eGPR.r6.id -> GPR.r6
                    eGPR.r7.id -> GPR.r7
                    eGPR.r8.id -> GPR.r8
                    eGPR.r9.id -> GPR.r9
                    eGPR.r10.id -> GPR.r10
                    eGPR.r11.id -> GPR.r11
                    eGPR.r12.id -> GPR.r12
                    eGPR.r13.id -> GPR.r13
                    eGPR.r14.id -> GPR.r14
                    eGPR.r15.id -> GPR.r15
                    else -> throw GeneralException("Unknown GPR id = $id")
                }
                else -> throw GeneralException("Unusable datatype = $dtype")
            }
        }
    }

    sealed class GPRB(id: Int) : MSP430Register(id) {
        override fun value(core: MSP430Core, data: Long) = core.cpu.regs.writeIntern(reg, data and byteMask)
        override fun value(core: MSP430Core): Long = core.cpu.regs.readIntern(reg) and byteMask

        object r0l : GPRB(eGPR.r0.id)
        object r1l : GPRB(eGPR.r1.id)
        object r2l : GPRB(eGPR.r2.id)
        object r3l : GPRB(eGPR.r3.id)
        object r4l : GPRB(eGPR.r4.id)
        object r5l : GPRB(eGPR.r5.id)
        object r6l : GPRB(eGPR.r6.id)
        object r7l : GPRB(eGPR.r7.id)
        object r8l : GPRB(eGPR.r8.id)
        object r9l : GPRB(eGPR.r9.id)
        object r10l : GPRB(eGPR.r10.id)
        object r11l : GPRB(eGPR.r11.id)
        object r12l : GPRB(eGPR.r12.id)
        object r13l : GPRB(eGPR.r13.id)
        object r14l : GPRB(eGPR.r14.id)
        object r15l : GPRB(eGPR.r15.id)
    }

    sealed class GPR(id: Int) : MSP430Register(id) {
        override fun value(core: MSP430Core, data: Long) = core.cpu.regs.writeIntern(reg, data)
        override fun value(core: MSP430Core): Long = core.cpu.regs.readIntern(reg)

        object r0 : GPR(eGPR.r0.id)
        object r1 : GPR(eGPR.r1.id)
        object r2 : GPR(eGPR.r2.id)
        object r3 : GPR(eGPR.r3.id)
        object r4 : GPR(eGPR.r4.id)
        object r5 : GPR(eGPR.r5.id)
        object r6 : GPR(eGPR.r6.id)
        object r7 : GPR(eGPR.r7.id)
        object r8 : GPR(eGPR.r8.id)
        object r9 : GPR(eGPR.r9.id)
        object r10 : GPR(eGPR.r10.id)
        object r11 : GPR(eGPR.r11.id)
        object r12 : GPR(eGPR.r12.id)
        object r13 : GPR(eGPR.r13.id)
        object r14 : GPR(eGPR.r14.id)
        object r15 : GPR(eGPR.r15.id)
    }
}