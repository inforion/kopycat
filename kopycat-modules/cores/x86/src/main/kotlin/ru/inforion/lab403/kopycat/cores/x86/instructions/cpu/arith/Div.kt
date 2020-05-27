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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.arith

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Div(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "div"

    override fun execute() {
        val a2 = op1.value(core)
        if (a2 == 0L)
            throw x86HardwareException.DivisionByZero(core.pc)

        when (op1.dtyp) {
            Datatype.BYTE -> {
                val a1 = core.cpu.regs.ax
                val quotient = a1 / a2
                val remainder = a1 % a2
                if (quotient > 0xFF)
                    throw x86HardwareException.Overflow(core.pc)
                core.cpu.regs.al = quotient
                core.cpu.regs.ah = remainder
            }
            Datatype.WORD -> {
                val a1 = core.cpu.regs.dx.shl(16) or core.cpu.regs.ax
                val quotient = a1 / a2
                val remainder = a1 % a2
                if (quotient > 0xFFFF)
                    throw x86HardwareException.Overflow(core.pc)
                core.cpu.regs.ax = quotient
                core.cpu.regs.dx = remainder
            }
            Datatype.DWORD -> {
                val a1 = (core.cpu.regs.edx.shl(32) or core.cpu.regs.eax)
                val quotient = a1 / a2
                val remainder = a1 % a2
                if (quotient > 0xFFFFFFFF)
                    throw x86HardwareException.Overflow(core.pc)
                core.cpu.regs.eax = quotient
                core.cpu.regs.edx = remainder
            }
            else -> throw GeneralException("Wrong datatype!")
        }
    }
}