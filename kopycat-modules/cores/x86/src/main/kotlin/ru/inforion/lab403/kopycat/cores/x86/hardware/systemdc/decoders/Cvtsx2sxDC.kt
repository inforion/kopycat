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
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Cvtpd2ps
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Cvtsd2ss
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Cvtss2sd
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Cvtsx2sxDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.readOpcode()
        val rm = RMDC(s, prefs)

        if (opcode != 0x5A) {
            throw GeneralException("Incorrect opcode in decoder $this")
        }

        val insn = if (prefs.operandOverride) {
            arrayOf(rm.rxmm, rm.xmmpref).run { Cvtpd2ps(core, s.data, prefs, *this) }
        } else when (prefs.string) {
            StringPrefix.REPNZ -> arrayOf(rm.rxmm, rm.xmmm64).run { Cvtsd2ss(core, s.data, prefs, *this) }
            StringPrefix.REPZ -> arrayOf(rm.rxmm, rm.xmmm32).run { Cvtss2sd(core, s.data, prefs, *this) }
            else -> TODO("cvts*2s* variant")
        }

        prefs.string = StringPrefix.NO
        return insn
    }
}
