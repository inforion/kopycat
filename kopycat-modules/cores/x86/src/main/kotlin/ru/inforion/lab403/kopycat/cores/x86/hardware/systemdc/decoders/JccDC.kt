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
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class JccDC(core: x86Core, val construct: (x86Core, ByteArray, Prefixes, AOperand<x86Core>) -> AX86Instruction) :
        ADecoder<AX86Instruction>(core) {

    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val op = when (opcode) {
            0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77,
            0x78, 0x79, 0x7A, 0x7B, 0x7C, 0x7D, 0x7E, 0x7F, 0xE3 -> s.near8(prefs)
            0x0F -> {
                val sopcode = s.readByte().toInt()
                when (sopcode) {
                    0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87,
                    0x88, 0x89, 0x8A, 0x8B, 0x8C, 0x8D, 0x8E, 0x8F -> s.near(prefs)
                    else -> throw GeneralException("Incorrect opcode in decoder")
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return construct(core, s.data, prefs, op)
    }
}