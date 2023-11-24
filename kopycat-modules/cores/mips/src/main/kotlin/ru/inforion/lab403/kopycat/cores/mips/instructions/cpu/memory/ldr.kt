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

/**
 * LDR rt, offset(base)
 *
 * Load Doubleword Right
 *
 * To load the least-significant part of a doubleword from an unaligned memory address
 */

class ldr(core: MipsCore,
          data: ULong,
          rt: MipsRegister,
          off: MipsDisplacement
) : RtOffsetInsn(core, data, Type.VOID, rt, off) {

    override val mnem = "ldr"

    override fun execute() {

        if (core.is32bit) throw MipsHardwareException.RI(core.pc)

        val dataword = vrt
        val vAddr = address

        val byte = (vAddr[2..0] xor core.cpu.bigEndianCPU.bext(3)).int

        // Can't use operand value because to specific handler required
        val memdoubleword = core.inq(vAddr and 0xFFFF_FFFF_FFFF_FFF8u)

        val at = (64 - 8 * byte - 1)
        val hi = dataword[63..(64 - 8*byte)]
        val lo = memdoubleword[63..(8 * byte)]
        vrt = cat(hi, lo, at)

    }
}