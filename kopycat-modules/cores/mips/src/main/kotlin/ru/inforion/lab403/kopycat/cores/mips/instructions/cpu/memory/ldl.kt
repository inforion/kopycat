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
import ru.inforion.lab403.common.extensions.cat
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsDisplacement
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.interfaces.inq
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class ldl(core: MipsCore,
          data: ULong,
          rt: MipsRegister,
          off: MipsDisplacement
) : RtOffsetInsn(core, data, Type.VOID, rt, off) {

    override val mnem = "ldl"

    override fun execute() {
        // I hate mips...
        // TODO: test this!!!!!
        if (core.is32bit) throw MipsHardwareException.RI(core.pc)

        val dataword = vrt
        val vAddr = address
        val byte = (vAddr[2..0] xor core.cpu.bigEndianCPU.bext(3)).int

        val memdoubleword = core.inq(vAddr and 0xFFFF_FFFF_FFFF_FFF8u)

        val hi = memdoubleword[7 + 8 * byte..0]
        val lo = dataword[55 - 8 * byte..0]
        val at = 55 - 8 * byte

        vrt = if (at >= 0) cat(hi, lo, at) else hi

    }
}