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
package ru.inforion.lab403.kopycat.cores.mips.instructions

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.interfaces.read
import ru.inforion.lab403.kopycat.interfaces.write
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.modules.memory.RAM
import java.nio.ByteOrder

class Mips64InstructionsTest : Module(null, "Mips64Test") {

    val mips64 = MipsCore(
        this,
        "mips64",
        frequency = 800.MHz,
        1.0,
        1,
        0x00000000u, // random PRId
        64,
        64,
        ArchitectureRevision = 2,
        Config0Preset = 0x4000uL // 14 bit to 1
    )

    inner class Buses : ModuleBuses(this) {
        val mips64_mem = Bus("mips64_mem", mips64.PASIZE)
    }

    @DontAutoSerialize
    override val buses = Buses()
    private val ram0 = RAM(this, "ram0", 0x0FFF_FFFF)

    init {
        mips64.ports.mem.connect(buses.mips64_mem)
        ram0.ports.mem.connect(buses.mips64_mem, 0xFFFF_FFFF_0000_0000u)
        initializeAndResetAsTopInstance()
    }

    var size = 0
    private val startAddress = 0xFFFF_FFFF_8000_0000uL

    private fun execute(offset: Int = 0, generate: () -> ByteArray) {
        val data = generate()
        mips64.store(startAddress + size, data)
        mips64.step()
        println("%16s -> %s".format(data.hexlify(), mips64.cpu.insn))
        size += data.size + offset
    }

    private fun assertAssembly(expected: String) =
        Assert.assertEquals("Unexpected disassembly view!", expected, mips64.cpu.insn.toString())

    private fun assertRegister(num: Int, expected: ULong, actual: ULong, type: String = "GPR") =
        Assert.assertEquals(
            "${mips64.cpu.insn} -> $type $num error: 0x${expected.hex8} != 0x${actual.hex8}",
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
        assertRegister(0, zero, mips64.cpu.regs.zero.value)
        assertRegister(1, at, mips64.cpu.regs.at.value)
        assertRegister(2, v0, mips64.cpu.regs.v0.value)
        assertRegister(3, v1, mips64.cpu.regs.v1.value)
        assertRegister(4, a0, mips64.cpu.regs.a0.value)
        assertRegister(5, a1, mips64.cpu.regs.a1.value)
        assertRegister(6, a2, mips64.cpu.regs.a2.value)
        assertRegister(7, a3, mips64.cpu.regs.a3.value)
        assertRegister(8, t0, mips64.cpu.regs.t0.value)
        assertRegister(9, t1, mips64.cpu.regs.t1.value)
        assertRegister(10, t2, mips64.cpu.regs.t2.value)
        assertRegister(11, t3, mips64.cpu.regs.t3.value)
        assertRegister(12, t4, mips64.cpu.regs.t4.value)
        assertRegister(13, t5, mips64.cpu.regs.t5.value)
        assertRegister(14, t6, mips64.cpu.regs.t6.value)
        assertRegister(15, t7, mips64.cpu.regs.t7.value)
        assertRegister(16, s0, mips64.cpu.regs.s0.value)
        assertRegister(17, s1, mips64.cpu.regs.s1.value)
        assertRegister(18, s2, mips64.cpu.regs.s2.value)
        assertRegister(19, s3, mips64.cpu.regs.s3.value)
        assertRegister(20, s4, mips64.cpu.regs.s4.value)
        assertRegister(21, s5, mips64.cpu.regs.s5.value)
        assertRegister(22, s6, mips64.cpu.regs.s6.value)
        assertRegister(23, s7, mips64.cpu.regs.s7.value)
        assertRegister(24, t8, mips64.cpu.regs.t8.value)
        assertRegister(25, t9, mips64.cpu.regs.t9.value)
        assertRegister(26, k0, mips64.cpu.regs.k0.value)
        assertRegister(27, k1, mips64.cpu.regs.k1.value)
        assertRegister(28, gp, mips64.cpu.regs.gp.value)
        assertRegister(29, sp, mips64.cpu.regs.sp.value)
        assertRegister(30, fp, mips64.cpu.regs.fp.value)
        assertRegister(31, ra, mips64.cpu.regs.ra.value)
    }

    private fun regs(
        at: ULong = 0u, v0: ULong = 0u, v1: ULong = 0u, a0: ULong = 0u, a1: ULong = 0u, a2: ULong = 0u, a3: ULong = 0u,
        t0: ULong = 0u, t1: ULong = 0u, t2: ULong = 0u, t3: ULong = 0u, t4: ULong = 0u, t5: ULong = 0u, t6: ULong = 0u,
        t7: ULong = 0u, s0: ULong = 0u, s1: ULong = 0u, s2: ULong = 0u, s3: ULong = 0u, s4: ULong = 0u, s5: ULong = 0u,
        s6: ULong = 0u, s7: ULong = 0u, t8: ULong = 0u, t9: ULong = 0u, k0: ULong = 0u, k1: ULong = 0u, gp: ULong = 0u,
        sp: ULong = 0u, fp: ULong = 0u, ra: ULong = 0u
    ) {
        mips64.cpu.regs.at.value = at
        mips64.cpu.regs.v0.value = v0
        mips64.cpu.regs.v1.value = v1
        mips64.cpu.regs.a0.value = a0
        mips64.cpu.regs.a1.value = a1
        mips64.cpu.regs.a2.value = a2
        mips64.cpu.regs.a3.value = a3
        mips64.cpu.regs.t0.value = t0
        mips64.cpu.regs.t1.value = t1
        mips64.cpu.regs.t2.value = t2
        mips64.cpu.regs.t3.value = t3
        mips64.cpu.regs.t4.value = t4
        mips64.cpu.regs.t5.value = t5
        mips64.cpu.regs.t6.value = t6
        mips64.cpu.regs.t7.value = t7
        mips64.cpu.regs.s0.value = s0
        mips64.cpu.regs.s1.value = s1
        mips64.cpu.regs.s2.value = s2
        mips64.cpu.regs.s3.value = s3
        mips64.cpu.regs.s4.value = s4
        mips64.cpu.regs.s5.value = s5
        mips64.cpu.regs.s6.value = s6
        mips64.cpu.regs.s7.value = s7
        mips64.cpu.regs.t8.value = t8
        mips64.cpu.regs.t9.value = t9
        mips64.cpu.regs.k0.value = k0
        mips64.cpu.regs.k1.value = k1
        mips64.cpu.regs.gp.value = gp
        mips64.cpu.regs.sp.value = sp
        mips64.cpu.regs.fp.value = fp
        mips64.cpu.regs.ra.value = ra
    }

    private fun assertSpecialRegister(num: Int, expected: ULong, actual: ULong, type: String = "SRVC") =
        Assert.assertEquals(
            "${mips64.cpu.insn} -> $type $num error: 0x${expected.hex8} != 0x${actual.hex8}",
            expected,
            actual
        )

    private fun specialRegs(hi: ULong = 0u, lo: ULong = 0u, status: ULong = 0u) {
        mips64.cpu.hi = hi
        mips64.cpu.lo = lo
        mips64.cop.regs.Status.value = status
    }

    private fun assertSpecialRegisters(hi: ULong = 0u, lo: ULong = 0u, status: ULong = 0u) {
        assertSpecialRegister(0, hi, mips64.cpu.hi)
        assertSpecialRegister(1, lo, mips64.cpu.lo)
        assertSpecialRegister(2, status, mips64.cop.regs.Status.value)
    }

    private fun load(address: ULong, dtyp: Datatype): ULong = mips64.read(dtyp, address, 0)
    private fun store(address: ULong, data: ULong, dtyp: Datatype) = mips64.write(dtyp, address, data, 0)

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
        putUInt(0, data, dtyp.bytes, ByteOrder.LITTLE_ENDIAN)
    }

    enum class OPCODE(val main: Int, val subOpcode: Int = 0, val helper: Int = 0) {
        Addu(0b100001, 0, 0),
        Daddu(0b101101, 0, 0),
        Madd(0, 0b11100),
        Xor(0b100110, 0, 0),
        Sd(0b111111),
        Subu(0b100011, 0, 0),
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
        mips64.reset()
        mips64.cpu.pc = startAddress
        mips64.cop.regs.Status.FR = true
        mips64.cop.regs.Status.EXL = false
    }

    @After
    fun checkPC() {
        // standard exception vector address 0x80000180
        val expected = if (mips64.cpu.exception == null) startAddress + size else 0x80000180uL
        Assert.assertEquals(
            "Program counter error: ${(startAddress + size).hex8} != ${mips64.cpu.pc.hex8}",
            expected, mips64.cpu.pc
        )
    }

    @Test
    fun dadduTest() {
        regs(v0 = 0xFFFF_FFFE_AAAAu, v1 = 0xDCBAu)
        execute { rdRsRt(OPCODE.Daddu, 1, 2, 3) }
        assertAssembly("daddu \$at, \$v0, \$v1")
        assertRegisters(at = 0x0000_FFFF_FFFF_8764u, v0 = 0x0000_FFFF_FFFE_AAAAu, v1 = 0xDCBAu)
    }

    @Test
    fun adduTestPositive() {
        regs(v0 = 0xFFFE_AAAAu, v1 = 0xDCBAu)
        execute { rdRsRt(OPCODE.Addu, 1, 2, 3) }
        assertAssembly("addu \$at, \$v0, \$v1")
        assertRegisters(at = 0xFFFF_FFFF_FFFF_8764u, v0 = 0xFFFE_AAAAu, v1 = 0xDCBAu)
    }

    @Test
    fun sdTest() {
        regs(k0 = 0xFFFF_FFFF_FFFF_BB53u, k1 = 0xFFFF_FFFF_8000_0000u)
        execute { rtBaseOffset(OPCODE.Sd, 26, 27, 0x7FF0u) }
        assertAssembly("sd \$k0, qword [\$k1+0x7FF0]")
        assertMemory(0xFFFF_FFFF_8000_7FF0u, 0xFFFF_FFFF_FFFF_BB53u, Datatype.QWORD)
    }

    @Test
    fun `subu 64 small Test`() {
        regs(t7 = 0x1u, s2 = 0x0u)
        execute { rdRsRt(OPCODE.Subu, 12, 15, 18) }
        assertAssembly("subu \$t4, \$t7, \$s2")
        assertRegisters(t4 = 0x1u, t7 = 0x1u, s2 = 0x0u)
    }

    @Test
    fun `subu фигня из ядра`() {
        regs(t7 = 0x0u, s2 = 0x1u)
        execute { rdRsRt(OPCODE.Subu, 12, 15, 18) }
        assertAssembly("subu \$t4, \$t7, \$s2")
        assertRegisters(t4 = 0xffffffffffffffffu, t7 = 0x0u, s2 = 0x1u)
    }


}