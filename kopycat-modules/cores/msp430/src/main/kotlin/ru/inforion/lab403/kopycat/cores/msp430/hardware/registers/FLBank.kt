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
import ru.inforion.lab403.kopycat.cores.msp430.enums.Flags
import ru.inforion.lab403.kopycat.cores.msp430.operands.MSP430Register
import ru.inforion.lab403.kopycat.modules.cores.MSP430Core



class FLBank(core : MSP430Core) : ARegistersBank<MSP430Core, Flags>(core, Flags.values(), bits = 16) {
    override val name: String = "Flags Register"

    var value by valueOf(MSP430Register.GPR.r2)

    var c by bitOf(MSP430Register.GPR.r2, Flags.C.bit)
    var z by bitOf(MSP430Register.GPR.r2, Flags.Z.bit)
    var n by bitOf(MSP430Register.GPR.r2, Flags.N.bit)
    var gie by bitOf(MSP430Register.GPR.r2, Flags.GIE.bit)
    var cpuoff by bitOf(MSP430Register.GPR.r2, Flags.CPUOFF.bit)
    var oscoff by bitOf(MSP430Register.GPR.r2, Flags.OSCOFF.bit)
    var scg0 by bitOf(MSP430Register.GPR.r2, Flags.SCG0.bit)
    var scg1 by bitOf(MSP430Register.GPR.r2, Flags.SCG1.bit)
    var v by bitOf(MSP430Register.GPR.r2, Flags.V.bit)
}