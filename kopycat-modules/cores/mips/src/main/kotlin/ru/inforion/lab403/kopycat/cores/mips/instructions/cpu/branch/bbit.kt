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
package ru.inforion.lab403.kopycat.cores.mips.instructions.cpu.branch

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.mips.instructions.AMipsInstruction
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsImmediate
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsNear
import ru.inforion.lab403.kopycat.cores.mips.operands.MipsRegister
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class bbit(
    core: MipsCore,
    data: ULong,
    private val set: Boolean,
    private val add32: Boolean,
    private val rs: MipsRegister,
    private val p: MipsImmediate,
    private val offt: MipsNear,
) : AMipsInstruction(core, data, Type.COND_JUMP, rs, p, offt) {
    override val mnem = "bbit" + (if (set) "1" else "0") + if (add32) "32" else ""

    private inline val vrs: ULong get() = rs.value(core)

    private inline val address: ULong get() = when {
        core.is32bit -> (core.cpu.pc + size) + offt.offset
        else -> core.cpu.pc + size.uint + offt.usext(core)
    }

    override fun execute() {
        core.cpu.branchCntrl.validate()
        if (vrs[p.value.int + if (add32) 32 else 0].truth == set) {
            core.cpu.branchCntrl.schedule(address)
        } else {
            core.cpu.branchCntrl.nop()
        }
    }
}