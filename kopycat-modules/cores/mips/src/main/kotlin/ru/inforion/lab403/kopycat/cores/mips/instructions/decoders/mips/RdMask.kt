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
package ru.inforion.lab403.kopycat.cores.mips.instructions.decoders.mips

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.instructions.decoders.ADecoder
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * RDDSP, WRDSP
 */

// Have you ever experienced a sudden urge to rearrange things?
enum class Position { MLEFT, MRIGHT }

class RdMask(
    core: MipsCore,
    val construct: (MipsCore, ULong, MipsRegister, MipsImmediate) -> AMipsInstruction,
    private val maskPos: Position

) : ADecoder(core) {
    override fun decode(data: ULong): AMipsInstruction {
        val rd = when (maskPos) {
            Position.MLEFT -> data[15..11].int
            Position.MRIGHT -> data[25..21].int
        }
        val mask = when (maskPos) {
            Position.MLEFT -> data[25..16]
            Position.MRIGHT -> data[20..11]
        }

        return construct(core, data, gpr(rd), imm(mask))
    }
}