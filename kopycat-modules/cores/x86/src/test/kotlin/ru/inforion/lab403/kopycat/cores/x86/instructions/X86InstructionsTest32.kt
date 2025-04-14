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
package ru.inforion.lab403.kopycat.cores.x86.instructions

import org.junit.jupiter.api.Test
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.x86.config.Generation
import ru.inforion.lab403.kopycat.cores.x86.instructions.X86CommonTests.relativeJumpDecodeTestInner
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.modules.memory.RAM
import kotlin.test.assertEquals
import kotlin.test.assertNull


class X86InstructionsTest32: AX86InstructionTest() {
    override val x86 = x86Core(this, "x86 Core", 400.MHz, Generation.Am5x86, 1.0)
    override val ram0 = RAM(this, "ram0", 0xFFF_FFFF)
    override val ram1 = RAM(this, "ram1", 0x1_0000)
    init {
        x86.ports.mem.connect(buses.mem)
        x86.ports.io.connect(buses.io)
        ram0.ports.mem.connect(buses.mem, 0u)
        ram1.ports.mem.connect(buses.io, 0u)
        x86.cpu.csd = true
        initializeAndResetAsTopInstance()
    }
    override val mode: Long
        get() = 32

    override val bitMode: ByteArray
        get() = byteArrayOf(-1, -1, 0, 0, 1, -109, -49)

    // TEST LGDT INSTRUCTION

    @Test fun lgdtTestm48() {
        val instruction = "lgdt [EAX+0xFF]"
        val insnString = "lgdt fword [EAX+0xFF]"
        store(startAddress + 0xF0FFu, "CA110008FFAC")
        gprRegisters(eax = 0xF000u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertMMURegisters(gdtrBase = 0xACFF0800u, gdtrLimit = 0x11CAu)
    }

    // TEST LIDT INSTRUCTION

    @Test fun lidtTestm48() {
        val instruction = "lidt [EAX+0xFF]"
        val insnString = "lidt fword [EAX+0xFF]"
        store(startAddress + 0xF0FFu, "CA110008FFAC")
        gprRegisters(eax = 0xF000u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertCopRegisters(idtrBase = 0xACFF0800u, idtrLimit = 0x11CAu, irq = -1uL)
    }

    // TEST SIDT INSTRUCTION

    @Test fun sidtTestm48() {
        val instruction = "sidt [0xF0FF]"
        val insnString = "sidt fword [0xF0FF]"
        store(startAddress + 0xF0FFu, "CA110008FFAC")
        copRegisters(idtrBase = 0xFF_CABAu, idtrLimit = 0x1B7u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertMemory(startAddress + 0xF0FFu, "B701BACAFF")
    }

    // TEST SGDT INSTRUCTION

    @Test fun sgdtTestm48() {
        val instruction = "sgdt [0xF0FF]"
        val insnString = "sgdt fword [0xF0FF]"
        store(startAddress + 0xF0FFu, "CA110008FFAC")
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertMemory(startAddress + 0xF0FFu, "2000000000")
    }

    // TEST LEAVE INSTRUCTION

    @Test fun leaveTest() {
        val instructionPush = "push 0xCA16"
        gprRegisters(esp = 0xF000u)
        execute(-5uL) { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(esp = 0xEFFCu)

        gprRegisters(esp = 0xB2ACu, ebp = 0xEFFCu)
        val instructionLeave = "leave "
        execute { assemble(instructionLeave) }
        assertAssembly(instructionLeave)
        assertGPRRegisters(ebp = 0xCA16u, esp = 0xF000u)
    }

    // TEST POPF/PUSHF INSTRUCTION

    @Test fun pushfPopfTest() {
        val instructionPush = "pushf "
        eflag(0x4221u)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertEflag(0x4221u)
        eflag(0u)

        val instructionPop = "popf "
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertEflag(0x4223u)
    }

    // TEST IRET INSTRUCTION

    @Test fun iretTest1() {
        val instructionPushFlag = "push EAX"
        gprRegisters(eax = 0x18_2203u, esp = 0x1_0000u)
        execute(-2uL) { assemble(instructionPushFlag) }
        assertAssembly(instructionPushFlag)  // flags cf, pf, sf, of
        assertGPRRegisters(eax = 0x18_2203u, esp = 0xFFFCu)

        val instructionPushEip = "push EAX"
        gprRegisters(eax = x86.cpu.sregs.cs.value, esp = 0xFFFCu)
        execute(-1uL) { assemble(instructionPushEip) }
        assertAssembly(instructionPushEip)
        assertGPRRegisters(eax = x86.cpu.sregs.cs.value, esp = 0xFFF8u)

        val instructionPushCs = "push EAX"
        gprRegisters(eax = 0x2u, esp = 0xFFF8u)
        execute { assemble(instructionPushCs) }
        assertAssembly(instructionPushCs)
        assertGPRRegisters(eax = 0x2u, esp = 0xFFF4u)

        val instruction = "iret "
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertNull(x86.cpu.exception)
        assertFlagRegisters(cf = true, vip = true, vif = true, ifq = true)
        assertIopl(2)
    }

    @Test fun iretTest2() {
        x86.mmu.gdtr.base = 0x1_0000uL
        x86.mmu.gdtr.limit = 0x20u
        x86.mmu.ldtr = 8u
        ram0.write(0x1_0000u + x86.mmu.ldtr, 0, 8, 0x478a010100ffffuL) // LDT
        ram0.write(0x1_0100u, 0, 8, 0x478a000100ffffuL)

        val pushEax = assemble("push EAX")

        gprRegisters(eax = 5u, esp = 0x1_0000u)
        execute(-4uL) { pushEax } // SS
        assertAssembly("push EAX")
        assertGPRRegisters(eax = 5u, esp = 0xfffcu)

        gprRegisters(eax = 0x100u, esp = 0xfffcu)
        execute(-3uL) { pushEax } // SP
        assertGPRRegisters(eax = 0x100u, esp = 0xfff8u)

        gprRegisters(eax = 0x18_2203u, esp = 0xfff8u)
        execute(-2uL) { pushEax } // flags cf, pf, sf, of
        assertGPRRegisters(eax = 0x18_2203u, esp = 0xfff4u)

        gprRegisters(eax = 0x5u, esp = 0xfff4u)
        execute(-1uL) { pushEax } // CS
        assertGPRRegisters(eax = 0x5u, esp = 0xfff0u)

        gprRegisters(eax = 0x2u, esp = 0xfff0u)
        execute { pushEax } // IP
        assertGPRRegisters(eax = 0x2u, esp = 0xffecu)

        val instruction = "iret "
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertNull(x86.cpu.exception)
        assertEquals(2u, x86.cpu.regs.ip.value)
        assertEquals(0x100u, x86.cpu.regs.sp.value)
        assertEquals(0x05u, x86.cpu.sregs.cs.value)
        assertEquals(0x05u, x86.cpu.sregs.ss.value)
        assertEquals(0x150u, x86.mmu.translate(0x50uL, x86.cpu.sregs.cs.id, 1, AccessAction.LOAD))
        assertFlagRegisters(cf = true, vip = true, vif = true, ifq = true)
    }

    @Test fun relativeJumpDecodeTest() = relativeJumpDecodeTestInner()
}