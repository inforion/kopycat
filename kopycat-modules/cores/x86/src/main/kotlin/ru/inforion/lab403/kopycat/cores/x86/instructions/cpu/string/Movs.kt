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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.string

import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Displacement
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Movs(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AStringInstruction(core, opcode, prefs, false, *operands) {
    override val mnem = "movs"

    override fun executeStringInstruction() {
        val dst = op1 as x86Displacement
        val src = op2 as x86Displacement

        val data = src.value(core)
        dst.value(core, data)

        var spos = src.reg.value(core)
        var dpos = dst.reg.value(core)
        if (!core.cpu.flags.df) {
            spos += src.dtyp.bytes.uint
            dpos += dst.dtyp.bytes.uint
        } else {
            spos -= src.dtyp.bytes.uint
            dpos -= dst.dtyp.bytes.uint
        }
        src.reg.value(core, spos)
        dst.reg.value(core, dpos)
    }
}