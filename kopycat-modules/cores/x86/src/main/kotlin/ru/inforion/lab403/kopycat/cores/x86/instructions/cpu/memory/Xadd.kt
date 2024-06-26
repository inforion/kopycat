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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.Variable
import ru.inforion.lab403.kopycat.cores.x86.hardware.flags.FlagProcessor
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.pageFault
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Xadd(core: x86Core, opcode: ByteArray, prefs: Prefixes,
           val dest: AOperand<x86Core>, val src: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, dest, src) {
    override val mnem = "xadd"

    override fun execute() {
        pageFault(core) {
            src.write()
            dest.write()
        }

        val result = Variable<x86Core>(0u, dest.dtyp)
        result.value(core, src.value(core) + dest.value(core))
        FlagProcessor.processAddSubCmpFlag(core, result, op1, op2, false)

        src.value(core, dest)
        dest.value(core, result)
    }
}
