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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.decoders

import ru.inforion.lab403.kopycat.cores.x86.enums.StringPrefix
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Movd
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class MovdDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    private inline fun RMDC.reg(prefs: Prefixes) = if (prefs.operandOverride) rxmm else rmmx

    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.readOpcode()
        val rm = RMDC(s, prefs)

        val (ops, movq) = when (opcode) {
            // NP 0F 6E /r MOVD mm, r/m32
            // NP REX.W + 0F 6E /r MOVQ mm, r/m64
            // 66 0F 6E /r MOVD xmm, r/m32
            // 66 REX.W 0F 6E /r MOVQ xmm, r/m64
            0x6E -> arrayOf(rm.reg(prefs), if (prefs.rexW) rm.m64 else rm.m32) to prefs.rexW
            0x7E -> {
                if (prefs.string == StringPrefix.REPZ) {
                    prefs.string = StringPrefix.NO
                    arrayOf(rm.rxmm, rm.xmmm64) to true
                } else {
                    // NP 0F 7E /r MOVD r/m32, mm
                    // NP REX.W + 0F 7E /r MOVQ r/m64, mm
                    // 66 0F 7E /r MOVD r/m32, xmm
                    // 66 REX.W 0F 7E /r MOVQ r/m64, xmm
                    arrayOf(if (prefs.rexW) rm.m64 else rm.m32, rm.reg(prefs)) to prefs.rexW
                }
            }
            // SSE2; 66 0F D6 /r MOVQ xmm2/m64, xmm1
            0xD6 -> arrayOf(rm.xmmm64, rm.rxmm) to true
            else -> error("Incorrect opcode in decoder $this")
        }

        return Movd(core, s.data, prefs, movq, *ops)
    }
}