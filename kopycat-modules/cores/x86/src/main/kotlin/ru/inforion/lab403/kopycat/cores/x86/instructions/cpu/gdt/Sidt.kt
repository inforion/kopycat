/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.gdt

import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class Sidt(core: x86Core, opcode: ByteArray, prefs: Prefixes, operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, operand) {
    override val mnem = "sidt"

    override fun execute() {
        var data = 0L
        if (prefs.is16BitOperandMode) {
            data = data.insert(core.cop.idtr.limit, 15..0)
            data = data.insert(core.cop.idtr.base, 39..16)
        } else {
            data = data.insert(core.cop.idtr.limit, 15..0)
            data = data.insert(core.cop.idtr.base, 47..16)
        }

        val ssr = op1.ssr
        val ea = op1.effectiveAddress(core)

        core.write(Datatype.FWORD, ea, data, ssr.reg)
    }
}