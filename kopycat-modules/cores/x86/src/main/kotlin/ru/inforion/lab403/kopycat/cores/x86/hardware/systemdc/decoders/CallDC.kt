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
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.RMDC
import ru.inforion.lab403.kopycat.cores.x86.hardware.x86OperandStream
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch.Call
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Far
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class CallDC(core: x86Core) : ADecoder<AX86Instruction>(core) {
    override fun decode(s: x86OperandStream, prefs: Prefixes): AX86Instruction {
        val opcode = s.last
        return when(opcode) {
            0xE8 -> Call(core, s.near(prefs), s.data, prefs, isRelative = true)
            0x9A -> Call(core, s.far(prefs), s.data, prefs, isRelative = false, isFar = true)
            0xFF -> {
                val rm = RMDC(s, prefs)
                val sopcode = s.peekOpcode()
                val row = sopcode[5..3]
                when (row) {
                    0x02 -> Call(core, rm.mpref, s.data, prefs, isRelative = false, isFar = false)
                    0x03 -> {
                        val mpref = rm.mpref
                        val where = mpref.effectiveAddress(core)
                        val ssr = mpref.ssr
                        val address = core.read(prefs.opsize, where, ssr.reg)
                        val far_ss = core.inw(where + prefs.opsize.bytes, ssr.reg)
                        Call(core, x86Far(address, far_ss), s.data, prefs, isRelative = false, isFar = true)
                    }
                    else -> throw GeneralException("Incorrect row = $row")
                }
            }
            else -> throw GeneralException("Incorrect opcode in decoder")
        }
    }
}