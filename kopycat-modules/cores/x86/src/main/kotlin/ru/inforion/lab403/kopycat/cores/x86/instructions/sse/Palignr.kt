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

import ru.inforion.lab403.common.extensions.bigint
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.common.extensions.ushr
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Palignr(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>) :
    ASSEInstruction(core, opcode, prefs, *operands) {

    override val mnem = "palignr"

    override fun executeSSEInstruction() {
        when (op1.dtyp) {
            Datatype.MMXWORD -> {
                val dest = op1.value(core)
                val src = op2.value(core)
                val imm = op3.value(core)

                // temp1[127:0] = CONCATENATE(DEST, SRC)>>(imm8*8)
                // DEST[63:0] = temp1[63:0]
                op1.value(core, (((dest.bigint shl 64) or src.bigint) ushr (imm.int * 8)).ulong)
            }
            else -> {
                val dest = op1.extValue(core)
                val src = op2.extValue(core)
                val imm = op3.value(core)

                // temp1[255:0] := ((DEST[127:0] << 128) OR SRC[127:0])>>(imm8*8);
                // DEST[127:0] := temp1[127:0]
                // DEST[MAXVL-1:128] (Unmodified)
                op1.extValue(
                    core,
                    (((dest shl 128) or src) ushr (imm.int * 8)),
                )
            }
        }
    }
}
