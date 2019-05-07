package ru.inforion.lab403.kopycat.cores.x86.instructions

import org.junit.Test
import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.modules.memory.RAM

/**
 * Created by r.valitov on 23.08.17
 */
class X86InstructionsTest32: AX86InstructionTest() {
    override val x86 = x86Core(this, "x86 Core", 400.MHz, x86Core.Generation.Am5x86, 1.0)
    override val ram0 = RAM(this, "ram0", 0xFFF_FFFF)
    override val ram1 = RAM(this, "ram1", 0x1_0000)
    init {
        x86.ports.mem.connect(buses.mem)
        x86.ports.io.connect(buses.io)
        ram0.ports.mem.connect(buses.mem, 0)
        ram1.ports.mem.connect(buses.io, 0)
        x86.cpu.defaultSize = true
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
        store(startAddress + 0xF0FF, "CA110008FFAC")
        gprRegisters(eax = 0xF000)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertMMURegisters(gdtrBase = 0xACFF0800, gdtrLimit = 0x11CA)
    }

    // TEST LIDT INSTRUCTION

    @Test fun lidtTestm48() {
        val instruction = "lidt [EAX+0xFF]"
        val insnString = "lidt fword [EAX+0xFF]"
        store(startAddress + 0xF0FF, "CA110008FFAC")
        gprRegisters(eax = 0xF000)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertCopRegisters(idtrBase = 0xACFF0800, idtrLimit = 0x11CA, irq = -1)
    }

    // TEST SIDT INSTRUCTION

    @Test fun sidtTestm48() {
        val instruction = "sidt [0xF0FF]"
        val insnString = "sidt fword_0000f0ff"
        store(startAddress + 0xF0FF, "CA110008FFAC")
        copRegisters(idtrBase = 0xFF_CABA, idtrLimit = 0x1B7)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertMemory(startAddress + 0xF0FF, "B701BACAFF")
    }

    // TEST SGDT INSTRUCTION

    @Test fun sgdtTestm48() {
        val instruction = "sgdt [0xF0FF]"
        val insnString = "sgdt fword_0000f0ff"
        store(startAddress + 0xF0FF, "CA110008FFAC")
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertMemory(startAddress + 0xF0FF, "2000000000")
    }

    // TEST LEAVE INSTRUCTION

    @Test fun leaveTest() {
        val instructionPush = "push 0xCA16"
        gprRegisters(esp = 0xF000)
        execute(-5) { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(esp = 0xEFFC)

        gprRegisters(esp = 0xB2AC, ebp = 0xEFFC)
        val instructionLeave = "leave "
        execute { assemble(instructionLeave) }
        assertAssembly(instructionLeave)
        assertGPRRegisters(ebp = 0xCA16, esp = 0xF000)
    }

    // TEST POPF/PUSHF INSTRUCTION

    @Test fun pushfPopfTest() {
        val instructionPush = "pushf "
        eflag(0x4221)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertEflag(0x4221)
        eflag(0)

        val instructionPop = "popf "
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertEflag(0x4223)
    }

    // TEST IRET INSTRUCTION

    @Test fun iretTest1() {
        val instructionPushFlag = "push EAX"
        gprRegisters(eax = 0x18_2203, esp = 0x1_0000)
        execute(-2) { assemble(instructionPushFlag) }
        assertAssembly(instructionPushFlag)  // flags cf, pf, sf, of
        assertGPRRegisters(eax = 0x18_2203, esp = 0xFFFC)

        val instructionPushEip = "push EAX"
        gprRegisters(esp = 0xFFFC)
        execute(-1) { assemble(instructionPushEip) }
        assertAssembly(instructionPushEip)
        assertGPRRegisters(esp = 0xFFF8)

        val instructionPushCs = "push EAX"
        gprRegisters(eax = 0x2, esp = 0xFFF8)
        execute { assemble(instructionPushCs) }
        assertAssembly(instructionPushCs)
        assertGPRRegisters(eax = 0x2, esp = 0xFFF4)

        val instruction = "iret "
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters(cf = true, vip = true, vif = true, ifq = true)
        assertIopl(2)
    }

    @Test fun iretTest2() {
        val instructionPushFlag = "push EAX"
        gprRegisters(eax = 0x18_2203, esp = 0x1_0000)
        execute(-2) { assemble(instructionPushFlag) }
        assertAssembly(instructionPushFlag)  // flags cf, pf, sf, of
        assertGPRRegisters(eax = 0x18_2203, esp = 0xFFFC)

        val instructionPushEip = "push EAX"
        gprRegisters(esp = 0xFFFC, eax = 0x5)
        execute(-1) { assemble(instructionPushEip) }
        assertAssembly(instructionPushEip)
        assertGPRRegisters(esp = 0xFFF8, eax = 0x5)

        val instructionPushCs = "push EAX"
        gprRegisters(eax = 0x2, esp = 0xFFF8)
        execute { assemble(instructionPushCs) }
        assertAssembly(instructionPushCs)
        assertGPRRegisters(eax = 0x2, esp = 0xFFF4)

        val instruction = "iret "
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters(cf = true, vip = true, vif = true, ifq = true)
    }
}