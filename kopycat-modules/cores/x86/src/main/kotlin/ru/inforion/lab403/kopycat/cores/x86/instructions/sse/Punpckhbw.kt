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

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger

class Punpckhbw(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>) :
    ASSEInstruction(core, opcode, prefs, *operands) {

    override val mnem = "punpckhbw"

    override fun executeSSEInstruction() {
        val dest = op1.extValue(core)
        val src = op2.extValue(core)

        val result = BigInteger.ZERO
            .insert(dest[71..64], 7..0)
            .insert(src[71..64], 15..8)
            .insert(dest[79..72], 23..16)
            .insert(src[79..72], 31..24)
            .insert(dest[87..80], 39..32)
            .insert(src[87..80], 47..40)
            .insert(dest[95..88], 55..48)
            .insert(src[95..88], 63..56)
            .insert(dest[103..96], 71..64)
            .insert(src[103..96], 79..72)
            .insert(dest[111..104], 87..80)
            .insert(src[111..104], 95..88)
            .insert(dest[119..112], 103..96)
            .insert(src[119..112], 111..104)
            .insert(dest[127..120], 119..112)
            .insert(src[127..120], 127..120)

        op1.extValue(core, result)
    }
}
