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

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.GPRBL.cl
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class ShxdDC(dev: x86Core, val construct: (x86Core, ByteArray, Prefixes, Array<AOperand<x86Core>>) -> AX86Instruction) :
        ADecoder<AX86Instruction>(dev) {

    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.readByte().asInt
        val rm = RMDC(s, prefs)
        val ops = when (opcode mask 3) {
            4 -> arrayOf(rm.mpref, rm.rpref, s.imm8)  // 0xA4 0xAC
            5 -> arrayOf(rm.mpref, rm.rpref, cl)      // 0xA5 0xAD
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return construct(core, s.data, prefs, ops)
    }
}