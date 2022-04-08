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

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory.Mov
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class MovDbgDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        // Default 64-bit operand size
        // IDK, why this instruction is 64-bit by default, but OSDev said that it is
        val datatype = when {
            core.is64bit -> {
                prefs.rexW = true
                QWORD
            }
            else -> DWORD
        }

        val ops = when(opcode){
            0x0F -> {
                val sopcode = s.readByte().int
                val raw = s.readByte()
                val dbgid = raw[5..3].int
                val rid = raw[2..0].int
                when (sopcode) {
                    0x21 -> arrayOf(gprr(rid, prefs.rexB, datatype), dreg(dbgid))
                    0x23 -> arrayOf(dreg(dbgid), gprr(rid, prefs.rexB, datatype))
                    else -> throw GeneralException("Incorrect opcode in decoder")
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
        return Mov(core, s.data, prefs, *ops)
    }
}