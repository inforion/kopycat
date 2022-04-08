/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders.mips16

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.instructions.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * Created by shiftdj on 21.06.2021.
 */

class MOVR32DE_EXT(core: MipsCore,
                   val constructFirst: (MipsCore, ULong, MipsImmediate, MipsRegister) -> AMipsInstruction = { _, _, _, _ -> throw NotImplementedError("Not implemented") },
                   val constructSecond: (MipsCore, ULong, MipsImmediate, MipsRegister) -> AMipsInstruction = { _, _, _, _ -> throw NotImplementedError("Not implemented") },
                   val constructThird: (MipsCore, ULong, MipsImmediate, MipsRegister) -> AMipsInstruction = { _, _, _, _ -> throw NotImplementedError("Not implemented") }) : ADecoder(core) {

    override fun decode(data: ULong): AMipsInstruction {
        require(isExtended(data)) { "Can't be applied to not-extended instruction" }
        require(data[10..8] == 0uL) { "Not CPU0" }
        val lowData = getLowData(data)

        val sel = data[7..5].int
        val bits = data[4..8]
        val ry = lowData[7..5].int
        val r32 = lowData[4..0].int

        return when (r32) {
            0b00000 -> {
                require(sel == 1) { "Unknown encoding" }
                constructFirst(core, data, imm(bits), gpr16(ry))
            }
            0b00001 -> {
                require(sel == 1) { "Unknown encoding" }
                constructSecond(core, data, imm(bits), gpr16(ry))
            }
            0b01100 -> {
                require(sel == 0) { "Unknown encoding" }
                constructThird(core, data, imm(bits), gpr16(ry))
            }
            else -> throw GeneralException("Unknown encoding")
        }
    }
}