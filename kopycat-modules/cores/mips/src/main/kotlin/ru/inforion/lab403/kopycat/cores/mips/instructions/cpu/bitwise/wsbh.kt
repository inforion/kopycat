/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
import ru.inforion.lab403.common.extensions.signext
import ru.inforion.lab403.kopycat.cores.mips.instructions.RdRtInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * wsbh rd, rt
 *
 * To swap the bytes within each halfword of GPR rt and store the value into GPR rd.
 */
class wsbh(
        core: MipsCore,
        data: ULong,
        rd: MipsRegister,
        rt: MipsRegister) : RdRtInsn(core, data, Type.VOID, rd, rt) {

    override val mnem = "wsbh"

    override fun execute() {
        val intermediate = vrt[31..24].shl(16) or vrt[23..16].shl(24) or vrt[15..8] or vrt[7..0].shl(8)
        vrd = if (core.is32bit) {
            intermediate
        } else {
            intermediate.signext(31)
        }
    }

}