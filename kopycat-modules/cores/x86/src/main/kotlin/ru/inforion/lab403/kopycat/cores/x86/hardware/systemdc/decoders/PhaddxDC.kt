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
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Phaddx
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class PhaddxDC(dev: x86Core) : ADecoder<AX86Instruction>(dev) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        // NP 0F 38 01 /r1 PHADDW mm1, mm2/m64
        // 66 0F 38 01 /r PHADDW xmm1, xmm2/m128
        // NP 0F 38 02 /r PHADDD mm1, mm2/m64
        // 66 0F 38 02 /r PHADDD xmm1, xmm2/m128

        val opcode = s.last
        val rm = RMDC(s, prefs)

        val typ = when (opcode) {
            0x01 -> Datatype.WORD
            0x02 -> Datatype.DWORD
            else -> throw GeneralException("Incorrect opcode in decoder $this")
        }

        val operands = if (rm.prefixes.operandOverride) arrayOf(
            rm.rxmm,
            rm.xmmpref,
        ) else arrayOf(
            rm.rmmx,
            rm.mmxm64,
        )

        return Phaddx(
            core,
            s.data,
            prefs,
            typ,
            *operands,
        )
    }
}
