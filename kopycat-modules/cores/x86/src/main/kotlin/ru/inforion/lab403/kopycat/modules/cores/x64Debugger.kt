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
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.common.Debugger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.SSRBank
import ru.inforion.lab403.kopycat.modules.BUS32
import java.math.BigInteger


class x64Debugger constructor(parent: Module, name: String, dbgAreaSize: ULong = BUS32):
    Debugger(parent, name, dbgAreaSize) {

    inline val x86 get() = core as x86Core

    override fun ident() = "i386:x86-64"

    override fun target() = "amd64-avx.xml"

    private val regCount = 0x27

    override fun registers() = List(regCount) { regRead(it) }

    override fun regSize(index: Int) = if (index in 0x00..0x10) QWORD else DWORD

    override fun regRead(index: Int) = with(x86) {
        when (index) {
            0x00 -> cpu.regs.rax.value
            0x01 -> cpu.regs.rbx.value
            0x02 -> cpu.regs.rcx.value
            0x03 -> cpu.regs.rdx.value
            0x04 -> cpu.regs.rsi.value
            0x05 -> cpu.regs.rdi.value
            0x06 -> cpu.regs.rbp.value
            0x07 -> cpu.regs.rsp.value
            0x08 -> cpu.regs.r8.value
            0x09 -> cpu.regs.r9.value
            0x0A -> cpu.regs.r10.value
            0x0B -> cpu.regs.r11.value
            0x0C -> cpu.regs.r12.value
            0x0D -> cpu.regs.r13.value
            0x0E -> cpu.regs.r14.value
            0x0F -> cpu.regs.r15.value
            0x10 -> cpu.regs.rip.value
            0x11 -> cpu.flags.eflags.value
            0x12 -> cpu.sregs.cs.value
            0x13 -> cpu.sregs.ss.value
            0x14 -> cpu.sregs.ds.value
            0x15 -> cpu.sregs.es.value
            0x16 -> cpu.sregs.fs.value
            0x17 -> cpu.sregs.gs.value
            0x18 -> fpu[0]
            0x19 -> fpu[1]
            0x1A -> fpu[2]
            0x1B -> fpu[3]
            0x1C -> fpu[4]
            0x1D -> fpu[5]
            0x1E -> fpu[6]
            0x1F -> fpu[7]
            0x20 -> fpu.fwr.FPUControlWord.value
            0x21 -> fpu.fwr.FPUStatusWord.value
            0x22 -> fpu.fwr.FPUTagWord.value
            0x23 -> 0u
            0x24 -> fpu.fwr.FPUInstructionPointer.value
            0x25 -> 0u
            0x26 -> fpu.fwr.FPUDataPointer.value
            0x27 -> fpu.fwr.FPULastInstructionOpcode.value
            else -> 0xDEADBEEFu
//        }.also {
//            log.info { "RD index=0x${index.hex} value=0x${it.hex}" }
        }
    }

    override fun regWrite(index: Int, value: ULong): Unit = with(x86) {
        log.info { "WR index=0x${index.hex} value=0x${value.hex}" }

        when (index) {
            0x00 -> cpu.regs.rax.value = value
            0x01 -> cpu.regs.rbx.value = value
            0x02 -> cpu.regs.rcx.value = value
            0x03 -> cpu.regs.rdx.value = value
            0x04 -> cpu.regs.rsi.value = value
            0x05 -> cpu.regs.rdi.value = value
            0x06 -> cpu.regs.rbp.value = value
            0x07 -> cpu.regs.rsp.value = value
            0x08 -> cpu.regs.r8.value = value
            0x09 -> cpu.regs.r9.value = value
            0x0A -> cpu.regs.r10.value = value
            0x0B -> cpu.regs.r11.value = value
            0x0C -> cpu.regs.r12.value = value
            0x0D -> cpu.regs.r13.value = value
            0x0E -> cpu.regs.r14.value = value
            0x0F -> cpu.regs.r15.value = value
            0x10 -> {
                cpu.regs.rip.value = value
                cpu.resetFault()
            }
            0x11 -> cpu.flags.eflags.value = value
            0x12 -> {
                cpu.invalidateDecoderCache()
                cpu.sregs.cs.value = value
            }
            0x13 -> cpu.sregs.ss.value = value
            0x14 -> cpu.sregs.ds.value = value
            0x15 -> cpu.sregs.es.value = value
            0x16 -> cpu.sregs.fs.value = value
            0x17 -> cpu.sregs.gs.value = value
            0x18 -> fpu[0] = value
            0x19 -> fpu[1] = value
            0x1A -> fpu[2] = value
            0x1B -> fpu[3] = value
            0x1C -> fpu[4] = value
            0x1D -> fpu[5] = value
            0x1E -> fpu[6] = value
            0x1F -> fpu[7] = value
            0x20 -> fpu.fwr.FPUControlWord.value = value
            0x21 -> fpu.fwr.FPUStatusWord.value = value
            0x22 -> fpu.fwr.FPUTagWord.value = value
            0x23 -> log.warning { "FPU instruction pointer segment [id=0x${index.hex}] not supported value=0x${value.hex} ignored" }
            0x24 -> fpu.fwr.FPUInstructionPointer.value
            0x25 -> log.warning { "FPU data pointer segment [id=0x${index.hex}] not supported value=0x${value.hex} ignored" }
            0x26 -> fpu.fwr.FPUDataPointer.value
            0x27 -> fpu.fwr.FPULastInstructionOpcode.value
            else -> log.warning { "Unknown register written index=0x${index.hex} value=0x${value.hex}" }
        }
    }

    override fun dbgLoad(address: ULong, size: Int) = with(ports.reader) {
        if (x86.is16bit) load(address, size, UNDEF) else load(address, size, x86.cpu.sregs.cs.id)
    }

    override fun dbgStore(address: ULong, data: ByteArray) = with(ports.reader) {
        x86.cpu.invalidateDecoderCache()
        if (x86.is16bit) store(address, data, UNDEF) else store(address, data, x86.cpu.sregs.cs.id)
    }
}