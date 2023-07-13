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

import org.junit.Assert
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86FPU
import ru.inforion.lab403.kopycat.interfaces.ine
import unicorn.Unicorn
import java.math.BigInteger

internal class Parallel(val test: AX86InstructionTest, private val unicorn: UnicornEmu) {
    fun sync() {
        unicorn.gpr(Unicorn.UC_X86_REG_RAX, test.x86.cpu.regs.rax.value)
        unicorn.gpr(Unicorn.UC_X86_REG_RBX, test.x86.cpu.regs.rbx.value)
        unicorn.gpr(Unicorn.UC_X86_REG_RCX, test.x86.cpu.regs.rcx.value)
        unicorn.gpr(Unicorn.UC_X86_REG_RDX, test.x86.cpu.regs.rdx.value)
        unicorn.gpr(Unicorn.UC_X86_REG_RSP, test.x86.cpu.regs.rsp.value)
        unicorn.gpr(Unicorn.UC_X86_REG_RBP, test.x86.cpu.regs.rbp.value)
        unicorn.gpr(Unicorn.UC_X86_REG_RSI, test.x86.cpu.regs.rsi.value)
        unicorn.gpr(Unicorn.UC_X86_REG_RDI, test.x86.cpu.regs.rdi.value)
        unicorn.gpr(Unicorn.UC_X86_REG_RIP, test.x86.cpu.regs.rip.value)

        unicorn.gpr(Unicorn.UC_X86_REG_R8, test.x86.cpu.regs.r8.value)
        unicorn.gpr(Unicorn.UC_X86_REG_R9, test.x86.cpu.regs.r9.value)
        unicorn.gpr(Unicorn.UC_X86_REG_R10, test.x86.cpu.regs.r10.value)
        unicorn.gpr(Unicorn.UC_X86_REG_R11, test.x86.cpu.regs.r11.value)
        unicorn.gpr(Unicorn.UC_X86_REG_R12, test.x86.cpu.regs.r12.value)
        unicorn.gpr(Unicorn.UC_X86_REG_R13, test.x86.cpu.regs.r13.value)
        unicorn.gpr(Unicorn.UC_X86_REG_R14, test.x86.cpu.regs.r14.value)
        unicorn.gpr(Unicorn.UC_X86_REG_R15, test.x86.cpu.regs.r15.value)

        unicorn.gpr(Unicorn.UC_X86_REG_RFLAGS, test.x86.cpu.flags())

        test.x86.sse.xmm.forEachIndexed { i, xmmVal ->
            unicorn.xmm(i, xmmVal)
        }

        unicorn.fpcw(test.x86.fpu.fwr.FPUControlWord.value)
        unicorn.fpsw(test.x86.fpu.fwr.FPUStatusWord.value)
        unicorn.fptag(test.x86.fpu.fwr.FPUTagWord.value)
        (0 until x86FPU.FPU_STACK_SIZE).forEach { i ->
            unicorn.st(i, test.x86.fpu.st(i))
        }
    }

    fun flags(
        cf: Boolean? = null,
        pf: Boolean? = null,
        af: Boolean? = null,
        zf: Boolean? = null,
        sf: Boolean? = null,
        df: Boolean? = null,
        of: Boolean? = null
    ) {
        val flags = unicorn.flags()

        if (cf != null) {
            test.x86.cpu.flags.cf = cf
            flags.cf = cf.int
        }
        if (pf != null) {
            test.x86.cpu.flags.pf = pf
            flags.pf = pf.int
        }
        if (af != null) {
            test.x86.cpu.flags.af = af
            flags.af = af.int
        }
        if (zf != null) {
            test.x86.cpu.flags.zf = zf
            flags.zf = zf.int
        }
        if (sf != null) {
            test.x86.cpu.flags.sf = sf
            flags.sf = sf.int
        }
        if (df != null) {
            test.x86.cpu.flags.df = df
            flags.df = df.int
        }
        if (of != null) {
            test.x86.cpu.flags.of = of
            flags.of = of.int
        }

        unicorn.flags(flags)
    }

    fun store(address: ULong, data: String) = data.unhexlify().also { store(address, it) }

    fun store(address: ULong, data: ByteArray) {
        test.x86.store(address, data)
        unicorn.store(address, data)
    }

    fun execute(offset: ULong = 0u, generator: () -> ByteArray) {
        val data = generator()
        test.execute(offset) { data }
        unicorn.storeExecute(unicorn.gpr(Unicorn.UC_X86_REG_RIP), data)
    }

    fun xmm(i: Int, value: BigInteger) {
        test.x86.sse.xmm[i] = value
        unicorn.xmm(i, value)
    }

    fun mmx(i: Int, value: ULong) {
        test.x86.fpu.mmx(i, value)
        unicorn.mmx(i, value)
    }

    private fun assertRegister(name: String, u: ULong, kc: ULong) =
        Assert.assertEquals(
            "${test.x86.cpu.insn} -> $name mismatch: 0x${kc.hex} != 0x${u.hex}",
            u,
            kc,
        )

    private fun assertRegister(name: String, u: BigInteger, kc: BigInteger) =
        Assert.assertEquals(
            "${test.x86.cpu.insn} -> $name mismatch: 0x${kc.hex} != 0x${u.hex}",
            u,
            kc,
        )

    private fun assertFlag(name: String, u: Boolean, kc: Boolean) =
        Assert.assertEquals(
            "${test.x86.cpu.insn} -> flag $name mismatch: $kc != $u",
            u,
            kc,
        )

    fun assert(
        gpr: Boolean = true,
        rip: Boolean = true,
        r8r15: Boolean = true,
        xmm: Boolean = true,
        x87: Boolean = true,
        flags: Boolean = true,
    ) {
        if (gpr) {
            assertRegister("RAX", unicorn.gpr(Unicorn.UC_X86_REG_RAX), test.x86.cpu.regs.rax.value)
            assertRegister("RBX", unicorn.gpr(Unicorn.UC_X86_REG_RBX), test.x86.cpu.regs.rbx.value)
            assertRegister("RCX", unicorn.gpr(Unicorn.UC_X86_REG_RCX), test.x86.cpu.regs.rcx.value)
            assertRegister("RDX", unicorn.gpr(Unicorn.UC_X86_REG_RDX), test.x86.cpu.regs.rdx.value)
            assertRegister("RSP", unicorn.gpr(Unicorn.UC_X86_REG_RSP), test.x86.cpu.regs.rsp.value)
            assertRegister("RBP", unicorn.gpr(Unicorn.UC_X86_REG_RBP), test.x86.cpu.regs.rbp.value)
            assertRegister("RSI", unicorn.gpr(Unicorn.UC_X86_REG_RSI), test.x86.cpu.regs.rsi.value)
            assertRegister("RDI", unicorn.gpr(Unicorn.UC_X86_REG_RDI), test.x86.cpu.regs.rdi.value)
        }

        if (rip) {
            assertRegister("RIP", unicorn.gpr(Unicorn.UC_X86_REG_RIP), test.x86.cpu.regs.rip.value)
        }

        if (r8r15) {
            assertRegister("R8", unicorn.gpr(Unicorn.UC_X86_REG_R8), test.x86.cpu.regs.r8.value)
            assertRegister("R9", unicorn.gpr(Unicorn.UC_X86_REG_R9), test.x86.cpu.regs.r9.value)
            assertRegister("R10", unicorn.gpr(Unicorn.UC_X86_REG_R10), test.x86.cpu.regs.r10.value)
            assertRegister("R11", unicorn.gpr(Unicorn.UC_X86_REG_R11), test.x86.cpu.regs.r11.value)
            assertRegister("R12", unicorn.gpr(Unicorn.UC_X86_REG_R12), test.x86.cpu.regs.r12.value)
            assertRegister("R13", unicorn.gpr(Unicorn.UC_X86_REG_R13), test.x86.cpu.regs.r13.value)
            assertRegister("R14", unicorn.gpr(Unicorn.UC_X86_REG_R14), test.x86.cpu.regs.r14.value)
            assertRegister("R15", unicorn.gpr(Unicorn.UC_X86_REG_R15), test.x86.cpu.regs.r15.value)
        }

        if (xmm) {
            test.x86.sse.xmm.forEachIndexed { i, xmmVal ->
                assertRegister("XMM$i", unicorn.xmm(i), xmmVal)
            }
        }

        if (x87) {
            (0 until x86FPU.FPU_STACK_SIZE).forEach { i ->
                var x86Value = test.x86.fpu.st(i)
                var unicornValue = unicorn.st(i)
                if (x86Value[79..64].ulong == 0xFFFFuL || unicornValue[79..64].ulong == 0xFFFFuL) {
                    // TODO: think of a better way of checking if mmx
                    x86Value = x86Value[63..0]
                    unicornValue = unicornValue[63..0]
                }
                assertRegister("st($i)", unicornValue, x86Value)
            }
            assertRegister("FPU Control Word", unicorn.fpcw(), test.x86.fpu.fwr.FPUControlWord.value)
            assertRegister("FPU Status Word", unicorn.fpsw(), test.x86.fpu.fwr.FPUStatusWord.value)
            assertRegister("FPU Tag Word", unicorn.fptag(), test.x86.fpu.fwr.FPUTagWord.value)
        }

        if (flags) {
            unicorn.flags().also {
                assertFlag("cf", it.cf.truth, test.x86.cpu.flags.cf)
                assertFlag("pf", it.pf.truth, test.x86.cpu.flags.pf)
                assertFlag("af", it.af.truth, test.x86.cpu.flags.af)
                assertFlag("zf", it.zf.truth, test.x86.cpu.flags.zf)
                assertFlag("sf", it.sf.truth, test.x86.cpu.flags.sf)
                assertFlag("df", it.df.truth, test.x86.cpu.flags.df)
                assertFlag("of", it.of.truth, test.x86.cpu.flags.of)
            }
        }
    }

    fun assertMem(addr: ULong, size: Int) {
        val expected = unicorn.ine(addr, size)
        val actual = test.core.ine(addr, size)

        Assert.assertEquals(
            "${test.x86.cpu.insn} -> memory mismatch: ${expected.hex} != ${actual.hex}",
            expected,
            actual,
        )
    }

    fun assertMem(addr: ULong, dtype: Datatype): Unit = assertMem(addr, dtype.bytes)
}

internal fun parallel(test: AX86InstructionTest, unicorn: UnicornEmu, block: Parallel.() -> Unit) {
    unicorn.mmap()
    unicorn.storeExecute(0uL, byteArrayOf(0xdb.byte, 0xe3.byte)) // fninit
    try {
        Parallel(test, unicorn).block()
    } finally {
        unicorn.stop()
        unicorn.unmap()
    }
}
