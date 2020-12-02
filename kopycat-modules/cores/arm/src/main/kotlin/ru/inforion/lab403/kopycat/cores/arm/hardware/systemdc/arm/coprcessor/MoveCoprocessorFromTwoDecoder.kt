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
package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.coprcessor

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.isProgramCounter
import ru.inforion.lab403.kopycat.cores.arm.operands.isStackPointer
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


// See A8.8.99
class MoveCoprocessorFromTwoDecoder(
        cpu: AARMCore,
        val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                cp: Int,
                opc1: Int,
                rt: ARMRegister,
                rt2: ARMRegister,
                crm: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: Long): AARMInstruction {
        val cond = cond(data)
        val rt2 = gpr(data[19..16].asInt)
        val rt = gpr(data[15..12].asInt)
        val coproc = data[11..8].toInt()
        val opc1 = data[7..4].toInt()
        val crm = data[3..0].toInt()

        if (coproc and 0b1110 == 0b1010)
            TODO(" SEE \"Advanced SIMD and Floating-point\"")

        if (rt.isProgramCounter(core) || rt2.isProgramCounter(core))
            throw ARMHardwareException.Unpredictable

        if ((rt.isStackPointer(core) || rt2.isStackPointer(core)) && core.cpu.CurrentInstrSet() != AARMCore.InstructionSet.ARM)
            throw ARMHardwareException.Unpredictable

        return constructor(core, data, cond, coproc, opc1, rt, rt2, crm)
    }
}