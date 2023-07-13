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
package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.interfaces.inl
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Fldenv(core: x86Core, opcode: ByteArray, prefs: Prefixes, operand: AOperand<x86Core>) :
    AFPUInstruction(core, opcode, prefs, operand) {
    override val mnem = "fldenv"

    override fun executeFPUInstruction() {
        val address = op1.effectiveAddress(core)
        core.fpu.fwr.FPUControlWord.value = core.inl(address + 0u)
        core.fpu.fwr.FPUStatusWord.value = core.inl(address + 4u)
        core.fpu.fwr.FPUTagWord.value = core.inl(address + 8u)
        core.fpu.fwr.FPUInstructionPointer.value = core.inl(address + 12u)
        core.fpu.fwr.FPULastInstructionOpcode.value = core.inl(address + 18u)
        core.fpu.fwr.FPUDataPointer.value = core.inl(address + 20u)
    }
}
