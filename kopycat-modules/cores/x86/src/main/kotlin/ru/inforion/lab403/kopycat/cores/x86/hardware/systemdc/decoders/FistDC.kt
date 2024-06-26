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

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC

import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.Fist
import ru.inforion.lab403.kopycat.cores.x86.operands.x86FprRegister
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class FistDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val currByte = s.peekByte()
        val column = currByte[5..3].int
        val rm = RMDC(s, prefs)
        var popCount = 0
        val op0 = when (opcode) {
            0xDB -> when(column){
                2 -> rm.m32
                3 -> {
                    popCount = 1
                    rm.m32
                }
                else -> throw GeneralException("Incorrect opcode in decoder $this")
            }
            0xDF -> when(column){
                2 -> rm.m16
                3 -> {
                    popCount = 1
                    rm.m16
                }
                7 -> {
                    popCount = 1
                    rm.m64
                }
                else -> throw GeneralException("Incorrect opcode in decoder $this")
            }
            else -> throw GeneralException("Incorrect opcode in decoder $this")
        }
        val op1 = x86FprRegister(0)
        return Fist(core, s.data, prefs, popCount, op0, op1)
    }
}