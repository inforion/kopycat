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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.bitwise

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.common.extensions.untruth
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.StringPrefix
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Bsr(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    private val lzcnt = prefs.string == StringPrefix.REPZ
    override val mnem = if (lzcnt) "lzcnt" else "bsr"

    init {
        prefs.string = StringPrefix.NO
    }

    override fun execute() {
        val src = op2.value(core)
        if (src == 0uL) {
            core.cpu.flags.apply {
                if (lzcnt) {
                    cf = true
                    zf = false
                    op1.value(core, op2.dtyp.bits.ulong_z)
                }
                else {
                    zf = true
                    // bsr: DEST is undefined
                }
            }
        } else {
            var temp = prefs.opsize.bits - 1
            while (src[temp] == 0uL) temp -= 1
            core.cpu.flags.apply {
                if (lzcnt) {
                    cf = false
                    temp = prefs.opsize.bits - temp - 1
                    zf = temp.untruth
                } else zf = false
            }
            op1.value(core, temp.ulong_z)
        }
    }
}