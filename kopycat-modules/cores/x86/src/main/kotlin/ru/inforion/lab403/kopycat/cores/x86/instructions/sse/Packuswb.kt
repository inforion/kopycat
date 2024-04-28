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
import java.math.BigInteger

class Packuswb(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>) :
    ASSEInstruction(core, opcode, prefs, *operands) {

    override val mnem = "packuswb"

    private fun saturateSignedWordToUnsignedByte(data: Short) = if (data >= 0x100L) {
        0xFF
    } else if (data <= -1) {
        0x00
    } else {
        data
    }

    override fun executeSSEInstruction() {
        when (op1.dtyp) {
            Datatype.MMXWORD -> {
                val a1 = op1.value(core)
                val a2 = op2.value(core)

                val result = 0uL
                    .insert(saturateSignedWordToUnsignedByte(a1[15..0].short).ulong_z, 7..0)
                    .insert(saturateSignedWordToUnsignedByte(a1[31..16].short).ulong_z, 15..8)
                    .insert(saturateSignedWordToUnsignedByte(a1[47..32].short).ulong_z, 23..16)
                    .insert(saturateSignedWordToUnsignedByte(a1[63..48].short).ulong_z, 31..24)
                    .insert(saturateSignedWordToUnsignedByte(a2[15..0].short).ulong_z, 39..32)
                    .insert(saturateSignedWordToUnsignedByte(a2[31..16].short).ulong_z, 47..40)
                    .insert(saturateSignedWordToUnsignedByte(a2[47..32].short).ulong_z, 55..48)
                    .insert(saturateSignedWordToUnsignedByte(a2[63..48].short).ulong_z, 63..56)

                op1.value(core, result)
            }
            else -> {
                val a1 = op1.extValue(core)
                val a2 = op2.extValue(core)

                val result = BigInteger.ZERO
                    .insert(saturateSignedWordToUnsignedByte(a1[15..0].short).ulong_z, 7..0)
                    .insert(saturateSignedWordToUnsignedByte(a1[31..16].short).ulong_z, 15..8)
                    .insert(saturateSignedWordToUnsignedByte(a1[47..32].short).ulong_z, 23..16)
                    .insert(saturateSignedWordToUnsignedByte(a1[63..48].short).ulong_z, 31..24)
                    .insert(saturateSignedWordToUnsignedByte(a1[79..64].short).ulong_z, 39..32)
                    .insert(saturateSignedWordToUnsignedByte(a1[95..80].short).ulong_z, 47..40)
                    .insert(saturateSignedWordToUnsignedByte(a1[111..96].short).ulong_z, 55..48)
                    .insert(saturateSignedWordToUnsignedByte(a1[127..112].short).ulong_z, 63..56)
                    .insert(saturateSignedWordToUnsignedByte(a2[15..0].short).ulong_z, 71..64)
                    .insert(saturateSignedWordToUnsignedByte(a2[31..16].short).ulong_z, 79..72)
                    .insert(saturateSignedWordToUnsignedByte(a2[47..32].short).ulong_z, 87..80)
                    .insert(saturateSignedWordToUnsignedByte(a2[63..48].short).ulong_z, 95..88)
                    .insert(saturateSignedWordToUnsignedByte(a2[79..64].short).ulong_z, 103..96)
                    .insert(saturateSignedWordToUnsignedByte(a2[95..80].short).ulong_z, 111..104)
                    .insert(saturateSignedWordToUnsignedByte(a2[111..96].short).ulong_z, 119..112)
                    .insert(saturateSignedWordToUnsignedByte(a2[127..112].short).ulong_z, 127..120)

                op1.extValue(core, result)
            }
        }
    }
}
