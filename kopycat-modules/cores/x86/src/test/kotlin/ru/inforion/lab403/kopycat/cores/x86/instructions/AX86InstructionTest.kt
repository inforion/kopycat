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

import org.junit.After
import org.junit.Assert
import org.junit.Before
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import ru.inforion.lab403.kopycat.modules.memory.RAM
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.assertNull


abstract class AX86InstructionTest: Module(null, "x86InstructionTest") {
    abstract val x86: x86Core
    abstract val ram0: RAM
    abstract val ram1: RAM
    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem")
        val io = Bus("io", BUS16)
    }
    override val buses = Buses()

    abstract val mode: Long
    abstract val bitMode: ByteArray

    var size = 0L
    val startAddress: Long = 0//x0001_0000

    fun execute(offset: Long = 0, generator: () -> ByteArray) {
        val data = generator()
        x86.store(startAddress + size, data)
        x86.step()
        println("%16s -> %s".format(data.hexlify(), x86.cpu.insn))
        size += data.size + offset
        if(size < 0) size = 0
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
        return resTemp.readBytes()
    }

    fun assertAssembly(expected: String) {
        val disasm = x86.cpu.insn.toString()
        val actual = disasm.splitWhitespaces().drop(2).joinToString(" ").toLowerCase()
        Assert.assertEquals("Unexpected disassembly view!", expected.toLowerCase(), actual)
    }

    private fun assertRegister(num: Int, expected: Long, actual: Long, type: String = "GPR") =
            Assert.assertEquals("${x86.cpu.insn} -> " +
                    "$type $num error: 0x${expected.hex} != 0x${actual.hex}", expected, actual)

    private fun assertFlag(num: Int, expected: Boolean, actual: Boolean, type: String = "Flag") =
            Assert.assertEquals("${x86.cpu.insn} -> $type $num error: $expected != $actual", expected, actual)

    fun assertGPRRegisters(eax: Long = 0, ecx: Long = 0, edx: Long = 0, ebx: Long = 0,
                           esp: Long = 0, ebp: Long = 0, esi: Long = 0, edi: Long = 0) {
        assertRegister(0, eax, x86.cpu.regs.eax)
        assertRegister(1, ecx, x86.cpu.regs.ecx)
        assertRegister(2, edx, x86.cpu.regs.edx)
        assertRegister(3, ebx, x86.cpu.regs.ebx)
        assertRegister(4, esp, x86.cpu.regs.esp)
        assertRegister(5, ebp, x86.cpu.regs.ebp)
        assertRegister(6, esi, x86.cpu.regs.esi)
        assertRegister(7, edi, x86.cpu.regs.edi)
    }

    fun gprRegisters(eax: Long = 0, ecx: Long = 0, edx: Long = 0, ebx: Long = 0, esp: Long = 0,
                     ebp: Long = 0, esi: Long = 0, edi: Long = 0, eip: Long = 0) {
        x86.cpu.regs.eax = eax
        x86.cpu.regs.ecx = ecx
        x86.cpu.regs.edx = edx
        x86.cpu.regs.ebx = ebx
        x86.cpu.regs.esp = esp
        x86.cpu.regs.ebp = ebp
        x86.cpu.regs.esi = esi
        x86.cpu.regs.edi = edi
        x86.cpu.regs.eip = eip
    }

    fun mmuRegisters(gdtrBase: Long = 0, gdtrLimit: Long = 0, ldtr: Long = 0) {
        x86.mmu.gdtr.base  = gdtrBase
        x86.mmu.gdtr.limit = gdtrLimit
        x86.mmu.ldtr = ldtr
    }

    fun copRegisters(idtrBase: Long = 0, idtrLimit: Long = 0) {
        x86.cop.idtr.base  = idtrBase
        x86.cop.idtr.limit = idtrLimit
    }

    fun assertCopRegisters(idtrBase: Long = 0, idtrLimit: Long = 0, int: Boolean = false, irq: Long = 0) {
        assertRegister(0, idtrBase,  x86.cop.idtr.base,  "Cop")
        assertRegister(1, idtrLimit, x86.cop.idtr.limit, "Cop")
        assertFlag(2, int, x86.cop.INT, "Cop")
        assertRegister(3, irq, x86.cop.IRQ.toLong(), "Cop")
    }

    fun assertMMURegisters(gdtrBase: Long = 0, gdtrLimit: Long = 0, ldtr: Long = 0) {
        assertRegister(0, gdtrBase,  x86.mmu.gdtr.base,  "MMU")
        assertRegister(1, gdtrLimit, x86.mmu.gdtr.limit, "MMU")
        assertRegister(2, ldtr, x86.mmu.ldtr, "MMU")
    }

    fun assertSegmentRegisters(cs: Long = 0x08, ds: Long = 0x08, ss: Long = 0x08, es: Long = 0x08, fs: Long = 0x08, gs: Long = 0x08) {
        assertRegister(0, cs, x86.cpu.sregs.cs, "Segment")
        assertRegister(1, ds, x86.cpu.sregs.ds, "Segment")
        assertRegister(2, ss, x86.cpu.sregs.ss, "Segment")
        assertRegister(3, es, x86.cpu.sregs.es, "Segment")
        assertRegister(4, fs, x86.cpu.sregs.fs, "Segment")
        assertRegister(5, gs, x86.cpu.sregs.gs, "Segment")
    }

    fun segmentRegisters(cs: Long = 0x08, ds: Long = 0x08, ss: Long = 0x08, es: Long = 0x08, fs: Long = 0x08, gs: Long = 0x08) {
        x86.cpu.sregs.cs = cs
        x86.cpu.sregs.ds = ds
        x86.cpu.sregs.ss = ss
        x86.cpu.sregs.es = es
        x86.cpu.sregs.fs = fs
        x86.cpu.sregs.gs = gs

        x86.cpu.sregs.cs = 8
        x86.cpu.sregs.ds = 8
        x86.cpu.sregs.ss = 8
    }

    fun assertFlagRegisters(ac: Boolean = false, rf: Boolean = false, vm: Boolean = false, vif: Boolean = false,
                            vip: Boolean = false, id: Boolean = false, cf: Boolean = false, pf: Boolean = false,
                            af: Boolean = false, zf: Boolean = false, sf: Boolean = false, tf: Boolean = false,
                            ifq: Boolean = false, df: Boolean = false, of: Boolean = false) {
        assertFlag(0, ac, x86.cpu.flags.ac)
        assertFlag(1, rf, x86.cpu.flags.rf)
        assertFlag(2, vm, x86.cpu.flags.vm)
        assertFlag(3, vif, x86.cpu.flags.vif)
        assertFlag(4, vip, x86.cpu.flags.vip)
        assertFlag(5, id, x86.cpu.flags.id)
        assertFlag(6, cf, x86.cpu.flags.cf)
        assertFlag(7, pf, x86.cpu.flags.pf)
        assertFlag(8, af, x86.cpu.flags.af)
        assertFlag(9, zf, x86.cpu.flags.zf)
        assertFlag(10, sf, x86.cpu.flags.sf)
        assertFlag(11, tf, x86.cpu.flags.tf)
        assertFlag(12, ifq, x86.cpu.flags.ifq)
        assertFlag(13, df, x86.cpu.flags.df)
        assertFlag(14, of, x86.cpu.flags.of)
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
        x86.cpu.flags.id = id
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

    fun eflag(eflags: Long = 0L) { x86.cpu.flags.eflags = eflags }

    fun assertEflag(eflags: Long = 0L) = assertRegister(0, eflags, x86.cpu.flags.eflags, "Flag")

    fun iopl(iopl: Int = 0) { x86.cpu.flags.iopl = iopl }

    fun assertIopl(iopl: Int = 0) = assertRegister(0, iopl.toLong(), x86.cpu.flags.iopl.toLong(), "Flag")


    fun load(address: Long, size: Int): String = x86.load(address, size).hexlify()
    fun load(address: Long, dtyp: Datatype, io: Boolean = false): Long =
            if(io) x86.ports.io.read(dtyp, address, 0)
            else x86.read(dtyp, address, 0)
    fun store(address: Long, data: String) = x86.store(address, data.unhexlify())
    fun store(address: Long, data: Long, dtyp: Datatype, io: Boolean = false) =
            if(io) x86.ports.io.write(dtyp, address, data, 0)
            else x86.write(dtyp, address, data, 0)

    fun assertMemory(address: Long, expected: String) {
        assert(expected.length % 2 == 0)
        val size = expected.length / 2
        val actual = load(address, size)
        Assert.assertEquals("Memory 0x${address.hex8} error: $expected != $actual", expected, actual)
    }

    fun assertMemory(address: Long, expected: Long, dtyp: Datatype, io: Boolean = false) {
        val actual = load(address, dtyp, io)
        Assert.assertEquals("Memory 0x${address.hex8} error: $expected != $actual", expected, actual)
    }

    @Before
    fun resetTest() {
        x86.reset()
        x86Register.CTRLR.cr0.pe(x86, true)
        x86.store(0x8, bitMode)
        x86.cpu.regs.eip = 0
        x86.mmu.gdtr.base = 0
        x86.mmu.gdtr.limit = 0x20
        x86.cpu.sregs.cs = 8
        x86.cpu.sregs.ds = 8
        x86.cpu.sregs.ss = 8
        x86.cpu.sregs.es = 8
        x86.cpu.sregs.fs = 8
        x86.cpu.sregs.gs = 8
        x86.cpu.regs.esp = 0x1000
    }

    @After
    fun checkPC() {
        Assert.assertEquals("Program counter error: ${(size).hex8} != ${x86.cpu.regs.eip.hex8}",
                size, x86.cpu.regs.eip)
    }
}