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

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.*
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class PsraxDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        // NP 0F E1 /r1 PSRAW mm, mm/m64
        // 66 0F E1 /r PSRAW xmm1, xmm2/m128
        // NP 0F 71 /4 ib1 PSRAW mm, imm8
        // 66 0F 71 /4 ib PSRAW xmm1, imm8

        // NP 0F E2 /r1 PSRAD mm, mm/m64
        // 66 0F E2 /r PSRAD xmm1, xmm2/m128
        // NP 0F 72 /4 ib1 PSRAD mm, imm8
        // 66 0F 72 /4 ib PSRAD xmm1, imm8

        val opcode = s.readOpcode()
        val rm = RMDC(s, prefs)

        val variant = when (opcode) {
            0xE1, 0x71 -> Datatype.WORD
            0xE2, 0x72 -> Datatype.DWORD
            else -> throw GeneralException("Incorrect opcode in decoder $this")
        }

        val operands = when (opcode) {
            0xE1, 0xE2 -> if (prefs.operandOverride) arrayOf(rm.rxmm, rm.xmmpref) else arrayOf(rm.rmmx, rm.mmxpref)
            0x71, 0x72 -> arrayOf(if (prefs.operandOverride) rm.xmmpref else rm.mmxpref, s.imm8)
            else -> throw GeneralException("Incorrect opcode in decoder $this")
        }

        return Psrax(core, s.data, prefs, variant, *operands)
    }
}
