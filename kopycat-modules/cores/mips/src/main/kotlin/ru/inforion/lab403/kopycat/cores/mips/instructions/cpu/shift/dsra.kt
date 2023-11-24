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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.shift

import ru.inforion.lab403.common.extensions.ULONG_MAX
import ru.inforion.lab403.common.extensions.ashr
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtSaInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 * DSRA rd, rt, sa
 *
 * Doubleword Shift Right Arithmetic
 */

class dsra(core: MipsCore,
           data: ULong,
           rd: MipsRegister,
           rs: MipsRegister,
           sa: MipsImmediate
) : RdRtSaInsn(core, data, Type.VOID, rd, rs, sa) {

    override val mnem = "dsra"

    override fun execute() {
        if (core.is32bit) throw MipsHardwareException.RI(core.pc)
        // TODO: test
        if (vsa < 32) {
            vrd = if (vrt[63] != 0uL) {
                (ULONG_MAX shl vsa) or (vrt ashr vsa)
            } else {
                vrt ashr vsa
            }
        } else {
            throw IllegalStateException("Illegal value in sa operand")
        }
    }
}