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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.arith

import ru.inforion.lab403.common.extensions.bigint
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.instructions.RtRsImmInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore


/**
 * DADDI rt, rs, immediate
 *
 * To add a constant to a 64-bit integer. If overflow occurs, then trap
 *
 */

class daddi(
    core: MipsCore,
    data: ULong,
    rt: MipsRegister,
    rs: MipsRegister,
    imm: MipsImmediate
) : RtRsImmInsn(core, data, Type.VOID, rt, rs, imm) {

    override val mnem = "daddi"

    override fun execute() {
//        val temp = (cat(vrs[63], vrs, 63) + imm.usext).bigint

        val t_vrs = vrs.bigint.insert(vrs[63], 64)
        val temp = (t_vrs + imm.usext.bigint)

        if (temp[64] != temp[63]) throw MipsHardwareException.OV(core.pc) else vrt = temp[63..0].ulong
//        vrt = (vrs.long + imm.usext.long).ulong

    }
}