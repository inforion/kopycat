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
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.Fucom
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.Fucomi
import ru.inforion.lab403.kopycat.cores.x86.operands.x86FprRegister
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class FucomDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val currByte = s.readByte()
        val op0 = x86FprRegister(0)
        return when (opcode) {
            // https://www.felixcloutier.com/x86/fucom:fucomp:fucompp
            0xDA -> Fucom(core, s.data, prefs, 2, op0, x86FprRegister(1))
            0xDD -> {
                val popCount = if (currByte[3] == 1uL) 1 else 0
                val regIndex = currByte[2..0].int
                when (regIndex) {
                    //0 -> throw GeneralException("Incorrect opcode in decoder $this")
                    else -> Fucom(core, s.data, prefs, popCount, op0, x86FprRegister(regIndex))
                }
            }
            // https://www.felixcloutier.com/x86/fcomi:fcomip:fucomi:fucomip
            0xDB -> {
                val regIndex = currByte[2..0].int
                when (regIndex) {
                    //0 -> throw GeneralException("Incorrect opcode in decoder $this")
                    else -> Fucomi(core, s.data, prefs, 0, op0, x86FprRegister(regIndex))
                }
            }
            0xDF -> {
                val regIndex = currByte[2..0].int
                when (regIndex) {
                    //0 -> throw GeneralException("Incorrect opcode in decoder $this")
                    else -> Fucomi(core, s.data, prefs, 1, op0, x86FprRegister(regIndex))
                }
            }

            else -> throw GeneralException("Incorrect opcode ${opcode.hex} in decoder")
        }
    }
}
