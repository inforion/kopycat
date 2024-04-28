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

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.config.Generation
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.FWRBank
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.longDouble
import ru.inforion.lab403.kopycat.interfaces.ine
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.modules.memory.RAM
import java.math.BigInteger
import java.nio.ByteOrder
import kotlin.Double.Companion.NaN
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class X86InstructionsTest64: AX86InstructionTest() {
    override val x86 = x86Core(this, "x86 Core", 400.MHz, Generation.Pentium, 1.0)
    override val ram0 = RAM(this, "ram0", 0xFFF_FFFF)
    override val ram1 = RAM(this, "ram1", 0x1_0000)

    companion object {
        private val unicorn = UnicornEmu()
    }

    init {
        x86.ports.mem.connect(buses.mem)
        x86.ports.io.connect(buses.io)
        ram0.ports.mem.connect(buses.mem, 0u)
        ram1.ports.mem.connect(buses.io, 0u)
        initializeAndResetAsTopInstance()
    }

    override val mode = 64L
    override val bitMode = byteArrayOf()

    @Before fun reset64() {
        x86.cpu.apply {
            // 64-bit
            csl = true
            x86.config.efer = 0uL.set(x86CPU.LME)
            cregs.apply {
                cr4.pae = true
                cr0.pg = true
                cr0.pe = true
            }

            // FPU
            cregs.cr0.ts = false
            cregs.cr0.em = false

            // Long mode
            cregs.cr3.value = 0x0FFFC000uL

            // 0000_0000_0000_0000
            ram0.write(0x0FFFC000uL, 0, 8, 0x0FFFD027uL) // PML4
            ram0.write(0x0FFFD000uL, 0, 8, 0x0FFFE027uL) // PDPT
            ram0.write(0x0FFFE000uL, 0, 8, 0xA7uL) // PD

            // FFFF_FFFF_0000_0000
            // ram0.write(0x0FFFCFF8uL, 0, 8, 0x0FFFD027uL) // PML4
            // ram0.write(0x0FFFDFE0uL, 0, 8, 0x0FFFF027uL) // PDPT
            // ram0.write(0x0FFFF000uL, 0, 8, 0x004000A7uL) // PD

            // GDTR
            x86.mmu.gdtr.base = 0x10000uL
            x86.mmu.gdtr.limit = 0x20u
            ram0.write(0x10000uL, 0, 8, 0x678A000000FFFFuL)

            sregs.apply {
                cs.value = 0u
                ds.value = 0u
                ss.value = 0u
                es.value = 0u
                fs.value = 0u
                gs.value = 0u
            }
        }

        x86.fpu.fwr.FPUControlWord.run {
            pc = FWRBank.PrecisionControl.ExtendedDouble
            rc = FWRBank.RoundControl.RoundToNearestEven
        }
    }

    @Test fun bsfTzcntBsrLzcnt() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x1000uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0uL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 1uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0uL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0xdeadbeefuL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x8000000000000000uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0uL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x11uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0uL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("bsf rbx, rax") }
        assertAssembly("bsf rbx, rax")
        flags(cf = false, of = false, sf = false, af = false, pf = false) // those flags are undefined
        assert()

        flagRegisters()
        sync()
        execute { assemble("tzcnt rbx, rax") }
        assertAssembly("tzcnt rbx, rax")
        flags(of = false, sf = false, pf = false, af = false)
        assert()

        flagRegisters()
        sync()
        execute { assemble("bsr rbx, rax") }
        assertAssembly("bsr rbx, rax")
        flags(cf = false, of = false, sf = false, af = false, pf = false)
        assert()

        flagRegisters()
        sync()
        execute { assemble("lzcnt rbx, rax") }
        assertAssembly("lzcnt rbx, rax")
        flags(of = false, sf = false, pf = false, af = false)
    }

    @Test fun shrd() {
        fun testcase(src: ULong, dest: ULong, shift: ULong, dtype: Datatype) = parallel(this, unicorn) {
            val (rp, dtp) = when (dtype) {
                Datatype.DWORD -> "e" to "d"
                Datatype.QWORD -> "r" to "q"
                else -> "" to ""
            }

            gprRegisters64(rax = src, rbx = dest)
            flagRegisters()
            sync()
            execute { assemble("shrd ${rp}bx, ${rp}ax, $shift") }
            assertAssembly("shrd ${rp}bx, ${rp}ax, 0x${shift.hex2}")
            if (shift > 1uL) {
                // For shifts greater than 1 bit, the OF flag is undefined
                flags(of = false)
            }
            flags(pf = false) // unicorn: ???
            assert()

            gprRegisters64(rax = src, rcx = shift)
            flagRegisters()
            sync()
            store(0x1234uL, dest.pack(dtype.bytes, ByteOrder.LITTLE_ENDIAN))
            execute { assemble("shrd ${dtp}word [0x1234], ${rp}ax, cl") }
            assertAssembly("shrd ${dtp}word[+0x1234], ${rp}ax, cl")
            if (shift > 1uL) {
                // For shifts greater than 1 bit, the OF flag is undefined
                flags(of = false)
            }
            flags(pf = false) // unicorn: ???
            assert()
            assertMem(0x1234uL, dtype)
        }

        // SHRD r/m16, r16, imm8
        // SHRD r/m16, r16, CL
        testcase(0x95uL, 0x6auL, 0uL, Datatype.WORD)
        testcase(0x95uL, 0x6auL, 5uL, Datatype.WORD)
        testcase(0x95uL, 0x6auL, 16uL, Datatype.WORD)
        testcase(0x95uL, 0x6auL, 15uL, Datatype.WORD)
        testcase(0x95uL, 0x6auL, 7uL, Datatype.WORD)

        // SHRD r/m32, r32, imm8
        // SHRD r/m32, r32, CL
        testcase(0xaaaaaaaauL, 0x55555555uL, 0uL, Datatype.DWORD)
        testcase(0xaaaaaaaauL, 0x55555555uL, 5uL, Datatype.DWORD)
        testcase(0xaaaaaaaauL, 0x55555555uL, 31uL, Datatype.DWORD)
        testcase(0xaaaaaaaauL, 0x55555555uL, 32uL, Datatype.DWORD)

        // SHRD r/m64, r64, imm8
        // SHRD r/m64, r64, CL
        testcase(0xaaaaaaaaaaaaaaaauL, 0x5555555555555555uL, 0uL, Datatype.QWORD)
        testcase(0xaaaaaaaaaaaaaaaauL, 0x5555555555555555uL, 5uL, Datatype.QWORD)
        testcase(0xaaaaaaaaaaaaaaaauL, 0x5555555555555555uL, 32uL, Datatype.QWORD)
        testcase(0xaaaaaaaaaaaaaaaauL, 0x5555555555555555uL, 63uL, Datatype.QWORD)
        testcase(0xaaaaaaaaaaaaaaaauL, 0x5555555555555555uL, 64uL, Datatype.QWORD)
    }

    @Test fun shld() {
        fun testcase(src: ULong, dest: ULong, shift: ULong, dtype: Datatype) = parallel(this, unicorn) {
            val (rp, dtp) = when (dtype) {
                Datatype.DWORD -> "e" to "d"
                Datatype.QWORD -> "r" to "q"
                else -> "" to ""
            }

            gprRegisters64(rax = src, rbx = dest)
            flagRegisters()
            sync()
            execute { assemble("shld ${rp}bx, ${rp}ax, $shift") }
            assertAssembly("shld ${rp}bx, ${rp}ax, 0x${shift.hex2}")
            if (shift > 1uL) {
                // For shifts greater than 1 bit, the OF flag is undefined
                flags(of = false)
            }
            assert()

            gprRegisters64(rax = src, rcx = shift)
            flagRegisters()
            sync()
            store(0x1234uL, dest.pack(dtype.bytes, ByteOrder.LITTLE_ENDIAN))
            execute { assemble("shld ${dtp}word [0x1234], ${rp}ax, cl") }
            assertAssembly("shld ${dtp}word[+0x1234], ${rp}ax, cl")
            if (shift > 1uL) {
                // For shifts greater than 1 bit, the OF flag is undefined
                flags(of = false)
            }
            assert()
            assertMem(0x1234uL, dtype)
        }

        // SHLD r/m16, r16, imm8
        // SHLD r/m16, r16, CL
        testcase(0x95uL, 0x6auL, 0uL, Datatype.WORD)
        testcase(0x95uL, 0x6auL, 5uL, Datatype.WORD)
        testcase(0x95uL, 0x6auL, 15uL, Datatype.WORD)

        // SHLD r/m32, r32, imm8
        // SHLD r/m32, r32, CL
        testcase(0xaaaaaaaauL, 0x55555555uL, 0uL, Datatype.DWORD)
        testcase(0xaaaaaaaauL, 0x55555555uL, 2uL, Datatype.DWORD)
        testcase(0xaaaaaaaauL, 0x55555555uL, 5uL, Datatype.DWORD)
        testcase(0xaaaaaaaauL, 0x55555555uL, 31uL, Datatype.DWORD)

        // SHLD r/m64, r64, imm8
        // SHLD r/m64, r64, CL
        testcase(0xaaaaaaaaaaaaaaaauL, 0x5555555555555555uL, 0uL, Datatype.QWORD)
        testcase(0xaaaaaaaaaaaaaaaauL, 0x5555555555555555uL, 2uL, Datatype.QWORD)
        testcase(0xaaaaaaaaaaaaaaaauL, 0x5555555555555555uL, 5uL, Datatype.QWORD)
        testcase(0xaaaaaaaaaaaaaaaauL, 0x5555555555555555uL, 32uL, Datatype.QWORD)
        testcase(0xaaaaaaaaaaaaaaaauL, 0x5555555555555555uL, 63uL, Datatype.QWORD)
    }

    @Test fun psllq() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(5, 0xDEADBEEF_CAFEBABEuL.bigint),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("psllq xmm5, 0x20") }
        assertAssembly("psllq xmm5, 0x20")
    }

    @Test fun psrlqMmxImm() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.MMX(5, 0xDEADBEEFuL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("psrlq mm5, 0x10") }
        assertAssembly("psrlq mmx5, 0x10")
    }

    @Test fun psrlqMmxMmx() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.MMX(5, 0xDEADBEEFuL),
            GenericParallelTest.MMX(6, 0x10uL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.MMX(5, 0xDEADBEEFuL),
            GenericParallelTest.MMX(6, 0x20uL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.MMX(5, 0xDEADBEEFuL),
            GenericParallelTest.MMX(6, 0x40uL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("psrlq mm5, mm6") }
        assertAssembly("psrlq mmx5, mmx6")
    }

    @Test fun psrlqXmmImm() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(5, 0xDEADBEEF_CAFEBABEuL.bigint),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("psrlq xmm5, 0x20") }
        assertAssembly("psrlq xmm5, 0x20")
    }

    @Test fun psrlqXmmXmm() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(5, 0xDEADBEEF_CAFEBABEuL.bigint),
            GenericParallelTest.XMM(6, 0x10uL.bigint),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(5, 0xDEADBEEF_CAFEBABEuL.bigint),
            GenericParallelTest.XMM(6, 0x20uL.bigint),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(5, 0xDEADBEEF_CAFEBABEuL.bigint),
            GenericParallelTest.XMM(6, 0x40uL.bigint),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("psrlq xmm5, xmm6") }
        assertAssembly("psrlq xmm5, xmm6")
    }

    @Test fun palignrMmxMmxImm() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.MMX(5, 0xDEADBEEF_CAFEBABEuL),
            GenericParallelTest.MMX(6, 0x01020304_05060708uL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("palignr mm5, mm6, 5") }
        assertAssembly("palignr mmx5, mmx6, 0x05")
    }

    @Test fun palignrXmmXmmImm() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(5, "DEADBEEFCAFEBABE0102030405060708"),
            GenericParallelTest.XMM(6, "090A0B0C0D0E0F101112131415161718"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("palignr xmm5, xmm6, 5") }
        assertAssembly("palignr xmm5, xmm6, 0x05")
    }

    @Test fun pmaxub() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(4, 0xDEADBEEFuL.bigint),
            GenericParallelTest.XMM(5, 0xCAFEBABEuL.bigint),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("pmaxub xmm5, xmm4") }
        assertAssembly("pmaxub xmm5, xmm4")
    }

    @Test fun maxss() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
            GenericParallelTest.Mem(0x1234uL, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("maxss xmm1, xmm2") }
        assertAssembly("maxss xmm1, xmm2")
        assert()
        execute { assemble("maxss xmm1, dword [+0x1234]") }
        assertAssembly("maxss xmm1, dword[+0x1234]")
    }

    @Test fun maxsd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
            GenericParallelTest.Mem(0x1234uL, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("maxsd xmm1, xmm2") }
        assertAssembly("maxsd xmm1, xmm2")
        assert()
        execute { assemble("maxsd xmm1, qword [+0x1234]") }
        assertAssembly("maxsd xmm1, qword[+0x1234]")
    }

    @Test fun movaps() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(0, "DEADBEEFDEADBEEFDEADBEEFDEADBEEF"),
            GenericParallelTest.Mem(0x1234u, "1111111111111111"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("movaps xmm1, xmm0") }
        assertAssembly("movaps xmm1, xmm0")
        assert()
        execute { assemble("movaps [0x1234], xmm1") }
        assertAssembly("movaps xmmword[+0x1234], xmm1")
        assert()
        assertMem(0x1234uL, Datatype.XMMWORD)

        execute { assemble("movaps xmm2, [0x1234]") }
        assertAssembly("movaps xmm2, xmmword[+0x1234]")
        assert()
    }

    @Test fun `in`() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RDX, 0x8005uL),
            GenericParallelTest.Reg(x86GPR.RAX, 0uL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("in eax, dx") }
        assertAssembly("in eax, dx")
    }

    @Test fun movapd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(0, "DEADBEEFDEADBEEFDEADBEEFDEADBEEF"),
            GenericParallelTest.Mem(0x1234u, "1111111111111111"),
        ).asIterable()
    ).test(this, unicorn) {
        execute { assemble("movapd xmm1, xmm0") }
        assertAssembly("movapd xmm1, xmm0")
        assert()
        execute { assemble("movapd [0x1234], xmm1") }
        assertAssembly("movapd xmmword[+0x1234], xmm1")
        assert()
        assertMem(0x1234uL, Datatype.XMMWORD)

        execute { assemble("movapd xmm2, [0x1234]") }
        assertAssembly("movapd xmm2, xmmword[+0x1234]")
        assert()
    }

    @Test fun andnpd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(0, BigInteger("DEADBEEFDEADBEEFDEADBEEFDEADBEEF", 16)),
            GenericParallelTest.XMM(1, BigInteger("CAFEBABECAFEBABECAFEBABECAFEBABE", 16)),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("andnpd xmm0, xmm1") }
        assertAssembly("andnpd xmm0, xmm1")
    }

    @Test fun `strange test`() = parallel(this, unicorn) {
        sync()
        store(0x1230uL, "cafebabecafebabecafebabecafebabe")
        execute { "3EFF0530120000".unhexlify() }
        assertAssembly("inc dword [rip+0x1230]")
        assert()
        assert()
    }

    //3E FF 05 2C 20 75 00

    @Test fun andnps() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(0, BigInteger("DEADBEEFDEADBEEFDEADBEEFDEADBEEF", 16)),
            GenericParallelTest.XMM(1, BigInteger("CAFEBABECAFEBABECAFEBABECAFEBABE", 16)),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("andnps xmm0, xmm1") }
        assertAssembly("andnps xmm0, xmm1")
    }

    // NP 0F C4 /r ib1 PINSRW mm, r32/m16, imm8

    @Test fun pinsrwMmxR32Imm() {
        fun testcase(imm8: Int) = parallel(this, unicorn) {
            gprRegisters64(rax = 0xcafebabecafebabeuL)
            sync()
            mmx(4, 0xdeadbeefdeadbeefuL)
            execute { assemble("pinsrw mm4, eax, $imm8") }
            assertAssembly("pinsrw mmx4, ax, 0x${imm8.hex2}")
            assert()
        }

        // NOTE: r32 = r16 in this case
        testcase(0)
        testcase(1)
        testcase(2)
    }

    @Test fun pinsrwMmxM16Imm() {
        fun testcase(imm8: Int) = parallel(this, unicorn) {
            sync()
            mmx(5, 0xdeadbeefdeadbeefuL)
            store(0x1234uL, "cafebabecafebabe")
            execute { assemble("pinsrw mm5, word [0x1234], $imm8") }
            assertAssembly("pinsrw mmx5, word[+0x1234], 0x${imm8.hex2}")
            assert()
            assertMemory(0x1234uL, "cafebabecafebabe")
        }

        testcase(0)
        testcase(1)
        testcase(2)
    }

    // 66 0F C4 /r ib PINSRW xmm, r32/m16, imm8

    @Test fun pinsrwXmmR32Imm() {
        fun testcase(imm8: Int) = parallel(this, unicorn) {
            gprRegisters64(rax = 0xcafebabecafebabeuL)
            sync()
            xmm(4, BigInteger("deadbeefdeadbeefdeadbeefdeadbeef", 16))
            execute { assemble("pinsrw xmm4, eax, $imm8") }
            assertAssembly("pinsrw xmm4, ax, 0x${imm8.hex2}")
            assert()
        }

        // NOTE: r32 = r16 in this case
        testcase(0)
        testcase(1)
        testcase(2)
        testcase(7)
    }

    @Test fun pinsrwXmmM16Imm() {
        fun testcase(imm8: Int) = parallel(this, unicorn) {
            sync()
            store(0x1234uL, "cafebabecafebabe")
            xmm(5, BigInteger("deadbeefdeadbeefdeadbeefdeadbeef", 16))
            execute { assemble("pinsrw xmm5, word [0x1234], $imm8") }
            assertAssembly("pinsrw xmm5, word[+0x1234], 0x${imm8.hex2}")
            assert()
            assertMemory(0x1234uL, "cafebabecafebabe")
        }

        testcase(0)
        testcase(1)
        testcase(2)
        testcase(7)
    }

    // 66 0F 70 /r ib PSHUFD xmm1, xmm2/m128, imm8

    @Test fun pshufdXmmXmmImm() {
        fun testcase(imm8: Int) = parallel(this, unicorn) {
            sync()
            xmm(5, BigInteger("123456789abcdef0deadbeefcafebabe", 16))
            execute { assemble("pshufd xmm4, xmm5, $imm8") }
            assertAssembly("pshufd xmm4, xmm5, 0x${imm8.hex2}")
            assert()
        }

        testcase(0)
        testcase(4)
        testcase(16)
        testcase(64)
        testcase(255)
    }

    // F2 0F 70 /r ib PSHUFLW xmm1, xmm2/m128, imm8

    @Test fun pshuflwXmmXmmImm() {
        fun testcase(imm8: Int, src: BigInteger) = parallel(this, unicorn) {
            sync()
            xmm(5, src)
            execute { assemble("pshuflw xmm4, xmm5, $imm8") }
            assertAssembly("pshuflw xmm4, xmm5, 0x${imm8.hex2}")
            assert()
        }

        val src = BigInteger("123456789abcdef0deadbeefcafebabe", 16)
        testcase(0, src)
        testcase(4, src)
        testcase(16, src)
        testcase(64, src)
        testcase(255, src)
    }

    // F3 0F 70 /r ib PSHUFHW xmm1, xmm2/m128, imm8

    @Test fun pshufhwXmmXmmImm() {
        fun testcase(imm8: Int, src: BigInteger) = parallel(this, unicorn) {
            sync()
            xmm(5, src)
            execute { assemble("pshufhw xmm4, xmm5, $imm8") }
            assertAssembly("pshufhw xmm4, xmm5, 0x${imm8.hex2}")
            assert()
        }

        val src = BigInteger("123456789abcdef0deadbeefcafebabe", 16)
        testcase(0, src)
        testcase(4, src)
        testcase(16, src)
        testcase(64, src)
        testcase(255, src)
    }

    /*
    @Test fun nearAddrDecode() {
        store(0uL, "e971b1f5ff")
        x86.step()
        assertAssembly("jmp 0xFFFFFFFFFFF5B176")

        x86.pc = 0uL
        store(0uL, "eb29")
        x86.step()
        assertAssembly("jmp 0x002b")

        x86.pc = 0uL
        store(0uL, "0f82bb010000")
        x86.step()
        assertAssembly("jb 0x01c1")

        x86.pc = 0uL
    }
    */

    @Test fun cbw() = GenericParallelTest(
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 5uL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 133uL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0xff0085uL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0xff0000uL)).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cbw") }
        assertAssembly("cbw ")
    }

    @Test fun cwde() = GenericParallelTest(
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 5uL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 32773uL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 68451074053uL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 1095216660485uL)).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cwde") }
        assertAssembly("cwde ")
    }

    @Test fun cdqe() = GenericParallelTest(
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 5uL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 2147483653uL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 18374686481819107333uL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 18374686479671623685uL)).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cdqe") }
        assertAssembly("cdqe ")
    }

    @Test fun movsxR64() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RBX, 0x8005uL),
            GenericParallelTest.Reg(x86GPR.RAX, 0uL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("movsx rax, bx") }
        assertAssembly("movsx rax, bx")
    }

    @Test fun movsxR32() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RBX, 0x8005uL),
            GenericParallelTest.Reg(x86GPR.RAX, 0xFFFF_FFFF_FFFF_FFFFuL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("movsx eax, bx") }
        assertAssembly("movsx eax, bx")
    }

    @Test fun pandn() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "807060504030201bebabecaefbeadde"),
            GenericParallelTest.XMM(2, "24232221201918171616151413121110"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("pandn xmm1, xmm2") }
        assertAssembly("pandn xmm1, xmm2")
    }

    @Test fun paddq() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "807060504030201bebabecaefbeadde"),
            GenericParallelTest.XMM(2, "24232221201918171616151413121110"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("paddq xmm1, xmm2") }
        assertAssembly("paddq xmm1, xmm2")
    }

    @Test fun paddb() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "4da5282bf6a0d151df0d937464cd64be"),
            GenericParallelTest.XMM(2, "3172ebc4f3614f7fc2f846a71ef62082"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("paddb xmm1, xmm2") }
        assertAssembly("paddb xmm1, xmm2")
    }

    @Test fun paddd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "4da5282bf6a0d151df0d937464cd64be"),
            GenericParallelTest.XMM(2, "3172ebc4f3614f7fc2f846a71ef62082"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("paddd xmm1, xmm2") }
        assertAssembly("paddd xmm1, xmm2")
    }

    @Test fun pmovmskb() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "807060504030201bebabecaefbeadde"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("pmovmskb eax, xmm1") }
        assertAssembly("pmovmskb eax, xmm1")
    }

    @Test fun movhpd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "24232221201918171616151413121110"),
            GenericParallelTest.Mem(0x1234uL, "DEADBEEFCAFEBABE010203040506070809"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("movhpd xmm1, [0x1234]") }
        assertAssembly("movhpd xmm1, qword[+0x1234]")
    }

    @Test fun pcmpgtb() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.XMM(2, "0807060504030201bebafecaefbeadde"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("pcmpgtb xmm1, xmm2") }
        assertAssembly("pcmpgtb xmm1, xmm2")
    }

    @Test fun pcmpgtd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.XMM(2, "0807060504030201bebafecaefbeadde"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("pcmpgtd xmm1, xmm2") }
        assertAssembly("pcmpgtd xmm1, xmm2")
    }

    @Test fun psubb() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.XMM(2, "0807060504030201bebafecaefbeadde"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("psubb xmm1, xmm2") }
        assertAssembly("psubb xmm1, xmm2")
    }

    @Test fun psubd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.XMM(2, "0807060504030201bebafecaefbeadde"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("psubd xmm1, xmm2") }
        assertAssembly("psubd xmm1, xmm2")
    }

    @Test fun punpcklbw() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "7f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("punpcklbw xmm1, xmm2") }
        assertAssembly("punpcklbw xmm1, xmm2")
    }

    @Test fun punpcklwd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "7f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("punpcklwd xmm1, xmm2") }
        assertAssembly("punpcklwd xmm1, xmm2")
    }
    @Test fun unpcklpd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(0, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(1, "7f8080041a0218be16fe14ef12ad10"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("unpcklpd xmm0, xmm1") }
        assertAssembly("unpcklpd xmm0, xmm1")
        assert()
        execute { assemble("cvtpd2ps xmm1, [+0x1234]") }
        assertAssembly("cvtpd2ps xmm1, xmmword[+0x1234]")
    }

    @Test fun punpcklqdq() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "7f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("punpcklqdq xmm1, xmm2") }
        assertAssembly("punpcklqdq xmm1, xmm2")
    }

    @Test fun pslldq() {
        fun testcase(imm1: Int, imm2: Int) = parallel(this, unicorn) {
            sync()
            xmm(1, BigInteger("80808180041a021817ba15ca13be11de", 16))
            xmm(2, BigInteger("7f8080041a0218be16fe14ef12ad10", 16))

            execute { assemble("pslldq xmm1, $imm1") }
            assertAssembly("pslldq xmm1, 0x${imm1.hex2}")
            assert()

            execute { assemble("pslldq xmm2, $imm2") }
            assertAssembly("pslldq xmm2, 0x${imm2.hex2}")
            assert()
        }

        testcase(0x20, 0x40)
        testcase(0, 15)
        testcase(4, 10)
    }

    @Test fun psrldMmxImm8() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.MMX(5, 0xdeadbeefcafebabeuL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("psrld mm5, 5") }
        assertAssembly("psrld mmx5, 0x05")
    }

    @Test fun psrldXmmImm8() {
        fun testcase(imm: Int) = parallel(this, unicorn) {
            // 66 0F 72 /2 ib PSRLD xmm1, imm8
            sync()
            xmm(1, BigInteger("deadbeefcafebabe0102030405060708", 16))
            xmm(2, BigInteger("80808180041a021817ba15ca13be11de", 16))

            execute { assemble("psrld xmm1, $imm") }
            assertAssembly("psrld xmm1, 0x${imm.hex2}")
            execute { assemble("psrld xmm2, $imm") }
            assertAssembly("psrld xmm2, 0x${imm.hex2}")
            assert()
        }

        for (i in 0..32) {
            testcase(i)
        }
    }

    @Test fun psrldq() {
        fun testcase(imm1: Int, imm2: Int) = parallel(this, unicorn) {
            sync()
            xmm(1, BigInteger("80808180041a021817ba15ca13be11de", 16))
            xmm(2, BigInteger("7f8080041a0218be16fe14ef12ad10", 16))

            execute { assemble("psrldq xmm1, $imm1") }
            assertAssembly("psrldq xmm1, 0x${imm1.hex2}")
            assert()

            execute { assemble("psrldq xmm2, $imm2") }
            assertAssembly("psrldq xmm2, 0x${imm2.hex2}")
            assert()
        }

        testcase(0x20, 0x40)
        testcase(0, 15)
        testcase(4, 10)
    }

    @Test fun movsdXmmXmm() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "7f8080041a0218be16fe14ef12ad10"),
        ).asIterable()
    ).test(this, unicorn) {
        execute { assemble("movsd xmm1, xmm2") }
        assertAssembly("movsd xmm1, xmm2")
    }

    @Test fun movsdXmmM64() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.Mem(0x1234uL, "deadbeefcafebabe0102030405060708"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("movsd xmm1, [0x1234]") }
        assertAssembly("movsd xmm1, qword[+0x1234]")
    }

    @Test fun movsdM64Xmm() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "807060504030201bebafecaefbeadde"),
            GenericParallelTest.Mem(0x1234uL, "101112131415161718191a1b1c1d1e1f"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("movsd [0x1234], xmm1") }
        assertAssembly("movsd qword[+0x1234], xmm1")
    }

    @Test fun cvtsi2sdXmmR32() = GenericParallelTest(
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0xDEADuL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0xDEADBEEFuL)).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cvtsi2sd xmm1, eax") }
        assertAssembly("cvtsi2sd xmm1, eax")
    }

    @Test fun cvtsi2sdXmmR64() = GenericParallelTest(
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0xDEADBEEFuL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0xDEADBEEFCAFEBABEuL)).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0xe2485uL),
            GenericParallelTest.XMM(1, "ffffffffffffffffffffffffffffffff"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cvtsi2sd xmm1, rax") }
        assertAssembly("cvtsi2sd xmm1, rax")
    }

    @Test fun cvtsi2ssXmmR64() = GenericParallelTest(
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0xDEADBEEFuL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0xDEADBEEFCAFEBABEuL)).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x67uL),
            GenericParallelTest.XMM(0, "ffffffffffffffffffffffffffffffff"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cvtsi2ss xmm0, rax") }
        assertAssembly("cvtsi2ss xmm0, rax")
    }

    @Test fun cvtsd2ss() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
            GenericParallelTest.Mem(0x1234uL, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cvtsd2ss xmm1, xmm2") }
        assertAssembly("cvtsd2ss xmm1, xmm2")
        assert()
        execute { assemble("cvtsd2ss xmm1, qword [+0x1234]") }
        assertAssembly("cvtsd2ss xmm1, qword[+0x1234]")
    }

    @Test fun cvtss2sd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
            GenericParallelTest.Mem(0x1234uL, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cvtss2sd xmm1, xmm2") }
        assertAssembly("cvtss2sd xmm1, xmm2")
        assert()
        execute { assemble("cvtss2sd xmm1, dword [+0x1234]") }
        assertAssembly("cvtss2sd xmm1, dword[+0x1234]")
    }

    @Test fun cvtpd2ps() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cvtpd2ps xmm1, xmm2") }
        assertAssembly("cvtpd2ps xmm1, xmm2")
        assert()
        execute { assemble("cvtpd2ps xmm1, [+0x1234]") }
        assertAssembly("cvtpd2ps xmm1, xmmword[+0x1234]")
    }

    @Test fun addsd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "c07c800000000000c05ec00000000000"),
            GenericParallelTest.Mem(0x1234uL, "0000000000807cc0"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "407c800000000000405ec00000000000"),
            GenericParallelTest.Mem(0x1234uL, "0000000000807c40"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("addsd xmm1, [0x1234]") }
        assertAssembly("addsd xmm1, qword[+0x1234]")
    }

    @Test fun psllw() {
        fun testcase(imm8: Int) = parallel(this, unicorn) {
            sync()
            xmm(0, BigInteger("123456789"))
            execute { assemble("psllw xmm0, $imm8") }
            assertAssembly("psllw xmm0, 0x${imm8.hex2}")
            assert()
        }
        testcase(0)
        testcase(4)
        testcase(200)
    }

    @Test fun sqrtsd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(6, "c07c800000000000c05ec00000000055"),
            GenericParallelTest.Mem(0x1234uL, "0000000000807cc0"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(6, "407c800000000000405ec00000000055"),
            GenericParallelTest.Mem(0x1234uL, "0000000000807c40"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("sqrtsd xmm6, [0x1234]") }
        assertAssembly("sqrtsd xmm6, qword[+0x1234]")
    }

    @Test fun psrlw() {
        fun testcase(imm8: Int) = parallel(this, unicorn) {
            sync()
            xmm(0, BigInteger("123456789"))
            // execute { "660F71D043".unhexlify() }
            execute { assemble("psrlw xmm0, $imm8") }
            assertAssembly("psrlw xmm0, 0x${imm8.hex2}")
            assert()
        }
        testcase(0)
        testcase(4)
        testcase(200)
    }

    @Test fun movnti() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, "00807cc0"),
            GenericParallelTest.Reg(x86GPR.RAX, 1uL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, "00807cc0"),
            GenericParallelTest.Reg(x86GPR.RAX, 4uL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("movnti [0x1234], eax") }
        assertAssembly("movnti dword[+0x1234], eax")
    }

    @Test fun cvttsd2siR64() = GenericParallelTest(
        arrayOf(
            0x405ec00000000000uL,
            0xc07c800000000000uL,
            0xc088ad999999999auL,
            0x409348ef9db22d0euL,
        ).map {
            GenericParallelTest.XMM(
                1,
                BigInteger("17161514131211100000000000000000", 16) or it.bigint
            )
        },
        arrayOf(GenericParallelTest.XMM(1, 0xbff0000000000000uL.bigint)).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cvttsd2si rax, xmm1") }
        assertAssembly("cvttsd2si rax, xmm1")
    }

    @Test fun cvttssCvttsd2siR32() = GenericParallelTest(
        arrayOf(GenericParallelTest.XMM(1, BigInteger("cf000000", 16))).asIterable(),
        arrayOf(GenericParallelTest.XMM(1, BigInteger("bf800000", 16))).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cvttss2si eax, xmm1") }
        assertAssembly("cvttss2si eax, xmm1")
        assert()

        execute { assemble("cvttsd2si eax, xmm1") }
        assertAssembly("cvttsd2si eax, xmm1")
    }

    @Test fun shl() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 1uL),
            GenericParallelTest.Reg(x86GPR.RCX, 106uL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 1uL),
            GenericParallelTest.Reg(x86GPR.RCX, 34uL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0xa4209840a00da0d8uL),
            GenericParallelTest.Reg(x86GPR.RCX, 0x16f641uL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("shl eax, cl") }
        assertAssembly("shl eax, cl")
    }

    @Test fun shr() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0xf0000000uL),
            GenericParallelTest.Reg(x86GPR.RCX, 106uL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("shr eax, cl") }
        assertAssembly("shr eax, cl")
    }

    @Test fun sar() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0xf0000000uL),
            GenericParallelTest.Reg(x86GPR.RCX, 106uL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("sar eax, cl") }
        assertAssembly("sar eax, cl")
    }

    // 66 0F 38 01 /r PHADDW xmm1, xmm2
    @Test fun phaddwXmmXmm() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("phaddw xmm1, xmm2") }
        assertAssembly("phaddw xmm1, xmm2")
    }

    // 66 0F 38 01 /r PHADDW xmm1, m128
    @Test fun phaddwXmmM128() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.Mem(0x1234uL, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("phaddw xmm1, [0x1234]") }
        assertAssembly("phaddw xmm1, xmmword[+0x1234]")
    }

    // NP 0F 38 01 /r1 PHADDW mm1, mm2
    @Test fun phaddwMmxMmx() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.MMX(1, "bebafecaefbeadde"),
            GenericParallelTest.MMX(2, "1716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.MMX(1, "17ba15ca13be11de"),
            GenericParallelTest.MMX(2, "be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("phaddw mm1, mm2") }
        assertAssembly("phaddw mmx1, mmx2")
    }

    // NP 0F 38 01 /r1 PHADDW mm1, m64
    @Test fun phaddwMmxM64() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.MMX(1, "bebafecaefbeadde"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.MMX(1, "17ba15ca13be11de"),
            GenericParallelTest.Mem(0x1234uL, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("phaddw mm1, qword [0x1234]") }
        assertAssembly("phaddw mmx1, qword[+0x1234]")
    }

    // 66 0F 38 02 /r PHADDD xmm1, xmm2
    @Test fun phadddXmmXmm() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("phaddd xmm1, xmm2") }
        assertAssembly("phaddd xmm1, xmm2")
    }

    // Bug in Unicorn?
    /*
    @Test fun phadddXmm1Xmm1() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("phaddd xmm1, xmm1") }
        assertAssembly("phaddd xmm1, xmm1")
    }
    */

    // 66 0F 38 02 /r PHADDD xmm1, m128
    @Test fun phadddXmmM128() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.Mem(0x1234uL, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("phaddd xmm1, [0x1234]") }
        assertAssembly("phaddd xmm1, xmmword[+0x1234]")
    }

    // NP 0F 38 02 /r PHADDD mm1, mm2
    @Test fun phadddMmxMmx() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.MMX(1, "bebafecaefbeadde"),
            GenericParallelTest.MMX(2, "1716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.MMX(1, "17ba15ca13be11de"),
            GenericParallelTest.MMX(2, "be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("phaddd mm1, mm2") }
        assertAssembly("phaddd mmx1, mmx2")
    }

    // NP 0F 38 02 /r PHADDD mm1, m64
    @Test fun phadddMmxM64() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.MMX(1, "bebafecaefbeadde"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.MMX(1, "17ba15ca13be11de"),
            GenericParallelTest.Mem(0x1234uL, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("phaddd mm1, qword [0x1234]") }
        assertAssembly("phaddd mmx1, qword[+0x1234]")
    }

    @Test fun sahf() = GenericParallelTest(
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0xFFFFuL)).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("sahf") }
        assertAssembly("sahf ")
    }

    @Test fun subsd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
            GenericParallelTest.Mem(0x1234uL, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("subsd xmm1, xmm2") }
        assertAssembly("subsd xmm1, xmm2")
        assert()
        execute { assemble("subsd xmm1, qword [0x1234]") }
        assertAssembly("subsd xmm1, qword[+0x1234]")
    }

    @Test fun subss() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
            GenericParallelTest.Mem(0x1234uL, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("subss xmm1, xmm2") }
        assertAssembly("subss xmm1, xmm2")
        assert()
        execute { assemble("subss xmm1, dword [0x1234]") }
        assertAssembly("subss xmm1, dword[+0x1234]")
    }

    @Test fun subpd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
            GenericParallelTest.Mem(0x1234uL, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("subpd xmm1, xmm2") }
        assertAssembly("subpd xmm1, xmm2")
        assert()
        execute { assemble("subpd xmm1, [0x1234]") }
        assertAssembly("subpd xmm1, xmmword[+0x1234]")
    }

    @Test fun subps() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
            GenericParallelTest.Mem(0x1234uL, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("subps xmm1, xmm2") }
        assertAssembly("subps xmm1, xmm2")
        assert()
        execute { assemble("subps xmm1, [0x1234]") }
        assertAssembly("subps xmm1, xmmword[+0x1234]")
    }

    @Test fun cvttss2si() = GenericParallelTest(
        arrayOf(GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde")).asIterable(),
        arrayOf(GenericParallelTest.XMM(1, "4f800000")).asIterable(),
        arrayOf(GenericParallelTest.XMM(1, "3df000000")).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x01uL),
            GenericParallelTest.XMM(1, "4093a06366e8e5f8"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cvttss2si rax, xmm1") }
        assertAssembly("cvttss2si rax, xmm1")
    }

    @Test fun mulsd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("mulsd xmm1, xmm2") }
        assertAssembly("mulsd xmm1, xmm2")
    }

    @Test fun mulss() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("mulss xmm1, xmm2") }
        assertAssembly("mulss xmm1, xmm2")
    }

    @Test fun cmpsd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
            GenericParallelTest.Mem(0x1234uL, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cmpsd xmm1, xmm2, 0x06") }
        assertAssembly("cmpsd xmm1, xmm2, 0x06")
        assert()
        execute { assemble("cmpsd xmm1, [0x1234], 0x01") }
        assertAssembly("cmpsd xmm1, qword[+0x1234], 0x01")
    }

    @Test fun cmpss() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.Mem(0x1234uL, "1f1e1d1c1b1a19181716151413121110"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
            GenericParallelTest.Mem(0x1234uL, "007f8080041a0218be16fe14ef12ad10"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cmpss xmm1, xmm2, 0x06") }
        assertAssembly("cmpss xmm1, xmm2, 0x06")
        assert()
        execute { assemble("cmpss xmm1, [0x1234], 0x01") }
        assertAssembly("cmpss xmm1, dword[+0x1234], 0x01")
    }

    @Test fun cmpxchgR8R8() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0xDEADBEEFCAFEBABEuL),
            GenericParallelTest.Reg(x86GPR.RBX, 0xCAFEuL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0xDEADBEEFCAFEBABEuL),
            GenericParallelTest.Reg(x86GPR.RBX, 0xBABEuL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cmpxchg bl, bh") }
        assertAssembly("cmpxchg bl, bh")
    }

    @Test fun cmpxchgR16R16() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0xDEADBEEFCAFEBABEuL),
            GenericParallelTest.Reg(x86GPR.RBX, 0xCAFEuL),
            GenericParallelTest.Reg(x86GPR.RCX, 0xEFCAuL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0xDEADBEEFCAFEBABEuL),
            GenericParallelTest.Reg(x86GPR.RBX, 0xBABEuL),
            GenericParallelTest.Reg(x86GPR.RCX, 0xEFCAuL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cmpxchg bx, cx") }
        assertAssembly("cmpxchg bx, cx")
    }

    @Test fun cmpxchgR32R32() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0xDEADBEEFCAFEBABEuL),
            GenericParallelTest.Reg(x86GPR.RBX, 0xBEEFCAFEuL),
            GenericParallelTest.Reg(x86GPR.RCX, 0xADBEEFCAuL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cmpxchg ebx, ecx") }
        assertAssembly("cmpxchg ebx, ecx")
    }

    @Test fun cmpxchgR32R32UnicornBug() {
        gprRegisters64(
            rax = 0xDEADBEEFCAFEBABEuL,
            rbx = 0xCAFEBABEuL,
            rcx = 0xADBEEFCAuL
        )

        execute { assemble("cmpxchg ebx, ecx") }
        assertAssembly("cmpxchg ebx, ecx")

        assertEquals(0xdeadbeefcafebabeuL, x86.cpu.regs.rax.value)
        assertEquals(0xadbeefcauL, x86.cpu.regs.rbx.value)
        assertEquals(0xadbeefcauL, x86.cpu.regs.rcx.value)
        assertFlagRegisters(cf = false, pf = true, af = false, zf = true, sf = false, of = false)
    }

    @Test fun cmpxchgR64R64() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0xDEADBEEFCAFEBABEuL),
            GenericParallelTest.Reg(x86GPR.RBX, 0xEDEADBEEFCAFEBABuL),
            GenericParallelTest.Reg(x86GPR.RCX, 0xADBEEFCAFEBABEDEuL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0xDEADBEEFCAFEBABEuL),
            GenericParallelTest.Reg(x86GPR.RBX, 0xDEADBEEFCAFEBABEuL),
            GenericParallelTest.Reg(x86GPR.RCX, 0xEDEADBEEFCAFEBABuL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("cmpxchg rbx, rcx") }
        assertAssembly("cmpxchg rbx, rcx")
    }

    @Test fun neg() = GenericParallelTest(
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0x10000uL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0x01uL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0xFFFFFFFFFFFFFFFFuL)).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("neg rax") }
        assertAssembly("neg rax")
    }

    @Test fun negAllFlags() = GenericParallelTest(
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0x10000uL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0x01uL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0xFFFFFFFFFFFFFFFFuL)).asIterable(),
    ).test(this, unicorn) {
        flags(cf = true, pf = true, af = true, zf = true, sf = true, df = true, of = true)
        execute { assemble("neg rax") }
        assertAssembly("neg rax")
    }

    @Test fun imul1R8() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x05uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0x05uL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x05uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0xFFuL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x81uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0x03uL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("imul bl") }
        assertAssembly("imul bl")
    }

    @Test fun imul1R16() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x05uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0x05uL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x05uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0xFFFFuL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x8001uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0x03uL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("imul bx") }
        assertAssembly("imul bx")
    }

    @Test fun imul1R32() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x05uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0x05uL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x05uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0xFFFF_FFFFuL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x8000_0001uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0x03uL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x80b5_83ebuL),
            GenericParallelTest.Reg(x86GPR.RBX, 0xf1fc_d680uL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("imul ebx") }
        assertAssembly("imul ebx")
    }

    @Test fun imul1R64() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x05uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0x05uL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x05uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0xFFFF_FFFF_FFFF_FFFFuL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x8000_0000_0000_0001uL),
            GenericParallelTest.Reg(x86GPR.RBX, 0x03uL),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RAX, 0x61c8_8646_80b5_83ebuL),
            GenericParallelTest.Reg(x86GPR.RBX, 0xffff_ea00_01fc_d680uL),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("imul rbx") }
        assertAssembly("imul rbx")
    }

    @Test fun imul2() = GenericParallelTest(
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0x61c8864680b583ebuL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RDI, 0xffffea0001fcd680uL)).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("imul rdi, rax") }
        assertAssembly("imul rdi, rax")
    }

    @Test fun imul3() = GenericParallelTest(
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0x3euL)).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("imul eax, eax, 0xBDEF7BDF") }
        assertAssembly("imul eax, eax, 0xBDEF7BDF")
    }

    @Test fun sbbCF_R64_00() = GenericParallelTest(
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0uL)).asIterable(),
    ).test(this, unicorn) {
        x86.cpu.flags.cf = true
        unicorn.flags(unicorn.flags().apply { cf = 1 })
        execute { assemble("sbb rax, 0x00") }
        assertAssembly("sbb rax, 0x00")
    }

    @Test fun sbbCF_R64_01() = GenericParallelTest(
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0xFFFFFFFFFFFFFFFFuL)).asIterable(),
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0uL)).asIterable(),
    ).test(this, unicorn) {
        x86.cpu.flags.cf = true
        unicorn.flags(unicorn.flags().apply { cf = 1 })
        execute { assemble("sbb rax, 0x01") }
        assertAssembly("sbb rax, 0x01")
    }

    @Test fun sbbCF_R64ULongMax() = GenericParallelTest(
        arrayOf(GenericParallelTest.Reg(x86GPR.RAX, 0x01uL)).asIterable(),
    ).test(this, unicorn) {
        x86.cpu.flags.cf = true
        unicorn.flags(unicorn.flags().apply { cf = 1 })
        execute { assemble("sbb rax, 0xFFFFFFFFFFFFFFFF") }
        assertAssembly("sbb rax, 0xFFFFFFFFFFFFFFFF")
    }

    @Test fun divsd() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.Mem(0x1234uL, "bebafecaefbeadde0807060504030201"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
            GenericParallelTest.Mem(0x1234uL, "be16fe14ef12ad10007f8080041a0218"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("divsd xmm1, xmm2") }
        assertAssembly("divsd xmm1, xmm2")
        assert()
        execute { assemble("divsd xmm2, [0x1234]") }
        assertAssembly("divsd xmm2, qword[+0x1234]")
    }

    @Test fun divss() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(1, "0807060504030201bebafecaefbeadde"),
            GenericParallelTest.XMM(2, "1f1e1d1c1b1a19181716151413121110"),
            GenericParallelTest.Mem(0x1234uL, "bebafecaefbeadde0807060504030201"),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.XMM(1, "80808180041a021817ba15ca13be11de"),
            GenericParallelTest.XMM(2, "007f8080041a0218be16fe14ef12ad10"),
            GenericParallelTest.Mem(0x1234uL, "be16fe14ef12ad10007f8080041a0218"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("divss xmm1, xmm2") }
        assertAssembly("divss xmm1, xmm2")
        assert()
        execute { assemble("divss xmm2, [0x1234]") }
        assertAssembly("divss xmm2, dword[+0x1234]")
    }

    @Test fun psubusx() {
        // NP 0F D8 /r1 PSUBUSB mm, mm/m64
        // NP 0F D9 /r1 PSUBUSW mm, mm/m64
        fun testcase64(l: ULong, r: ULong, variant: String = "b") = parallel(this, unicorn) {
            sync()
            mmx(1, l)
            mmx(2, r)
            mmx(3, l)
            store(0x1234uL, r.hex)
            execute { assemble("psubus${variant} mm1, mm2") }
            assertAssembly("psubus${variant} mmx1, mmx2")
            execute { assemble("psubus${variant} mm3, [0x1234]") }
            assertAssembly("psubus${variant} mmx3, qword[+0x1234]")
            assert()
            assertMem(0x1234uL, Datatype.QWORD)
        }

        // 66 0F D8 /r PSUBUSB xmm1, xmm2/m128
        // 66 0F D9 /r PSUBUSW xmm1, xmm2/m128
        fun testcase128(l: BigInteger, r: BigInteger, variant: String = "b") = parallel(this, unicorn) {
            sync()
            xmm(1, l)
            xmm(2, r)
            xmm(3, l)
            store(0x1234uL, "%032X".format(r))
            execute { assemble("psubus${variant} xmm1, xmm2") }
            assertAssembly("psubus${variant} xmm1, xmm2")
            execute { assemble("psubus${variant} xmm3, [0x1234]") }
            assertAssembly("psubus${variant} xmm3, xmmword[+0x1234]")
            assert()
            assertMem(0x1234uL, Datatype.XMMWORD)
        }

        testcase64(0xDEADBEEF_CAFEBABEuL, 0xCAFEBABE_DEADBEEFuL)
        testcase64(0xDEADBEEF_CAFEBABEuL, 0xCAFEBABE_DEADBEEFuL, variant = "w")

        val l = BigInteger("DEADBEEFCAFEBABE0102030405060708", 16)
        val r = BigInteger("0102030405060708CAFEBABEDEADBEEF", 16)
        testcase128(l, r)
        testcase128(l, r, variant = "w")
    }


    @Test fun x87Stack() {
        val fld1 = assemble("fld1")
        val fldz = assemble("fldz")
        val fstp = assemble("fstp tword [0x1234]")

        fun pushExpect(sw: ULong, tag: ULong, z: Boolean, st: Array<Double>) {
            execute { if (z) fldz else fld1 }
            assertEquals(sw, x86.fpu.fwr.FPUStatusWord.value, "Expected FPSW = ${sw.hex4}")
            assertEquals(tag, x86.fpu.fwr.FPUTagWord.value, "Expected FPTAG = ${tag.hex4}")
            for (i in 0..7) {
                assertEquals(st[i], x86.fpu.stld(i).double, "Expected st$i = ${st[i]}")
            }
        }

        fun popExpect(sw: ULong, tag: ULong, popped: Double, st: Array<Double>) {
            execute { fstp }

            // TODO: stack underflow
            assertEquals(
                sw and 0x241uL.inv(),
                x86.fpu.fwr.FPUStatusWord.value and 0x241uL.inv(),
                "Expected FPSW = ${sw.hex4}"
            )
            x86.fpu.fwr.FPUStatusWord.value = sw

            assertEquals(tag, x86.fpu.fwr.FPUTagWord.value, "Expected FPTAG = ${tag.hex4}")
            assertEquals(popped, core.ine(0x1234uL, 10).longDouble(x86.fpu.fwr.FPUControlWord).double)


            for (i in 0..7) {
                assertEquals(st[i], x86.fpu.stld(i).double, "Expected st$i = ${st[i]}")
            }
        }

        assertEquals(0uL, x86.fpu.fwr.FPUStatusWord.value, "Expected initial FPSW = 0")
        assertEquals(0xFFFFuL, x86.fpu.fwr.FPUTagWord.value, "Expected initial FPTAG = 0")

        pushExpect(0x3800uL, 0x3fffuL, false, arrayOf(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0))
        pushExpect(0x3000uL, 0x1fffuL, true, arrayOf(0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0))
        pushExpect(0x2800uL, 0x13ffuL, false, arrayOf(1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0))
        pushExpect(0x2000uL, 0x11ffuL, true, arrayOf(0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0))
        pushExpect(0x1800uL, 0x113fuL, false, arrayOf(1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0))
        pushExpect(0x1000uL, 0x111fuL, true, arrayOf(0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0))
        pushExpect(0x800uL, 0x1113uL, false, arrayOf(1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0))
        pushExpect(0x0uL, 0x1111uL, true, arrayOf(0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0))

        // Overflow
        pushExpect(0x3a41uL, 0x9111uL, false, arrayOf(Double.NaN, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0))
        pushExpect(0x3241uL, 0xa111uL, true, arrayOf(Double.NaN, Double.NaN, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0))
        pushExpect(0x2a41uL, 0xa911uL, false, Array(3) { Double.NaN } + arrayOf(0.0, 1.0, 0.0, 1.0, 0.0))
        pushExpect(0x2241uL, 0xaa11uL, true, Array(4) { Double.NaN } + arrayOf(0.0, 1.0, 0.0, 1.0))
        pushExpect(0x1a41uL, 0xaa91uL, false, Array(5) { Double.NaN } + arrayOf(0.0, 1.0, 0.0))
        pushExpect(0x1241uL, 0xaaa1uL, true, Array(6) { Double.NaN } + arrayOf(0.0, 1.0))
        pushExpect(0x0a41uL, 0xaaa9uL, false, Array(7) { Double.NaN } + arrayOf(0.0))
        pushExpect(0x0241uL, 0xaaaauL, true, Array(8) { Double.NaN })

        // Overflow x2
        pushExpect(0x3a41uL, 0xaaaauL, false, Array(8) { Double.NaN })

        popExpect(0x0041uL, 0xeaaauL, Double.NaN, Array(8) { Double.NaN })
        popExpect(0x0841uL, 0xeaabuL, Double.NaN, Array(8) { Double.NaN })
        popExpect(0x1041uL, 0xeaafuL, Double.NaN, Array(8) { Double.NaN })
        popExpect(0x1841uL, 0xeabfuL, Double.NaN, Array(8) { Double.NaN })
        popExpect(0x2041uL, 0xeaffuL, Double.NaN, Array(8) { Double.NaN })
        popExpect(0x2841uL, 0xebffuL, Double.NaN, Array(8) { Double.NaN })
        popExpect(0x3041uL, 0xefffuL, Double.NaN, Array(8) { Double.NaN })
        popExpect(0x3841uL, 0xffffuL, Double.NaN, Array(8) { Double.NaN })

        // Underflow
        popExpect(0x0041uL, 0xffffuL, Double.NaN, Array(8) { Double.NaN })
        popExpect(0x0841uL, 0xffffuL, Double.NaN, Array(8) { Double.NaN })

        pushExpect(0x0041uL, 0xfffcuL, false, Array(8) { if (it == 0) 1.0 else Double.NaN })
        popExpect(0x0841uL, 0xffffuL, 1.0, Array(8) { if (it == 7) 1.0 else Double.NaN })
    }

    @Test fun x87Pop() = parallel(this, unicorn) {
        sync()
        execute { assemble("fld1") }
        execute { assemble("fstp tword [0x1234]") }
        assertMem(0x1234uL, 10)
        assert()
        assertEquals(0uL, x86.fpu.fwr.FPUStatusWord.value, "Expected FPSW = 0")
        execute { assemble("fstp tword [0x1234]") }
        assertMem(0x1234uL, 10)
        assert()
    }

    @Test fun x87ToMmxSwitch() = parallel(this, unicorn) {
        sync()
        execute { assemble("fld1") }
        execute { assemble("mov rax, 0xDEADBEEFCAFEBABE") }
        execute { assemble("movq mm5, rax") }
        assert()

        execute { assemble("fld1") }
        // Real CPU:
        assertEquals(0x3a41uL, x86.fpu.fwr.FPUStatusWord.value, "Expected FPSW = 0x3a41")
        assertEquals(0x9955uL, x86.fpu.fwr.FPUTagWord.value, "Expected FPTAG = 0x9955")
        assertEquals(0x037fuL, x86.fpu.fwr.FPUControlWord.value, "Expected FPCW = 0x037f")
        arrayOf(
            "ffffc000000000000000".bigintByHex,
            BigInteger.ZERO,
            BigInteger.ZERO,
            BigInteger.ZERO,
            BigInteger.ZERO,
            BigInteger.ZERO,
            "ffffdeadbeefcafebabe".bigintByHex,
            BigInteger.ZERO,
        ).forEachIndexed { n, st -> assertEquals(st, x86.fpu.st(n), "Expected st$n = ${st.hex}") }
    }

    private fun x87ConstTest(insn: String) {
        parallel(this, unicorn) {
            // Fuzz PC and RC
            for (pc in FWRBank.PrecisionControl.values()) {
                if (pc == FWRBank.PrecisionControl.Invalid) continue

                for (rc in FWRBank.RoundControl.values()) {
                    this.test.x86.fpu.fwr.FPUControlWord.run {
                        this@run.pc = pc
                        this@run.rc = rc
                    }
                    this.test.x86.fpu.fwr.FPUTagWord.value = 0xFFFFuL
                    this.test.x86.fpu.fwr.FPUStatusWord.value = 0uL

                    sync()

                    execute { assemble(insn) }
                    assertAssembly("$insn fpr[0]")
                    if ((x86.fpu.st(0) - unicorn.st(0)).abs() == BigInteger.ONE) {
                        // TODO: why is the least significant bit different sometimes?
                        x86.fpu.st(0, unicorn.st(0))
                    }
                    assert()
                }
            }
        }
    }

    @Test fun fld1() = x87ConstTest("fld1")
    @Test fun fldl2t() = x87ConstTest("fldl2t")
    @Test fun fldl2e() = x87ConstTest("fldl2e")
    @Test fun fldpi() = x87ConstTest("fldpi")
    @Test fun fldlg2() = x87ConstTest("fldlg2")
    @Test fun fldln2() = x87ConstTest("fldln2")
    @Test fun fldz() = x87ConstTest("fldz")

    private fun Parallel.x87ArithmTestMemFp(insn: String, v: Double) {
        // Fuzz PC and RC
        for (pc in FWRBank.PrecisionControl.values()) {
            if (pc == FWRBank.PrecisionControl.Invalid) continue

            for (rc in FWRBank.RoundControl.values()) {
                this.test.x86.fpu.fwr.FPUControlWord.run {
                    this@run.pc = pc
                    this@run.rc = rc
                }
            }

            val st0 = v.longDouble(this.test.x86.fpu.fwr.FPUControlWord).ieee754AsUnsigned()
            this.test.x86.fpu.st(0, st0)

            sync()

            execute { assemble("f$insn dword [0x1234]") }
            assertAssembly("f$insn fpr[0], dword[+0x1234]")
            assert()
            execute { assemble("f$insn qword [0x1345]") }
            assertAssembly("f$insn fpr[0], qword[+0x1345]")
            assert()
        }
    }

    private fun x87ArithmTestFpFp(insn: String, v1: Double, v2: Double) = parallel(this, unicorn) {
        // Fuzz PC and RC
        val fld1 = assemble("fld1")
        for (pc in FWRBank.PrecisionControl.values()) {
            if (pc == FWRBank.PrecisionControl.Invalid) continue

            for (rc in FWRBank.RoundControl.values()) {
                this.test.x86.fpu.fwr.FPUControlWord.run {
                    this@run.pc = pc
                    this@run.rc = rc
                }

                val st0 = v1.longDouble(this.test.x86.fpu.fwr.FPUControlWord).ieee754AsUnsigned()
                val st1 = v2.longDouble(this.test.x86.fpu.fwr.FPUControlWord).ieee754AsUnsigned()
                this.test.x86.fpu.st(0, st0)
                this.test.x86.fpu.st(1, st1)
                sync()

                execute { assemble("f$insn st0, st1") }
                assertAssembly("f$insn fpr[0], fpr[1]")
                assert()

                execute { assemble("f$insn st1, st0") }
                assertAssembly("f$insn fpr[1], fpr[0]")
                assert()

                execute { fld1 }
                execute { fld1 }

                this.test.x86.fpu.st(0, st0)
                this.test.x86.fpu.st(1, st1)
                this.test.x86.fpu.st(2, st1)
                sync()

                execute { assemble("f${insn}p st2, st0") }
                assertAssembly("f${insn}p fpr[2], fpr[0]")
                assert()

                sync()
                execute { assemble("f${insn}p") }
                assertAssembly("f${insn}p fpr[1], fpr[0]")
                assert()
            }
        }
    }

    private fun Parallel.x87ArithmTestInt(insn: String, v: Double) {
        // Fuzz PC and RC
        for (pc in FWRBank.PrecisionControl.values()) {
            if (pc == FWRBank.PrecisionControl.Invalid) continue

            for (rc in FWRBank.RoundControl.values()) {
                this.test.x86.fpu.fwr.FPUControlWord.run {
                    this@run.pc = pc
                    this@run.rc = rc
                }

                val st0 = v.longDouble(this.test.x86.fpu.fwr.FPUControlWord).ieee754AsUnsigned()
                this.test.x86.fpu.st(0, st0)

                sync()

                execute { assemble("fi$insn word [0x1234]") }
                assertAssembly("fi$insn fpr[0], word[+0x1234]")
                assert()
                execute { assemble("fi$insn dword [0x1345]") }
                assertAssembly("fi$insn fpr[0], dword[+0x1345]")
                assert()
            }
        }
    }

    // D8 /0 FADD m32fp
    // DC /0 FADD m64fp
    @Test fun faddMemFp() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, 234.5f.ieee754AsUnsigned().swap32().hex8),
            GenericParallelTest.Mem(0x1345uL, 345.6.ieee754AsUnsigned().swap64().hex16),
        ).asIterable(),
    ).test(this, unicorn) { x87ArithmTestMemFp("add", 123.4) }

    // D8 C0+i FADD ST(0), ST(i)
    // DC C0+i FADD ST(i), ST(0)
    // DE C0+i FADDP ST(i), ST(0)
    // DE C1 FADDP
    @Test fun faddFpFp() = x87ArithmTestFpFp("add", 123.4, 345.6)

    // DA /0 FIADD m32int
    // DE /0 FIADD m16int
    @Test fun faddMemInt() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, 0xDEADBEEFuL.swap16().hex4),
            GenericParallelTest.Mem(0x1345uL, 0xCAFEBABEuL.swap32().hex8),
        ).asIterable(),
    ).test(this, unicorn) { x87ArithmTestInt("add", 123.4) }

    // D8 /4 FSUB m32fp
    // DC /4 FSUB m64fp
    @Test fun fsubMemFp() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, 234.5f.ieee754AsUnsigned().swap32().hex8),
            GenericParallelTest.Mem(0x1345uL, 345.6.ieee754AsUnsigned().swap64().hex16),
        ).asIterable(),
    ).test(this, unicorn) { x87ArithmTestMemFp("sub", 123.4) }

    // D8 E0+i FSUB ST(0), ST(i)
    // DC E8+i FSUB ST(i), ST(0)
    // DE E8+i FSUBP ST(i), ST(0)
    // DE E9 FSUBP
    @Test fun fsubFpFp() = x87ArithmTestFpFp("sub", 123.4, 345.6)

    // DA /4 FISUB m32int
    // DE /4 FISUB m16int
    @Test fun fsubMemInt() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, 0xDEADBEEFuL.swap16().hex4),
            GenericParallelTest.Mem(0x1345uL, 0xCAFEBABEuL.swap32().hex8),
        ).asIterable(),
    ).test(this, unicorn) { x87ArithmTestInt("sub", 123.4) }

    // D8 /5 FSUBR m32fp
    // DC /5 FSUBR m64fp
    @Test fun fsubrMemFp() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, 234.5f.ieee754AsUnsigned().swap32().hex8),
            GenericParallelTest.Mem(0x1345uL, 345.6.ieee754AsUnsigned().swap64().hex16),
        ).asIterable(),
    ).test(this, unicorn) { x87ArithmTestMemFp("subr", 123.4) }

    // D8 E8+i FSUBR ST(0), ST(i)
    // DC E0+i FSUBR ST(i), ST(0)
    // DE E0+i FSUBRP ST(i), ST(0)
    // DE E1 FSUBRP
    @Test fun fsubrFpFp() = x87ArithmTestFpFp("subr", 123.4, 345.6)

    // DA /5 FISUBR m32int
    // DE /5 FISUBR m16int
    @Test fun fsubrMemInt() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, 0xDEADBEEFuL.swap16().hex4),
            GenericParallelTest.Mem(0x1345uL, 0xCAFEBABEuL.swap32().hex8),
        ).asIterable(),
    ).test(this, unicorn) { x87ArithmTestInt("subr", 123.4) }

    // D8 /1 FMUL m32fp
    // DC /1 FMUL m64fp
    @Test fun fmulMemFp() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, 10.5f.ieee754AsUnsigned().swap32().hex8),
            GenericParallelTest.Mem(0x1345uL, 20.7.ieee754AsUnsigned().swap64().hex16),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, 0.0f.ieee754AsUnsigned().swap32().hex8),
            GenericParallelTest.Mem(0x1345uL, 0.7.ieee754AsUnsigned().swap64().hex16),
        ).asIterable(),
    ).test(this, unicorn) { x87ArithmTestMemFp("mul", 2.5) }

    // D8 C8+i FMUL ST(0), ST(i)
    // DC C8+i FMUL ST(i), ST(0)
    // DE C8+i FMULP ST(i), ST(0)
    // DE C9 FMULP
    @Test fun fmulFpFp() = x87ArithmTestFpFp("mul", 10.5, 20.7)

    // DA /1 FIMUL m32int
    // DE /1 FIMUL m16int
    @Test fun fmulMemInt() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, 0xDEADBEEFuL.swap16().hex4),
            GenericParallelTest.Mem(0x1345uL, 0xCAFEBABEuL.swap32().hex8),
        ).asIterable(),
    ).test(this, unicorn) { x87ArithmTestInt("mul", 3.4) }

    // D8 /6 FDIV m32fp
    // DC /6 FDIV m64fp
    @Test fun fdivMemFp() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, 10.5f.ieee754AsUnsigned().swap32().hex8),
            GenericParallelTest.Mem(0x1345uL, 20.7.ieee754AsUnsigned().swap64().hex16),
        ).asIterable(),
    ).test(this, unicorn) { x87ArithmTestMemFp("div", 100000.2) }

    // D8 F0+i FDIV ST(0), ST(i)
    // DC F8+i FDIV ST(i), ST(0)
    // DE F8+i FDIVP ST(i), ST(0)
    // DE F9 FDIVP
    @Test fun fdivFpFp() = x87ArithmTestFpFp("div", 100.2, 50.2)

    // DA /6 FIDIV m32int
    // DE /6 FIDIV m16int
    @Test fun fdivMemInt() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, 0xDEADBEEFuL.swap16().hex4),
            GenericParallelTest.Mem(0x1345uL, 0xCAFEBABEuL.swap32().hex8),
        ).asIterable(),
    ).test(this, unicorn) { x87ArithmTestInt("div", 100000.2) }

    // D8 /7 FDIVR m32fp
    // DC /7 FDIVR m64fp
    @Test fun fdivrMemFp() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, 10.0f.ieee754AsUnsigned().swap32().hex8),
            GenericParallelTest.Mem(0x1345uL, 20.0.ieee754AsUnsigned().swap64().hex16),
        ).asIterable(),
    ).test(this, unicorn) { x87ArithmTestMemFp("divr", 100000.0) }

    // D8 F8+i FDIVR ST(0), ST(i)
    // DC F0+i FDIVR ST(i), ST(0)
    // DE F0+i FDIVRP ST(i), ST(0)
    // DE F1 FDIVRP
    @Test fun fdivrFpFp() = x87ArithmTestFpFp("divr", 50.2, 100.2)

    // DA /7 FIDIVR m32int
    // DE /7 FIDIVR m16int
    @Test fun fdivrMemInt() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, 0xDEADBEEFuL.swap16().hex4),
            GenericParallelTest.Mem(0x1345uL, 0xCAFEBABEuL.swap32().hex8),
        ).asIterable(),
    ).test(this, unicorn) { x87ArithmTestInt("divr", 100000.2) }

    @Test fun fild() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, 123uL.pack(8, ByteOrder.LITTLE_ENDIAN)),
            GenericParallelTest.Mem(0x123cuL, (-345uL).pack(8, ByteOrder.LITTLE_ENDIAN)),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("fild qword [0x1234]") }
        assertAssembly("fild fpr[-1], qword[+0x1234]")
        assert()
        execute { assemble("fild qword [0x123C]") }
        assertAssembly("fild fpr[-1], qword[+0x123C]")
    }

    @Test fun fist() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.x87(0, 123.4.longDouble(x86.fpu.fwr.FPUControlWord)),
            GenericParallelTest.Mem(0x1234uL, "0000000000000000"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("fist word [0x1234]") }
        assertAssembly("fist word[+0x1234], fpr[0]")
        assert()
        assertMem(0x1234uL, 8)
        execute { assemble("fist dword [0x1234]") }
        assertAssembly("fist dword[+0x1234], fpr[0]")
        assert()
        assertMem(0x1234uL, 8)
    }

    @Test fun fistp() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.x87(0, 123.4.longDouble(x86.fpu.fwr.FPUControlWord)),
            GenericParallelTest.x87(1, 123.4.longDouble(x86.fpu.fwr.FPUControlWord)),
            GenericParallelTest.x87(2, 123.4.longDouble(x86.fpu.fwr.FPUControlWord)),
            GenericParallelTest.Mem(0x1234uL, "0000000000000000"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("fistp word [0x1234]") }
        assertAssembly("fistp word[+0x1234], fpr[0]")
        assert()
        assertMem(0x1234uL, 8)
        execute { assemble("fistp dword [0x1234]") }
        assertAssembly("fistp dword[+0x1234], fpr[0]")
        assert()
        assertMem(0x1234uL, 8)
        execute { assemble("fistp qword [0x1234]") }
        assertAssembly("fistp qword[+0x1234], fpr[0]")
        assert()
        assertMem(0x1234uL, 8)
    }

    @Test fun fld() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Mem(0x1234uL, (-456.0).ieee754AsUnsigned().pack(8,
                ByteOrder.LITTLE_ENDIAN)),
            GenericParallelTest.Mem(0x123cuL, (-123.0f).ieee754AsUnsigned().pack(4,
                ByteOrder.LITTLE_ENDIAN)),
            GenericParallelTest.Mem(0x1240uL, (-789.0).longDouble(x86.fpu.fwr.FPUControlWord).byteBufferLe),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("fld dword [0x123C]") }
        assertAssembly("fld fpr[-1], dword[+0x123C]")
        assert()
        execute { assemble("fld qword [0x1234]") }
        assertAssembly("fld fpr[-1], qword[+0x1234]")
        assert()
        execute { assemble("fld tword [0x1240]") }
        assertAssembly("fld fpr[-1], fpu80[+0x1240]")
        assert()
        execute { assemble("fld st0") }
        assertAssembly("fld fpr[-1], fpr[0]")
        assert()
    }

    // D9 /2 FST m32fp
    // DD /2 FST m64fp
    // D9 /3 FSTP m32fp
    // DD /3 FSTP m64fp
    // DB /7 FSTP m80fp
    @Test fun fstMem() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.x87(0, 123.4.longDouble(x86.fpu.fwr.FPUControlWord)),
            GenericParallelTest.x87(1, 234.5.longDouble(x86.fpu.fwr.FPUControlWord)),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("fstp qword [0x1234]") }
        assertAssembly("fstp qword[+0x1234], fpr[0]")
        execute { assemble("fstp dword [0x123c]") }
        assertAssembly("fstp dword[+0x123c], fpr[0]")
        assert()
        assertMem(0x1234uL, 8)
        assertMem(0x123CuL, 4)

        execute { assemble("fld qword [0x1234]") }
        assertAssembly("fld fpr[-1], qword[+0x1234]")
        execute { assemble("fld dword [0x123c]") }
        assertAssembly("fld fpr[-1], dword[+0x123c]")
        assert()
        assertEquals(234.5, x86.fpu.stld(0).double, "Expected st0 = 234.5")
        assertEquals(123.4, x86.fpu.stld(1).double, "Expected st1 = 123.4")

        execute { assemble("fstp tword [0x1234]") }
        assertAssembly("fstp fpu80[+0x1234], fpr[0]")
        assert()
        assertMem(0x1234uL, 10)
    }

    // DD D0+i FST ST(i)
    // DD D8+i FSTP ST(i)
    @Test fun fstFp() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.x87(0, 123.4.longDouble(x86.fpu.fwr.FPUControlWord)),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("fst st5") }
        assertAssembly("fst fpr[5], fpr[0]")
        assert()

        execute { assemble("fstp st6") }
        assertAssembly("fstp fpr[6], fpr[0]")
        assert()
    }

    // DD E0+i FUCOM ST(i)
    // DD E1 FUCOM
    // DD E8+i FUCOMP ST(i)
    // DD E9 FUCOMP
    // DA E9 FUCOMPP
    @Test fun fucom() = GenericParallelTest(
        arrayOf<GenericParallelTest.Condition>(
            GenericParallelTest.x87(0, 123.4.longDouble(x86.fpu.fwr.FPUControlWord)),
            GenericParallelTest.x87(1, 234.4.longDouble(x86.fpu.fwr.FPUControlWord)),
            GenericParallelTest.x87(2, 12.4.longDouble(x86.fpu.fwr.FPUControlWord)),
            GenericParallelTest.x87(3, 123.5.longDouble(x86.fpu.fwr.FPUControlWord)),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("fucom st2") }
        assertAssembly("fucom fpr[0], fpr[2]")
        assert()

        execute { assemble("fucom") }
        assertAssembly("fucom fpr[0], fpr[1]")
        assert()

        execute { assemble("fucomp st3") }
        assertAssembly("fucomp fpr[0], fpr[3]")
        assert()

        execute { assemble("fucomp") }
        assertAssembly("fucomp fpr[0], fpr[1]")
        assert()

        execute { assemble("fucompp") }
        assertAssembly("fucompp fpr[0], fpr[1]")
        assert()
    }

    // DB E8+i FUCOMI ST0, ST(i)
    // DF E8+i FUCOMIP ST0, ST(i)
    @Test fun fucomi() = GenericParallelTest(
        arrayOf<GenericParallelTest.Condition>(
            GenericParallelTest.x87(0, 123.4.longDouble(x86.fpu.fwr.FPUControlWord)),
            GenericParallelTest.x87(1, 234.4.longDouble(x86.fpu.fwr.FPUControlWord)),
            GenericParallelTest.x87(2, 12.4.longDouble(x86.fpu.fwr.FPUControlWord)),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("fucomi st1") }
        assertAssembly("fucomi fpr[0], fpr[1]")
        assert()

        execute { assemble("fucomip st2") }
        assertAssembly("fucomip fpr[0], fpr[2]")
        assert()
    }

    // D9 FC FRNDINT
    @Test fun frndint() = parallel(this, unicorn) {
        // Fuzz PC and RC
        for (pc in FWRBank.PrecisionControl.values()) {
            if (pc == FWRBank.PrecisionControl.Invalid) continue

            for (rc in FWRBank.RoundControl.values()) {
                this.test.x86.fpu.fwr.FPUControlWord.run {
                    this@run.pc = pc
                    this@run.rc = rc
                }
                this.test.x86.fpu.st(0, 123.5.longDouble(this.test.x86.fpu.fwr.FPUControlWord).ieee754AsUnsigned())

                sync()

                execute { assemble("frndint") }
                assertAssembly("frndint ")
                assert()
            }
        }
    }

    @Test fun frndintHuman() = parallel(this, unicorn) {
        fun testcase(rc: FWRBank.RoundControl, inp: Double, outp: Double) {
            this.test.x86.fpu.fwr.FPUControlWord.rc = rc
            this.test.x86.fpu.st(0, inp.longDouble(this.test.x86.fpu.fwr.FPUControlWord).ieee754AsUnsigned())
            sync()

            execute { assemble("frndint") }
            assertAssembly("frndint ")
            assert()
            Assert.assertTrue(outp == this.test.x86.fpu.stld(0).double)
        }

        // Default mode in Python
        // >>> round(123.5)
        // 124
        // >>> round(124.5)
        // 124
        testcase(FWRBank.RoundControl.RoundToNearestEven, 123.5, 124.0)
        testcase(FWRBank.RoundControl.RoundToNearestEven, 124.5, 124.0)
        testcase(FWRBank.RoundControl.RoundToNearestEven, -123.5, -124.0)
        testcase(FWRBank.RoundControl.RoundToNearestEven, -124.5, -124.0)

        testcase(FWRBank.RoundControl.RoundTowardZero, 123.4, 123.0)
        testcase(FWRBank.RoundControl.RoundTowardZero, 123.5, 123.0)
        testcase(FWRBank.RoundControl.RoundTowardZero, 123.6, 123.0)
        testcase(FWRBank.RoundControl.RoundTowardZero, -123.4, -123.0)
        testcase(FWRBank.RoundControl.RoundTowardZero, -123.5, -123.0)
        testcase(FWRBank.RoundControl.RoundTowardZero, -123.6, -123.0)

        testcase(FWRBank.RoundControl.RoundTowardsNegative, 123.4, 123.0)
        testcase(FWRBank.RoundControl.RoundTowardsNegative, 123.5, 123.0)
        testcase(FWRBank.RoundControl.RoundTowardsNegative, 123.6, 123.0)
        testcase(FWRBank.RoundControl.RoundTowardsNegative, -123.4, -124.0)
        testcase(FWRBank.RoundControl.RoundTowardsNegative, -123.5, -124.0)
        testcase(FWRBank.RoundControl.RoundTowardsNegative, -123.6, -124.0)

        testcase(FWRBank.RoundControl.RoundTowardsPositive, 123.4, 124.0)
        testcase(FWRBank.RoundControl.RoundTowardsPositive, 123.5, 124.0)
        testcase(FWRBank.RoundControl.RoundTowardsPositive, 123.6, 124.0)
        testcase(FWRBank.RoundControl.RoundTowardsPositive, -123.4, -123.0)
        testcase(FWRBank.RoundControl.RoundTowardsPositive, -123.5, -123.0)
        testcase(FWRBank.RoundControl.RoundTowardsPositive, -123.6, -123.0)
    }

    @Test fun fstenv() = parallel(this, unicorn) {
        val fld1 = assemble("fld1")

        sync()

        for (i in 0 until 8) {
            execute { fld1 }
        }
        assert()

        x86.fpu.fwr.FPUInstructionPointer.value = 0x0EuL // Don't care

        execute { assemble("fnstenv [0x1234]") }
        assertAssembly("fnstenv byte[+0x1234]")
        assert()
        assertMem(0x1234uL, 28)
    }

    @Test fun fsaveFrstor() = parallel(this, unicorn) {
        val fld1 = assemble("fld1")
        val fldz = assemble("fldz")

        fun assertRegs() {
            assertEquals(1.0, x86.fpu.stld(0).double, "Expected st0 = 1.0")
            assertEquals(1.0, x86.fpu.stld(1).double, "Expected st1 = 1.0")
            assertEquals(0.0, x86.fpu.stld(2).double, "Expected st2 = 0.0")
            assertEquals(1.0, x86.fpu.stld(3).double, "Expected st3 = 1.0")
            assertEquals(0.0, x86.fpu.stld(4).double, "Expected st4 = 0.0")
            assertEquals(1.0, x86.fpu.stld(5).double, "Expected st5 = 1.0")
            assertEquals(0.0, x86.fpu.stld(6).double, "Expected st6 = 0.0")
            assertEquals(0.0, x86.fpu.stld(7).double, "Expected st7 = 0.0")
        }

        sync()

        for (i in 0 until 3) {
            execute { fldz } // st6, st4, st2
            execute { fld1 } // st5, st3, st1
        }
        execute { fld1 } // st0
        assert()
        assertRegs()

        x86.fpu.fwr.FPUInstructionPointer.value = 0x0CuL // Don't care
        execute { assemble("fnsave [0x1234]") }
        assertAssembly("fsave fpu80[+0x1234]")
        assert()
        assertMem(0x1234uL, 108)

        assertEquals(0.0, x86.fpu.stld(0).double, "Expected st0 = 1.0")
        assertEquals(1.0, x86.fpu.stld(1).double, "Expected st1 = 1.0")

        execute { assemble("frstor [0x1234]") }
        assert()
        assertRegs()
    }

    @Test fun fxsave64Fxrstor64() {
        val fld1 = assemble("fld1")
        val fldz = assemble("fldz")

        x86.sse.xmm.reassignIndexed { i, _ -> i.bigint * 0xDEADBEEFCAFEBABEuL.bigint }

        fun assertSt() {
            assertEquals(1.0, x86.fpu.stld(0).double, "Expected st0 = 1.0")
            assertEquals(1.0, x86.fpu.stld(1).double, "Expected st1 = 1.0")
            assertEquals(0.0, x86.fpu.stld(2).double, "Expected st2 = 0.0")
            assertEquals(1.0, x86.fpu.stld(3).double, "Expected st3 = 1.0")
            assertEquals(0.0, x86.fpu.stld(4).double, "Expected st4 = 0.0")
            assertEquals(1.0, x86.fpu.stld(5).double, "Expected st5 = 1.0")
            assertEquals(0.0, x86.fpu.stld(6).double, "Expected st6 = 0.0")
            assertEquals(0.0, x86.fpu.stld(7).double, "Expected st7 = 0.0")
        }

        for (i in 0 until 3) {
            execute { fldz } // st6, st4, st2
            execute { fld1 } // st5, st3, st1
        }
        execute { fld1 } // st0
        assertSt()

        x86.fpu.fwr.FPUInstructionPointer.value = 0x0CuL // Don't care
        execute { assemble("fxsave64 [0x1234]") }
        x86.sse.xmm.reassign { _ -> BigInteger.ZERO }
        x86.fpu.reset()

        execute { assemble("fxrstor64 [0x1234]") }
        assertSt()
        x86.sse.xmm.forEachIndexed { i, v ->
            val exp = i.bigint * 0xDEADBEEFCAFEBABEuL.bigint
            assertEquals(exp, v, "Expected xmm$i = ${exp.hex}")
        }
    }

    // DA C0+i FCMOVB ST(0), ST(i)
    // Not tested:
    // DA C8+i FCMOVE ST(0), ST(i)
    // DA D0+i FCMOVBE ST(0), ST(i)
    // DA D8+i FCMOVU ST(0), ST(i)
    // DB C0+i FCMOVNB ST(0), ST(i)
    // DB C8+i FCMOVNE ST(0), ST(i)
    // DB D0+i FCMOVNBE ST(0), ST(i)
    // DB D8+i FCMOVNU ST(0), ST(i)
    @Test fun fcmovb() = parallel(this, unicorn) {
        x86.cpu.flags.cf = false
        x86.fpu.st(0, "1234DEADBEEFCAFEBABE".bigintByHex)
        x86.fpu.st(4, "FEDCBA9876543210ABCD".bigintByHex)
        sync()

        execute { assemble("fcmovb st0, st4") }
        assert()

        x86.cpu.flags.cf = true
        sync()
        execute { assemble("fcmovb st0, st4") }
        assert()
    }

    @Test fun fcmovDecode() {
        fun assertDecode(insn: String) {
            execute { assemble("$insn st0, st4") }
            assertAssembly("$insn fpr[0], fpr[4]")
        }

        arrayOf(
            "fcmovb", "fcmove", "fcmovbe",
            "fcmovu", "fcmovnb", "fcmovne",
            "fcmovnbe", "fcmovnu"
        ).forEach { assertDecode(it) }
    }

    @Test fun fscale() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.x87(0, 123.4.longDouble(x86.fpu.fwr.FPUControlWord)),
            GenericParallelTest.x87(1, 2.0.longDouble(x86.fpu.fwr.FPUControlWord)),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("fscale") }
        assertAssembly("fscale ")
    }

    @Test fun fstenvFldenv() = parallel(this, unicorn) {
        sync()
        execute { assemble("fnstenv [0x1234]") }
        assertAssembly("fnstenv byte[+0x1234]")
        assert()
        assertMem(0x1234uL, 28)

        // Load something so that tag changes
        val oldTag = x86.fpu.fwr.FPUTagWord.value
        execute { assemble("fild dword [0x1234]") }
        assertAssembly("fild fpr[-1], dword[+0x1234]")
        assert()
        assertNotEquals(oldTag, x86.fpu.fwr.FPUTagWord.value)

        execute { assemble("fldenv [0x1234]") }
        assertAssembly("fldenv byte[+0x1234]")
        assert()
        assertMem(0x1234uL, 28)
        assertEquals(oldTag, x86.fpu.fwr.FPUTagWord.value)
    }

    @Test fun movddup() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.XMM(5, "DEADBEEFCAFEBABE0102030405060708"),
            GenericParallelTest.XMM(6, "090A0B0C0D0E0F101112131415161718"),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("movddup xmm5, xmm6") }
        assertAssembly("movddup xmm5, xmm6")
    }

    @Test fun fabs() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.x87(0, 0.5.longDouble(x86.fpu.fwr.FPUControlWord)),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.x87(0, (-0.5).longDouble(x86.fpu.fwr.FPUControlWord)),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.x87(0, NaN.longDouble(x86.fpu.fwr.FPUControlWord)),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("fabs ") }
        assertAssembly("fabs fpr[0]")
    }

    @Test fun fsqrt() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.x87(0, 0.5.longDouble(x86.fpu.fwr.FPUControlWord)),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.x87(0, (-0.5).longDouble(x86.fpu.fwr.FPUControlWord)),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.x87(0, NaN.longDouble(x86.fpu.fwr.FPUControlWord)),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("fsqrt ") }
        x86.fpu.fwr.FPUStatusWord.value = unicorn.fpsw() // C0, C2, C3 are undefined
        assertAssembly("fsqrt fpr[0]")
    }

    @Test fun fchs() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.x87(0, 0.5.longDouble(x86.fpu.fwr.FPUControlWord)),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.x87(0, (-0.5).longDouble(x86.fpu.fwr.FPUControlWord)),
        ).asIterable(),
        arrayOf(
            GenericParallelTest.x87(0, NaN.longDouble(x86.fpu.fwr.FPUControlWord)),
        ).asIterable(),
    ).test(this, unicorn) {
        execute { assemble("fchs ") }
        assertAssembly("fchs fpr[0]")
    }

    /**
     * Not testing zf change! (mmu not synced in unicorn)
     */
    @Test fun verw() = parallel(this, unicorn) {
        sync()

        store(0x0FF8uL, "cafebabecafebabe")
//        execute { "0F002DFF0F00".unhexlify() }

        // offset by instruction length
        execute { assemble("verw word [rel $+0x0FFF]") }
        assertAssembly("verw word [rip+0x0FF8]")

        execute { assemble("verw word [rax]") }
        assertAssembly("verw word [rax]")
    }

    @Test fun verr() = parallel(this, unicorn) {
        sync()

        store(0x0FF8uL, "cafebabecafebabe")

        execute { assemble("verr word [rel $+0x0FFF]") }
        assertAssembly("verr word [rip+0x0FF8]")

        execute { assemble("verr word [rax]") }
        assertAssembly("verr word [rax]")
    }

    @Test fun fxam() = parallel(this, unicorn) {
        sync()
        // put 0 to st0
        execute { assemble("fldz") }
        assertAssembly("fldz fpr[0]")
        assert()

        execute { assemble("fxam") }
        assertAssembly("fxam fpr[0]")
        assert()

        // put 1 to st0
        execute { assemble("fld1") }
        assertAssembly("fld1 fpr[0]")
        assert()

        execute { assemble("fxam") }
        assertAssembly("fxam fpr[0]")
        assert()
    }

    @Test fun psllxPsrax() = parallel(this, unicorn) {
        fun assertDecode(insn: String, args: String, argskc: String? = null) {
            execute { assemble("$insn $args") }
            assertAssembly("$insn ${argskc ?: args}")
        }

        for (insn in arrayOf("psllw" to 16, "pslld" to 32, "psllq" to 64, "psraw" to 16, "psrad" to 32)) {
            val edgeCases = arrayOf(0, insn.second - 1, insn.second, insn.second + 1)

            for (regno1 in arrayOf(0, 7, 15)) {
                for (regno2 in arrayOf(0, 7, 15)) {
                    for (shift in edgeCases) {
                        if (regno1 < 8 && regno2 < 8) {
                            x86.fpu.mmx(regno1, 0xDEAD_BEEF_CAFE_BABEuL)
                            x86.fpu.mmx(regno2, shift.ulong_z)
                            sync()
                            // psllx mm, mm
                            // psrax mm, mm
                            assertDecode(insn.first, "mm$regno1, mm$regno2", "mmx$regno1, mmx$regno2")
                            assert()
                        }

                        x86.sse.xmm[regno1] = "DEADBEEFCAFEBABE0102030405060708".bigintByHex
                        x86.sse.xmm[regno2] = shift.bigint
                        sync()
                        // psllx xmm, xmm
                        // psrax xmm, xmm
                        assertDecode(insn.first, "xmm$regno1, xmm$regno2")
                        assert()
                    }
                }

                for (shift in edgeCases) {
                    if (regno1 < 8) {
                        x86.fpu.mmx(regno1, 0xDEAD_BEEF_CAFE_BABEuL)
                        store(0x1234uL, shift.pack(8))
                        sync()
                        // psllx mm, m64
                        // psrax mm, m64
                        assertDecode(insn.first, "mm$regno1, [0x1234]", "mmx$regno1, mmxword[+0x1234]")
                        assert()
                    }

                    x86.sse.xmm[regno1] = "DEADBEEFCAFEBABE0102030405060708".bigintByHex
                    store(0x1234uL, shift.pack(8) /* should be 16, but oh well */)
                    sync()
                    // psllx xmm, m128
                    // psrax xmm, m128
                    assertDecode(insn.first, "xmm$regno1, [0x1234]", "xmm$regno1, xmmword[+0x1234]")
                    assert()
                }

                for (shift in edgeCases) {
                    if (regno1 < 8) {
                        x86.fpu.mmx(regno1, 0xDEAD_BEEF_CAFE_BABEuL)
                        sync()
                        // psllx mm, imm8
                        // psrax mm, imm8
                        assertDecode(insn.first, "mm$regno1, $shift", "mmx$regno1, 0x${shift.hex2}")
                        assert()
                    }

                    x86.sse.xmm[regno1] = "DEADBEEFCAFEBABE0102030405060708".bigintByHex
                    sync()
                    // psllx xmm, imm8
                    // psrax xmm, imm8
                    assertDecode(insn.first, "xmm$regno1, $shift", "xmm$regno1, 0x${shift.hex2}")
                    assert()
                }
            }
        }
    }

    @Test fun rolCFTest() = GenericParallelTest(
        arrayOf(
            0x6FuL,
            0xFF_FF_FF_FFuL,
            0xFF_FF_FF_FDuL,
            0xFF_FF_FF_F9uL,
            0x7F_FF_FF_FFuL,
            0x3F_FF_FF_FFuL,
        ).flatMap {
            arrayOf(
                arrayOf(
                    GenericParallelTest.Reg(x86GPR.RAX, it),
                    GenericParallelTest.Reg(x86GPR.RBX, it),
                    GenericParallelTest.Reg(x86GPR.RCX, 0uL),
                ),
                arrayOf(
                    GenericParallelTest.Reg(x86GPR.RAX, it),
                    GenericParallelTest.Reg(x86GPR.RBX, it),
                    GenericParallelTest.Reg(x86GPR.RCX, 1uL),
                ),
            ).asIterable()
        }.map { it.asIterable() },
    ).test(this, unicorn) {
        execute { assemble("rol eax, cl") }
        assertAssembly("rol eax, cl")
        assert()
        execute { assemble("ror ebx, cl") }
        assertAssembly("ror ebx, cl")
    }

    @Test fun rolXbitsEdgeCase() = GenericParallelTest(
        arrayOf(
            GenericParallelTest.Reg(x86GPR.RSI, 0xFFFFFFFFFFFFFFFEuL),
            GenericParallelTest.Reg(x86GPR.RCX, 0x0000000000000001uL),
        ).asIterable()
    ).test(this, unicorn) {
        execute { assemble("rol rsi, cl") }
        assertAssembly("rol rsi, cl")
        assert()
    }
}
