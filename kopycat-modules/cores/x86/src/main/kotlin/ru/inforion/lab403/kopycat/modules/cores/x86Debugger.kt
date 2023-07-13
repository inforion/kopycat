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
package ru.inforion.lab403.kopycat.modules.cores

import ru.inforion.lab403.common.extensions.UNDEF
import ru.inforion.lab403.common.extensions.bigint
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.modules.BUS32
import java.math.BigInteger

class x86Debugger constructor(parent: Module, name: String, dbgAreaSize: ULong = BUS32):
    Debugger(parent, name, dbgAreaSize) {

    inline val x86 get() = core as x86Core

    override fun ident() = "i386"

    override fun target() = "i386-avx.xml"

    private val regCount = 0x20

    override fun registers() = List(regCount) { regRead(it) }

    override fun regSize(index: Int) = when (index) {
        in 0x10..0x17 -> FPU80
        in 0x20..0x27 -> XMMWORD
        else -> DWORD
    }

    override fun regRead(index: Int) = with(x86) {
        when (index) {
            0x00 -> cpu.regs.eax.value.bigint
            0x01 -> cpu.regs.ecx.value.bigint
            0x02 -> cpu.regs.edx.value.bigint
            0x03 -> cpu.regs.ebx.value.bigint
            0x04 -> cpu.regs.esp.value.bigint
            0x05 -> cpu.regs.ebp.value.bigint
            0x06 -> cpu.regs.esi.value.bigint
            0x07 -> cpu.regs.edi.value.bigint
            0x08 -> cpu.pc.bigint // We can't make better solution now... :(
            0x09 -> cpu.flags.eflags.value.bigint
            0x0A -> cpu.sregs.cs.value.bigint
            0x0B -> cpu.sregs.ss.value.bigint
            0x0C -> cpu.sregs.ds.value.bigint
            0x0D -> cpu.sregs.es.value.bigint
            0x0E -> cpu.sregs.fs.value.bigint
            0x0F -> cpu.sregs.gs.value.bigint
            0x10 -> fpu.st(0)
            0x11 -> fpu.st(1)
            0x12 -> fpu.st(2)
            0x13 -> fpu.st(3)
            0x14 -> fpu.st(4)
            0x15 -> fpu.st(5)
            0x16 -> fpu.st(6)
            0x17 -> fpu.st(7)
            0x18 -> fpu.fwr.FPUControlWord.value.bigint
            0x19 -> fpu.fwr.FPUStatusWord.value.bigint
            0x1A -> fpu.fwr.FPUTagWord.value.bigint
            0x1B -> fpu.fwr.FPUInstructionPointer.value.bigint
            0x1C -> BigInteger.ZERO
            0x1D -> fpu.fwr.FPUDataPointer.value.bigint
            0x1E -> BigInteger.ZERO
            0x1F -> fpu.fwr.FPULastInstructionOpcode.value.bigint
            0x20 -> sse.xmm[0]
            0x21 -> sse.xmm[1]
            0x22 -> sse.xmm[2]
            0x23 -> sse.xmm[3]
            0x24 -> sse.xmm[4]
            0x25 -> sse.xmm[5]
            0x26 -> sse.xmm[6]
            0x27 -> sse.xmm[7]
            0x28 -> config.mxcsr.bigint
            else -> 0xDEADBEEFu.bigint
        }
    }

    override fun regWrite(index: Int, value: BigInteger) = with(x86) {
        when (index) {
            0x00 -> cpu.regs.eax.value = value.ulong
            0x01 -> cpu.regs.ecx.value = value.ulong
            0x02 -> cpu.regs.edx.value = value.ulong
            0x03 -> cpu.regs.ebx.value = value.ulong
            0x04 -> cpu.regs.esp.value = value.ulong
            0x05 -> cpu.regs.ebp.value = value.ulong
            0x06 -> cpu.regs.esi.value = value.ulong
            0x07 -> cpu.regs.edi.value = value.ulong
            0x08 -> {
                log.warning { "Setup EIP register, this action works only within one segment..." }
                log.warning { "IDA Pro probably show you a bloody mess ... don't worry it's ok just go ahead!" }
                cpu.pc = value.ulong
                cpu.resetFault()
            }
            0x09 -> cpu.flags.eflags.value = value.ulong
            0x0A -> cpu.sregs.cs.value = value.ulong
            0x0B -> cpu.sregs.ss.value = value.ulong
            0x0C -> cpu.sregs.ds.value = value.ulong
            0x0D -> cpu.sregs.es.value = value.ulong
            0x0E -> cpu.sregs.fs.value = value.ulong
            0x0F -> cpu.sregs.gs.value = value.ulong
            0x10 -> fpu.st(0, value)
            0x11 -> fpu.st(1, value)
            0x12 -> fpu.st(2, value)
            0x13 -> fpu.st(3, value)
            0x14 -> fpu.st(4, value)
            0x15 -> fpu.st(5, value)
            0x16 -> fpu.st(6, value)
            0x17 -> fpu.st(7, value)
            0x18 -> fpu.fwr.FPUControlWord.value = value.ulong
            0x19 -> fpu.fwr.FPUStatusWord.value = value.ulong
            0x1A -> fpu.fwr.FPUTagWord.value = value.ulong
            0x1B -> fpu.fwr.FPUInstructionPointer.value = value.ulong
            0x1C -> log.warning { "FPU instruction pointer segment [id=0x${index.hex}] not supported value=0x${value.hex} ignored" }
            0x1D -> fpu.fwr.FPUDataPointer.value = value.ulong
            0x1E -> log.warning { "FPU data pointer segment [id=0x${index.hex}] not supported value=0x${value.hex} ignored" }
            0x1F -> fpu.fwr.FPULastInstructionOpcode.value = value.ulong
            0x20 -> sse.xmm[0] = value
            0x21 -> sse.xmm[1] = value
            0x22 -> sse.xmm[2] = value
            0x23 -> sse.xmm[3] = value
            0x24 -> sse.xmm[4] = value
            0x25 -> sse.xmm[5] = value
            0x26 -> sse.xmm[6] = value
            0x27 -> sse.xmm[7] = value
            0x28 -> config.mxcsr = value.ulong
            else -> log.warning { "Unknown register written index=0x${index.hex} value=0x${value.hex}" }
        }
    }

    override fun dbgLoad(address: ULong, size: Int) = with(ports.reader) {
        if (x86.is16bit) load(address, size, UNDEF) else load(address, size, x86.cpu.sregs.cs.id)
    }

    override fun dbgStore(address: ULong, data: ByteArray) = with(ports.reader) {
        if (x86.is16bit) store(address, data, UNDEF) else store(address, data, x86.cpu.sregs.cs.id)
    }
}