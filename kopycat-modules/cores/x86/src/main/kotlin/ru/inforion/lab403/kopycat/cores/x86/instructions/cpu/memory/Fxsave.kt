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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory

import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.PageFaultHelper
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86FPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.pageFault
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.interfaces.*


class Fxsave(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
    AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "fxsave"

    private fun writeLegacyHeader(base: ULong) {
        val fwr = core.fpu.fwr

        core.outw(base + 0uL, fwr.FPUControlWord.value)
        core.outw(base + 2uL, fwr.FPUStatusWord.value)
        core.outb(base + 4uL, fwr.FPUTagWord.value)
        core.outl(base + 6uL, fwr.FPULastInstructionOpcode.value)
        core.outw(base + 8uL, fwr.FPUInstructionPointer.value)
        // x87 FPU Instruction Pointer Selector (16 bits). If CPUID.(EAX=07H,ECX=0H):EBX[bit 13] = 1, the
        // processor deprecates FCS and FDS, and this field is saved as 0000H.
        core.outw(base + 12uL, 0uL)
        core.outl(base + 16uL, fwr.FPUDataPointer.value)
        // x87 FPU Instruction Operand (Data) Pointer Selector (16 bits). If CPUID.(EAX=07H,ECX=0H):EBX[bit
        // 13] = 1, the processor deprecates FCS and FDS, and this field is saved as 0000H.
        core.outw(base + 20uL, 0uL)
        core.outl(base + 24uL, core.config.mxcsr)
        // MXCSR_MASK (32 bits). This mask can be used to adjust values written to the MXCSR register,
        // ensuring that reserved bits are set to 0.
        core.outl(base + 28uL, 0uL)
    }

    private fun PageFaultHelper.pfWriteLegacyHeader(base: ULong) =
        (0uL until 4uL).forEach { outq(base + it * 8uL) }

    private fun writeRexWHeader(base: ULong) {
        val fwr = core.fpu.fwr

        core.outw(base + 0uL, fwr.FPUControlWord.value)
        core.outw(base + 2uL, fwr.FPUStatusWord.value)
        core.outb(base + 4uL, fwr.FPUTagWord.value)
        core.outl(base + 6uL, fwr.FPULastInstructionOpcode.value)
        core.outq(base + 8uL, fwr.FPUInstructionPointer.value)
        core.outq(base + 16uL, fwr.FPUDataPointer.value)
        core.outl(base + 24uL, core.config.mxcsr)
        // MXCSR_MASK (32 bits). This mask can be used to adjust values written to the MXCSR register,
        // ensuring that reserved bits are set to 0.
        core.outl(base + 28uL, 0uL)
    }

    private fun PageFaultHelper.pfWriteRexWHeader(base: ULong) =
        (0uL until 4uL).forEach { outq(base + it * 8uL) }

    private fun writeFPUMMX(base: ULong): Unit = (0 until x86FPU.FPU_STACK_SIZE).forEach { i ->
        core.oute(base + 32uL + (i * 16).uint, core.fpu.st(i), 10)
    }

    private fun PageFaultHelper.pfWriteFPUMMX(base: ULong) = (0 until x86FPU.FPU_STACK_SIZE).forEach { i ->
        oute(base + 32uL + (i * 16).uint, 10)
    }

    private fun writeXMM(base: ULong, count: Int = 16) = core.sse.xmm.take(count).forEachIndexed { i, xmm ->
        core.oute(base + 160uL + (i * 16).uint, xmm, Datatype.XMMWORD.bytes)
//        core.outq(base + 160uL + (i * 16).uint, xmm.ulong)
//        core.outq(base + 168uL + (i * 16).uint, (xmm ushr 64).ulong)
    }

    private fun PageFaultHelper.pfWriteXMM(base: ULong, count: Int = 16) = (0 until count).forEach {
        oute(base + 160uL + (it * 16).uint, Datatype.XMMWORD.bytes)
    }

    private fun save64BitPromotedFxsave(base: ULong) {
        pageFault(core) {
            pfWriteRexWHeader(base)
            pfWriteFPUMMX(base)
            pfWriteXMM(base)
        }

        writeRexWHeader(base)
        writeFPUMMX(base)
        writeXMM(base)
    }

    private fun Save64BitDefaultFxsave(base: ULong) {
        pageFault(core) {
            pfWriteLegacyHeader(base)
            pfWriteFPUMMX(base)
            pfWriteXMM(base)
        }

        writeLegacyHeader(base)
        writeFPUMMX(base)
        writeXMM(base)
    }

    private fun SaveLegacyFxsave(base: ULong) {
        pageFault(core) {
            pfWriteLegacyHeader(base)
            pfWriteFPUMMX(base)
            pfWriteXMM(base, 8)
        }

        writeLegacyHeader(base)
        writeFPUMMX(base)
        writeXMM(base, 8)
    }

    override fun execute() {
        if (core.cpu.cregs.cr0.em || core.cpu.cregs.cr0.ts) {
            throw x86HardwareException.DeviceNotAvailable(core.pc)
        }

        val base = op1.effectiveAddress(core)
        if (core.is64bit) {
            if (prefs.rexW)
                save64BitPromotedFxsave(base)
            else
                Save64BitDefaultFxsave(base)
        }
        else
            SaveLegacyFxsave(base)
    }
}
