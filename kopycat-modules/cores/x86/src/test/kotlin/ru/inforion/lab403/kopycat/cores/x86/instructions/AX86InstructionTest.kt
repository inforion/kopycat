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

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.interfaces.*
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.modules.memory.RAM
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull


abstract class AX86InstructionTest: Module(null, "x86InstructionTest") {
    companion object {
        const val startAddress: ULong = 0u//x0001_0000
    }

    abstract val x86: x86Core
    abstract val ram0: RAM
    abstract val ram1: RAM
    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem")
        val io = Bus("io")
    }
    override val buses = Buses()

    abstract val mode: Long
    abstract val bitMode: ByteArray

    private var size = 0uL

    fun execute(offset: ULong = 0u, generator: () -> ByteArray) {
        val data = generator()
        x86.store(startAddress + size, data)
        x86.step()
//        println("%16s -> %s".format(data.hexlify(), x86.cpu.insn))
        size += data.size.uint + offset
        if (size.long < 0) size = 0u
    }

    fun assemble(instruction: String): ByteArray {
        val temp = File.createTempFile("temp", ".asm")
        val resTemp = File.createTempFile("res", ".bin")

        temp.writeText("[bits $mode]\n $instruction")

        val comm = "nasm ${temp.absolutePath} -o ${resTemp.absolutePath} "
        val prc = Runtime.getRuntime().exec(comm.split(" ").toTypedArray())
        prc.waitFor(10, TimeUnit.SECONDS)
        val error = prc.errorStream.bufferedReader().readLine()
        assertNull(error, "Decoding error!")
        val bytes = resTemp.readBytes()
        temp.delete()
        resTemp.delete()
        return bytes
    }

    fun assertAssembly(expected: String) {
        val disasm = x86.cpu.insn.toString()
        val actual = disasm.split(whitespaces).drop(2).joinToString(" ").lowercase()
        assertEquals(expected.lowercase(), actual, "Unexpected disassembly view!")
    }

    private fun assertRegister(num: Int, expected: ULong, actual: ULong, type: String = "GPR") = assertEquals(
        expected,
        actual,
        "${x86.cpu.insn} -> $type $num error: 0x${expected.hex} != 0x${actual.hex}"
    )

    private fun assertFlag(name: String, expected: Boolean, actual: Boolean, type: String = "Flag") = assertEquals(
        expected,
        actual,
        "${x86.cpu.insn} -> $type $name error: $expected != $actual"
    )

    fun assertGPRRegisters(eax: ULong = 0u, ecx: ULong = 0u, edx: ULong = 0u, ebx: ULong = 0u,
                           esp: ULong = 0u, ebp: ULong = 0u, esi: ULong = 0u, edi: ULong = 0u) {
        assertRegister(0, eax, x86.cpu.regs.eax.value)
        assertRegister(1, ecx, x86.cpu.regs.ecx.value)
        assertRegister(2, edx, x86.cpu.regs.edx.value)
        assertRegister(3, ebx, x86.cpu.regs.ebx.value)
        assertRegister(4, esp, x86.cpu.regs.esp.value)
        assertRegister(5, ebp, x86.cpu.regs.ebp.value)
        assertRegister(6, esi, x86.cpu.regs.esi.value)
        assertRegister(7, edi, x86.cpu.regs.edi.value)
    }

    fun gprRegisters(eax: ULong = 0u, ecx: ULong = 0u, edx: ULong = 0u, ebx: ULong = 0u, esp: ULong = 0u,
                     ebp: ULong = 0u, esi: ULong = 0u, edi: ULong = 0u, eip: ULong = 0u) {
        x86.cpu.regs.eax.value = eax
        x86.cpu.regs.ecx.value = ecx
        x86.cpu.regs.edx.value = edx
        x86.cpu.regs.ebx.value = ebx
        x86.cpu.regs.esp.value = esp
        x86.cpu.regs.ebp.value = ebp
        x86.cpu.regs.esi.value = esi
        x86.cpu.regs.edi.value = edi
        x86.cpu.regs.eip.value = eip
    }

    fun gprRegisters64(rax: ULong = 0u, rcx: ULong = 0u, rdx: ULong = 0u, rbx: ULong = 0u, rsp: ULong = 0u,
                     rbp: ULong = 0u, rsi: ULong = 0u, rdi: ULong = 0u) {
        x86.cpu.regs.apply {
            this.rax.value = rax
            this.rcx.value = rcx
            this.rdx.value = rdx
            this.rbx.value = rbx
            this.rsp.value = rsp
            this.rbp.value = rbp
            this.rsi.value = rsi
            this.rdi.value = rdi
        }
    }

    fun mmuRegisters(gdtrBase: ULong = 0u, gdtrLimit: ULong = 0u, ldtr: ULong = 0u) {
        x86.mmu.gdtr.base  = gdtrBase
        x86.mmu.gdtr.limit = gdtrLimit
        x86.mmu.ldtr = ldtr
    }

    fun copRegisters(idtrBase: ULong = 0u, idtrLimit: ULong = 0u) {
        x86.cop.idtr.base  = idtrBase
        x86.cop.idtr.limit = idtrLimit
    }

    fun assertCopRegisters(idtrBase: ULong = 0u, idtrLimit: ULong = 0u, int: Boolean = false, irq: ULong = 0u) {
        assertRegister(0, idtrBase,  x86.cop.idtr.base,  "Cop")
        assertRegister(1, idtrLimit, x86.cop.idtr.limit, "Cop")
        assertFlag("cop", int, x86.cop.INT, "Cop")
        assertRegister(3, irq, x86.cop.IRQ.ulong_s, "Cop")
    }

    fun assertMMURegisters(gdtrBase: ULong = 0u, gdtrLimit: ULong = 0u, ldtr: ULong = 0u) {
        assertRegister(0, gdtrBase,  x86.mmu.gdtr.base,  "MMU")
        assertRegister(1, gdtrLimit, x86.mmu.gdtr.limit, "MMU")
        assertRegister(2, ldtr, x86.mmu.ldtr, "MMU")
    }

    fun assertSegmentRegisters(cs: ULong = 0x08u, ds: ULong = 0x08u, ss: ULong = 0x08u, es: ULong = 0x08u, fs: ULong = 0x08u, gs: ULong = 0x08u) {
        assertRegister(0, cs, x86.cpu.sregs.cs.value, "Segment")
        assertRegister(1, ds, x86.cpu.sregs.ds.value, "Segment")
        assertRegister(2, ss, x86.cpu.sregs.ss.value, "Segment")
        assertRegister(3, es, x86.cpu.sregs.es.value, "Segment")
        assertRegister(4, fs, x86.cpu.sregs.fs.value, "Segment")
        assertRegister(5, gs, x86.cpu.sregs.gs.value, "Segment")
    }

    fun segmentRegisters(cs: ULong = 0x08u, ds: ULong = 0x08u, ss: ULong = 0x08u, es: ULong = 0x08u, fs: ULong = 0x08u, gs: ULong = 0x08u) {
        x86.cpu.sregs.cs.value = cs
        x86.cpu.sregs.ds.value = ds
        x86.cpu.sregs.ss.value = ss
        x86.cpu.sregs.es.value = es
        x86.cpu.sregs.fs.value = fs
        x86.cpu.sregs.gs.value = gs

        x86.cpu.sregs.cs.value = 8u
        x86.cpu.sregs.ds.value = 8u
        x86.cpu.sregs.ss.value = 8u
    }

    fun assertFlagRegisters(ac: Boolean? = null, rf: Boolean? = null, vm: Boolean? = null, vif: Boolean? = null,
                            vip: Boolean? = null, id: Boolean? = null, cf: Boolean? = null, pf: Boolean? = null,
                            af: Boolean? = null, zf: Boolean? = null, sf: Boolean? = null, tf: Boolean? = null,
                            ifq: Boolean? = null, df: Boolean? = null, of: Boolean? = null) {
        ac?.let { assertFlag("ac", it, x86.cpu.flags.ac) }
        rf?.let { assertFlag("rf", it, x86.cpu.flags.rf) }
        vm?.let { assertFlag("vm", it, x86.cpu.flags.vm) }
        vif?.let { assertFlag("vif", it, x86.cpu.flags.vif) }
        vip?.let { assertFlag("vip", it, x86.cpu.flags.vip) }
        id?.let { assertFlag("idq", it, x86.cpu.flags.idq) }
        cf?.let { assertFlag("cf", it, x86.cpu.flags.cf) }
        pf?.let { assertFlag("pf", it, x86.cpu.flags.pf) }
        af?.let { assertFlag("af", it, x86.cpu.flags.af) }
        zf?.let { assertFlag("zf", it, x86.cpu.flags.zf) }
        sf?.let { assertFlag("sf", it, x86.cpu.flags.sf) }
        tf?.let { assertFlag("tf", it, x86.cpu.flags.tf) }
        ifq?.let { assertFlag("ifq", it, x86.cpu.flags.ifq) }
        df?.let { assertFlag("df", it, x86.cpu.flags.df) }
        of?.let { assertFlag("of", it, x86.cpu.flags.of) }
    }

    fun flagRegisters(ac: Boolean = false, rf: Boolean = false, vm: Boolean = false, vif: Boolean = false,
                      vip: Boolean = false, id: Boolean = false, cf: Boolean = false, pf: Boolean = false,
                      af: Boolean = false, zf: Boolean = false, sf: Boolean = false, tf: Boolean = false,
                      ifq: Boolean = false, df: Boolean = false, of: Boolean = false) {
        x86.cpu.flags.ac = ac
        x86.cpu.flags.rf = rf
        x86.cpu.flags.vm = vm
        x86.cpu.flags.vif = vif
        x86.cpu.flags.vip = vip
        x86.cpu.flags.idq = id
        x86.cpu.flags.cf = cf
        x86.cpu.flags.pf = pf
        x86.cpu.flags.af = af
        x86.cpu.flags.zf = zf
        x86.cpu.flags.sf = sf
        x86.cpu.flags.tf = tf
        x86.cpu.flags.ifq = ifq
        x86.cpu.flags.df = df
        x86.cpu.flags.of = of
    }

    fun eflag(eflags: ULong = 0uL) { x86.cpu.flags.eflags.value = eflags }

    fun assertEflag(eflags: ULong = 0uL) = assertRegister(0, eflags, x86.cpu.flags.eflags.value, "Flag")

    fun iopl(iopl: Int = 0) { x86.cpu.flags.iopl = iopl.ulong_z }

    fun assertIopl(iopl: Int = 0) = assertRegister(0, iopl.ulong_z, x86.cpu.flags.iopl, "Flag")


    fun load(address: ULong, size: Int): String = x86.load(address, size).hexlify()
    fun load(address: ULong, dtyp: Datatype, io: Boolean = false): ULong =
            if(io) x86.ports.io.read(dtyp, address, 0)
            else x86.read(dtyp, address, 0)
    fun store(address: ULong, data: String) = x86.store(address, data.unhexlify())
    fun store(address: ULong, data: ULong, dtyp: Datatype, io: Boolean = false) =
            if(io) x86.ports.io.write(dtyp, address, data, 0)
            else x86.write(dtyp, address, data, 0)

    fun assertMemory(address: ULong, expected: String) {
        assert(expected.length % 2 == 0)
        val size = expected.length / 2
        val actual = load(address, size)
        assertEquals(expected.uppercase(), actual, "Memory 0x${address.hex8} error: $expected != $actual")
    }

    fun assertMemory(address: ULong, expected: ULong, dtyp: Datatype, io: Boolean = false) {
        val actual = load(address, dtyp, io)
        assertEquals(expected, actual, "Memory 0x${address.hex8} error: $expected != $actual")
    }

    @BeforeEach
    fun resetTest() {
        x86.reset()
        x86.cpu.cregs.cr0.pe = true
        x86.store(0x8u, bitMode)
        x86.cpu.regs.eip.value = 0u
        x86.mmu.gdtr.base = 0u
        x86.mmu.gdtr.limit = 0x20u
        x86.cpu.sregs.cs.value = 8u
        x86.cpu.sregs.ds.value = 8u
        x86.cpu.sregs.ss.value = 8u
        x86.cpu.sregs.es.value = 8u
        x86.cpu.sregs.fs.value = 8u
        x86.cpu.sregs.gs.value = 8u
        x86.cpu.regs.esp.value = 0x1000u
    }

    @AfterEach
    fun checkPC() {
        assertEquals(
            size,
            x86.cpu.regs.rip.value,
            "Program counter error: ${size.hex8} != ${x86.cpu.regs.rip.value.hex8}"
        )
    }

    fun assertDecode(assembled: ByteArray, kc: String) {
        x86.store(x86.pc, assembled)
        x86.doDecodeInstruction()
        assertAssembly(kc)
    }

    fun assertDecode(nasm: String, kc: String) = assertDecode(assemble(nasm), kc)
}
