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
package ru.inforion.lab403.kopycat.cores.msp430.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.msp430.enums.MSP430GPR
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Register
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core



//TODO: there are some registers that not of general purpose
class GPRBank(core: MSP430Core) : ARegistersBank<MSP430Core, MSP430GPR>(core , MSP430GPR.values(), bits = 16) {
    override val name: String = "CPU General Purpose Registers"

    var r0ProgramCounter by valueOf(MSP430Register.GPR.r0)
    var r1StackPointer by valueOf(MSP430Register.GPR.r1)
    var r2StatusRegister by valueOf(MSP430Register.GPR.r2)
    var r3ConstantGenerator by valueOf(MSP430Register.GPR.r3)
    var r4 by valueOf(MSP430Register.GPR.r4)
    var r5 by valueOf(MSP430Register.GPR.r5)
    var r6 by valueOf(MSP430Register.GPR.r6)
    var r7 by valueOf(MSP430Register.GPR.r7)
    var r8 by valueOf(MSP430Register.GPR.r8)
    var r9 by valueOf(MSP430Register.GPR.r9)
    var r10 by valueOf(MSP430Register.GPR.r10)
    var r11 by valueOf(MSP430Register.GPR.r11)
    var r12 by valueOf(MSP430Register.GPR.r12)
    var r13 by valueOf(MSP430Register.GPR.r13)
    var r14 by valueOf(MSP430Register.GPR.r14)
    var r15 by valueOf(MSP430Register.GPR.r15)
}