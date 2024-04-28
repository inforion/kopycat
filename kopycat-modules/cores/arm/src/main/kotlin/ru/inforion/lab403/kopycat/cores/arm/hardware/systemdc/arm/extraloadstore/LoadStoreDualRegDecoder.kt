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
package ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.arm.extraloadstore

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.arm.enums.Condition
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.arm.instructions.AARMInstruction
import ru.inforion.lab403.kopycat.cores.arm.operands.ARMRegister
import ru.inforion.lab403.kopycat.cores.arm.operands.isProgramCounter
import ru.inforion.lab403.kopycat.modules.cores.AARMCore


class LoadStoreDualRegDecoder(
        cpu: AARMCore,
        val constructor: (
                cpu: AARMCore,
                opcode: ULong,
                cond: Condition,
                index: Boolean,
                add: Boolean,
                wback: Boolean,
                rn: ARMRegister,
                rt: ARMRegister,
                rt2: ARMRegister,
                rm: ARMRegister) -> AARMInstruction) : ADecoder<AARMInstruction>(cpu) {
    override fun decode(data: ULong): AARMInstruction {
        val cond = cond(data)
        val rn = gpr(data[19..16].int)
        val rt = gpr(data[15..12].int)
        val rt2 = gpr(data[15..12].int + 1)
        val rm = gpr(data[3..0].int)

        val index = data[24] == 1uL
        val add = data[23] == 1uL
        val wback = data[24] == 0uL || data[21] == 1uL

        if(data[24] == 0uL && data[21] == 1uL) throw Unpredictable
        if ((rt2.isProgramCounter(core)) || rm.isProgramCounter(core)) throw Unpredictable
        if (wback && (rn.isProgramCounter(core) || rn.desc == rt2.desc)) throw Unpredictable
        if (core.cpu.ArchVersion() < 6 && wback && rm.desc == rn.desc) throw Unpredictable

        return constructor(core, data, cond, index, add, wback, rn, rt, rt2, rm)
    }
}