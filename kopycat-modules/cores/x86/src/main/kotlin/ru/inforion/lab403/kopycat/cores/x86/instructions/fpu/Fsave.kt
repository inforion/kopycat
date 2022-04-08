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



class Fsave(core: x86Core, opcode: ByteArray, prefs: Prefixes, val dst: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, dst) {
    override val mnem = "fsave"

    override fun execute() {
        val address = dst.effectiveAddress(core)

        if (!core.cpu.cregs.cr0.pe || !core.is32bit)
            TODO("Only for PE and 32-bit mode implemented!")

        core.outl(address +  0u, core.fpu.fwr.FPUControlWord.value)
        core.outl(address +  4u, core.fpu.fwr.FPUStatusWord.value)
        core.outl(address +  8u, core.fpu.fwr.FPUTagWord.value)
        core.outl(address + 12u, core.fpu.fwr.FPUInstructionPointer.value)
        core.outl(address + 16u, 0u)  // FPUInstructionPointer Selector
        core.outl(address + 20u, core.fpu.fwr.FPUDataPointer.value)
        core.outl(address + 24u, 0u)  // FPUDataPointer Selector

        repeat(x86FPU.FPU_STACK_SIZE) {
            core.outl(address + 28u + 10u * it.uint, core.fpu[it])
        }
        // occupied 0x6C bytes (108)

        core.fpu.fwr.FPUControlWord.value = 0x37Fu
        core.fpu.fwr.FPUStatusWord.value = 0u
        core.fpu.fwr.FPUTagWord.value = 0xFFFFu
        core.fpu.fwr.FPUDataPointer.value = 0u
        core.fpu.fwr.FPUInstructionPointer.value = 0u
        core.fpu.fwr.FPULastInstructionOpcode.value = 0u
    }
}