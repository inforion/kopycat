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
package ru.inforion.lab403.kopycat.cores.x86.instructions.sse

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Pinsrw(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>) :
    ASSEInstruction(core, opcode, prefs, *operands) {

    override val mnem = "pinsrw"

    override fun executeSSEInstruction() {
        if (op1.dtyp == Datatype.MMXWORD) {
            // SEL := imm8[1:0]
            // DEST.word[SEL] := src.word[0]

            val dest = op1.value(core)
            val src = op2.value(core)
            val sel = op3.value(core)[1..0].int

            op1.value(
                core,
                dest.insert(src[15..0], (sel + 1) * 16 - 1..sel * 16)
            )
        } else {
            // SEL := imm8[2:0]
            // DEST.word[SEL] := src.word[0]

            val dest = op1.extValue(core)
            val src = op2.value(core)
            val sel = op3.value(core)[2..0].int

            op1.extValue(
                core,
                dest.insert(src[15..0], (sel + 1) * 16 - 1..sel * 16)
            )
        }
    }
}
