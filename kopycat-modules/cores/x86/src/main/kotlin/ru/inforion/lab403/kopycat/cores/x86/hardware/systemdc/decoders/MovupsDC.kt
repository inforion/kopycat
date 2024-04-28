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
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Movsd
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Movss
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Movups
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class MovupsDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.readOpcode()
        val rm = RMDC(s, prefs)

        return when (prefs.string) {
            StringPrefix.REPZ -> {
                val operands = when (opcode) {
                    // F3 0F 10 /r MOVSS xmm1, xmm2
                    // F3 0F 10 /r MOVSS xmm1, m32
                    0x10 -> arrayOf(rm.rxmm, rm.xmmm32)
                    // F3 0F 11 /r MOVSS xmm2/m32, xmm1
                    0x11 -> arrayOf(rm.xmmm32, rm.rxmm)
                    else -> throw GeneralException("Incorrect opcode in decoder $this")
                }

                prefs.string = StringPrefix.NO
                Movss(core, s.data, prefs, *operands)
            }
            StringPrefix.REPNZ -> {
                val operands = when (opcode) {
                    // F2 0F 10 /r MOVSD xmm1, xmm2
                    // F2 0F 10 /r MOVSD xmm1, m64
                    0x10 -> arrayOf(rm.rxmm, rm.xmmm64)
                    // F2 0F 11 /r MOVSD xmm1/m64, xmm2
                    0x11 -> arrayOf(rm.xmmm64, rm.rxmm)
                    else -> throw GeneralException("Incorrect opcode in decoder $this")
                }

                prefs.string = StringPrefix.NO
                Movsd(core, s.data, prefs, *operands)
            }
            StringPrefix.NO -> {
                val operands = when (opcode) {
                    // NP 0F 10 /r MOVUPS xmm1, xmm2/m128
                    0x10 -> arrayOf(rm.rxmm, rm.xmmpref)
                    // NP 0F 11 /r MOVUPS xmm2/m128, xmm1
                    0x11 -> arrayOf(rm.xmmpref, rm.rxmm)
                    else -> throw GeneralException("Incorrect opcode in decoder $this")
                }

                Movups(core, s.data, prefs, *operands)
            }
            else -> TODO("SSE mov* variant")
        }
    }
}
