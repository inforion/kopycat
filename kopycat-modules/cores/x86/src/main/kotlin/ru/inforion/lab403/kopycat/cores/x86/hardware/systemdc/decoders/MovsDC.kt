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
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.string.Movs
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class MovsDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val src = xsi(prefs.addrsize)
        val dst = xdi(prefs.addrsize)
        val ops = when (opcode) {
            0xA4 -> arrayOf(
                    x86Displacement(Datatype.BYTE, dst, es),
                    x86Displacement(Datatype.BYTE, src, prefs))
            0xA5 -> arrayOf(
                    x86Displacement(prefs.opsize, dst, es),
                    x86Displacement(prefs.opsize, src, prefs))
            else -> throw GeneralException("Incorrect opcode in decoder $this")
        }
        return Movs(core, s.data, prefs, *ops)
    }
}