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
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.arith.Imul
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class ImulDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val rm = RMDC(s, prefs)
        val ops = when (opcode) {
            0xF6 -> arrayOf(rm.m8)
            0xF7 -> arrayOf(rm.mpref)
            0x6B -> arrayOf(rm.rpref, rm.mpref, s.imm8)
            0x69 -> arrayOf(rm.rpref, rm.mpref, s.imm(prefs))
            0x0F -> {
                val sopcode = s.readByte().toInt()
                when (sopcode) {
                    0xAF -> arrayOf(rm.rpref, rm.mpref)
                    else -> throw GeneralException("Incorrect sopcode = $sopcode")
                }
            }
            else -> throw GeneralException("Incorrect opcode = $opcode")
        }
        return Imul(core, s.data, prefs, *ops)
    }
}