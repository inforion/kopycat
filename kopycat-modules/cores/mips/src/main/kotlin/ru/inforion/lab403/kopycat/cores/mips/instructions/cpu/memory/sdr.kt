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

import ru.inforion.lab403.common.extensions.bext
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.interfaces.inq
import ru.inforion.lab403.kopycat.interfaces.outq
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class sdr(core: MipsCore,
          data: ULong,
          rt: MipsRegister,
          off: MipsDisplacement
) : RtOffsetInsn(core, data, Type.VOID, rt, off) {

    override val mnem = "sdr"

    override fun execute() {
        if (core.is32bit) throw MipsHardwareException.RI(core.pc)

        val vAddr = address
        val alignAddrLo = vAddr and 0xFFFF_FFFF_FFFF_FFF8u
        val alignAddrHi =  alignAddrLo + 0x8u

        val bytesel = (vAddr[2..0] xor core.cpu.bigEndianCPU.bext(3)).int
        val memdoubleword = core.inq(alignAddrLo)

        val himask = 8 * (8 - bytesel) - 1
        val lowmask = 63 - himask
        val hi = vrt.shl(lowmask)
        val lo = if (lowmask != 0) {
            memdoubleword mask lowmask
        } else {
            memdoubleword
        }

        val result = hi or lo

        core.outq(alignAddrLo, result)
    }
}
