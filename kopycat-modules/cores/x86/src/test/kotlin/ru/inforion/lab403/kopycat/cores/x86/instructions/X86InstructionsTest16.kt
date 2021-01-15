/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
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

import org.junit.Test
import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.modules.memory.RAM
import kotlin.test.assertTrue


class X86InstructionsTest16: AX86InstructionTest() {
    override val x86 = x86Core(this, "x86Core", 400.MHz, x86Core.Generation.Am5x86, 1.0)
    override val ram0 = RAM(this, "ram0", 0xFFF_FFFF)
    override val ram1 = RAM(this, "ram1", 0x1_0000)
    init {
        x86.ports.mem.connect(buses.mem)
        x86.ports.io.connect(buses.io)
        ram0.ports.mem.connect(buses.mem, 0)
        ram1.ports.mem.connect(buses.io, 0)
        initializeAndResetAsTopInstance()
    }

    override val mode = 16L

    override val bitMode: ByteArray
        get() = byteArrayOf(-1, -1, 0, 0, 1, -109, -113)

    // TEST ADD INSTRUCTION
    // ALL OPERANDS

    @Test fun addTestALi8() {
        val instruction = "add AL, 0x69"
        gprRegisters(eax = 0x25)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x8E)
    }

    @Test fun addTestAXi16() {
        val instruction = "add AX, 0x7ABA"
        gprRegisters(eax = 0x2222)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x9CDC)
    }

    @Test fun addTestEAXi32() {
        val instruction = "add EAX, 0x58AA7ABA"
        gprRegisters(eax = 0xCAFE_BABA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x23A9_3574)
    }

    @Test fun addTestr8i8() {
        val instruction = "add CL, 0x58"
        gprRegisters(ecx = 0xBA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x12)
    }

    @Test fun addTestr16i16() {
        val instruction = "add CX, 0xBCDE"
        gprRegisters(ecx = 0x4323)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x1)
    }

    @Test fun addTestr32i32() {
        val instruction = "add EDX, 0xABCDEF12"
        gprRegisters(edx = 0xDEAD_BABA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x8A7B_A9CC)
    }

    @Test fun addTestr16i8() {
        val instruction = "add CX, 0xDE"
        gprRegisters(ecx = 0x4322)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4400)
    }

    @Test fun addTestr32i8() {
        val instruction = "add EDX, 0x12"
        gprRegisters(edx = 0xDEAD_BABA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xDEAD_BACC)
    }

    @Test fun addTestm8i8() {
        val instruction = "add BYTE [EAX+0xFF], 0xBA"
        store(startAddress + 0xF0FF, 0xCA, Datatype.BYTE)
        gprRegisters(eax = 0xF000)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FF, 0x84, Datatype.BYTE)
        assertGPRRegisters(eax = 0xF000)
    }

    @Test fun addTestm16i16() {
        val instruction = "add WORD [EDX+0x5678], 0x4322"
        store(startAddress + 0xBE08, 0xBACA, Datatype.WORD)
        gprRegisters(edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xFDEC, Datatype.WORD)
        assertGPRRegisters(edx = 0x6790)
    }

    @Test fun addTestm32i32() {
        val instruction = "add DWORD [EDX+0x5678], 0x12344322"
        store(startAddress + 0xBE08, 0xFAAA_BACA, Datatype.DWORD)
        gprRegisters(edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xCDE_FDEC, Datatype.DWORD)
        assertGPRRegisters(edx = 0x6790)
    }

    @Test fun addTestm16i8() {
        val instruction = "add WORD [EDX+0x5678], 0x22"
        store(startAddress + 0xBE08, 0xBACA, Datatype.WORD)
        gprRegisters(edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xBAEC, Datatype.WORD)
        assertGPRRegisters(edx = 0x6790)
    }

    @Test fun addTestm32i8() {
        val instruction = "add DWORD [EDX+0x5678], 0x22"
        store(startAddress + 0xBE08, 0xFAAA_BACA, Datatype.DWORD)
        gprRegisters(edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xFAAA_BAEC, Datatype.DWORD)
        assertGPRRegisters(edx = 0x6790)
    }

    @Test fun addTestr8r8() {
        val instruction = "add CL, DH"
        gprRegisters(ecx = 0xBA, edx = 0xFA00)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xB4, edx = 0xFA00)
    }

    @Test fun addTestr16r16() {
        val instruction = "add CX, DX"
        gprRegisters(ecx = 0x4322, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xAAB2, edx = 0x6790)
    }

    @Test fun addTestr32r32() {
        val instruction = "add EDX, EBX"
        gprRegisters(edx = 0xDEAD_BABA, ebx = 0xABCD_EF12)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x8A7B_A9CC, ebx = 0xABCD_EF12)
    }

    @Test fun addTestm8r8() {
        val instruction = "add BYTE [EAX+0xFF], BL"
        store(startAddress + 0xF0FF, 0xCA, Datatype.BYTE)
        gprRegisters(ebx = 0xBA, eax = 0xF000)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FF, 0x84, Datatype.BYTE)
        assertGPRRegisters(eax = 0xF000, ebx = 0xBA)
    }

    @Test fun addTestm16r16() {
        val instruction = "add WORD [EDX+0x5678], CX"
        store(startAddress + 0xBE08, 0xBACA, Datatype.WORD)
        gprRegisters(ecx = 0x4322, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xFDEC, Datatype.WORD)
        assertGPRRegisters(ecx = 0x4322, edx = 0x6790)
    }

    @Test fun addTestm32r32() {
        val instruction = "add DWORD [EDX+0x5678], ECX"
        store(startAddress + 0xBE08, 0xFAAA_BACA, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xCDE_FDEC, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x1234_4322, edx = 0x6790)
    }

    @Test fun addTestr8m8() {
        val instruction = "add BL, BYTE [EAX+0xFF]"
        store(startAddress + 0xF0FF, 0xCA, Datatype.BYTE)
        gprRegisters(ebx = 0xBA, eax = 0xF000)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FF, 0xCA, Datatype.BYTE)
        assertGPRRegisters(eax = 0xF000, ebx = 0x84)
    }

    @Test fun addTestr16m16() {
        val instruction = "add CX, WORD [EDX+0x5678]"
        store(startAddress + 0xBE08, 0xBACA, Datatype.WORD)
        gprRegisters(ecx = 0x4322, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xBACA, Datatype.WORD)
        assertGPRRegisters(ecx = 0xFDEC, edx = 0x6790)
    }

    @Test fun addTestr32m32() {
        val instruction = "add ECX, DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08, 0xFAAA_BACA, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xFAAA_BACA, Datatype.DWORD)
        assertGPRRegisters(ecx = 0xCDE_FDEC, edx = 0x6790)
    }

    @Test fun addTestFlags1() {
        val instruction = "add EDX, EBX"
        gprRegisters(edx = 0xDEAD_BABA, ebx = 0xABCD_EF12)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x8A7B_A9CC, ebx = 0xABCD_EF12)

        assertFlagRegisters(pf = true, sf = true, cf = true)
    }

    @Test fun addTestFlags2() {
        val instruction = "add EBX, DWORD [EAX+0xFF]"
        store(startAddress + 0xF0FF, 0x57AE_129D, Datatype.DWORD)
        gprRegisters(ebx = 0xA851_ED63, eax = 0xF000)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FF, 0x57AE_129D, Datatype.DWORD)
        assertGPRRegisters(eax = 0xF000, ebx = 0)

        assertFlagRegisters(zf = true, pf = true, af = true, cf = true)
    }

    // TEST ADC INSTRUCTION
    // FOR ARITHMETICAL INSTRUCTIONS SAME DECODER

    @Test fun adcTestr16m16() {
        val instruction = "adc ECX, DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08, 0xBACA_BACA, Datatype.DWORD)
        gprRegisters(ecx = 0x8423_7322, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xBACA_BACA, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x3EEE_2DEC, edx = 0x6790)
        assertFlagRegisters(cf = true, of = true)
    }

    // TEST SBB INSTRUCTION
    // FOR ARITHMETICAL INSTRUCTIONS SAME DECODER

    @Test fun sbbTestr32r32() {
        val instruction = "sbb BL, BYTE [EAX+0xFF]"
        store(startAddress + 0xF0FF, 0xCA, Datatype.BYTE)
        gprRegisters(ebx = 0xBA, eax = 0xF000)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FF, 0xCA, Datatype.BYTE)
        assertGPRRegisters(eax = 0xF000, ebx = 0xF0)
    }

    @Test fun sbbTestFlag() {
        val instruction = "sbb EAX, EDX"
        flagRegisters(cf = true)
        gprRegisters(edx = 0xFA_56BA, eax = 0xD458_963B)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xD35E_3F80, edx = 0xFA_56BA)
        assertFlagRegisters(sf = true)
    }

    // TEST SUB INSTRUCTION
    // FOR ARITHMETICAL INSTRUCTIONS SAME DECODER

    @Test fun subTestr32i8() {
        val instruction = "sub EDX, 0x12"
        gprRegisters(edx = 0xDEAD_BABA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xDEAD_BAA8)
        assertFlagRegisters(sf = true)
    }

    // TEST AND INSTRUCTION
    // FOR ARITHMETICAL INSTRUCTIONS SAME DECODER

    @Test fun andTestAXi16() {
        val instruction = "and AX, 0xA5A5"
        gprRegisters(eax = 0x1723)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x521)
        assertFlagRegisters(pf = true)
    }

    // TEST OR INSTRUCTION
    // FOR ARITHMETICAL INSTRUCTIONS SAME DECODER

    @Test fun orTestm16r16() {
        val instruction = "or WORD [EDX+0x5678], CX"
        store(startAddress + 0xBE08, 0xBACA, Datatype.WORD)
        gprRegisters(ecx = 0x4322, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xFBEA, Datatype.WORD)
        assertGPRRegisters(ecx = 0x4322, edx = 0x6790)
        assertFlagRegisters(sf = true)
    }

    // TEST XOR INSTRUCTION
    // FOR ARITHMETICAL INSTRUCTIONS SAME DECODER

    @Test fun xorTestr32m32() {
        val instruction = "xor ECX, DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08, 0xFAAA_BACA, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xFAAA_BACA, Datatype.DWORD)
        assertGPRRegisters(ecx = 0xE89E_F9E8, edx = 0x6790)
        assertFlagRegisters(sf = true, pf = true)
    }

    // TEST CMP INSTRUCTION
    // FOR ARITHMETICAL INSTRUCTIONS SAME DECODER

    @Test fun cmpTestr8imm8() {
        val instruction = "cmp BH, 0xA"
        gprRegisters(ebx = 0xA00)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0xA00)
        assertFlagRegisters(zf = true, pf = true)
    }

    @Test fun cmpTestFlags1() {
        val instruction = "cmp ECX, 0x100"
        gprRegisters(ecx = 0xA0)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xA0)
        assertFlagRegisters(sf = true, pf = true, cf = true)
    }

    @Test fun cmpTestFlags2() {
        val instruction = "cmp ECX, 0x100"
        gprRegisters(ecx = 0x8000_0000)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x8000_0000)
        assertFlagRegisters(of = true, pf = true)
    }

    // TEST AAA INSTRUCTION

    @Test fun aaaTest1() {
        val instruction = "aaa "
        gprRegisters(eax = 0x111A)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x1200)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun aaaTest2() {
        val instruction = "aaa "
        flagRegisters(af = true)
        gprRegisters(eax = 0x5514)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x560A)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun aaaTest3() {
        val instruction = "aaa "
        flagRegisters(af = true)
        gprRegisters(eax = 0x551A)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x5600)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun aaaTest4() {
        val instruction = "aaa "
        gprRegisters(eax = 0x5514)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x5504)
        assertFlagRegisters(af = false, cf = false)
    }

    // TEST AAS INSTRUCTION

    @Test fun aasTest1() {
        val instruction = "aas "
        gprRegisters(eax = 0x111A)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x1004)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun aasTest2() {
        val instruction = "aas "
        flagRegisters(af = true)
        gprRegisters(eax = 0x5514)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x540E)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun aasTest3() {
        val instruction = "aas "
        flagRegisters(af = true)
        gprRegisters(eax = 0x551A)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x5404)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun aasTest4() {
        val instruction = "aas "
        gprRegisters(eax = 0x5514)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x5504)
        assertFlagRegisters(af = false, cf = false)
    }

    // TEST DAA INSTRUCTION

    @Test fun daaTest1() {
        val instruction = "daa "
        gprRegisters(eax = 0x111A)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x1120)
        assertFlagRegisters(af = true)
    }

    @Test fun daaTest2() {
        val instruction = "daa "
        flagRegisters(af = true)
        gprRegisters(eax = 0xFB)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x61)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun daaTest3() {
        val instruction = "daa "
        flagRegisters(af = true, cf = true)
        gprRegisters(eax = 0x5511)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x5577)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun daaTest4() {
        val instruction = "daa "
        gprRegisters(eax = 0x55A4)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x5504)
        assertFlagRegisters(af = false, cf = true)
    }

    // TEST DAS INSTRUCTION

    @Test fun dasTest1() {
        val instruction = "das "
        gprRegisters(eax = 0x111A)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x1114)
        assertFlagRegisters(af = true)
    }

    @Test fun dasTest2() {
        val instruction = "das "
        flagRegisters(af = true)
        gprRegisters(eax = 0xFB)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x95)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun dasTest3() {
        val instruction = "das "
        flagRegisters(af = true, cf = true)
        gprRegisters(eax = 0x5511)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x55AB)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun dasTest4() {
        val instruction = "das "
        gprRegisters(eax = 0x55A4)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x5544)
        assertFlagRegisters(af = false, cf = true)
    }

    // TEST DEC INSTRUCTION

    @Test fun decTestm8() {
        val instruction = "dec DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08, 0xCA, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xC9, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x1234_4322, edx = 0x6790)
    }

    @Test fun decTestm16() {
        val instruction = "dec DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08, 0xCAC1, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xCAC0, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x1234_4322, edx = 0x6790)
    }

    @Test fun decTestm32() {
        val instruction = "dec DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08, 0xFACC_CAC1, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xFACC_CAC0, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x1234_4322, edx = 0x6790)
    }

    @Test fun decTestr16() {
        val instruction = "dec CX"
        gprRegisters(ecx = 0x4322)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4321)
    }

    @Test fun decTestr32() {
        val instruction = "dec ECX"
        gprRegisters(ecx = 0x8000_0000)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x7FFF_FFFF)
        assertFlagRegisters(of = true, af = true, pf = true)
    }

    // TEST DIV INSTRUCTION

    @Test fun divTestr8() {
        val instruction = "div CL"
        gprRegisters(ecx = 0xB, eax = 0x76)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xB, eax = 0x080A)
    }

    @Test fun divTestr16() {
        val instruction = "div CX"
        gprRegisters(eax = 0x253B, ecx = 0x8E)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x8E, edx = 0x0011, eax = 0x0043)
    }

    @Test fun divTestr32() {
        val instruction = "div ECX"
        gprRegisters(edx = 0x33_5D25, eax = 0x9380_A2F4, ecx = 0x9E_1247)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x9E_1247, edx = 0x82_1DC7, eax = 0x532F_52EB)
    }

    @Test fun divTestm8() {
        val instruction = "div BYTE [EDX+0x325F]"
        store(startAddress + 0x8452, 0x0B, Datatype.BYTE)
        gprRegisters(edx = 0x51F3, eax = 0x76)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x51F3, eax = 0x080A)
    }

    @Test fun divTestm16() {
        val instruction = "div WORD [EBX+0x325F]"
        store(startAddress + 0x8452, 0x008E, Datatype.WORD)
        gprRegisters(eax = 0x253B, ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x0011, eax = 0x0043, ebx = 0x51F3)
    }

    @Test fun divTestm32() {
        val instruction = "div DWORD [EBX+0x325F]"
        store(startAddress + 0x8452, 0x9E_1247, Datatype.DWORD)
        gprRegisters(edx = 0x33_5D25, eax = 0x9380_A2F4, ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x82_1DC7, eax = 0x532F_52EB, ebx = 0x51F3)
    }

    @Test fun divTestr8Zero() {
        val instruction = "div CL"
        gprRegisters(ecx = 0, eax = 0x5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertTrue { x86.cpu.exception is x86HardwareException.DivisionByZero }
    }

    @Test fun divTestr8Overflow() {
        val instruction = "div CL"
        gprRegisters(ecx = 0x10, eax = 0x1000)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertTrue { x86.cpu.exception is x86HardwareException.Overflow }
    }

    @Test fun divTestr16Overflow() {
        val instruction = "div CX"
        gprRegisters(ecx = 0x10, eax = 0, edx = 0x10)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertTrue { x86.cpu.exception is x86HardwareException.Overflow }
    }

    @Test fun divTestr32Overflow() {
        val instruction = "div ECX"
        gprRegisters(ecx = 0x10, eax = 0, edx = 0x10)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertTrue { x86.cpu.exception is x86HardwareException.Overflow }
    }

    // TEST IDIV INSTRUCTION

    @Test fun idivTestr8() {
        val instruction = "idiv CL"
        gprRegisters(ecx = 0xB, eax = 0xFF86)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xB, eax = 0xFFF5)
    }

    @Test fun idivTestr16() {
        val instruction = "idiv CX"
        gprRegisters(eax = 0x253B, ecx = 0x8E)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x8E, edx = 0x0011, eax = 0x0043)
    }

    @Test fun idivTestr32() {
        val instruction = "idiv ECX"
        gprRegisters(edx = 0xFFCC_A2DA, eax = 0x6D01_7AD3, ecx = 0x9E_1247)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x9E_1247, edx = 0, eax = 0xACD0_AD15)
    }

    @Test fun idivTestm8() {
        val instruction = "idiv BYTE [EDX+0x325F]"
        store(startAddress + 0x8452, 0x0B, Datatype.BYTE)
        gprRegisters(edx = 0x51F3, eax = 0xFF86)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x51F3, eax = 0xFFF5)
    }

    @Test fun idivTestm16() {
        val instruction = "idiv WORD [EBX+0x325F]"
        store(startAddress + 0x8452, 0x008E, Datatype.WORD)
        gprRegisters(eax = 0x253B, ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x0011, eax = 0x0043, ebx = 0x51F3)
    }

    @Test fun idivTestm32() {
        val instruction = "idiv DWORD [EBX+0x325F]"
        store(startAddress + 0x8452, 0x9E_1247, Datatype.DWORD)
        gprRegisters(edx = 0xFFCC_A2DA, eax = 0x6D01_7AD3, ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0, eax = 0xACD0_AD15, ebx = 0x51F3)
    }

    @Test fun idivTestr8Zero() {
        val instruction = "idiv CL"
        gprRegisters(ecx = 0, eax = 0x5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertTrue { x86.cpu.exception is x86HardwareException.DivisionByZero }
    }

    // TEST IMUL INSTRUCTION

    @Test fun imulTestr8() {
        val instruction = "imul BL"
        gprRegisters(eax = 0x16, ebx = 0x3A)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0x3A, eax = 0x04FC)
    }

    @Test fun imulTestr16() {
        val instruction = "imul BX"
        gprRegisters(eax = 0x253B, ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x0BEB, eax = 0x0201, ebx = 0x51F3)
    }

    @Test fun imulTestr32() {
        val instruction = "imul EBX"
        gprRegisters(ebx = 0x4FCC_A2DA, eax = 0x6D01_7AD3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x21FA_976C, eax = 0xC020_1DAE, ebx = 0x4FCC_A2DA)
    }

    @Test fun imulTestm8() {
        val instruction = "imul BYTE [EDX+0x325F]"
        store(startAddress + 0x8452, 0x3A, Datatype.BYTE)
        gprRegisters(edx = 0x51F3, eax = 0x16)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x04FC, edx = 0x51F3)
    }

    @Test fun imulTestm16() {
        val instruction = "imul WORD [EDX+0x325F]"
        store(startAddress + 0x8452, 0x51F3, Datatype.WORD)
        gprRegisters(eax = 0x253B, edx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x0BEB, eax = 0x0201)
    }

    @Test fun imulTestm32() {
        val instruction = "imul DWORD [EDX+0x325F]"
        store(startAddress + 0x8452, 0x4FCC_A2DA, Datatype.DWORD)
        gprRegisters(eax = 0x6D01_7AD3, edx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x21FA_976C, eax = 0xC020_1DAE)
    }

    @Test fun imulTestr16m16() {
        val instruction = "imul AX, WORD [EDX+0x325F]"
        store(startAddress + 0x8452, 0x51F3, Datatype.WORD)
        gprRegisters(eax = 0x253B, edx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x51F3, eax = 0x0201)
    }

    @Test fun imulTestr32m32() {
        val instruction = "imul EAX, DWORD [EDX+0x325F]"
        store(startAddress + 0x8452, 0x4FCC_A2DA, Datatype.DWORD)
        gprRegisters(eax = 0x6D01_7AD3, edx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x51F3, eax = 0xC020_1DAE)
    }

    @Test fun imulTestr32r32imm32() {
        val instruction = "imul EAX, ECX, 0x62FA1C"
        gprRegisters(ecx = 0x6D01_7AD3, eax = 0x1)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x6D01_7AD3, eax = 0x62E1_7D14)
    }

    // TEST INC INSTRUCTION

    @Test fun incTestm8() {
        val instruction = "inc DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08, 0xCA, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xCB, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x1234_4322, edx = 0x6790)
    }

    @Test fun incTestm16() {
        val instruction = "inc DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08, 0xCAC1, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xCAC2, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x1234_4322, edx = 0x6790)
    }

    @Test fun incTestm32() {
        val instruction = "inc DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08, 0xFACC_CAC1, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xFACC_CAC2, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x1234_4322, edx = 0x6790)
    }

    @Test fun incTestr16() {
        val instruction = "inc CX"
        gprRegisters(ecx = 0x4322)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4323)
    }

    @Test fun incTestr32() {
        val instruction = "inc ECX"
        gprRegisters(ecx = 0x4322_FA11)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4322_FA12)
    }

    @Test fun incTestFlag1() {
        val instruction = "inc ECX"
        gprRegisters(ecx = 0xFFFF_FFFF)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0)
        assertFlagRegisters(zf = true, af = true, pf = true)
    }

    @Test fun incTestFlag2() {
        val instruction = "inc ECX"
        gprRegisters(ecx = 0x7FFF_FFFF)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x8000_0000)
        assertFlagRegisters(of = true, af = true, pf = true, sf = true)
    }

    // TEST MUL INSTRUCTION

    @Test fun mulTestr8() {
        val instruction = "mul CL"
        gprRegisters(ecx = 0xB, eax = 0x76)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xB, eax = 0x0512)
    }

    @Test fun mulTestr16() {
        val instruction = "mul CX"
        gprRegisters(eax = 0x253B, ecx = 0x8E)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x8E, edx = 0x0014, eax = 0xA6BA)
    }

    @Test fun mulTestr32() {
        val instruction = "mul ECX"
        gprRegisters(edx = 0x33_5D25, eax = 0x9380_A2F4, ecx = 0x9E_1247)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x9E_1247, edx = 0x5B_13EC, eax = 0x86BA_59AC)
    }

    @Test fun mulTestm8() {
        val instruction = "mul BYTE [EDX+0x325F]"
        store(startAddress + 0x8452, 0x0B, Datatype.BYTE)
        gprRegisters(edx = 0x51F3, eax = 0x76)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x51F3, eax = 0x0512)
        assertFlagRegisters(of = true, cf = true)
    }

    @Test fun mulTestFlags() {
        val instruction = "mul BYTE [EDX+0x325F]"
        store(startAddress + 0x8452, 0xB, Datatype.BYTE)
        gprRegisters(edx = 0x51F3, eax = 0x6)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x51F3, eax = 0x42)
        assertFlagRegisters()
    }

    @Test fun mulTestm16() {
        val instruction = "mul WORD [EBX+0x325F]"
        store(startAddress + 0x8452, 0x008E, Datatype.WORD)
        gprRegisters(eax = 0x253B, ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x0014, eax = 0xA6BA, ebx = 0x51F3)
    }

    @Test fun mulTestm32() {
        val instruction = "mul DWORD [EBX+0x325F]"
        store(startAddress + 0x8452, 0x9E_1247, Datatype.DWORD)
        gprRegisters(edx = 0x33_5D25, eax = 0x9380_A2F4, ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x5B_13EC, eax = 0x86BA_59AC, ebx = 0x51F3)
        assertFlagRegisters(of = true, cf = true)
    }

    // TEST NEG INSTRUCTION

    @Test fun negTestr8() {
        val instruction = "neg CL"
        gprRegisters(ecx = 0xB)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xF5)
        assertFlagRegisters(cf = true)
    }

    @Test fun negTestr16() {
        val instruction = "neg CX"
        gprRegisters(ecx = 0x8E57)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x71A9)
        assertFlagRegisters(cf = true)
    }

    @Test fun negTestr32() {
        val instruction = "neg ECX"
        gprRegisters(ecx = 0x9380_A2F4)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x6C7F_5D0C)
        assertFlagRegisters(cf = true)
    }

    @Test fun negTestm8() {
        val instruction = "neg BYTE [EDX+0x325F]"
        store(startAddress + 0x8452, 0x0B, Datatype.BYTE)
        gprRegisters(edx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452, 0xF5, Datatype.BYTE)
        assertFlagRegisters(cf = true)
    }

    @Test fun negTestm16() {
        val instruction = "neg WORD [EBX+0x325F]"
        store(startAddress + 0x8452, 0x008E, Datatype.WORD)
        gprRegisters(ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452, 0xFF72, Datatype.WORD)
        assertFlagRegisters(cf = true)
    }

    @Test fun negTestm32() {
        val instruction = "neg DWORD [EBX+0x325F]"
        store(startAddress + 0x8452, 0x9E_1247, Datatype.DWORD)
        gprRegisters(ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452, 0xFF61_EDB9, Datatype.DWORD)
        assertFlagRegisters(cf = true)
    }

    // TEST NOT INSTRUCTION

    @Test fun notTestr8() {
        val instruction = "not CL"
        gprRegisters(ecx = 0xB)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xF4)
    }

    @Test fun notTestr16() {
        val instruction = "not CX"
        gprRegisters(ecx = 0x8E57)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x71A8)
    }

    @Test fun notTestr32() {
        val instruction = "not ECX"
        gprRegisters(ecx = 0x9380_A2F4)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x6C7F_5D0B)
    }

    @Test fun notTestm8() {
        val instruction = "not BYTE [EDX+0x325F]"
        store(startAddress + 0x8452, 0x0B, Datatype.BYTE)
        gprRegisters(edx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452, 0xF4, Datatype.BYTE)
    }

    @Test fun notTestm16() {
        val instruction = "not WORD [EBX+0x325F]"
        store(startAddress + 0x8452, 0x008E, Datatype.WORD)
        gprRegisters(ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452, 0xFF71, Datatype.WORD)
    }

    @Test fun notTestm32() {
        val instruction = "not DWORD [EBX+0x325F]"
        store(startAddress + 0x8452, 0x9E_1247, Datatype.DWORD)
        gprRegisters(ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452, 0xFF61_EDB8, Datatype.DWORD)
    }

    // TEST BSF INSTRUCTION

    @Test fun bsfTestr16r16() {
        val instruction = "bsf CX, BX"
        gprRegisters(ecx = 0x8E57, ebx = 0x800)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0x800, ecx = 0xB)
        assertFlagRegisters()
    }

    @Test fun bsfTestr32r32() {
        val instruction = "bsf ECX, EBX"
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters(zf = true)
    }

    @Test fun bsfTestr16m16() {
        val instruction = "bsf CX, WORD [EBX+0x325F]"
        store(startAddress + 0x8452, 0x8000, Datatype.WORD)
        gprRegisters(ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452, 0x8000, Datatype.WORD)
        assertGPRRegisters(ebx = 0x51F3, ecx = 0xF)
    }

    @Test fun bsfTestr32m32() {
        val instruction = "bsf ECX, DWORD [EBX+0x325F]"
        store(startAddress + 0x8452, 0x40_1240, Datatype.DWORD)
        gprRegisters(ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452, 0x40_1240, Datatype.DWORD)
        assertGPRRegisters(ebx = 0x51F3, ecx = 0x6)
    }

    // TEST BSR INSTRUCTION

    @Test fun bsrTestr16r16() {
        val instruction = "bsr CX, BX"
        gprRegisters(ecx = 0x8E57, ebx = 0x800)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0x800, ecx = 0xB)
        assertFlagRegisters()
    }

    @Test fun bsrTestr32r32() {
        val instruction = "bsr ECX, EBX"
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters(zf = true)
    }

    @Test fun bsrTestr16m16() {
        val instruction = "bsr CX, WORD [EBX+0x325F]"
        store(startAddress + 0x8452, 0x8000, Datatype.WORD)
        gprRegisters(ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452, 0x8000, Datatype.WORD)
        assertGPRRegisters(ebx = 0x51F3, ecx = 0xF)
    }

    @Test fun bsrTestr32m32() {
        val instruction = "bsr ECX, DWORD [EBX+0x325F]"
        store(startAddress + 0x8452, 0x40_1240, Datatype.DWORD)
        gprRegisters(ebx = 0x51F3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452, 0x40_1240, Datatype.DWORD)
        assertGPRRegisters(ebx = 0x51F3, ecx = 0x16)
    }

    // TEST BT INSTRUCTION

    @Test fun btTestr16r16() {
        val instruction = "bt CX, BX"
        gprRegisters(ecx = 0x8E57, ebx = 0xF)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0xF, ecx = 0x8E57)
        assertFlagRegisters(cf = true)
    }

    @Test fun btTestr32r32() {
        val instruction = "bt ECX, EBX"
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters()
    }

    @Test fun btTestr16i8() {
        val instruction = "bt CX, 0xC"
        gprRegisters(ecx = 0x124F)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x124F)
        assertFlagRegisters(cf = true)
    }

    @Test fun btTestr32i8() {
        val instruction = "bt EBX, 0x18"
        gprRegisters(ebx = 0x2008_DAB2)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0x2008_DAB2)
        assertFlagRegisters()
    }

    // TEST BTR INSTRUCTION

    @Test fun btrTestr16r16() {
        val instruction = "btr CX, BX"
        gprRegisters(ecx = 0x8E57, ebx = 0xF)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0xF, ecx = 0xE57)
        assertFlagRegisters(cf = true)
    }

    @Test fun btrTestr32r32() {
        val instruction = "btr ECX, EBX"
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters()
    }

    @Test fun btrTestr16i8() {
        val instruction = "btr CX, 0xC"
        gprRegisters(ecx = 0x124F)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x24F)
        assertFlagRegisters(cf = true)
    }

    @Test fun btrTestr32i8() {
        val instruction = "btr EBX, 0x18"
        gprRegisters(ebx = 0x2008_DAB2)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0x2008_DAB2)
        assertFlagRegisters()
    }

    // TEST BTS INSTRUCTION

    @Test fun btsTestr16r16() {
        val instruction = "bts CX, BX"
        gprRegisters(ecx = 0x8E57, ebx = 0xF)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0xF, ecx = 0x8E57)
        assertFlagRegisters(cf = true)
    }

    @Test fun btsTestr32r32() {
        val instruction = "bts ECX, EBX"
        gprRegisters(ecx = 0x2)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x3)
        assertFlagRegisters()
    }

    @Test fun btsTestr16i8() {
        val instruction = "bts CX, 0xC"
        gprRegisters(ecx = 0x124F)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x124F)
        assertFlagRegisters(cf = true)
    }

    @Test fun btsTestr32i8() {
        val instruction = "bts EBX, 0x18"
        gprRegisters(ebx = 0x2008_DAB2)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0x2108_DAB2)
        assertFlagRegisters()
    }

    // TEST TEST INSTRUCTION

    @Test fun testTestALi8() {
        val instruction = "test AL, 0x69"
        gprRegisters(eax = 0x25)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x25)
        assertFlagRegisters(sf = false, zf = false, pf = true, cf = false, of = false)
    }

    @Test fun testTestAXi16() {
        val instruction = "test AX, 0x7ABA"
        gprRegisters(eax = 0x2222)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x2222)
        assertFlagRegisters(sf = false, zf = false, pf = true, cf = false, of = false)
    }

    @Test fun testTestEAXi32() {
        val instruction = "test EAX, 0xD8AA7ABA"
        gprRegisters(eax = 0xCAFE_BABA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xCAFE_BABA)
        assertFlagRegisters(sf = true, zf = false, pf = false, cf = false, of = false)
    }

    @Test fun testTestr8i8() {
        val instruction = "test CL, 0xA7"
        gprRegisters(ecx = 0x58)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x58)
        assertFlagRegisters(sf = false, zf = true, pf = true, cf = false, of = false)
    }

    @Test fun testTestr16i16() {
        val instruction = "test CX, 0xBCDE"
        gprRegisters(ecx = 0x4323)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4323)
        assertFlagRegisters(sf = false, zf = false, pf = false, cf = false, of = false)
    }

    @Test fun testTestr32i32() {
        val instruction = "test EDX, 0xABCDEF12"
        gprRegisters(edx = 0xDEAD_BABA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xDEAD_BABA)
        assertFlagRegisters(sf = true, zf = false, pf = true, cf = false, of = false)
    }

    @Test fun testTestm8i8() {
        val instruction = "test BYTE [EAX+0xFF], 0xA7"
        store(startAddress + 0xF0FF, 0x58, Datatype.BYTE)
        gprRegisters(eax = 0xF000)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FF, 0x58, Datatype.BYTE)
        assertFlagRegisters(sf = false, zf = true, pf = true, cf = false, of = false)
    }

    @Test fun testTestm16i16() {
        val instruction = "test WORD [EDX+0x5678], 0xBCDE"
        store(startAddress + 0xBE08, 0x4323, Datatype.WORD)
        gprRegisters(edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0x4323, Datatype.WORD)
        assertFlagRegisters(sf = false, zf = false, pf = false, cf = false, of = false)
    }

    @Test fun testTestm32i32() {
        val instruction = "test DWORD [EDX+0x5678], 0xABCDEF12"
        store(startAddress + 0xBE08, 0xDEAD_BABA, Datatype.DWORD)
        gprRegisters(edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xDEAD_BABA, Datatype.DWORD)
        assertGPRRegisters(edx = 0x6790)
        assertFlagRegisters(sf = true, zf = false, pf = true, cf = false, of = false)
    }

    @Test fun testTestr8r8() {
        val instruction = "test CL, DH"
        gprRegisters(ecx = 0x58, edx = 0xA700)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x58, edx = 0xA700)
        assertFlagRegisters(sf = false, zf = true, pf = true, cf = false, of = false)
    }

    @Test fun testTestr16r16() {
        val instruction = "test CX, DX"
        gprRegisters(ecx = 0x4323, edx = 0xBCDE)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4323, edx = 0xBCDE)
        assertFlagRegisters(sf = false, zf = false, pf = false, cf = false, of = false)
    }

    @Test fun testTestr32r32() {
        val instruction = "test EDX, EBX"
        gprRegisters(edx = 0xDEAD_BABA, ebx = 0xABCD_EF12)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xDEAD_BABA, ebx = 0xABCD_EF12)
        assertFlagRegisters(sf = true, zf = false, pf = true, cf = false, of = false)
    }

    @Test fun testTestm8r8() {
        val instruction = "test BYTE [EAX+0xFF], DH"
        store(startAddress + 0xF0FF, 0x58, Datatype.BYTE)
        gprRegisters(edx = 0xA700, eax = 0xF000)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FF, 0x58, Datatype.BYTE)
        assertGPRRegisters(edx = 0xA700, eax = 0xF000)
        assertFlagRegisters(sf = false, zf = true, pf = true, cf = false, of = false)
    }

    @Test fun testTestm16r16() {
        val instruction = "test WORD [EDX+0x5678], CX"
        store(startAddress + 0xBE08, 0xBACA, Datatype.WORD)
        gprRegisters(ecx = 0xFABA, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xBACA, Datatype.WORD)
        assertGPRRegisters(ecx = 0xFABA, edx = 0x6790)
        assertFlagRegisters(sf = true, zf = false, pf = false, cf = false, of = false)
    }

    @Test fun testTestm32r32() {
        val instruction = "test DWORD [EDX+0x5678], ECX"
        store(startAddress + 0xBE08, 0xDEAD_BABA, Datatype.DWORD)
        gprRegisters(ecx = 0xABCD_EF12, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xDEAD_BABA, Datatype.DWORD)
        assertGPRRegisters(ecx = 0xABCD_EF12, edx = 0x6790)
        assertFlagRegisters(sf = true, zf = false, pf = true, cf = false, of = false)
    }

    @Test fun testTestr8m() {
        val instruction = "test BYTE [EAX+0xFF], DH"
        store(startAddress + 0xF0FF, 0x58, Datatype.BYTE)
        gprRegisters(edx = 0xA700, eax = 0xF000)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FF, 0x58, Datatype.BYTE)
        assertGPRRegisters(edx = 0xA700, eax = 0xF000)
        assertFlagRegisters(sf = false, zf = true, pf = true, cf = false, of = false)
    }

    @Test fun testTestr16m16() {
        val instruction = "test WORD [EDX+0x5678], CX"
        store(startAddress + 0xBE08, 0xBACA, Datatype.WORD)
        gprRegisters(ecx = 0xFABA, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xBACA, Datatype.WORD)
        assertGPRRegisters(ecx = 0xFABA, edx = 0x6790)
        assertFlagRegisters(sf = true, zf = false, pf = false, cf = false, of = false)
    }

    @Test fun testTestr32m32() {
        val instruction = "test DWORD [EDX+0x5678], ECX"
        store(startAddress + 0xBE08, 0xDEAD_BABA, Datatype.DWORD)
        gprRegisters(ecx = 0xABCD_EF12, edx = 0x6790)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08, 0xDEAD_BABA, Datatype.DWORD)
        assertGPRRegisters(ecx = 0xABCD_EF12, edx = 0x6790)
        assertFlagRegisters(sf = true, zf = false, pf = true, cf = false, of = false)
    }

    // TEST RCL INSTRUCTION

    @Test fun rclTestr81() {
        val instruction = "rcl DH, 0x1"
        gprRegisters(edx = 0x2500)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x4B00)
        assertFlagRegisters(cf = false, of = false)
    }

    @Test fun rclTestr8CL() {
        val instruction = "rcl DH, CL"
        gprRegisters(edx = 0x2500, ecx = 3)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x2C00, ecx = 3)
        assertFlagRegisters(cf = true)
    }

    @Test fun rclTestr8imm8() {
        val instruction = "rcl DH, 0x4"
        gprRegisters(edx = 0x2500)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x5900)
        assertFlagRegisters(cf = false)
    }

    @Test fun rclTestr161() {
        val instruction = "rcl CX, 0x1"
        gprRegisters(ecx = 0xA513)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4A27)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rclTestr16CL() {
        val instruction = "rcl DX, CL"
        gprRegisters(edx = 0xA513, ecx = 0xFA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x4D4A, ecx = 0xFA)
    }

    @Test fun rclTestr16imm8() {
        val instruction = "rcl DX, 0x4"
        gprRegisters(edx = 0x2500)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x5009)
        assertFlagRegisters(cf = false)
    }

    @Test fun rclTestr321() {
        val instruction = "rcl ECX, 0x1"
        gprRegisters(ecx = 0xA513_FFC5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4A27_FF8A)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rclTestr32CL() {
        val instruction = "rcl EDX, CL"
        flagRegisters(cf = true)
        gprRegisters(edx = 0x22_A513, ecx = 0xFA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x4E00_454A, ecx = 0xFA)
    }

    @Test fun rclTestr32imm8() {
        val instruction = "rcl EDX, 0x2"
        gprRegisters(edx = 0xFF00_0000)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xFC00_0003)
        assertFlagRegisters(cf = true)
    }

    @Test fun rclTestm81() {
        val instruction = "rcl BYTE [EDX+0xFA12], 0x1"
        store(startAddress + 0xFF45, 0x25, Datatype.BYTE)
        gprRegisters(edx = 0x533)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45, 0x4B, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533)
        assertFlagRegisters(cf = false, of = false)
    }

    @Test fun rclTestm8CL() {
        val instruction = "rcl BYTE [EDX+0xFA12], CL"
        store(startAddress + 0xFF45, 0x25, Datatype.BYTE)
        gprRegisters(edx = 0x533, ecx = 3)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45, 0x2C, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533, ecx = 3)
        assertFlagRegisters(cf = true)
    }

    @Test fun rclTestm8imm8() {
        val instruction = "rcl BYTE [EDX+0xFA12], 0x4"
        store(startAddress + 0xFF45, 0x25, Datatype.BYTE)
        gprRegisters(edx = 0x533)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45, 0x59, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533)
        assertFlagRegisters(cf = false)
    }

    @Test fun rclTestm161() {
        val instruction = "rcl WORD [EDX+0xFA12], 0x1"
        store(startAddress + 0xFF45, 0xA513, Datatype.WORD)
        gprRegisters(edx = 0x533)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45, 0x4A27, Datatype.WORD)
        assertGPRRegisters(edx = 0x533)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rclTestm16CL() {
        val instruction = "rcl WORD [EDX+0xFA12], CL"
        store(startAddress + 0xFF45, 0xA513, Datatype.WORD)
        gprRegisters(edx = 0x533, ecx = 0xFA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533, ecx = 0xFA)
        assertMemory(startAddress + 0xFF45, 0x4D4A, Datatype.WORD)
    }

    @Test fun rclTestm16imm8() {
        val instruction = "rcl WORD [EDX+0xFA12], 0x4"
        store(startAddress + 0xFF45, 0x2500, Datatype.WORD)
        gprRegisters(edx = 0x533)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45, 0x5009, Datatype.WORD)
        assertGPRRegisters(edx = 0x533)
        assertFlagRegisters(cf = false)
    }

    @Test fun rclTestm321() {
        val instruction = "rcl DWORD [EDX+0xFA12], 0x1"
        store(startAddress + 0xFF45, 0xA513_FFC5, Datatype.DWORD)
        gprRegisters(edx = 0x533)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533)
        assertMemory(startAddress + 0xFF45, 0x4A27_FF8A, Datatype.DWORD)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rclTestm32CL() {
        val instruction = "rcl DWORD [EDX+0xFA12], CL"
        store(startAddress + 0xFF45, 0x22_A513, Datatype.DWORD)
        flagRegisters(cf = true)
        gprRegisters(edx = 0x533, ecx = 0xFA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45, 0x4E00_454A, Datatype.DWORD)
        assertGPRRegisters(edx = 0x533, ecx = 0xFA)
    }

    @Test fun rclTestm32imm8() {
        val instruction = "rcl DWORD [EDX+0xFA12], 0x2"
        store(startAddress + 0xFF45, 0xFF00_0000, Datatype.DWORD)
        gprRegisters(edx = 0x533)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533)
        assertMemory(startAddress + 0xFF45, 0xFC00_0003, Datatype.DWORD)
        assertFlagRegisters(cf = true)
    }

    // TEST RCR INSTRUCTION

    @Test fun rcrTestr81() {
        val instruction = "rcr DH, 0x1"
        gprRegisters(edx = 0x2500)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x9200)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rcrTestr16CL() {
        val instruction = "rcr DX, CL"
        gprRegisters(edx = 0xA513, ecx = 0xFA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x89A9, ecx = 0xFA)
    }

    @Test fun rcrTestr32imm8() {
        val instruction = "rcr EDX, 0x2"
        gprRegisters(edx = 0xFF00_0000)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x7FC0_0000)
        flagRegisters(of = true)
    }

    @Test fun rcrTestm8CL() {
        val instruction = "rcr BYTE [EDX+0xFA12], CL"
        store(startAddress + 0xFF45, 0x25, Datatype.BYTE)
        gprRegisters(edx = 0x533, ecx = 3)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45, 0x64, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533, ecx = 3)
        assertFlagRegisters(cf = true)
    }

    @Test fun rcrTestm161() {
        val instruction = "rcr WORD [EDX+0xFA12], 0x1"
        store(startAddress + 0xFF45, 0xA513, Datatype.WORD)
        gprRegisters(edx = 0x533)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45, 0xD289, Datatype.WORD)
        assertGPRRegisters(edx = 0x533)
        assertFlagRegisters(cf = true)
    }

    @Test fun rcrTestm32imm8() {
        val instruction = "rcr DWORD [EDX+0xFA12], 0x2"
        store(startAddress + 0xFF45, 0xFF00_0000, Datatype.DWORD)
        gprRegisters(edx = 0x533)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533)
        assertMemory(startAddress + 0xFF45, 0x7FC0_0000, Datatype.DWORD)
        assertFlagRegisters()
    }

    // TEST ROL INSTRUCTION

    @Test fun rolTestr8imm8() {
        val instruction = "rol DH, 0x4"
        gprRegisters(edx = 0x2500)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x5200)
        assertFlagRegisters(cf = false)
    }

    @Test fun rolTestr16CL() {
        val instruction = "rol DX, CL"
        gprRegisters(edx = 0xA513, ecx = 0xFA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x4E94, ecx = 0xFA)
    }

    @Test fun rolTestr321() {
        val instruction = "rol ECX, 0x1"
        gprRegisters(ecx = 0xA513_FFC5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4A27_FF8B)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rolTestm8CL() {
        val instruction = "rol BYTE [EDX+0xFA12], CL"
        store(startAddress + 0xFF45, 0x25, Datatype.BYTE)
        gprRegisters(edx = 0x533, ecx = 3)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45, 0x29, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533, ecx = 3)
        assertFlagRegisters(cf = true)
    }

    @Test fun rolTestm161() {
        val instruction = "rol WORD [EDX+0xFA12], 0x1"
        store(startAddress + 0xFF45, 0xA513, Datatype.WORD)
        gprRegisters(edx = 0x533)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45, 0x4A27, Datatype.WORD)
        assertGPRRegisters(edx = 0x533)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rolTestm32imm8() {
        val instruction = "rol DWORD [EDX+0xFA12], 0x2"
        store(startAddress + 0xFF45, 0xFF00_0000, Datatype.DWORD)
        gprRegisters(edx = 0x533)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533)
        assertMemory(startAddress + 0xFF45, 0xFC00_0003, Datatype.DWORD)
        assertFlagRegisters(cf = true)
    }

    // TEST ROR INSTRUCTION

    @Test fun rorTestr8CL() {
        val instruction = "ror DH, CL"
        gprRegisters(edx = 0x2500, ecx = 3)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xA400, ecx = 3)
        assertFlagRegisters(cf = true)
    }

    @Test fun rorTestr161() {
        val instruction = "ror CX, 0x1"
        gprRegisters(ecx = 0xA513)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xD289)
        assertFlagRegisters(cf = true)
    }

    @Test fun rorTestr321() {
        val instruction = "ror ECX, 0x1"
        gprRegisters(ecx = 0x2513_FFC5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x9289_FFE2)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rorTestm8imm8() {
        val instruction = "ror BYTE [EDX+0xFA12], 0x4"
        store(startAddress + 0xFF45, 0x25, Datatype.BYTE)
        gprRegisters(edx = 0x533)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45, 0x52, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533)
    }

    @Test fun rorTestm16CL() {
        val instruction = "ror WORD [EDX+0xFA12], CL"
        store(startAddress + 0xFF45, 0xA513, Datatype.WORD)
        gprRegisters(edx = 0x533, ecx = 0xFA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533, ecx = 0xFA)
        assertMemory(startAddress + 0xFF45, 0x44E9, Datatype.WORD)
    }

    @Test fun rorTestm321() {
        val instruction = "ror DWORD [EDX+0xFA12], 0x1"
        store(startAddress + 0xFF45, 0xA513_FFC4, Datatype.DWORD)
        gprRegisters(edx = 0x533)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533)
        assertMemory(startAddress + 0xFF45, 0x5289_FFE2, Datatype.DWORD)
        assertFlagRegisters(of = true)
    }

    // TEST SAL/SHL INSTRUCTION

    @Test fun salShlTestr81() {
        val instruction = "shl DH, 0x1"
        gprRegisters(edx = 0xA500)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x4A00)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun salShlTestr8CL() {
        val instruction = "shl DH, CL"
        gprRegisters(edx = 0x2500, ecx = 3)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x2800, ecx = 3)
        assertFlagRegisters(cf = true, pf = true)
    }

    @Test fun salShlTestr161() {
        val instruction = "shl CX, 0x1"
        gprRegisters(ecx = 0x8000)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0)
        assertFlagRegisters(zf = true, pf = true, cf = true, of = true)
    }

    @Test fun salShlTestr16CL() {
        val instruction = "shl BX, CL"
        gprRegisters(ebx = 0x8000, ecx = 0x454E_ABFF)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x454E_ABFF)
        assertFlagRegisters(zf = true, pf = true)
    }

    // TEST SAR INSTRUCTION

    @Test fun sarTestr321() {
        val instruction = "sar ECX, 0x1"
        gprRegisters(ecx = 0x2513_FFC5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x1289_FFE2)
        assertFlagRegisters(cf = true, pf = true)
    }

    @Test fun sarTestm8imm8() {
        val instruction = "sar BYTE [EDX+0xFA12], 0x4"
        store(startAddress + 0xFF45, 0xFF, Datatype.BYTE)
        gprRegisters(edx = 0x533)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45, 0xFF, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533)
        assertFlagRegisters(cf = true, pf = true, sf = true)
    }

    @Test fun sarTestm32CL() {
        val instruction = "sar DWORD [EDX+0xFA12], CL"
        store(startAddress + 0xFF45, 0xA513_FFC4, Datatype.DWORD)
        gprRegisters(edx = 0x533, ecx = 0xF7)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533, ecx = 0xF7)
        assertMemory(startAddress + 0xFF45, 0xFFFF_FF4A, Datatype.DWORD)
        assertFlagRegisters(sf = true)
    }

    @Test fun sarTestr16CL() {
        val instruction = "sar AX, CL"
        gprRegisters(ecx = 0x10, eax = 0x656D)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x10, eax = 0x0)
        assertFlagRegisters(cf = false, pf = true, zf = true)
    }

    @Test fun sarTestr16CLFlag() {
        val instruction = "sar AL, CL"
        gprRegisters(ecx = 0x10, eax = 0xFF)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x10, eax = 0xFF)
        assertFlagRegisters(cf = true, pf = true, sf = true)
    }

    // TEST SHR INSTRUCTION

    @Test fun shrTestr321() {
        val instruction = "shr ECX, 0x1"
        gprRegisters(ecx = 0x2513_FFC5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x1289_FFE2)
        assertFlagRegisters(cf = true, pf = true)
    }

    @Test fun shrTestm8imm8() {
        val instruction = "shr BYTE [EDX+0xFA12], 0x4"
        store(startAddress + 0xFF45, 0xFF, Datatype.BYTE)
        gprRegisters(edx = 0x533)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45, 0x0F, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533)
        assertFlagRegisters(cf = true, pf = true)
    }

    @Test fun shrTestm32CL() {
        val instruction = "shr DWORD [EDX+0xFA12], CL"
        store(startAddress + 0xFF45, 0xA5C3_FFC4, Datatype.DWORD)
        gprRegisters(edx = 0x533, ecx = 0xF7)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533, ecx = 0xF7)
        assertMemory(startAddress + 0xFF45, 0x0000_014B, Datatype.DWORD)
        assertFlagRegisters(cf = true, pf = true)
    }

    @Test fun shrTestr16CL() {
        val instruction = "shr AX, CL"
        gprRegisters(ecx = 0xE5, eax = 0x656D)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xE5, eax = 0x32B)
        assertFlagRegisters(cf = false, pf = true)
    }

    // TEST CDQ INSTRUCTION

    @Test fun cdqTestEAXNegative() {
        val instruction = "cdq "
        gprRegisters(eax = 0xFFAC_FFC5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFFAC_FFC5, edx = 0xFFFF_FFFF)
    }

    @Test fun cdqTestEAXPositive() {
        val instruction = "cdq "
        gprRegisters(eax = 0x60AC_FFC5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x60AC_FFC5)
    }

    // TEST CWDE INSTRUCTION

    @Test fun cwdeTestAXNegative() {
        val instruction = "cwde "
        gprRegisters(eax = 0xFFC5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFFFF_FFC5)
    }

    @Test fun cwdeTestAXPositive() {
        val instruction = "cwde "
        gprRegisters(eax = 0x60AC)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x60AC)
    }

    // TEST LEA INSTRUCTION

    @Test fun leaTestr16m16() {
        val instruction = "lea AX, [0xFC54]"
        val insnString =  "lea ax, word_0000fc54"
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xFC54, esp = 0x1000)
    }

    @Test fun leaTestr16m32() {
        val instruction = "lea EAX, [ECX + 0xFC54]"
        val insnString =  "lea eax, DWORD [ECX+0xFC54]"
        gprRegisters(ecx = 0xFFA0_1425)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(ecx = 0xFFA0_1425, eax = 0xFFA1_1079)
    }

    @Test fun leaTestr16p32() {
        val instruction = "lea EAX, [ECX + 8 * EBX + 4]"
        val insnString =  "lea eax, DWORD[ECX+8*EBX0x4]"
        gprRegisters(ecx = 0xFFA0_1425, ebx = 0x1F8A)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(ecx = 0xFFA0_1425, eax = 0xFFA1_1079, ebx = 0x1F8A)
    }

    // TEST LSL INSTRUCTION

    @Test fun lslTestr16m16() {
        val instruction = "lsl AX, BX"
        val insnString =  "lsl ax, bx"
        gprRegisters(ebx = 0xBABA)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(ebx = 0xBABA)
    }

    // TEST LDS INSTRUCTION

    @Test fun ldsTestr16m16() {
        val instruction = "lds AX, [0xFC54]"
        val insnString =  "lds ax, FWORD_0000FC54"
        store(startAddress + 0xFC54, 0xFACA_BABA, Datatype.DWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA, esp = 0x1000)
        assertSegmentRegisters(cs = 0x8, ds = 0xFACA, ss = 0x8)
    }

    @Test fun ldsTestr16m32() {
        val instruction = "lds EAX, [0xFC54]"
        val insnString =  "lds eax, FWORD_0000FC54"
        store(startAddress + 0xFC54, 0xFACA_BABA_CABA, Datatype.FWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA_CABA, esp = 0x1000)
        assertSegmentRegisters(cs = 0x8, ds = 0xFACA, ss = 0x8)
    }

    // TEST LES INSTRUCTION

    @Test fun lesTestr16m16() {
        val instruction = "les AX, [0xFC54]"
        val insnString =  "les ax, FWORD_0000FC54"
        store(startAddress + 0xFC54, 0xFACA_BABA, Datatype.DWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA, esp = 0x1000)
        assertSegmentRegisters(ds = 0x8, cs = 0x8, es = 0xFACA, ss = 0x8)
    }

    @Test fun lesTestr16m32() {
        val instruction = "les EAX, [0xFC54]"
        val insnString =  "les eax, FWORD_0000FC54"
        store(startAddress + 0xFC54, 0xFACA_BABA_CABA, Datatype.FWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA_CABA, esp = 0x1000)
        assertSegmentRegisters(ds = 0x8, cs = 0x8, es = 0xFACA, ss = 0x8)
    }

    // TEST LFS INSTRUCTION

    @Test fun lfsTestr16m16() {
        val instruction = "lfs AX, [0xFC54]"
        val insnString =  "lfs ax, FWORD_0000FC54"
        store(startAddress + 0xFC54, 0xFACA_BABA, Datatype.DWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA, esp = 0x1000)
        assertSegmentRegisters(ds = 0x8, cs = 0x8, fs = 0xFACA, ss = 0x8)
    }

    @Test fun lfsTestr16m32() {
        val instruction = "lfs EAX, [0xFC54]"
        val insnString =  "lfs eax, FWORD_0000FC54"
        store(startAddress + 0xFC54, 0xFACA_BABA_CABA, Datatype.FWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA_CABA, esp = 0x1000)
        assertSegmentRegisters(ds = 0x8, cs = 0x8, fs = 0xFACA, ss = 0x8)
    }

    // TEST LFS INSTRUCTION

    @Test fun lgsTestr16m16() {
        val instruction = "lgs AX, [0xFC54]"
        val insnString =  "lgs ax, FWORD_0000FC54"
        store(startAddress + 0xFC54, 0xFACA_BABA, Datatype.DWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA, esp = 0x1000)
        assertSegmentRegisters(ds = 0x8, cs = 0x8, gs = 0xFACA, ss = 0x8)
    }

    @Test fun lgsTestr16m32() {
        val instruction = "lgs EAX, [0xFC54]"
        val insnString =  "lgs eax, FWORD_0000FC54"
        store(startAddress + 0xFC54, 0xFACA_BABA_CABA, Datatype.FWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA_CABA, esp = 0x1000)
        assertSegmentRegisters(ds = 0x8, cs = 0x8, gs = 0xFACA, ss = 0x8)
    }

    // TEST LSS INSTRUCTION

    @Test fun lssTestr16m16() {
        val instruction = "lss AX, [0xFC54]"
        val insnString =  "lss ax, FWORD_0000FC54"
        store(startAddress + 0xFC54, 0xFACA_BABA, Datatype.DWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA, esp = 0x1000)
        assertSegmentRegisters(ds = 0x8, cs = 0x8, ss = 0xFACA)
    }

    @Test fun lssTestr16m32() {
        val instruction = "lss EAX, [0xFC54]"
        val insnString =  "lss eax, FWORD_0000FC54"
        store(startAddress + 0xFC54, 0xFACA_BABA_CABA, Datatype.FWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA_CABA, esp = 0x1000)
        assertSegmentRegisters(ds = 0x8, cs = 0x8, ss = 0xFACA)
    }

    // TEST POP/PUSH INSTRUCTION

    @Test fun pushPopTestr16() {
        val instructionPush = "push AX"
        gprRegisters(eax = 0x60AC)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0x60AC, esp = 0xFFFE)

        val instructionPop = "pop BX"
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertGPRRegisters(eax = 0x60AC, ebx = 0x60AC)
    }

    @Test fun pushPopTestr32() {
        val instructionPush = "push EAX"
        gprRegisters(eax = 0xFFA8_60AC)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0xFFA8_60AC, esp = 0xFFFC)

        val instructionPop = "pop EBX"
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertGPRRegisters(eax = 0xFFA8_60AC, ebx = 0xFFA8_60AC)
    }

    @Test fun pushPopTestimm8r16() {
        val instructionPush = "push 0x16"
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(esp = 0xFFE)

        val instructionPop = "pop BX"
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertGPRRegisters(ebx = 0x16, esp = 0x1000)
    }

    @Test fun pushPopTestimm16m16() {
        val instructionPush = "push 0x60AC"
        store(startAddress + 0xF540, 0xFACA, Datatype.WORD)
        gprRegisters(esp = 0xF000, edx = 0xDA41)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(edx = 0xDA41, esp = 0xEFFE)

        val instructionPop = "pop WORD [EDX+0x1AFF]"
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertMemory(startAddress + 0xF540, 0x60AC, Datatype.WORD)
    }

    @Test fun pushPopTestm16DS() {
        val instructionPush = "push WORD [EDX+0x1AFF]"
        store(startAddress + 0xF540, 0xFACA, Datatype.WORD)
        gprRegisters(edx = 0xDA41, esp = 0xF000)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(edx = 0xDA41, esp = 0xEFFE)

        val instructionPop = "pop FS"
        store(startAddress + 0xF540, 0xFACA, Datatype.WORD)
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertSegmentRegisters(cs = 0x8, ds = 0x8, ss = 0x8, fs = 0xFACA)
    }

    @Test fun pushPopTestGSr32() {
        val instructionPush = "push GS"
        gprRegisters(esp = 0xF000)
        segmentRegisters(gs = 0xABBA)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(esp = 0xEFFE)

        val instructionPop = "pop EAX"
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertGPRRegisters(esp = 0xF002, eax = 0xABBA)
    }

    // TEST POPA/PUSHA INSTRUCTION

    @Test fun pushaPopaTest() {
        val instructionPush = "pusha "
        gprRegisters(eax = 0xFF01, ebx = 0xFF02, ecx = 0xFF03, edx = 0xFF04,
                     esp = 0xF000, ebp = 0xFF05, esi = 0xFF06, edi = 0xFF07)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0xFF01, ebx = 0xFF02, ecx = 0xFF03, edx = 0xFF04,
                           ebp = 0xFF05, esi = 0xFF06, edi = 0xFF07, esp = 0xEFF0)

        val instructionPop = "popa "
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertGPRRegisters(eax = 0xFF01, ebx = 0xFF02, ecx = 0xFF03, edx = 0xFF04,
                           esp = 0xF000, ebp = 0xFF05, esi = 0xFF06, edi = 0xFF07)
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

    @Test fun pushfPopfTestGeneral1() {
        val instructionPush = "pushf "
        flagRegisters(vm = true)
        eflag(0x4221)
        iopl(2)
        x86.cpu.cregs.cr0 = 0L
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertTrue { x86.cpu.exception is x86HardwareException.GeneralProtectionFault }
    }

    @Test fun pushfPopfTestGeneral2() {
        val instructionPush = "pushf "
        eflag(0x4221)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertEflag(0x4221)
        eflag(0)

        val instructionPop = "popf "
        flagRegisters(vm = true)
        iopl(2)
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertTrue { x86.cpu.exception is x86HardwareException.GeneralProtectionFault }
    }

    // TEST LEAVE INSTRUCTION

    @Test fun leaveTest() {
        val instructionPush = "push 0xCA16"
        gprRegisters(esp = 0xF000)
        execute(-3) { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(esp = 0xEFFE)

        gprRegisters(esp = 0xB2AC, ebp = 0xEFFE)
        val instructionLeave = "leave "
        execute { assemble(instructionLeave) }
        assertAssembly(instructionLeave)
        assertGPRRegisters(ebp = 0xCA16, esp = 0xF000)
    }

    // TEST ENTER INSTRUCTION

    @Test fun enterTest() {
        val instructionLeave = "enter 0x82, 0x0"
        execute { assemble(instructionLeave) }
        assertAssembly(instructionLeave)
        assertGPRRegisters(esp = 0xF7C, ebp = 0xFFE)
    }

    // TEST MOVSX INSTRUCTION

    @Test fun movsxTestr16m8() {
        val instruction = "movsx AX, BYTE [EDX+0x1AFF]"
        store(startAddress + 0xF540, 0x7A, Datatype.WORD)
        gprRegisters(eax = 0xCAFF, edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x7A, edx = 0xDA41)
    }

    @Test fun movsxTestr32m8() {
        val instruction = "movsx EAX, BYTE [EDX+0x1AFF]"
        store(startAddress + 0xF540, 0xFA, Datatype.WORD)
        gprRegisters(eax = 0xCAFF, edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFFFF_FFFA, edx = 0xDA41)
    }

    @Test fun movsxTestr32r16() {
        val instruction = "movsx EAX, DX"
        gprRegisters(eax = 0xACCA_CAFF, edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFFFF_DA41, edx = 0xDA41)
    }

    // TEST MOVZX INSTRUCTION

    @Test fun movzxTestr32m16() {
        val instruction = "movzx EAX, WORD [EDX+0x1AFF]"
        store(startAddress + 0xF540, 0xFF7A, Datatype.WORD)
        gprRegisters(eax = 0xFFFF_CAFF, edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFF7A, edx = 0xDA41)
    }

    @Test fun movzxTestr32r8() {
        val instruction = "movzx EAX, BL"
        gprRegisters(eax = 0xCAFF, ebx = 0x3E52, edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x52, edx = 0xDA41, ebx = 0x3E52)
    }

    @Test fun movzxTestr16r8() {
        val instruction = "movzx AX, CL"
        gprRegisters(eax = 0xACCA_CAFF, edx = 0xDA41, ecx = 0xF1)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xACCA_00F1, ecx = 0xF1, edx = 0xDA41)
    }

    // TEST MOV INSTRUCTION

    @Test fun movTestr8m8() {
        val instruction = "mov AL, BYTE [EDX+0x1AFF]"
        store(startAddress + 0xF540, 0x7A, Datatype.BYTE)
        gprRegisters(eax = 0xCAFF, edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xCA7A, edx = 0xDA41)
    }

    @Test fun movTestr16m16() {
        val instruction = "mov AX, WORD [EDX+0x1AFF]"
        store(startAddress + 0xF540, 0xFF7A, Datatype.WORD)
        gprRegisters(eax = 0xCAFF, edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFF7A, edx = 0xDA41)
    }

    @Test fun movTestr32m32() {
        val instruction = "mov EAX, DWORD [EDX+0x1AFF]"
        store(startAddress + 0xF540, 0xFFFF_FFFA, Datatype.DWORD)
        gprRegisters(eax = 0xCAFF, edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFFFF_FFFA, edx = 0xDA41)
    }

    @Test fun movTestr8r8() {
        val instruction = "mov AL, DH"
        gprRegisters(eax = 0xCA, edx = 0xDA00)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xDA, edx = 0xDA00)
    }

    @Test fun movTestr16r16() {
        val instruction = "mov AX, BX"
        gprRegisters(eax = 0xCAFF, ebx = 0x3E52)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x3E52, ebx = 0x3E52)
    }

    @Test fun movTestr32r32() {
        val instruction = "mov EAX, ECX"
        gprRegisters(eax = 0xACCA_CAFF, ecx = 0xDA41_01F1)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xDA41_01F1, ecx = 0xDA41_01F1)
    }

    @Test fun movTestr8imm8() {
        val instruction = "mov AL, 0xDA"
        gprRegisters(eax = 0xCA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xDA)
    }

    @Test fun movTestr16imm16() {
        val instruction = "mov AX, 0x3E52"
        gprRegisters(eax = 0xCAFF)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x3E52)
    }

    @Test fun movTestr32imm32() {
        val instruction = "mov EAX, 0xDA4101F1"
        gprRegisters(eax = 0xACCA_CAFF)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xDA41_01F1)
    }

    @Test fun movTestm8imm8() {
        val instruction = "mov BYTE [EDX+0x10F7], 0xDA"
        gprRegisters(edx = 0xACA1)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBD98, 0xDA, Datatype.BYTE)
        assertGPRRegisters(edx = 0xACA1)
    }

    @Test fun movTestm16imm16() {
        val instruction = "mov WORD [EDX+0x10F7], 0xA0DA"
        gprRegisters(edx = 0xACA1)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBD98, 0xA0DA, Datatype.WORD)
        assertGPRRegisters(edx = 0xACA1)
    }

    @Test fun movTestm32imm32() {
        val instruction = "mov DWORD [EDX+0x10F7], 0xFAC6A0DA"
        gprRegisters(edx = 0xACA1)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBD98, 0xFAC6_A0DA, Datatype.DWORD)
        assertGPRRegisters(edx = 0xACA1)
    }

    @Test fun movTestm8r8() {
        val instruction = "mov BYTE [EDX+0x10F7], AH"
        gprRegisters(edx = 0xACA1, eax = 0xDA00)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBD98, 0xDA, Datatype.BYTE)
        assertGPRRegisters(edx = 0xACA1, eax = 0xDA00)
    }

    @Test fun movTestm16r16() {
        val instruction = "mov WORD [EDX+0x10F7], AX"
        gprRegisters(edx = 0xACA1, eax = 0xA0DA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBD98, 0xA0DA, Datatype.WORD)
        assertGPRRegisters(edx = 0xACA1, eax = 0xA0DA)
    }

    @Test fun movTestm32r32() {
        val instruction = "mov DWORD [EDX+0x10F7], EBX"
        gprRegisters(edx = 0xACA1, ebx = 0xFAC6_A0DA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBD98, 0xFAC6_A0DA, Datatype.DWORD)
        assertGPRRegisters(edx = 0xACA1, ebx = 0xFAC6_A0DA)
    }

    @Test fun movTestm16Sreg() {
        val instruction = "mov WORD [EDX+0x10F7], GS"
        segmentRegisters(gs = 0xACFA)
        gprRegisters(edx = 0xACA1)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBD98, 0xACFA, Datatype.WORD)
        assertGPRRegisters(edx = 0xACA1)
        assertSegmentRegisters(gs = 0xACFA, ds = 0x8, ss = 0x8, cs = 0x8)
    }

    @Test fun movTestr16Sreg() {
        val instruction = "mov DX, GS"
        segmentRegisters(gs = 0xACFA)
        gprRegisters(edx = 0xACA1)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xACFA)
        assertSegmentRegisters(gs = 0xACFA, ds = 0x8, ss = 0x8, cs = 0x8)
    }

    @Test fun movTestSregm16() {
        val instruction = "mov FS, WORD [EDX+0x1AFF]"
        store(startAddress + 0xF540, 0xFF7A, Datatype.WORD)
        gprRegisters(edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xDA41)
        assertSegmentRegisters(fs = 0xFF7A, ds = 0x8, ss = 0x8, cs = 0x8)
    }

    @Test fun movTestSregr16() {
        val instruction = "mov FS, DX"
        gprRegisters(edx = 0xFF7A)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xFF7A)
        assertSegmentRegisters(fs = 0xFF7A, ds = 0x8, ss = 0x8, cs = 0x8)
    }

    // TEST XCHG INSTRUCTION

    @Test fun xchgTestm8r8() {
        val instruction = "xchg BH, BYTE [EDX+0x1AFF]"
        store(startAddress + 0xF540, 0x7A, Datatype.BYTE)
        gprRegisters(edx = 0xDA41, ebx = 0x6543)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xDA41, ebx = 0x7A43)
        assertMemory(startAddress + 0xF540, 0x65, Datatype.BYTE)
    }

    @Test fun xchgTestr16r16() {
        val instruction = "xchg AX, DX"
        gprRegisters(eax = 0xCAFF, edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xCAFF, eax = 0xDA41)
    }

    @Test fun xchgTestr32r32() {
        val instruction = "xchg EAX, EDX"
        gprRegisters(eax = 0xFAFA_CAFF, edx = 0xBABA_DA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xFAFA_CAFF, eax = 0xBABA_DA41)
    }

    // TEST SETA INSTRUCTION

    @Test fun setaTestm8Posistive() {
        val instruction = "seta BYTE [EDX+0x1AFF]"
        gprRegisters(edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540, 0x1, Datatype.BYTE)
    }

    @Test fun setaTestm8Negative1() {
        val instruction = "seta BYTE [EDX+0x1AFF]"
        flagRegisters(zf = true)
        gprRegisters(edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540, 0, Datatype.BYTE)
    }

    @Test fun setaTestm8Negative2() {
        val instruction = "seta BYTE [EDX+0x1AFF]"
        flagRegisters(cf = true)
        gprRegisters(edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540, 0, Datatype.BYTE)
    }

    @Test fun setaTestm8Negative3() {
        val instruction = "seta BYTE [EDX+0x1AFF]"
        flagRegisters(zf = true, cf = true)
        gprRegisters(edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540, 0, Datatype.BYTE)
    }

    // TEST SETB INSTRUCTION

    @Test fun setbTestm8Posistive() {
        val instruction = "setb BYTE [EDX+0x1AFF]"
        gprRegisters(edx = 0xDA41)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540, 0x1, Datatype.BYTE)
    }

    @Test fun setbTestm8Negative() {
        val instruction = "setb BYTE [EDX+0x1AFF]"
        gprRegisters(edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540, 0, Datatype.BYTE)
    }

    // TEST SETBE INSTRUCTION

    @Test fun setbeTestm8Posistive1() {
        val instruction = "setbe BYTE [EDX+0x1AFF]"
        gprRegisters(edx = 0xDA41)
        flagRegisters(zf = true, cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540, 0x1, Datatype.BYTE)
    }

    @Test fun setbeTestm8Posistive2() {
        val instruction = "setbe BYTE [EDX+0x1AFF]"
        flagRegisters(zf = true)
        gprRegisters(edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540, 0x1, Datatype.BYTE)
    }

    @Test fun setbeTestm8Posistive3() {
        val instruction = "setbe BYTE [EDX+0x1AFF]"
        flagRegisters(cf = true)
        gprRegisters(edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540, 0x1, Datatype.BYTE)
    }

    @Test fun setbeTestm8Negative() {
        val instruction = "setbe BYTE [EDX+0x1AFF]"
        gprRegisters(edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540, 0, Datatype.BYTE)
    }

    // TEST SETG INSTRUCTION

    @Test fun setgTestm8Posistive1() {
        val instruction = "setg BYTE [EDX+0x1AFF]"
        gprRegisters(edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540, 0x1, Datatype.BYTE)
    }

    @Test fun setgTestm8Posistive2() {
        val instruction = "setg BYTE [EDX+0x1AFF]"
        flagRegisters(sf = true, of = true)
        gprRegisters(edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540, 0x1, Datatype.BYTE)
    }

    @Test fun setgTestm8Negative1() {
        val instruction = "setg BYTE [EDX+0x1AFF]"
        flagRegisters(zf = true)
        gprRegisters(edx = 0xDA41)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540, 0, Datatype.BYTE)
    }

    @Test fun setgTestm8Negative2() {
        val instruction = "setg BYTE [EDX+0x1AFF]"
        gprRegisters(edx = 0xDA41)
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540, 0, Datatype.BYTE)
    }

    // TEST SETGE INSTRUCTION

    @Test fun setgeTestr8Posistive1() {
        val instruction = "setge AH"
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100, esp = 0x1000)
    }

    @Test fun setgeTestr8Posistive2() {
        val instruction = "setge AH"
        flagRegisters(sf = true, of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100, esp = 0x1000)
    }

    @Test fun setgeTestr8Negative1() {
        val instruction = "setge AH"
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(esp = 0x1000)
    }

    @Test fun setgeTestr8Negative2() {
        val instruction = "setge AH"
        flagRegisters(of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(esp = 0x1000)
    }

    // TEST SETL INSTRUCTION

    @Test fun setlTestr8Positive1() {
        val instruction = "setl AH"
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100, esp = 0x1000)
    }

    @Test fun setlTestr8Positive2() {
        val instruction = "setl AH"
        flagRegisters(of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100, esp = 0x1000)
    }

    @Test fun setlTestr8Negative1() {
        val instruction = "setl AH"
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(esp = 0x1000)
    }

    @Test fun setlTestr8Negative2() {
        val instruction = "setl AH"
        flagRegisters(sf = true, of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(esp = 0x1000)
    }

    // TEST SETLE INSTRUCTION

    @Test fun setleTestr8Positive1() {
        val instruction = "setle AH"
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100, esp = 0x1000)
    }

    @Test fun setleTestr8Positive2() {
        val instruction = "setle AH"
        flagRegisters(of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100, esp = 0x1000)
    }

    @Test fun setleTestr8Positive3() {
        val instruction = "setle AH"
        flagRegisters(zf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100, esp = 0x1000)
    }

    @Test fun setleTestr8Positive4() {
        val instruction = "setle AH"
        flagRegisters(zf = true, sf = true, of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100, esp = 0x1000)
    }

    @Test fun setleTestr8Negative1() {
        val instruction = "setle AH"
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(esp = 0x1000)
    }

    @Test fun setleTestr8Negative2() {
        val instruction = "setle AH"
        flagRegisters(sf = true, of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(esp = 0x1000)
    }

    // TEST SETNB INSTRUCTION

    @Test fun setnbTestm8Posistive() {
        val instruction = "setnb BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0x1, Datatype.BYTE)
    }

    @Test fun setnbTestm8Negative() {
        val instruction = "setnb BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0, Datatype.BYTE)
    }

    // TEST SETNE INSTRUCTION

    @Test fun setneTestm8Positive() {
        val instruction = "setne BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0x1, Datatype.BYTE)
    }

    @Test fun setneTestm8Negative() {
        val instruction = "setne BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        flagRegisters(zf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0, Datatype.BYTE)
    }

    // TEST SETNO INSTRUCTION

    @Test fun setnoTestm8Posistive() {
        val instruction = "setno BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0x1, Datatype.BYTE)
    }

    @Test fun setnoTestm8Negative() {
        val instruction = "setno BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        flagRegisters(of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0, Datatype.BYTE)
    }

    // TEST SETNS INSTRUCTION

    @Test fun setnsTestm8Posistive() {
        val instruction = "setns BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0x1, Datatype.BYTE)
    }

    @Test fun setnsTestm8Negative() {
        val instruction = "setns BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0, Datatype.BYTE)
    }

    // TEST SETO INSTRUCTION

    @Test fun setoTestm8Positive() {
        val instruction = "seto BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        flagRegisters(of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0x1, Datatype.BYTE)
    }

    @Test fun setoTestm8Negative() {
        val instruction = "seto BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0, Datatype.BYTE)
    }

    // TEST SETPE INSTRUCTION

    @Test fun setpeTestm8Positive() {
        val instruction = "setpe BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        flagRegisters(pf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0x1, Datatype.BYTE)
    }

    @Test fun setpeTestm8Negative() {
        val instruction = "setpe BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0, Datatype.BYTE)
    }

    // TEST SETPO INSTRUCTION

    @Test fun setpoTestm8Posistive() {
        val instruction = "setpo BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0x1, Datatype.BYTE)
    }

    @Test fun setpoTestm8Negative() {
        val instruction = "setpo BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        flagRegisters(pf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0, Datatype.BYTE)
    }

    // TEST SETS INSTRUCTION

    @Test fun setsTestm8Positive() {
        val instruction = "sets BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0x1, Datatype.BYTE)
    }

    @Test fun setsTestm8Negative() {
        val instruction = "sets BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0, Datatype.BYTE)
    }

    // TEST SETZ INSTRUCTION

    @Test fun setzTestm8Positive() {
        val instruction = "setz BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        flagRegisters(zf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0x1, Datatype.BYTE)
    }

    @Test fun setzTestm8Negative() {
        val instruction = "setz BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AE, 0, Datatype.BYTE)
    }

    // TEST CALL INSTRUCTION

    @Test fun callTestptr1616() {
        val instruction = "call 0x1234:0x5678" // 5 byte hex
        val insnString = "call 1234:00005678"
        execute(0x5673) { assemble(instruction) }
        assertAssembly(insnString)
        assertSegmentRegisters(ds = 0x8, ss = 0x8, cs = 0x1234)
    }

    @Test fun callTestptr1632() {
        val instruction = "call 0x1234: dword 0xAAC_5678" // 8 byte hex
        val insnString = "call 1234:0AAC5678"
        execute(0xAAC_5670) { assemble(instruction) }
        assertAssembly(insnString)
        assertSegmentRegisters(ds = 0x8, ss = 0x8, cs = 0x1234)
    }

    @Test fun callTestrel16() {
        val instruction = "call 0x5678"  // 3 byte hex
        val insnString = "call 0x5675"
        execute(0x5675) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun callTestrel32() {
        val instruction = "call dword 0xAAC_5678"  // 6 byte hex
        val insnString = "call 0xAAC5672"
        execute(0xAAC_5672) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun callTestr16() {
        val instruction = "call DX"  // 2 byte hex
        gprRegisters(edx = 0x5678)
        execute(0x5676) { assemble(instruction) }
        assertAssembly(instruction)
    }

    @Test fun callTestr32() {
        val instruction = "call EDX"  // 3 byte hex
        gprRegisters(edx = 0xAAC_5678)
        execute(0xAAC_5675) { assemble(instruction) }
        assertAssembly(instruction)
    }

    @Test fun callTestm1616() {
        val instruction = "call word far [cs: word 0xF540]" // 5 byte hex
        val insnString = "call 0008:00005678"
        store(startAddress + 0xF540, 0x5678, Datatype.WORD)
        store(startAddress + 0xF542, 0x0008, Datatype.WORD)
        execute(0x5673) { assemble(instruction) }
        assertAssembly(insnString)
        assertSegmentRegisters(ds = 0x8, ss = 0x8, cs = 0x08)
    }

    @Test fun callTestm1632() {
        val instruction = "call dword far [fs: dword 0xF540]" // 9 byte hex
        val insnString = "call 0008:0AAC5678"
        segmentRegisters(cs = 0x08, ds = 0x08, ss = 0x08, es = 0x08, fs = 0x8, gs = 0x08)
        store(startAddress + 0xF540, 0xAAC_5678 , Datatype.DWORD)
        store(startAddress + 0xF544, 0x0008 , Datatype.WORD)
        execute(0xAAC_566F) { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JMP INSTRUCTION

    @Test fun jmpTestptr1616() {
        val instruction = "jmp 0x1234:0x5678" // 5 byte hex
        val insnString = "jmp 1234:00005678"
        execute(0x5673) { assemble(instruction) }
        assertAssembly(insnString)
        assertSegmentRegisters(ds = 0x8, ss = 0x8, cs = 0x1234)
    }

    @Test fun jmpTestptr1632() {
        val instruction = "jmp 0x1234: dword 0xAAC_5678" // 8 byte hex
        val insnString = "jmp 1234:0AAC5678"
        execute(0xAAC_5670) { assemble(instruction) }
        assertAssembly(insnString)
        assertSegmentRegisters(ds = 0x8, ss = 0x8, cs = 0x1234)
    }

    @Test fun jmpTestrel16() {
        val instruction = "jmp 0x5678"  // 3 byte hex
        val insnString = "jmp 0x5675"
        execute(0x5675) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jmpTestrel32() {
        val instruction = "jmp dword 0xAAC_5678"  // 6 byte hex
        val insnString = "jmp 0xAAC5672"
        execute(0xAAC_5672) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jmpTestr16() {
        val instruction = "jmp DX"  // 2 byte hex
        gprRegisters(edx = 0x5678)
        execute(0x5676) { assemble(instruction) }
        assertAssembly(instruction)
    }

    @Test fun jmpTestr32() {
        val instruction = "jmp EDX"  // 3 byte hex
        gprRegisters(edx = 0xAAC_5678)
        execute(0xAAC_5675) { assemble(instruction) }
        assertAssembly(instruction)
    }

    @Test fun jmpTestm1616() {
        val instruction = "jmp word far [cs: word 0xF540]" // 5 byte hex
        val insnString = "jmp 0008:00005678"
        store(startAddress + 0xF540, 0x5678, Datatype.WORD)
        store(startAddress + 0xF542, 0x0008, Datatype.WORD)
        execute(0x5673) { assemble(instruction) }
        assertAssembly(insnString)
        assertSegmentRegisters(ds = 0x8, ss = 0x8, cs = 0x8)
    }

    @Test fun jmpTestm1632() {
        val instruction = "jmp dword far [fs: dword 0xF540]" // 9 byte hex
        val insnString = "jmp 0008:0AAC5678"
        segmentRegisters(cs = 0x08, ds = 0x08, ss = 0x08, es = 0x08, fs = 0x8, gs = 0x08)
        store(startAddress + 0xF540, 0xAAC_5678, Datatype.DWORD)
        store(startAddress + 0xF544, 0x0008, Datatype.WORD)
        execute(0xAAC_566F) { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST RET INSTRUCTION

    @Test fun retTestNear16() {
        val instructionPush = "push AX"
        gprRegisters(eax = 0x60AC, esp = 0xF000)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0x60AC, esp = 0xEFFE)

        val instruction = "retn 0x0"
        val insnString = "ret 0x0"
        execute(0x60A8) { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0x60AC, esp = 0xF000)
    }

    @Test fun retTestNear32() {
        val instructionPush = "push EAX"
        gprRegisters(eax = 0xCAFF_60AC, esp = 0xF000)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0xCAFF_60AC, esp = 0xEFFC)

        val instruction = "retf 0x0"
        val insnString = "ret 0x0"
        execute(0x60A7) { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xCAFF_60AC, esp = 0xF000)
        assertSegmentRegisters(ds = 0x8, ss = 0x8, cs = 0xCAFF)
    }

    @Test fun retTestFar16() {
        val instructionPush = "push EAX"
        gprRegisters(eax = 0x1111_2222, esp = 0xF000)
        execute(-2) { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0x1111_2222, esp = 0xEFFC)

        gprRegisters(eax = 0x0000_4444, esp = 0xEFFC)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0x0000_4444, esp = 0xEFF8)

        val instruction = "retn 0x2"
        val insnString = "ret 0x2"
        execute(0x443F) { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0x4444, esp = 0xEFFC)
    }

    @Test fun retTestFar32() {
        val instructionPush = "push EAX"
        gprRegisters(eax = 0x1111_2222, esp = 0xF000)
        execute(-2) { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0x1111_2222, esp = 0xEFFC)

        gprRegisters(eax = 0x5555_4444, esp = 0xEFFC)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0x5555_4444, esp = 0xEFF8)

        gprRegisters(esp = 0xEFF8, eip = 2)
        val instruction = "retf 0x2"
        val insnString = "ret 0x2"
        execute(0x443F) { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(esp = 0xEFFE)
        assertSegmentRegisters(ds = 0x8, ss = 0x8, cs = 0x5555)

        segmentRegisters()
        val instructionPop = "pop EAX"
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertGPRRegisters(eax = 0x1111, esp = 0xF002)
    }

    // TEST JA INSTRUCTION

    @Test fun jaTestrel8Positive() {
        val instruction = "ja 0x78"  // 4 byte hex
        val insnString = "ja 0x0074"
        execute(0x74) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jaTestrel16Negative1() {
        val instruction = "ja 0x5678"  // 4 byte hex
        val insnString = "ja 0x5674"
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jaTestrel16Negative2() {
        val instruction = "ja 0x5678"  // 4 byte hex
        val insnString = "ja 0x5674"
        flagRegisters(zf = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jaTestrel16Negative3() {
        val instruction = "ja 0x5678"  // 4 byte hex
        val insnString = "ja 0x5674"
        flagRegisters(zf = true, cf = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JNB INSTRUCTION

    @Test fun jnbTestrel16Positive() {
        val instruction = "jnb 0x5678"  // 4 byte hex
        val insnString = "jnb 0x5674"
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jnbTestrel16Negative() {
        val instruction = "jnb 0x5678"  // 4 byte hex
        val insnString = "jnb 0x5674"
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JB INSTRUCTION

    @Test fun jbTestrel16Positive() {
        val instruction = "jb 0x5678"  // 4 byte hex
        val insnString = "jb 0x5674"
        flagRegisters(cf = true)
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jbTestrel16Negative() {
        val instruction = "jb 0x5678"  // 4 byte hex
        val insnString = "jb 0x5674"
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JBE INSTRUCTION

    @Test fun jbeTestrel16Negative() {
        val instruction = "jbe 0x5678"  // 4 byte hex
        val insnString = "jbe 0x5674"
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jbeTestrel16Positive1() {
        val instruction = "jbe 0x5678"  // 4 byte hex
        val insnString = "jbe 0x5674"
        flagRegisters(cf = true)
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jbeTestrel16Positive2() {
        val instruction = "jbe 0x5678"  // 4 byte hex
        val insnString = "jbe 0x5674"
        flagRegisters(zf = true)
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jbeTestrel16Positive3() {
        val instruction = "jbe 0x5678"  // 4 byte hex
        val insnString = "jbe 0x5674"
        flagRegisters(zf = true, cf = true)
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JE INSTRUCTION

    @Test fun jeTestrel16Positive() {
        val instruction = "je 0x5678"  // 4 byte hex
        val insnString = "je 0x5674"
        flagRegisters(zf = true)
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jeTestrel16Negative() {
        val instruction = "je 0x5678"  // 4 byte hex
        val insnString = "je 0x5674"
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JG INSTRUCTION

    @Test fun jgTestrel16Positive1() {
        val instruction = "jg 0x5678"  // 4 byte hex
        val insnString = "jg 0x5674"
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jgTestrel8Positive2() {
        val instruction = "jg 0x78"  // 4 byte hex
        val insnString = "jg 0x0074"
        flagRegisters(sf = true, of = true)
        execute(0x74) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jgTestrel16Negative1() {
        val instruction = "jg 0x5678"  // 4 byte hex
        val insnString = "jg 0x5674"
        flagRegisters(zf = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jgTestrel16Negative2() {
        val instruction = "jg 0x5678"  // 4 byte hex
        val insnString = "jg 0x5674"
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jgTestrel16Negative3() {
        val instruction = "jg 0x5678"  // 4 byte hex
        val insnString = "jg 0x5674"
        flagRegisters(of = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JGE INSTRUCTION

    @Test fun jgeTestrel16Positive1() {
        val instruction = "jge 0x5678"  // 4 byte hex
        val insnString = "jge 0x5674"
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jgeTestrel16Positive2() {
        val instruction = "jge 0x5678"  // 4 byte hex
        val insnString = "jge 0x5674"
        flagRegisters(sf = true, of = true)
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jgeTestrel16Negative2() {
        val instruction = "jge 0x5678"  // 4 byte hex
        val insnString = "jge 0x5674"
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jgeTestrel16Negative3() {
        val instruction = "jge 0x5678"  // 4 byte hex
        val insnString = "jge 0x5674"
        flagRegisters(of = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JL INSTRUCTION

    @Test fun jlTestrel16Positive1() {
        val instruction = "jl 0x5678"  // 4 byte hex
        val insnString = "jl 0x5674"
        flagRegisters(of = true)
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jlTestrel16Positive2() {
        val instruction = "jl 0x5678"  // 4 byte hex
        val insnString = "jl 0x5674"
        flagRegisters(sf = true)
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jlTestrel16Negative1() {
        val instruction = "jl 0x5678"  // 4 byte hex
        val insnString = "jl 0x5674"
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jlTestrel16Negative2() {
        val instruction = "jl 0x5678"  // 4 byte hex
        val insnString = "jl 0x5674"
        flagRegisters(of = true, sf = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JLE INSTRUCTION

    @Test fun jleTestrel16Positive1() {
        val instruction = "jle 0x5678"  // 4 byte hex
        val insnString = "jle 0x5674"
        flagRegisters(zf = true, of = true)
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jleTestrel16Positive2() {
        val instruction = "jle 0x5678"  // 4 byte hex
        val insnString = "jle 0x5674"
        flagRegisters(zf = true, sf = true)
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jleTestrel16Positive3() {
        val instruction = "jle 0x5678"  // 4 byte hex
        val insnString = "jle 0x5674"
        flagRegisters(zf = true, of = true, sf = true)
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jleTestrel16Positive4() {
        val instruction = "jle 0x5678"  // 4 byte hex
        val insnString = "jle 0x5674"
        flagRegisters(zf = true)
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jleTestrel16Positive5() {
        val instruction = "jle 0x5678"  // 4 byte hex
        val insnString = "jle 0x5674"
        flagRegisters(of = true)
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jleTestrel16Positive6() {
        val instruction = "jle 0x5678"  // 4 byte hex
        val insnString = "jle 0x5674"
        flagRegisters(sf = true)
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jleTestrel16Negative1() {
        val instruction = "jle 0x5678"  // 4 byte hex
        val insnString = "jle 0x5674"
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jleTestrel16Negative2() {
        val instruction = "jle 0x5678"  // 4 byte hex
        val insnString = "jle 0x5674"
        flagRegisters(of = true, sf = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JNE INSTRUCTION

    @Test fun jneTestrel16Positive() {
        val instruction = "jne 0x5678"  // 4 byte hex
        val insnString = "jne 0x5674"
        execute(0x5674) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jneTestrel16Negative() {
        val instruction = "jne 0x5678"  // 4 byte hex
        val insnString = "jne 0x5674"
        flagRegisters(zf = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JO INSTRUCTION

    @Test fun joTestrel16Positive() {
        val instruction = "jo 0xFAC4"  // 4 byte hex
        val insnString = "jo 0xFAC0"
        flagRegisters(of = true)
        execute(0xFFFFFAC0) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun joTestrel16Negative() {
        val instruction = "jo 0x5678"  // 4 byte hex
        val insnString = "jo 0x5674"
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JNO INSTRUCTION

    @Test fun jnoTestrel16Positive() {
        val instruction = "jno 0xFAC4"  // 4 byte hex
        val insnString = "jno 0xFAC0"
        execute(0xFFFFFAC0) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jnoTestrel16Negative() {
        val instruction = "jno 0x5678"  // 4 byte hex
        val insnString = "jno 0x5674"
        flagRegisters(of = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JPO INSTRUCTION

    @Test fun jpoTestrel16Positive() {
        val instruction = "jpo 0xFAC4"  // 4 byte hex
        val insnString = "jpo 0xFAC0"
        execute(0xFFFFFAC0) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jpoTestrel16Negative() {
        val instruction = "jpo 0x5678"  // 4 byte hex
        val insnString = "jpo 0x5674"
        flagRegisters(pf = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JNS INSTRUCTION

    @Test fun jnsTestrel16Positive() {
        val instruction = "jns 0xFAC4"  // 4 byte hex
        val insnString = "jns 0xFAC0"
        execute(0xFFFFFAC0) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jnsTestrel16Negative() {
        val instruction = "jns 0x5678"  // 4 byte hex
        val insnString = "jns 0x5674"
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JS INSTRUCTION

    @Test fun jsTestrel16Positive() {
        val instruction = "js 0xFAC4"  // 4 byte hex
        val insnString = "js 0xFAC0"
        flagRegisters(sf = true)
        execute(0xFFFFFAC0) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jsTestrel16Negative() {
        val instruction = "js 0x5678"  // 4 byte hex
        val insnString = "js 0x5674"
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JECXZ INSTRUCTION

    @Test fun jecxzTestrel16Positive() {
        val instruction = "jecxz 0xC4"  // 3 byte hex
        val insnString = "jecxz 0x00C1"
        execute(0xFFFF_FFC1) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jecxzTestrel16Negative() {
        val instruction = "jecxz 0x78"  // 3 byte hex
        val insnString = "jecxz 0x0075"
        gprRegisters(ecx = 0xF000_0000)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JCXZ INSTRUCTION

    @Test fun jcxzTestrel16Positive1() {
        val instruction = "jcxz 0xC4"  // 2 byte hex
        val insnString = "jecxz 0x00C2"
        execute(0xFFFF_FFC2) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jcxzTestrel16Positive2() {
        val instruction = "jcxz 0x78"  // 4 byte hex
        val insnString = "jecxz 0x0076"
        gprRegisters(ecx = 0xF000_0000)
        execute(0x76) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jcxzTestrel16Negative() {
        val instruction = "jcxz 0x78"  // 4 byte hex
        val insnString = "jecxz 0x0076"
        gprRegisters(ecx = 0xF000)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JPE INSTRUCTION

    @Test fun jpeTestrel16Positive() {
        val instruction = "jpe 0xFAC4"  // 4 byte hex
        val insnString = "jpe 0xFAC0"
        flagRegisters(pf = true)
        execute(0xFFFFFAC0) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jpeTestrel16Negative() {
        val instruction = "jpe 0x5678"  // 4 byte hex
        val insnString = "jpe 0x5674"
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST CLI INSTRUCTION

    @Test fun cliTest() {
        val instruction = "cli "
        flagRegisters(ifq = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters()
    }

    @Test fun cliTestPe() {
        x86Register.CTRLR.cr0.pe(x86, false)
        val instruction = "cli "
        flagRegisters(ifq = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters()
    }

    // TEST INT INSTRUCTION

    @Test fun intTest() {
        val instruction = "int 0xFA"
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertCopRegisters(int = true, irq = 0xFA)
    }

    // TEST INT3 INSTRUCTION

    @Test fun int3Test() {
        val instruction = "int3 "
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertCopRegisters(int = true, irq = 3)
    }

    // TEST LAHF INSTRUCTION

    @Test fun lahfTest() {
        val instruction = "lahf "
        flagRegisters(sf = true, af = true, pf = true, cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x9700, esp = 0x1000)
    }

    // TEST LAR INSTRUCTION

    @Test fun larTestr32r32() {
        val instruction = "lar ebx, eax"
        gprRegisters(eax = 0x2, ebx = 0x907C_FAFA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x2, ebx = 0x0100)
        assertFlagRegisters(zf = true)
    }

    // TEST STI INSTRUCTION

    @Test fun stiTest() {
        val instruction = "sti "
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters(ifq = true)
    }

    @Test fun stiTestPe() {
        val instruction = "sti "
        x86Register.CTRLR.cr0.pe(x86, false)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters(ifq = true)
    }

    // TEST LIDT INSTRUCTION

    @Test fun lidtTestm48() {
        val instruction = "lidt [EAX+0xFF]"
        val insnString = "lidt fword [EAX+0xFF]"
        store(startAddress + 0xF0FF, "CA110008FFAC")
        gprRegisters(eax = 0xF000)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertCopRegisters(idtrBase = 0xFF0800, idtrLimit = 0x11CA, irq = -1)
    }

    // TEST LGDT INSTRUCTION

    @Test fun lgdtTestm48() {
        val instruction = "lgdt [EAX+0xFF]"
        val insnString = "lgdt fword [EAX+0xFF]"
        store(startAddress + 0xF0FF, "CA110008FFAC")
        gprRegisters(eax = 0xF000)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertMMURegisters(gdtrBase = 0xFF0800, gdtrLimit = 0x11CA)
    }

    // TEST LLDT INSTRUCTION

    @Test fun lldtTestm16() {
        val instruction = "lldt word [EAX+0xFF]"
        store(startAddress + 0xF0FF, 0x1A2A, Datatype.DWORD)
        gprRegisters(eax = 0xF000)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMMURegisters(gdtrBase = 0, gdtrLimit = 0x20, ldtr = 0x1A2A)
    }

    @Test fun lldtTestr16() {
        val instruction = "lldt AX"
        gprRegisters(eax = 0x1A2A)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMMURegisters(gdtrBase = 0, gdtrLimit = 0x20, ldtr = 0x1A2A)
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

    // TEST SLDT INSTRUCTION

    @Test fun sldtTestm16() {
        val instruction = "sldt word [EAX+0xFF]"
        gprRegisters(eax = 0xF000)
        mmuRegisters(gdtrLimit = 0x20, ldtr = 0x1234)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FF, 0x1234, Datatype.WORD)
        assertMMURegisters(gdtrBase = 0, gdtrLimit = 0x20, ldtr = 0x1234)
    }

    @Test fun sldtTestr16() {
        val instruction = "sldt AX"
        gprRegisters(eax = 0x1A2A)
        mmuRegisters(gdtrLimit = 0x20, ldtr = 0x1234)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x1234)
        assertMMURegisters(gdtrBase = 0, gdtrLimit = 0x20, ldtr = 0x1234)
    }

    // TEST CLD INSTRUCTION

    @Test fun cldTest() {
        val instruction = "cld "
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters()
    }

    // TEST STD INSTRUCTION

    @Test fun stdTest() {
        val instruction = "std "
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters(df = true)
    }

    // TEST IN INSTRUCTION

    @Test fun inTest8imm8() {
        val instruction = "in al, 0x6A"
        store(0x6A, 0xAC, Datatype.BYTE, true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xAC, esp = 0x1000)
    }

    @Test fun inTest16imm8() {
        val instruction = "in ax, 0x6A"
        store(0x6A, 0xACCA, Datatype.WORD, true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xACCA, esp = 0x1000)
    }

    @Test fun inTest32imm8() {
        val instruction = "in eax, 0xF0"
        store(0xF0, 0x1234_ACCA, Datatype.DWORD, true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x1234_ACCA, esp = 0x1000)
    }

    @Test fun inTest8DX() {
        val instruction = "in al, dx"
        gprRegisters(edx = 0xF0FF)
        store(0xF0FF, 0xAC, Datatype.BYTE, true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xAC, edx = 0xF0FF)
    }

    @Test fun inTest16DX() {
        val instruction = "in ax, dx"
        gprRegisters(edx = 0xF0FF)
        store(0xF0FF, 0xBAAC, Datatype.WORD, true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xBAAC, edx = 0xF0FF)
    }

    @Test fun inTest32DX() {
        val instruction = "in eax, dx"
        gprRegisters(edx = 0xF0FF)
        store(0xF0FF, 0xFACA_BAAC, Datatype.DWORD, true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFACA_BAAC, edx = 0xF0FF)
    }

    // TEST OUT INSTRUCTION

    @Test fun outTest8imm8() {
        val instruction = "out 0x6A, al"
        gprRegisters(eax = 0xAC)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(0x6A, 0xAC, Datatype.BYTE, true)
    }

    @Test fun outTest16imm8() {
        val instruction = "out 0x6A, ax"
        gprRegisters(eax = 0xACCA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(0x6A, 0xACCA, Datatype.WORD, true)
    }

    @Test fun outTest32imm8() {
        val instruction = "out 0x6A, eax"
        gprRegisters(eax = 0xACCA_FACA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(0x6A, 0xACCA_FACA, Datatype.DWORD, true)
    }

    @Test fun outTest8DX() {
        val instruction = "out dx, al"
        gprRegisters(edx = 0xF0FF, eax = 0xAC)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(0xF0FF, 0xAC, Datatype.BYTE, true)
    }

    @Test fun outTest16DX() {
        val instruction = "out dx, ax"
        gprRegisters(edx = 0xF0FF, eax = 0xACDA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(0xF0FF, 0xACDA, Datatype.WORD, true)
    }

    @Test fun outTest32DX() {
        val instruction = "out dx, eax"
        gprRegisters(edx = 0xF0FF, eax = 0x1122_ACDA)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(0xF0FF, 0x1122_ACDA, Datatype.DWORD, true)
    }

    // TEST CPUID INSTRUCTION

    // Instruction not implemented
    @Test fun cpuidTest1() {
        val instruction = "cpuid "
        gprRegisters(eax = 1, ebx = 0xF_ACAC, ecx = 0xDA61_45CA0)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x4F4)
    }

    @Test fun cpuidTest0() {
        val instruction = "cpuid "
        gprRegisters(ebx = 0xF_ACAC, ecx = 0xDA61_45CA0)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x1, ebx = 0x756e6547, edx = 0x49656e69, ecx = 0x6c65746e)
    }

    // Instruction not implemented
    @Test fun cpuidTestGeneral() {
        val instruction = "cpuid "
        gprRegisters(eax = 0x8, ebx = 0xF_ACAC, ecx = 0xDA61_45CA0)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertTrue { x86.cpu.exception is GeneralException }
    }

    // TEST INSW INSTRUCTION

    @Test fun inswTestInc() {
        val instruction = "insw "
        val insnString = "insw word [di], dx"
        gprRegisters(edx = 0xF0FF, edi = 0xBABA)
        segmentRegisters(es = 0x8)
        store(0xF0FF, 0xFACA, Datatype.WORD, true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertMemory(startAddress + 0xBABA, 0xFACA, Datatype.WORD)
        assertGPRRegisters(edx = 0xF0FF, edi = 0xBABC)
    }

    @Test fun inswTestDec() {
        val instruction = "insw "
        val insnString = "insw word [di], dx"
        gprRegisters(edx = 0xF0FF, edi = 0xBABA)
        flagRegisters(df = true)
        segmentRegisters(es = 0x8)
        store(0xF0FF, 0xFACA, Datatype.WORD, true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertMemory(startAddress + 0xBABA, 0xFACA, Datatype.WORD)
        assertGPRRegisters(edx = 0xF0FF, edi = 0xBAB8)
    }

    // TEST LODS INSTRUCTION

    @Test fun lodsTest8Inc() {
        val instruction = "lodsb"
        val insnString = "lods al, byte [si]"
        store(startAddress + 0xBA22, 0xBB, Datatype.BYTE)
        gprRegisters(esi = 0xBA22)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBB, esi = 0xBA23)
    }

    @Test fun lodsTest8Dec() {
        val instruction = "lodsb"
        val insnString = "lods al, byte [si]"
        store(startAddress + 0xBA22, 0xBB, Datatype.BYTE)
        flagRegisters(df = true)
        gprRegisters(esi = 0xBA22)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBB, esi = 0xBA21)
    }

    @Test fun lodsTest16Inc() {
        val instruction = "lodsw"
        val insnString = "lods ax, word [si]"
        store(startAddress + 0xBA22, 0xBB01, Datatype.WORD)
        gprRegisters(esi = 0xBA22)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBB01, esi = 0xBA24)
    }

    @Test fun lodsTest16Dec() {
        val instruction = "lodsw"
        val insnString = "lods ax, word [si]"
        store(startAddress + 0xBA22, 0xBB01, Datatype.WORD)
        flagRegisters(df = true)
        gprRegisters(esi = 0xBA22)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBB01, esi = 0xBA20)
    }

    @Test fun lodsTest32Inc() {
        val instruction = "lodsd"
        val insnString = "lods eax, dword [si]"
        store(startAddress + 0xBA22, 0x12AB_BB01, Datatype.DWORD)
        gprRegisters(esi = 0xBA22)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0x12AB_BB01, esi = 0xBA26)
    }

    @Test fun lodsTest32Dec() {
        val instruction = "lodsd"
        val insnString = "lods eax, dword [si]"
        store(startAddress + 0xBA22, 0x12AB_BB01, Datatype.DWORD)
        flagRegisters(df = true)
        gprRegisters(esi = 0xBA22)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0x12AB_BB01, esi = 0xBA1E)
    }

    // TEST MOVS INSTRUCTION

    @Test fun movsTest8Inc() {
        val instruction = "movsb"
        val insnString = "movs byte es:[di], byte [si]"
        store(startAddress + 0xBA22, 0xBB, Datatype.BYTE)
        gprRegisters(esi = 0xBA22, edi = 0xFACC)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(esi = 0xBA23, edi = 0xFACD)
        assertMemory(startAddress + 0xFACC, 0xBB, Datatype.BYTE)
    }

    @Test fun movsTest8Dec() {
        val instruction = "movsb"
        val insnString = "movs byte es:[di], byte [si]"
        store(startAddress + 0xBA22, 0xBB, Datatype.BYTE)
        flagRegisters(df = true)
        gprRegisters(esi = 0xBA22, edi = 0xFACC)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(esi = 0xBA21, edi = 0xFACB)
        assertMemory(startAddress + 0xFACC, 0xBB, Datatype.BYTE)
    }

    @Test fun movsTest16Inc() {
        val instruction = "movsw"
        val insnString = "movs word es:[di], word [si]"
        store(startAddress + 0xBA22, 0xB0CC, Datatype.WORD)
        gprRegisters(esi = 0xBA22, edi = 0xFACC)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(esi = 0xBA24, edi = 0xFACE)
        assertMemory(startAddress + 0xFACC, 0xB0CC, Datatype.WORD)
    }

    @Test fun movsTest16Dec() {
        val instruction = "movsw"
        val insnString = "movs word es:[di], word [si]"
        store(startAddress + 0xBA22, 0xB0CC, Datatype.WORD)
        flagRegisters(df = true)
        gprRegisters(esi = 0xBA22, edi = 0xFACC)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(esi = 0xBA20, edi = 0xFACA)
        assertMemory(startAddress + 0xFACC, 0xB0CC, Datatype.WORD)
    }

    @Test fun movsTest32Inc() {
        val instruction = "movsd"
        val insnString = "movs dword es:[di], dword [si]"
        store(startAddress + 0xBA22, 0x1234_B0CC, Datatype.DWORD)
        gprRegisters(esi = 0xBA22, edi = 0xFACC)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(esi = 0xBA26, edi = 0xFAD0)
        assertMemory(startAddress + 0xFACC, 0x1234_B0CC, Datatype.DWORD)
    }

    @Test fun movsTest32Dec() {
        val instruction = "movsd"
        val insnString = "movs dword es:[di], dword [si]"
        store(startAddress + 0xBA22, 0x1234_B0CC, Datatype.DWORD)
        flagRegisters(df = true)
        gprRegisters(esi = 0xBA22, edi = 0xFACC)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(esi = 0xBA1E, edi = 0xFAC8)
        assertMemory(startAddress + 0xFACC, 0x1234_B0CC, Datatype.DWORD)
    }

    // TEST STOS INSTRUCTION

    @Test fun stosTest8Inc() {
        val instruction = "stosb"
        val insnString = "stos byte es:[di], al"
        gprRegisters(eax = 0xBB, edi = 0xBA22)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBB, edi = 0xBA23)
        assertMemory(startAddress + 0xBA22, 0xBB, Datatype.BYTE)
    }

    @Test fun stosTest8Dec() {
        val instruction = "stosb"
        val insnString = "stos byte es:[di], al"
        gprRegisters(eax = 0xBB, edi = 0xBA22)
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBB, edi = 0xBA21)
        assertMemory(startAddress + 0xBA22, 0xBB, Datatype.BYTE)
    }

    @Test fun stosTest16Inc() {
        val instruction = "stosw"
        val insnString = "stos word es:[di], ax"
        gprRegisters(eax = 0xAABB, edi = 0xBA22)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xAABB, edi = 0xBA24)
        assertMemory(startAddress + 0xBA22, 0xAABB, Datatype.WORD)
    }

    @Test fun stosTest16Dec() {
        val instruction = "stosw"
        val insnString = "stos word es:[di], ax"
        gprRegisters(eax = 0xAABB, edi = 0xBA22)
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xAABB, edi = 0xBA20)
        assertMemory(startAddress + 0xBA22, 0xAABB, Datatype.WORD)
    }

    @Test fun stosTest32Inc() {
        val instruction = "stosd"
        val insnString = "stos dword es:[di], eax"
        gprRegisters(eax = 0xFACA_AABB, edi = 0xBA22)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xFACA_AABB, edi = 0xBA26)
        assertMemory(startAddress + 0xBA22, 0xFACA_AABB, Datatype.DWORD)
    }

    @Test fun stosTest32Dec() {
        val instruction = "stosd"
        val insnString = "stos dword es:[di], eax"
        gprRegisters(eax = 0xFACA_AABB, edi = 0xBA22)
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xFACA_AABB, edi = 0xBA1E)
        assertMemory(startAddress + 0xBA22, 0xFACA_AABB, Datatype.DWORD)
    }

    // TEST CMPS INSTRUCTION

    @Test fun cmpsTest8Inc() {
        val instruction = "cmpsb"
        val insnString = "cmps byte es:[di], byte [si]"
        gprRegisters(esi = 0xBA22, edi = 0xFACC)
        store(startAddress + 0xBA22, 0xA, Datatype.BYTE)
        store(startAddress + 0xFACC, 0xA, Datatype.BYTE)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(zf = true, pf = true)
        assertGPRRegisters(esi = 0xBA23, edi = 0xFACD)
        assertMemory(startAddress + 0xBA22, 0xA, Datatype.BYTE)
        assertMemory(startAddress + 0xFACC, 0xA, Datatype.BYTE)
    }

    @Test fun cmpsTest8Dec() {
        val instruction = "cmpsb"
        val insnString = "cmps byte es:[di], byte [si]"
        gprRegisters(esi = 0xBA22, edi = 0xFACC)
        flagRegisters(df = true)
        store(startAddress + 0xBA22, 0xA, Datatype.BYTE)
        store(startAddress + 0xFACC, 0xA, Datatype.BYTE)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(zf = true, pf = true, df = true)
        assertGPRRegisters(esi = 0xBA21, edi = 0xFACB)
        assertMemory(startAddress + 0xBA22, 0xA, Datatype.BYTE)
        assertMemory(startAddress + 0xFACC, 0xA, Datatype.BYTE)
    }

    @Test fun cmpsTest16Inc() {
        val instruction = "cmpsw"
        val insnString = "cmps word es:[di], word [si]"
        gprRegisters(esi = 0xBA22, edi = 0xFACC)
        store(startAddress + 0xBA22, 0x100, Datatype.WORD)
        store(startAddress + 0xFACC, 0xA0, Datatype.WORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(sf = true, pf = true, cf = true)
        assertGPRRegisters(esi = 0xBA24, edi = 0xFACE)
        assertMemory(startAddress + 0xBA22, 0x100, Datatype.WORD)
        assertMemory(startAddress + 0xFACC, 0xA0, Datatype.WORD)
    }

    @Test fun cmpsTest16Dec() {
        val instruction = "cmpsw"
        val insnString = "cmps word es:[di], word [si]"
        gprRegisters(esi = 0xBA22, edi = 0xFACC)
        store(startAddress + 0xBA22, 0x100, Datatype.WORD)
        store(startAddress + 0xFACC, 0xA0, Datatype.WORD)
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(sf = true, pf = true, cf = true, df = true)
        assertGPRRegisters(esi = 0xBA20, edi = 0xFACA)
        assertMemory(startAddress + 0xBA22, 0x100, Datatype.WORD)
        assertMemory(startAddress + 0xFACC, 0xA0, Datatype.WORD)
    }

    @Test fun cmpsTest32Inc() {
        val instruction = "cmpsd"
        val insnString = "cmps dword es:[di], dword [si]"
        gprRegisters(esi = 0xBA22, edi = 0xFACC)
        store(startAddress + 0xBA22, 0x100, Datatype.DWORD)
        store(startAddress + 0xFACC, 0x8000_0000, Datatype.DWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(of = true, pf = true)
        assertGPRRegisters(esi = 0xBA26, edi = 0xFAD0)
        assertMemory(startAddress + 0xBA22, 0x100, Datatype.DWORD)
        assertMemory(startAddress + 0xFACC, 0x8000_0000, Datatype.DWORD)
    }

    @Test fun cmpsTest32Dec() {
        val instruction = "cmpsd"
        val insnString = "cmps dword es:[di], dword [si]"
        gprRegisters(esi = 0xBA22, edi = 0xFACC)
        store(startAddress + 0xBA22, 0x100, Datatype.DWORD)
        store(startAddress + 0xFACC, 0x8000_0000, Datatype.DWORD)
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(of = true, pf = true, df = true)
        assertGPRRegisters(esi = 0xBA1E, edi = 0xFAC8)
        assertMemory(startAddress + 0xBA22, 0x100, Datatype.DWORD)
        assertMemory(startAddress + 0xFACC, 0x8000_0000, Datatype.DWORD)
    }

    // TEST SCAS INSTRUCTION

    @Test fun scassTest8Inc() {
        val instruction = "scasb"
        val insnString = "scas al, byte es:[di]"
        gprRegisters(edi = 0xFACC, eax = 0xA)
        store(startAddress + 0xFACC, 0xA, Datatype.BYTE)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(zf = true, pf = true)
        assertGPRRegisters(edi = 0xFACD, eax = 0xA)
        assertMemory(startAddress + 0xFACC, 0xA, Datatype.BYTE)
    }

    @Test fun scasTest8Dec() {
        val instruction = "scasb"
        val insnString = "scas al, byte es:[di]"
        gprRegisters(edi = 0xFACC, eax = 0xA)
        flagRegisters(df = true)
        store(startAddress + 0xFACC, 0xA, Datatype.BYTE)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(zf = true, pf = true, df = true)
        assertGPRRegisters(edi = 0xFACB, eax = 0xA)
        assertMemory(startAddress + 0xFACC, 0xA, Datatype.BYTE)
    }

    @Test fun scasTest16Inc() {
        val instruction = "scasw"
        val insnString = "scas ax, word es:[di]"
        gprRegisters(edi = 0xFACC, eax = 0xA0)
        store(startAddress + 0xFACC, 0x100, Datatype.WORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(sf = true, pf = true, cf = true)
        assertGPRRegisters(edi = 0xFACE, eax = 0xA0)
        assertMemory(startAddress + 0xFACC, 0x100, Datatype.WORD)
    }

    @Test fun scasTest16Dec() {
        val instruction = "scasw"
        val insnString = "scas ax, word es:[di]"
        gprRegisters(edi = 0xFACC, eax = 0xA0)
        store(startAddress + 0xFACC, 0x100, Datatype.WORD)
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(sf = true, pf = true, cf = true, df = true)
        assertGPRRegisters(edi = 0xFACA, eax = 0xA0)
        assertMemory(startAddress + 0xFACC, 0x100, Datatype.WORD)
    }

    @Test fun scasTest32Inc() {
        val instruction = "scasd"
        val insnString = "scas eax, dword es:[di]"
        gprRegisters(edi = 0xFACC, eax = 0x8000_0000)
        store(startAddress + 0xFACC, 0x100, Datatype.DWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(of = true, pf = true)
        assertGPRRegisters(eax = 0x8000_0000, edi = 0xFAD0)
        assertMemory(startAddress + 0xFACC, 0x100, Datatype.DWORD)
    }

    @Test fun scasTest32Dec() {
        val instruction = "scasd"
        val insnString = "scas eax, dword es:[di]"
        gprRegisters(edi = 0xFACC, eax = 0x8000_0000)
        store(startAddress + 0xFACC, 0x100, Datatype.DWORD)
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(of = true, pf = true, df = true)
        assertGPRRegisters(eax = 0x8000_0000, edi = 0xFAC8)
        assertMemory(startAddress + 0xFACC, 0x100, Datatype.DWORD)
    }

    // TEST LOOP INSTRUCTION

    @Test fun loopTestJmp() {
        gprRegisters(ecx = 0x2)
        val instructionLoop = "loop 0x3"
        val insnStringLoop = "loop 0x0001" // hex 2 byte
        execute(1) { assemble(instructionLoop) }
        assertAssembly(insnStringLoop)
        assertGPRRegisters(ecx = 0x1)
    }

    @Test fun loopTestNoJmp() {
        gprRegisters(ecx = 0x1)
        val instructionLoop = "loop 0x3"
        val insnStringLoop = "loop 0x0001" // hex 2 byte
        execute { assemble(instructionLoop) }
        assertAssembly(insnStringLoop)
    }

    @Test fun loopnzTestPositive() {
        gprRegisters(ecx = 0x2)
        val instructionLoop = "loopnz 0x3"
        val insnStringLoop = "loopnz 0x0001"
        execute(1) { assemble(instructionLoop) }
        assertAssembly(insnStringLoop)
        assertGPRRegisters(ecx = 0x1)
    }

    @Test fun loopnzTestNegative() {
        gprRegisters(ecx = 0x2)
        flagRegisters(zf = true)
        val instructionLoop = "loopnz 0x00FF"
        val insnStringLoop = "loopnz 0x00FD"
        execute { assemble(instructionLoop) }
        assertAssembly(insnStringLoop)
        assertGPRRegisters(ecx = 0x1)
    }

    @Test fun loopzTestPositive() {
        gprRegisters(ecx = 0x2)
        flagRegisters(zf = true)
        val instructionLoop = "loopz 0x00FF"
        val insnStringLoop = "loopz 0x00FD"
        execute(253) { assemble(instructionLoop) }
        assertAssembly(insnStringLoop)
        assertGPRRegisters(ecx = 0x1)
    }

    @Test fun loopzTestNegative() {
        gprRegisters(ecx = 0x2)
        val instructionLoop = "loopz 0x00FF"
        val insnStringLoop = "loopz 0x00FD"
        execute { assemble(instructionLoop) }
        assertAssembly(insnStringLoop)
        assertGPRRegisters(ecx = 0x1)
    }

    // TEST IRET INSTRUCTION

    @Test fun iretTest1() {
        val instructionPushFlag = "push AX"
        gprRegisters(eax = 0x887)
        execute(-1) { assemble(instructionPushFlag) }
        assertAssembly(instructionPushFlag)  // flags cf, pf, sf, of
        assertGPRRegisters(eax = 0x887, esp = 0xFFFE)

        val instructionPushEipCs = "push EAX"
        gprRegisters(eax = 0x3, esp = 0xFFFE)
        execute { assemble(instructionPushEipCs) }
        assertAssembly(instructionPushEipCs)
        assertGPRRegisters(eax = 0x3, esp = 0xFFFA)

        val instruction = "iret "
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters(pf = true, sf = true, of = true, cf = true)
    }

    @Test fun iretTest2() {
        val instructionPushFlag = "push AX"
        gprRegisters(eax = 0x887)
        execute(-1) { assemble(instructionPushFlag) }
        assertAssembly(instructionPushFlag)  // flags cf, pf, sf, of
        assertGPRRegisters(eax = 0x887, esp = 0xFFFE)

        val instructionPushEipCs = "push EAX"
        gprRegisters(eax = 0x5_0003, esp = 0xFFFE)
        execute { assemble(instructionPushEipCs) }
        assertAssembly(instructionPushEipCs)
        assertGPRRegisters(eax = 0x5_0003, esp = 0xFFFA)

        val instruction = "iret "
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters(pf = true, sf = true, of = true, cf = true)
    }
}
