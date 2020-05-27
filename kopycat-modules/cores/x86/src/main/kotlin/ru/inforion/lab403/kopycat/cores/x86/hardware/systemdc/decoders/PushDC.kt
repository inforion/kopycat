/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
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
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.stack.Push
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.*
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class PushDC(core: x86Core) : ADecoder<AX86Instruction>(core) {

    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        var isSSR = false
        val op = when (opcode) {
            0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57 -> x86Register.gpr(prefs.opsize, opcode % 0x50)
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
                    else -> throw GeneralException("Incorrect opcode in decoder")
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return Push(core, s.data, prefs, isSSR, op)
    }
}