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
package ru.inforion.lab403.kopycat.cores.v850es.hardware.memory

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.v850es.enums.GPR
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore



class GPRBank(core: v850ESCore) : ARegistersBank<v850ESCore, GPR>(core, GPR.values(), bits = 32) {
    override val name: String = "CPU General Purpose Registers"

    var r0Zero by valueOf(v850esRegister.GPR.r0)
    var r1AssemblerReserved by valueOf(v850esRegister.GPR.r1)
    var r2 by valueOf(v850esRegister.GPR.r2)
    var r3StackPointer by valueOf(v850esRegister.GPR.r3)
    var r4GlobalPointer by valueOf(v850esRegister.GPR.r4)
    var r5TextPointer by valueOf(v850esRegister.GPR.r5)
    var r6 by valueOf(v850esRegister.GPR.r6)
    var r7 by valueOf(v850esRegister.GPR.r7)
    var r8 by valueOf(v850esRegister.GPR.r8)
    var r9 by valueOf(v850esRegister.GPR.r9)
    var r10 by valueOf(v850esRegister.GPR.r10)
    var r11 by valueOf(v850esRegister.GPR.r11)
    var r12 by valueOf(v850esRegister.GPR.r12)
    var r13 by valueOf(v850esRegister.GPR.r13)
    var r14 by valueOf(v850esRegister.GPR.r14)
    var r15 by valueOf(v850esRegister.GPR.r15)
    var r16 by valueOf(v850esRegister.GPR.r16)
    var r17 by valueOf(v850esRegister.GPR.r17)
    var r18 by valueOf(v850esRegister.GPR.r18)
    var r19 by valueOf(v850esRegister.GPR.r19)
    var r20 by valueOf(v850esRegister.GPR.r20)
    var r21 by valueOf(v850esRegister.GPR.r21)
    var r22 by valueOf(v850esRegister.GPR.r22)
    var r23 by valueOf(v850esRegister.GPR.r23)
    var r24 by valueOf(v850esRegister.GPR.r24)
    var r25 by valueOf(v850esRegister.GPR.r25)
    var r26 by valueOf(v850esRegister.GPR.r26)
    var r27 by valueOf(v850esRegister.GPR.r27)
    var r28 by valueOf(v850esRegister.GPR.r28)
    var r29 by valueOf(v850esRegister.GPR.r29)
    var r30ElementPointer by valueOf(v850esRegister.GPR.r30)
    var r31LinkPointer by valueOf(v850esRegister.GPR.r31)
    var pc by valueOf(v850esRegister.GPR.pc)
}
