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
package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.enums.StringPrefix
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Subpd
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Subps
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Subsd
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Subss
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class SubxxDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        // F3 0F 5C /r SUBSS xmm1, xmm2/m32
        // F2 0F 5C /r SUBSD xmm1, xmm2/m64
        // NP 0F 5C /r SUBPS xmm1, xmm2/m128
        // 66 0F 5C /r SUBPD xmm1, xmm2/m128

        val opcode = s.readOpcode()
        val rm = RMDC(s, prefs)

        if (opcode != 0x5C) {
            throw GeneralException("Incorrect opcode in decoder $this")
        }

        val insn = when (prefs.string) {
            StringPrefix.REPZ -> arrayOf(rm.rxmm, rm.xmmm32).run { Subss(core, s.data, prefs, *this) }
            StringPrefix.REPNZ -> arrayOf(rm.rxmm, rm.xmmm64).run { Subsd(core, s.data, prefs, *this) }
            StringPrefix.NO -> {
                if (prefs.operandOverride) arrayOf(rm.rxmm, rm.xmmpref).run { Subpd(core, s.data, prefs, *this) }
                else arrayOf(rm.rxmm, rm.xmmpref).run { Subps(core, s.data, prefs, *this) }
            }
            else -> TODO("sub** variant")
        }

        prefs.string = StringPrefix.NO
        return insn
    }
}
