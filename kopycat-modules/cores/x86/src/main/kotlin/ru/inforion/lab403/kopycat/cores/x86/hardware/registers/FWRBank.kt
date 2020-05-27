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
package ru.inforion.lab403.kopycat.cores.x86.hardware.registers

import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.x86.enums.FWR
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class FWRBank(core: x86Core) : ARegistersBank<x86Core, FWR>(core, FWR.values(), bits = 16) {
    override val name: String = "FWR Control Registers"

    var FPUStatusWord by valueOf(x86Register.FWR.SWR)
    var FPUControlWord by valueOf(x86Register.FWR.CWR)
    var FPUTagWord by valueOf(x86Register.FWR.TWR)
    var FPUDataPointer by valueOf(x86Register.FWR.FDP)
    var FPUInstructionPointer by valueOf(x86Register.FWR.FIP)
    var FPULastInstructionOpcode by valueOf(x86Register.FWR.LIO)

    override fun reset() {
        super.reset()
        FPUStatusWord = 0
        FPUControlWord = 0
        FPUTagWord = 0
        FPUDataPointer = 0
        FPUInstructionPointer = 0
        FPULastInstructionOpcode = 0
    }
}