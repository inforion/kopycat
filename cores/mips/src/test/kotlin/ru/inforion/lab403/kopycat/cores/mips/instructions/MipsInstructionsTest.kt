package ru.inforion.lab403.kopycat.cores.mips.instructions

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
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

/**
 * Created by user on 25.07.17.
 */

class MipsInstructionsTest: Module(null, "top") {
    private val mips = MipsCore(
            this, 
            "mips",
            400.MHz,
            1.0,
            1,
            0x000000A7,
            30, 
            1)
    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem", BUS30)
    }
    override val buses = Buses()
    private val ram0 = RAM(this, "ram0", 0x0FFF_FFFF)
    init {
        mips.ports.mem.connect(buses.mem)
        ram0.ports.mem.connect(buses.mem, 0)
        this.initializeAndResetAsTopInstance()
    }

    var size = 0
    private val startAddress: Long = 0x8000_0000

    private fun execute(offset: Int = 0, generate: () -> ByteArray) {
        val data = generate()
        mips.store(startAddress + size, data)
        mips.execute()
        println("%16s -> %s".format(data.hexlify(), mips.cpu.insn))
        size += data.size + offset
    }

    private fun assertAssembly(expected: String) = Assert.assertEquals("Unexpected disassembly view!", expected, mips.cpu.insn.toString())

    private fun assertRegister(num: Int, expected: Long, actual: Long, type: String = "GPR") =
            Assert.assertEquals("${mips.cpu.insn} -> $type $num error: 0x${expected.hex8} != 0x${actual.hex8}", expected, actual)

    private fun assertRegisters(zero: Long = 0, at: Long = 0, v0: Long = 0, v1: Long = 0, a0: Long = 0, a1: Long = 0,
                                a2: Long = 0, a3: Long = 0, t0: Long = 0, t1: Long = 0, t2: Long = 0, t3: Long = 0, t4: Long = 0,
                                t5: Long = 0, t6: Long = 0, t7: Long = 0, s0: Long = 0, s1: Long = 0, s2: Long = 0, s3: Long = 0,
                                s4: Long = 0, s5: Long = 0, s6: Long = 0, s7: Long = 0, t8: Long = 0, t9: Long = 0, k0: Long = 0,
                                k1: Long = 0, gp: Long = 0, sp: Long = 0, fp: Long = 0, ra: Long = 0) {
        assertRegister(0, zero, mips.cpu.regs.zero)
        assertRegister(1, at, mips.cpu.regs.at)
        assertRegister(2, v0, mips.cpu.regs.v0)
        assertRegister(3, v1, mips.cpu.regs.v1)
        assertRegister(4, a0, mips.cpu.regs.a0)
        assertRegister(5, a1, mips.cpu.regs.a1)
        assertRegister(6, a2, mips.cpu.regs.a2)
        assertRegister(7, a3, mips.cpu.regs.a3)
        assertRegister(8, t0, mips.cpu.regs.t0)
        assertRegister(9, t1, mips.cpu.regs.t1)
        assertRegister(10, t2, mips.cpu.regs.t2)
        assertRegister(11, t3, mips.cpu.regs.t3)
        assertRegister(12, t4, mips.cpu.regs.t4)
        assertRegister(13, t5, mips.cpu.regs.t5)
        assertRegister(14, t6, mips.cpu.regs.t6)
        assertRegister(15, t7, mips.cpu.regs.t7)
        assertRegister(16, s0, mips.cpu.regs.s0)
        assertRegister(17, s1, mips.cpu.regs.s1)
        assertRegister(18, s2, mips.cpu.regs.s2)
        assertRegister(19, s3, mips.cpu.regs.s3)
        assertRegister(20, s4, mips.cpu.regs.s4)
        assertRegister(21, s5, mips.cpu.regs.s5)
        assertRegister(22, s6, mips.cpu.regs.s6)
        assertRegister(23, s7, mips.cpu.regs.s7)
        assertRegister(24, t8, mips.cpu.regs.t8)
        assertRegister(25, t9, mips.cpu.regs.t9)
        assertRegister(26, k0, mips.cpu.regs.k0)
        assertRegister(27, k1, mips.cpu.regs.k1)
        assertRegister(28, gp, mips.cpu.regs.gp)
        assertRegister(29, sp, mips.cpu.regs.sp)
        assertRegister(30, fp, mips.cpu.regs.fp)
        assertRegister(31, ra, mips.cpu.regs.ra)
    }

    private fun regs(at: Long = 0, v0: Long = 0, v1: Long = 0, a0: Long = 0, a1: Long = 0, a2: Long = 0, a3: Long = 0,
                     t0: Long = 0, t1: Long = 0, t2: Long = 0, t3: Long = 0, t4: Long = 0, t5: Long = 0, t6: Long = 0,
                     t7: Long = 0, s0: Long = 0, s1: Long = 0, s2: Long = 0, s3: Long = 0, s4: Long = 0, s5: Long = 0,
                     s6: Long = 0, s7: Long = 0, t8: Long = 0, t9: Long = 0, k0: Long = 0, k1: Long = 0, gp: Long = 0,
                     sp: Long = 0, fp: Long = 0, ra: Long = 0) {
        mips.cpu.regs.at = at
        mips.cpu.regs.v0 = v0
        mips.cpu.regs.v1 = v1
        mips.cpu.regs.a0 = a0
        mips.cpu.regs.a1 = a1
        mips.cpu.regs.a2 = a2
        mips.cpu.regs.a3 = a3
        mips.cpu.regs.t0 = t0
        mips.cpu.regs.t1 = t1
        mips.cpu.regs.t2 = t2
        mips.cpu.regs.t3 = t3
        mips.cpu.regs.t4 = t4
        mips.cpu.regs.t5 = t5
        mips.cpu.regs.t6 = t6
        mips.cpu.regs.t7 = t7
        mips.cpu.regs.s0 = s0
        mips.cpu.regs.s1 = s1
        mips.cpu.regs.s2 = s2
        mips.cpu.regs.s3 = s3
        mips.cpu.regs.s4 = s4
        mips.cpu.regs.s5 = s5
        mips.cpu.regs.s6 = s6
        mips.cpu.regs.s7 = s7
        mips.cpu.regs.t8 = t8
        mips.cpu.regs.t9 = t9
        mips.cpu.regs.k0 = k0
        mips.cpu.regs.k1 = k1
        mips.cpu.regs.gp = gp
        mips.cpu.regs.sp = sp
        mips.cpu.regs.fp = fp
        mips.cpu.regs.ra = ra
    }

    private fun assertSpecialRegister(num: Int, expected: Long, actual: Long, type: String = "SRVC") =
            Assert.assertEquals("${mips.cpu.insn} -> $type $num error: 0x${expected.hex8} != 0x${actual.hex8}", expected, actual)

    private fun specialRegs(hi: Long = 0, lo: Long = 0, status: Long = 0) {
        mips.cpu.hi = hi
        mips.cpu.lo = lo
        mips.cop.regs.Status = status
    }

    private fun assertSpecialRegisters(hi: Long = 0, lo: Long = 0, status: Long = 0) {
        assertSpecialRegister(0, hi, mips.cpu.hi)
        assertSpecialRegister(1, lo, mips.cpu.lo)
        assertSpecialRegister(2, status, mips.cop.regs.Status)
    }

    private fun load(address: Long, dtyp: Datatype): Long = mips.read(dtyp, address, 0)
    private fun store(address: Long, data: Long, dtyp: Datatype) = mips.write(dtyp, address, data, 0)

    private fun assertMemory(address: Long, expected: Long, dtyp: Datatype) {
        val actual = load(address, dtyp)
        Assert.assertEquals("Memory 0x${address.hex8} error: $expected != $actual", expected, actual)
    }

    private fun assertDelaySlot(offset: Int = 0, isBranch: Boolean) {
        if (isBranch) {
            regs(t7 = 0xA781_4EF2, s7 = 0xC796_32FE)
            execute(offset = offset) { rdRsRt(MipsInstructionsTest.OPCODE.Xor, 4, 15, 23) }
            assertAssembly("xorr \$a0, \$t7, \$s7")
            assertRegisters(a0 = 0x6017_7C0C, t7 = 0xA781_4EF2, s7 = 0xC796_32FE)
        } else {
            size += 4 // For "LIKELY" instructions
            specialRegs(hi = 0x1F57_AC90, lo = 0x2F56_AB21)
            regs(sp = 0x2856_FE53, v1 = 0x4563_D752)
            execute { rsRt(MipsInstructionsTest.OPCODE.Madd, 29, 3) }
            assertAssembly("madd \$zero, \$sp, \$v1")
            assertSpecialRegisters(hi = 0x2A46_DAAE, lo = 0xE3E4_D6B7)
            assertRegisters(sp = 0x2856_FE53, v1 = 0x4563_D752)
        }
    }

    private fun toBuffer(dtyp: Datatype, data: Long): ByteArray = ByteArray(dtyp.bytes).apply {
        putInt(0, data, dtyp.bytes, LITTLE_ENDIAN)
    }

    enum class OPCODE (val main: Int, val subOpcode: Int = 0, val helper: Int = 0) {
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

    private fun rdRsRt(opcode: MipsInstructionsTest.OPCODE, rd: Int, rs: Int, rt: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.main, 5..0)
                    .insert(0, 10..6)
                    .insert(opcode.helper, 6)
                    .insert(rd, 15..11)
                    .insert(rt, 20..16)
                    .insert(rs, 25..21)
                    .insert(opcode.subOpcode, 31..26)
                    .asULong)

    private fun rtRsImm(opcode: MipsInstructionsTest.OPCODE, rt: Int, rs: Int, imm: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.main, 31..26)
                    .insert(rs, 25..21)
                    .insert(rt, 20..16)
                    .insert(imm, 15..0)
                    .asULong)

    private fun rsRt(opcode: MipsInstructionsTest.OPCODE, rs: Int, rt: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.main, 5..0)
                    .insert(0, 15..6)
                    .insert(rt, 20..16)
                    .insert(rs, 25..21)
                    .insert(opcode.subOpcode, 31..26)
                    .asULong)

    private fun rt(opcode: MipsInstructionsTest.OPCODE, rt: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.main, 31..26)
                    .insert(opcode.subOpcode, 25..21)
                    .insert(rt, 20..16)
                    .insert(0b01100, 15..11)
                    .insert(0, 10..6)
                    .insert(opcode.helper, 5)
                    .insert(0, 4..0)
                    .asULong)

    private fun rdRt(opcode: MipsInstructionsTest.OPCODE, rd: Int, rt: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.subOpcode, 31..26)
                    .insert(0, 25..21)
                    .insert(rt, 20..16)
                    .insert(rd, 15..11)
                    .insert(opcode.main, 10..6)
                    .insert(opcode.helper, 5..0)
                    .asULong)

    private fun rtImm(opcode: MipsInstructionsTest.OPCODE, rt: Int, imm: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.main, 31..26)
                    .insert(0, 25..21)
                    .insert(rt, 20..16)
                    .insert(imm, 15..0)
                    .asULong)

    private fun rsRtMsbLsb(opcode: MipsInstructionsTest.OPCODE, rt: Int, rs: Int, pos: Int, size: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.subOpcode, 31..26)
                    .insert(rs, 25..21)
                    .insert(rt, 20..16)
                    .insert(pos + size - 1, 15..11)
                    .insert(pos, 10..6)
                    .insert(opcode.main, 5..0)
                    .asULong)

    private fun rsRtMsbdLsb(opcode: MipsInstructionsTest.OPCODE, rt: Int, rs: Int, pos: Int, size: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.subOpcode, 31..26)
                    .insert(rs, 25..21)
                    .insert(rt, 20..16)
                    .insert(size - 1, 15..11)
                    .insert(pos, 10..6)
                    .insert(opcode.main, 5..0)
                    .asULong)

    private fun rd(opcode: MipsInstructionsTest.OPCODE, rd: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.main, 5..0)
                    .insert(0, 10..6)
                    .insert(rd, 15..11)
                    .insert(0, 25..16)
                    .insert(opcode.subOpcode, 31..26)
                    .asULong)

    private fun rs(opcode: MipsInstructionsTest.OPCODE, rs: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.subOpcode, 31..26)
                    .insert(rs, 25..21)
                    .insert(0, 20..6)
                    .insert(opcode.main, 5..0)
                    .asULong)

    private fun rtBaseOffset(opcode: MipsInstructionsTest.OPCODE, rt: Int, base: Int, offset: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.main, 31..26)
                    .insert(base, 25..21)
                    .insert(rt, 20..16)
                    .insert(offset, 15..0)
                    .asULong)

    private fun rsOffset(opcode: MipsInstructionsTest.OPCODE, rs: Int, offset: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.subOpcode, 31..26)
                    .insert(rs, 25..21)
                    .insert(opcode.main, 20..16)
                    .insert(offset, 15..0)
                    .asULong)

    private fun rdRtSa(opcode: MipsInstructionsTest.OPCODE, rd: Int, rt: Int, sa: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.main, 5..0)
                    .insert(sa, 10..6)
                    .insert(rd, 15..11)
                    .insert(rt, 20..16)
                    .insert(opcode.helper, 21)
                    .insert(0, 25..22)
                    .insert(opcode.subOpcode, 31..26)
                    .asULong)

    private fun instrIndex(opcode: MipsInstructionsTest.OPCODE, instrIndex: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.main, 31..26)
                    .insert(instrIndex, 25..0)
                    .asULong)

    private fun rdRsHint(opcode: MipsInstructionsTest.OPCODE, rd: Int, rs: Int): ByteArray =
            toBuffer(Datatype.DWORD, insert(opcode.subOpcode, 31..26)
                    .insert(rs, 25..21)
                    .insert(0, 20..16)
                    .insert(rd, 15..11)
                    .insert(0, 10..6)
                    .insert(opcode.main, 5..0)
                    .asULong)

    @Before fun resetTest() {
        mips.reset()
        mips.cpu.pc = startAddress
    }

    @After fun checkPC() {
        Assert.assertEquals("Program counter error: ${(startAddress + size).hex8} != ${mips.cpu.pc.hex8}",
                startAddress + size, mips.cpu.pc)
    }

    @Test fun addTest() {
        regs(v0 = 0xFFFE_AAAA, v1 = 0xDCBA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Add, 1, 2, 3) }
        assertAssembly("add \$at, \$v0, \$v1")
        assertRegisters(at = 0xFFFF_8764, v0 = 0xFFFE_AAAA, v1 = 0xDCBA)
    }

    @Test fun addTestOverflow() {
        regs(v0 = 0x7FFF_FFFF, v1 = 0x7FFF_FFFF)
        execute(offset = -4) { rdRsRt(MipsInstructionsTest.OPCODE.Add, 1, 2, 3) }
        assertTrue { mips.cpu.exception is MipsHardwareException.OV }
    }

    @Test fun addiTestPositive() {
        regs(t0 = 0xFFFF_FFFF, t1 = 0xFFFE_BABA)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Addi, 8, 9, 0x7AFE) }
        assertAssembly("addi \$t0, \$t1, 0x7AFE")
        assertRegisters(t0 = 0xFFFF_35B8, t1 = 0xFFFE_BABA)
    }

    @Test fun addiTestNegative() {
        regs(t0 = 0xFFFF_FFFF, t1 = 0xBABA)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Addi, 8, 9, 0xCAFE) }
        assertAssembly("addi \$t0, \$t1, -0x3502")
        assertRegisters(t0 = 0x85B8, t1 = 0xBABA)
    }

    @Test fun addiTestOverflow() {
        regs(t0 = 0xFFFF_FFFF, t1 = 0x7FFF_FFFF)
        execute(offset = -4) { rtRsImm(MipsInstructionsTest.OPCODE.Addi, 8, 9, 0x7FFF) }
        assertTrue { mips.cpu.exception is MipsHardwareException.OV }
    }

    @Test fun addiuTestPositive() {
        regs(t0 = 0xFFFF_FFFF, t1 = 0xBABA)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Addiu, 8, 9, 0x7AFE) }
        assertAssembly("addiu \$t0, \$t1, 0x7AFE")
        assertRegisters(t0 = 0x1_35B8, t1 = 0xBABA)
    }

    @Test fun addiuTestNegative() {
        regs(t0 = 0xFFFF_FFFF, t1 = 0xBABA)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Addiu, 8, 9, 0xCAFE) }
        assertAssembly("addiu \$t0, \$t1, 0xCAFE")
        assertRegisters(t0 = 0x85B8, t1 = 0xBABA)
    }

    @Test fun addiuTestOverflow() {
        regs(t0 = 0xFFFF_FFFF, t1 = 0xFFFF_BABA)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Addiu, 8, 9, 0x7AFE) }
        assertAssembly("addiu \$t0, \$t1, 0x7AFE")
        assertRegisters(t0 = 0x35B8, t1 = 0xFFFF_BABA)
    }

    @Test fun adduTestPositive() {
        regs(v0 = 0xFFFE_AAAA, v1 = 0xDCBA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Addu, 1, 2, 3) }
        assertAssembly("addu \$at, \$v0, \$v1")
        assertRegisters(at = 0xFFFF_8764, v0 = 0xFFFE_AAAA, v1 = 0xDCBA)
    }

    @Test fun adduTestNegative() {
        regs(v0 = 0xFFFE_AAAA, v1 = 0xFFFF_DCBA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Addu, 1, 2, 3) }
        assertAssembly("addu \$at, \$v0, \$v1")
        assertRegisters(at = 0xFFFE_8764, v0 = 0xFFFE_AAAA, v1 = 0xFFFF_DCBA)
    }

    @Test fun adduTestOverflow() {
        regs(v0 = 0xFFFE_AAAA, v1 = 0x1_DCBA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Addu, 1, 2, 3) }
        assertAssembly("addu \$at, \$v0, \$v1")
        assertRegisters(at = 0x8764, v0 = 0xFFFE_AAAA, v1 = 0x1_DCBA)
    }

    @Test fun andTest() {
        regs(s0 = 0x986D_1A2F, a3 = 0x8475_6321)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.And, 24, 16, 7) }
        assertAssembly("and \$t8, \$s0, \$a3")
        assertRegisters(t8 = 0x8065_0221, s0 = 0x986D_1A2F, a3 = 0x8475_6321)

        regs(s1 = 0xCA_FFCA, s2 = 0xBABA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.And, 16, 17, 18) }
        assertAssembly("and \$s0, \$s1, \$s2")
        assertRegisters(s0 = 0xBA8A, s1 = 0xCAFFCA, s2 = 0xBABA)
    }

    @Test fun andiTest() {
        regs(t8 = 0x8065_0221, s0 = 0x986D_1A2F)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Andi, 24, 16, 0x6321) }
        assertAssembly("andi \$t8, \$s0, 0x6321")
        assertRegisters(t8 = 0x221, s0 = 0x986D_1A2F)

        regs(s0 = 0xFFFF_FFFF, s1 = 0xCA_FFCA)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Andi, 16, 17, 0xBABA) }
        assertAssembly("andi \$s0, \$s1, 0xBABA")
        assertRegisters(s0 = 0xBA8A, s1 = 0xCAFFCA)
    }

    @Test fun beqTest() {
        regs(s0 = 0xFACC_BABA, s1 = 0xFACC_BABA)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Beq, 16, 17, 0x6321) }
        assertAssembly("beq \$s1, \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun beqlTestTrue() {
        regs(s0 = 0xFACC_BABA, s1 = 0xFACC_BABA)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Beql, 16, 17, 0x6321) }
        assertAssembly("beql \$s1, \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun beqlTestFalse() {
        regs(s0 = 0xFACC_BABA, s1 = 0xFACC_BABB)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Beql, 16, 17, 0x6321) }
        assertAssembly("beql \$s1, \$s0, 00018C84")
        assertDelaySlot(0x18C80, false)
    }

    @Test fun bgezTest() {
        regs(s0 = 0x7ACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bgez, 16, 0x6321) }
        assertAssembly("bgez \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bgezTestZero() {
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bgez, 16, 0x6321) }
        assertAssembly("bgez \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bgezalTest() {
        regs(s0 = 0x7ACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bgezal, 16, 0x6321) }
        assertAssembly("bgezal \$s0, 00018C84")
        assertRegisters(s0 = 0x7ACC_BABA, ra = 0x8000_0008)
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bgezalTestZero() {
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bgezal, 16, 0x6321) }
        assertAssembly("bgezal \$s0, 00018C84")
        assertRegisters(ra = 0x8000_0008)
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bgezallTestTrue() {
        regs(s0 = 0xACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bgezall, 16, 0x6321) }
        assertAssembly("bgezall \$s0, 00018C84")
        assertRegisters(s0 = 0xACC_BABA, ra = 0x8000_0008)
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bgezallTestFalse() {
        regs(s0 = 0xFACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bgezall, 16, 0x6321) }
        assertAssembly("bgezall \$s0, 00018C84")
        assertRegisters(s0 = 0xFACC_BABA, ra = 0x8000_0008)
        assertDelaySlot(0x18C80, false)
    }

    @Test fun bgezallTestZero() {
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bgezall, 16, 0x6321) }
        assertAssembly("bgezall \$s0, 00018C84")
        assertRegisters(ra = 0x8000_0008)
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bgezlTestTrue() {
        regs(s0 = 0xACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bgezl, 16, 0x6321) }
        assertAssembly("bgezl \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bgezlTestFalse() {
        regs(s0 = 0xFACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bgezl, 16, 0x6321) }
        assertAssembly("bgezl \$s0, 00018C84")
        assertDelaySlot(0x18C80, false)
    }

    @Test fun bgezlTestZero() {
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bgezl, 16, 0x6321) }
        assertAssembly("bgezl \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bgtzTest() {
        regs(s0 = 0xACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bgtz, 16, 0x6321) }
        assertAssembly("bgtz \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bgtzlTestTrue() {
        regs(s0 = 0xACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bgtzl, 16, 0x6321) }
        assertAssembly("bgtzl \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bgtzlFalse() {
        regs(s0 = 0xFACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bgtzl, 16, 0x6321) }
        assertAssembly("bgtzl \$s0, 00018C84")
        assertDelaySlot(0x18C80, false)
    }

    @Test fun bgtzlZero() {
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bgtzl, 16, 0x6321) }
        assertAssembly("bgtzl \$s0, 00018C84")
        assertDelaySlot(0x18C80, false)
    }

    @Test fun blezTest() {
        regs(s0 = 0xBACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Blez, 16, 0x6321) }
        assertAssembly("blez \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun blezTestZero() {
        execute { rsOffset(MipsInstructionsTest.OPCODE.Blez, 16, 0x6321) }
        assertAssembly("blez \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun blezlTestTrue() {
        regs(s0 = 0xFACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Blezl, 16, 0x6321) }
        assertAssembly("blezl \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun blezlTestFalse() {
        regs(s0 = 0xACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Blezl, 16, 0x6321) }
        assertAssembly("blezl \$s0, 00018C84")
        assertDelaySlot(0x18C80, false)
    }

    @Test fun blezlTestZero() {
        execute { rsOffset(MipsInstructionsTest.OPCODE.Blezl, 16, 0x6321) }
        assertAssembly("blezl \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bltzTest() {
        regs(s0 = 0xFACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bltz, 16, 0x6321) }
        assertAssembly("bltz \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bltzalTest() {
        regs(s0 = 0xBACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bltzal, 16, 0x6321) }
        assertAssembly("bltzal \$s0, 00018C84")
        assertRegisters(s0 = 0xBACC_BABA, ra = 0x8000_0008)
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bltzallTestTrue() {
        regs(s0 = 0xFACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bltzall, 16, 0x6321) }
        assertAssembly("bltzall \$s0, 00018C84")
        assertRegisters(s0 = 0xFACC_BABA, ra = 0x8000_0008)
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bltzallTestFalse() {
        regs(s0 = 0xACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bltzall, 16, 0x6321) }
        assertAssembly("bltzall \$s0, 00018C84")
        assertRegisters(s0 = 0xACC_BABA, ra = 0x8000_0008)
        assertDelaySlot(0x18C80, false)
    }

    @Test fun bltzallTestZero() {
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bltzall, 16, 0x6321) }
        assertAssembly("bltzall \$s0, 00018C84")
        assertRegisters(ra = 0x8000_0008)
        assertDelaySlot(0x18C80, false)
    }

    @Test fun bltzlTestTrue() {
        regs(s0 = 0xFACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bltzl, 16, 0x6321) }
        assertAssembly("bltzl \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bltzlFalse() {
        regs(s0 = 0xACC_BABA)
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bltzl, 16, 0x6321) }
        assertAssembly("bltzl \$s0, 00018C84")
        assertDelaySlot(0x18C80, false)
    }

    @Test fun bltzlZero() {
        execute { rsOffset(MipsInstructionsTest.OPCODE.Bltzl, 16, 0x6321) }
        assertAssembly("bltzl \$s0, 00018C84")
        assertDelaySlot(0x18C80, false)
    }

    @Test fun bneTest() {
        regs(s0 = 0xFACC_BACA, s1 = 0xFACC_BABA)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Bne, 16, 17, 0x6321) }
        assertAssembly("bne \$s1, \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bnelTestTrue() {
        regs(s0 = 0xFACC_BACA, s1 = 0xFACC_BABA)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Bnel, 16, 17, 0x6321) }
        assertAssembly("bnel \$s1, \$s0, 00018C84")
        assertDelaySlot(0x18C80, true)
    }

    @Test fun bnelTestFalse() {
        regs(s0 = 0xFACC_BABA, s1 = 0xFACC_BABA)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Bnel, 16, 17, 0x6321) }
        assertAssembly("bnel \$s1, \$s0, 00018C84")
        assertDelaySlot(0x18C80, false)
    }

    @Test fun cloTest() {
        regs(s0 = 0xFA01_4759, t8 = 0x8475_6321)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Clo, 24, 16, 16) }
        assertAssembly("clo \$t8, \$s0, \$s0")
        assertRegisters(s0 = 0xFA01_4759, t8 = 0x5)

        regs(s0 = 0x28_822F, t8 = 0x8475_6321)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Clo, 24, 16, 16) }
        assertAssembly("clo \$t8, \$s0, \$s0")
        assertRegisters(s0 = 0x28822F, t8 = 0x0)
    }

    @Test fun clzTest() {
        regs(s0 = 0xFA01_4759, t8 = 0x8475_6321)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Clz, 24, 16, 16) }
        assertAssembly("clz \$t8, \$s0, \$s0")
        assertRegisters(s0 = 0xFA01_4759, t8 = 0x0)

        regs(s0 = 0x28_822F, t8 = 0x8475_6321)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Clz, 24, 16, 16) }
        assertAssembly("clz \$t8, \$s0, \$s0")
        assertRegisters(s0 = 0x28822F, t8 = 0xA)
    }

    @Test fun diTest() {
        regs(ra = 0x7642_BA52)
        specialRegs(status = 0x0024_F44B)
        execute { rt(MipsInstructionsTest.OPCODE.Di, 31) }
        assertAssembly("di \$ra")
        assertRegisters(ra = 0x0024_F44B)
        assertSpecialRegisters(status = 0x0024_F44A)

        regs()
        specialRegs(status = 0x0024_F44B)
        execute { rt(MipsInstructionsTest.OPCODE.Di, 0) }
        assertAssembly("di \$zero")
        assertRegisters(zero = 0)
        assertSpecialRegisters(status = 0x0024_F44A)
    }

    @Test fun divTestPositive() {
        regs(t0 = 0x1_4FCB, t1 = 0x6)
        execute { rsRt(MipsInstructionsTest.OPCODE.Div, 8, 9) }
        assertAssembly("div \$zero, \$t0, \$t1")
        assertSpecialRegisters(hi = 0x1, lo = 0x37F7)
        assertRegisters(t0 = 0x1_4FCB, t1 = 0x6)
    }

    @Test fun divTestNegative() {
        regs(t0 = 0xFFFE_B035, t1 = 0x6)
        execute { rsRt(MipsInstructionsTest.OPCODE.Div, 8, 9) }
        assertAssembly("div \$zero, \$t0, \$t1")
        assertSpecialRegisters(hi = 0xFFFF_FFFF, lo = 0xFFFF_C809)
        assertRegisters(t0 = 0xFFFE_B035, t1 = 0x6)
    }

    @Test fun divuTestPositive() {
        regs(t0 = 0x1_4FCB, t1 = 0x6)
        execute { rsRt(MipsInstructionsTest.OPCODE.Divu, 8, 9) }
        assertAssembly("divu \$zero, \$t0, \$t1")
        assertSpecialRegisters(hi = 0x1, lo = 0x37F7)
        assertRegisters(t0 = 0x1_4FCB, t1 = 0x6)
    }

    @Test fun divuTestNegative() {
        regs(t0 = 0xFFFE_B035, t1 = 0x6)
        execute { rsRt(MipsInstructionsTest.OPCODE.Divu, 8, 9) }
        assertAssembly("divu \$zero, \$t0, \$t1")
        assertSpecialRegisters(hi = 0x3, lo = 0x2AAA_72B3)
        assertRegisters(t0 = 0xFFFE_B035, t1 = 0x6)
    }

    @Test fun eiTest() {
        regs(ra = 0x7642_BA52)
        specialRegs(status = 0x0024_F44A)
        execute { rt(MipsInstructionsTest.OPCODE.Ei, 31) }
        assertAssembly("ei \$ra")
        assertRegisters(ra = 0x0024_F44A)
        assertSpecialRegisters(status = 0x0024_F44B)

        regs()
        specialRegs(status = 0x0024_F44A)
        execute { rt(MipsInstructionsTest.OPCODE.Ei, 0) }
        assertAssembly("ei \$zero")
        assertRegisters(zero = 0)
        assertSpecialRegisters(status = 0x0024_F44B)
    }

    @Test fun extTest() {
        regs(s5 = 0x4F56_DD58, s6 = 0x1693_98E5)
        execute { rsRtMsbdLsb(MipsInstructionsTest.OPCODE.Ext, 21, 22, 0x8, 0xC) }
        // Does not match the documentation
        assertAssembly("ext \$s5, \$s6, 0x8, 0xB")
        assertRegisters(s5 = 0x398, s6 = 0x1693_98E5)
    }

    @Test fun insTest() {
        regs(s6 = 0x1693_98E5, s7 = 0x4F56_DD58)
        execute { rsRtMsbLsb(MipsInstructionsTest.OPCODE.Ins, 22, 23, 0x8, 0xC) }
        // Does not match the documentation
        assertAssembly("ins \$s6, \$s7, 0x8, 0x13")
        assertRegisters(s6 = 0x169D_58E5, s7 = 0x4F56_DD58)
    }

    @Test fun jTest() {
        execute { instrIndex(MipsInstructionsTest.OPCODE.J, 0x8F_7A41) }
        // 0x8F_7A41 shl 2 = 0x23D_E904
        assertAssembly("j 023DE904")
        // 2 instr done: PC = 0x8000_0008
        // Then PC = 0x823D_E904
        // Offset = 0x823D_E904 - 0x8000_0008 = 0x23D_E8FC
        assertDelaySlot(0x23D_E8FC, true)
    }

    @Test fun jalTest() {
        execute { instrIndex(MipsInstructionsTest.OPCODE.Jal, 0x8F_7A41) }
        // 0x8F_7A41 shl 2 = 0x23D_E904
        assertAssembly("jal 023DE904")
        assertRegisters(ra = 0x8000_0008)
        // 2 instr done: PC = 0x8000_0008
        // Then PC = 0x823D_E904
        // Offset = 0x823D_E904 - 0x8000_0008 = 0x23D_E8FC
        assertDelaySlot(0x23D_E8FC, true)
    }

    @Test fun jalrTest() {
        regs(s1 = 0xFFFF_FFFF, v0 = 0x80C6_A781)
        execute { rdRsHint(MipsInstructionsTest.OPCODE.Jalr, 17, 2) }
        // 0x8F_7A41 shl 2 = 0x23D_E904
        assertAssembly("jalr \$s1, \$v0, 0x0")
        assertRegisters(s1 = 0x8000_0008, v0 = 0x80C6_A781)
        // 2 instr done: PC = 0x8000_0008
        // Then PC = 0x80C6_A781
        // Offset = 0x80C6_A781 - 0x8000_0008 = 0xC6_A779
        assertDelaySlot(0xC6_A779, true)
    }

    @Test fun jrTest() {
        regs(v0 = 0x80C6_A781)
        execute { rdRsHint(MipsInstructionsTest.OPCODE.Jr, 0, 2) }
        // 0x8F_7A41 shl 2 = 0x23D_E904
        assertAssembly("jr \$v0")
        assertRegisters(v0 = 0x80C6_A781)
        // 2 instr done: PC = 0x8000_0008
        // Then PC = 0x80C6_A781
        // Offset = 0x80C6_A781 - 0x8000_0008 = 0xC6_A779
        assertDelaySlot(0xC6_A779, true)
    }

    @Test fun lbTestPositive() {
        store(0x8000_FFFF, 0x55, Datatype.BYTE)
        regs(sp = 0x2856_FE53, fp = 0x8000_FF00)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Lb, 29, 30, 0xFF) }
        assertAssembly("lb \$sp, byte [\$fp+0xFF]")
        assertRegisters(sp = 0x55, fp = 0x8000_FF00)
    }

    @Test fun lbTestNegative() {
        store(0x8000_FFFF, 0xBB, Datatype.BYTE)
        regs(sp = 0x2856_FE53, a1 = 0x8000_FF00)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Lb, 29, 5, 0xFF) }
        assertAssembly("lb \$sp, byte [\$a1+0xFF]")
        assertRegisters(sp = 0xFFFF_FFBB, a1 = 0x8000_FF00)
    }

    @Test fun lbuTest() {
        store(0x8000_FFFF, 0xBB, Datatype.BYTE)
        regs(sp = 0x2856_FE53, a1 = 0x8000_FF00)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Lbu, 29, 5, 0xFF) }
        assertAssembly("lbu \$sp, byte [\$a1+0xFF]")
        assertRegisters(sp = 0xBB, a1 = 0x8000_FF00)
    }

    @Test fun lhTestPositive() {
        store(0x8001_0000, 0x77BB, Datatype.WORD)
        regs(sp = 0x2856_FE53, a1 = 0x8001_0000)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Lh, 29, 5, 0x0) }
        assertAssembly("lh \$sp, word [\$a1]")
        assertRegisters(a1 = 0x8001_0000, sp = 0x77BB)
    }

    @Test fun lhTestNegative() {
        store(0x8001_0000, 0xFFBB, Datatype.WORD)
        regs(sp = 0x2856_FE53, a1 = 0x8001_0000)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Lh, 29, 5, 0x0) }
        assertAssembly("lh \$sp, word [\$a1]")
        assertRegisters(a1 = 0x8001_0000, sp = 0xFFFF_FFBB)
    }

    @Test fun lhTestError() {
        execute(offset = -4) { rtBaseOffset(MipsInstructionsTest.OPCODE.Lh, 29, 5, 0xFF) }
        assertTrue { (mips.cpu.exception as? MemoryAccessError)?.LorS == LOAD }
    }

    @Test fun lhuTest() {
        store(0x8001_0000, 0xFFBB, Datatype.WORD)
        regs(sp = 0x2856_FE53, a1 = 0x8001_0000)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Lhu, 29, 5, 0x0) }
        assertAssembly("lhu \$sp, word [\$a1]")
        assertRegisters(sp = 0xFFBB, a1 = 0x8001_0000)
    }

    @Test fun lhuTestError() {
        execute(offset = -4) { rtBaseOffset(MipsInstructionsTest.OPCODE.Lhu, 29, 5, 0xFF) }
        assertTrue { (mips.cpu.exception as? MemoryAccessError)?.LorS == LOAD }
    }

    @Test fun llTest() {
        store(0x8001_0000, 0xFFFF_77BB, Datatype.DWORD)
        regs(sp = 0x2856_FE53, a1 = 0x8001_0000)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Ll, 29, 5, 0x0) }
        assertAssembly("ll \$sp, dword [\$a1]")
        assertRegisters(a1 = 0x8001_0000, sp = 0xFFFF_77BB)
        assertTrue { mips.cpu.llbit == 1 }
    }

    @Test fun luiTest() {
        regs(k0 = 0x2856_FE53)
        execute { rtImm(MipsInstructionsTest.OPCODE.Lui, 26, 0xFF89) }
        assertAssembly("lui \$k0, 0xFF89")
        assertRegisters(k0 = 0xFF89_0000)
    }

    @Test fun lwTestError() {
        execute(offset = -4) { rtBaseOffset(MipsInstructionsTest.OPCODE.Lh, 29, 5, 0xFFFD) }
        assertTrue { (mips.cpu.exception as? MemoryAccessError)?.LorS == LOAD }
    }

    @Test fun lwTest() {
        store(0x8001_0000, 0xFAFF_00BB, Datatype.DWORD)
        regs(sp = 0x2856_FE53, a1 = 0x8000_FF10)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Lw, 29, 5, 0xF0) }
        assertAssembly("lw \$sp, dword [\$a1+0xF0]")
        assertRegisters(sp = 0xFAFF_00BB, a1 = 0x8000_FF10)
    }

    @Test fun lwlTest() {
        store(0x8001_0000, 0xFAFF_00BB, Datatype.DWORD)
        regs(sp = 0x2856_FE53, a1 = 0x8001_0000)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Lwl, 29, 5, 0) }
        assertAssembly("lwl \$sp, dword [\$a1]")
        assertRegisters(sp = 0xBB56_FE53, a1 = 0x8001_0000)
    }

    @Test fun lwrTest() {
        store(0x8001_0000, 0xFAFF_00BB, Datatype.DWORD)
        regs(sp = 0x2856_FE53, a1 = 0x8001_0000)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Lwr, 29, 5, 0) }
        assertAssembly("lwr \$sp, dword [\$a1]")
        assertRegisters(sp = 0xFAFF_00BB, a1 = 0x8001_0000)
    }

    @Test fun maddTestPositive() {
        specialRegs(hi = 0x1F57_AC90, lo = 0x2F56_AB21)
        regs(sp = 0x2856_FE53, v1 = 0x4563_D752)
        execute { rsRt(MipsInstructionsTest.OPCODE.Madd, 29, 3) }
        assertAssembly("madd \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0x2A46_DAAE, lo = 0xE3E4_D6B7)
        assertRegisters(sp = 0x2856_FE53, v1 = 0x4563_D752)
    }

    @Test fun maddTestNegative() {
        specialRegs(hi = 0x1F57_AC90, lo = 0x2F56_AB21)
        regs(sp = 0x8856_FE53, v1 = 0x4563_D752)
        execute { rsRt(MipsInstructionsTest.OPCODE.Madd, 29, 3) }
        assertAssembly("madd \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0xFEE8_741B, lo = 0xA3E4_D6B7)
        assertRegisters(sp = 0x8856_FE53, v1 = 0x4563_D752)
    }

    @Test fun madduTestPositive() {
        specialRegs(hi = 0x1F57_AC90, lo = 0x2F56_AB21)
        regs(sp = 0x2856_FE53, v1 = 0x4563_D752)
        execute { rsRt(MipsInstructionsTest.OPCODE.Maddu, 29, 3) }
        assertAssembly("maddu \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0x2A46_DAAE, lo = 0xE3E4_D6B7)
        assertRegisters(sp = 0x2856_FE53, v1 = 0x4563_D752)
    }

    @Test fun madduTestNegative() {
        specialRegs(hi = 0x1F57_AC90, lo = 0x2F56_AB21)
        regs(sp = 0x8856_FE53, v1 = 0xF563_D752)
        execute { rsRt(MipsInstructionsTest.OPCODE.Maddu, 29, 3) }
        assertAssembly("maddu \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0xA208_1A46, lo = 0xB3E4_D6B7)
        assertRegisters(sp = 0x8856_FE53, v1 = 0xF563_D752)
    }

    @Test fun mfhiTest() {
        regs(v1 = 0xF563_D752)
        specialRegs(hi = 0xC45A_09FF)
        execute { rd(MipsInstructionsTest.OPCODE.Mfhi, 3) }
        assertAssembly("mfhi \$v1")
        assertSpecialRegisters(hi = 0xC45A_09FF)
        assertRegisters(v1 = 0xC45A_09FF)
    }

    @Test fun mfloTest() {
        regs(v0 = 0xF563_D752)
        specialRegs(lo = 0xC45A_09FF)
        execute { rd(MipsInstructionsTest.OPCODE.Mflo, 2) }
        assertAssembly("mflo \$v0")
        assertSpecialRegisters(lo = 0xC45A_09FF)
        assertRegisters(v0 = 0xC45A_09FF)
    }

    @Test fun movnTest() {
        regs(a3 = 0x96_43AC, t9 = 0xFF78_32A4)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Movn, 7, 25, 30) }
        assertAssembly("movn \$a3, \$t9, \$fp")
        assertRegisters(a3 = 0x96_43AC, t9 = 0xFF78_32A4)

        regs(a3 = 0x96_43AC, t9 = 0xFF78_32A4, t3 = 1)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Movn, 7, 25, 11) }
        assertAssembly("movn \$a3, \$t9, \$t3")
        assertRegisters(a3 = 0xFF78_32A4, t9 = 0xFF78_32A4, t3 = 1)
    }

    @Test fun movzTest() {
        regs(a3 = 0x96_43AC, t9 = 0xFF78_32A4, t3 = 1)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Movz, 7, 25, 11) }
        assertAssembly("movz \$a3, \$t9, \$t3")
        assertRegisters(a3 = 0x96_43AC, t9 = 0xFF78_32A4, t3 = 1)

        regs(a3 = 0x96_43AC, t9 = 0xFF78_32A4)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Movz, 7, 25, 11) }
        assertAssembly("movz \$a3, \$t9, \$t3")
        assertRegisters(a3 = 0xFF78_32A4, t9 = 0xFF78_32A4)
    }

    @Test fun msubTestPositive() {
        specialRegs(hi = 0xFF93_E1D7, lo = 0x58BA_5631)
        regs(sp = 0x2856_FE53, v1 = 0x4563_D752)
        execute { rsRt(MipsInstructionsTest.OPCODE.Msub, 29, 3) }
        assertAssembly("msub \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0xF4A4_B3B8, lo = 0xA42C_2A9B)
        assertRegisters(sp = 0x2856_FE53, v1 = 0x4563_D752)
    }

    @Test fun msubTestNegative() {
        specialRegs(hi = 0xFF93_E1D7, lo = 0x58BA_5631)
        regs(sp = 0x8856_FE53, v1 = 0x4563_D752)
        execute { rsRt(MipsInstructionsTest.OPCODE.Msub, 29, 3) }
        assertAssembly("msub \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0x2003_1A4B, lo = 0xE42C_2A9B)
        assertRegisters(sp = 0x8856_FE53, v1 = 0x4563_D752)
    }

    @Test fun msubuTestPositive() {
        specialRegs(hi = 0xFF93_E1D7, lo = 0x58BA_5631)
        regs(sp = 0x2856_FE53, v1 = 0x4563_D752)
        execute { rsRt(MipsInstructionsTest.OPCODE.Msubu, 29, 3) }
        assertAssembly("msubu \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0xF4A4_B3B8, lo = 0xA42C_2A9B)
        assertRegisters(sp = 0x2856_FE53, v1 = 0x4563_D752)
    }

    @Test fun msubuTestNegative() {
        specialRegs(hi = 0xFF93_E1D7, lo = 0x58BA_5631)
        regs(sp = 0x8856_FE53, v1 = 0xF563_D752)
        execute { rsRt(MipsInstructionsTest.OPCODE.Msubu, 29, 3) }
        assertAssembly("msubu \$zero, \$sp, \$v1")
        assertSpecialRegisters(hi = 0x7CE3_7420, lo = 0xD42C_2A9B)
        assertRegisters(sp = 0x8856_FE53, v1 = 0xF563_D752)
    }

    @Test fun mthiTest() {
        regs(s5 = 0xF563_D752)
        specialRegs(hi = 0xC45A_09FF)
        execute { rs(MipsInstructionsTest.OPCODE.Mthi, 21) }
        assertAssembly("mthi \$s5")
        assertSpecialRegisters(hi = 0xF563_D752)
        assertRegisters(s5 = 0xF563_D752)
    }

    @Test fun mtloTest() {
        regs(v0 = 0xF563_D752)
        specialRegs(lo = 0xC45A_09FF)
        execute { rs(MipsInstructionsTest.OPCODE.Mtlo, 2) }
        assertAssembly("mtlo \$v0")
        assertSpecialRegisters(lo = 0xF563_D752)
        assertRegisters(v0 = 0xF563_D752)
    }

    @Test fun mulTest() {
        regs(a2 = 0xF563_D752, k1 = 0xC45A_09FF)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Mul, 9, 6, 27) }
        assertAssembly("mul \$t1, \$a2, \$k1")
        assertRegisters(a2 = 0xF563_D752, k1 = 0xC45A_09FF, t1 = 0x6BD9_5CAE)
    }

    @Test fun multTestNegative() {
        regs(a2 = 0xF563_D752, k1 = 0xC45A_09FF)
        execute { rsRt(MipsInstructionsTest.OPCODE.Mult, 27, 6) }
        assertAssembly("mult \$zero, \$k1, \$a2")
        assertSpecialRegisters(hi = 0x278_DE38, lo = 0x6BD9_5CAE)
        assertRegisters(a2 = 0xF563_D752, k1 = 0xC45A_09FF)
    }

    @Test fun multTestPositive() {
        regs(a2 = 0x2563_D752, k1 = 0x745A_09FF)
        execute { rsRt(MipsInstructionsTest.OPCODE.Mult, 27, 6) }
        assertAssembly("mult \$zero, \$k1, \$a2")
        assertSpecialRegisters(hi = 0x10FE_6420, lo = 0x9BD9_5CAE)
        assertRegisters(a2 = 0x2563_D752, k1 = 0x745A_09FF)
    }

    @Test fun multuTestNegative() {
        regs(a2 = 0xF563_D752, k1 = 0xC45A_09FF)
        execute { rsRt(MipsInstructionsTest.OPCODE.Multu, 27, 6) }
        assertAssembly("multu \$zero, \$k1, \$a2")
        assertSpecialRegisters(hi = 0xBC36_BF89, lo = 0x6BD9_5CAE)
        assertRegisters(a2 = 0xF563_D752, k1 = 0xC45A_09FF)
    }

    @Test fun multuTestPositive() {
        regs(a2 = 0x2563_D752, k1 = 0x745A_09FF)
        execute { rsRt(MipsInstructionsTest.OPCODE.Multu, 27, 6) }
        assertAssembly("multu \$zero, \$k1, \$a2")
        assertSpecialRegisters(hi = 0x10FE_6420, lo = 0x9BD9_5CAE)
        assertRegisters(a2 = 0x2563_D752, k1 = 0x745A_09FF)
    }

    @Test fun norTest() {
        regs(t7 = 0xA781_4EF2, s7 = 0xC796_32FE)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Nor, 4, 15, 23) }
        assertAssembly("nor \$a0, \$t7, \$s7")
        assertRegisters(a0 = 0x1868_8101, t7 = 0xA781_4EF2, s7 = 0xC796_32FE)
    }

    @Test fun orTest() {
        regs(t7 = 0xA781_4EF2, s7 = 0xC796_32FE)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Or, 4, 15, 23) }
        assertAssembly("or \$a0, \$t7, \$s7")
        assertRegisters(a0 = 0xE797_7EFE, t7 = 0xA781_4EF2, s7 = 0xC796_32FE)
    }

    @Test fun oriTest() {
        regs(t0 = 0xFFFF_FFFF, t1 = 0xFFFE_BABA)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Ori, 8, 9, 0x7AFE) }
        assertAssembly("ori \$t0, \$t1, 0x7AFE")
        assertRegisters(t0 = 0xFFFE_FAFE, t1 = 0xFFFE_BABA)
    }

    @Test fun rotrTest() {
        regs(t0 = 0xFFFF_FFFF, t1 = 0xCAFE_BABA)
        execute { rdRtSa(MipsInstructionsTest.OPCODE.Rotr, 8, 9, 0x10) }
        assertAssembly("rotr \$t0, \$t1, 0x10")
        assertRegisters(t0 = 0xBABA_CAFE, t1 = 0xCAFE_BABA)
    }

    @Test fun rotrvTest() {
        regs(s0 = 0xFFFF_FFFF, s1 = 0x10, s2 = 0xCAFE_BABA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Rotrv, 16, 17, 18) }
        assertAssembly("rotrv \$s0, \$s2, \$s1")
        assertRegisters(s0 = 0xBABA_CAFE, s1 = 0x10, s2 = 0xCAFE_BABA)
    }

    @Test fun sbTestPositive() {
        regs(k0 = 0x53, k1 = 0x8000_0000)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Sb, 26, 27, 0x7FFF) }
        assertAssembly("sb \$k0, byte [\$k1+0x7FFF]")
        assertMemory(0x8000_7FFF, 0x53, Datatype.BYTE)
    }

    @Test fun sbTestNegative() {
        regs(k0 = 0xFFFF_FF53, k1 = 0x8000_0000)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Sb, 26, 27, 0x7FFF) }
        assertAssembly("sb \$k0, byte [\$k1+0x7FFF]")
        assertMemory(0x8000_7FFF, 0x53, Datatype.BYTE)
    }

    @Test fun sebTest() {
        regs(k0 = 0xFFFF_FF53, k1 = 0x8000_009A)
        execute { rdRt(MipsInstructionsTest.OPCODE.Seb, 26, 27) }
        assertAssembly("seb \$k0, \$k1")
        assertRegisters(k0 = 0xFFFF_FF9A, k1 = 0x8000_009A)
    }

    @Test fun sehTest() {
        regs(k0 = 0xFFFF_FF53, k1 = 0x8000_8B9A)
        execute { rdRt(MipsInstructionsTest.OPCODE.Seh, 26, 27) }
        assertAssembly("seh \$k0, \$k1")
        assertRegisters(k0 = 0xFFFF_8B9A, k1 = 0x8000_8B9A)
    }

    @Test fun shTestPositive() {
        regs(k0 = 0xBB53, k1 = 0x8000_0000)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Sh, 26, 27, 0x7FFE) }
        assertAssembly("sh \$k0, word [\$k1+0x7FFE]")
        assertMemory(0x8000_7FFE, 0xBB53, Datatype.WORD)
    }

    @Test fun shTestNegative() {
        regs(k0 = 0xFFFF_BB53, k1 = 0x8000_0000)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Sh, 26, 27, 0x7FFE) }
        assertAssembly("sh \$k0, word [\$k1+0x7FFE]")
        assertMemory(0x8000_7FFE, 0xBB53, Datatype.WORD)
    }

    @Test fun shTestError() {
        execute(offset = -4) { rtBaseOffset(MipsInstructionsTest.OPCODE.Sh, 29, 5, 0xFF) }
        assertTrue { (mips.cpu.exception as? MemoryAccessError)?.LorS == STORE }
    }

    @Test fun sllTest() {
        regs(k0 = 0xFFFF_CAFE, k1 = 0xBABA_BABA)
        execute { rdRtSa(MipsInstructionsTest.OPCODE.Sll, 26, 27, 0x10) }
        assertAssembly("sll \$k0, \$k1, 0x10")
        assertRegisters(k0 = 0xBABA_0000, k1 = 0xBABA_BABA)
    }

    @Test fun sllvTest() {
        regs(k0 = 0xFFFF_CAFE, k1 = 0xBABA_BABA, v1 = 0x10)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Sllv, 26, 3, 27) }
        assertAssembly("sllv \$k0, \$k1, \$v1")
        assertRegisters(k0 = 0xBABA_0000, k1 = 0xBABA_BABA, v1 = 0x10)
    }

    @Test fun sltTestPositive() {
        regs(k0 = 0xFFFF_CAFE, k1 = 0xABA_BABA, v1 = 0xAFE_BABA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Slt, 26, 3, 27) }
        assertAssembly("slt \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x0, k1 = 0xABA_BABA, v1 = 0xAFE_BABA)

        regs(k0 = 0xFFFF_CAFE, k1 = 0xAFE_BABA, v1 = 0xABA_BABA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Slt, 26, 3, 27) }
        assertAssembly("slt \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x1, k1 = 0xAFE_BABA, v1 = 0xABA_BABA)
    }

    @Test fun sltTestNegative() {
        regs(k0 = 0xFFFF_CAFE, k1 = 0xBABA_BABA, v1 = 0xCAFE_BABA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Slt, 26, 3, 27) }
        assertAssembly("slt \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x0, k1 = 0xBABA_BABA, v1 = 0xCAFE_BABA)

        regs(k0 = 0xFFFF_CAFE, k1 = 0xCAFE_BABA, v1 = 0xBABA_BABA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Slt, 26, 3, 27) }
        assertAssembly("slt \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x1, k1 = 0xCAFE_BABA, v1 = 0xBABA_BABA)
    }

    @Test fun sltuTestPositive() {
        regs(k0 = 0xFFFF_CAFE, k1 = 0xABA_BABA, v1 = 0xAFE_BABA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Sltu, 26, 3, 27) }
        assertAssembly("sltu \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x0, k1 = 0xABA_BABA, v1 = 0xAFE_BABA)

        regs(k0 = 0xFFFF_CAFE, k1 = 0xAFE_BABA, v1 = 0xABA_BABA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Sltu, 26, 3, 27) }
        assertAssembly("sltu \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x1, k1 = 0xAFE_BABA, v1 = 0xABA_BABA)
    }

    @Test fun sltuTestNegative() {
        regs(k0 = 0xFFFF_CAFE, k1 = 0xBABA_BABA, v1 = 0xCAFE_BABA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Sltu, 26, 3, 27) }
        assertAssembly("sltu \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x0, k1 = 0xBABA_BABA, v1 = 0xCAFE_BABA)

        regs(k0 = 0xFFFF_CAFE, k1 = 0xCAFE_BABA, v1 = 0xBABA_BABA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Sltu, 26, 3, 27) }
        assertAssembly("sltu \$k0, \$v1, \$k1")
        assertRegisters(k0 = 0x1, k1 = 0xCAFE_BABA, v1 = 0xBABA_BABA)
    }

    @Test fun sltiTestPositive() {
        regs(k0 = 0xFFFF_CAFE, v1 = 0x6ABF)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Slti, 26, 3, 0x6ABC) }
        assertAssembly("slti \$k0, \$v1, 0x6ABC")
        assertRegisters(k0 = 0x0, v1 = 0x6ABF)

        regs(k0 = 0xFFFF_CAFE, v1 = 0x6ABA)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Slti, 26, 3, 0x6ABC) }
        assertAssembly("slti \$k0, \$v1, 0x6ABC")
        assertRegisters(k0 = 0x1, v1 = 0x6ABA)
    }

    @Test fun sltiTestNegative() {
        regs(k0 = 0xFFFF_CAFE, v1 = 0xFFFF_8ABF)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Slti, 26, 3, 0x8ABC) }
        assertAssembly("slti \$k0, \$v1, -0x7544")
        assertRegisters(k0 = 0x0, v1 = 0xFFFF_8ABF)

        regs(k0 = 0xFFFF_CAFE, v1 = 0xFFFF_8ABA)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Slti, 26, 3, 0x8ABC) }
        assertAssembly("slti \$k0, \$v1, -0x7544")
        assertRegisters(k0 = 0x1, v1 = 0xFFFF_8ABA)
    }

    @Test fun sltiuTest() {
        regs(k0 = 0xFFFF_CAFE, v1 = 0xFFFF_8ABF)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Sltiu, 26, 3, 0x8ABC) }
        assertAssembly("sltiu \$k0, \$v1, 0x8ABC")
        assertRegisters(k0 = 0x0, v1 = 0xFFFF_8ABF)

        regs(k0 = 0xFFFF_CAFE, v1 = 0xFFFF_8ABA)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Sltiu, 26, 3, 0x8ABC) }
        assertAssembly("sltiu \$k0, \$v1, 0x8ABC")
        assertRegisters(k0 = 0x1, v1 = 0xFFFF_8ABA)
    }

    @Test fun sraTestPositive() {
        regs(k0 = 0xFFFF_CAFE, k1 = 0x7ABA_BABA)
        execute { rdRtSa(MipsInstructionsTest.OPCODE.Sra, 26, 27, 0x10) }
        assertAssembly("sra \$k0, \$k1, 0x10")
        assertRegisters(k0 = 0x7ABA, k1 = 0x7ABA_BABA)
    }

    @Test fun sraTestNegative() {
        regs(k0 = 0xFFFF_CAFE, k1 = 0xBABA_BABA)
        execute { rdRtSa(MipsInstructionsTest.OPCODE.Sra, 26, 27, 0x10) }
        assertAssembly("sra \$k0, \$k1, 0x10")
        assertRegisters(k0 = 0xFFFF_BABA, k1 = 0xBABA_BABA)
    }

    @Test fun sravTestPositive() {
        regs(k0 = 0xFFFF_CAFE, k1 = 0x7ABA_BABA, v1 = 0x10)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Srav, 26, 3, 27) }
        assertAssembly("srav \$k0, \$k1, \$v1")
        assertRegisters(k0 = 0x7ABA, k1 = 0x7ABA_BABA, v1 = 0x10)
    }

    @Test fun sravTestNegative() {
        regs(k0 = 0xFFFF_CAFE, k1 = 0xBABA_BABA, v1 = 0x10)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Srav, 26, 3, 27) }
        assertAssembly("srav \$k0, \$k1, \$v1")
        assertRegisters(k0 = 0xFFFF_BABA, k1 = 0xBABA_BABA, v1 = 0x10)
    }

    @Test fun srlTest() {
        regs(k0 = 0xFFFF_CAFE, k1 = 0xBABA_BABA)
        execute { rdRtSa(MipsInstructionsTest.OPCODE.Srl, 26, 27, 0x10) }
        assertAssembly("srl \$k0, \$k1, 0x10")
        assertRegisters(k0 = 0xBABA, k1 = 0xBABA_BABA)
    }

    @Test fun srlvTest() {
        regs(k0 = 0xFFFF_CAFE, k1 = 0xBABA_BABA, v1 = 0x10)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Srlv, 26, 3, 27) }
        assertAssembly("srlv \$k0, \$k1, \$v1")
        assertRegisters(k0 = 0xBABA, k1 = 0xBABA_BABA, v1 = 0x10)
    }

    @Test fun subTest() {
        regs(t7 = 0xFFFE_AAAA, s2 = 0xDCBA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Sub, 12, 15, 18) }
        assertAssembly("sub \$t4, \$t7, \$s2")
        assertRegisters(t4 = 0xFFFD_CDF0, t7 = 0xFFFE_AAAA, s2 = 0xDCBA)
    }

    @Test fun subTestOverflow() {
        regs(v0 = 0x8000_0000, v1 = 0x1)
        execute(offset = -4) { rdRsRt(MipsInstructionsTest.OPCODE.Sub, 1, 2, 3) }
        assertTrue { mips.cpu.exception is MipsHardwareException.OV }
    }

    @Test fun subuTest() {
        regs(t7 = 0xFFFF_DCBA, s2 = 0xAAAA)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Subu, 12, 15, 18) }
        assertAssembly("subu \$t4, \$t7, \$s2")
        assertRegisters(t4 = 0xFFFF_3210, t7 = 0xFFFF_DCBA, s2 = 0xAAAA)
    }

    @Test fun swTest() {
        regs(k0 = 0xFFFF_BB53, k1 = 0x8000_0000)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Sw, 26, 27, 0x7FF0) }
        assertAssembly("sw \$k0, dword [\$k1+0x7FF0]")
        assertMemory(0x8000_7FF0, 0xFFFF_BB53, Datatype.DWORD)
    }

    @Test fun swTestError() {
        execute(offset = -4) { rtBaseOffset(MipsInstructionsTest.OPCODE.Sw, 29, 5, 0xFF) }
        assertTrue { (mips.cpu.exception as? MemoryAccessError)?.LorS == STORE }
    }

    @Test fun swlTest() {
        regs(k0 = 0x2856_FE53, k1 = 0x8000_0000)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Swl, 26, 27, 0x7FF0) }
        assertAssembly("swl \$k0, dword [\$k1+0x7FF0]")
        assertMemory(0x8000_7FF0, 0x28, Datatype.DWORD)
    }

    @Test fun swrTest() {
        regs(k0 = 0x2856_FE53, k1 = 0x8000_0000)
        execute { rtBaseOffset(MipsInstructionsTest.OPCODE.Swr, 26, 27, 0x7FF0) }
        assertAssembly("swr \$k0, dword [\$k1+0x7FF0]")
        assertMemory(0x8000_7FF0, 0x2856_FE53, Datatype.DWORD)
    }

    @Test fun wsbhTest() {
        regs(k0 = 0xFFFF_FF53, k1 = 0x8321_199A)
        execute { rdRt(MipsInstructionsTest.OPCODE.Wsbh, 26, 27) }
        assertAssembly("wsbh \$k0, \$k1")
        assertRegisters(k0 = 0x2183_9A19, k1 = 0x8321_199A)
    }

    @Test fun xorTest() {
        regs(t7 = 0xA781_4EF2, s7 = 0xC796_32FE)
        execute { rdRsRt(MipsInstructionsTest.OPCODE.Xor, 4, 15, 23) }
        assertAssembly("xorr \$a0, \$t7, \$s7")
        assertRegisters(a0 = 0x6017_7C0C, t7 = 0xA781_4EF2, s7 = 0xC796_32FE)
    }

    @Test fun xoriTest() {
        regs(t0 = 0xFFFF_FFFF, t1 = 0xFFFE_BABA)
        execute { rtRsImm(MipsInstructionsTest.OPCODE.Xori, 8, 9, 0x7AFE) }
        assertAssembly("xori \$t0, \$t1, 0x7AFE")
        assertRegisters(t0 = 0xFFFE_C044, t1 = 0xFFFE_BABA)
    }
}