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
package ru.inforion.lab403.kopycat.cores.x86.instructions

import org.junit.Test
import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.common.extensions.unaryMinus
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.config.Generation
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.modules.memory.RAM
import kotlin.test.assertTrue


class X86InstructionsTest16: AX86InstructionTest() {
    override val x86 = x86Core(this, "x86Core", 400.MHz, Generation.Am5x86, 1.0)
    override val ram0 = RAM(this, "ram0", 0xFFF_FFFF)
    override val ram1 = RAM(this, "ram1", 0x1_0000)
    init {
        x86.ports.mem.connect(buses.mem)
        x86.ports.io.connect(buses.io)
        ram0.ports.mem.connect(buses.mem, 0u)
        ram1.ports.mem.connect(buses.io, 0u)
        initializeAndResetAsTopInstance()
    }

    override val mode = 16L

    override val bitMode: ByteArray
        get() = byteArrayOf(-1, -1, 0, 0, 1, -109, -113)

    // TEST ADD INSTRUCTION
    // ALL OPERANDS

    @Test fun addTestALi8() {
        val instruction = "add AL, 0x69"
        gprRegisters(eax = 0x25u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x8Eu)
    }

    @Test fun addTestAXi16() {
        val instruction = "add AX, 0x7ABA"
        gprRegisters(eax = 0x2222u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x9CDCu)
    }

    @Test fun addTestEAXi32() {
        val instruction = "add EAX, 0x58AA7ABA"
        gprRegisters(eax = 0xCAFE_BABAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x23A9_3574u)
    }

    @Test fun addTestr8i8() {
        val instruction = "add CL, 0x58"
        gprRegisters(ecx = 0xBAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x12u)
    }

    @Test fun addTestr16i16() {
        val instruction = "add CX, 0xBCDE"
        gprRegisters(ecx = 0x4323u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x1u)
    }

    @Test fun addTestr32i32() {
        val instruction = "add EDX, 0xABCDEF12"
        gprRegisters(edx = 0xDEAD_BABAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x8A7B_A9CCu)
    }

    @Test fun addTestr16i8() {
        val instruction = "add CX, 0xDE"
        gprRegisters(ecx = 0x4322u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4400u)
    }

    @Test fun addTestr32i8() {
        val instruction = "add EDX, 0x12"
        gprRegisters(edx = 0xDEAD_BABAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xDEAD_BACCu)
    }

    @Test fun addTestm8i8() {
        val instruction = "add BYTE [EAX+0xFF], 0xBA"
        store(startAddress + 0xF0FFu, 0xCAu, Datatype.BYTE)
        gprRegisters(eax = 0xF000u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FFu, 0x84u, Datatype.BYTE)
        assertGPRRegisters(eax = 0xF000u)
    }

    @Test fun addTestm16i16() {
        val instruction = "add WORD [EDX+0x5678], 0x4322"
        store(startAddress + 0xBE08u, 0xBACAu, Datatype.WORD)
        gprRegisters(edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xFDECu, Datatype.WORD)
        assertGPRRegisters(edx = 0x6790u)
    }

    @Test fun addTestm32i32() {
        val instruction = "add DWORD [EDX+0x5678], 0x12344322"
        store(startAddress + 0xBE08u, 0xFAAA_BACAu, Datatype.DWORD)
        gprRegisters(edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xCDE_FDECu, Datatype.DWORD)
        assertGPRRegisters(edx = 0x6790u)
    }

    @Test fun addTestm16i8() {
        val instruction = "add WORD [EDX+0x5678], 0x22"
        store(startAddress + 0xBE08u, 0xBACAu, Datatype.WORD)
        gprRegisters(edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xBAECu, Datatype.WORD)
        assertGPRRegisters(edx = 0x6790u)
    }

    @Test fun addTestm32i8() {
        val instruction = "add DWORD [EDX+0x5678], 0x22"
        store(startAddress + 0xBE08u, 0xFAAA_BACAu, Datatype.DWORD)
        gprRegisters(edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xFAAA_BAECu, Datatype.DWORD)
        assertGPRRegisters(edx = 0x6790u)
    }

    @Test fun addTestr8r8() {
        val instruction = "add CL, DH"
        gprRegisters(ecx = 0xBAu, edx = 0xFA00u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xB4u, edx = 0xFA00u)
    }

    @Test fun addTestr16r16() {
        val instruction = "add CX, DX"
        gprRegisters(ecx = 0x4322u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xAAB2u, edx = 0x6790u)
    }

    @Test fun addTestr32r32() {
        val instruction = "add EDX, EBX"
        gprRegisters(edx = 0xDEAD_BABAu, ebx = 0xABCD_EF12u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x8A7B_A9CCu, ebx = 0xABCD_EF12u)
    }

    @Test fun addTestm8r8() {
        val instruction = "add BYTE [EAX+0xFF], BL"
        store(startAddress + 0xF0FFu, 0xCAu, Datatype.BYTE)
        gprRegisters(ebx = 0xBAu, eax = 0xF000u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FFu, 0x84u, Datatype.BYTE)
        assertGPRRegisters(eax = 0xF000u, ebx = 0xBAu)
    }

    @Test fun addTestm16r16() {
        val instruction = "add WORD [EDX+0x5678], CX"
        store(startAddress + 0xBE08u, 0xBACAu, Datatype.WORD)
        gprRegisters(ecx = 0x4322u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xFDECu, Datatype.WORD)
        assertGPRRegisters(ecx = 0x4322u, edx = 0x6790u)
    }

    @Test fun addTestm32r32() {
        val instruction = "add DWORD [EDX+0x5678], ECX"
        store(startAddress + 0xBE08u, 0xFAAA_BACAu, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xCDE_FDECu, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
    }

    @Test fun addTestr8m8() {
        val instruction = "add BL, BYTE [EAX+0xFF]"
        store(startAddress + 0xF0FFu, 0xCAu, Datatype.BYTE)
        gprRegisters(ebx = 0xBAu, eax = 0xF000u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FFu, 0xCAu, Datatype.BYTE)
        assertGPRRegisters(eax = 0xF000u, ebx = 0x84u)
    }

    @Test fun addTestr16m16() {
        val instruction = "add CX, WORD [EDX+0x5678]"
        store(startAddress + 0xBE08u, 0xBACAu, Datatype.WORD)
        gprRegisters(ecx = 0x4322u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xBACAu, Datatype.WORD)
        assertGPRRegisters(ecx = 0xFDECu, edx = 0x6790u)
    }

    @Test fun addTestr32m32() {
        val instruction = "add ECX, DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08u, 0xFAAA_BACAu, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xFAAA_BACAu, Datatype.DWORD)
        assertGPRRegisters(ecx = 0xCDE_FDECu, edx = 0x6790u)
    }

    @Test fun addTestFlags1() {
        val instruction = "add EDX, EBX"
        gprRegisters(edx = 0xDEAD_BABAu, ebx = 0xABCD_EF12u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x8A7B_A9CCu, ebx = 0xABCD_EF12u)

        assertFlagRegisters(pf = true, sf = true, cf = true)
    }

    @Test fun addTestFlags2() {
        val instruction = "add EBX, DWORD [EAX+0xFF]"
        store(startAddress + 0xF0FFu, 0x57AE_129Du, Datatype.DWORD)
        gprRegisters(ebx = 0xA851_ED63u, eax = 0xF000u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FFu, 0x57AE_129Du, Datatype.DWORD)
        assertGPRRegisters(eax = 0xF000u, ebx = 0u)

        assertFlagRegisters(zf = true, pf = true, af = true, cf = true)
    }

    // TEST ADC INSTRUCTION
    // FOR ARITHMETICAL INSTRUCTIONS SAME DECODER

    @Test fun adcTestr16m16() {
        val instruction = "adc ECX, DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08u, 0xBACA_BACAu, Datatype.DWORD)
        gprRegisters(ecx = 0x8423_7322u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xBACA_BACAu, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x3EEE_2DECu, edx = 0x6790u)
        assertFlagRegisters(cf = true, of = true)
    }

    // TEST SBB INSTRUCTION
    // FOR ARITHMETICAL INSTRUCTIONS SAME DECODER

    @Test fun sbbTestr32r32() {
        val instruction = "sbb BL, BYTE [EAX+0xFF]"
        store(startAddress + 0xF0FFu, 0xCAu, Datatype.BYTE)
        gprRegisters(ebx = 0xBAu, eax = 0xF000u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FFu, 0xCAu, Datatype.BYTE)
        assertGPRRegisters(eax = 0xF000u, ebx = 0xF0u)
    }

    @Test fun sbbTestFlag() {
        val instruction = "sbb EAX, EDX"
        flagRegisters(cf = true)
        gprRegisters(edx = 0xFA_56BAu, eax = 0xD458_963Bu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xD35E_3F80u, edx = 0xFA_56BAu)
        assertFlagRegisters(sf = true)
    }

    // TEST SUB INSTRUCTION
    // FOR ARITHMETICAL INSTRUCTIONS SAME DECODER

    @Test fun subTestr32i8() {
        val instruction = "sub EDX, 0x12"
        gprRegisters(edx = 0xDEAD_BABAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xDEAD_BAA8u)
        assertFlagRegisters(sf = true)
    }

    // TEST AND INSTRUCTION
    // FOR ARITHMETICAL INSTRUCTIONS SAME DECODER

    @Test fun andTestAXi16() {
        val instruction = "and AX, 0xA5A5"
        gprRegisters(eax = 0x1723u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x521u)
        assertFlagRegisters(pf = true)
    }

    // TEST OR INSTRUCTION
    // FOR ARITHMETICAL INSTRUCTIONS SAME DECODER

    @Test fun orTestm16r16() {
        val instruction = "or WORD [EDX+0x5678], CX"
        store(startAddress + 0xBE08u, 0xBACAu, Datatype.WORD)
        gprRegisters(ecx = 0x4322u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xFBEAu, Datatype.WORD)
        assertGPRRegisters(ecx = 0x4322u, edx = 0x6790u)
        assertFlagRegisters(sf = true)
    }

    // TEST XOR INSTRUCTION
    // FOR ARITHMETICAL INSTRUCTIONS SAME DECODER

    @Test fun xorTestr32m32() {
        val instruction = "xor ECX, DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08u, 0xFAAA_BACAu, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xFAAA_BACAu, Datatype.DWORD)
        assertGPRRegisters(ecx = 0xE89E_F9E8u, edx = 0x6790u)
        assertFlagRegisters(sf = true, pf = true)
    }

    // TEST CMP INSTRUCTION
    // FOR ARITHMETICAL INSTRUCTIONS SAME DECODER

    @Test fun cmpTestr8imm8() {
        val instruction = "cmp BH, 0x0A"
        gprRegisters(ebx = 0xA00u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0xA00u)
        assertFlagRegisters(zf = true, pf = true)
    }

    @Test fun cmpTestFlags1() {
        val instruction = "cmp ECX, 0x0100"
        gprRegisters(ecx = 0xA0u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xA0u)
        assertFlagRegisters(sf = true, pf = true, cf = true)
    }

    @Test fun cmpTestFlags2() {
        val instruction = "cmp ECX, 0x0100"
        gprRegisters(ecx = 0x8000_0000u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x8000_0000u)
        assertFlagRegisters(of = true, pf = true)
    }

    // TEST AAA INSTRUCTION

    @Test fun aaaTest1() {
        val instruction = "aaa "
        gprRegisters(eax = 0x111Au)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x1200u)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun aaaTest2() {
        val instruction = "aaa "
        flagRegisters(af = true)
        gprRegisters(eax = 0x5514u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x560Au)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun aaaTest3() {
        val instruction = "aaa "
        flagRegisters(af = true)
        gprRegisters(eax = 0x551Au)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x5600u)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun aaaTest4() {
        val instruction = "aaa "
        gprRegisters(eax = 0x5514u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x5504u)
        assertFlagRegisters(af = false, cf = false)
    }

    // TEST AAS INSTRUCTION

    @Test fun aasTest1() {
        val instruction = "aas "
        gprRegisters(eax = 0x111Au)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x1004u)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun aasTest2() {
        val instruction = "aas "
        flagRegisters(af = true)
        gprRegisters(eax = 0x5514u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x540Eu)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun aasTest3() {
        val instruction = "aas "
        flagRegisters(af = true)
        gprRegisters(eax = 0x551Au)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x5404u)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun aasTest4() {
        val instruction = "aas "
        gprRegisters(eax = 0x5514u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x5504u)
        assertFlagRegisters(af = false, cf = false)
    }

    // TEST DAA INSTRUCTION

    @Test fun daaTest1() {
        val instruction = "daa "
        gprRegisters(eax = 0x111Au)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x1120u)
        assertFlagRegisters(af = true)
    }

    @Test fun daaTest2() {
        val instruction = "daa "
        flagRegisters(af = true)
        gprRegisters(eax = 0xFBu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x61u)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun daaTest3() {
        val instruction = "daa "
        flagRegisters(af = true, cf = true)
        gprRegisters(eax = 0x5511u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x5577u)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun daaTest4() {
        val instruction = "daa "
        gprRegisters(eax = 0x55A4u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x5504u)
        assertFlagRegisters(af = false, cf = true)
    }

    // TEST DAS INSTRUCTION

    @Test fun dasTest1() {
        val instruction = "das "
        gprRegisters(eax = 0x111Au)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x1114u)
        assertFlagRegisters(af = true)
    }

    @Test fun dasTest2() {
        val instruction = "das "
        flagRegisters(af = true)
        gprRegisters(eax = 0xFBu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x95u)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun dasTest3() {
        val instruction = "das "
        flagRegisters(af = true, cf = true)
        gprRegisters(eax = 0x5511u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x55ABu)
        assertFlagRegisters(af = true, cf = true)
    }

    @Test fun dasTest4() {
        val instruction = "das "
        gprRegisters(eax = 0x55A4u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x5544u)
        assertFlagRegisters(af = false, cf = true)
    }

    // TEST DEC INSTRUCTION

    @Test fun decTestm8() {
        val instruction = "dec DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08u, 0xCAu, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xC9u, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
    }

    @Test fun decTestm16() {
        val instruction = "dec DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08u, 0xCAC1u, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xCAC0u, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
    }

    @Test fun decTestm32() {
        val instruction = "dec DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08u, 0xFACC_CAC1u, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xFACC_CAC0u, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
    }

    @Test fun decTestr16() {
        val instruction = "dec CX"
        gprRegisters(ecx = 0x4322u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4321u)
    }

    @Test fun decTestr32() {
        val instruction = "dec ECX"
        gprRegisters(ecx = 0x8000_0000u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x7FFF_FFFFu)
        assertFlagRegisters(of = true, af = true, pf = true)
    }

    // TEST DIV INSTRUCTION

    @Test fun divTestr8() {
        val instruction = "div CL"
        gprRegisters(ecx = 0xBu, eax = 0x76u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xBu, eax = 0x080Au)
    }

    @Test fun divTestr16() {
        val instruction = "div CX"
        gprRegisters(eax = 0x253Bu, ecx = 0x8Eu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x8Eu, edx = 0x0011u, eax = 0x0043u)
    }

    @Test fun divTestr32() {
        val instruction = "div ECX"
        gprRegisters(edx = 0x33_5D25u, eax = 0x9380_A2F4u, ecx = 0x9E_1247u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x9E_1247u, edx = 0x82_1DC7u, eax = 0x532F_52EBu)
    }

    @Test fun divTestm8() {
        val instruction = "div BYTE [EDX+0x325F]"
        store(startAddress + 0x8452u, 0x0Bu, Datatype.BYTE)
        gprRegisters(edx = 0x51F3u, eax = 0x76u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x51F3u, eax = 0x080Au)
    }

    @Test fun divTestm16() {
        val instruction = "div WORD [EBX+0x325F]"
        store(startAddress + 0x8452u, 0x008Eu, Datatype.WORD)
        gprRegisters(eax = 0x253Bu, ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x0011u, eax = 0x0043u, ebx = 0x51F3u)
    }

    @Test fun divTestm32() {
        val instruction = "div DWORD [EBX+0x325F]"
        store(startAddress + 0x8452u, 0x9E_1247u, Datatype.DWORD)
        gprRegisters(edx = 0x33_5D25u, eax = 0x9380_A2F4u, ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x82_1DC7u, eax = 0x532F_52EBu, ebx = 0x51F3u)
    }

    @Test fun divTestr8Zero() {
        val instruction = "div CL"
        gprRegisters(ecx = 0u, eax = 0x5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertTrue { x86.cpu.exception is x86HardwareException.DivisionByZero }
    }

    @Test fun divTestr8Overflow() {
        val instruction = "div CL"
        gprRegisters(ecx = 0x10u, eax = 0x1000u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertTrue { x86.cpu.exception is x86HardwareException.Overflow }
    }

    @Test fun divTestr16Overflow() {
        val instruction = "div CX"
        gprRegisters(ecx = 0x10u, eax = 0u, edx = 0x10u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertTrue { x86.cpu.exception is x86HardwareException.Overflow }
    }

    @Test fun divTestr32Overflow() {
        val instruction = "div ECX"
        gprRegisters(ecx = 0x10u, eax = 0u, edx = 0x10u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertTrue { x86.cpu.exception is x86HardwareException.Overflow }
    }

    // TEST IDIV INSTRUCTION

    @Test fun idivTestr8() {
        val instruction = "idiv CL"
        gprRegisters(ecx = 0xBu, eax = 0xFF86u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xBu, eax = 0xFFF5u)
    }

    @Test fun idivTestr16() {
        val instruction = "idiv CX"
        gprRegisters(eax = 0x253Bu, ecx = 0x8Eu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x8Eu, edx = 0x0011u, eax = 0x0043u)
    }

    @Test fun idivTestr32() {
        val instruction = "idiv ECX"
        gprRegisters(edx = 0xFFCC_A2DAu, eax = 0x6D01_7AD3u, ecx = 0x9E_1247u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x9E_1247u, edx = 0u, eax = 0xACD0_AD15u)
    }

    @Test fun idivTestm8() {
        val instruction = "idiv BYTE [EDX+0x325F]"
        store(startAddress + 0x8452u, 0x0Bu, Datatype.BYTE)
        gprRegisters(edx = 0x51F3u, eax = 0xFF86u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x51F3u, eax = 0xFFF5u)
    }

    @Test fun idivTestm16() {
        val instruction = "idiv WORD [EBX+0x325F]"
        store(startAddress + 0x8452u, 0x008Eu, Datatype.WORD)
        gprRegisters(eax = 0x253Bu, ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x0011u, eax = 0x0043u, ebx = 0x51F3u)
    }

    @Test fun idivTestm32() {
        val instruction = "idiv DWORD [EBX+0x325F]"
        store(startAddress + 0x8452u, 0x9E_1247u, Datatype.DWORD)
        gprRegisters(edx = 0xFFCC_A2DAu, eax = 0x6D01_7AD3u, ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0u, eax = 0xACD0_AD15u, ebx = 0x51F3u)
    }

    @Test fun idivTestr8Zero() {
        val instruction = "idiv CL"
        gprRegisters(ecx = 0u, eax = 0x5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertTrue { x86.cpu.exception is x86HardwareException.DivisionByZero }
    }

    // TEST IMUL INSTRUCTION

    @Test fun imulTestr8() {
        val instruction = "imul BL"
        gprRegisters(eax = 0x16u, ebx = 0x3Au)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0x3Au, eax = 0x04FCu)
    }

    @Test fun imulTestr16() {
        val instruction = "imul BX"
        gprRegisters(eax = 0x253Bu, ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x0BEBu, eax = 0x0201u, ebx = 0x51F3u)
    }

    @Test fun imulTestr32() {
        val instruction = "imul EBX"
        gprRegisters(ebx = 0x4FCC_A2DAu, eax = 0x6D01_7AD3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x21FA_976Cu, eax = 0xC020_1DAEu, ebx = 0x4FCC_A2DAu)
    }

    @Test fun imulTestm8() {
        val instruction = "imul BYTE [EDX+0x325F]"
        store(startAddress + 0x8452u, 0x3Au, Datatype.BYTE)
        gprRegisters(edx = 0x51F3u, eax = 0x16u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x04FCu, edx = 0x51F3u)
    }

    @Test fun imulTestm16() {
        val instruction = "imul WORD [EDX+0x325F]"
        store(startAddress + 0x8452u, 0x51F3u, Datatype.WORD)
        gprRegisters(eax = 0x253Bu, edx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x0BEBu, eax = 0x0201u)
    }

    @Test fun imulTestm32() {
        val instruction = "imul DWORD [EDX+0x325F]"
        store(startAddress + 0x8452u, 0x4FCC_A2DAu, Datatype.DWORD)
        gprRegisters(eax = 0x6D01_7AD3u, edx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x21FA_976Cu, eax = 0xC020_1DAEu)
    }

    @Test fun imulTestr16m16() {
        val instruction = "imul AX, WORD [EDX+0x325F]"
        store(startAddress + 0x8452u, 0x51F3u, Datatype.WORD)
        gprRegisters(eax = 0x253Bu, edx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x51F3u, eax = 0x0201u)
    }

    @Test fun imulTestr32m32() {
        val instruction = "imul EAX, DWORD [EDX+0x325F]"
        store(startAddress + 0x8452u, 0x4FCC_A2DAu, Datatype.DWORD)
        gprRegisters(eax = 0x6D01_7AD3u, edx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x51F3u, eax = 0xC020_1DAEu)
    }

    @Test fun imulTestr32r32imm32() {
        val instruction = "imul EAX, ECX, 0x0062FA1C"
        gprRegisters(ecx = 0x6D01_7AD3u, eax = 0x1u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x6D01_7AD3u, eax = 0x62E1_7D14u)
    }

    // TEST INC INSTRUCTION

    @Test fun incTestm8() {
        val instruction = "inc DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08u, 0xCAu, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xCBu, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
    }

    @Test fun incTestm16() {
        val instruction = "inc DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08u, 0xCAC1u, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xCAC2u, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
    }

    @Test fun incTestm32() {
        val instruction = "inc DWORD [EDX+0x5678]"
        store(startAddress + 0xBE08u, 0xFACC_CAC1u, Datatype.DWORD)
        gprRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xFACC_CAC2u, Datatype.DWORD)
        assertGPRRegisters(ecx = 0x1234_4322u, edx = 0x6790u)
    }

    @Test fun incTestr16() {
        val instruction = "inc CX"
        gprRegisters(ecx = 0x4322u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4323u)
    }

    @Test fun incTestr32() {
        val instruction = "inc ECX"
        gprRegisters(ecx = 0x4322_FA11u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4322_FA12u)
    }

    @Test fun incTestFlag1() {
        val instruction = "inc ECX"
        gprRegisters(ecx = 0xFFFF_FFFFu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0u)
        assertFlagRegisters(zf = true, af = true, pf = true)
    }

    @Test fun incTestFlag2() {
        val instruction = "inc ECX"
        gprRegisters(ecx = 0x7FFF_FFFFu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x8000_0000u)
        assertFlagRegisters(of = true, af = true, pf = true, sf = true)
    }

    // TEST MUL INSTRUCTION

    @Test fun mulTestr8() {
        val instruction = "mul CL"
        gprRegisters(ecx = 0xBu, eax = 0x76u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xBu, eax = 0x0512u)
    }

    @Test fun mulTestr16() {
        val instruction = "mul CX"
        gprRegisters(eax = 0x253Bu, ecx = 0x8Eu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x8Eu, edx = 0x0014u, eax = 0xA6BAu)
    }

    @Test fun mulTestr32() {
        val instruction = "mul ECX"
        gprRegisters(edx = 0x33_5D25u, eax = 0x9380_A2F4u, ecx = 0x9E_1247u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x9E_1247u, edx = 0x5B_13ECu, eax = 0x86BA_59ACu)
    }

    @Test fun mulTestm8() {
        val instruction = "mul BYTE [EDX+0x325F]"
        store(startAddress + 0x8452u, 0x0Bu, Datatype.BYTE)
        gprRegisters(edx = 0x51F3u, eax = 0x76u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x51F3u, eax = 0x0512u)
        assertFlagRegisters(of = true, cf = true)
    }

    @Test fun mulTestFlags() {
        val instruction = "mul BYTE [EDX+0x325F]"
        store(startAddress + 0x8452u, 0xBu, Datatype.BYTE)
        gprRegisters(edx = 0x51F3u, eax = 0x6u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x51F3u, eax = 0x42u)
        assertFlagRegisters()
    }

    @Test fun mulTestm16() {
        val instruction = "mul WORD [EBX+0x325F]"
        store(startAddress + 0x8452u, 0x008Eu, Datatype.WORD)
        gprRegisters(eax = 0x253Bu, ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x0014u, eax = 0xA6BAu, ebx = 0x51F3u)
    }

    @Test fun mulTestm32() {
        val instruction = "mul DWORD [EBX+0x325F]"
        store(startAddress + 0x8452u, 0x9E_1247u, Datatype.DWORD)
        gprRegisters(edx = 0x33_5D25u, eax = 0x9380_A2F4u, ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x5B_13ECu, eax = 0x86BA_59ACu, ebx = 0x51F3u)
        assertFlagRegisters(of = true, cf = true)
    }

    // TEST NEG INSTRUCTION

    @Test fun negTestr8() {
        val instruction = "neg CL"
        gprRegisters(ecx = 0xBu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xF5u)
        assertFlagRegisters(cf = true)
    }

    @Test fun negTestr16() {
        val instruction = "neg CX"
        gprRegisters(ecx = 0x8E57u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x71A9u)
        assertFlagRegisters(cf = true)
    }

    @Test fun negTestr32() {
        val instruction = "neg ECX"
        gprRegisters(ecx = 0x9380_A2F4u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x6C7F_5D0Cu)
        assertFlagRegisters(cf = true)
    }

    @Test fun negTestm8() {
        val instruction = "neg BYTE [EDX+0x325F]"
        store(startAddress + 0x8452u, 0x0Bu, Datatype.BYTE)
        gprRegisters(edx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452u, 0xF5u, Datatype.BYTE)
        assertFlagRegisters(cf = true)
    }

    @Test fun negTestm16() {
        val instruction = "neg WORD [EBX+0x325F]"
        store(startAddress + 0x8452u, 0x008Eu, Datatype.WORD)
        gprRegisters(ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452u, 0xFF72u, Datatype.WORD)
        assertFlagRegisters(cf = true)
    }

    @Test fun negTestm32() {
        val instruction = "neg DWORD [EBX+0x325F]"
        store(startAddress + 0x8452u, 0x9E_1247u, Datatype.DWORD)
        gprRegisters(ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452u, 0xFF61_EDB9u, Datatype.DWORD)
        assertFlagRegisters(cf = true)
    }

    // TEST NOT INSTRUCTION

    @Test fun notTestr8() {
        val instruction = "not CL"
        gprRegisters(ecx = 0xBu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xF4u)
    }

    @Test fun notTestr16() {
        val instruction = "not CX"
        gprRegisters(ecx = 0x8E57u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x71A8u)
    }

    @Test fun notTestr32() {
        val instruction = "not ECX"
        gprRegisters(ecx = 0x9380_A2F4u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x6C7F_5D0Bu)
    }

    @Test fun notTestm8() {
        val instruction = "not BYTE [EDX+0x325F]"
        store(startAddress + 0x8452u, 0x0Bu, Datatype.BYTE)
        gprRegisters(edx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452u, 0xF4u, Datatype.BYTE)
    }

    @Test fun notTestm16() {
        val instruction = "not WORD [EBX+0x325F]"
        store(startAddress + 0x8452u, 0x008Eu, Datatype.WORD)
        gprRegisters(ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452u, 0xFF71u, Datatype.WORD)
    }

    @Test fun notTestm32() {
        val instruction = "not DWORD [EBX+0x325F]"
        store(startAddress + 0x8452u, 0x9E_1247u, Datatype.DWORD)
        gprRegisters(ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452u, 0xFF61_EDB8u, Datatype.DWORD)
    }

    // TEST BSF INSTRUCTION

    @Test fun bsfTestr16r16() {
        val instruction = "bsf CX, BX"
        gprRegisters(ecx = 0x8E57u, ebx = 0x800u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0x800u, ecx = 0xBu)
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
        store(startAddress + 0x8452u, 0x8000u, Datatype.WORD)
        gprRegisters(ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452u, 0x8000u, Datatype.WORD)
        assertGPRRegisters(ebx = 0x51F3u, ecx = 0xFu)
    }

    @Test fun bsfTestr32m32() {
        val instruction = "bsf ECX, DWORD [EBX+0x325F]"
        store(startAddress + 0x8452u, 0x40_1240u, Datatype.DWORD)
        gprRegisters(ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452u, 0x40_1240u, Datatype.DWORD)
        assertGPRRegisters(ebx = 0x51F3u, ecx = 0x6u)
    }

    // TEST BSR INSTRUCTION

    @Test fun bsrTestr16r16() {
        val instruction = "bsr CX, BX"
        gprRegisters(ecx = 0x8E57u, ebx = 0x800u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0x800u, ecx = 0xBu)
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
        store(startAddress + 0x8452u, 0x8000u, Datatype.WORD)
        gprRegisters(ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452u, 0x8000u, Datatype.WORD)
        assertGPRRegisters(ebx = 0x51F3u, ecx = 0xFu)
    }

    @Test fun bsrTestr32m32() {
        val instruction = "bsr ECX, DWORD [EBX+0x325F]"
        store(startAddress + 0x8452u, 0x40_1240u, Datatype.DWORD)
        gprRegisters(ebx = 0x51F3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0x8452u, 0x40_1240u, Datatype.DWORD)
        assertGPRRegisters(ebx = 0x51F3u, ecx = 0x16u)
    }

    // TEST BT INSTRUCTION

    @Test fun btTestr16r16() {
        val instruction = "bt CX, BX"
        gprRegisters(ecx = 0x8E57u, ebx = 0xFu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0xFu, ecx = 0x8E57u)
        assertFlagRegisters(cf = true)
    }

    @Test fun btTestr32r32() {
        val instruction = "bt ECX, EBX"
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters()
    }

    @Test fun btTestr16i8() {
        val instruction = "bt CX, 0x0C"
        gprRegisters(ecx = 0x124Fu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x124Fu)
        assertFlagRegisters(cf = true)
    }

    @Test fun btTestr32i8() {
        val instruction = "bt EBX, 0x18"
        gprRegisters(ebx = 0x2008_DAB2u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0x2008_DAB2u)
        assertFlagRegisters()
    }

    // TEST BTR INSTRUCTION

    @Test fun btrTestr16r16() {
        val instruction = "btr CX, BX"
        gprRegisters(ecx = 0x8E57u, ebx = 0xFu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0xFu, ecx = 0xE57u)
        assertFlagRegisters(cf = true)
    }

    @Test fun btrTestr32r32() {
        val instruction = "btr ECX, EBX"
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters()
    }

    @Test fun btrTestr16i8() {
        val instruction = "btr CX, 0x0C"
        gprRegisters(ecx = 0x124Fu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x24Fu)
        assertFlagRegisters(cf = true)
    }

    @Test fun btrTestr32i8() {
        val instruction = "btr EBX, 0x18"
        gprRegisters(ebx = 0x2008_DAB2u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0x2008_DAB2u)
        assertFlagRegisters()
    }

    // TEST BTS INSTRUCTION

    @Test fun btsTestr16r16() {
        val instruction = "bts CX, BX"
        gprRegisters(ecx = 0x8E57u, ebx = 0xFu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0xFu, ecx = 0x8E57u)
        assertFlagRegisters(cf = true)
    }

    @Test fun btsTestr32r32() {
        val instruction = "bts ECX, EBX"
        gprRegisters(ecx = 0x2u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x3u)
        assertFlagRegisters()
    }

    @Test fun btsTestr16i8() {
        val instruction = "bts CX, 0x0C"
        gprRegisters(ecx = 0x124Fu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x124Fu)
        assertFlagRegisters(cf = true)
    }

    @Test fun btsTestr32i8() {
        val instruction = "bts EBX, 0x18"
        gprRegisters(ebx = 0x2008_DAB2u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ebx = 0x2108_DAB2u)
        assertFlagRegisters()
    }

    // TEST TEST INSTRUCTION

    @Test fun testTestALi8() {
        val instruction = "test AL, 0x69"
        gprRegisters(eax = 0x25u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x25u)
        assertFlagRegisters(sf = false, zf = false, pf = true, cf = false, of = false)
    }

    @Test fun testTestAXi16() {
        val instruction = "test AX, 0x7ABA"
        gprRegisters(eax = 0x2222u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x2222u)
        assertFlagRegisters(sf = false, zf = false, pf = true, cf = false, of = false)
    }

    @Test fun testTestEAXi32() {
        val instruction = "test EAX, 0xD8AA7ABA"
        gprRegisters(eax = 0xCAFE_BABAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xCAFE_BABAu)
        assertFlagRegisters(sf = true, zf = false, pf = false, cf = false, of = false)
    }

    @Test fun testTestr8i8() {
        val instruction = "test CL, 0xA7"
        gprRegisters(ecx = 0x58u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x58u)
        assertFlagRegisters(sf = false, zf = true, pf = true, cf = false, of = false)
    }

    @Test fun testTestr16i16() {
        val instruction = "test CX, 0xBCDE"
        gprRegisters(ecx = 0x4323u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4323u)
        assertFlagRegisters(sf = false, zf = false, pf = false, cf = false, of = false)
    }

    @Test fun testTestr32i32() {
        val instruction = "test EDX, 0xABCDEF12"
        gprRegisters(edx = 0xDEAD_BABAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xDEAD_BABAu)
        assertFlagRegisters(sf = true, zf = false, pf = true, cf = false, of = false)
    }

    @Test fun testTestm8i8() {
        val instruction = "test BYTE [EAX+0xFF], 0xA7"
        store(startAddress + 0xF0FFu, 0x58u, Datatype.BYTE)
        gprRegisters(eax = 0xF000u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FFu, 0x58u, Datatype.BYTE)
        assertFlagRegisters(sf = false, zf = true, pf = true, cf = false, of = false)
    }

    @Test fun testTestm16i16() {
        val instruction = "test WORD [EDX+0x5678], 0xBCDE"
        store(startAddress + 0xBE08u, 0x4323u, Datatype.WORD)
        gprRegisters(edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0x4323u, Datatype.WORD)
        assertFlagRegisters(sf = false, zf = false, pf = false, cf = false, of = false)
    }

    @Test fun testTestm32i32() {
        val instruction = "test DWORD [EDX+0x5678], 0xABCDEF12"
        store(startAddress + 0xBE08u, 0xDEAD_BABAu, Datatype.DWORD)
        gprRegisters(edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xDEAD_BABAu, Datatype.DWORD)
        assertGPRRegisters(edx = 0x6790u)
        assertFlagRegisters(sf = true, zf = false, pf = true, cf = false, of = false)
    }

    @Test fun testTestr8r8() {
        val instruction = "test CL, DH"
        gprRegisters(ecx = 0x58u, edx = 0xA700u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x58u, edx = 0xA700u)
        assertFlagRegisters(sf = false, zf = true, pf = true, cf = false, of = false)
    }

    @Test fun testTestr16r16() {
        val instruction = "test CX, DX"
        gprRegisters(ecx = 0x4323u, edx = 0xBCDEu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4323u, edx = 0xBCDEu)
        assertFlagRegisters(sf = false, zf = false, pf = false, cf = false, of = false)
    }

    @Test fun testTestr32r32() {
        val instruction = "test EDX, EBX"
        gprRegisters(edx = 0xDEAD_BABAu, ebx = 0xABCD_EF12u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xDEAD_BABAu, ebx = 0xABCD_EF12u)
        assertFlagRegisters(sf = true, zf = false, pf = true, cf = false, of = false)
    }

    @Test fun testTestm8r8() {
        val instruction = "test BYTE [EAX+0xFF], DH"
        store(startAddress + 0xF0FFu, 0x58u, Datatype.BYTE)
        gprRegisters(edx = 0xA700u, eax = 0xF000u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FFu, 0x58u, Datatype.BYTE)
        assertGPRRegisters(edx = 0xA700u, eax = 0xF000u)
        assertFlagRegisters(sf = false, zf = true, pf = true, cf = false, of = false)
    }

    @Test fun testTestm16r16() {
        val instruction = "test WORD [EDX+0x5678], CX"
        store(startAddress + 0xBE08u, 0xBACAu, Datatype.WORD)
        gprRegisters(ecx = 0xFABAu, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xBACAu, Datatype.WORD)
        assertGPRRegisters(ecx = 0xFABAu, edx = 0x6790u)
        assertFlagRegisters(sf = true, zf = false, pf = false, cf = false, of = false)
    }

    @Test fun testTestm32r32() {
        val instruction = "test DWORD [EDX+0x5678], ECX"
        store(startAddress + 0xBE08u, 0xDEAD_BABAu, Datatype.DWORD)
        gprRegisters(ecx = 0xABCD_EF12u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xDEAD_BABAu, Datatype.DWORD)
        assertGPRRegisters(ecx = 0xABCD_EF12u, edx = 0x6790u)
        assertFlagRegisters(sf = true, zf = false, pf = true, cf = false, of = false)
    }

    @Test fun testTestr8m() {
        val instruction = "test BYTE [EAX+0xFF], DH"
        store(startAddress + 0xF0FFu, 0x58u, Datatype.BYTE)
        gprRegisters(edx = 0xA700u, eax = 0xF000u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FFu, 0x58u, Datatype.BYTE)
        assertGPRRegisters(edx = 0xA700u, eax = 0xF000u)
        assertFlagRegisters(sf = false, zf = true, pf = true, cf = false, of = false)
    }

    @Test fun testTestr16m16() {
        val instruction = "test WORD [EDX+0x5678], CX"
        store(startAddress + 0xBE08u, 0xBACAu, Datatype.WORD)
        gprRegisters(ecx = 0xFABAu, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xBACAu, Datatype.WORD)
        assertGPRRegisters(ecx = 0xFABAu, edx = 0x6790u)
        assertFlagRegisters(sf = true, zf = false, pf = false, cf = false, of = false)
    }

    @Test fun testTestr32m32() {
        val instruction = "test DWORD [EDX+0x5678], ECX"
        store(startAddress + 0xBE08u, 0xDEAD_BABAu, Datatype.DWORD)
        gprRegisters(ecx = 0xABCD_EF12u, edx = 0x6790u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBE08u, 0xDEAD_BABAu, Datatype.DWORD)
        assertGPRRegisters(ecx = 0xABCD_EF12u, edx = 0x6790u)
        assertFlagRegisters(sf = true, zf = false, pf = true, cf = false, of = false)
    }

    // TEST RCL INSTRUCTION

    @Test fun rclTestr81() {
        val instruction = "rcl DH, 0x01"
        gprRegisters(edx = 0x2500u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x4B00u)
        assertFlagRegisters(cf = false, of = false)
    }

    @Test fun rclTestr8CL() {
        val instruction = "rcl DH, CL"
        gprRegisters(edx = 0x2500u, ecx = 3u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x2C00u, ecx = 3u)
        assertFlagRegisters(cf = true)
    }

    @Test fun rclTestr8imm8() {
        val instruction = "rcl DH, 0x04"
        gprRegisters(edx = 0x2500u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x5900u)
        assertFlagRegisters(cf = false)
    }

    @Test fun rclTestr161() {
        val instruction = "rcl CX, 0x01"
        gprRegisters(ecx = 0xA513u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4A27u)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rclTestr16CL() {
        val instruction = "rcl DX, CL"
        gprRegisters(edx = 0xA513u, ecx = 0xFAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x4D4Au, ecx = 0xFAu)
    }

    @Test fun rclTestr16imm8() {
        val instruction = "rcl DX, 0x04"
        gprRegisters(edx = 0x2500u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x5009u)
        assertFlagRegisters(cf = false)
    }

    @Test fun rclTestr321() {
        val instruction = "rcl ECX, 0x01"
        gprRegisters(ecx = 0xA513_FFC5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4A27_FF8Au)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rclTestr32CL() {
        val instruction = "rcl EDX, CL"
        flagRegisters(cf = true)
        gprRegisters(edx = 0x22_A513u, ecx = 0xFAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x4E00_454Au, ecx = 0xFAu)
    }

    @Test fun rclTestr32imm8() {
        val instruction = "rcl EDX, 0x02"
        gprRegisters(edx = 0xFF00_0000u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xFC00_0003u)
        assertFlagRegisters(cf = true)
    }

    @Test fun rclTestm81() {
        val instruction = "rcl BYTE [EDX+0xFA12], 0x01"
        store(startAddress + 0xFF45u, 0x25u, Datatype.BYTE)
        gprRegisters(edx = 0x533u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45u, 0x4Bu, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533u)
        assertFlagRegisters(cf = false, of = false)
    }

    @Test fun rclTestm8CL() {
        val instruction = "rcl BYTE [EDX+0xFA12], CL"
        store(startAddress + 0xFF45u, 0x25u, Datatype.BYTE)
        gprRegisters(edx = 0x533u, ecx = 3u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45u, 0x2Cu, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533u, ecx = 3u)
        assertFlagRegisters(cf = true)
    }

    @Test fun rclTestm8imm8() {
        val instruction = "rcl BYTE [EDX+0xFA12], 0x04"
        store(startAddress + 0xFF45u, 0x25u, Datatype.BYTE)
        gprRegisters(edx = 0x533u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45u, 0x59u, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533u)
        assertFlagRegisters(cf = false)
    }

    @Test fun rclTestm161() {
        val instruction = "rcl WORD [EDX+0xFA12], 0x01"
        store(startAddress + 0xFF45u, 0xA513u, Datatype.WORD)
        gprRegisters(edx = 0x533u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45u, 0x4A27u, Datatype.WORD)
        assertGPRRegisters(edx = 0x533u)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rclTestm16CL() {
        val instruction = "rcl WORD [EDX+0xFA12], CL"
        store(startAddress + 0xFF45u, 0xA513u, Datatype.WORD)
        gprRegisters(edx = 0x533u, ecx = 0xFAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533u, ecx = 0xFAu)
        assertMemory(startAddress + 0xFF45u, 0x4D4Au, Datatype.WORD)
    }

    @Test fun rclTestm16imm8() {
        val instruction = "rcl WORD [EDX+0xFA12], 0x04"
        store(startAddress + 0xFF45u, 0x2500u, Datatype.WORD)
        gprRegisters(edx = 0x533u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45u, 0x5009u, Datatype.WORD)
        assertGPRRegisters(edx = 0x533u)
        assertFlagRegisters(cf = false)
    }

    @Test fun rclTestm321() {
        val instruction = "rcl DWORD [EDX+0xFA12], 0x01"
        store(startAddress + 0xFF45u, 0xA513_FFC5u, Datatype.DWORD)
        gprRegisters(edx = 0x533u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533u)
        assertMemory(startAddress + 0xFF45u, 0x4A27_FF8Au, Datatype.DWORD)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rclTestm32CL() {
        val instruction = "rcl DWORD [EDX+0xFA12], CL"
        store(startAddress + 0xFF45u, 0x22_A513u, Datatype.DWORD)
        flagRegisters(cf = true)
        gprRegisters(edx = 0x533u, ecx = 0xFAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45u, 0x4E00_454Au, Datatype.DWORD)
        assertGPRRegisters(edx = 0x533u, ecx = 0xFAu)
    }

    @Test fun rclTestm32imm8() {
        val instruction = "rcl DWORD [EDX+0xFA12], 0x02"
        store(startAddress + 0xFF45u, 0xFF00_0000u, Datatype.DWORD)
        gprRegisters(edx = 0x533u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533u)
        assertMemory(startAddress + 0xFF45u, 0xFC00_0003u, Datatype.DWORD)
        assertFlagRegisters(cf = true)
    }

    // TEST RCR INSTRUCTION

    @Test fun rcrTestr81() {
        val instruction = "rcr DH, 0x01"
        gprRegisters(edx = 0x2500u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x9200u)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rcrTestr16CL() {
        val instruction = "rcr DX, CL"
        gprRegisters(edx = 0xA513u, ecx = 0xFAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x89A9u, ecx = 0xFAu)
    }

    @Test fun rcrTestr32imm8() {
        val instruction = "rcr EDX, 0x02"
        gprRegisters(edx = 0xFF00_0000u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x7FC0_0000u)
        flagRegisters(of = true)
    }

    @Test fun rcrTestm8CL() {
        val instruction = "rcr BYTE [EDX+0xFA12], CL"
        store(startAddress + 0xFF45u, 0x25u, Datatype.BYTE)
        gprRegisters(edx = 0x533u, ecx = 3u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45u, 0x64u, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533u, ecx = 3u)
        assertFlagRegisters(cf = true)
    }

    @Test fun rcrTestm161() {
        val instruction = "rcr WORD [EDX+0xFA12], 0x01"
        store(startAddress + 0xFF45u, 0xA513u, Datatype.WORD)
        gprRegisters(edx = 0x533u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45u, 0xD289u, Datatype.WORD)
        assertGPRRegisters(edx = 0x533u)
        assertFlagRegisters(cf = true)
    }

    @Test fun rcrTestm32imm8() {
        val instruction = "rcr DWORD [EDX+0xFA12], 0x02"
        store(startAddress + 0xFF45u, 0xFF00_0000u, Datatype.DWORD)
        gprRegisters(edx = 0x533u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533u)
        assertMemory(startAddress + 0xFF45u, 0x7FC0_0000u, Datatype.DWORD)
        assertFlagRegisters()
    }

    // TEST ROL INSTRUCTION

    @Test fun rolTestr8imm8() {
        val instruction = "rol DH, 0x04"
        gprRegisters(edx = 0x2500u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x5200u)
        assertFlagRegisters(cf = false)
    }

    @Test fun rolTestr16CL() {
        val instruction = "rol DX, CL"
        gprRegisters(edx = 0xA513u, ecx = 0xFAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x4E94u, ecx = 0xFAu)
    }

    @Test fun rolTestr321() {
        val instruction = "rol ECX, 0x01"
        gprRegisters(ecx = 0xA513_FFC5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x4A27_FF8Bu)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rolTestm8CL() {
        val instruction = "rol BYTE [EDX+0xFA12], CL"
        store(startAddress + 0xFF45u, 0x25u, Datatype.BYTE)
        gprRegisters(edx = 0x533u, ecx = 3u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45u, 0x29u, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533u, ecx = 3u)
        assertFlagRegisters(cf = true)
    }

    @Test fun rolTestm161() {
        val instruction = "rol WORD [EDX+0xFA12], 0x01"
        store(startAddress + 0xFF45u, 0xA513u, Datatype.WORD)
        gprRegisters(edx = 0x533u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45u, 0x4A27u, Datatype.WORD)
        assertGPRRegisters(edx = 0x533u)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rolTestm32imm8() {
        val instruction = "rol DWORD [EDX+0xFA12], 0x02"
        store(startAddress + 0xFF45u, 0xFF00_0000u, Datatype.DWORD)
        gprRegisters(edx = 0x533u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533u)
        assertMemory(startAddress + 0xFF45u, 0xFC00_0003u, Datatype.DWORD)
        assertFlagRegisters(cf = true)
    }

    // TEST ROR INSTRUCTION

    @Test fun rorTestr8CL() {
        val instruction = "ror DH, CL"
        gprRegisters(edx = 0x2500u, ecx = 3u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xA400u, ecx = 3u)
        assertFlagRegisters(cf = true)
    }

    @Test fun rorTestr161() {
        val instruction = "ror CX, 0x01"
        gprRegisters(ecx = 0xA513u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xD289u)
        assertFlagRegisters(cf = true)
    }

    @Test fun rorTestr321() {
        val instruction = "ror ECX, 0x01"
        gprRegisters(ecx = 0x2513_FFC5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x9289_FFE2u)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun rorTestm8imm8() {
        val instruction = "ror BYTE [EDX+0xFA12], 0x04"
        store(startAddress + 0xFF45u, 0x25u, Datatype.BYTE)
        gprRegisters(edx = 0x533u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45u, 0x52u, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533u)
    }

    @Test fun rorTestm16CL() {
        val instruction = "ror WORD [EDX+0xFA12], CL"
        store(startAddress + 0xFF45u, 0xA513u, Datatype.WORD)
        gprRegisters(edx = 0x533u, ecx = 0xFAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533u, ecx = 0xFAu)
        assertMemory(startAddress + 0xFF45u, 0x44E9u, Datatype.WORD)
    }

    @Test fun rorTestm321() {
        val instruction = "ror DWORD [EDX+0xFA12], 0x01"
        store(startAddress + 0xFF45u, 0xA513_FFC4u, Datatype.DWORD)
        gprRegisters(edx = 0x533u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533u)
        assertMemory(startAddress + 0xFF45u, 0x5289_FFE2u, Datatype.DWORD)
        assertFlagRegisters(of = true)
    }

    // TEST SAL/SHL INSTRUCTION

    @Test fun salShlTestr81() {
        val instruction = "shl DH, 0x01"
        gprRegisters(edx = 0xA500u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x4A00u)
        assertFlagRegisters(cf = true, of = true)
    }

    @Test fun salShlTestr8CL() {
        val instruction = "shl DH, CL"
        gprRegisters(edx = 0x2500u, ecx = 3u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x2800u, ecx = 3u)
        assertFlagRegisters(cf = true, pf = true)
    }

    @Test fun salShlTestr161() {
        val instruction = "shl CX, 0x01"
        gprRegisters(ecx = 0x8000u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0u)
        assertFlagRegisters(zf = true, pf = true, cf = true, of = true)
    }

    @Test fun salShlTestr16CL() {
        val instruction = "shl BX, CL"
        gprRegisters(ebx = 0x8000u, ecx = 0x454E_ABFFu)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x454E_ABFFu)
        assertFlagRegisters(zf = true, pf = true)
    }

    // TEST SAR INSTRUCTION

    @Test fun sarTestr321() {
        val instruction = "sar ECX, 0x01"
        gprRegisters(ecx = 0x2513_FFC5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x1289_FFE2u)
        assertFlagRegisters(cf = true, pf = true)
    }

    @Test fun sarTestm8imm8() {
        val instruction = "sar BYTE [EDX+0xFA12], 0x04"
        store(startAddress + 0xFF45u, 0xFFu, Datatype.BYTE)
        gprRegisters(edx = 0x533u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45u, 0xFFu, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533u)
        assertFlagRegisters(cf = true, pf = true, sf = true)
    }

    @Test fun sarTestm32CL() {
        val instruction = "sar DWORD [EDX+0xFA12], CL"
        store(startAddress + 0xFF45u, 0xA513_FFC4u, Datatype.DWORD)
        gprRegisters(edx = 0x533u, ecx = 0xF7u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533u, ecx = 0xF7u)
        assertMemory(startAddress + 0xFF45u, 0xFFFF_FF4Au, Datatype.DWORD)
        assertFlagRegisters(sf = true)
    }

    @Test fun sarTestr16CL() {
        val instruction = "sar AX, CL"
        gprRegisters(ecx = 0x10u, eax = 0x656Du)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x10u, eax = 0x0u)
        assertFlagRegisters(cf = false, pf = true, zf = true)
    }

    @Test fun sarTestr16CLFlag() {
        val instruction = "sar AL, CL"
        gprRegisters(ecx = 0x10u, eax = 0xFFu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x10u, eax = 0xFFu)
        assertFlagRegisters(cf = true, pf = true, sf = true)
    }

    // TEST SHR INSTRUCTION

    @Test fun shrTestr321() {
        val instruction = "shr ECX, 0x01"
        gprRegisters(ecx = 0x2513_FFC5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0x1289_FFE2u)
        assertFlagRegisters(cf = true, pf = true)
    }

    @Test fun shrTestm8imm8() {
        val instruction = "shr BYTE [EDX+0xFA12], 0x04"
        store(startAddress + 0xFF45u, 0xFFu, Datatype.BYTE)
        gprRegisters(edx = 0x533u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xFF45u, 0x0Fu, Datatype.BYTE)
        assertGPRRegisters(edx = 0x533u)
        assertFlagRegisters(cf = true, pf = true)
    }

    @Test fun shrTestm32CL() {
        val instruction = "shr DWORD [EDX+0xFA12], CL"
        store(startAddress + 0xFF45u, 0xA5C3_FFC4u, Datatype.DWORD)
        gprRegisters(edx = 0x533u, ecx = 0xF7u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0x533u, ecx = 0xF7u)
        assertMemory(startAddress + 0xFF45u, 0x0000_014Bu, Datatype.DWORD)
        assertFlagRegisters(cf = true, pf = true)
    }

    @Test fun shrTestr16CL() {
        val instruction = "shr AX, CL"
        gprRegisters(ecx = 0xE5u, eax = 0x656Du)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(ecx = 0xE5u, eax = 0x32Bu)
        assertFlagRegisters(cf = false, pf = true)
    }

    // TEST CDQ INSTRUCTION

    @Test fun cdqTestEAXNegative() {
        val instruction = "cdq "
        gprRegisters(eax = 0xFFAC_FFC5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFFAC_FFC5u, edx = 0xFFFF_FFFFu)
    }

    @Test fun cdqTestEAXPositive() {
        val instruction = "cdq "
        gprRegisters(eax = 0x60AC_FFC5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x60AC_FFC5u)
    }

    // TEST CWDE INSTRUCTION

    @Test fun cwdeTestAXNegative() {
        val instruction = "cwde "
        gprRegisters(eax = 0xFFC5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFFFF_FFC5u)
    }

    @Test fun cwdeTestAXPositive() {
        val instruction = "cwde "
        gprRegisters(eax = 0x60ACu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x60ACu)
    }

    // TEST LEA INSTRUCTION

    @Test fun leaTestr16m16() {
        val instruction = "lea AX, [0xFC54]"
        val insnString =  "lea ax, word [0xFC54]"
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xFC54u, esp = 0x1000u)
    }

    @Test fun leaTestr16m32() {
        val instruction = "lea EAX, [ECX + 0xFC54]"
        val insnString =  "lea eax, DWORD [ECX+0xFC54]"
        gprRegisters(ecx = 0xFFA0_1425u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(ecx = 0xFFA0_1425u, eax = 0xFFA1_1079u)
    }

    @Test fun leaTestr16p32() {
        val instruction = "lea EAX, [ECX + 8 * EBX + 4]"
        val insnString =  "lea eax, DWORD[ECX+8*EBX+0x04]"
        gprRegisters(ecx = 0xFFA0_1425u, ebx = 0x1F8Au)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(ecx = 0xFFA0_1425u, eax = 0xFFA1_1079u, ebx = 0x1F8Au)
    }

    // TEST LSL INSTRUCTION

    @Test fun lslTestr16m16() {
        val instruction = "lsl AX, BX"
        val insnString =  "lsl ax, bx"
        gprRegisters(ebx = 0xBABAu)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(ebx = 0xBABAu)
    }

    // TEST LDS INSTRUCTION

    @Test fun ldsTestr16m16() {
        val instruction = "lds AX, [0xFC54]"
        val insnString =  "lds ax, fword [0xfc54]"
        store(startAddress + 0xFC54u, 0xFACA_BABAu, Datatype.DWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABAu, esp = 0x1000u)
        assertSegmentRegisters(cs = 0x8u, ds = 0xFACAu, ss = 0x8u)
    }

    @Test fun ldsTestr16m32() {
        val instruction = "lds EAX, [0xFC54]"
        val insnString =  "lds eax, fword [0xfc54]"
        store(startAddress + 0xFC54u, 0xFACA_BABA_CABAu, Datatype.FWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA_CABAu, esp = 0x1000u)
        assertSegmentRegisters(cs = 0x8u, ds = 0xFACAu, ss = 0x8u)
    }

    // TEST LES INSTRUCTION

    @Test fun lesTestr16m16() {
        val instruction = "les AX, [0xFC54]"
        val insnString =  "les ax, fword [0xFC54]"
        store(startAddress + 0xFC54u, 0xFACA_BABAu, Datatype.DWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABAu, esp = 0x1000u)
        assertSegmentRegisters(ds = 0x8u, cs = 0x8u, es = 0xFACAu, ss = 0x8u)
    }

    @Test fun lesTestr16m32() {
        val instruction = "les EAX, [0xFC54]"
        val insnString =  "les eax, FWORD [0xFC54]"
        store(startAddress + 0xFC54u, 0xFACA_BABA_CABAu, Datatype.FWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA_CABAu, esp = 0x1000u)
        assertSegmentRegisters(ds = 0x8u, cs = 0x8u, es = 0xFACAu, ss = 0x8u)
    }

    // TEST LFS INSTRUCTION

    @Test fun lfsTestr16m16() {
        val instruction = "lfs AX, [0xFC54]"
        val insnString =  "lfs ax, FWORD [0xFC54]"
        store(startAddress + 0xFC54u, 0xFACA_BABAu, Datatype.DWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABAu, esp = 0x1000u)
        assertSegmentRegisters(ds = 0x8u, cs = 0x8u, fs = 0xFACAu, ss = 0x8u)
    }

    @Test fun lfsTestr16m32() {
        val instruction = "lfs EAX, [0xFC54]"
        val insnString =  "lfs eax, FWORD [0xFC54]"
        store(startAddress + 0xFC54u, 0xFACA_BABA_CABAu, Datatype.FWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA_CABAu, esp = 0x1000u)
        assertSegmentRegisters(ds = 0x8u, cs = 0x8u, fs = 0xFACAu, ss = 0x8u)
    }

    // TEST LFS INSTRUCTION

    @Test fun lgsTestr16m16() {
        val instruction = "lgs AX, [0xFC54]"
        val insnString =  "lgs ax, FWORD [0xFC54]"
        store(startAddress + 0xFC54u, 0xFACA_BABAu, Datatype.DWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABAu, esp = 0x1000u)
        assertSegmentRegisters(ds = 0x8u, cs = 0x8u, gs = 0xFACAu, ss = 0x8u)
    }

    @Test fun lgsTestr16m32() {
        val instruction = "lgs EAX, [0xFC54]"
        val insnString =  "lgs eax, FWORD [0xFC54]"
        store(startAddress + 0xFC54u, 0xFACA_BABA_CABAu, Datatype.FWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA_CABAu, esp = 0x1000u)
        assertSegmentRegisters(ds = 0x8u, cs = 0x8u, gs = 0xFACAu, ss = 0x8u)
    }

    // TEST LSS INSTRUCTION

    @Test fun lssTestr16m16() {
        val instruction = "lss AX, [0xFC54]"
        val insnString =  "lss ax, FWORD [0xFC54]"
        store(startAddress + 0xFC54u, 0xFACA_BABAu, Datatype.DWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABAu, esp = 0x1000u)
        assertSegmentRegisters(ds = 0x8u, cs = 0x8u, ss = 0xFACAu)
    }

    @Test fun lssTestr16m32() {
        val instruction = "lss EAX, [0xFC54]"
        val insnString =  "lss eax, FWORD [0xFC54]"
        store(startAddress + 0xFC54u, 0xFACA_BABA_CABAu, Datatype.FWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBABA_CABAu, esp = 0x1000u)
        assertSegmentRegisters(ds = 0x8u, cs = 0x8u, ss = 0xFACAu)
    }

    // TEST POP/PUSH INSTRUCTION

    @Test fun pushPopTestr16() {
        val instructionPush = "push AX"
        gprRegisters(eax = 0x60ACu)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0x60ACu, esp = 0xFFFEu)

        val instructionPop = "pop BX"
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertGPRRegisters(eax = 0x60ACu, ebx = 0x60ACu)
    }

    @Test fun pushPopTestr32() {
        val instructionPush = "push EAX"
        gprRegisters(eax = 0xFFA8_60ACu)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0xFFA8_60ACu, esp = 0xFFFCu)

        val instructionPop = "pop EBX"
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertGPRRegisters(eax = 0xFFA8_60ACu, ebx = 0xFFA8_60ACu)
    }

    @Test fun pushPopTestimm8r16() {
        val instructionPush = "push 0x16"
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(esp = 0xFFEu)

        val instructionPop = "pop BX"
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertGPRRegisters(ebx = 0x16u, esp = 0x1000u)
    }

    @Test fun pushPopTestimm16m16() {
        val instructionPush = "push 0x60AC"
        store(startAddress + 0xF540u, 0xFACAu, Datatype.WORD)
        gprRegisters(esp = 0xF000u, edx = 0xDA41u)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(edx = 0xDA41u, esp = 0xEFFEu)

        val instructionPop = "pop WORD [EDX+0x1AFF]"
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertMemory(startAddress + 0xF540u, 0x60ACu, Datatype.WORD)
    }

    @Test fun pushPopTestm16DS() {
        val instructionPush = "push WORD [EDX+0x1AFF]"
        store(startAddress + 0xF540u, 0xFACAu, Datatype.WORD)
        gprRegisters(edx = 0xDA41u, esp = 0xF000u)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(edx = 0xDA41u, esp = 0xEFFEu)

        val instructionPop = "pop FS"
        store(startAddress + 0xF540u, 0xFACAu, Datatype.WORD)
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertSegmentRegisters(cs = 0x8u, ds = 0x8u, ss = 0x8u, fs = 0xFACAu)
    }

    @Test fun pushPopTestGSr32() {
        val instructionPush = "push GS"
        gprRegisters(esp = 0xF000u)
        segmentRegisters(gs = 0xABBAu)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(esp = 0xEFFEu)

        val instructionPop = "pop EAX"
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertGPRRegisters(esp = 0xF002u, eax = 0xABBAu)
    }

    // TEST POPA/PUSHA INSTRUCTION

    @Test fun pushaPopaTest() {
        val instructionPush = "pusha "
        gprRegisters(eax = 0xFF01u, ebx = 0xFF02u, ecx = 0xFF03u, edx = 0xFF04u,
                     esp = 0xF000u, ebp = 0xFF05u, esi = 0xFF06u, edi = 0xFF07u)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0xFF01u, ebx = 0xFF02u, ecx = 0xFF03u, edx = 0xFF04u,
                           ebp = 0xFF05u, esi = 0xFF06u, edi = 0xFF07u, esp = 0xEFF0u)

        val instructionPop = "popa "
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertGPRRegisters(eax = 0xFF01u, ebx = 0xFF02u, ecx = 0xFF03u, edx = 0xFF04u,
                           esp = 0xF000u, ebp = 0xFF05u, esi = 0xFF06u, edi = 0xFF07u)
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

    @Test fun pushfPopfTestGeneral1() {
        val instructionPush = "pushf "
        flagRegisters(vm = true)
        eflag(0x4221u)
        iopl(2)
        x86.cpu.cregs.cr0.value = 0uL
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertTrue { x86.cpu.exception is x86HardwareException.GeneralProtectionFault }
    }

    @Test fun pushfPopfTestGeneral2() {
        val instructionPush = "pushf "
        eflag(0x4221u)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertEflag(0x4221u)
        eflag(0u)

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
        gprRegisters(esp = 0xF000u)
        execute(-3uL) { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(esp = 0xEFFEu)

        gprRegisters(esp = 0xB2ACu, ebp = 0xEFFEu)
        val instructionLeave = "leave "
        execute { assemble(instructionLeave) }
        assertAssembly(instructionLeave)
        assertGPRRegisters(ebp = 0xCA16u, esp = 0xF000u)
    }

    // TEST ENTER INSTRUCTION

    @Test fun enterTest() {
        val instructionLeave = "enter 0x82, 0x00"
        execute { assemble(instructionLeave) }
        assertAssembly(instructionLeave)
        assertGPRRegisters(esp = 0xF7Cu, ebp = 0xFFEu)
    }

    // TEST MOVSX INSTRUCTION

    @Test fun movsxTestr16m8() {
        val instruction = "movsx AX, BYTE [EDX+0x1AFF]"
        store(startAddress + 0xF540u, 0x7Au, Datatype.WORD)
        gprRegisters(eax = 0xCAFFu, edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x7Au, edx = 0xDA41u)
    }

    @Test fun movsxTestr32m8() {
        val instruction = "movsx EAX, BYTE [EDX+0x1AFF]"
        store(startAddress + 0xF540u, 0xFAu, Datatype.WORD)
        gprRegisters(eax = 0xCAFFu, edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFFFF_FFFAu, edx = 0xDA41u)
    }

    @Test fun movsxTestr32r16() {
        val instruction = "movsx EAX, DX"
        gprRegisters(eax = 0xACCA_CAFFu, edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFFFF_DA41u, edx = 0xDA41u)
    }

    // TEST MOVZX INSTRUCTION

    @Test fun movzxTestr32m16() {
        val instruction = "movzx EAX, WORD [EDX+0x1AFF]"
        store(startAddress + 0xF540u, 0xFF7Au, Datatype.WORD)
        gprRegisters(eax = 0xFFFF_CAFFu, edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFF7Au, edx = 0xDA41u)
    }

    @Test fun movzxTestr32r8() {
        val instruction = "movzx EAX, BL"
        gprRegisters(eax = 0xCAFFu, ebx = 0x3E52u, edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x52u, edx = 0xDA41u, ebx = 0x3E52u)
    }

    @Test fun movzxTestr16r8() {
        val instruction = "movzx AX, CL"
        gprRegisters(eax = 0xACCA_CAFFu, edx = 0xDA41u, ecx = 0xF1u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xACCA_00F1u, ecx = 0xF1u, edx = 0xDA41u)
    }

    // TEST MOV INSTRUCTION

    @Test fun movTestr8m8() {
        val instruction = "mov AL, BYTE [EDX+0x1AFF]"
        store(startAddress + 0xF540u, 0x7Au, Datatype.BYTE)
        gprRegisters(eax = 0xCAFFu, edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xCA7Au, edx = 0xDA41u)
    }

    @Test fun movTestr16m16() {
        val instruction = "mov AX, WORD [EDX+0x1AFF]"
        store(startAddress + 0xF540u, 0xFF7Au, Datatype.WORD)
        gprRegisters(eax = 0xCAFFu, edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFF7Au, edx = 0xDA41u)
    }

    @Test fun movTestr32m32() {
        val instruction = "mov EAX, DWORD [EDX+0x1AFF]"
        store(startAddress + 0xF540u, 0xFFFF_FFFAu, Datatype.DWORD)
        gprRegisters(eax = 0xCAFFu, edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFFFF_FFFAu, edx = 0xDA41u)
    }

    @Test fun movTestr8r8() {
        val instruction = "mov AL, DH"
        gprRegisters(eax = 0xCAu, edx = 0xDA00u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xDAu, edx = 0xDA00u)
    }

    @Test fun movTestr16r16() {
        val instruction = "mov AX, BX"
        gprRegisters(eax = 0xCAFFu, ebx = 0x3E52u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x3E52u, ebx = 0x3E52u)
    }

    @Test fun movTestr32r32() {
        val instruction = "mov EAX, ECX"
        gprRegisters(eax = 0xACCA_CAFFu, ecx = 0xDA41_01F1u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xDA41_01F1u, ecx = 0xDA41_01F1u)
    }

    @Test fun movTestr8imm8() {
        val instruction = "mov AL, 0xDA"
        gprRegisters(eax = 0xCAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xDAu)
    }

    @Test fun movTestr16imm16() {
        val instruction = "mov AX, 0x3E52"
        gprRegisters(eax = 0xCAFFu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x3E52u)
    }

    @Test fun movTestr32imm32() {
        val instruction = "mov EAX, 0xDA4101F1"
        gprRegisters(eax = 0xACCA_CAFFu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xDA41_01F1u)
    }

    @Test fun movTestm8imm8() {
        val instruction = "mov BYTE [EDX+0x10F7], 0xDA"
        gprRegisters(edx = 0xACA1u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBD98u, 0xDAu, Datatype.BYTE)
        assertGPRRegisters(edx = 0xACA1u)
    }

    @Test fun movTestm16imm16() {
        val instruction = "mov WORD [EDX+0x10F7], 0xA0DA"
        gprRegisters(edx = 0xACA1u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBD98u, 0xA0DAu, Datatype.WORD)
        assertGPRRegisters(edx = 0xACA1u)
    }

    @Test fun movTestm32imm32() {
        val instruction = "mov DWORD [EDX+0x10F7], 0xFAC6A0DA"
        gprRegisters(edx = 0xACA1u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBD98u, 0xFAC6_A0DAu, Datatype.DWORD)
        assertGPRRegisters(edx = 0xACA1u)
    }

    @Test fun movTestm8r8() {
        val instruction = "mov BYTE [EDX+0x10F7], AH"
        gprRegisters(edx = 0xACA1u, eax = 0xDA00u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBD98u, 0xDAu, Datatype.BYTE)
        assertGPRRegisters(edx = 0xACA1u, eax = 0xDA00u)
    }

    @Test fun movTestm16r16() {
        val instruction = "mov WORD [EDX+0x10F7], AX"
        gprRegisters(edx = 0xACA1u, eax = 0xA0DAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBD98u, 0xA0DAu, Datatype.WORD)
        assertGPRRegisters(edx = 0xACA1u, eax = 0xA0DAu)
    }

    @Test fun movTestm32r32() {
        val instruction = "mov DWORD [EDX+0x10F7], EBX"
        gprRegisters(edx = 0xACA1u, ebx = 0xFAC6_A0DAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBD98u, 0xFAC6_A0DAu, Datatype.DWORD)
        assertGPRRegisters(edx = 0xACA1u, ebx = 0xFAC6_A0DAu)
    }

    @Test fun movTestm16Sreg() {
        val instruction = "mov WORD [EDX+0x10F7], GS"
        segmentRegisters(gs = 0xACFAu)
        gprRegisters(edx = 0xACA1u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xBD98u, 0xACFAu, Datatype.WORD)
        assertGPRRegisters(edx = 0xACA1u)
        assertSegmentRegisters(gs = 0xACFAu, ds = 0x8u, ss = 0x8u, cs = 0x8u)
    }

    @Test fun movTestr16Sreg() {
        val instruction = "mov DX, GS"
        segmentRegisters(gs = 0xACFAu)
        gprRegisters(edx = 0xACA1u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xACFAu)
        assertSegmentRegisters(gs = 0xACFAu, ds = 0x8u, ss = 0x8u, cs = 0x8u)
    }

    @Test fun movTestSregm16() {
        val instruction = "mov FS, WORD [EDX+0x1AFF]"
        store(startAddress + 0xF540u, 0xFF7Au, Datatype.WORD)
        gprRegisters(edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xDA41u)
        assertSegmentRegisters(fs = 0xFF7Au, ds = 0x8u, ss = 0x8u, cs = 0x8u)
    }

    @Test fun movTestSregr16() {
        val instruction = "mov FS, DX"
        gprRegisters(edx = 0xFF7Au)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xFF7Au)
        assertSegmentRegisters(fs = 0xFF7Au, ds = 0x8u, ss = 0x8u, cs = 0x8u)
    }

    // TEST XCHG INSTRUCTION

    @Test fun xchgTestm8r8() {
        val instruction = "xchg BH, BYTE [EDX+0x1AFF]"
        store(startAddress + 0xF540u, 0x7Au, Datatype.BYTE)
        gprRegisters(edx = 0xDA41u, ebx = 0x6543u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xDA41u, ebx = 0x7A43u)
        assertMemory(startAddress + 0xF540u, 0x65u, Datatype.BYTE)
    }

    @Test fun xchgTestr16r16() {
        val instruction = "xchg AX, DX"
        gprRegisters(eax = 0xCAFFu, edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xCAFFu, eax = 0xDA41u)
    }

    @Test fun xchgTestr32r32() {
        val instruction = "xchg EAX, EDX"
        gprRegisters(eax = 0xFAFA_CAFFu, edx = 0xBABA_DA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(edx = 0xFAFA_CAFFu, eax = 0xBABA_DA41u)
    }

    // TEST SETA INSTRUCTION

    @Test fun setaTestm8Posistive() {
        val instruction = "seta BYTE [EDX+0x1AFF]"
        gprRegisters(edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540u, 0x1u, Datatype.BYTE)
    }

    @Test fun setaTestm8Negative1() {
        val instruction = "seta BYTE [EDX+0x1AFF]"
        flagRegisters(zf = true)
        gprRegisters(edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540u, 0u, Datatype.BYTE)
    }

    @Test fun setaTestm8Negative2() {
        val instruction = "seta BYTE [EDX+0x1AFF]"
        flagRegisters(cf = true)
        gprRegisters(edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540u, 0u, Datatype.BYTE)
    }

    @Test fun setaTestm8Negative3() {
        val instruction = "seta BYTE [EDX+0x1AFF]"
        flagRegisters(zf = true, cf = true)
        gprRegisters(edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540u, 0u, Datatype.BYTE)
    }

    // TEST SETB INSTRUCTION

    @Test fun setbTestm8Posistive() {
        val instruction = "setb BYTE [EDX+0x1AFF]"
        gprRegisters(edx = 0xDA41u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540u, 0x1u, Datatype.BYTE)
    }

    @Test fun setbTestm8Negative() {
        val instruction = "setb BYTE [EDX+0x1AFF]"
        gprRegisters(edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540u, 0u, Datatype.BYTE)
    }

    // TEST SETBE INSTRUCTION

    @Test fun setbeTestm8Posistive1() {
        val instruction = "setbe BYTE [EDX+0x1AFF]"
        gprRegisters(edx = 0xDA41u)
        flagRegisters(zf = true, cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540u, 0x1u, Datatype.BYTE)
    }

    @Test fun setbeTestm8Posistive2() {
        val instruction = "setbe BYTE [EDX+0x1AFF]"
        flagRegisters(zf = true)
        gprRegisters(edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540u, 0x1u, Datatype.BYTE)
    }

    @Test fun setbeTestm8Posistive3() {
        val instruction = "setbe BYTE [EDX+0x1AFF]"
        flagRegisters(cf = true)
        gprRegisters(edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540u, 0x1u, Datatype.BYTE)
    }

    @Test fun setbeTestm8Negative() {
        val instruction = "setbe BYTE [EDX+0x1AFF]"
        gprRegisters(edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540u, 0u, Datatype.BYTE)
    }

    // TEST SETG INSTRUCTION

    @Test fun setgTestm8Posistive1() {
        val instruction = "setg BYTE [EDX+0x1AFF]"
        gprRegisters(edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540u, 0x1u, Datatype.BYTE)
    }

    @Test fun setgTestm8Posistive2() {
        val instruction = "setg BYTE [EDX+0x1AFF]"
        flagRegisters(sf = true, of = true)
        gprRegisters(edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540u, 0x1u, Datatype.BYTE)
    }

    @Test fun setgTestm8Negative1() {
        val instruction = "setg BYTE [EDX+0x1AFF]"
        flagRegisters(zf = true)
        gprRegisters(edx = 0xDA41u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540u, 0u, Datatype.BYTE)
    }

    @Test fun setgTestm8Negative2() {
        val instruction = "setg BYTE [EDX+0x1AFF]"
        gprRegisters(edx = 0xDA41u)
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF540u, 0u, Datatype.BYTE)
    }

    // TEST SETGE INSTRUCTION

    @Test fun setgeTestr8Posistive1() {
        val instruction = "setge AH"
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100u, esp = 0x1000u)
    }

    @Test fun setgeTestr8Posistive2() {
        val instruction = "setge AH"
        flagRegisters(sf = true, of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100u, esp = 0x1000u)
    }

    @Test fun setgeTestr8Negative1() {
        val instruction = "setge AH"
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(esp = 0x1000u)
    }

    @Test fun setgeTestr8Negative2() {
        val instruction = "setge AH"
        flagRegisters(of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(esp = 0x1000u)
    }

    // TEST SETL INSTRUCTION

    @Test fun setlTestr8Positive1() {
        val instruction = "setl AH"
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100u, esp = 0x1000u)
    }

    @Test fun setlTestr8Positive2() {
        val instruction = "setl AH"
        flagRegisters(of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100u, esp = 0x1000u)
    }

    @Test fun setlTestr8Negative1() {
        val instruction = "setl AH"
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(esp = 0x1000u)
    }

    @Test fun setlTestr8Negative2() {
        val instruction = "setl AH"
        flagRegisters(sf = true, of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(esp = 0x1000u)
    }

    // TEST SETLE INSTRUCTION

    @Test fun setleTestr8Positive1() {
        val instruction = "setle AH"
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100u, esp = 0x1000u)
    }

    @Test fun setleTestr8Positive2() {
        val instruction = "setle AH"
        flagRegisters(of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100u, esp = 0x1000u)
    }

    @Test fun setleTestr8Positive3() {
        val instruction = "setle AH"
        flagRegisters(zf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100u, esp = 0x1000u)
    }

    @Test fun setleTestr8Positive4() {
        val instruction = "setle AH"
        flagRegisters(zf = true, sf = true, of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x100u, esp = 0x1000u)
    }

    @Test fun setleTestr8Negative1() {
        val instruction = "setle AH"
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(esp = 0x1000u)
    }

    @Test fun setleTestr8Negative2() {
        val instruction = "setle AH"
        flagRegisters(sf = true, of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(esp = 0x1000u)
    }

    // TEST SETNB INSTRUCTION

    @Test fun setnbTestm8Posistive() {
        val instruction = "setnb BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0x1u, Datatype.BYTE)
    }

    @Test fun setnbTestm8Negative() {
        val instruction = "setnb BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        flagRegisters(cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0u, Datatype.BYTE)
    }

    // TEST SETNE INSTRUCTION

    @Test fun setneTestm8Positive() {
        val instruction = "setne BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0x1u, Datatype.BYTE)
    }

    @Test fun setneTestm8Negative() {
        val instruction = "setne BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        flagRegisters(zf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0u, Datatype.BYTE)
    }

    // TEST SETNO INSTRUCTION

    @Test fun setnoTestm8Posistive() {
        val instruction = "setno BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0x1u, Datatype.BYTE)
    }

    @Test fun setnoTestm8Negative() {
        val instruction = "setno BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        flagRegisters(of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0u, Datatype.BYTE)
    }

    // TEST SETNS INSTRUCTION

    @Test fun setnsTestm8Posistive() {
        val instruction = "setns BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0x1u, Datatype.BYTE)
    }

    @Test fun setnsTestm8Negative() {
        val instruction = "setns BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0u, Datatype.BYTE)
    }

    // TEST SETO INSTRUCTION

    @Test fun setoTestm8Positive() {
        val instruction = "seto BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        flagRegisters(of = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0x1u, Datatype.BYTE)
    }

    @Test fun setoTestm8Negative() {
        val instruction = "seto BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0u, Datatype.BYTE)
    }

    // TEST SETPE INSTRUCTION

    @Test fun setpeTestm8Positive() {
        val instruction = "setpe BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        flagRegisters(pf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0x1u, Datatype.BYTE)
    }

    @Test fun setpeTestm8Negative() {
        val instruction = "setpe BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0u, Datatype.BYTE)
    }

    // TEST SETPO INSTRUCTION

    @Test fun setpoTestm8Posistive() {
        val instruction = "setpo BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0x1u, Datatype.BYTE)
    }

    @Test fun setpoTestm8Negative() {
        val instruction = "setpo BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        flagRegisters(pf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0u, Datatype.BYTE)
    }

    // TEST SETS INSTRUCTION

    @Test fun setsTestm8Positive() {
        val instruction = "sets BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        flagRegisters(sf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0x1u, Datatype.BYTE)
    }

    @Test fun setsTestm8Negative() {
        val instruction = "sets BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0u, Datatype.BYTE)
    }

    // TEST SETZ INSTRUCTION

    @Test fun setzTestm8Positive() {
        val instruction = "setz BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        flagRegisters(zf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0x1u, Datatype.BYTE)
    }

    @Test fun setzTestm8Negative() {
        val instruction = "setz BYTE [EDX+0x5FE9]"
        gprRegisters(edx = 0x63C5u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xC3AEu, 0u, Datatype.BYTE)
    }

    // TEST CALL INSTRUCTION

    @Test fun callTestptr1616() {
        val instruction = "call 0x1234:0x5678" // 5 byte hex
        val insnString = "call 1234:00005678"
        execute(0x5673u) { assemble(instruction) }
        assertAssembly(insnString)
        assertSegmentRegisters(ds = 0x8u, ss = 0x8u, cs = 0x1234u)
    }

    @Test fun callTestptr1632() {
        val instruction = "call 0x1234: dword 0xAAC_5678" // 8 byte hex
        val insnString = "call 1234:0AAC5678"
        execute(0xAAC_5670u) { assemble(instruction) }
        assertAssembly(insnString)
        assertSegmentRegisters(ds = 0x8u, ss = 0x8u, cs = 0x1234u)
    }

    @Test fun callTestrel16() {
        val instruction = "call 0x5678"  // 3 byte hex
        val insnString = "call 0x5675"
        execute(0x5675u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun callTestrel32() {
        val instruction = "call dword 0xAAC_5678"  // 6 byte hex
        val insnString = "call 0xAAC5672"
        execute(0xAAC_5672u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun callTestr16() {
        val instruction = "call DX"  // 2 byte hex
        gprRegisters(edx = 0x5678u)
        execute(0x5676u) { assemble(instruction) }
        assertAssembly(instruction)
    }

    @Test fun callTestr32() {
        val instruction = "call EDX"  // 3 byte hex
        gprRegisters(edx = 0xAAC_5678u)
        execute(0xAAC_5675u) { assemble(instruction) }
        assertAssembly(instruction)
    }

    @Test fun callTestm1616() {
        val instruction = "call word far [cs: word 0xF540]" // 5 byte hex
        val insnString = "call 0008:00005678"
        store(startAddress + 0xF540u, 0x5678u, Datatype.WORD)
        store(startAddress + 0xF542u, 0x0008u, Datatype.WORD)
        execute(0x5673u) { assemble(instruction) }
        assertAssembly(insnString)
        assertSegmentRegisters(ds = 0x8u, ss = 0x8u, cs = 0x08u)
    }

    @Test fun callTestm1632() {
        val instruction = "call dword far [fs: dword 0xF540]" // 9 byte hex
        val insnString = "call 0008:0AAC5678"
        segmentRegisters(cs = 0x08u, ds = 0x08u, ss = 0x08u, es = 0x08u, fs = 0x8u, gs = 0x08u)
        store(startAddress + 0xF540u, 0xAAC_5678u , Datatype.DWORD)
        store(startAddress + 0xF544u, 0x0008u , Datatype.WORD)
        execute(0xAAC_566Fu) { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JMP INSTRUCTION

    @Test fun jmpTestptr1616() {
        val instruction = "jmp 0x1234:0x5678" // 5 byte hex
        val insnString = "jmp 1234:00005678"
        execute(0x5673u) { assemble(instruction) }
        assertAssembly(insnString)
        assertSegmentRegisters(ds = 0x8u, ss = 0x8u, cs = 0x1234u)
    }

    @Test fun jmpTestptr1632() {
        val instruction = "jmp 0x1234: dword 0xAAC_5678" // 8 byte hex
        val insnString = "jmp 1234:0AAC5678"
        execute(0xAAC_5670u) { assemble(instruction) }
        assertAssembly(insnString)
        assertSegmentRegisters(ds = 0x8u, ss = 0x8u, cs = 0x1234u)
    }

    @Test fun jmpTestrel16() {
        val instruction = "jmp 0x5678"  // 3 byte hex
        val insnString = "jmp 0x5675"
        execute(0x5675u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jmpTestrel32() {
        val instruction = "jmp dword 0xAAC_5678"  // 6 byte hex
        val insnString = "jmp 0xAAC5672"
        execute(0xAAC_5672u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jmpTestr16() {
        val instruction = "jmp DX"  // 2 byte hex
        gprRegisters(edx = 0x5678u)
        execute(0x5676u) { assemble(instruction) }
        assertAssembly(instruction)
    }

    @Test fun jmpTestr32() {
        val instruction = "jmp EDX"  // 3 byte hex
        gprRegisters(edx = 0xAAC_5678u)
        execute(0xAAC_5675u) { assemble(instruction) }
        assertAssembly(instruction)
    }

    @Test fun jmpTestm1616() {
        val instruction = "jmp word far [cs: word 0xF540]" // 5 byte hex
        val insnString = "jmp 0008:00005678"
        store(startAddress + 0xF540u, 0x5678u, Datatype.WORD)
        store(startAddress + 0xF542u, 0x0008u, Datatype.WORD)
        execute(0x5673u) { assemble(instruction) }
        assertAssembly(insnString)
        assertSegmentRegisters(ds = 0x8u, ss = 0x8u, cs = 0x8u)
    }

    @Test fun jmpTestm1632() {
        val instruction = "jmp dword far [fs: dword 0xF540]" // 9 byte hex
        val insnString = "jmp 0008:0AAC5678"
        segmentRegisters(cs = 0x08u, ds = 0x08u, ss = 0x08u, es = 0x08u, fs = 0x8u, gs = 0x08u)
        store(startAddress + 0xF540u, 0xAAC_5678u, Datatype.DWORD)
        store(startAddress + 0xF544u, 0x0008u, Datatype.WORD)
        execute(0xAAC_566Fu) { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST RET INSTRUCTION

    @Test fun retTestNear16() {
        val instructionPush = "push AX"
        gprRegisters(eax = 0x60ACu, esp = 0xF000u)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0x60ACu, esp = 0xEFFEu)

        val instruction = "retn 0x0"
        val insnString = "ret 0x00"
        execute(0x60A8u) { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0x60ACu, esp = 0xF000u)
    }

    @Test fun retTestNear32() {
        val instructionPush = "push EAX"
        gprRegisters(eax = 0xCAFF_60ACu, esp = 0xF000u)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0xCAFF_60ACu, esp = 0xEFFCu)

        val instruction = "retf 0x0"
        val insnString = "ret 0x00"
        execute(0x60A7u) { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xCAFF_60ACu, esp = 0xF000u)
        assertSegmentRegisters(ds = 0x8u, ss = 0x8u, cs = 0xCAFFu)
    }

    @Test fun retTestFar16() {
        val instructionPush = "push EAX"
        gprRegisters(eax = 0x1111_2222u, esp = 0xF000u)
        execute(-2uL) { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0x1111_2222u, esp = 0xEFFCu)

        gprRegisters(eax = 0x0000_4444u, esp = 0xEFFCu)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0x0000_4444u, esp = 0xEFF8u)

        val instruction = "retn 0x2"
        val insnString = "ret 0x02"
        execute(0x443Fu) { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0x4444u, esp = 0xEFFCu)
    }

    @Test fun retTestFar32() {
        val instructionPush = "push EAX"
        gprRegisters(eax = 0x1111_2222u, esp = 0xF000u)
        execute(-2uL) { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0x1111_2222u, esp = 0xEFFCu)

        gprRegisters(eax = 0x5555_4444u, esp = 0xEFFCu)
        execute { assemble(instructionPush) }
        assertAssembly(instructionPush)
        assertGPRRegisters(eax = 0x5555_4444u, esp = 0xEFF8u)

        gprRegisters(esp = 0xEFF8u, eip = 2u)
        val instruction = "retf 0x2"
        val insnString = "ret 0x02"
        execute(0x443Fu) { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(esp = 0xEFFEu)
        assertSegmentRegisters(ds = 0x8u, ss = 0x8u, cs = 0x5555u)

        segmentRegisters()
        val instructionPop = "pop EAX"
        execute { assemble(instructionPop) }
        assertAssembly(instructionPop)
        assertGPRRegisters(eax = 0x1111u, esp = 0xF002u)
    }

    // TEST JA INSTRUCTION

    @Test fun jaTestrel8Positive() {
        val instruction = "ja 0x78"  // 4 byte hex
        val insnString = "ja 0x0074"
        execute(0x74u) { assemble(instruction) }
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
        execute(0x5674u) { assemble(instruction) }
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
        execute(0x5674u) { assemble(instruction) }
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
        execute(0x5674u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jbeTestrel16Positive2() {
        val instruction = "jbe 0x5678"  // 4 byte hex
        val insnString = "jbe 0x5674"
        flagRegisters(zf = true)
        execute(0x5674u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jbeTestrel16Positive3() {
        val instruction = "jbe 0x5678"  // 4 byte hex
        val insnString = "jbe 0x5674"
        flagRegisters(zf = true, cf = true)
        execute(0x5674u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JE INSTRUCTION

    @Test fun jeTestrel16Positive() {
        val instruction = "je 0x5678"  // 4 byte hex
        val insnString = "je 0x5674"
        flagRegisters(zf = true)
        execute(0x5674u) { assemble(instruction) }
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
        execute(0x5674u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jgTestrel8Positive2() {
        val instruction = "jg 0x78"  // 4 byte hex
        val insnString = "jg 0x0074"
        flagRegisters(sf = true, of = true)
        execute(0x74u) { assemble(instruction) }
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
        execute(0x5674u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jgeTestrel16Positive2() {
        val instruction = "jge 0x5678"  // 4 byte hex
        val insnString = "jge 0x5674"
        flagRegisters(sf = true, of = true)
        execute(0x5674u) { assemble(instruction) }
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
        execute(0x5674u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jlTestrel16Positive2() {
        val instruction = "jl 0x5678"  // 4 byte hex
        val insnString = "jl 0x5674"
        flagRegisters(sf = true)
        execute(0x5674u) { assemble(instruction) }
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
        execute(0x5674u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jleTestrel16Positive2() {
        val instruction = "jle 0x5678"  // 4 byte hex
        val insnString = "jle 0x5674"
        flagRegisters(zf = true, sf = true)
        execute(0x5674u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jleTestrel16Positive3() {
        val instruction = "jle 0x5678"  // 4 byte hex
        val insnString = "jle 0x5674"
        flagRegisters(zf = true, of = true, sf = true)
        execute(0x5674u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jleTestrel16Positive4() {
        val instruction = "jle 0x5678"  // 4 byte hex
        val insnString = "jle 0x5674"
        flagRegisters(zf = true)
        execute(0x5674u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jleTestrel16Positive5() {
        val instruction = "jle 0x5678"  // 4 byte hex
        val insnString = "jle 0x5674"
        flagRegisters(of = true)
        execute(0x5674u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jleTestrel16Positive6() {
        val instruction = "jle 0x5678"  // 4 byte hex
        val insnString = "jle 0x5674"
        flagRegisters(sf = true)
        execute(0x5674u) { assemble(instruction) }
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
        execute(0x5674u) { assemble(instruction) }
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
        execute(0xFAC0u) { assemble(instruction) }
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
        execute(0xFAC0u) { assemble(instruction) }
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
        execute(0xFAC0u) { assemble(instruction) }
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
        execute(0xFAC0u) { assemble(instruction) }
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
        execute(0xFAC0u) { assemble(instruction) }
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
        execute(0xFFC1u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jecxzTestrel16Negative() {
        val instruction = "jecxz 0x78"  // 3 byte hex
        val insnString = "jecxz 0x0075"
        gprRegisters(ecx = 0xF000_0000u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JCXZ INSTRUCTION

    @Test fun jcxzTestrel16Positive1() {
        val instruction = "jcxz 0xC4"  // 2 byte hex
        val insnString = "jecxz 0x00C2"
        execute(0xFFC2u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jcxzTestrel16Positive2() {
        val instruction = "jcxz 0x78"  // 4 byte hex
        val insnString = "jecxz 0x0076"
        gprRegisters(ecx = 0xF000_0000u)
        execute(0x76u) { assemble(instruction) }
        assertAssembly(insnString)
    }

    @Test fun jcxzTestrel16Negative() {
        val instruction = "jcxz 0x78"  // 4 byte hex
        val insnString = "jecxz 0x0076"
        gprRegisters(ecx = 0xF000u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
    }

    // TEST JPE INSTRUCTION

    @Test fun jpeTestrel16Positive() {
        val instruction = "jpe 0xFAC4"  // 4 byte hex
        val insnString = "jpe 0xFAC0"
        flagRegisters(pf = true)
        execute(0xFAC0u) { assemble(instruction) }
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
        x86.cpu.cregs.cr0.pe = false
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
        assertCopRegisters(int = true, irq = 0xFAu)
    }

    // TEST INT3 INSTRUCTION

    @Test fun int3Test() {
        val instruction = "int3 "
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertCopRegisters(int = true, irq = 3u)
    }

    // TEST LAHF INSTRUCTION

    @Test fun lahfTest() {
        val instruction = "lahf "
        flagRegisters(sf = true, af = true, pf = true, cf = true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x9700u, esp = 0x1000u)
    }

    // TEST LAR INSTRUCTION

    @Test fun larTestr32r32() {
        val instruction = "lar ebx, eax"
        gprRegisters(eax = 0x2u, ebx = 0x907C_FAFAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x2u, ebx = 0x0100u)
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
        x86.cpu.cregs.cr0.pe = false
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters(ifq = true)
    }

    // TEST LIDT INSTRUCTION

    @Test fun lidtTestm48() {
        val instruction = "lidt [EAX+0xFF]"
        val insnString = "lidt fword [EAX+0xFF]"
        store(startAddress + 0xF0FFu, "CA110008FFAC")
        gprRegisters(eax = 0xF000u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertCopRegisters(idtrBase = 0xFF0800u, idtrLimit = 0x11CAu, irq = -1uL)
    }

    // TEST LGDT INSTRUCTION

    @Test fun lgdtTestm48() {
        val instruction = "lgdt [EAX+0xFF]"
        val insnString = "lgdt fword [EAX+0xFF]"
        store(startAddress + 0xF0FFu, "CA110008FFAC")
        gprRegisters(eax = 0xF000u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertMMURegisters(gdtrBase = 0xFF0800u, gdtrLimit = 0x11CAu)
    }

    // TEST LLDT INSTRUCTION

    @Test fun lldtTestm16() {
        val instruction = "lldt word [EAX+0xFF]"
        store(startAddress + 0xF0FFu, 0x1A2Au, Datatype.DWORD)
        gprRegisters(eax = 0xF000u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMMURegisters(gdtrBase = 0u, gdtrLimit = 0x20u, ldtr = 0x1A2Au)
    }

    @Test fun lldtTestr16() {
        val instruction = "lldt AX"
        gprRegisters(eax = 0x1A2Au)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMMURegisters(gdtrBase = 0u, gdtrLimit = 0x20u, ldtr = 0x1A2Au)
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

    // TEST SLDT INSTRUCTION

    @Test fun sldtTestm16() {
        val instruction = "sldt word [EAX+0xFF]"
        gprRegisters(eax = 0xF000u)
        mmuRegisters(gdtrLimit = 0x20u, ldtr = 0x1234u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(startAddress + 0xF0FFu, 0x1234u, Datatype.WORD)
        assertMMURegisters(gdtrBase = 0u, gdtrLimit = 0x20u, ldtr = 0x1234u)
    }

    @Test fun sldtTestr16() {
        val instruction = "sldt AX"
        gprRegisters(eax = 0x1A2Au)
        mmuRegisters(gdtrLimit = 0x20u, ldtr = 0x1234u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x1234u)
        assertMMURegisters(gdtrBase = 0u, gdtrLimit = 0x20u, ldtr = 0x1234u)
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
        store(0x6Au, 0xACu, Datatype.BYTE, true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xACu, esp = 0x1000u)
    }

    @Test fun inTest16imm8() {
        val instruction = "in ax, 0x6A"
        store(0x6Au, 0xACCAu, Datatype.WORD, true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xACCAu, esp = 0x1000u)
    }

    @Test fun inTest32imm8() {
        val instruction = "in eax, 0xF0"
        store(0xF0u, 0x1234_ACCAu, Datatype.DWORD, true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x1234_ACCAu, esp = 0x1000u)
    }

    @Test fun inTest8DX() {
        val instruction = "in al, dx"
        gprRegisters(edx = 0xF0FFu)
        store(0xF0FFu, 0xACu, Datatype.BYTE, true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xACu, edx = 0xF0FFu)
    }

    @Test fun inTest16DX() {
        val instruction = "in ax, dx"
        gprRegisters(edx = 0xF0FFu)
        store(0xF0FFu, 0xBAACu, Datatype.WORD, true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xBAACu, edx = 0xF0FFu)
    }

    @Test fun inTest32DX() {
        val instruction = "in eax, dx"
        gprRegisters(edx = 0xF0FFu)
        store(0xF0FFu, 0xFACA_BAACu, Datatype.DWORD, true)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0xFACA_BAACu, edx = 0xF0FFu)
    }

    // TEST OUT INSTRUCTION

    @Test fun outTest8imm8() {
        val instruction = "out 0x6A, al"
        gprRegisters(eax = 0xACu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(0x6Au, 0xACu, Datatype.BYTE, true)
    }

    @Test fun outTest16imm8() {
        val instruction = "out 0x6A, ax"
        gprRegisters(eax = 0xACCAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(0x6Au, 0xACCAu, Datatype.WORD, true)
    }

    @Test fun outTest32imm8() {
        val instruction = "out 0x6A, eax"
        gprRegisters(eax = 0xACCA_FACAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(0x6Au, 0xACCA_FACAu, Datatype.DWORD, true)
    }

    @Test fun outTest8DX() {
        val instruction = "out dx, al"
        gprRegisters(edx = 0xF0FFu, eax = 0xACu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(0xF0FFu, 0xACu, Datatype.BYTE, true)
    }

    @Test fun outTest16DX() {
        val instruction = "out dx, ax"
        gprRegisters(edx = 0xF0FFu, eax = 0xACDAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(0xF0FFu, 0xACDAu, Datatype.WORD, true)
    }

    @Test fun outTest32DX() {
        val instruction = "out dx, eax"
        gprRegisters(edx = 0xF0FFu, eax = 0x1122_ACDAu)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertMemory(0xF0FFu, 0x1122_ACDAu, Datatype.DWORD, true)
    }

    // TEST CPUID INSTRUCTION

    @Test fun cpuidTest0() {
        val instruction = "cpuid "
        gprRegisters(ebx = 0xF_ACACu, ecx = 0xDA61_45CA0u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertGPRRegisters(eax = 0x1u, ebx = 0x756e6547u, edx = 0x49656e69u, ecx = 0x6c65746eu)
    }

    // Instruction not implemented
    @Test fun cpuidTestGeneral() {
        val instruction = "cpuid "
        gprRegisters(eax = 0x8u, ebx = 0xF_ACACu, ecx = 0xDA61_45CA0u)
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertTrue { x86.cpu.exception is GeneralException }
    }

    // TEST INSW INSTRUCTION

    @Test fun inswTestInc() {
        val instruction = "insw "
        val insnString = "insw word [di], dx"
        gprRegisters(edx = 0xF0FFu, edi = 0xBABAu)
        segmentRegisters(es = 0x8u)
        store(0xF0FFu, 0xFACAu, Datatype.WORD, true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertMemory(startAddress + 0xBABAu, 0xFACAu, Datatype.WORD)
        assertGPRRegisters(edx = 0xF0FFu, edi = 0xBABCu)
    }

    @Test fun inswTestDec() {
        val instruction = "insw "
        val insnString = "insw word [di], dx"
        gprRegisters(edx = 0xF0FFu, edi = 0xBABAu)
        flagRegisters(df = true)
        segmentRegisters(es = 0x8u)
        store(0xF0FFu, 0xFACAu, Datatype.WORD, true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertMemory(startAddress + 0xBABAu, 0xFACAu, Datatype.WORD)
        assertGPRRegisters(edx = 0xF0FFu, edi = 0xBAB8u)
    }

    // TEST LODS INSTRUCTION

    @Test fun lodsTest8Inc() {
        val instruction = "lodsb"
        val insnString = "lods al, byte [si]"
        store(startAddress + 0xBA22u, 0xBBu, Datatype.BYTE)
        gprRegisters(esi = 0xBA22u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBBu, esi = 0xBA23u)
    }

    @Test fun lodsTest8Dec() {
        val instruction = "lodsb"
        val insnString = "lods al, byte [si]"
        store(startAddress + 0xBA22u, 0xBBu, Datatype.BYTE)
        flagRegisters(df = true)
        gprRegisters(esi = 0xBA22u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBBu, esi = 0xBA21u)
    }

    @Test fun lodsTest16Inc() {
        val instruction = "lodsw"
        val insnString = "lods ax, word [si]"
        store(startAddress + 0xBA22u, 0xBB01u, Datatype.WORD)
        gprRegisters(esi = 0xBA22u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBB01u, esi = 0xBA24u)
    }

    @Test fun lodsTest16Dec() {
        val instruction = "lodsw"
        val insnString = "lods ax, word [si]"
        store(startAddress + 0xBA22u, 0xBB01u, Datatype.WORD)
        flagRegisters(df = true)
        gprRegisters(esi = 0xBA22u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBB01u, esi = 0xBA20u)
    }

    @Test fun lodsTest32Inc() {
        val instruction = "lodsd"
        val insnString = "lods eax, dword [si]"
        store(startAddress + 0xBA22u, 0x12AB_BB01u, Datatype.DWORD)
        gprRegisters(esi = 0xBA22u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0x12AB_BB01u, esi = 0xBA26u)
    }

    @Test fun lodsTest32Dec() {
        val instruction = "lodsd"
        val insnString = "lods eax, dword [si]"
        store(startAddress + 0xBA22u, 0x12AB_BB01u, Datatype.DWORD)
        flagRegisters(df = true)
        gprRegisters(esi = 0xBA22u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0x12AB_BB01u, esi = 0xBA1Eu)
    }

    // TEST MOVS INSTRUCTION

    @Test fun movsTest8Inc() {
        val instruction = "movsb"
        val insnString = "movs byte es:[di], byte [si]"
        store(startAddress + 0xBA22u, 0xBBu, Datatype.BYTE)
        gprRegisters(esi = 0xBA22u, edi = 0xFACCu)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(esi = 0xBA23u, edi = 0xFACDu)
        assertMemory(startAddress + 0xFACCu, 0xBBu, Datatype.BYTE)
    }

    @Test fun movsTest8Dec() {
        val instruction = "movsb"
        val insnString = "movs byte es:[di], byte [si]"
        store(startAddress + 0xBA22u, 0xBBu, Datatype.BYTE)
        flagRegisters(df = true)
        gprRegisters(esi = 0xBA22u, edi = 0xFACCu)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(esi = 0xBA21u, edi = 0xFACBu)
        assertMemory(startAddress + 0xFACCu, 0xBBu, Datatype.BYTE)
    }

    @Test fun movsTest16Inc() {
        val instruction = "movsw"
        val insnString = "movs word es:[di], word [si]"
        store(startAddress + 0xBA22u, 0xB0CCu, Datatype.WORD)
        gprRegisters(esi = 0xBA22u, edi = 0xFACCu)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(esi = 0xBA24u, edi = 0xFACEu)
        assertMemory(startAddress + 0xFACCu, 0xB0CCu, Datatype.WORD)
    }

    @Test fun movsTest16Dec() {
        val instruction = "movsw"
        val insnString = "movs word es:[di], word [si]"
        store(startAddress + 0xBA22u, 0xB0CCu, Datatype.WORD)
        flagRegisters(df = true)
        gprRegisters(esi = 0xBA22u, edi = 0xFACCu)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(esi = 0xBA20u, edi = 0xFACAu)
        assertMemory(startAddress + 0xFACCu, 0xB0CCu, Datatype.WORD)
    }

    @Test fun movsTest32Inc() {
        val instruction = "movsd"
        val insnString = "movs dword es:[di], dword [si]"
        store(startAddress + 0xBA22u, 0x1234_B0CCu, Datatype.DWORD)
        gprRegisters(esi = 0xBA22u, edi = 0xFACCu)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(esi = 0xBA26u, edi = 0xFAD0u)
        assertMemory(startAddress + 0xFACCu, 0x1234_B0CCu, Datatype.DWORD)
    }

    @Test fun movsTest32Dec() {
        val instruction = "movsd"
        val insnString = "movs dword es:[di], dword [si]"
        store(startAddress + 0xBA22u, 0x1234_B0CCu, Datatype.DWORD)
        flagRegisters(df = true)
        gprRegisters(esi = 0xBA22u, edi = 0xFACCu)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(esi = 0xBA1Eu, edi = 0xFAC8u)
        assertMemory(startAddress + 0xFACCu, 0x1234_B0CCu, Datatype.DWORD)
    }

    // TEST STOS INSTRUCTION

    @Test fun stosTest8Inc() {
        val instruction = "stosb"
        val insnString = "stos byte es:[di], al"
        gprRegisters(eax = 0xBBu, edi = 0xBA22u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBBu, edi = 0xBA23u)
        assertMemory(startAddress + 0xBA22u, 0xBBu, Datatype.BYTE)
    }

    @Test fun stosTest8Dec() {
        val instruction = "stosb"
        val insnString = "stos byte es:[di], al"
        gprRegisters(eax = 0xBBu, edi = 0xBA22u)
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xBBu, edi = 0xBA21u)
        assertMemory(startAddress + 0xBA22u, 0xBBu, Datatype.BYTE)
    }

    @Test fun stosTest16Inc() {
        val instruction = "stosw"
        val insnString = "stos word es:[di], ax"
        gprRegisters(eax = 0xAABBu, edi = 0xBA22u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xAABBu, edi = 0xBA24u)
        assertMemory(startAddress + 0xBA22u, 0xAABBu, Datatype.WORD)
    }

    @Test fun stosTest16Dec() {
        val instruction = "stosw"
        val insnString = "stos word es:[di], ax"
        gprRegisters(eax = 0xAABBu, edi = 0xBA22u)
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xAABBu, edi = 0xBA20u)
        assertMemory(startAddress + 0xBA22u, 0xAABBu, Datatype.WORD)
    }

    @Test fun stosTest32Inc() {
        val instruction = "stosd"
        val insnString = "stos dword es:[di], eax"
        gprRegisters(eax = 0xFACA_AABBu, edi = 0xBA22u)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xFACA_AABBu, edi = 0xBA26u)
        assertMemory(startAddress + 0xBA22u, 0xFACA_AABBu, Datatype.DWORD)
    }

    @Test fun stosTest32Dec() {
        val instruction = "stosd"
        val insnString = "stos dword es:[di], eax"
        gprRegisters(eax = 0xFACA_AABBu, edi = 0xBA22u)
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertGPRRegisters(eax = 0xFACA_AABBu, edi = 0xBA1Eu)
        assertMemory(startAddress + 0xBA22u, 0xFACA_AABBu, Datatype.DWORD)
    }

    // TEST CMPS INSTRUCTION

    @Test fun cmpsTest8Inc() {
        val instruction = "cmpsb"
        val insnString = "cmps byte es:[di], byte [si]"
        gprRegisters(esi = 0xBA22u, edi = 0xFACCu)
        store(startAddress + 0xBA22u, 0xAu, Datatype.BYTE)
        store(startAddress + 0xFACCu, 0xAu, Datatype.BYTE)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(zf = true, pf = true)
        assertGPRRegisters(esi = 0xBA23u, edi = 0xFACDu)
        assertMemory(startAddress + 0xBA22u, 0xAu, Datatype.BYTE)
        assertMemory(startAddress + 0xFACCu, 0xAu, Datatype.BYTE)
    }

    @Test fun cmpsTest8Dec() {
        val instruction = "cmpsb"
        val insnString = "cmps byte es:[di], byte [si]"
        gprRegisters(esi = 0xBA22u, edi = 0xFACCu)
        flagRegisters(df = true)
        store(startAddress + 0xBA22u, 0xAu, Datatype.BYTE)
        store(startAddress + 0xFACCu, 0xAu, Datatype.BYTE)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(zf = true, pf = true, df = true)
        assertGPRRegisters(esi = 0xBA21u, edi = 0xFACBu)
        assertMemory(startAddress + 0xBA22u, 0xAu, Datatype.BYTE)
        assertMemory(startAddress + 0xFACCu, 0xAu, Datatype.BYTE)
    }

    @Test fun cmpsTest16Inc() {
        val instruction = "cmpsw"
        val insnString = "cmps word es:[di], word [si]"
        gprRegisters(esi = 0xBA22u, edi = 0xFACCu)
        store(startAddress + 0xBA22u, 0x100u, Datatype.WORD)
        store(startAddress + 0xFACCu, 0xA0u, Datatype.WORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(sf = true, pf = true, cf = true)
        assertGPRRegisters(esi = 0xBA24u, edi = 0xFACEu)
        assertMemory(startAddress + 0xBA22u, 0x100u, Datatype.WORD)
        assertMemory(startAddress + 0xFACCu, 0xA0u, Datatype.WORD)
    }

    @Test fun cmpsTest16Dec() {
        val instruction = "cmpsw"
        val insnString = "cmps word es:[di], word [si]"
        gprRegisters(esi = 0xBA22u, edi = 0xFACCu)
        store(startAddress + 0xBA22u, 0x100u, Datatype.WORD)
        store(startAddress + 0xFACCu, 0xA0u, Datatype.WORD)
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(sf = true, pf = true, cf = true, df = true)
        assertGPRRegisters(esi = 0xBA20u, edi = 0xFACAu)
        assertMemory(startAddress + 0xBA22u, 0x100u, Datatype.WORD)
        assertMemory(startAddress + 0xFACCu, 0xA0u, Datatype.WORD)
    }

    @Test fun cmpsTest32Inc() {
        val instruction = "cmpsd"
        val insnString = "cmps dword es:[di], dword [si]"
        gprRegisters(esi = 0xBA22u, edi = 0xFACCu)
        store(startAddress + 0xBA22u, 0x100u, Datatype.DWORD)
        store(startAddress + 0xFACCu, 0x8000_0000u, Datatype.DWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(of = true, pf = true)
        assertGPRRegisters(esi = 0xBA26u, edi = 0xFAD0u)
        assertMemory(startAddress + 0xBA22u, 0x100u, Datatype.DWORD)
        assertMemory(startAddress + 0xFACCu, 0x8000_0000u, Datatype.DWORD)
    }

    @Test fun cmpsTest32Dec() {
        val instruction = "cmpsd"
        val insnString = "cmps dword es:[di], dword [si]"
        gprRegisters(esi = 0xBA22u, edi = 0xFACCu)
        store(startAddress + 0xBA22u, 0x100u, Datatype.DWORD)
        store(startAddress + 0xFACCu, 0x8000_0000u, Datatype.DWORD)
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(of = true, pf = true, df = true)
        assertGPRRegisters(esi = 0xBA1Eu, edi = 0xFAC8u)
        assertMemory(startAddress + 0xBA22u, 0x100u, Datatype.DWORD)
        assertMemory(startAddress + 0xFACCu, 0x8000_0000u, Datatype.DWORD)
    }

    // TEST SCAS INSTRUCTION

    @Test fun scassTest8Inc() {
        val instruction = "scasb"
        val insnString = "scas al, byte es:[di]"
        gprRegisters(edi = 0xFACCu, eax = 0xAu)
        store(startAddress + 0xFACCu, 0xAu, Datatype.BYTE)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(zf = true, pf = true)
        assertGPRRegisters(edi = 0xFACDu, eax = 0xAu)
        assertMemory(startAddress + 0xFACCu, 0xAu, Datatype.BYTE)
    }

    @Test fun scasTest8Dec() {
        val instruction = "scasb"
        val insnString = "scas al, byte es:[di]"
        gprRegisters(edi = 0xFACCu, eax = 0xAu)
        flagRegisters(df = true)
        store(startAddress + 0xFACCu, 0xAu, Datatype.BYTE)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(zf = true, pf = true, df = true)
        assertGPRRegisters(edi = 0xFACBu, eax = 0xAu)
        assertMemory(startAddress + 0xFACCu, 0xAu, Datatype.BYTE)
    }

    @Test fun scasTest16Inc() {
        val instruction = "scasw"
        val insnString = "scas ax, word es:[di]"
        gprRegisters(edi = 0xFACCu, eax = 0xA0u)
        store(startAddress + 0xFACCu, 0x100u, Datatype.WORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(sf = true, pf = true, cf = true)
        assertGPRRegisters(edi = 0xFACEu, eax = 0xA0u)
        assertMemory(startAddress + 0xFACCu, 0x100u, Datatype.WORD)
    }

    @Test fun scasTest16Dec() {
        val instruction = "scasw"
        val insnString = "scas ax, word es:[di]"
        gprRegisters(edi = 0xFACCu, eax = 0xA0u)
        store(startAddress + 0xFACCu, 0x100u, Datatype.WORD)
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(sf = true, pf = true, cf = true, df = true)
        assertGPRRegisters(edi = 0xFACAu, eax = 0xA0u)
        assertMemory(startAddress + 0xFACCu, 0x100u, Datatype.WORD)
    }

    @Test fun scasTest32Inc() {
        val instruction = "scasd"
        val insnString = "scas eax, dword es:[di]"
        gprRegisters(edi = 0xFACCu, eax = 0x8000_0000u)
        store(startAddress + 0xFACCu, 0x100u, Datatype.DWORD)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(of = true, pf = true)
        assertGPRRegisters(eax = 0x8000_0000u, edi = 0xFAD0u)
        assertMemory(startAddress + 0xFACCu, 0x100u, Datatype.DWORD)
    }

    @Test fun scasTest32Dec() {
        val instruction = "scasd"
        val insnString = "scas eax, dword es:[di]"
        gprRegisters(edi = 0xFACCu, eax = 0x8000_0000u)
        store(startAddress + 0xFACCu, 0x100u, Datatype.DWORD)
        flagRegisters(df = true)
        execute { assemble(instruction) }
        assertAssembly(insnString)
        assertFlagRegisters(of = true, pf = true, df = true)
        assertGPRRegisters(eax = 0x8000_0000u, edi = 0xFAC8u)
        assertMemory(startAddress + 0xFACCu, 0x100u, Datatype.DWORD)
    }

    // TEST LOOP INSTRUCTION

    @Test fun loopTestJmp() {
        gprRegisters(ecx = 0x2u)
        val instructionLoop = "loop 0x3"
        val insnStringLoop = "loop 0x0001" // hex 2 byte
        execute(1u) { assemble(instructionLoop) }
        assertAssembly(insnStringLoop)
        assertGPRRegisters(ecx = 0x1u)
    }

    @Test fun loopTestNoJmp() {
        gprRegisters(ecx = 0x1u)
        val instructionLoop = "loop 0x3"
        val insnStringLoop = "loop 0x0001" // hex 2 byte
        execute { assemble(instructionLoop) }
        assertAssembly(insnStringLoop)
    }

    @Test fun loopnzTestPositive() {
        gprRegisters(ecx = 0x2u)
        val instructionLoop = "loopnz 0x3"
        val insnStringLoop = "loopnz 0x0001"
        execute(1u) { assemble(instructionLoop) }
        assertAssembly(insnStringLoop)
        assertGPRRegisters(ecx = 0x1u)
    }

    @Test fun loopnzTestNegative() {
        gprRegisters(ecx = 0x2u)
        flagRegisters(zf = true)
        val instructionLoop = "loopnz 0x00FF"
        val insnStringLoop = "loopnz 0x00FD"
        execute { assemble(instructionLoop) }
        assertAssembly(insnStringLoop)
        assertGPRRegisters(ecx = 0x1u)
    }

    @Test fun loopzTestPositive() {
        gprRegisters(ecx = 0x2u)
        flagRegisters(zf = true)
        val instructionLoop = "loopz 0x00FF"
        val insnStringLoop = "loopz 0x00FD"
        execute(253u) { assemble(instructionLoop) }
        assertAssembly(insnStringLoop)
        assertGPRRegisters(ecx = 0x1u)
    }

    @Test fun loopzTestNegative() {
        gprRegisters(ecx = 0x2u)
        val instructionLoop = "loopz 0x00FF"
        val insnStringLoop = "loopz 0x00FD"
        execute { assemble(instructionLoop) }
        assertAssembly(insnStringLoop)
        assertGPRRegisters(ecx = 0x1u)
    }

    // TEST IRET INSTRUCTION

    @Test fun iretTest1() {
        val instructionPushFlag = "push AX"
        gprRegisters(eax = 0x887u)
        execute(-1uL) { assemble(instructionPushFlag) }
        assertAssembly(instructionPushFlag)  // flags cf, pf, sf, of
        assertGPRRegisters(eax = 0x887u, esp = 0xFFFEu)

        val instructionPushEipCs = "push EAX"
        gprRegisters(eax = 0x3u, esp = 0xFFFEu)
        execute { assemble(instructionPushEipCs) }
        assertAssembly(instructionPushEipCs)
        assertGPRRegisters(eax = 0x3u, esp = 0xFFFAu)

        val instruction = "iret "
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters(pf = true, sf = true, of = true, cf = true)
    }

    @Test fun iretTest2() {
        val instructionPushFlag = "push AX"
        gprRegisters(eax = 0x887u)
        execute(-1uL) { assemble(instructionPushFlag) }
        assertAssembly(instructionPushFlag)  // flags cf, pf, sf, of
        assertGPRRegisters(eax = 0x887u, esp = 0xFFFEu)

        val instructionPushEipCs = "push EAX"
        gprRegisters(eax = 0x5_0003u, esp = 0xFFFEu)
        execute { assemble(instructionPushEipCs) }
        assertAssembly(instructionPushEipCs)
        assertGPRRegisters(eax = 0x5_0003u, esp = 0xFFFAu)

        val instruction = "iret "
        execute { assemble(instruction) }
        assertAssembly(instruction)
        assertFlagRegisters(pf = true, sf = true, of = true, cf = true)
    }
}
