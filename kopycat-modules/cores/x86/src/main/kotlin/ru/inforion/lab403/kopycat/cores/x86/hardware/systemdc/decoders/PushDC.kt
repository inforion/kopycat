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
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC

import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.stack.Push
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class PushDC(core: x86Core) : ADecoder<AX86Instruction>(core) {

    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        var isSSR = false

        // Default 64-bit operand size
        // All instructions, except far branches, that implicitly reference the RSP
        if (core.is64bit) when (opcode) {
            0x0E, 0x16, 0x1E, 0x06 -> Unit // CS, SS, DS, ES
            else -> prefs.rexW = true
        }

        val op = when (opcode) {
            0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57 -> gprr(opcode % 0x50, prefs.rexB, prefs.opsize)
            0xFF -> RMDC(s, prefs).mpref
            0x6A -> s.imm8
            0x68 -> s.imm(prefs)
            0x0E -> { isSSR = true; cs }
            0x16 -> { isSSR = true; ss }
            0x1E -> { isSSR = true; ds }
            0x06 -> { isSSR = true; es }
            0x0F -> {
                val sopcode = s.readOpcode()
                when (sopcode) {
                    0xA0 -> { isSSR = true; fs }
                    0xA8 -> { isSSR = true; gs }
                    else -> throw GeneralException("Incorrect opcode in decoder $this")
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder $this")
        }
        return Push(core, s.data, prefs, isSSR, op)
    }
}