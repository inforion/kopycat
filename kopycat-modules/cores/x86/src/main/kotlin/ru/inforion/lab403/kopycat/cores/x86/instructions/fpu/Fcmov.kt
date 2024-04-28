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
package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Fcmov(
    core: x86Core,
    opcode: ByteArray,
    prefs: Prefixes,
    private val movType: MovType,
    vararg operands: AOperand<x86Core>,
) : AFPUInstruction(core, opcode, prefs, *operands) {
    override val mnem = "fcmov" + when (movType) {
        MovType.FCMOVB -> "b"
        MovType.FCMOVE -> "e"
        MovType.FCMOVBE -> "be"
        MovType.FCMOVU -> "u"
        MovType.FCMOVNB -> "nb"
        MovType.FCMOVNE -> "ne"
        MovType.FCMOVNBE -> "nbe"
        MovType.FCMOVNU -> "nu"
    }

    override fun executeFPUInstruction() {
        val flags = core.cpu.flags
        when (movType) {
            MovType.FCMOVB -> moveIf(flags.cf)
            MovType.FCMOVE -> moveIf(flags.zf)
            MovType.FCMOVBE -> moveIf(flags.cf || flags.zf)
            MovType.FCMOVU -> moveIf(flags.pf)

            MovType.FCMOVNB -> moveIf(!flags.cf)
            MovType.FCMOVNE -> moveIf(!flags.zf)
            MovType.FCMOVNBE -> moveIf(!flags.cf && !flags.zf)
            MovType.FCMOVNU -> moveIf(!flags.pf)
        }
    }

    private fun moveIf(condition: Boolean) {
        if (condition)
            op1.extValue(core, op2.extValue(core))
    }

    enum class MovType {
        FCMOVB,
        FCMOVE,
        FCMOVBE,
        FCMOVU,
        FCMOVNB,
        FCMOVNE,
        FCMOVNBE,
        FCMOVNU
    }
}
