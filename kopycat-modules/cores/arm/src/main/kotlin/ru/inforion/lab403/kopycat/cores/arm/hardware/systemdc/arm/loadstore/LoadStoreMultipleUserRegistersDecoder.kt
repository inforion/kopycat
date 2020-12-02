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
package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.loadstore

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegisterList
import ru.inforion.lab403.kopycat.cores.arm.operands.isProgramCounter
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


// See B9.3.6
class LoadStoreMultipleUserRegistersDecoder(
        cpu: AARMCore,
        private val constructor: (
                cpu: AARMCore,
                opcode: Long,
                cond: Condition,
                increment: Boolean,
                wordhigher: Boolean,
                rn: ARMRegister,
                registers: ARMRegisterList,
                size: Int) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {

    override fun decode(data: Long): AARMInstruction {
        val cond = cond(data)
        val rn = gpr(data[19..16].toInt())
        val registers = list(data[14..0])
        val increment = data[23].toBool()
        val wordhigher = data[24] == data[23]
        if (rn.isProgramCounter(core) || registers.count < 1) throw ARMHardwareException.Unpredictable

        return constructor(core, data, cond, increment, wordhigher, rn, registers, 4)
    }
}