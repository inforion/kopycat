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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.branch

import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.common.extensions.long
import ru.inforion.lab403.common.extensions.long_s
import ru.inforion.lab403.kopycat.cores.mips.instructions.RsOffsetInsn
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

/**
 *
 * BLEZ rs, offset
 */
class blez(
    core: MipsCore,
    data: ULong,
    rs: MipsRegister,
    off: MipsNear
) : RsOffsetInsn(core, data, Type.COND_JUMP, rs, off) {

    override val mnem = "blez"

    override fun execute() {
        core.cpu.branchCntrl.validate()
        val vrsTemp = if (core.is32bit) vrs.int.long_s else vrs.long

        if (vrsTemp <= 0) {
            core.cpu.branchCntrl.schedule(address)
        } else {
            core.cpu.branchCntrl.nop()
        }

    }
}