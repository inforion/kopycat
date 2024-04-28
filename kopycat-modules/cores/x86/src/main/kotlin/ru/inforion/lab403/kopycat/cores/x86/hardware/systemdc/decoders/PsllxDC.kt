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

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Psllx
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class PsllxDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        // NP 0F F1 /r1 PSLLW mm, mm/m64
        // 66 0F F1 /r PSLLW xmm1, xmm2/m128
        // NP 0F 71 /6 ib PSLLW mm1, imm8
        // 66 0F 71 /6 ib PSLLW xmm1, imm8

        // NP 0F F2 /r1 PSLLD mm, mm/m64
        // 66 0F F2 /r PSLLD xmm1, xmm2/m128
        // NP 0F 72 /6 ib1 PSLLD mm, imm8
        // 66 0F 72 /6 ib PSLLD xmm1, imm8

        // NP 0F F3 /r1 PSLLQ mm, mm/m64
        // 66 0F F3 /r PSLLQ xmm1, xmm2/m128
        // NP 0F 73 /6 ib1 PSLLQ mm, imm8
        // 66 0F 73 /6 ib PSLLQ xmm1, imm8

        val opcode = s.readOpcode()
        val rm = RMDC(s, prefs)

        val variant = when (opcode) {
            0xF1, 0x71 -> WORD
            0xF2, 0x72 -> DWORD
            0xF3, 0x73 -> QWORD
            else -> throw GeneralException("Incorrect opcode in decoder $this")
        }

        val operands = when (opcode) {
            0xF1, 0xF2, 0xF3 -> if (prefs.operandOverride) arrayOf(rm.rxmm, rm.xmmpref) else arrayOf(rm.rmmx, rm.mmxpref)
            0x71, 0x72, 0x73 -> if (prefs.operandOverride) arrayOf(rm.xmmpref, s.imm8) else arrayOf(rm.mmxpref, s.imm8)
            else -> throw GeneralException("Incorrect opcode in decoder $this")
        }

        return Psllx(core, s.data, prefs, variant, *operands)
    }
}
