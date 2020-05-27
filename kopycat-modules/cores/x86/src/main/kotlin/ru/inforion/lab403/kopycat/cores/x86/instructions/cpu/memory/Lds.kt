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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.ds.value
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Lds(core: x86Core, opcode: ByteArray, prefs: Prefixes, op1: AOperand<x86Core>, op2: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, op1, op2) {
    override val mnem = "lds"

    override fun execute() {
        val a2 = op2.value(core)
        if(prefs.is16BitOperandMode){
            op1.value(core, a2[15..0])
            value(core, a2[31..16])
        } else {
            op1.value(core, a2[31..0])  // always use 31..0 (snapped in value)
            value(core, a2[47..32])
        }
    }
}