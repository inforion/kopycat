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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.interfaces.inl
import ru.inforion.lab403.kopycat.interfaces.inq
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


/**
 *
 * LWL rt, offset(base)
 */
class lwl(core: MipsCore,
          data: ULong,
          rt: MipsRegister,
          off: MipsDisplacement) : RtOffsetInsn(core, data, Type.VOID, rt, off) {

    override val mnem = "lwl"

    override fun execute() {
        // I hate mips...
        val dataword = vrt

        val vAddr = address

        val byte = (vAddr[1..0] xor core.cpu.bigEndianCPU.bext(2)).int

        vrt = if (core.is32bit) {
            // Can't use operand value because to specific handler required
            val memword = core.inl(vAddr and 0xFFFFFFFCu)

            val hi = memword[8 * byte + 7..0]
            val lo = dataword[23 - 8 * byte..0]
            hi.shl(24 - 8 * byte) or lo         // temp

        } else {
            // throw IllegalStateException("lwl is not implemented for 64bit regs")
            // TODO: test this!!!!!!
            val word = (vAddr[2] xor core.cpu.bigEndianCPU.ulong_z).int

            val memdoubleword = core.inq(vAddr and 0xFFFF_FFFF_FFFF_FFFCu)

            val hi = memdoubleword[(31 + 32 * word - 8 * byte)..32 * word]
            val lo = dataword[23 - 8 * byte .. 0]
            val temp = hi.shl(24 - 8 * byte) or lo
            temp.signext(31)
        }
    }
}