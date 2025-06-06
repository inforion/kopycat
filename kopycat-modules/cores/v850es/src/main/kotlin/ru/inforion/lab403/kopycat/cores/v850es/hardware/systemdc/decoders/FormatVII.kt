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
package ru.inforion.lab403.kopycat.cores.v850es.hardware.systemdc.decoders

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.v850es.constructor
import ru.inforion.lab403.kopycat.cores.v850es.instructions.AV850ESInstruction
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esDisplacement
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esImmediate
import ru.inforion.lab403.kopycat.cores.v850es.operands.v850esRegister
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore

/**
 *
 * lsb - index in opcode of least significant bit (LSB) of 16-bit displacement. If bit == -1 then LSB = 0
 */
class FormatVII(
        core: v850ESCore,
        val construct: constructor, val dtyp: Datatype, val lsb: Int = -1
) : ADecoder<AV850ESInstruction>(core) {

    override fun decode(s: ULong): AV850ESInstruction {
        val reg1 = v850esRegister.gpr(s[4..0].int)
        val reg2 = v850esRegister.gpr(s[15..11].int)
        val bit = if (lsb != -1) s[lsb] else 0u
        val offset = cat(s[31..17], bit, 0)
        val imm = v850esImmediate(Datatype.DWORD, offset.signextRenameMeAfter(15), true)
        val disp = v850esDisplacement(dtyp, reg1, imm)
        return construct(core, 4, arrayOf(reg2, disp))
    }
}