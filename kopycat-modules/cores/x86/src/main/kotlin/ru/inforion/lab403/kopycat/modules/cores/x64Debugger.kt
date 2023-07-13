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

class x64Debugger constructor(parent: Module, name: String, dbgAreaSize: ULong = BUS32):
    Debugger(parent, name, dbgAreaSize) {

    inline val x86 get() = core as x86Core

    override fun ident() = "i386:x86-64"

    override fun target() = "amd64-avx.xml"

    private val regCount = 0x27

    override fun registers() = List(regCount) { regRead(it) }

    override fun regSize(index: Int) = when (index) {
        in 0x00..0x10 -> QWORD
        in 0x18..0x1F -> FPU80
        in 0x28..0x37 -> XMMWORD
        else -> DWORD
    }

    override fun regRead(index: Int) = with(x86) {
        when (index) {
            0x00 -> cpu.regs.rax.value.bigint
            0x01 -> cpu.regs.rbx.value.bigint
            0x02 -> cpu.regs.rcx.value.bigint
            0x03 -> cpu.regs.rdx.value.bigint
            0x04 -> cpu.regs.rsi.value.bigint
            0x05 -> cpu.regs.rdi.value.bigint
            0x06 -> cpu.regs.rbp.value.bigint
            0x07 -> cpu.regs.rsp.value.bigint
            0x08 -> cpu.regs.r8.value.bigint
            0x09 -> cpu.regs.r9.value.bigint
            0x0A -> cpu.regs.r10.value.bigint
            0x0B -> cpu.regs.r11.value.bigint
            0x0C -> cpu.regs.r12.value.bigint
            0x0D -> cpu.regs.r13.value.bigint
            0x0E -> cpu.regs.r14.value.bigint
            0x0F -> cpu.regs.r15.value.bigint
            0x10 -> cpu.regs.rip.value.bigint
            0x11 -> cpu.flags.eflags.value.bigint
            0x12 -> cpu.sregs.cs.value.bigint
            0x13 -> cpu.sregs.ss.value.bigint
            0x14 -> cpu.sregs.ds.value.bigint
            0x15 -> cpu.sregs.es.value.bigint
            0x16 -> cpu.sregs.fs.value.bigint
            0x17 -> cpu.sregs.gs.value.bigint
            0x18 -> fpu.st(0)
            0x19 -> fpu.st(1)
            0x1A -> fpu.st(2)
            0x1B -> fpu.st(3)
            0x1C -> fpu.st(4)
            0x1D -> fpu.st(5)
            0x1E -> fpu.st(6)
            0x1F -> fpu.st(7)
            0x20 -> fpu.fwr.FPUControlWord.value.bigint
            0x21 -> fpu.fwr.FPUStatusWord.value.bigint
            0x22 -> fpu.fwr.FPUTagWord.value.bigint
            0x23 -> BigInteger.ZERO
            0x24 -> fpu.fwr.FPUInstructionPointer.value.bigint
            0x25 -> BigInteger.ZERO
            0x26 -> fpu.fwr.FPUDataPointer.value.bigint
            0x27 -> fpu.fwr.FPULastInstructionOpcode.value.bigint
            0x28 -> sse.xmm[0]
            0x29 -> sse.xmm[1]
            0x2A -> sse.xmm[2]
            0x2B -> sse.xmm[3]
            0x2C -> sse.xmm[4]
            0x2D -> sse.xmm[5]
            0x2E -> sse.xmm[6]
            0x2F -> sse.xmm[7]
            0x30 -> sse.xmm[8]
            0x31 -> sse.xmm[9]
            0x32 -> sse.xmm[10]
            0x33 -> sse.xmm[11]
            0x34 -> sse.xmm[12]
            0x35 -> sse.xmm[13]
            0x36 -> sse.xmm[14]
            0x37 -> sse.xmm[15]
            0x38 -> config.mxcsr.bigint
            else -> 0xDEADBEEFu.bigint
//        }.also {
//            log.info { "RD index=0x${index.hex} value=0x${it.hex}" }
        }
    }

    override fun regWrite(index: Int, value: BigInteger): Unit = with(x86) {
        log.info { "WR index=0x${index.hex} value=0x${value.hex}" }

        return when (index) {
            0x00 -> cpu.regs.rax.value = value.ulong
            0x01 -> cpu.regs.rbx.value = value.ulong
            0x02 -> cpu.regs.rcx.value = value.ulong
            0x03 -> cpu.regs.rdx.value = value.ulong
            0x04 -> cpu.regs.rsi.value = value.ulong
            0x05 -> cpu.regs.rdi.value = value.ulong
            0x06 -> cpu.regs.rbp.value = value.ulong
            0x07 -> cpu.regs.rsp.value = value.ulong
            0x08 -> cpu.regs.r8.value = value.ulong
            0x09 -> cpu.regs.r9.value = value.ulong
            0x0A -> cpu.regs.r10.value = value.ulong
            0x0B -> cpu.regs.r11.value = value.ulong
            0x0C -> cpu.regs.r12.value = value.ulong
            0x0D -> cpu.regs.r13.value = value.ulong
            0x0E -> cpu.regs.r14.value = value.ulong
            0x0F -> cpu.regs.r15.value = value.ulong
            0x10 -> {
                cpu.regs.rip.value = value.ulong
                cpu.resetFault()
            }
            0x11 -> cpu.flags.eflags.value = value.ulong
            0x12 -> cpu.sregs.cs.value = value.ulong
            0x13 -> cpu.sregs.ss.value = value.ulong
            0x14 -> cpu.sregs.ds.value = value.ulong
            0x15 -> cpu.sregs.es.value = value.ulong
            0x16 -> cpu.sregs.fs.value = value.ulong
            0x17 -> cpu.sregs.gs.value = value.ulong
            0x18 -> fpu.st(0, value)
            0x19 -> fpu.st(1, value)
            0x1A -> fpu.st(2, value)
            0x1B -> fpu.st(3, value)
            0x1C -> fpu.st(4, value)
            0x1D -> fpu.st(5, value)
            0x1E -> fpu.st(6, value)
            0x1F -> fpu.st(7, value)
            0x20 -> fpu.fwr.FPUControlWord.value = value.ulong
            0x21 -> fpu.fwr.FPUStatusWord.value = value.ulong
            0x22 -> fpu.fwr.FPUTagWord.value = value.ulong
            0x23 -> log.warning { "FPU instruction pointer segment [id=0x${index.hex}] not supported value=0x${value.hex} ignored" }
            0x24 -> fpu.fwr.FPUInstructionPointer.value = value.ulong
            0x25 -> log.warning { "FPU data pointer segment [id=0x${index.hex}] not supported value=0x${value.hex} ignored" }
            0x26 -> fpu.fwr.FPUDataPointer.value = value.ulong
            0x27 -> fpu.fwr.FPULastInstructionOpcode.value = value.ulong
            0x28 -> sse.xmm[0] = value
            0x29 -> sse.xmm[1] = value
            0x2A -> sse.xmm[2] = value
            0x2B -> sse.xmm[3] = value
            0x2C -> sse.xmm[4] = value
            0x2D -> sse.xmm[5] = value
            0x2E -> sse.xmm[6] = value
            0x2F -> sse.xmm[7] = value
            0x30 -> sse.xmm[8] = value
            0x31 -> sse.xmm[9] = value
            0x32 -> sse.xmm[10] = value
            0x33 -> sse.xmm[11] = value
            0x34 -> sse.xmm[12] = value
            0x35 -> sse.xmm[13] = value
            0x36 -> sse.xmm[14] = value
            0x37 -> sse.xmm[15] = value
            0x38 -> config.mxcsr = value.ulong
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
