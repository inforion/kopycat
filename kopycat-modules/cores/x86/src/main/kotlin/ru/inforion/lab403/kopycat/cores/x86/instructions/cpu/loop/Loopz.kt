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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.loop

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.cs
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Loopz(core: x86Core, opcode: ByteArray, prefs: Prefixes, val operand: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, operand) {
    override val mnem = "loopz"

    override fun execute() {
        val ecx = x86Register.gpr(prefs.opsize, x86GPR.ECX.id)
        val count = ecx.value(core) - 1
        ecx.value(core, count)
        if (core.cpu.flags.zf && count != 0L){
            val eip = core.cpu.regs.eip + op1.value(core)
            if (!x86utils.isWithinCodeSegmentLimits(eip))
                throw x86HardwareException.GeneralProtectionFault(core.pc, cs.value(core))
            // TODO: always write to eip?
            core.cpu.regs.eip = eip
        }
    }
}