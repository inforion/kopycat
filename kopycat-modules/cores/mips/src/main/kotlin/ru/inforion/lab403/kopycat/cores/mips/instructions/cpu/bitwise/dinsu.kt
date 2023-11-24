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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.bitwise

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.mips.instructions.RsRtPosSizeInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * dinsu rt, rs, pos, size
 *
 * To merge a right-justified bit field from GPR rs into a specified position in GPR rt.
 */

class dinsu(
    core: MipsCore,
    data: ULong,
    rt: MipsRegister,
    rs: MipsRegister,
    pos: MipsImmediate,
    siz: MipsImmediate
) : RsRtPosSizeInsn(core, data, Type.VOID, rt, rs, pos, siz) {

    override val mnem = "dinsu"

    override fun execute() {

        val lsb32 = lsb + 32
        val msb32 = msb + 32

        vrt = if (lsb32 <= msb32) {
            val high = vrt[63..msb32 + 1]
            val inserted = vrs[msb32 - lsb32..0]
            val low = vrt[lsb32 - 1..0]
            val res = high.shl(msb32 + 1) or inserted.shl(lsb32) or low
            res
        } else 0u

    }

}