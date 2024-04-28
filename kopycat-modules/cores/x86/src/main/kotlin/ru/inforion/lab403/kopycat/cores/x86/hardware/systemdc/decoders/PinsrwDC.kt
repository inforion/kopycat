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
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC

import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Pinsrw
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class PinsrwDC(dev: x86Core) : ADecoder<AX86Instruction>(dev) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        // NP 0F C4 /r ib1 PINSRW mm, r32/m16, imm8
        // 66 0F C4 /r ib PINSRW xmm, r32/m16, imm8

        val opcode = s.readOpcode()
        val rm = RMDC(s, prefs)

        val operands = when (opcode) {
            0xC4 -> arrayOf(if (rm.prefixes.operandOverride) rm.rxmm else rm.rmmx, rm.m16, s.imm8)
            else -> throw GeneralException("Incorrect opcode in decoder $this")
        }

        return Pinsrw(core, s.data, prefs, *operands)
    }
}
