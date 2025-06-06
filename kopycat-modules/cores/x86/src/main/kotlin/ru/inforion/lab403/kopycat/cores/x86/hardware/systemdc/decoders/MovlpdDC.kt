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
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Movddup
import ru.inforion.lab403.kopycat.cores.x86.instructions.sse.Movlpd
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class MovlpdDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.readOpcode()
        val rm = RMDC(s, prefs)

        return when (prefs.string) {
            StringPrefix.NO -> {
                val operands = when (opcode) {
                    0x12 -> arrayOf(rm.rxmm, rm.m64)
                    0x13 -> arrayOf(rm.m64, rm.rxmm)
                    else -> throw GeneralException("Incorrect opcode in decoder $this")
                }
                Movlpd(core, s.data, prefs, *operands)
            }
            StringPrefix.REPNZ -> {
                val operands = when (opcode) {
                    0x12 -> arrayOf(rm.rxmm, rm.xmmm64)
                    else -> throw GeneralException("Incorrect opcode in decoder $this")
                }
                prefs.string = StringPrefix.NO
                Movddup(core, s.data, prefs, *operands)
            }
            else -> throw GeneralException("Incorrect opcode in decoder $this")
        }
    }
}