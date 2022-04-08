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
package ru.inforion.lab403.kopycat.cores.v850es.instructions

import ru.inforion.lab403.kopycat.interfaces.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.v850es.enums.CONDITION
import ru.inforion.lab403.kopycat.modules.cores.v850ESCore
import ru.inforion.lab403.kopycat.modules.memory.RAM
import java.nio.ByteOrder.LITTLE_ENDIAN


class V850ESInstructionsTest: Module(null, "v850esInstructionTest") {
    private val v850es = v850ESCore(this, "v850ESCore", 20.MHz)
    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem")
    }
    inner class Ports : ModulePorts(this) {  }
    override val ports = Ports()
    override val buses = Buses()

    val ram0 = RAM(this, "ram0", 0xF_FFFF)
    val ram1 = RAM(this, "ram1", 0xFFFF)

    init {
        v850es.ports.mem.connect(buses.mem)
        ram0.ports.mem.connect(buses.mem)
        ram1.ports.mem.connect(buses.mem, 0xFFFF_0000u)
//        this.ports.mem.connect(buses.mem)
        initializeAndResetAsTopInstance()
    }

    private var size = 0
    private val startAddress = 0uL

    private fun execute(offset: Int = 0, generate: () -> ByteArray) {
        val data = generate()
        v850es.store(startAddress + size.uint, data)
        v850es.step()
        println("%16s -> %s".format(data.hexlify(), v850es.cpu.insn))
        size += data.size + offset
    }

    private fun assertAssembly(expected: String) = assertEquals("Unexpected disassembly view!", expected, v850es.cpu.insn.toString())

    private fun assertFlags(cy: Int, ov: Int, s: Int, z: Int) {
        assertEquals("${v850es.cpu.insn} -> Carry flag error", cy.truth, v850es.cpu.flags.cy)
        assertEquals("${v850es.cpu.insn} -> Overflow flag error", ov.truth, v850es.cpu.flags.ov)
        assertEquals("${v850es.cpu.insn} -> Sign flag error", s.truth, v850es.cpu.flags.s)
        assertEquals("${v850es.cpu.insn} -> Zero flag error", z.truth, v850es.cpu.flags.z)
    }

    private fun flags(cy: Int? = null, ov: Int? = null, s: Int? = null, z: Int? = null) {
        if (cy != null) v850es.cpu.flags.cy = cy.truth
        if (ov != null) v850es.cpu.flags.ov = ov.truth
        if (s != null) v850es.cpu.flags.s = s.truth
        if (z != null) v850es.cpu.flags.z = z.truth
    }

    private fun assertRegister(num: Int, expected: ULong, actual: ULong, type: String = "GPR") =
            assertEquals("${v850es.cpu.insn} -> $type r$num error: 0x${expected.hex8} != 0x${actual.hex8}", expected, actual)

    private fun assertRegisters(
            r0: ULong = 0u, r1: ULong = 0u, r2: ULong = 0u, r3: ULong = 0u, r4: ULong = 0u, r5: ULong = 0u, r6: ULong = 0u, r7: ULong = 0u,
            r8: ULong = 0u, r9: ULong = 0u, r10: ULong = 0u, r11: ULong = 0u, r12: ULong = 0u, r13: ULong = 0u, r14: ULong = 0u, r15: ULong = 0u,
            r16: ULong = 0u, r17: ULong = 0u, r18: ULong = 0u, r19: ULong = 0u, r20: ULong = 0u, r21: ULong = 0u, r22: ULong = 0u, r23: ULong = 0u,
            r24: ULong = 0u, r25: ULong = 0u, r26: ULong = 0u, r27: ULong = 0u, r28: ULong = 0u, r29: ULong = 0u, r30: ULong = 0u, r31: ULong = 0u) {
        assertRegister(0, r0, v850es.cpu.regs.r0Zero)
        assertRegister(1, r1, v850es.cpu.regs.r1AssemblerReserved)
        assertRegister(2, r2, v850es.cpu.regs.r2)
        assertRegister(3, r3, v850es.cpu.regs.r3StackPointer)
        assertRegister(4, r4, v850es.cpu.regs.r4GlobalPointer)
        assertRegister(5, r5, v850es.cpu.regs.r5TextPointer)
        assertRegister(6, r6, v850es.cpu.regs.r6)
        assertRegister(7, r7, v850es.cpu.regs.r7)
        assertRegister(8, r8, v850es.cpu.regs.r8)
        assertRegister(9, r9, v850es.cpu.regs.r9)
        assertRegister(10, r10, v850es.cpu.regs.r10)
        assertRegister(11, r11, v850es.cpu.regs.r11)
        assertRegister(12, r12, v850es.cpu.regs.r12)
        assertRegister(13, r13, v850es.cpu.regs.r13)
        assertRegister(14, r14, v850es.cpu.regs.r14)
        assertRegister(15, r15, v850es.cpu.regs.r15)
        assertRegister(16, r16, v850es.cpu.regs.r16)
        assertRegister(17, r17, v850es.cpu.regs.r17)
        assertRegister(18, r18, v850es.cpu.regs.r18)
        assertRegister(19, r19, v850es.cpu.regs.r19)
        assertRegister(20, r20, v850es.cpu.regs.r20)
        assertRegister(21, r21, v850es.cpu.regs.r21)
        assertRegister(22, r22, v850es.cpu.regs.r22)
        assertRegister(23, r23, v850es.cpu.regs.r23)
        assertRegister(24, r24, v850es.cpu.regs.r24)
        assertRegister(25, r25, v850es.cpu.regs.r25)
        assertRegister(26, r26, v850es.cpu.regs.r26)
        assertRegister(27, r27, v850es.cpu.regs.r27)
        assertRegister(28, r28, v850es.cpu.regs.r28)
        assertRegister(29, r29, v850es.cpu.regs.r29)
        assertRegister(30, r30, v850es.cpu.regs.r30ElementPointer)
        assertRegister(31, r31, v850es.cpu.regs.r31LinkPointer)
    }

    private fun regs(
            r0: ULong = 0u, r1: ULong = 0u, r2: ULong = 0u, r3: ULong = 0u, r4: ULong = 0u, r5: ULong = 0u, r6: ULong = 0u, r7: ULong = 0u,
            r8: ULong = 0u, r9: ULong = 0u, r10: ULong = 0u, r11: ULong = 0u, r12: ULong = 0u, r13: ULong = 0u, r14: ULong = 0u, r15: ULong = 0u,
            r16: ULong = 0u, r17: ULong = 0u, r18: ULong = 0u, r19: ULong = 0u, r20: ULong = 0u, r21: ULong = 0u, r22: ULong = 0u, r23: ULong = 0u,
            r24: ULong = 0u, r25: ULong = 0u, r26: ULong = 0u, r27: ULong = 0u, r28: ULong = 0u, r29: ULong = 0u, r30: ULong = 0u, r31: ULong = 0u) {
        v850es.cpu.regs.r0Zero = r0
        v850es.cpu.regs.r1AssemblerReserved = r1
        v850es.cpu.regs.r2 = r2
        v850es.cpu.regs.r3StackPointer = r3
        v850es.cpu.regs.r4GlobalPointer = r4
        v850es.cpu.regs.r5TextPointer = r5
        v850es.cpu.regs.r6 = r6
        v850es.cpu.regs.r7 = r7
        v850es.cpu.regs.r8 = r8
        v850es.cpu.regs.r9 = r9

        v850es.cpu.regs.r10 = r10
        v850es.cpu.regs.r11 = r11
        v850es.cpu.regs.r12 = r12
        v850es.cpu.regs.r13 = r13
        v850es.cpu.regs.r14 = r14
        v850es.cpu.regs.r15 = r15
        v850es.cpu.regs.r16 = r16
        v850es.cpu.regs.r17 = r17
        v850es.cpu.regs.r18 = r18
        v850es.cpu.regs.r19 = r19

        v850es.cpu.regs.r20 = r20
        v850es.cpu.regs.r21 = r21
        v850es.cpu.regs.r22 = r22
        v850es.cpu.regs.r23 = r23
        v850es.cpu.regs.r24 = r24
        v850es.cpu.regs.r25 = r25
        v850es.cpu.regs.r26 = r26
        v850es.cpu.regs.r27 = r27
        v850es.cpu.regs.r28 = r28
        v850es.cpu.regs.r29 = r29

        v850es.cpu.regs.r30ElementPointer = r30
        v850es.cpu.regs.r31LinkPointer = r31
    }

    private fun cregs(
            r0eipc: ULong = 0u, r1eipsw: ULong = 0u, r2fepc: ULong = 0u, r3fepsw: ULong = 0u,
            r4ecr: ULong = 0u, r5psw: ULong = 0u, r6ctpc: ULong = 0u, r7ctpsw: ULong = 0u,
            r8dbpc: ULong = 0u, r9dbpsw: ULong = 0u, r10ctbp: ULong = 0u, r11dir: ULong = 0u) {
        v850es.cpu.cregs.eipc = r0eipc
        v850es.cpu.cregs.eipsw = r1eipsw
        v850es.cpu.cregs.fepc = r2fepc
        v850es.cpu.cregs.fepsw = r3fepsw
        v850es.cpu.cregs.ecr = r4ecr
        v850es.cpu.cregs.psw = r5psw
        v850es.cpu.cregs.ctpc = r6ctpc
        v850es.cpu.cregs.ctpsw = r7ctpsw
        v850es.cpu.cregs.dbpc = r8dbpc
        v850es.cpu.cregs.dbpsw = r9dbpsw
        v850es.cpu.cregs.ctbp = r10ctbp
        v850es.cpu.cregs.dir = r11dir
    }

    private fun assertSystemRegister(num: Int, expected: ULong, actual: ULong) = assertRegister(num, expected, actual, "SYS")

    private fun assertSystermRegisters(
            r0eipc: ULong = 0u, r1eipsw: ULong = 0u, r2fepc: ULong = 0u, r3fepsw: ULong = 0u,
            r4ecr: ULong = 0u, r5psw: ULong = 0u, r6ctpc: ULong = 0u, r7ctpsw: ULong = 0u,
            r8dbpc: ULong = 0u, r9dbpsw: ULong = 0u, r10ctbp: ULong = 0u, r11dir: ULong = 0u) {
        assertSystemRegister(0, r0eipc, v850es.cpu.cregs.eipc)
        assertSystemRegister(1, r1eipsw, v850es.cpu.cregs.eipsw)
        assertSystemRegister(2, r2fepc, v850es.cpu.cregs.fepc)
        assertSystemRegister(3, r3fepsw, v850es.cpu.cregs.fepsw)
        assertSystemRegister(4, r4ecr, v850es.cpu.cregs.ecr)
        assertSystemRegister(5, r5psw, v850es.cpu.cregs.psw)
        assertSystemRegister(6, r6ctpc, v850es.cpu.cregs.ctpc)
        assertSystemRegister(7, r7ctpsw, v850es.cpu.cregs.ctpsw)
        assertSystemRegister(8, r8dbpc, v850es.cpu.cregs.dbpc)
        assertSystemRegister(9, r9dbpsw, v850es.cpu.cregs.dbpsw)
        assertSystemRegister(10, r10ctbp, v850es.cpu.cregs.ctbp)
        assertSystemRegister(11, r11dir, v850es.cpu.cregs.dir)
    }

    private fun load(address: ULong, dtyp: Datatype): ULong = v850es.read(dtyp, address, 0)
    private fun store(address: ULong, data: ULong, dtyp: Datatype) = v850es.write(dtyp, address, data, 0)

    private fun assertMemory(address: ULong, expected: ULong, dtyp: Datatype) {
        val actual = load(address, dtyp)
        assertEquals("Memory 0x${address.hex8} error: $expected != $actual", expected, actual)
    }

    private fun toBuffer(dtyp: Datatype, data: ULong): ByteArray = ByteArray(dtyp.bytes).apply {
        putUInt(0, data, dtyp.bytes, LITTLE_ENDIAN)
    }

    enum class OPCODE(val main: Int, val sub: Int = 0, val length: Int = -1) {
        ADD_I(0b001110),
        ADD_II(0b010010),
        ADDI_VI(0b110000),
        AND_I(0b001010),
        ANDI_VI(0b110110),
        BSH_XII(0b110110),
        BCOND_III(0b1011),
        CLR1_VIII(0b111110, 0b10),
        CLR1_IX(0b111111, 0b0000000011100100),
        CMOV_XI(0b111111, 0b011001),
        CMP_I(0b001111),
        CMP_II(0b010011),
        DISPOSE_XIII(0b11001),
        DIV_XI(0b111111, 0b010110),
        LDB_VII(0b111000),
        LDH_VII(0b111001, 0),
        LDW_VII(0b111001, 1),
        LDBU_VII(0b111100),
        LDHU_VII(0b111111),
        LDSR_IX(0b111111, 0b0000000000100000),
        NOT_I(0b000001),
        NOT1_VIII(0b111110, 0b01),
        NOT1_IX(0b111111, 0b0000000011100010),
        OR_I(0b001000),
        ORI_VI(0b110100),
        PREPARE_XIII(0b11110),
        SET1_VIII(0b111110, 0b00),
        SET1_IX(0b111111, 0b0000000011100000),
        SLDB_IV(0b0110, 0, 4),
        SLDH_IV(0b1000, 0, 4),
        SLDW_IV(0b1010, 0, 4),
        SLDBU_IV(0b0000110, 0, 7),
        SLDHU_IV(0b0000111, 0, 7),
        SSTB_IV(0b0111, 0, 4),
        SSTH_IV(0b1001, 0, 4),
        SSTW_IV(0b1010, 1, 4),
        STB_VII(0b111010),
        STH_VII(0b111011, 0),
        STW_VII(0b111011, 1),
        SUB_I(0b001101),
        SUBR_I(0b001100),
        SXB_I(0b000101),
        SXH_I(0b000111),
        TST1_VIII(0b111110, 0b11),
        TST1_IX(0b111111, 0b0000000011100110),
        STSR_IX(0b111111, 0b0000000001000000),
        XORI_VI(0b110101),
        XOR_I(0b001001),
        ZXB_I(0b00000000100),
        ZXH_I(0b00000000110)
    }

    // R - 1-bit data of code specifying reg1 or regID
    // r - 1-bit data of code specifying reg2
    // w - 1-bit data of code specifying reg3
    // d - 1-bit data of displacement
    // I - 1-bit data of immediate (indicates higher bits of immediate)
    // i - 1-bit data of immediate
    // bbb - 3-bit data for bit number specification

    private fun formatI(opcode: OPCODE, reg1: Int, reg2: Int): ByteArray = toBuffer(WORD,
            insert(opcode.main, 10..5)
                    .insert(reg1, 4..0) // R
                    .insert(reg2, 15..11)  // r
                    .ulong_z)

    private fun formatII(opcode: OPCODE, imm5: Int, reg2: Int): ByteArray = toBuffer(WORD,
            insert(opcode.main, 10..5)
                    .insert(imm5, 4..0)  // i
                    .insert(reg2, 15..11)  // r
                    .ulong_z)

    private fun formatIII(opcode: OPCODE, cond: CONDITION, disp9: Int): ByteArray = toBuffer(WORD,
            insert(opcode.main, 10..7)
                    .insert(cond.bits, 3..0)  // CCCC
                    .insert((disp9 ushr 1)[2..0], 6..4)  // ddd
                    .insert((disp9 ushr 1)[9..3], 15..11)  // ddddd
                    .ulong_z)

    private fun formatIV(opcode: OPCODE, disp: Int, reg2: Int): ByteArray {
        return toBuffer(WORD, insert(opcode.main, 10..(10 - opcode.length + 1))
                .insert(reg2, 15..11)  // RRRR
                .insert(disp, (10 - opcode.length)..0)  // ddddd
                .ulong_z)
    }

    private fun formatVI(opcode: OPCODE, imm16: Int, reg1: Int, reg2: Int): ByteArray = toBuffer(DWORD,
            insert(opcode.main, 10..5)
                    .insert(reg1, 4..0)  // R
                    .insert(reg2, 15..11)  // r
                    .insert(imm16, 31..16)  // i
                    .ulong_z)

    private fun formatVII(opcode: OPCODE, disp16: Int, reg1: Int, reg2: Int): ByteArray = toBuffer(DWORD,
            insert(opcode.main, 10..5)
                    .insert(reg1, 4..0)  // R
                    .insert(reg2, 15..11)  // r
                    .insert(disp16, 31..16)  // dddddddddddddddd
                    .ulong_z)

    private fun formatVIII(opcode: OPCODE, bit3: Int, reg1: Int, disp16: Int): ByteArray = toBuffer(DWORD,
            insert(opcode.main, 10..5)
                    .insert(opcode.sub, 15..14)
                    .insert(bit3, 13..11)  // bbb
                    .insert(reg1, 4..0)  // RRRRR
                    .insert(disp16, 31..16)  // dddddddddddddddd
                    .ulong_z)

    private fun formatIX(opcode: OPCODE, reg1: Int, reg2: Int): ByteArray = toBuffer(DWORD,
            insert(opcode.main, 10..5)
                    .insert(reg1, 4..0)  // R or regId
                    .insert(reg2, 15..11)  // r
                    .insert(opcode.sub, 31..16)  // dddddddddddddddd
                    .ulong_z)

    private fun formatXI(opcode: OPCODE, cond: CONDITION, reg1: Int, reg2: Int, reg3: Int): ByteArray = toBuffer(DWORD,
            insert(opcode.main, 10..5)
                    .insert(cond.bits, 20..17) // cccc
                    .insert(reg1, 4..0)  // R or regId
                    .insert(reg2, 15..11)  // r
                    .insert(reg3, 31..27)  // w
                    .insert(opcode.sub, 26..21)  //
                    .ulong_z)

    private fun formatXIII(opcode: OPCODE, imm: Int, reg1: Int, list1: Int, list: Int, imm16: Int = -1): ByteArray {
        val data = insert(opcode.main, 10..6)
                .insert(imm, 5..1)
                .insert(reg1, 20..16)
                .insert(list1, 0)
                .insert(list, 31..21)

        if (imm16 > 0)
            if (imm16 > 0xFFFF)
                data.insert(imm16, 63..32)
            else
                data.insert(imm16, 47..32)

        return toBuffer(DWORD, data.ulong_z)
    }

    @Before fun resetTest() {
        v850es.reset()
        v850es.cpu.pc = startAddress
    }

    @After fun checkPC() {
        assertEquals("Program counter error: ${(startAddress + size).hex8} != ${v850es.cpu.pc.hex8}",
                startAddress + size, v850es.cpu.pc)
    }

    @Test fun addFormatITestSimple() {
        regs(r11 = 0xDCBAu, r12 = 0xAAAAu)
        execute { formatI(OPCODE.ADD_I, 11, 12) }
        assertAssembly("add r11, r12")
        assertRegisters(r11 = 0x0DCBAu, r12 = 0x18764u)
        assertFlags(0, 0, 0, 0)
    }

    @Test fun addFormatITestFlags() {
        regs(r11 = 0xAAAA_AAAAu, r12 = 0xBBBB_BBBBu)
        execute { formatI(OPCODE.ADD_I, 11, 12) }
        assertAssembly("add r11, r12")
        assertRegisters(r11 = 0xAAAA_AAAAu, r12 = 0x6666_6665u)
        assertFlags(1, 1, 0, 0)
    }

    @Test fun addFormatIITestPosImm() {
        regs(r5 = 0x7FFF_FFF1u)
        execute { formatII(OPCODE.ADD_II, 0xF, 5) }
        assertAssembly("add 0xF, r5")
        assertRegisters(r5 = 0x8000_0000u)
        assertFlags(0, 1, 1, 0)
    }

    @Test fun addFormatIITestNegImm() {
        regs(r5 = 0x1u)
        execute { formatII(OPCODE.ADD_II, 0x1F, 5) }
        assertAssembly("add -0x1, r5")
        assertRegisters(r5 = 0u)
        assertFlags(1, 0, 0, 1)
    }

    @Test fun addiTestLower20h() {
        flags(1)
        regs(r5 = 0x0u)
        execute { formatVI(OPCODE.ADDI_VI, 0xFFE0, 5, 0) }
        assertAssembly("addi r5, r0, -0x20")
        assertRegisters() // all must be equal zero
        assertFlags(0, 0, 1, 0)
    }

    @Test fun addiTestGreater20h() {
        flags(0)
        regs(r5 = 0x21u)
        execute { formatVI(OPCODE.ADDI_VI, 0xFFE0, 5, 0) }
        assertAssembly("addi r5, r0, -0x20")
        assertRegisters(r5 = 0x21u)
        assertFlags(1, 0, 0, 0)
    }

    @Test fun andTestSign() {
        regs(r20 = 0xDCCD_ABBAu, r21 = 0xCDDC_BAABu)
        execute { formatI(OPCODE.AND_I, 20, 21) }
        assertAssembly("and r20, r21")
        assertRegisters(r20 = 0xDCCD_ABBAu, r21 = 0xCCCC_AAAAu)
        assertFlags(0, 0, 1, 0)
    }

    @Test fun andTestZero() {
        regs(r20 = 0x0101_0101u, r21 = 0x1010_1010u)
        execute { formatI(OPCODE.AND_I, 20, 21) }
        assertAssembly("and r20, r21")
        assertRegisters(r20 = 0x0101_0101u, r21 = 0u)
        assertFlags(0, 0, 0, 1)
    }

    @Test fun andiTest() {
        flags(1, 1, 1, 1)
        regs(r15 = 0xF0F0_F0F0u, r16 = 0x0F0F_0F0Fu)
        execute { formatVI(OPCODE.ANDI_VI, 0x0010, 15, 16) }
        assertAssembly("andi r15, r16, 0x10")
        assertRegisters(r15 = 0xF0F0_F0F0u, r16 = 0x10u)
        assertFlags(1, 0, 0, 0) // cy not affected!
    }

    @Test fun bgeTest() {
        // jump 1
        flags(0, 0, 0, 0)
        execute(offset = 0x72) { formatIII(OPCODE.BCOND_III, CONDITION.BGE, 0x74) }
        assertAssembly("bcond 0x74, 0xE")
        // no jump
        flags(0, 0, 1, 0)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BGE, 0x74) }
        assertAssembly("bcond 0x74, 0xE")
    }

    @Test fun bgtTest() {
        // jump 1
        flags(0, 1, 1, 0)
        execute(offset = 0xC8) { formatIII(OPCODE.BCOND_III, CONDITION.BGT, 0xCA) }
        assertAssembly("bcond 0xCA, 0xF")
        // no jump 1
        flags(0, 0, 0, 1)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BGT, 0xCA) }
        assertAssembly("bcond 0xCA, 0xF")
        // no jump 2
        flags(0, 1, 0, 0)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BGT, 0xCA) }
        assertAssembly("bcond 0xCA, 0xF")
    }

    @Test fun bleTest() {
        // jump 1
        flags(0, 1, 1, 1)
        execute(offset = 0xC8) { formatIII(OPCODE.BCOND_III, CONDITION.BLE, 0xCA) }
        assertAssembly("bcond 0xCA, 0x7")
        // jump 2
        flags(0, 1, 0, 0)
        execute(offset = 0xC8) { formatIII(OPCODE.BCOND_III, CONDITION.BLE, 0xCA) }
        assertAssembly("bcond 0xCA, 0x7")
        // no jump
        flags(0, 1, 1, 0)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BLE, 0xCA) }
        assertAssembly("bcond 0xCA, 0x7")
    }

    @Test fun bltTest() {
        // jump 1
        flags(0, 1, 0, 0)
        execute(offset = 0x72) { formatIII(OPCODE.BCOND_III, CONDITION.BLT, 0x74) }
        assertAssembly("bcond 0x74, 0x6")
        // no jump
        flags(0, 1, 1, 0)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BLT, 0x74) }
        assertAssembly("bcond 0x74, 0x6")
    }

    @Test fun bhTest() {
        // jump 1
        flags(0, 0, 0, 0)
        execute(offset = 0x72) { formatIII(OPCODE.BCOND_III, CONDITION.BH, 0x74) }
        assertAssembly("bcond 0x74, 0xB")
        // no jump 1
        flags(0, 0, 0, 1)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BH, 0x74) }
        assertAssembly("bcond 0x74, 0xB")
        // no jump 2
        flags(1, 0, 0, 0)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BH, 0x74) }
        assertAssembly("bcond 0x74, 0xB")
    }

    @Test fun bnhTest() {
        // jump 1
        flags(0, 0, 0, 1)
        execute(offset = 0x72) { formatIII(OPCODE.BCOND_III, CONDITION.BNH, 0x74) }
        assertAssembly("bcond 0x74, 0x3")
        // jump 2
        flags(1, 0, 0, 0)
        execute(offset = 0x72) { formatIII(OPCODE.BCOND_III, CONDITION.BNH, 0x74) }
        assertAssembly("bcond 0x74, 0x3")
        // no jump
        flags(0, 0, 0, 0)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BNH, 0x74) }
        assertAssembly("bcond 0x74, 0x3")
    }

    @Test fun bnlTest() { // BNC
        // jump
        flags(0, 1, 1, 1)
        execute(offset = 0xFA) { formatIII(OPCODE.BCOND_III, CONDITION.BNL, 0xFD) }
        assertAssembly("bcond 0xFC, 0x9")
        // no jump
        flags(1, 0, 0, 0)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BNL, 0xFD) }
        assertAssembly("bcond 0xFC, 0x9")
    }

    @Test fun blTest() { // BC
        // jump
        flags(1, 1, 0, 0)
        execute(offset = 0x72) { formatIII(OPCODE.BCOND_III, CONDITION.BL, 0x74) }
        assertAssembly("bcond 0x74, 0x1")
        // no jump
        flags(0, 1, 1, 0)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BL, 0x74) }
        assertAssembly("bcond 0x74, 0x1")
    }

    @Test fun beTest() { // BZ
        // jump
        flags(0, 0, 0, 1)
        execute(offset = 0x72) { formatIII(OPCODE.BCOND_III, CONDITION.BE, 0x74) }
        assertAssembly("bcond 0x74, 0x2")
        // no jump
        flags(0, 0, 0, 0)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BE, 0x74) }
        assertAssembly("bcond 0x74, 0x2")
    }

    @Test fun bneTest() { // BNZ
        // jump
        flags(0, 0, 0, 0)
        execute(offset = 0x72) { formatIII(OPCODE.BCOND_III, CONDITION.BNE, 0x74) }
        assertAssembly("bcond 0x74, 0xA")
        // no jump
        flags(0, 0, 0, 1)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BNE, 0x74) }
        assertAssembly("bcond 0x74, 0xA")
    }

    @Test fun bnTest() {
        // jump
        flags(0, 0, 1, 0)
        execute(offset = 0x72) { formatIII(OPCODE.BCOND_III, CONDITION.BN, 0x74) }
        assertAssembly("bcond 0x74, 0x4")
        // no jump
        flags(0, 0, 0, 0)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BN, 0x74) }
        assertAssembly("bcond 0x74, 0x4")
    }

    @Test fun bpTest() {
        // jump
        flags(0, 0, 0, 0)
        execute(offset = 0x72) { formatIII(OPCODE.BCOND_III, CONDITION.BP, 0x74) }
        assertAssembly("bcond 0x74, 0xC")
        // no jump
        flags(0, 0, 1, 0)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BP, 0x74) }
        assertAssembly("bcond 0x74, 0xC")
    }

    @Test fun bnvTest() {
        // jump
        flags(0, 0, 0, 0)
        execute(offset = 0x72) { formatIII(OPCODE.BCOND_III, CONDITION.BNV, 0x74) }
        assertAssembly("bcond 0x74, 0x8")
        // no jump
        flags(0, 1, 0, 0)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BNV, 0x74) }
        assertAssembly("bcond 0x74, 0x8")
    }

    @Test fun bvTest() {
        // jump
        flags(0, 1, 0, 0)
        execute(offset = 0x72) { formatIII(OPCODE.BCOND_III, CONDITION.BV, 0x74) }
        assertAssembly("bcond 0x74, 0x0")
        // no jump
        flags(0, 0, 0, 0)
        execute(offset = 0x0) { formatIII(OPCODE.BCOND_III, CONDITION.BV, 0x74) }
        assertAssembly("bcond 0x74, 0x0")
    }

    @Test fun cmpFormatITestBelow() {
        flags(0, 0, 0, 0)
        regs(r22 = 0xFFFF_0001u, r23 = 0xFFFF_0000u)
        execute { formatI(OPCODE.CMP_I, 22, 23) }
        assertAssembly("cmp r22, r23")
        assertRegisters(r22 = 0xFFFF_0001u, r23 = 0xFFFF_0000u)
        assertFlags(1, 0, 1, 0)
    }

    @Test fun cmpFormatITestAbove() {
        flags(0, 0, 0, 0)
        regs(r22 = 0xFFFF_0000u, r23 = 0xFFFF_0001u)
        execute { formatI(OPCODE.CMP_I, 22, 23) }
        assertAssembly("cmp r22, r23")
        assertRegisters(r22 = 0xFFFF_0000u, r23 = 0xFFFF_0001u)
        assertFlags(0, 0, 0, 0)
    }

    @Test fun cmpFormatIITestZero() {
        regs(r6 = -5uL)
        flags(0, 0, 0, 0)
        execute { formatII(OPCODE.CMP_II, -5, 6) }
        assertAssembly("cmp -0x5, r6")
        assertRegisters(r6 = 0xFFFFFFFBu)
        assertFlags(1, 0, 0, 1)
    }

    @Test fun clr1FormatVIIITestZero() {
        flags(z = 0)
        store(0xFFFF_FAC0u, 0xFFu, BYTE)
        execute { formatVIII(OPCODE.CLR1_VIII, 3, 0, 0xFAC0) }
        assertAssembly("clr1 byte [r0-0x540], 0x3")
        assertMemory(0xFFFF_FAC0u, 0xF7u, BYTE)
        assertRegisters() // check all registers on zero
        assertFlags(0, 0, 0, 1)
    }

    @Test fun clr1FormatVIIITest() {
        flags(z = 0)
        regs(r1 = 0x100u)
        store(0x7AC0u, 0xFFu, BYTE)
        execute { formatVIII(OPCODE.CLR1_VIII, 2, 1, 0x79C0) }
        assertAssembly("clr1 byte [r1+0x79C0], 0x2")
        assertMemory(0x7AC0u, 0xFBu, BYTE)
        assertRegisters(r1 = 0x100u)
        assertFlags(0, 0, 0, 1)
    }

    @Test fun clr1FormatIXTest() {
        flags(z = 0)
        regs(r1 = 0xBABAu, r2 = 0xFFFF_FFFFu)  // check that last 3 bits or r2 used
        store(0xBABAu, 0xFFu, BYTE)
        execute { formatIX(OPCODE.CLR1_IX, 1, 2) }
        assertAssembly("clr1 byte [r1], r2")
        assertMemory(0xBABAu, 0x7Fu, BYTE)
        assertRegisters(r1 = 0xBABAu, r2 = 0xFFFF_FFFFu)
        assertFlags(0, 0, 0, 1)
    }

    @Test fun divTest() {
        regs(r1 = 0x19u, r2 = 0xD_1107u)
        execute { formatXI(OPCODE.DIV_XI, CONDITION.NONE, 1, 2, 3) }
        assertAssembly("div r1, r2, r3")
        assertRegisters(r1 = 0x19u, r2 = 0x85CDu, r3 = 0x2u)
        assertFlags(0, 0, 0, 0)
    }

    @Test fun divFlagTest() {
        // test zero
        regs(r1 = 0x1u)
        execute { formatXI(OPCODE.DIV_XI, CONDITION.NONE, 1, 0, 0) }
        assertAssembly("div r1, r0, r0")
        assertFlags(0, 0, 0, 1)
    }

    @Test fun divTestNegative() {
        // test negative
        flags(z = 1, s = 0)
        regs(r1 = 0x19u, r2 = 0xFFF8_5690u)
        execute { formatXI(OPCODE.DIV_XI, CONDITION.NONE, 1, 2, 3) }
        assertAssembly("div r1, r2, r3")
        assertRegisters(r1 = 0x19u, r2 = 0xFFFF_B18Bu, r3 = 0xFFFF_FFFDu)
        assertFlags(0, 0, 1, 0)
    }

    @Test fun prepareTest() {
        regs(r3 = 0xFFFF_0080u, r20 = 0x20u, r21 = 0x21u, r22 = 0x22u, r23 = 0x23u, r24 = 0x24u,
                r25 = 0x25u, r26 = 0x26u, r27 = 0x27u, r28 = 0x28u, r29 = 0x29u, r30 = 0x0u, r31 = 0x31u)
        execute { formatXIII(OPCODE.PREPARE_XIII, 0x14, 3, 1, 0x7FF) }
        assertAssembly("prepare 0x14, 0xFFF, r3")

        assertMemory(0xFFFF_007Cu, 0x0u, BYTE)
        assertMemory(0xFFFF_0078u, 0x31u, BYTE)
        assertMemory(0xFFFF_0074u, 0x29u, BYTE)
        assertMemory(0xFFFF_0070u, 0x28u, BYTE)
        assertMemory(0xFFFF_006Cu, 0x23u, BYTE)
        assertMemory(0xFFFF_0068u, 0x22u, BYTE)
        assertMemory(0xFFFF_0064u, 0x21u, BYTE)
        assertMemory(0xFFFF_0060u, 0x20u, BYTE)
        assertMemory(0xFFFF_005Cu, 0x27u, BYTE)
        assertMemory(0xFFFF_0058u, 0x26u, BYTE)
        assertMemory(0xFFFF_0054u, 0x25u, BYTE)
        assertMemory(0xFFFF_0050u, 0x24u, BYTE)

        assertRegisters(r3 = 0xFFFF_0000u, r20 = 0x20u, r21 = 0x21u, r22 = 0x22u, r23 = 0x23u, r24 = 0x24u,
                r25 = 0x25u, r26 = 0x26u, r27 = 0x27u, r28 = 0x28u, r29 = 0x29u, r30 = 0xFFFF_0000u, r31 = 0x31u)
    }

    @Test fun disposeTest() {
        regs(r3 = 0xFFFF_0000u)
        (0..12).forEach { i -> store(0xFFFF_0050uL + (4u * i), 20uL + i, DWORD) }
        execute { formatXIII(OPCODE.DISPOSE_XIII, 0x14, 0, 1, 0x7FF) }
        assertAssembly("dispose 0x14, 0xFFF, r0")
        assertRegisters(r3 = 0xFFFF_0080u,
                r20 = 24u, r21 = 25u, r22 = 26u, r23 = 27u, r24 = 20u, r25 = 21u,
                r26 = 22u, r27 = 23u, r28 = 28u, r29 = 29u, r30 = 31u, r31 = 30u)
    }

    @Test fun set1FormatVIIITest() {
        flags(z = 1)
        regs(r1 = 0x400u)
        store(0x5437u, 0xAAu, BYTE)
        execute { formatVIII(OPCODE.SET1_VIII, 2, 1, 0x5037) }
        assertAssembly("set1 byte [r1+0x5037], 0x2")
        assertMemory(0x5437u, 0xAEu, BYTE)
        assertRegisters(r1 = 0x400u)
        assertFlags(0, 0, 0, 0)
    }

    @Test fun set1FormatIXTest() {
        flags(z = 1)
        regs(r1 = 0xBABAu, r2 = 0xFFFF_FFFFu)  // check that last 3 bits or r2 used
        store(0xBABAu, 0x7Fu, BYTE)
        execute { formatIX(OPCODE.SET1_IX, 1, 2) }
        assertAssembly("set1 byte [r1], r2")
        assertMemory(0xBABAu, 0xFFu, BYTE)
        assertRegisters(r1 = 0xBABAu, r2 = 0xFFFF_FFFFu)
        assertFlags(0, 0, 0, 0)
    }

    @Test fun notFormatITestPositive() {
        flags(z = 1, s = 0, ov = 1)
        regs(r1 = 0x1FFu)
        execute { formatI(OPCODE.NOT_I, 1, 2) }
        assertAssembly("not r1, r2")
        assertRegisters(r1 = 0x1FFu, r2 = 0xFFFF_FE00u)
        assertFlags(0, 0, 1, 0)
    }

    @Test fun notFormatITestNegative() {
        flags(z = 1, ov = 1)
        regs(r1 = 0xFFFF_FFFFu)
        execute { formatI(OPCODE.NOT_I, 1, 2) }
        assertAssembly("not r1, r2")
        assertRegisters(r1 = 0xFFFF_FFFFu, r2 = 0x0000_0000u)
        assertFlags(0, 0, 0, 1)
    }

    @Test fun not1FormatVIIITest() {
        flags(z = 0)
        regs(r1 = 0x400u)
        store(0x5437u, 0xDEu, BYTE)
        execute { formatVIII(OPCODE.NOT1_VIII, 2, 1, 0x5037) }
        assertAssembly("not1 byte [r1+0x5037], 0x2")
        assertMemory(0x5437u, 0xDAu, BYTE)
        assertRegisters(r1 = 0x400u)
        assertFlags(0, 0, 0, 1)
    }

    @Test fun not1FormatIXTest() {
        flags(z = 1)
        regs(r1 = 0xBABAu, r2 = 0xDEAD_BEEFu)  // check that last 3 bits or r2 used
        store(0xBABAu, 0x75u, BYTE)
        execute { formatIX(OPCODE.NOT1_IX, 1, 2) }
        assertAssembly("not1 byte [r1], r2")
        assertMemory(0xBABAu, 0xF5u, BYTE)
        assertRegisters(r1 = 0xBABAu, r2 = 0xDEAD_BEEFu)
        assertFlags(0, 0, 0, 0)
    }

    @Test fun orTestPositive() {
        flags(z = 1, s = 0, ov = 1)
        regs(r1 = 0xFFFF_EDFBu, r2 = 0x1D0Eu)
        execute { formatI(OPCODE.OR_I, 1, 2) }
        assertAssembly("or r1, r2")
        assertRegisters(r1 = 0xFFFF_EDFBu, r2 = 0xFFFF_FDFFu)
        assertFlags(0, 0, 1, 0)
    }

    @Test fun orTestNegative() {
        flags(ov = 1)
        execute { formatI(OPCODE.OR_I, 1, 0) }
        assertAssembly("or r1, r0")
        assertFlags(0, 0, 0, 1)
    }

    @Test fun oriTestPos() {
        flags(z = 1, ov = 1)
        regs(r1 = 0xFFFF_EDFBu)
        execute { formatVI(OPCODE.ORI_VI, 0x1D0E, 1, 2) }
        assertAssembly("ori r1, r2, 0x1D0E")
        assertRegisters(r1 = 0xFFFF_EDFBu, r2 = 0xFFFF_FDFFu)
        assertFlags(0, 0, 1, 0)
    }

    @Test fun oriTestNeg() {
        flags(ov = 1)
        execute { formatVI(OPCODE.ORI_VI, 0x0, 0, 1) }
        assertAssembly("ori r0, r1, 0x0")
        assertFlags(0, 0, 0, 1)
    }

    @Test fun tst1FormatVIIITest() {
        flags(z = 1)
        regs(r1 = 0x400u)
        store(0x5437u, 0xDEu, BYTE)
        execute { formatVIII(OPCODE.TST1_VIII, 2, 1, 0x5037) }
        assertAssembly("tst1 byte [r1+0x5037], 0x2")
        assertMemory(0x5437u, 0xDEu, BYTE)
        assertRegisters(r1 = 0x400u)
        assertFlags(0, 0, 0, 0)
    }

    @Test fun tst1FormatIXTest() {
        flags(z = 0)
        regs(r1 = 0xBABAu, r2 = 0xDEAD_BEEFu)  // check that last 3 bits or r2 used
        store(0xBABAu, 0x75u, BYTE)
        execute { formatIX(OPCODE.TST1_IX, 1, 2) }
        assertAssembly("tst1 byte [r1], r2")
        assertMemory(0xBABAu, 0x75u, BYTE)
        assertRegisters(r1 = 0xBABAu, r2 = 0xDEAD_BEEFu)
        assertFlags(0, 0, 0, 1)
    }

    @Test fun ldsrTest() {
        regs(r28 = 0xCAFE_AFFEu)
        cregs(r1eipsw = 0xBA_DF0Eu)
        execute { formatIX(OPCODE.LDSR_IX, 28, 1) }
        assertAssembly("ldsr r28, eipsw")
        assertRegisters(r28 = 0xCAFEAFFEu)
        assertSystermRegisters(r1eipsw = 0xCAFE_AFFEu)
    }

    @Test fun ldbTest() {
        store(0xFFFF_BEEFu, 0xAAu, BYTE)
        execute { formatVII(OPCODE.LDB_VII, 0xBEEF, 0, 1) }
        assertAssembly("ld.b r1, byte [r0-0x4111]")
        assertRegisters(r1 = 0xFFFF_FFAAu)
    }

    @Test fun ldhTest() {
        store(0xFFFF_BEEEu, 0xAA55u, WORD)
        execute { formatVII(OPCODE.LDH_VII, 0xBEEE, 0, 1) }
        assertAssembly("ld.h r1, word [r0-0x4112]")
        assertRegisters(r1 = 0xFFFF_AA55u)
    }

    @Test fun ldwTest() {
        store(0xFFFF_BEECu, 0xFFBB_AA55u, DWORD)
        execute { formatVII(OPCODE.LDW_VII, 0xBEED, 0, 1) }
        assertAssembly("ld.w r1, dword [r0-0x4114]")
        assertRegisters(r1 = 0xFFBB_AA55u)
    }

    @Test fun ldbuTest() {
        store(0xFFFF_B008u, 0xAAu, BYTE)
        execute { formatVII(OPCODE.LDBU_VII, 0xB009, 0, 1) }
        assertAssembly("ld.bu r1, byte [r0-0x4FF8]")
        assertRegisters(r1 = 0xAAu)
    }

    @Test fun ldhuTest() {
        store(0xFFFF_B008u, 0xAA55u, WORD)
        execute { formatVII(OPCODE.LDHU_VII, 0xB009, 0, 1) }
        assertAssembly("ld.hu r1, word [r0-0x4FF8]")
        assertRegisters(r1 = 0xAA55u)
    }

    @Test fun stsrTest() {
        regs(r8 = 0xCAFE_AFFEu)
        cregs(r10ctbp = 0xBA_DF0Eu)
        execute { formatIX(OPCODE.STSR_IX, 10, 8) }
        assertAssembly("stsr ctbp, r8")
        assertRegisters(r8 = 0xBA_DF0Eu)
        assertSystermRegisters(r10ctbp = 0xBA_DF0Eu)
    }

    @Test fun stbTest() {
        regs(r1 = 0xEFu)
        execute { formatVII(OPCODE.STB_VII, 0xB008, 0, 1) }
        assertAssembly("st.b r1, byte [r0-0x4FF8]")
        assertMemory(0xFFFF_B008u, 0xEFu, BYTE)
        assertRegisters(r1 = 0xEFu)
    }

    @Test fun sthTest() {
        regs(r1 = 0xBEEFu)
        execute { formatVII(OPCODE.STH_VII, 0xB008, 0, 1) }
        assertAssembly("st.h r1, word [r0-0x4FF8]")
        assertMemory(0xFFFF_B008u, 0xBEEFu, WORD)
        assertRegisters(r1 = 0xBEEFu)
    }

    @Test fun stwTest() {
        regs(r1 = 0xDEAD_BEEFu)
        execute { formatVII(OPCODE.STW_VII, 0xB009, 0, 1) }
        assertAssembly("st.w r1, dword [r0-0x4FF8]")
        assertMemory(0xFFFF_B008u, 0xDEAD_BEEFu, DWORD)
        assertRegisters(r1 = 0xDEAD_BEEFu)
    }

    @Test fun sldbTest() {
        regs(r30 = 0xFFFF_DE37u)
        store(0xFFFF_DEADu, 0xAAu, BYTE)
        execute { formatIV(OPCODE.SLDB_IV, 0x76, 1) }
        assertAssembly("sld.b r1, byte [r30+0x76]")
        assertRegisters(r1 = 0xFFFF_FFAAu, r30 = 0xFFFF_DE37u)
    }

    @Test fun sldhTest() {
        regs(r30 = 0xFFFF_AA00u)
        store(0xFFFF_AAB2u, 0xAA55u, WORD)
        execute { formatIV(OPCODE.SLDH_IV, 0x59, 1) }
        assertAssembly("sld.h r1, word [r30+0xB2]")
        assertRegisters(r1 = 0xFFFF_AA55u, r30 = 0xFFFF_AA00u)
    }

    @Test fun sldwTest() {
        regs(r30 = 0xFFFF_0000u)
        store(0xFFFF_00F0u, 0xFFBB_AA55u, DWORD)
        execute { formatIV(OPCODE.SLDW_IV, 0x78, 1) }
        assertAssembly("sld.w r1, dword [r30+0xF0]")
        assertRegisters(r1 = 0xFFBB_AA55u, r30 = 0xFFFF_0000u)
    }

    @Test fun sldbuTest() {
        regs(r30 = 0xFFFF_0000u)
        store(0xFFFF_000Bu, 0xAAu, BYTE)
        execute { formatIV(OPCODE.SLDBU_IV, 0xB, 1) }
        assertAssembly("sld.bu r1, byte [r30+0xB]")
        assertRegisters(r1 = 0xAAu, r30 = 0xFFFF_0000u)
    }

    @Test fun sldhuTest() {
        regs(r30 = 0xFFFF_FAA0u)
        store(0xFFFF_FAACu, 0xAA55u, WORD)
        execute { formatIV(OPCODE.SLDHU_IV, 0xC, 1) }
        assertAssembly("sld.hu r1, word [r30+0xC]")
        assertRegisters(r1 = 0xAA55u, r30 = 0xFFFF_FAA0u)
    }

    @Test fun sstbTest() {
        regs(r1 = 0x85u, r30 = 0xFFFF_0000u)
        execute { formatIV(OPCODE.SSTB_IV, 0x59, 1) }
        assertAssembly("sst.b r1, byte [r30+0x59]")
        assertMemory(0xFFFF_0059u, 0x85u, BYTE)
        assertRegisters(r1 = 0x85u, r30 = 0xFFFF_0000u)
    }

    @Test fun ssthTest() {
        regs(r1 = 0xAAA8u, r30 = 0xFFFF_0000u)
        execute { formatIV(OPCODE.SSTH_IV, 0x7F, 1) }
        assertAssembly("sst.h r1, word [r30+0xFE]")
        assertMemory(0xFFFF_00FEu, 0xAAA8u, WORD)
        assertRegisters(r1 = 0xAAA8u, r30 = 0xFFFF_0000u)
    }

    @Test fun sstwTest() {
        regs(r1 = 0xAAAA_BEEFu, r30 = 0xFFFF_0000u)
        execute { formatIV(OPCODE.SSTW_IV, 0x3D, 1) }
        assertAssembly("sst.w r1, dword [r30+0x78]")
        assertMemory(0xFFFF_0078u, 0xAAAA_BEEFu, DWORD)
        assertRegisters(r1 = 0xAAAA_BEEFu, r30 = 0xFFFF_0000u)
    }

    @Test fun subTestNegative() {
        flags(z = 1, ov = 1)
        regs(r1 = 0xFFF7_8902u, r2 = 0xFFF7_8901u)
        execute { formatI(OPCODE.SUB_I, 1, 2) }
        assertAssembly("sub r1, r2")
        assertRegisters(r1 = 0xFFF7_8902u, r2 = 0xFFFF_FFFFu)
        assertFlags(1, 0, 1, 0)
    }

    @Test fun subTestPositive() {
        regs(r1 = 0x5678u, r2 = 0xFFF8u)
        execute { formatI(OPCODE.SUB_I, 1, 2) }
        assertAssembly("sub r1, r2")
        assertRegisters(r1 = 0x5678u, r2 = 0xA980u)
    }

    @Test fun subrTestNegative() {
        flags(z = 1, ov = 1)
        regs(r1 = 0xFFF7_8901u, r2 = 0xFFF7_8902u)
        execute { formatI(OPCODE.SUBR_I, 1, 2) }
        assertAssembly("subr r1, r2")
        assertRegisters(r1 = 0xFFF7_8901u, r2 = 0xFFFF_FFFFu)
        assertFlags(1, 0, 1, 0)
    }

    @Test fun subrTestPositive() {
        regs(r1 = 0xFFF8u, r2 = 0x5678u)
        execute { formatI(OPCODE.SUBR_I, 1, 2) }
        assertAssembly("subr r1, r2")
        assertRegisters(r1 = 0xFFF8u, r2 = 0xA980u)
    }

    @Test fun sxbTestPositive() {
        regs(r1 = 0xFFF_8963u)
        execute { formatI(OPCODE.SXB_I, 1, 0) }
        assertAssembly("sxb r1, r0")
        assertRegisters(r1 = 0x63u)
    }

    @Test fun sxbTestNegative() {
        regs(r1 = 0xFFFF_89E3u)
        execute { formatI(OPCODE.SXB_I, 1, 0) }
        assertAssembly("sxb r1, r0")
        assertRegisters(r1 = 0xFFFF_FFE3u)
    }

    @Test fun sxhTestPositive() {
        regs(r1 = 0xFFF_8963u)
        execute { formatI(OPCODE.SXH_I, 1, 0) }
        assertAssembly("sxh r1, r0")
        assertRegisters(r1 = 0xFFFF_8963u)
    }

    @Test fun sxhTestNegative() {
        regs(r1 = 0xFFFF_89E3u)
        execute { formatI(OPCODE.SXH_I, 1, 0) }
        assertAssembly("sxh r1, r0")
        assertRegisters(r1 = 0xFFFF_89E3u)
    }

    @Test fun xoriTest() {
        flags(1)
        regs(r10 = 0xDCBAu, r11 = 0xAAAAu)
        execute { formatVI(OPCODE.XORI_VI, 0xABCD, 11, 10) }
        assertAssembly("xori r11, r10, 0xABCD")
        assertRegisters(r10 = 0x167u, r11 = 0xAAAAu)
        assertFlags(1, 0, 0, 0)
    }

    @Test fun xorTestPositive() {
        flags(1, 1, 0, 1)
        regs(r14 = 0xFAAFu, r18 = 0x550u)
        execute { formatI(OPCODE.XOR_I, 14, 18) }
        assertAssembly("xor r14, r18")
        assertRegisters(r14 = 0xFAAFu, r18 = 0xFFFFu)
        assertFlags(1, 0, 0, 0)
    }

    @Test fun xorTestNegative() {
        flags(cy = 1, ov = 1, z = 1)
        regs(r14 = 0x8000_FAAFu, r18 = 0x550u)
        execute { formatI(OPCODE.XOR_I, 14, 18) }
        assertAssembly("xor r14, r18")
        assertRegisters(r14 = 0x8000_FAAFu, r18 = 0x8000_FFFFu)
        assertFlags(1, 0, 1, 0)
    }

    @Test fun zxbTestPositive() {
        regs(r1 = 0x8963u)
        execute { formatI(OPCODE.ZXB_I, 1, 0) }
        assertAssembly("zxb r1, r0")
        assertRegisters(r1 = 0x63u)
    }

    @Test fun zxbTestNegative() {
        regs(r1 = 0xFFFF_89E3u)
        execute { formatI(OPCODE.ZXB_I, 1, 0) }
        assertAssembly("zxb r1, r0")
        assertRegisters(r1 = 0xE3u)
    }

    @Test fun zxhTestPositive() {
        regs(r1 = 0xFF_8963u)
        execute { formatI(OPCODE.ZXH_I, 1, 0) }
        assertAssembly("zxh r1, r0")
        assertRegisters(r1 = 0x8963u)
    }

    @Test fun zxhTestNegative() {
        regs(r1 = 0xFFFF_89E3u)
        execute { formatI(OPCODE.ZXH_I, 1, 0) }
        assertAssembly("zxh r1, r0")
        assertRegisters(r1 = 0x89E3u)
    }
}