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
package ru.inforion.lab403.kopycat.cores.mips.instructions

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import ru.inforion.lab403.kopycat.interfaces.*
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.LOAD
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.STORE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.modules.BUS30
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.modules.memory.RAM
import java.nio.ByteOrder.LITTLE_ENDIAN
import kotlin.test.assertTrue


class MipsInstructionsTest : Module(null, "top") {
    private val mips = MipsCore(
        this,
        "mips",
        400.MHz,
        1.0,
        1,
        0x000000A7u,
        30,
        1
    )

    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem", BUS30)
    }

    override val buses = Buses()
    private val ram0 = RAM(this, "ram0", 0x0FFF_FFFF)

    init {
        mips.ports.mem.connect(buses.mem)
        ram0.ports.mem.connect(buses.mem, 0u)
        initializeAndResetAsTopInstance()
    }

    var size = 0
    private val startAddress = 0x8000_0000uL

    private fun execute(offset: Int = 0, generate: () -> ByteArray) {
        val data = generate()
        mips.store(startAddress + size, data)
        mips.step()
        println("%16s -> %s".format(data.hexlify(), mips.cpu.insn))
        size += data.size + offset
    }

    private fun assertAssembly(expected: String) =
        Assert.assertEquals("Unexpected disassembly view!", expected, mips.cpu.insn.toString())

    private fun assertRegister(num: Int, expected: ULong, actual: ULong, type: String = "GPR") =
        Assert.assertEquals(
            "${mips.cpu.insn} -> $type $num error: 0x${expected.hex8} != 0x${actual.hex8}",
            expected,
            actual
        )

    private fun assertRegisters(
        zero: ULong = 0u, at: ULong = 0u, v0: ULong = 0u, v1: ULong = 0u, a0: ULong = 0u, a1: ULong = 0u,
        a2: ULong = 0u, a3: ULong = 0u, t0: ULong = 0u, t1: ULong = 0u, t2: ULong = 0u, t3: ULong = 0u, t4: ULong = 0u,
        t5: ULong = 0u, t6: ULong = 0u, t7: ULong = 0u, s0: ULong = 0u, s1: ULong = 0u, s2: ULong = 0u, s3: ULong = 0u,
        s4: ULong = 0u, s5: ULong = 0u, s6: ULong = 0u, s7: ULong = 0u, t8: ULong = 0u, t9: ULong = 0u, k0: ULong = 0u,
        k1: ULong = 0u, gp: ULong = 0u, sp: ULong = 0u, fp: ULong = 0u, ra: ULong = 0u
    ) {
        assertRegister(0, zero, mips.cpu.regs.zero.value)
        assertRegister(1, at, mips.cpu.regs.at.value)
        assertRegister(2, v0, mips.cpu.regs.v0.value)
        assertRegister(3, v1, mips.cpu.regs.v1.value)
        assertRegister(4, a0, mips.cpu.regs.a0.value)
        assertRegister(5, a1, mips.cpu.regs.a1.value)
        assertRegister(6, a2, mips.cpu.regs.a2.value)
        assertRegister(7, a3, mips.cpu.regs.a3.value)
        assertRegister(8, t0, mips.cpu.regs.t0.value)
        assertRegister(9, t1, mips.cpu.regs.t1.value)
        assertRegister(10, t2, mips.cpu.regs.t2.value)
        assertRegister(11, t3, mips.cpu.regs.t3.value)
        assertRegister(12, t4, mips.cpu.regs.t4.value)
        assertRegister(13, t5, mips.cpu.regs.t5.value)
        assertRegister(14, t6, mips.cpu.regs.t6.value)
        assertRegister(15, t7, mips.cpu.regs.t7.value)
        assertRegister(16, s0, mips.cpu.regs.s0.value)
        assertRegister(17, s1, mips.cpu.regs.s1.value)
        assertRegister(18, s2, mips.cpu.regs.s2.value)
        assertRegister(19, s3, mips.cpu.regs.s3.value)
        assertRegister(20, s4, mips.cpu.regs.s4.value)
        assertRegister(21, s5, mips.cpu.regs.s5.value)
        assertRegister(22, s6, mips.cpu.regs.s6.value)
        assertRegister(23, s7, mips.cpu.regs.s7.value)
        assertRegister(24, t8, mips.cpu.regs.t8.value)
        assertRegister(25, t9, mips.cpu.regs.t9.value)
        assertRegister(26, k0, mips.cpu.regs.k0.value)
        assertRegister(27, k1, mips.cpu.regs.k1.value)
        assertRegister(28, gp, mips.cpu.regs.gp.value)
        assertRegister(29, sp, mips.cpu.regs.sp.value)
        assertRegister(30, fp, mips.cpu.regs.fp.value)
        assertRegister(31, ra, mips.cpu.regs.ra.value)
    }

    private fun regs(
        at: ULong = 0u, v0: ULong = 0u, v1: ULong = 0u, a0: ULong = 0u, a1: ULong = 0u, a2: ULong = 0u, a3: ULong = 0u,
        t0: ULong = 0u, t1: ULong = 0u, t2: ULong = 0u, t3: ULong = 0u, t4: ULong = 0u, t5: ULong = 0u, t6: ULong = 0u,
        t7: ULong = 0u, s0: ULong = 0u, s1: ULong = 0u, s2: ULong = 0u, s3: ULong = 0u, s4: ULong = 0u, s5: ULong = 0u,
        s6: ULong = 0u, s7: ULong = 0u, t8: ULong = 0u, t9: ULong = 0u, k0: ULong = 0u, k1: ULong = 0u, gp: ULong = 0u,
        sp: ULong = 0u, fp: ULong = 0u, ra: ULong = 0u
    ) {
        mips.cpu.regs.at.value = at
        mips.cpu.regs.v0.value = v0
        mips.cpu.regs.v1.value = v1
        mips.cpu.regs.a0.value = a0
        mips.cpu.regs.a1.value = a1
        mips.cpu.regs.a2.value = a2
        mips.cpu.regs.a3.value = a3
        mips.cpu.regs.t0.value = t0
        mips.cpu.regs.t1.value = t1
        mips.cpu.regs.t2.value = t2
        mips.cpu.regs.t3.value = t3
        mips.cpu.regs.t4.value = t4
        mips.cpu.regs.t5.value = t5
        mips.cpu.regs.t6.value = t6
        mips.cpu.regs.t7.value = t7
        mips.cpu.regs.s0.value = s0
        mips.cpu.regs.s1.value = s1
        mips.cpu.regs.s2.value = s2
        mips.cpu.regs.s3.value = s3
        mips.cpu.regs.s4.value = s4
        mips.cpu.regs.s5.value = s5
        mips.cpu.regs.s6.value = s6
        mips.cpu.regs.s7.value = s7
        mips.cpu.regs.t8.value = t8
        mips.cpu.regs.t9.value = t9
        mips.cpu.regs.k0.value = k0
        mips.cpu.regs.k1.value = k1
        mips.cpu.regs.gp.value = gp
        mips.cpu.regs.sp.value = sp
        mips.cpu.regs.fp.value = fp
        mips.cpu.regs.ra.value = ra
    }

    private fun assertSpecialRegister(num: Int, expected: ULong, actual: ULong, type: String = "SRVC") =
        Assert.assertEquals(
            "${mips.cpu.insn} -> $type $num error: 0x${expected.hex8} != 0x${actual.hex8}",
            expected,
            actual
        )

    private fun specialRegs(hi: ULong = 0u, lo: ULong = 0u, status: ULong = 0u) {
        mips.cpu.hi = hi
        mips.cpu.lo = lo
        mips.cop.regs.Status.value = status
    }

    private fun assertSpecialRegisters(hi: ULong = 0u, lo: ULong = 0u, status: ULong = 0u) {
        assertSpecialRegister(0, hi, mips.cpu.hi)
        assertSpecialRegister(1, lo, mips.cpu.lo)
        assertSpecialRegister(2, status, mips.cop.regs.Status.value)
    }

    private fun load(address: ULong, dtyp: Datatype): ULong = mips.read(dtyp, address, 0)
    private fun store(address: ULong, data: ULong, dtyp: Datatype) = mips.write(dtyp, address, data, 0)

    private fun assertMemory(address: ULong, expected: ULong, dtyp: Datatype) {
        val actual = load(address, dtyp)
        Assert.assertEquals("Memory 0x${address.hex8} error: $expected != $actual", expected, actual)
    }

    private fun assertDelaySlot(offset: UInt = 0u, isBranch: Boolean) {
        if (isBranch) {
            regs(t7 = 0xA781_4EF2u, s7 = 0xC796_32FEu)
            execute(offset = offset.int) { rdRsRt(OPCODE.Xor, 4, 15, 23) }
            assertAssembly("xorr \$a0, \$t7, \$s7")
            assertRegisters(a0 = 0x6017_7C0Cu, t7 = 0xA781_4EF2u, s7 = 0xC796_32FEu)
        } else {
            size += 4 // For "LIKELY" instructions
            specialRegs(hi = 0x1F57_AC90u, lo = 0x2F56_AB21u)
            regs(sp = 0x2856_FE53u, v1 = 0x4563_D752u)
            execute { rsRt(OPCODE.Madd, 29, 3) }
            assertAssembly("madd \$zero, \$sp, \$v1")
            assertSpecialRegisters(hi = 0x2A46_DAAEu, lo = 0xE3E4_D6B7u)
            assertRegisters(sp = 0x2856_FE53u, v1 = 0x4563_D752u)
        }
    }

    private fun toBuffer(dtyp: Datatype, data: ULong): ByteArray = ByteArray(dtyp.bytes).apply {
        putUInt(0, data, dtyp.bytes, LITTLE_ENDIAN)
    }

    enum class OPCODE(val main: Int, val subOpcode: Int = 0, val helper: Int = 0) {
        Add(0b100000, 0, 0),
        Addi(0b1000),
        Addiu(0b1001),
        Addu(0b100001, 0, 0),
        And(0b100100, 0, 0),
        Andi(0b1100),
        Bc1f(0b1000, 0b10001),
        Beq(0b100),
        Beql(0b10100),
        Bgez(1, 1),
        Bgezal(0b10001, 1),
        Bgezall(0b10011, 1),
        Bgezl(0b11, 1),
        Bgtz(0, 0b111), // changed places main and sub opcodes like in documentation
        Bgtzl(0, 0b10111), // here too
        Blez(0, 0b110), // here too
        Blezl(0, 0b10110), // here too
        Bltz(0, 1),
        Bltzal(0b10000, 1),
        Bltzall(0b10010, 1),
        Bltzl(0b10, 1),
        Bne(0b101),
        Bnel(0b10101),
        Clo(0b100001, 0b11100, 0),
        Clz(0b100000, 0b11100, 0),
        Ctc1(0b010001, 0b00110),
        Di(0b10000, 0b1011, 0),
        Div(0b11010, 0),
        Divu(0b11011, 0),
        Ei(0b10000, 0b01011, 1),
        Ext(0, 0b11111),
        Ins(0b100, 0b11111),
        J(0b10),
        Jal(0b11),
        Jalr(0b1001, 0),
        Jr(0b1000, 0),
        Lb(0b100000),
        Lbu(0b100100),
        Lh(0b100001),
        Lhu(0b100101),
        Ll(0b110000),
        Lui(0b1111),
        Lw(0b100011),
        Lwl(0b100010),
        Lwr(0b100110),
        Madd(0, 0b11100),
        Maddu(1, 0b11100),
        Mfhi(0b10000, 0),
        Mflo(0b10010, 0),
        Movn(0b1011, 0),
        Movz(0b1010, 0),
        Msub(0b100, 0b11100),
        Msubu(0b101, 0b11100),
        Mthi(0b10001, 0),
        Mtlo(0b10011, 0),
        Mul(0b10, 0b11100),
        Mult(0b11000, 0),
        Multu(0b11001, 0),
        Nor(0b100111, 0, 0),
        Or(0b100101, 0, 0),
        Ori(0b1101),
        Rotr(0b10, 0, 1),
        Rotrv(0b110, 0, 1),
        Sb(0b101000),
        Seb(0b10000, 0b11111, 0b100000),
        Seh(0b11000, 0b11111, 0b100000),
        Sh(0b101001),
        Sll(0, 0, 0),
        Sllv(0b100, 0),
        Slt(0b101010, 0, 0),
        Slti(0b001010),
        Sltiu(0b1011),
        Sltu(0b101011, 0, 0),
        Sra(0b11, 0),
        Srav(0b111, 0),
        Srl(0b10, 0, 0),
        Srlv(0b110, 0, 0),
        Sub(0b100010, 0, 0),
        Subu(0b100011, 0, 0),
        Sw(0b101011),
        Swl(0b101010),
        Swr(0b101110),
        Wsbh(0b10, 0b11111, 0b100000),
        Xor(0b100110, 0, 0),
        Xori(0b1110)
    }

    private fun rdRsRt(opcode: OPCODE, rd: Int, rs: Int, rt: Int): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.main, 5..0)
                .insert(0, 10..6)
                .insert(opcode.helper, 6)
                .insert(rd, 15..11)
                .insert(rt, 20..16)
                .insert(rs, 25..21)
                .insert(opcode.subOpcode, 31..26)
                .ulong_z
        )

    private fun rtRsImm(opcode: OPCODE, rt: Int, rs: Int, imm: UInt): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.main, 31..26)
                .insert(rs, 25..21)
                .insert(rt, 20..16)
                .insert(imm.int, 15..0)
                .ulong_z
        )

    private fun rsRt(opcode: OPCODE, rs: Int, rt: Int): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.main, 5..0)
                .insert(0, 15..6)
                .insert(rt, 20..16)
                .insert(rs, 25..21)
                .insert(opcode.subOpcode, 31..26)
                .ulong_z
        )

    private fun rt(opcode: OPCODE, rt: Int): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.main, 31..26)
                .insert(opcode.subOpcode, 25..21)
                .insert(rt, 20..16)
                .insert(0b01100, 15..11)
                .insert(0, 10..6)
                .insert(opcode.helper, 5)
                .insert(0, 4..0)
                .ulong_z
        )

    private fun rdRt(opcode: OPCODE, rd: Int, rt: Int): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.subOpcode, 31..26)
                .insert(0, 25..21)
                .insert(rt, 20..16)
                .insert(rd, 15..11)
                .insert(opcode.main, 10..6)
                .insert(opcode.helper, 5..0)
                .ulong_z
        )

    private fun rtImm(opcode: OPCODE, rt: Int, imm: Int): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.main, 31..26)
                .insert(0, 25..21)
                .insert(rt, 20..16)
                .insert(imm, 15..0)
                .ulong_z
        )

    private fun rsRtMsbLsb(opcode: OPCODE, rt: Int, rs: Int, pos: Int, size: Int): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.subOpcode, 31..26)
                .insert(rs, 25..21)
                .insert(rt, 20..16)
                .insert(pos + size - 1, 15..11)
                .insert(pos, 10..6)
                .insert(opcode.main, 5..0)
                .ulong_z
        )

    private fun rsRtMsbdLsb(opcode: OPCODE, rt: Int, rs: Int, pos: Int, size: Int): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.subOpcode, 31..26)
                .insert(rs, 25..21)
                .insert(rt, 20..16)
                .insert(size - 1, 15..11)
                .insert(pos, 10..6)
                .insert(opcode.main, 5..0)
                .ulong_z
        )

    private fun rd(opcode: OPCODE, rd: Int): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.main, 5..0)
                .insert(0, 10..6)
                .insert(rd, 15..11)
                .insert(0, 25..16)
                .insert(opcode.subOpcode, 31..26)
                .ulong_z
        )

    private fun rs(opcode: OPCODE, rs: Int): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.subOpcode, 31..26)
                .insert(rs, 25..21)
                .insert(0, 20..6)
                .insert(opcode.main, 5..0)
                .ulong_z
        )

    private fun rtBaseOffset(opcode: OPCODE, rt: Int, base: Int, offset: UInt): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.main, 31..26)
                .insert(base, 25..21)
                .insert(rt, 20..16)
                .insert(offset.int, 15..0)
                .ulong_z
        )

    private fun rsOffset(opcode: OPCODE, rs: Int, offset: UInt): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.subOpcode, 31..26)
                .insert(rs, 25..21)
                .insert(opcode.main, 20..16)
                .insert(offset.int, 15..0)
                .ulong_z
        )

    private fun rdRtSa(opcode: OPCODE, rd: Int, rt: Int, sa: Int): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.main, 5..0)
                .insert(sa, 10..6)
                .insert(rd, 15..11)
                .insert(rt, 20..16)
                .insert(opcode.helper, 21)
                .insert(0, 25..22)
                .insert(opcode.subOpcode, 31..26)
                .ulong_z
        )

    private fun instrIndex(opcode: OPCODE, instrIndex: Int): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.main, 31..26)
                .insert(instrIndex, 25..0)
                .ulong_z
        )

    private fun rdRsHint(opcode: OPCODE, rd: Int, rs: Int): ByteArray =
        toBuffer(
            Datatype.DWORD, insert(opcode.subOpcode, 31..26)
                .insert(rs, 25..21)
                .insert(0, 20..16)
                .insert(rd, 15..11)
                .insert(0, 10..6)
                .insert(opcode.main, 5..0)
                .ulong_z
        )

    @Before
    fun resetTest() {
        mips.reset()
        mips.cpu.pc = startAddress
    }

    @After
    fun checkPC() {
        // standard exception vector address 0x80000180
        val expected = if (mips.cpu.exception == null) startAddress + size else 0x80000180uL
        Assert.assertEquals(
            "Program counter error: ${(startAddress + size).hex8} != ${mips.cpu.pc.hex8}",
            expected, mips.cpu.pc
        )
    }

    @Test
    fun addTest() {
        regs(v0 = 0xFFFE_AAAAu, v1 = 0xDCBAu)
        execute { rdRsRt(OPCODE.Add, 1, 2, 3) }
        assertAssembly("add \$at, \$v0, \$v1")
        assertRegisters(at = 0xFFFF_8764u, v0 = 0xFFFE_AAAAu, v1 = 0xDCBAu)
    }

    @Test
    fun addTestOverflow() {
        regs(v0 = 0x7FFF_FFFFu, v1 = 0x7FFF_FFFFu)
        execute(offset = -4) { rdRsRt(OPCODE.Add, 1, 2, 3) }
        assertTrue { mips.cpu.exception is MipsHardwareException.OV }
    }

    @Test
    fun addiTestPositive() {
        regs(t0 = 0xFFFF_FFFFu, t1 = 0xFFFE_BABAu)
        execute { rtRsImm(OPCODE.Addi, 8, 9, 0x7AFEu) }
        assertAssembly("addi \$t0, \$t1, 0x7AFE")
        assertRegisters(t0 = 0xFFFF_35B8u, t1 = 0xFFFE_BABAu)
    }

    @Test
    fun addiTestNegative() {
        regs(t0 = 0xFFFF_FFFFu, t1 = 0xBABAu)
        execute { rtRsImm(OPCODE.Addi, 8, 9, 0xCAFEu) }
        assertAssembly("addi \$t0, \$t1, -0x3502")
        assertRegisters(t0 = 0x85B8u, t1 = 0xBABAu)
    }

    @Test
    fun addiTestOverflow() {
        regs(t0 = 0xFFFF_FFFFu, t1 = 0x7FFF_FFFFu)
        execute(offset = -4) { rtRsImm(OPCODE.Addi, 8, 9, 0x7FFFu) }
        assertTrue { mips.cpu.exception is MipsHardwareException.OV }
    }

    @Test
    fun addiuTestPositive() {
        regs(t0 = 0xFFFF_FFFFu, t1 = 0xBABAu)
        execute { rtRsImm(OPCODE.Addiu, 8, 9, 0x7AFEu) }
        assertAssembly("addiu \$t0, \$t1, 0x7AFE")
        assertRegisters(t0 = 0x1_35B8u, t1 = 0xBABAu)
    }

    @Test
    fun addiuTestNegative() {
        regs(t0 = 0xFFFF_FFFFu, t1 = 0xBABAu)
        execute { rtRsImm(OPCODE.Addiu, 8, 9, 0xCAFEu) }
        assertAssembly("addiu \$t0, \$t1, 0xCAFE")
        assertRegisters(t0 = 0x85B8u, t1 = 0xBABAu)
    }

    @Test
    fun addiuTestOverflow() {
        regs(t0 = 0xFFFF_FFFFu, t1 = 0xFFFF_BABAu)
        execute { rtRsImm(OPCODE.Addiu, 8, 9, 0x7AFEu) }
        assertAssembly("addiu \$t0, \$t1, 0x7AFE")
        assertRegisters(t0 = 0x35B8u, t1 = 0xFFFF_BABAu)
    }

    @Test
    fun adduTestPositive() {
        regs(v0 = 0xFFFE_AAAAu, v1 = 0xDCBAu)
        execute { rdRsRt(OPCODE.Addu, 1, 2, 3) }
        assertAssembly("addu \$at, \$v0, \$v1")
        assertRegisters(at = 0xFFFF_8764u, v0 = 0xFFFE_AAAAu, v1 = 0xDCBAu)
    }

    @Test
    fun adduTestNegative() {
        regs(v0 = 0xFFFE_AAAAu, v1 = 0xFFFF_DCBAu)
        execute { rdRsRt(OPCODE.Addu, 1, 2, 3) }
        assertAssembly("addu \$at, \$v0, \$v1")
        assertRegisters(at = 0xFFFE_8764u, v0 = 0xFFFE_AAAAu, v1 = 0xFFFF_DCBAu)
    }

    @Test
    fun adduTestOverflow() {
        regs(v0 = 0xFFFE_AAAAu, v1 = 0x1_DCBAu)
        execute { rdRsRt(OPCODE.Addu, 1, 2, 3) }
        assertAssembly("addu \$at, \$v0, \$v1")
        assertRegisters(at = 0x8764u, v0 = 0xFFFE_AAAAu, v1 = 0x1_DCBAu)
    }

    @Test
    fun andTest() {
        regs(s0 = 0x986D_1A2Fu, a3 = 0x8475_6321u)
        execute { rdRsRt(OPCODE.And, 24, 16, 7) }
        assertAssembly("and \$t8, \$s0, \$a3")
        assertRegisters(t8 = 0x8065_0221u, s0 = 0x986D_1A2Fu, a3 = 0x8475_6321u)

        regs(s1 = 0xCA_FFCAu, s2 = 0xBABAu)
        execute { rdRsRt(OPCODE.And, 16, 17, 18) }
        assertAssembly("and \$s0, \$s1, \$s2")
        assertRegisters(s0 = 0xBA8Au, s1 = 0xCAFFCAu, s2 = 0xBABAu)
    }

    @Test
    fun andiTest() {
        regs(t8 = 0x8065_0221u, s0 = 0x986D_1A2Fu)
        execute { rtRsImm(OPCODE.Andi, 24, 16, 0x6321u) }
        assertAssembly("andi \$t8, \$s0, 0x6321")
        assertRegisters(t8 = 0x221u, s0 = 0x986D_1A2Fu)

        regs(s0 = 0xFFFF_FFFFu, s1 = 0xCA_FFCAu)
        execute { rtRsImm(OPCODE.Andi, 16, 17, 0xBABAu) }
        assertAssembly("andi \$s0, \$s1, 0xBABA")
        assertRegisters(s0 = 0xBA8Au, s1 = 0xCAFFCAu)
    }

    @Test
    fun beqTest() {
        regs(s0 = 0xFACC_BABAu, s1 = 0xFACC_BABAu)
        execute { rtBaseOffset(OPCODE.Beq, 16, 17, 0x6321u) }
        assertAssembly("beq \$s1, \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun beqlTestTrue() {
        regs(s0 = 0xFACC_BABAu, s1 = 0xFACC_BABAu)
        execute { rtBaseOffset(OPCODE.Beql, 16, 17, 0x6321u) }
        assertAssembly("beql \$s1, \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun beqlTestFalse() {
        regs(s0 = 0xFACC_BABAu, s1 = 0xFACC_BABBu)
        execute { rtBaseOffset(OPCODE.Beql, 16, 17, 0x6321u) }
        assertAssembly("beql \$s1, \$s0, 00018C84")
        assertDelaySlot(0x18C80u, false)
    }

    @Test
    fun bgezTest() {
        regs(s0 = 0x7ACC_BABAu)
        execute { rsOffset(OPCODE.Bgez, 16, 0x6321u) }
        assertAssembly("bgez \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bgezTestZero() {
        execute { rsOffset(OPCODE.Bgez, 16, 0x6321u) }
        assertAssembly("bgez \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bgezalTest() {
        regs(s0 = 0x7ACC_BABAu)
        execute { rsOffset(OPCODE.Bgezal, 16, 0x6321u) }
        assertAssembly("bgezal \$s0, 00018C84")
        assertRegisters(s0 = 0x7ACC_BABAu, ra = 0x8000_0008u)
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bgezalTestZero() {
        execute { rsOffset(OPCODE.Bgezal, 16, 0x6321u) }
        assertAssembly("bgezal \$s0, 00018C84")
        assertRegisters(ra = 0x8000_0008u)
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bgezallTestTrue() {
        regs(s0 = 0xACC_BABAu)
        execute { rsOffset(OPCODE.Bgezall, 16, 0x6321u) }
        assertAssembly("bgezall \$s0, 00018C84")
        assertRegisters(s0 = 0xACC_BABAu, ra = 0x8000_0008u)
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bgezallTestFalse() {
        regs(s0 = 0xFACC_BABAu)
        execute { rsOffset(OPCODE.Bgezall, 16, 0x6321u) }
        assertAssembly("bgezall \$s0, 00018C84")
        assertRegisters(s0 = 0xFACC_BABAu, ra = 0x8000_0008u)
        assertDelaySlot(0x18C80u, false)
    }

    @Test
    fun bgezallTestZero() {
        execute { rsOffset(OPCODE.Bgezall, 16, 0x6321u) }
        assertAssembly("bgezall \$s0, 00018C84")
        assertRegisters(ra = 0x8000_0008u)
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bgezlTestTrue() {
        regs(s0 = 0xACC_BABAu)
        execute { rsOffset(OPCODE.Bgezl, 16, 0x6321u) }
        assertAssembly("bgezl \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bgezlTestFalse() {
        regs(s0 = 0xFACC_BABAu)
        execute { rsOffset(OPCODE.Bgezl, 16, 0x6321u) }
        assertAssembly("bgezl \$s0, 00018C84")
        assertDelaySlot(0x18C80u, false)
    }

    @Test
    fun bgezlTestZero() {
        execute { rsOffset(OPCODE.Bgezl, 16, 0x6321u) }
        assertAssembly("bgezl \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bgtzTest() {
        regs(s0 = 0xACC_BABAu)
        execute { rsOffset(OPCODE.Bgtz, 16, 0x6321u) }
        assertAssembly("bgtz \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bgtzlTestTrue() {
        regs(s0 = 0xACC_BABAu)
        execute { rsOffset(OPCODE.Bgtzl, 16, 0x6321u) }
        assertAssembly("bgtzl \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bgtzlFalse() {
        regs(s0 = 0xFACC_BABAu)
        execute { rsOffset(OPCODE.Bgtzl, 16, 0x6321u) }
        assertAssembly("bgtzl \$s0, 00018C84")
        assertDelaySlot(0x18C80u, false)
    }

    @Test
    fun bgtzlZero() {
        execute { rsOffset(OPCODE.Bgtzl, 16, 0x6321u) }
        assertAssembly("bgtzl \$s0, 00018C84")
        assertDelaySlot(0x18C80u, false)
    }

    @Test
    fun blezTest() {
        regs(s0 = 0xBACC_BABAu)
        execute { rsOffset(OPCODE.Blez, 16, 0x6321u) }
        assertAssembly("blez \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun blezTestZero() {
        execute { rsOffset(OPCODE.Blez, 16, 0x6321u) }
        assertAssembly("blez \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun blezlTestTrue() {
        regs(s0 = 0xFACC_BABAu)
        execute { rsOffset(OPCODE.Blezl, 16, 0x6321u) }
        assertAssembly("blezl \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun blezlTestFalse() {
        regs(s0 = 0xACC_BABAu)
        execute { rsOffset(OPCODE.Blezl, 16, 0x6321u) }
        assertAssembly("blezl \$s0, 00018C84")
        assertDelaySlot(0x18C80u, false)
    }

    @Test
    fun blezlTestZero() {
        execute { rsOffset(OPCODE.Blezl, 16, 0x6321u) }
        assertAssembly("blezl \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bltzTest() {
        regs(s0 = 0xFACC_BABAu)
        execute { rsOffset(OPCODE.Bltz, 16, 0x6321u) }
        assertAssembly("bltz \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bltzalTest() {
        regs(s0 = 0xBACC_BABAu)
        execute { rsOffset(OPCODE.Bltzal, 16, 0x6321u) }
        assertAssembly("bltzal \$s0, 00018C84")
        assertRegisters(s0 = 0xBACC_BABAu, ra = 0x8000_0008u)
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bltzallTestTrue() {
        regs(s0 = 0xFACC_BABAu)
        execute { rsOffset(OPCODE.Bltzall, 16, 0x6321u) }
        assertAssembly("bltzall \$s0, 00018C84")
        assertRegisters(s0 = 0xFACC_BABAu, ra = 0x8000_0008u)
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bltzallTestFalse() {
        regs(s0 = 0xACC_BABAu)
        execute { rsOffset(OPCODE.Bltzall, 16, 0x6321u) }
        assertAssembly("bltzall \$s0, 00018C84")
        assertRegisters(s0 = 0xACC_BABAu, ra = 0x8000_0008u)
        assertDelaySlot(0x18C80u, false)
    }

    @Test
    fun bltzallTestZero() {
        execute { rsOffset(OPCODE.Bltzall, 16, 0x6321u) }
        assertAssembly("bltzall \$s0, 00018C84")
        assertRegisters(ra = 0x8000_0008u)
        assertDelaySlot(0x18C80u, false)
    }

    @Test
    fun bltzlTestTrue() {
        regs(s0 = 0xFACC_BABAu)
        execute { rsOffset(OPCODE.Bltzl, 16, 0x6321u) }
        assertAssembly("bltzl \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bltzlFalse() {
        regs(s0 = 0xACC_BABAu)
        execute { rsOffset(OPCODE.Bltzl, 16, 0x6321u) }
        assertAssembly("bltzl \$s0, 00018C84")
        assertDelaySlot(0x18C80u, false)
    }

    @Test
    fun bltzlZero() {
        execute { rsOffset(OPCODE.Bltzl, 16, 0x6321u) }
        assertAssembly("bltzl \$s0, 00018C84")
        assertDelaySlot(0x18C80u, false)
    }

    @Test
    fun bneTest() {
        regs(s0 = 0xFACC_BACAu, s1 = 0xFACC_BABAu)
        execute { rtBaseOffset(OPCODE.Bne, 16, 17, 0x6321u) }
        assertAssembly("bne \$s1, \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bnelTestTrue() {
        regs(s0 = 0xFACC_BACAu, s1 = 0xFACC_BABAu)
        execute { rtBaseOffset(OPCODE.Bnel, 16, 17, 0x6321u) }
        assertAssembly("bnel \$s1, \$s0, 00018C84")
        assertDelaySlot(0x18C80u, true)
    }

    @Test
    fun bnelTestFalse() {
        regs(s0 = 0xFACC_BABAu, s1 = 0xFACC_BABAu)
        execute { rtBaseOffset(OPCODE.Bnel, 16, 17, 0x6321u) }
        assertAssembly("bnel \$s1, \$s0, 00018C84")
        assertDelaySlot(0x18C80u, false)
    }

    @Test
    fun cloTest() {
        regs(s0 = 0xFA01_4759u, t8 = 0x8475_6321u)
        execute { rdRsRt(OPCODE.Clo, 24, 16, 16) }
        assertAssembly("clo \$t8, \$s0, \$s0")
        assertRegisters(s0 = 0xFA01_4759u, t8 = 0x5u)

        regs(s0 = 0x28_822Fu, t8 = 0x8475_6321u)
        execute { rdRsRt(OPCODE.Clo, 24, 16, 16) }
        assertAssembly("clo \$t8, \$s0, \$s0")
        assertRegisters(s0 = 0x28822Fu, t8 = 0x0u)
    }

    @Test
    fun clzTest() {
        regs(s0 = 0xFA01_4759u, t8 = 0x8475_6321u)
        execute { rdRsRt(OPCODE.Clz, 24, 16, 16) }
        assertAssembly("clz \$t8, \$s0, \$s0")
        assertRegisters(s0 = 0xFA01_4759u, t8 = 0x0u)

        regs(s0 = 0x28_822Fu, t8 = 0x8475_6321u)
        execute { rdRsRt(OPCODE.Clz, 24, 16, 16) }
        assertAssembly("clz \$t8, \$s0, \$s0")
        assertRegisters(s0 = 0x28822Fu, t8 = 0xAu)
    }

    @Test
    fun diTest() {
        regs(ra = 0x7642_BA52u)
        specialRegs(status = 0x0024_F44Bu)
        execute { rt(OPCODE.Di, 31) }
        assertAssembly("di \$ra")
        assertRegisters(ra = 0x0024_F44Bu)
        assertSpecialRegisters(status = 0x0024_F44Au)

        regs()
        specialRegs(status = 0x0024_F44Bu)
        execute { rt(OPCODE.Di, 0) }
        assertAssembly("di \$zero")
        assertRegisters(zero = 0u)
        assertSpecialRegisters(status = 0x0024_F44Au)
    }

    @Test
    fun divTestPositive() {
        regs(t0 = 0x1_4FCBu, t1 = 0x6u)
        execute { rsRt(OPCODE.Div, 8, 9) }
        assertAssembly("div \$zero, \$t0, \$t1")
        assertSpecialRegisters(hi = 0x1u, lo = 0x37F7u)
        assertRegisters(t0 = 0x1_4FCBu, t1 = 0x6u)
    }

    @Test
    fun divTestNegative() {
        regs(t0 = 0xFFFE_B035u, t1 = 0x6u)
        execute { rsRt(OPCODE.Div, 8, 9) }
        assertAssembly("div \$zero, \$t0, \$t1")
        assertSpecialRegisters(hi = 0xFFFF_FFFFu, lo = 0xFFFF_C809u)
        assertRegisters(t0 = 0xFFFE_B035u, t1 = 0x6u)
    }

    @Test
    fun divuTestPositive() {
        regs(t0 = 0x1_4FCBu, t1 = 0x6u)
        execute { rsRt(OPCODE.Divu, 8, 9) }
        assertAssembly("divu \$zero, \$t0, \$t1")
        assertSpecialRegisters(hi = 0x1u, lo = 0x37F7u)
        assertRegisters(t0 = 0x1_4FCBu, t1 = 0x6u)
    }

    @Test
    fun divuTestNegative() {
        regs(t0 = 0xFFFE_B035u, t1 = 0x6u)
        execute { rsRt(OPCODE.Divu, 8, 9) }
        assertAssembly("divu \$zero, \$t0, \$t1")
        assertSpecialRegisters(hi = 0x3u, lo = 0x2AAA_72B3u)
        assertRegisters(t0 = 0xFFFE_B035u, t1 = 0x6u)
    }

    @Test
    fun eiTest() {
        regs(ra = 0x7642_BA52u)
        specialRegs(status = 0x0024_F44Au)
        execute { rt(OPCODE.Ei, 31) }
        assertAssembly("ei \$ra")
        assertRegisters(ra = 0x0024_F44Au)
        assertSpecialRegisters(status = 0x0024_F44Bu)

        regs()
        specialRegs(status = 0x0024_F44Au)
        execute { rt(OPCODE.Ei, 0) }
        assertAssembly("ei \$zero")
        assertRegisters(zero = 0u)
        assertSpecialRegisters(status = 0x0024_F44Bu)
    }

    @Test
    fun extTest() {
        regs(s5 = 0x4F56_DD58u, s6 = 0x1693_98E5u)
        execute { rsRtMsbdLsb(OPCODE.Ext, 21, 22, 0x8, 0xC) }
        // Does not match the documentation
        assertAssembly("ext \$s5, \$s6, 0x08, 0x0B")
        assertRegisters(s5 = 0x398u, s6 = 0x1693_98E5u)
    }

    @Test
    fun insTest() {
        regs(s6 = 0x1693_98E5u, s7 = 0x4F56_DD58u)
        execute { rsRtMsbLsb(OPCODE.Ins, 22, 23, 0x8, 0xC) }
        // Does not match the documentation
        assertAssembly("ins \$s6, \$s7, 0x08, 0x13")
        assertRegisters(s6 = 0x169D_58E5u, s7 = 0x4F56_DD58u)
    }

    @Test
    fun jTest() {
        execute { instrIndex(OPCODE.J, 0x8F_7A41) }
        // 0x8F_7A41 shl 2 = 0x23D_E904
        assertAssembly("j 023DE904")
        // 2 instr done: PC = 0x8000_0008
        // Then PC = 0x823D_E904
        // Offset = 0x823D_E904 - 0x8000_0008 = 0x23D_E8FC
        assertDelaySlot(0x23D_E8FCu, true)
    }

    @Test
    fun jalTest() {
        execute { instrIndex(OPCODE.Jal, 0x8F_7A41) }
        // 0x8F_7A41 shl 2 = 0x23D_E904
        assertAssembly("jal 023DE904")
        assertRegisters(ra = 0x8000_0008u)
        // 2 instr done: PC = 0x8000_0008
        // Then PC = 0x823D_E904
        // Offset = 0x823D_E904 - 0x8000_0008 = 0x23D_E8FC
        assertDelaySlot(0x23D_E8FCu, true)
    }

    @Test
    fun jalrTest() {
        regs(s1 = 0xFFFF_FFFFu, v0 = 0x80C6_A780u)
        execute { rdRsHint(OPCODE.Jalr, 17, 2) }
        // 0x8F_7A41 shl 2 = 0x23D_E904
        assertAssembly("jalr \$s1, \$v0, 0x00")
        assertRegisters(s1 = 0x8000_0008u, v0 = 0x80C6_A780u)
        // 2 instr done: PC = 0x8000_0008
        // Then PC = 0x80C6_A780
        // Offset = 0x80C6_A780 - 0x8000_0008 = 0xC6_A778
        assertDelaySlot(0xC6_A778u, true)
    }

    @Test
    fun jrTest() {
        regs(v0 = 0x80C6_A780u)
        execute { rdRsHint(OPCODE.Jr, 0, 2) }
        // 0x8F_7A41 shl 2 = 0x23D_E904
        assertAssembly("jr \$v0")
        assertRegisters(v0 = 0x80C6_A780u)
        // 2 instr done: PC = 0x8000_0008
        // Then PC = 0x80C6_A780
        // Offset = 0x80C6_A780 - 0x8000_0008 = 0xC6_A778
        assertDelaySlot(0xC6_A778u, true)
    }

    @Test
    fun lbTestPositive() {
        store(0x8000_FFFFu, 0x55u, Datatype.BYTE)
        regs(sp = 0x2856_FE53u, fp = 0x8000_FF00u)
        execute { rtBaseOffset(OPCODE.Lb, 29, 30, 0xFFu) }
        assertAssembly("lb \$sp, byte [\$fp+0xFF]")
        assertRegisters(sp = 0x55u, fp = 0x8000_FF00u)
    }

    @Test
    fun lbTestNegative() {
        store(0x8000_FFFFu, 0xBBu, Datatype.BYTE)
        regs(sp = 0x2856_FE53u, a1 = 0x8000_FF00u)
        execute { rtBaseOffset(OPCODE.Lb, 29, 5, 0xFFu) }
        assertAssembly("lb \$sp, byte [\$a1+0xFF]")
        assertRegisters(sp = 0xFFFF_FFBBu, a1 = 0x8000_FF00u)
    }

    @Test
    fun lbuTest() {
        store(0x8000_FFFFu, 0xBBu, Datatype.BYTE)
        regs(sp = 0x2856_FE53u, a1 = 0x8000_FF00u)
        execute { rtBaseOffset(OPCODE.Lbu, 29, 5, 0xFFu) }
        assertAssembly("lbu \$sp, byte [\$a1+0xFF]")
        assertRegisters(sp = 0xBBu, a1 = 0x8000_FF00u)
    }

    @Test
    fun lhTestPositive() {
        store(0x8001_0000u, 0x77BBu, Datatype.WORD)
        regs(sp = 0x2856_FE53u, a1 = 0x8001_0000u)
        execute { rtBaseOffset(OPCODE.Lh, 29, 5, 0x0u) }
        assertAssembly("lh \$sp, word [\$a1]")
        assertRegisters(a1 = 0x8001_0000u, sp = 0x77BBu)
    }

    @Test
    fun lhTestNegative() {
        store(0x8001_0000u, 0xFFBBu, Datatype.WORD)
        regs(sp = 0x2856_FE53u, a1 = 0x8001_0000u)
        execute { rtBaseOffset(OPCODE.Lh, 29, 5, 0x0u) }
        assertAssembly("lh \$sp, word [\$a1]")
        assertRegisters(a1 = 0x8001_0000u, sp = 0xFFFF_FFBBu)
    }

    @Test
    fun lhTestError() {
        execute(offset = -4) { rtBaseOffset(OPCODE.Lh, 29, 5, 0xFFu) }
        assertTrue { (mips.cpu.exception as? MemoryAccessError)?.LorS == LOAD }
    }

    @Test
    fun lhuTest() {
        store(0x8001_0000u, 0xFFBBu, Datatype.WORD)
        regs(sp = 0x2856_FE53u, a1 = 0x8001_0000u)
        execute { rtBaseOffset(OPCODE.Lhu, 29, 5, 0x0u) }
        assertAssembly("lhu \$sp, word [\$a1]")
        assertRegisters(sp = 0xFFBBu, a1 = 0x8001_0000u)
    }

    @Test
    fun lhuTestError() {
        execute(offset = -4) { rtBaseOffset(OPCODE.Lhu, 29, 5, 0xFFu) }
        assertTrue { (mips.cpu.exception as? MemoryAccessError)?.LorS == LOAD }
    }

    @Test
    fun llTest() {
        store(0x8001_0000u, 0xFFFF_77BBu, Datatype.DWORD)
        regs(sp = 0x2856_FE53u, a1 = 0x8001_0000u)
        execute { rtBaseOffset(OPCODE.Ll, 29, 5, 0x0u) }
        assertAssembly("ll \$sp, dword [\$a1]")
        assertRegisters(a1 = 0x8001_0000u, sp = 0xFFFF_77BBu)
        assertTrue { mips.cpu.llbit == 1 }
    }

    @Test
    fun luiTest() {
        regs(k0 = 0x2856_FE53u)
        execute { rtImm(OPCODE.Lui, 26, 0xFF89) }
        assertAssembly("lui \$k0, 0xFF89")
        assertRegisters(k0 = 0xFF89_0000u)
    }

    @Test
    fun lwTestError() {
        execute(offset = -4) { rtBaseOffset(OPCODE.Lh, 29, 5, 0xFFFDu) }
        assertTrue { (mips.cpu.exception as? MemoryAccessError)?.LorS == LOAD }
    }

    @Test
    fun lwTest() {
        store(0x8001_0000u, 0xFAFF_00BBu, Datatype.DWORD)
        regs(sp = 0x2856_FE53u, a1 = 0x8000_FF10u)
        execute { rtBaseOffset(OPCODE.Lw, 29, 5, 0xF0u) }
        assertAssembly("lw \$sp, dword [\$a1+0xF0]")
        assertRegisters(sp = 0xFAFF_00BBu, a1 = 0x8000_FF10u)
    }

    @Test
    fun lwlTest() {
        store(0x8001_0000u, 0xFAFF_00BBu, Datatype.DWORD)
        regs(sp = 0x2856_FE53u, a1 = 0x8001_0000u)
        execute { rtBaseOffset(OPCODE.Lwl, 29, 5, 0u) }
        assertAssembly("lwl \$sp, dword [\$a1]")
        assertRegisters(sp = 0xBB56_FE53u, a1 = 0x8001_0000u)
    }

    @Test
    fun lwrTest() {
        store(0x8001_0000u, 0xFAFF_00BBu, Datatype.DWORD)
        regs(sp = 0x2856_FE53u, a1 = 0x8001_0000u)
        execute { rtBaseOffset(OPCODE.Lwr, 29, 5, 0u) }
        assertAssembly("lwr \$sp, dword [\$a1]")
        assertRegisters(sp = 0xFAFF_00BBu, a1 = 0x8001_0000u)
    }

    @Test
    fun maddTestPositive() {
        specialRegs(hi = 0x1F57_AC90u, lo = 0x2F56_AB21u)
        regs(sp = 0x2856_FE53u, v1 = 0x4563_D752u)
        execute { rsRt(OPCODE.Madd, 29, 3) }
        assertAssembly("madd \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0x2A46_DAAEu, lo = 0xE3E4_D6B7u)
        assertRegisters(sp = 0x2856_FE53u, v1 = 0x4563_D752u)
    }

    @Test
    fun maddTestNegative() {
        specialRegs(hi = 0x1F57_AC90u, lo = 0x2F56_AB21u)
        regs(sp = 0x8856_FE53u, v1 = 0x4563_D752u)
        execute { rsRt(OPCODE.Madd, 29, 3) }
        assertAssembly("madd \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0xFEE8_741Bu, lo = 0xA3E4_D6B7u)
        assertRegisters(sp = 0x8856_FE53u, v1 = 0x4563_D752u)
    }

    @Test
    fun madduTestPositive() {
        specialRegs(hi = 0x1F57_AC90u, lo = 0x2F56_AB21u)
        regs(sp = 0x2856_FE53u, v1 = 0x4563_D752u)
        execute { rsRt(OPCODE.Maddu, 29, 3) }
        assertAssembly("maddu \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0x2A46_DAAEu, lo = 0xE3E4_D6B7u)
        assertRegisters(sp = 0x2856_FE53u, v1 = 0x4563_D752u)
    }

    @Test
    fun madduTestNegative() {
        specialRegs(hi = 0x1F57_AC90u, lo = 0x2F56_AB21u)
        regs(sp = 0x8856_FE53u, v1 = 0xF563_D752u)
        execute { rsRt(OPCODE.Maddu, 29, 3) }
        assertAssembly("maddu \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0xA208_1A46u, lo = 0xB3E4_D6B7u)
        assertRegisters(sp = 0x8856_FE53u, v1 = 0xF563_D752u)
    }

    @Test
    fun mfhiTest() {
        regs(v1 = 0xF563_D752u)
        specialRegs(hi = 0xC45A_09FFu)
        execute { rd(OPCODE.Mfhi, 3) }
        assertAssembly("mfhi \$v1")
        assertSpecialRegisters(hi = 0xC45A_09FFu)
        assertRegisters(v1 = 0xC45A_09FFu)
    }

    @Test
    fun mfloTest() {
        regs(v0 = 0xF563_D752u)
        specialRegs(lo = 0xC45A_09FFu)
        execute { rd(OPCODE.Mflo, 2) }
        assertAssembly("mflo \$v0")
        assertSpecialRegisters(lo = 0xC45A_09FFu)
        assertRegisters(v0 = 0xC45A_09FFu)
    }

    @Test
    fun movnTest() {
        regs(a3 = 0x96_43ACu, t9 = 0xFF78_32A4u)
        execute { rdRsRt(OPCODE.Movn, 7, 25, 30) }
        assertAssembly("movn \$a3, \$t9, \$fp")
        assertRegisters(a3 = 0x96_43ACu, t9 = 0xFF78_32A4u)

        regs(a3 = 0x96_43ACu, t9 = 0xFF78_32A4u, t3 = 1u)
        execute { rdRsRt(OPCODE.Movn, 7, 25, 11) }
        assertAssembly("movn \$a3, \$t9, \$t3")
        assertRegisters(a3 = 0xFF78_32A4u, t9 = 0xFF78_32A4u, t3 = 1u)
    }

    @Test
    fun movzTest() {
        regs(a3 = 0x96_43ACu, t9 = 0xFF78_32A4u, t3 = 1u)
        execute { rdRsRt(OPCODE.Movz, 7, 25, 11) }
        assertAssembly("movz \$a3, \$t9, \$t3")
        assertRegisters(a3 = 0x96_43ACu, t9 = 0xFF78_32A4u, t3 = 1u)

        regs(a3 = 0x96_43ACu, t9 = 0xFF78_32A4u)
        execute { rdRsRt(OPCODE.Movz, 7, 25, 11) }
        assertAssembly("movz \$a3, \$t9, \$t3")
        assertRegisters(a3 = 0xFF78_32A4u, t9 = 0xFF78_32A4u)
    }

    @Test
    fun msubTestPositive() {
        specialRegs(hi = 0xFF93_E1D7u, lo = 0x58BA_5631u)
        regs(sp = 0x2856_FE53u, v1 = 0x4563_D752u)
        execute { rsRt(OPCODE.Msub, 29, 3) }
        assertAssembly("msub \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0xF4A4_B3B8u, lo = 0xA42C_2A9Bu)
        assertRegisters(sp = 0x2856_FE53u, v1 = 0x4563_D752u)
    }

    @Test
    fun msubTestNegative() {
        specialRegs(hi = 0xFF93_E1D7u, lo = 0x58BA_5631u)
        regs(sp = 0x8856_FE53u, v1 = 0x4563_D752u)
        execute { rsRt(OPCODE.Msub, 29, 3) }
        assertAssembly("msub \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0x2003_1A4Bu, lo = 0xE42C_2A9Bu)
        assertRegisters(sp = 0x8856_FE53u, v1 = 0x4563_D752u)
    }

    @Test
    fun msubuTestPositive() {
        specialRegs(hi = 0xFF93_E1D7u, lo = 0x58BA_5631u)
        regs(sp = 0x2856_FE53u, v1 = 0x4563_D752u)
        execute { rsRt(OPCODE.Msubu, 29, 3) }
        assertAssembly("msubu \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0xF4A4_B3B8u, lo = 0xA42C_2A9Bu)
        assertRegisters(sp = 0x2856_FE53u, v1 = 0x4563_D752u)
    }

    @Test
    fun msubuTestNegative() {
        specialRegs(hi = 0xFF93_E1D7u, lo = 0x58BA_5631u)
        regs(sp = 0x8856_FE53u, v1 = 0xF563_D752u)
        execute { rsRt(OPCODE.Msubu, 29, 3) }
        assertAssembly("msubu \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0x7CE3_7420u, lo = 0xD42C_2A9Bu)
        assertRegisters(sp = 0x8856_FE53u, v1 = 0xF563_D752u)
    }

    @Test
    fun mthiTest() {
        regs(s5 = 0xF563_D752u)
        specialRegs(hi = 0xC45A_09FFu)
        execute { rs(OPCODE.Mthi, 21) }
        assertAssembly("mthi \$s5")
        assertSpecialRegisters(hi = 0xF563_D752u)
        assertRegisters(s5 = 0xF563_D752u)
    }

    @Test
    fun mtloTest() {
        regs(v0 = 0xF563_D752u)
        specialRegs(lo = 0xC45A_09FFu)
        execute { rs(OPCODE.Mtlo, 2) }
        assertAssembly("mtlo \$v0")
        assertSpecialRegisters(lo = 0xF563_D752u)
        assertRegisters(v0 = 0xF563_D752u)
    }

    @Test
    fun mulTest() {
        regs(a2 = 0xF563_D752u, k1 = 0xC45A_09FFu)
        execute { rdRsRt(OPCODE.Mul, 9, 6, 27) }
        assertAssembly("mul \$t1, \$a2, \$k1")
        assertRegisters(a2 = 0xF563_D752u, k1 = 0xC45A_09FFu, t1 = 0x6BD9_5CAEu)
    }

    @Test
    fun multTestNegative() {
        regs(a2 = 0xF563_D752u, k1 = 0xC45A_09FFu)
        execute { rsRt(OPCODE.Mult, 27, 6) }
        assertAssembly("mult \$zero, \$k1, \$a2")
        assertSpecialRegisters(hi = 0x278_DE38u, lo = 0x6BD9_5CAEu)
        assertRegisters(a2 = 0xF563_D752u, k1 = 0xC45A_09FFu)
    }

    @Test
    fun multTestPositive() {
        regs(a2 = 0x2563_D752u, k1 = 0x745A_09FFu)
        execute { rsRt(OPCODE.Mult, 27, 6) }
        assertAssembly("mult \$zero, \$k1, \$a2")
        assertSpecialRegisters(hi = 0x10FE_6420u, lo = 0x9BD9_5CAEu)
        assertRegisters(a2 = 0x2563_D752u, k1 = 0x745A_09FFu)
    }

    @Test
    fun multuTestNegative() {
        regs(a2 = 0xF563_D752u, k1 = 0xC45A_09FFu)
        execute { rsRt(OPCODE.Multu, 27, 6) }
        assertAssembly("multu \$zero, \$k1, \$a2")
        assertSpecialRegisters(hi = 0xBC36_BF89u, lo = 0x6BD9_5CAEu)
        assertRegisters(a2 = 0xF563_D752u, k1 = 0xC45A_09FFu)
    }

    @Test
    fun multuTestPositive() {
        regs(a2 = 0x2563_D752u, k1 = 0x745A_09FFu)
        execute { rsRt(OPCODE.Multu, 27, 6) }
        assertAssembly("multu \$zero, \$k1, \$a2")
        assertSpecialRegisters(hi = 0x10FE_6420u, lo = 0x9BD9_5CAEu)
        assertRegisters(a2 = 0x2563_D752u, k1 = 0x745A_09FFu)
    }

    @Test
    fun norTest() {
        regs(t7 = 0xA781_4EF2u, s7 = 0xC796_32FEu)
        execute { rdRsRt(OPCODE.Nor, 4, 15, 23) }
        assertAssembly("nor \$a0, \$t7, \$s7")
        assertRegisters(a0 = 0x1868_8101u, t7 = 0xA781_4EF2u, s7 = 0xC796_32FEu)
    }

    @Test
    fun orTest() {
        regs(t7 = 0xA781_4EF2u, s7 = 0xC796_32FEu)
        execute { rdRsRt(OPCODE.Or, 4, 15, 23) }
        assertAssembly("or \$a0, \$t7, \$s7")
        assertRegisters(a0 = 0xE797_7EFEu, t7 = 0xA781_4EF2u, s7 = 0xC796_32FEu)
    }

    @Test
    fun oriTest() {
        regs(t0 = 0xFFFF_FFFFu, t1 = 0xFFFE_BABAu)
        execute { rtRsImm(OPCODE.Ori, 8, 9, 0x7AFEu) }
        assertAssembly("ori \$t0, \$t1, 0x7AFE")
        assertRegisters(t0 = 0xFFFE_FAFEu, t1 = 0xFFFE_BABAu)
    }

    @Test
    fun rotrTest() {
        regs(t0 = 0xFFFF_FFFFu, t1 = 0xCAFE_BABAu)
        execute { rdRtSa(OPCODE.Rotr, 8, 9, 0x10) }
        assertAssembly("rotr \$t0, \$t1, 0x10")
        assertRegisters(t0 = 0xBABA_CAFEu, t1 = 0xCAFE_BABAu)
    }

    @Test
    fun rotrvTest() {
        regs(s0 = 0xFFFF_FFFFu, s1 = 0x10u, s2 = 0xCAFE_BABAu)
        execute { rdRsRt(OPCODE.Rotrv, 16, 17, 18) }
        assertAssembly("rotrv \$s0, \$s2, \$s1")
        assertRegisters(s0 = 0xBABA_CAFEu, s1 = 0x10u, s2 = 0xCAFE_BABAu)
    }

    @Test
    fun sbTestPositive() {
        regs(k0 = 0x53u, k1 = 0x8000_0000u)
        execute { rtBaseOffset(OPCODE.Sb, 26, 27, 0x7FFFu) }
        assertAssembly("sb \$k0, byte [\$k1+0x7FFF]")
        assertMemory(0x8000_7FFFu, 0x53u, Datatype.BYTE)
    }

    @Test
    fun sbTestNegative() {
        regs(k0 = 0xFFFF_FF53u, k1 = 0x8000_0000u)
        execute { rtBaseOffset(OPCODE.Sb, 26, 27, 0x7FFFu) }
        assertAssembly("sb \$k0, byte [\$k1+0x7FFF]")
        assertMemory(0x8000_7FFFu, 0x53u, Datatype.BYTE)
    }

    @Test
    fun sebTest() {
        regs(k0 = 0xFFFF_FF53u, k1 = 0x8000_009Au)
        execute { rdRt(OPCODE.Seb, 26, 27) }
        assertAssembly("seb \$k0, \$k1")
        assertRegisters(k0 = 0xFFFF_FF9Au, k1 = 0x8000_009Au)
    }

    @Test
    fun sehTest() {
        regs(k0 = 0xFFFF_FF53u, k1 = 0x8000_8B9Au)
        execute { rdRt(OPCODE.Seh, 26, 27) }
        assertAssembly("seh \$k0, \$k1")
        assertRegisters(k0 = 0xFFFF_8B9Au, k1 = 0x8000_8B9Au)
    }

    @Test
    fun shTestPositive() {
        regs(k0 = 0xBB53u, k1 = 0x8000_0000u)
        execute { rtBaseOffset(OPCODE.Sh, 26, 27, 0x7FFEu) }
        assertAssembly("sh \$k0, word [\$k1+0x7FFE]")
        assertMemory(0x8000_7FFEu, 0xBB53u, Datatype.WORD)
    }

    @Test
    fun shTestNegative() {
        regs(k0 = 0xFFFF_BB53u, k1 = 0x8000_0000u)
        execute { rtBaseOffset(OPCODE.Sh, 26, 27, 0x7FFEu) }
        assertAssembly("sh \$k0, word [\$k1+0x7FFE]")
        assertMemory(0x8000_7FFEu, 0xBB53u, Datatype.WORD)
    }

    @Test
    fun shTestError() {
        execute(offset = -4) { rtBaseOffset(OPCODE.Sh, 29, 5, 0xFFu) }
        assertTrue { (mips.cpu.exception as? MemoryAccessError)?.LorS == STORE }
    }

    @Test
    fun sllTest() {
        regs(k0 = 0xFFFF_CAFEu, k1 = 0xBABA_BABAu)
        execute { rdRtSa(OPCODE.Sll, 26, 27, 0x10) }
        assertAssembly("sll \$k0, \$k1, 0x10")
        assertRegisters(k0 = 0xBABA_0000u, k1 = 0xBABA_BABAu)
    }

    @Test
    fun sllvTest() {
        regs(k0 = 0xFFFF_CAFEu, k1 = 0xBABA_BABAu, v1 = 0x10u)
        execute { rdRsRt(OPCODE.Sllv, 26, 3, 27) }
        assertAssembly("sllv \$k0, \$k1, \$v1")
        assertRegisters(k0 = 0xBABA_0000u, k1 = 0xBABA_BABAu, v1 = 0x10u)
    }

    @Test
    fun sltTestPositive() {
        regs(k0 = 0xFFFF_CAFEu, k1 = 0xABA_BABAu, v1 = 0xAFE_BABAu)
        execute { rdRsRt(OPCODE.Slt, 26, 3, 27) }
        assertAssembly("slt \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x0u, k1 = 0xABA_BABAu, v1 = 0xAFE_BABAu)

        regs(k0 = 0xFFFF_CAFEu, k1 = 0xAFE_BABAu, v1 = 0xABA_BABAu)
        execute { rdRsRt(OPCODE.Slt, 26, 3, 27) }
        assertAssembly("slt \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x1u, k1 = 0xAFE_BABAu, v1 = 0xABA_BABAu)
    }

    @Test
    fun sltTestNegative() {
        regs(k0 = 0xFFFF_CAFEu, k1 = 0xBABA_BABAu, v1 = 0xCAFE_BABAu)
        execute { rdRsRt(OPCODE.Slt, 26, 3, 27) }
        assertAssembly("slt \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x0u, k1 = 0xBABA_BABAu, v1 = 0xCAFE_BABAu)

        regs(k0 = 0xFFFF_CAFEu, k1 = 0xCAFE_BABAu, v1 = 0xBABA_BABAu)
        execute { rdRsRt(OPCODE.Slt, 26, 3, 27) }
        assertAssembly("slt \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x1u, k1 = 0xCAFE_BABAu, v1 = 0xBABA_BABAu)
    }

    @Test
    fun sltuTestPositive() {
        regs(k0 = 0xFFFF_CAFEu, k1 = 0xABA_BABAu, v1 = 0xAFE_BABAu)
        execute { rdRsRt(OPCODE.Sltu, 26, 3, 27) }
        assertAssembly("sltu \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x0u, k1 = 0xABA_BABAu, v1 = 0xAFE_BABAu)

        regs(k0 = 0xFFFF_CAFEu, k1 = 0xAFE_BABAu, v1 = 0xABA_BABAu)
        execute { rdRsRt(OPCODE.Sltu, 26, 3, 27) }
        assertAssembly("sltu \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x1u, k1 = 0xAFE_BABAu, v1 = 0xABA_BABAu)
    }

    @Test
    fun sltuTestNegative() {
        regs(k0 = 0xFFFF_CAFEu, k1 = 0xBABA_BABAu, v1 = 0xCAFE_BABAu)
        execute { rdRsRt(OPCODE.Sltu, 26, 3, 27) }
        assertAssembly("sltu \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x0u, k1 = 0xBABA_BABAu, v1 = 0xCAFE_BABAu)

        regs(k0 = 0xFFFF_CAFEu, k1 = 0xCAFE_BABAu, v1 = 0xBABA_BABAu)
        execute { rdRsRt(OPCODE.Sltu, 26, 3, 27) }
        assertAssembly("sltu \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x1u, k1 = 0xCAFE_BABAu, v1 = 0xBABA_BABAu)
    }

    @Test
    fun sltiTestPositive() {
        regs(k0 = 0xFFFF_CAFEu, v1 = 0x6ABFu)
        execute { rtRsImm(OPCODE.Slti, 26, 3, 0x6ABCu) }
        assertAssembly("slti \$k0, \$v1, 0x6ABC")
        assertRegisters(k0 = 0x0u, v1 = 0x6ABFu)

        regs(k0 = 0xFFFF_CAFEu, v1 = 0x6ABAu)
        execute { rtRsImm(OPCODE.Slti, 26, 3, 0x6ABCu) }
        assertAssembly("slti \$k0, \$v1, 0x6ABC")
        assertRegisters(k0 = 0x1u, v1 = 0x6ABAu)
    }

    @Test
    fun sltiTestNegative() {
        regs(k0 = 0xFFFF_CAFEu, v1 = 0xFFFF_8ABFu)
        execute { rtRsImm(OPCODE.Slti, 26, 3, 0x8ABCu) }
        assertAssembly("slti \$k0, \$v1, -0x7544")
        assertRegisters(k0 = 0x0u, v1 = 0xFFFF_8ABFu)

        regs(k0 = 0xFFFF_CAFEu, v1 = 0xFFFF_8ABAu)
        execute { rtRsImm(OPCODE.Slti, 26, 3, 0x8ABCu) }
        assertAssembly("slti \$k0, \$v1, -0x7544")
        assertRegisters(k0 = 0x1u, v1 = 0xFFFF_8ABAu)
    }

    @Test
    fun sltiuTest() {
        regs(k0 = 0xFFFF_CAFEu, v1 = 0xFFFF_8ABFu)
        execute { rtRsImm(OPCODE.Sltiu, 26, 3, 0x8ABCu) }
        assertAssembly("sltiu \$k0, \$v1, 0x8ABC")
        assertRegisters(k0 = 0x0u, v1 = 0xFFFF_8ABFu)

        regs(k0 = 0xFFFF_CAFEu, v1 = 0xFFFF_8ABAu)
        execute { rtRsImm(OPCODE.Sltiu, 26, 3, 0x8ABCu) }
        assertAssembly("sltiu \$k0, \$v1, 0x8ABC")
        assertRegisters(k0 = 0x1u, v1 = 0xFFFF_8ABAu)
    }

    @Test
    fun sraTestPositive() {
        regs(k0 = 0xFFFF_CAFEu, k1 = 0x7ABA_BABAu)
        execute { rdRtSa(OPCODE.Sra, 26, 27, 0x10) }
        assertAssembly("sra \$k0, \$k1, 0x10")
        assertRegisters(k0 = 0x7ABAu, k1 = 0x7ABA_BABAu)
    }

    @Test
    fun sraTestNegative() {
        regs(k0 = 0xFFFF_CAFEu, k1 = 0xBABA_BABAu)
        execute { rdRtSa(OPCODE.Sra, 26, 27, 0x10) }
        assertAssembly("sra \$k0, \$k1, 0x10")
        assertRegisters(k0 = 0xFFFF_BABAu, k1 = 0xBABA_BABAu)
    }

    @Test
    fun sravTestPositive() {
        regs(k0 = 0xFFFF_CAFEu, k1 = 0x7ABA_BABAu, v1 = 0x10u)
        execute { rdRsRt(OPCODE.Srav, 26, 3, 27) }
        assertAssembly("srav \$k0, \$k1, \$v1")
        assertRegisters(k0 = 0x7ABAu, k1 = 0x7ABA_BABAu, v1 = 0x10u)
    }

    @Test
    fun sravTestNegative() {
        regs(k0 = 0xFFFF_CAFEu, k1 = 0xBABA_BABAu, v1 = 0x10u)
        execute { rdRsRt(OPCODE.Srav, 26, 3, 27) }
        assertAssembly("srav \$k0, \$k1, \$v1")
        assertRegisters(k0 = 0xFFFF_BABAu, k1 = 0xBABA_BABAu, v1 = 0x10u)
    }

    @Test
    fun srlTest() {
        regs(k0 = 0xFFFF_CAFEu, k1 = 0xBABA_BABAu)
        execute { rdRtSa(OPCODE.Srl, 26, 27, 0x10) }
        assertAssembly("srl \$k0, \$k1, 0x10")
        assertRegisters(k0 = 0xBABAu, k1 = 0xBABA_BABAu)
    }

    @Test
    fun srlvTest() {
        regs(k0 = 0xFFFF_CAFEu, k1 = 0xBABA_BABAu, v1 = 0x10u)
        execute { rdRsRt(OPCODE.Srlv, 26, 3, 27) }
        assertAssembly("srlv \$k0, \$k1, \$v1")
        assertRegisters(k0 = 0xBABAu, k1 = 0xBABA_BABAu, v1 = 0x10u)
    }

    @Test
    fun subTest() {
        regs(t7 = 0xFFFE_AAAAu, s2 = 0xDCBAu)
        execute { rdRsRt(OPCODE.Sub, 12, 15, 18) }
        assertAssembly("sub \$t4, \$t7, \$s2")
        assertRegisters(t4 = 0xFFFD_CDF0u, t7 = 0xFFFE_AAAAu, s2 = 0xDCBAu)
    }

    @Test
    fun subTestOverflow() {
        regs(v0 = 0x8000_0000u, v1 = 0x1u)
        execute(offset = -4) { rdRsRt(OPCODE.Sub, 1, 2, 3) }
        assertTrue { mips.cpu.exception is MipsHardwareException.OV }
    }

    @Test
    fun subuTest() {
        regs(t7 = 0xFFFF_DCBAu, s2 = 0xAAAAu)
        execute { rdRsRt(OPCODE.Subu, 12, 15, 18) }
        assertAssembly("subu \$t4, \$t7, \$s2")
        assertRegisters(t4 = 0xFFFF_3210u, t7 = 0xFFFF_DCBAu, s2 = 0xAAAAu)
    }

    @Test
    fun swTest() {
        regs(k0 = 0xFFFF_BB53u, k1 = 0x8000_0000u)
        execute { rtBaseOffset(OPCODE.Sw, 26, 27, 0x7FF0u) }
        assertAssembly("sw \$k0, dword [\$k1+0x7FF0]")
        assertMemory(0x8000_7FF0u, 0xFFFF_BB53u, Datatype.DWORD)
    }

    @Test
    fun swTestError() {
        execute(offset = -4) { rtBaseOffset(OPCODE.Sw, 29, 5, 0xFFu) }
        assertTrue { (mips.cpu.exception as? MemoryAccessError)?.LorS == STORE }
    }

    @Test
    fun swlTest() {
        regs(k0 = 0x2856_FE53u, k1 = 0x8000_0000u)
        execute { rtBaseOffset(OPCODE.Swl, 26, 27, 0x7FF0u) }
        assertAssembly("swl \$k0, dword [\$k1+0x7FF0]")
        assertMemory(0x8000_7FF0u, 0x28u, Datatype.DWORD)
    }

    @Test
    fun swrTest() {
        regs(k0 = 0x2856_FE53u, k1 = 0x8000_0000u)
        execute { rtBaseOffset(OPCODE.Swr, 26, 27, 0x7FF0u) }
        assertAssembly("swr \$k0, dword [\$k1+0x7FF0]")
        assertMemory(0x8000_7FF0u, 0x2856_FE53u, Datatype.DWORD)
    }

    @Test
    fun wsbhTest() {
        regs(k0 = 0xFFFF_FF53u, k1 = 0x8321_199Au)
        execute { rdRt(OPCODE.Wsbh, 26, 27) }
        assertAssembly("wsbh \$k0, \$k1")
        assertRegisters(k0 = 0x2183_9A19u, k1 = 0x8321_199Au)
    }

    @Test
    fun xorTest() {
        regs(t7 = 0xA781_4EF2u, s7 = 0xC796_32FEu)
        execute { rdRsRt(OPCODE.Xor, 4, 15, 23) }
        assertAssembly("xorr \$a0, \$t7, \$s7")
        assertRegisters(a0 = 0x6017_7C0Cu, t7 = 0xA781_4EF2u, s7 = 0xC796_32FEu)
    }

    @Test
    fun xoriTest() {
        regs(t0 = 0xFFFF_FFFFu, t1 = 0xFFFE_BABAu)
        execute { rtRsImm(OPCODE.Xori, 8, 9, 0x7AFEu) }
        assertAssembly("xori \$t0, \$t1, 0x7AFE")
        assertRegisters(t0 = 0xFFFE_C044u, t1 = 0xFFFE_BABAu)
    }
}