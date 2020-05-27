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
package ru.inforion.lab403.kopycat.cores.v850es.operands

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand.Access.ANY
import ru.inforion.lab403.kopycat.cores.base.operands.ARegister
import ru.inforion.lab403.kopycat.cores.v850es.enums.Regtype
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore
import ru.inforion.lab403.kopycat.cores.v850es.enums.CTRLR as eCTRLR
import ru.inforion.lab403.kopycat.cores.v850es.enums.GPR as eGPR

abstract class v850esRegister(
        reg: Int,
        val rtyp: Regtype,
        access: AOperand.Access = ANY) :
        ARegister<v850ESCore>(reg, access, DWORD) {

    override fun toString(): String =
        when (rtyp) {
            Regtype.GPR -> eGPR.from(reg)
            Regtype.CTRLR -> eCTRLR.from(reg)
        }.name.toLowerCase()

    companion object {
        fun gpr(id: Int): v850esRegister {
            return when (id) {
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
                eGPR.r16.id -> GPR.r16
                eGPR.r17.id -> GPR.r17
                eGPR.r18.id -> GPR.r18
                eGPR.r19.id -> GPR.r19
                eGPR.r20.id -> GPR.r20
                eGPR.r21.id -> GPR.r21
                eGPR.r22.id -> GPR.r22
                eGPR.r23.id -> GPR.r23
                eGPR.r24.id -> GPR.r24
                eGPR.r25.id -> GPR.r25
                eGPR.r26.id -> GPR.r26
                eGPR.r27.id -> GPR.r27
                eGPR.r28.id -> GPR.r28
                eGPR.r29.id -> GPR.r29
                eGPR.r30.id -> GPR.r30
                eGPR.r31.id -> GPR.r31
                eGPR.pc.id -> GPR.pc
                else -> throw GeneralException("Unknown GPR id = $id")
            }
        }

        fun creg(id: Int): v850esRegister {
            return when (id) {
                eCTRLR.EIPC.id -> CTRLR.EIPC
                eCTRLR.EIPSW.id -> CTRLR.EIPSW
                eCTRLR.FEPC.id -> CTRLR.FEPC
                eCTRLR.FEPSW.id -> CTRLR.FEPSW
                eCTRLR.ECR.id -> CTRLR.ECR
                eCTRLR.PSW.id -> CTRLR.PSW
                eCTRLR.CTPC.id -> CTRLR.CTPC
                eCTRLR.CTPSW.id -> CTRLR.CTPSW
                eCTRLR.DBPC.id -> CTRLR.DBPC
                eCTRLR.DBPSW.id -> CTRLR.DBPSW
                eCTRLR.CTBP.id -> CTRLR.CTBP
                eCTRLR.DIR.id -> CTRLR.DIR
                else -> throw GeneralException("Unknown CTRL id = $id")
            }
        }
    }

    sealed class GPR(id: Int) : v850esRegister(id, Regtype.GPR) {
        override fun value(core: v850ESCore, data: Long) = core.cpu.regs.writeIntern(reg, data)
        override fun value(core: v850ESCore): Long = core.cpu.regs.readIntern(reg)

        object r0 : GPR(eGPR.r0.id) {
            override fun value(core: v850ESCore): Long = 0
            override fun value(core: v850ESCore, data: Long) = Unit
        }
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
        object r16 : GPR(eGPR.r16.id)
        object r17 : GPR(eGPR.r17.id)
        object r18 : GPR(eGPR.r18.id)
        object r19 : GPR(eGPR.r19.id)
        object r20 : GPR(eGPR.r20.id)
        object r21 : GPR(eGPR.r21.id)
        object r22 : GPR(eGPR.r22.id)
        object r23 : GPR(eGPR.r23.id)
        object r24 : GPR(eGPR.r24.id)
        object r25 : GPR(eGPR.r25.id)
        object r26 : GPR(eGPR.r26.id)
        object r27 : GPR(eGPR.r27.id)
        object r28 : GPR(eGPR.r28.id)
        object r29 : GPR(eGPR.r29.id)
        object r30 : GPR(eGPR.r30.id)
        object r31 : GPR(eGPR.r31.id)
        object pc : GPR(eGPR.pc.id)
    }

    sealed class CTRLR(id: Int) : v850esRegister(id, Regtype.CTRLR) {
        override fun value(core: v850ESCore, data: Long) = core.cpu.cregs.writeIntern(reg, data)
        override fun value(core: v850ESCore): Long = core.cpu.cregs.readIntern(reg)

        object EIPC : CTRLR(eCTRLR.EIPC.id)
        object EIPSW : CTRLR(eCTRLR.EIPSW.id)
        object FEPC : CTRLR(eCTRLR.FEPC.id)
        object FEPSW : CTRLR(eCTRLR.FEPSW.id)
        object ECR : CTRLR(eCTRLR.ECR.id)
        object PSW : CTRLR(eCTRLR.PSW.id)
        object CTPC : CTRLR(eCTRLR.CTPC.id)
        object CTPSW : CTRLR(eCTRLR.CTPSW.id)
        object DBPC : CTRLR(eCTRLR.DBPC.id)
        object DBPSW : CTRLR(eCTRLR.DBPSW.id)
        object CTBP : CTRLR(eCTRLR.CTBP.id)
        object DIR : CTRLR(eCTRLR.DIR.id)
    }
}