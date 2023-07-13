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
package ru.inforion.lab403.kopycat.cores.x86.instructions.sse

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger

class Psrlq(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>) :
    ASSEInstruction(core, opcode, prefs, *operands) {

    override val mnem = "psrlq"

    override fun executeSSEInstruction() {
        when (op1.dtyp) {
            Datatype.MMXWORD -> {
                // NP 0F D3 /r1 PSRLQ mm, mm/m64
                // NP 0F 73 /2 ib1 PSRLQ mm, imm8

                val a1 = op1.value(core)
                val a2 = op2.value(core)

                if (a2 > 63u) {
                    op1.value(core, 0uL)
                } else {
                    op1.value(core, a1 ushr a2.int)
                }
            }
            else -> {
                // 66 0F D3 /r PSRLQ xmm1, xmm2/m128
                // 66 0F 73 /2 ib PSRLQ xmm1, imm8

                val a1 = op1.extValue(core)
                val a2 = when (op2.dtyp) {
                    Datatype.XMMWORD -> op2.extValue(core)[63..0].ulong
                    else -> op2.value(core)
                }

                if (a2 > 63u) {
                    op1.extValue(core, BigInteger.ZERO)
                } else {
                    op1.extValue(
                        core,
                        BigInteger.ZERO
                            .insert(a1[63..0] ushr a2.int, 63..0)
                            .insert(a1[127..64] ushr a2.int, 127..64)
                    )
                }
            }
        }
    }
}
