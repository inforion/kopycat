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

import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86FPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.interfaces.*


class Fxrstor(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
    AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "fxrstor"


    inline fun<T> Array<T>.reassign(ctor: (Int, T) -> T) = forEachIndexed { index: Int, value: T ->
        set(index, ctor(index, value))
    }

    private fun readHeader(base: ULong) {
        val fwr = core.fpu.fwr

        fwr.FPUControlWord.value = core.inw(base + 0uL)
        fwr.FPUStatusWord.value = core.inw(base + 2uL)
        fwr.FPUTagWord.value = core.inb(base + 4uL)
        fwr.FPULastInstructionOpcode.value = core.inl(base + 6uL)
        fwr.FPUInstructionPointer.value = core.inq(base + 8uL)
        fwr.FPUDataPointer.value = core.inq(base + 16uL)
        core.config.mxcsr = core.inl(base + 24uL)
        // MXCSR_MASK (32 bits). This mask can be used to adjust values written to the MXCSR register,
        // ensuring that reserved bits are set to 0.
        core.inl(base + 28uL)
    }

    private fun readPUMMX(base: ULong) = (0 until x86FPU.FPU_STACK_SIZE).forEach { i ->
        core.fpu.st(i, core.ine(base + 32uL + (i * 16).uint, 10))
    }

    private fun readXMM(base: ULong, count: Int = 16) = core.sse.xmm.reassign { i, it ->
        if (i < count)
            core.ine(base + 160uL + (i * 16).uint,  Datatype.XMMWORD.bytes)
        else
            it
    }

    private fun Load64BitFxrstore(base: ULong) {
        readHeader(base)
        readPUMMX(base)
        readXMM(base)
    }

    private fun LoadLegacyFxrstore(base: ULong) {
        readHeader(base)
        readPUMMX(base)
        readXMM(base, 8)
    }

    override fun execute() {
        if (core.cpu.cregs.cr0.em || core.cpu.cregs.cr0.ts) {
            throw x86HardwareException.DeviceNotAvailable(core.pc)
        }

        val base = op1.effectiveAddress(core)
        if (core.is64bit)
            Load64BitFxrstore(base)
        else
            LoadLegacyFxrstore(base)
    }

}
