/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.multiply

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.find
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.isProgramCounter
import ru.inforion.lab403.kopycat.modules.cores.AARMCore



class MultipliesLongDecoder(
        cpu: AARMCore,
        val isUMAAL: Boolean,
        val constructor: (
                cpu: AARMCore,
                opcode: ULong,
                cond: Condition,
                flags: Boolean,
                rdHi: ARMRegister,
                rdLo: ARMRegister,
                rm: ARMRegister,
                rn: ARMRegister) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: ULong): AARMInstruction {
        val cond = find { it.opcode == data[31..28].int } ?: Condition.AL
        val rdHi = gpr(data[19..16].int) //RdHi
        val rdLo = gpr(data[15..12].int) //RdLo
        val rm = gpr(data[11..8].int)
        val rn = gpr(data[3..0].int)
        val setflags = data[20] == 1uL

        if (rdHi.isProgramCounter(core)
                || rn.isProgramCounter(core)
                || rm.isProgramCounter(core)
                || rdLo.isProgramCounter(core))
            throw Unpredictable

        if (rdHi.desc == rdLo.desc) throw Unpredictable
        if (!isUMAAL && core.cpu.ArchVersion() < 6 && (rdHi.isProgramCounter(core) || rdLo.isProgramCounter(core))) throw Unpredictable

        return constructor(core, data, cond, setflags, rdHi, rdLo, rm, rn)
    }
}