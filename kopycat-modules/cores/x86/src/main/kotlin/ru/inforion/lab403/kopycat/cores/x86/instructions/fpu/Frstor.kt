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

import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86FPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.interfaces.*



class Frstor(core: x86Core, opcode: ByteArray, prefs: Prefixes, val src: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, src) {
    override val mnem = "frstor"

    override fun execute() {
        val address = src.effectiveAddress(core)

        if (!core.cpu.cregs.cr0.pe || !core.is32bit)
            TODO("Only for PE and 32-bit mode implemented!")

        core.fpu.fwr.FPUControlWord.value = core.inl(address +  0u)
        core.fpu.fwr.FPUStatusWord.value = core.inl(address +  4u)
        core.fpu.fwr.FPUTagWord.value = core.inl(address +  8u)
        core.fpu.fwr.FPUInstructionPointer.value = core.inl(address + 12u)
//      FPUInstructionPointer Selector = core.read_word(address + 16)
        core.fpu.fwr.FPUDataPointer.value = core.inl(address + 20u)
//      FPUDataPointer Selector = core.read_word(address + 24)

        repeat(x86FPU.FPU_STACK_SIZE) {
            core.fpu[it] = core.inl(address + 28u + 10u * it.uint)
        }

        // occupied 0x6C bytes (108)
    }
}