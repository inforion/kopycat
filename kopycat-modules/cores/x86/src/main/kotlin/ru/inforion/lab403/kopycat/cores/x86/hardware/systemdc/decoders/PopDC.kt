/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.stack.Pop
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class PopDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        // Default 64-bit operand size
        // All instructions, except far branches, that implicitly reference the RSP
        if (core.is64bit) when (opcode) {
            0x1F, 0x07, 0x17 -> Unit // DS, ES, SS
            else -> prefs.rexW = true
        }

        val op = when (opcode) {
            0x58, 0x59, 0x5A, 0x5B, 0x5C, 0x5D, 0x5E, 0x5F -> gprr(opcode % 0x58, prefs.rexB, prefs.opsize)
            0x8F -> RMDC(s, prefs).mpref
            0x1F -> ds
            0x07 -> es
            0x17 -> ss
            0x0F -> {
                val sopcode = s.readOpcode()
                when (sopcode) {
                    0xA1 -> fs
                    0xA9 -> gs
                    else -> throw GeneralException("Incorrect opcode in decoder $this")
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder $this")
        }
        return Pop(core, s.data, prefs, op)
    }
}