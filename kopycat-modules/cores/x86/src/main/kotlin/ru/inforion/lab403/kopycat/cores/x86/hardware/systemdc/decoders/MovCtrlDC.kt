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

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory.Mov
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class MovCtrlDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        val ops = when(opcode){
            0x0F -> {
                val sopcode = s.readByte().toInt()
                val raw = s.readByte()
                val ctrlid = raw[5..3].toInt()
                if (ctrlid == 1 || ctrlid > 4)
                    throw GeneralException("Can't operate not with CR0, CR2-CR4")
                val rid = raw[2..0].toInt()
                when (sopcode) {
                    0x20 -> arrayOf(x86Register.gpr(prefs.opsize, rid), x86Register.creg(ctrlid))
                    0x22 -> arrayOf(x86Register.creg(ctrlid), x86Register.gpr(prefs.opsize, rid))
                    else -> throw GeneralException("Incorrect opcode in decoder")
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return Mov(core, s.data, prefs, *ops)
    }
}