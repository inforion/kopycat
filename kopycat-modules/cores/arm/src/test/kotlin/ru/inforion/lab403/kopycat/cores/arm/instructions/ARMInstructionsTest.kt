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
package ru.inforion.lab403.kopycat.cores.arm.instructions

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.modules.cores.ARMv7Core
import ru.inforion.lab403.kopycat.modules.memory.RAM
import unicorn.Unicorn
import unicorn.Unicorn.*
import java.util.logging.Level
import kotlin.test.assertTrue

class ARMInstructionsTest: Module(null, "ARMv7InstructionTest") {
    inner class Buses: ModuleBuses(this) {
        val mem = Bus("mem")
    }

    override val buses = Buses()

    private val arm = ARMv7Core(this, "arm", 48.MHz, 1.0)
    private val ram = RAM(this, "ram", 0x2_0000)
    private val boot = RAM(this, "boot", 0x0_1000)
    private var insnSize = 0
    private val startAddress: Long = 0

    init {
//        log.level = Level.SEVERE
        arm.ports.mem.connect(buses.mem)
        ram.ports.mem.connect(buses.mem, 0x0000_0000)
        boot.ports.mem.connect(buses.mem, 0x0800_0000)
        initializeAndResetAsTopInstance()
    }

    private fun createUnicorn(): Unicorn {
        val unc = Unicorn(UC_ARCH_ARM, UC_MODE_ARM)
        unc.reg_write(UC_ARM_REG_APSR, 0)
        return unc
    }

    private var unicorn = createUnicorn()

    private fun execute(data: ByteArray, offset: Int = 0) {
        arm.store(startAddress + insnSize, data)
        arm.step()
        println("%16s -> %s".format(data.hexlify(), arm.cpu.insn))

        unicorn.mem_write(startAddress + insnSize, data)
        unicorn.emu_start(startAddress + insnSize, startAddress + insnSize + data.size + 1, 0, 1)

        insnSize += data.size + offset
    }

    private fun assemble(instruction: String): ByteArray {
        val shell = Shell("kstool", "arm", instruction).execute()
        assertTrue("Decoding error: ${shell.stderr}") { shell.stderr.isBlank() }
        val raw = shell.stdout.removePrefix(instruction)
        val start = raw.indexOf('[') + 1
        val last = raw.lastIndex - 1
        return raw.substring(start, last).unhexlify()
    }

    private fun Unicorn.regRead(reg: Int): Long = this.reg_read(reg) as Long mask 32

    private fun regs(r0: Long = 0,  r1: Long = 0,  r2: Long = 0,  r3: Long = 0,  r4: Long = 0,
             r5: Long = 0,  r6: Long = 0,  r7: Long = 0,  r8: Long = 0,  r9: Long = 0,
             r10: Long = 0, r11: Long = 0, r12: Long = 0, r13: Long = 0, r14: Long = 0) {
        arm.cpu.regs.r0.value = r0
        arm.cpu.regs.r1.value = r1
        arm.cpu.regs.r2.value = r2
        arm.cpu.regs.r3.value = r3
        arm.cpu.regs.r4.value = r4

        arm.cpu.regs.r5.value = r5
        arm.cpu.regs.r6.value = r6
        arm.cpu.regs.r7.value = r7
        arm.cpu.regs.r8.value = r8
        arm.cpu.regs.r9.value = r9

        arm.cpu.regs.r10.value = r10
        arm.cpu.regs.r11.value = r11
        arm.cpu.regs.r12.value = r12
        arm.cpu.regs.sp.value = r13
        arm.cpu.regs.lr.value = r14

        unicorn.reg_write(UC_ARM_REG_R0, r0)
        unicorn.reg_write(UC_ARM_REG_R1, r1)
        unicorn.reg_write(UC_ARM_REG_R2, r2)
        unicorn.reg_write(UC_ARM_REG_R3, r3)
        unicorn.reg_write(UC_ARM_REG_R4, r4)

        unicorn.reg_write(UC_ARM_REG_R5, r5)
        unicorn.reg_write(UC_ARM_REG_R6, r6)
        unicorn.reg_write(UC_ARM_REG_R7, r7)
        unicorn.reg_write(UC_ARM_REG_R8, r8)
        unicorn.reg_write(UC_ARM_REG_R9, r9)

        unicorn.reg_write(UC_ARM_REG_R10, r10)
        unicorn.reg_write(UC_ARM_REG_R11, r11)
        unicorn.reg_write(UC_ARM_REG_R12, r12)
        unicorn.reg_write(UC_ARM_REG_R13, r13)
        unicorn.reg_write(UC_ARM_REG_R14, r14)
    }

    private fun flags(n: Long = 0, z: Long = 0,
              c: Long = 0, v: Long = 0) {

        arm.cpu.flags.n = n.toBool()
        arm.cpu.flags.z = z.toBool()
        arm.cpu.flags.c = c.toBool()
        arm.cpu.flags.v = v.toBool()

        unicorn.reg_write(UC_ARM_REG_APSR, arm.cpu.sregs.apsr.value)
    }

    private fun status(q: Long = 0, ge: Long = 0) {
        arm.cpu.status.q = q.toBool()
        arm.cpu.status.ge = ge.asLong

        unicorn.reg_write(UC_ARM_REG_CPSR, arm.cpu.sregs.cpsr.value)
    }

    private fun load(address: Long, size: Int): String = arm.load(address, size).hexlify()
    private fun load(address: Long, dtyp: Datatype): Long = arm.read(dtyp, address, 0)

    private fun store(address: Long, data: String) {
        arm.store(address, data.unhexlify())
        unicorn.mem_write(address, data.toByteArray())
    }

    private fun store(address: Long, data: Long, dtyp: Datatype) {
        arm.write(dtyp, address, data, 0)
        unicorn.mem_write(address, data.pack(dtyp.bytes))
    }

    private fun assertRegister(num: Int, expected: Long, actual: Long, type: String = "GPR") =
            Assert.assertEquals("${arm.cpu.insn} -> $type r$num error: 0x${expected.hex8} != 0x${actual.hex8}", expected, actual)

    private fun assertRegisters() {
        assertRegister(0,  unicorn.regRead(UC_ARM_REG_R0),  arm.cpu.regs.r0.value)
        assertRegister(1,  unicorn.regRead(UC_ARM_REG_R1),  arm.cpu.regs.r1.value)
        assertRegister(2,  unicorn.regRead(UC_ARM_REG_R2),  arm.cpu.regs.r2.value)
        assertRegister(3,  unicorn.regRead(UC_ARM_REG_R3),  arm.cpu.regs.r3.value)
        assertRegister(4,  unicorn.regRead(UC_ARM_REG_R4),  arm.cpu.regs.r4.value)
        assertRegister(5,  unicorn.regRead(UC_ARM_REG_R5),  arm.cpu.regs.r5.value)
        assertRegister(6,  unicorn.regRead(UC_ARM_REG_R6),  arm.cpu.regs.r6.value)
        assertRegister(7,  unicorn.regRead(UC_ARM_REG_R7),  arm.cpu.regs.r7.value)
        assertRegister(8,  unicorn.regRead(UC_ARM_REG_R8),  arm.cpu.regs.r8.value)
        assertRegister(9,  unicorn.regRead(UC_ARM_REG_R9),  arm.cpu.regs.r9.value)
        assertRegister(10, unicorn.regRead(UC_ARM_REG_R10), arm.cpu.regs.r10.value)
        assertRegister(11, unicorn.regRead(UC_ARM_REG_R11), arm.cpu.regs.r11.value)
        assertRegister(12, unicorn.regRead(UC_ARM_REG_R12), arm.cpu.regs.r12.value)
        assertRegister(13, unicorn.regRead(UC_ARM_REG_R13), arm.cpu.regs.sp.value)
        assertRegister(14, unicorn.regRead(UC_ARM_REG_R14), arm.cpu.regs.lr.value)
    }

    private fun assertFlag(chr: String, expected: Long, actual: Long, type: String = "Flag") {
        Assert.assertEquals("${arm.cpu.insn} -> $type ${chr.toUpperCase()} error: $expected != $actual",
                expected, actual)
    }

    private fun assertFlags() {
        assertFlag("n",  unicorn.regRead(UC_ARM_REG_CPSR)[31],  arm.cpu.flags.n.asLong)
        assertFlag("z",  unicorn.regRead(UC_ARM_REG_CPSR)[30],  arm.cpu.flags.z.asLong)
        assertFlag("c",  unicorn.regRead(UC_ARM_REG_CPSR)[29],  arm.cpu.flags.c.asLong)
        assertFlag("v",  unicorn.regRead(UC_ARM_REG_CPSR)[28],  arm.cpu.flags.v.asLong)
        assertFlag("q",  unicorn.regRead(UC_ARM_REG_CPSR)[27],  arm.cpu.status.q.asLong)
        assertFlag("ge", unicorn.regRead(UC_ARM_REG_CPSR)[19..16],  arm.cpu.status.ge)
    }

    private fun assertMemory() {
        Assert.assertArrayEquals("${arm.cpu.insn} -> Memory error",
                unicorn.mem_read(startAddress, ram.size.asULong),
                arm.load(startAddress, ram.size))
    }

    private fun assertException(exception: ARMHardwareException) {
        assertTrue { arm.cpu.exception === exception }
    }

    @Before fun resetTest() {
        arm.reset()
        unicorn.mem_map(startAddress, ram.size.asULong, UC_PROT_ALL)
    }

    @After fun checkPC() {
        Assert.assertEquals("Program counter error: ${unicorn.regRead(UC_ARM_REG_R15).hex8} != ${arm.cpu.pc.hex8}",
                unicorn.regRead(UC_ARM_REG_R15), arm.cpu.pc)
        unicorn.emu_stop()
        unicorn.mem_unmap(startAddress, ram.size.asULong)
    }

    @Test fun condEQFalse() {
        regs(r0 = 0x21)
        execute(assemble("moveq r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condEQTrue() {
        regs(r0 = 0x21)
        flags(z = 1)
        execute(assemble("moveq r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condNEFalse() {
        regs(r0 = 0x21)
        flags(z = 1)
        execute(assemble("movne r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condNETrue() {
        regs(r0 = 0x21)
        execute(assemble("movne r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condCSFalse() {
        regs(r0 = 0x21)
        execute(assemble("movcs r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condCSTrue() {
        regs(r0 = 0x21)
        flags(c = 1)
        execute(assemble("movcs r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condCCFalse() {
        regs(r0 = 0x21)
        flags(c = 1)
        execute(assemble("movcc r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condCCTrue() {
        regs(r0 = 0x21)
        execute(assemble("movcc r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condMIFalse() {
        regs(r0 = 0x21)
        execute(assemble("movmi r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condMITrue() {
        regs(r0 = 0x21)
        flags(n = 1)
        execute(assemble("movmi r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condPLFalse() {
        regs(r0 = 0x21)
        flags(n = 1)
        execute(assemble("movpl r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condPLTrue() {
        regs(r0 = 0x21)
        execute(assemble("movpl r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condVSFalse() {
        regs(r0 = 0x21)
        execute(assemble("movvs r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condVSTrue() {
        regs(r0 = 0x21)
        flags(v = 1)
        execute(assemble("movvs r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condVCFalse() {
        regs(r0 = 0x21)
        flags(v = 1)
        execute(assemble("movvc r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condVCTrue() {
        regs(r0 = 0x21)
        execute(assemble("movvc r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condHIFalse1() {
        regs(r0 = 0x21)
        execute(assemble("movhi r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condHIFalse2() {
        regs(r0 = 0x21)
        flags(z = 1)
        execute(assemble("movhi r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condHIFalse3() {
        regs(r0 = 0x21)
        flags(z = 1, c = 1)
        execute(assemble("movhi r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condHITrue() {
        regs(r0 = 0x21)
        flags(c = 1)
        execute(assemble("movhi r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condLSFalse() {
        regs(r0 = 0x21)
        flags(c = 1)
        execute(assemble("movls r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condLSTrue1() {
        regs(r0 = 0x21)
        execute(assemble("movls r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condLSTrue2() {
        regs(r0 = 0x21)
        flags(z = 1, c = 1)
        execute(assemble("movls r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condLSTrue3() {
        regs(r0 = 0x21)
        flags(z = 1)
        execute(assemble("movls r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condGEFalse1() {
        regs(r0 = 0x21)
        flags(n = 1)
        execute(assemble("movge r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condGEFalse2() {
        regs(r0 = 0x21)
        flags(v = 1)
        execute(assemble("movge r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condGETrue1() {
        regs(r0 = 0x21)
        execute(assemble("movge r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condGETrue2() {
        regs(r0 = 0x21)
        flags(v = 1, n = 1)
        execute(assemble("movge r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condLTFalse1() {
        regs(r0 = 0x21)
        execute(assemble("movlt r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condLTFalse2() {
        regs(r0 = 0x21)
        flags(v = 1, n = 1)
        execute(assemble("movlt r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condLTTrue1() {
        regs(r0 = 0x21)
        flags(n = 1)
        execute(assemble("movlt r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condLTTrue2() {
        regs(r0 = 0x21)
        flags(v = 1)
        execute(assemble("movlt r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condGTFalse1() {
        regs(r0 = 0x21)
        flags(z = 1)
        execute(assemble("movgt r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condGTFalse2() {
        regs(r0 = 0x21)
        flags(z = 1, n = 1, v = 1)
        execute(assemble("movgt r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condGTFalse3() {
        regs(r0 = 0x21)
        flags(n = 1)
        execute(assemble("movgt r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condGTFalse4() {
        regs(r0 = 0x21)
        flags(v = 1)
        execute(assemble("movgt r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condGTTrue1() {
        regs(r0 = 0x21)
        execute(assemble("movgt r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condGTTrue2() {
        regs(r0 = 0x21)
        flags(n = 1, v = 1)
        execute(assemble("movgt r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condLEFalse1() {
        regs(r0 = 0x21)
        execute(assemble("movle r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condLEFalse2() {
        regs(r0 = 0x21)
        flags(n = 1, v = 1)
        execute(assemble("movle r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condLETrue1() {
        regs(r0 = 0x21)
        flags(z = 1)
        execute(assemble("movle r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condLETrue2() {
        regs(r0 = 0x21)
        flags(n = 1)
        execute(assemble("movle r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condLETrue3() {
        regs(r0 = 0x21)
        flags(v = 1)
        execute(assemble("movle r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun condLETrue4() {
        regs(r0 = 0x21)
        flags(z = 1, v = 1)
        execute(assemble("movle r0, #0x37"))
        assertRegisters()
        assertFlags()
    }

    @Test fun adcsImmCZ() {
        flags(c = 1)
        regs(r2 = 0xFF_FFFF)
        execute(assemble("adcs r5, r2, 0xFF000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcsImmN() {
        flags()
        regs(r2 = 0xFF_FFFF)
        execute(assemble("adcs r5, r2, 0x9F000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcsImmZ() {
        flags()
        regs()
        execute(assemble("adcs r5, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcsImmPC() {
        regs()
        flags(c = 1, n = 1)
        execute(assemble("adcs pc, r2, #0x0"))
        assertMemory()
        assertRegisters()
        assertException(Unpredictable)
    }
    @Test fun adcsImm1() {
        regs(r2 = 0x4, r1 = 0x8)
        flags(c = 1)
        execute(assemble("adcs r1, r2, #0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcsImm2() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xFFFF_FFFF)
        execute(assemble("adcs r2, r1"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcsImm3() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xF0F_0F0F, r3 = 0xF0F0_F0F0)
        execute(assemble("adcs r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcsImm4() {
        flags(n = 1, v = 1)
        regs(r2 = 0xF0F_0F0F, r3 = 0xF0F0_F0F0)
        execute(assemble("adcs r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcsImm5() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xBABA_CAFE, r3 = 0x3615_2482)
        execute(assemble("adcs r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcsRegVN() {
        regs(r2 = 0x7FFF_FFFF)
        flags(c = 1)
        execute(assemble("adcs r5, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcsRegSh() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("adcs r5, r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcImmVN() {
        flags(v = 1, n = 1)
        regs(r2 = 0xFF_FFFF)
        execute(assemble("adc r5, r2, 0xFF000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcImmVZC() {
        flags(v = 1, z = 1, c = 1)
        regs(r2 = 0xFF_FFFF)
        execute(assemble("adc r5, r2, 0x9F000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcImmPC() {
        regs(r2 = 0x4)
        flags(z = 1)
        execute(assemble("adc pc, r2, #0x10"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun adcImm() {
        regs(r2 = 0x4, r1 = 0x8)
        flags(c = 1)
        execute(assemble("adc r1, r2, #0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcRegVNC() {
        flags(n = 1, v = 1, c = 1)
        regs()
        execute(assemble("adc r5, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcRegCZ() {
        regs(r2 = 0x7FFF_FFFF)
        flags(c = 1, z = 1)
        execute(assemble("adc r5, r2"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun adcReg1() {
        regs(r2 = 0x4, r1 = 0x8, r3 = 0xCC, r5 = 0x4)
        flags(c = 1)
        execute(assemble("adc r1, r2, r3, LSR #21"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcReg2() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("adc r5, r2, r8, LSR #12"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcRegSh1() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0xFF, r5 = 0xFACA, r8 = 0x56)
        execute(assemble("adc r5, r2, r8, LSL r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun adcRegSh2() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("adc r5, r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun addsImmPC() {
        regs()
        flags(c = 1, n = 1)
        execute(assemble("adds pc, r2, #0x0"))
        assertMemory()
        assertRegisters()
        assertException(Unpredictable)
    }
    @Test fun addsImm() {
        regs(r2 = 0xFFFF_FFF4, r1 = 0x8)
        flags(c = 1)
        execute(assemble("adds r1, r2, #0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun addImmPC() {
        regs(r2 = 0x4)
        flags(z = 1)
        execute(assemble("add pc, r2, #0x10"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun addImm1() {
        regs(r2 = 0x4, r1 = 0x8)
        flags(c = 1)
        execute(assemble("add r1, r2, #0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun addImm2() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xFFFF_FFFF)
        execute(assemble("add r2, r1"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun addImm3() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xF0F_0F0F, r3 = 0xF0F0_F0F0)
        execute(assemble("add r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun addImm4() {
        flags(n = 1, v = 1)
        regs(r2 = 0xF0F_0F0F, r3 = 0xF0F0_F0F0)
        execute(assemble("add r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun addImm5() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xBABA_CAFE, r3 = 0x3615_2482)
        execute(assemble("add r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun addReg1() {
        regs(r2 = 0x4, r1 = 0x8, r3 = 0xCC, r5 = 0x4)
        flags(c = 1)
        execute(assemble("add r1, r2, r3, LSR #21"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun addReg2() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("add r5, r2, r8, LSR #12"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun addRegSh1() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0xFF, r5 = 0xFACA, r8 = 0x56)
        execute(assemble("add r5, r2, r8, LSL r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun addRegSh2() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("add r5, r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun rsbsImmPC() {
        regs()
        flags(c = 1, n = 1)
        execute(assemble("rsbs pc, r2, #0x0"))
        assertMemory()
        assertRegisters()
        assertException(Unpredictable)
    }
    @Test fun rsbsImm() {
        regs(r2 = 0xFFFF_FFF4, r1 = 0x8)
        flags(c = 1)
        execute(assemble("rsbs r1, r2, #0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rsbImmPC() {
        regs(r2 = 0x4)
        flags(z = 1)
        execute(assemble("rsb pc, r2, #0x10"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun rsbImm1() {
        regs(r2 = 0x4, r1 = 0x8)
        flags(c = 1)
        execute(assemble("rsb r1, r2, #0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rsbImm2() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xFFFF_FFFF)
        execute(assemble("rsb r2, r1"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rsbImm3() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xF0F_0F0F, r3 = 0xF0F0_F0F0)
        execute(assemble("rsb r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rsbImm4() {
        flags(n = 1, v = 1)
        regs(r2 = 0xF0F_0F0F, r3 = 0xF0F0_F0F0)
        execute(assemble("rsb r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rsbImm5() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xBABA_CAFE, r3 = 0x3615_2482)
        execute(assemble("rsb r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rsbReg1() {
        regs(r2 = 0x4, r1 = 0x8, r3 = 0xCC, r5 = 0x4)
        flags(c = 1)
        execute(assemble("rsb r1, r2, r3, LSR #21"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rsbReg2() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("rsb r5, r2, r8, LSR #12"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rsbRegSh1() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0xFF, r5 = 0xFACA, r8 = 0x56)
        execute(assemble("rsb r5, r2, r8, LSL r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rsbRegSh2() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("rsb r5, r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun rscsImmPC() {
        regs()
        flags(c = 1, n = 1)
        execute(assemble("rscs pc, r2, #0x0"))
        assertMemory()
        assertRegisters()
        assertException(Unpredictable)
    }
    @Test fun rscsImm() {
        regs(r2 = 0xFFFF_FFF4, r1 = 0x8)
        flags(c = 1)
        execute(assemble("rscs r1, r2, #0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rscImmPC() {
        regs(r2 = 0xFFFF_FFFB)
        flags(z = 1)
        execute(assemble("rsc pc, r2, #0x10"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun rscImm1() {
        regs(r2 = 0x4, r1 = 0x8)
        flags(c = 1)
        execute(assemble("rsc r1, r2, #0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rscImm2() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xFFFF_FFFF)
        execute(assemble("rsc r2, r1"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rscImm3() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xF0F_0F0F, r3 = 0xF0F0_F0F0)
        execute(assemble("rsc r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rscImm4() {
        flags(n = 1, v = 1)
        regs(r2 = 0xF0F_0F0F, r3 = 0xF0F0_F0F0)
        execute(assemble("rsc r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rscImm5() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xBABA_CAFE, r3 = 0x3615_2482)
        execute(assemble("rsc r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rscReg1() {
        regs(r2 = 0x4, r1 = 0x8, r3 = 0xCC, r5 = 0x4)
        flags(c = 1)
        execute(assemble("rsc r1, r2, r3, LSR #21"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rscReg2() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("rsc r5, r2, r8, LSR #12"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rscRegSh1() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0xFF, r5 = 0xFACA, r8 = 0x56)
        execute(assemble("rsc r5, r2, r8, LSL r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rscRegSh2() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("rsc r5, r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun sbcsImmPC() {
        regs()
        flags(c = 1, n = 1)
        execute(assemble("sbcs pc, r2, #0x0"))
        assertMemory()
        assertRegisters()
        assertException(Unpredictable)
    }
    @Test fun sbcsImm() {
        regs(r2 = 0xFFFF_FFF4, r1 = 0x8)
        flags(c = 1)
        execute(assemble("sbcs r1, r2, #0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sbcImm1() {
        regs(r2 = 0x4, r1 = 0x8)
        flags(c = 1)
        execute(assemble("sbc r1, r2, #0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sbcImm2() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xFFFF_FFFF)
        execute(assemble("sbc r2, r1"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sbcImm3() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xF0F_0F0F, r3 = 0xF0F0_F0F0)
        execute(assemble("sbc r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sbcImm4() {
        flags(n = 1, v = 1)
        regs(r2 = 0xF0F_0F0F, r3 = 0xF0F0_F0F0)
        execute(assemble("sbc r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sbcImm5() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xBABA_CAFE, r3 = 0x3615_2482)
        execute(assemble("sbc r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sbcReg1() {
        regs(r2 = 0x4, r1 = 0x8, r3 = 0xCC, r5 = 0x4)
        flags(c = 1)
        execute(assemble("sbc r1, r2, r3, LSR #21"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sbcReg2() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("sbc r5, r2, r8, LSR #12"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sbcRegSh1() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0xFF, r5 = 0xFACA, r8 = 0x56)
        execute(assemble("sbc r5, r2, r8, LSL r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sbcRegSh2() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("sbc r5, r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun subsImmPC() {
        regs()
        flags(c = 1, n = 1)
        execute(assemble("subs pc, r2, #0x0"))
        assertMemory()
        assertRegisters()
        assertException(Unpredictable)
    }
    @Test fun subsImm() {
        regs(r2 = 0xFFFF_FFF4, r1 = 0x8)
        flags(c = 1)
        execute(assemble("subs r1, r2, #0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun subImm1() {
        regs(r2 = 0x4, r1 = 0x8)
        flags(c = 1)
        execute(assemble("sub r1, r2, #0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun subImm2() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xFFFF_FFFF)
        execute(assemble("sub r2, r1"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun subImm3() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xF0F_0F0F, r3 = 0xF0F0_F0F0)
        execute(assemble("sub r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun subImm4() {
        flags(n = 1, v = 1)
        regs(r2 = 0xF0F_0F0F, r3 = 0xF0F0_F0F0)
        execute(assemble("sub r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun subImm5() {
        flags(c = 1, n = 1, v = 1)
        regs(r2 = 0xBABA_CAFE, r3 = 0x3615_2482)
        execute(assemble("sub r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun subReg1() {
        regs(r2 = 0x4, r1 = 0x8, r3 = 0xCC, r5 = 0x4)
        flags(c = 1)
        execute(assemble("sub r1, r2, r3, LSR #21"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun subReg2() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("sub r5, r2, r8, LSR #12"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun subRegSh1() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0xFF, r5 = 0xFACA, r8 = 0x56)
        execute(assemble("sub r5, r2, r8, LSL r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun subRegSh2() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("sub r5, r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun cmnImmCZ() {
        flags(c = 1)
        regs(r2 = 0xFF_FFFF)
        execute(assemble("cmn r2, 0xFF000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmnImmN() {
        flags()
        regs(r2 = 0xFF_FFFF)
        execute(assemble("cmn r2, 0x9F000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmnImmZ() {
        flags()
        regs()
        execute(assemble("cmn r5, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmnRegVN() {
        regs(r2 = 0x7FFF_FFFF)
        flags(c = 1)
        execute(assemble("cmn r5, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmnRegSh() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("cmn r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmnImmVN() {
        flags(v = 1, n = 1)
        regs(r2 = 0xFF_FFFF)
        execute(assemble("cmn r2, 0xFF000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmnImmVZC() {
        flags(v = 1, z = 1, c = 1)
        regs(r2 = 0xFF_FFFF)
        execute(assemble("cmn r2, 0x9F000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmnRegVNC() {
        flags(n = 1, v = 1, c = 1)
        regs()
        execute(assemble("cmn r5, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmnRegCZ() {
        regs(r2 = 0x7FFF_FFFF)
        flags(c = 1, z = 1)
        execute(assemble("cmn r5, r2"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }

    @Test fun cmpImmCZ() {
        flags(c = 1)
        regs(r2 = 0xFF_FFFF)
        execute(assemble("cmp r2, 0xFF000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmpImmN() {
        flags()
        regs(r2 = 0xFF_FFFF)
        execute(assemble("cmp r2, 0x9F000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmpImmZ() {
        flags()
        regs()
        execute(assemble("cmp r5, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmpRegVN() {
        regs(r2 = 0x7FFF_FFFF)
        flags(c = 1)
        execute(assemble("cmp r5, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmpRegSh() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("cmp r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmpImmVN() {
        flags(v = 1, n = 1)
        regs(r2 = 0xFF_FFFF)
        execute(assemble("cmp r2, 0xFF000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmpImmVZC() {
        flags(v = 1, z = 1, c = 1)
        regs(r2 = 0xFF_FFFF)
        execute(assemble("cmp r2, 0x9F000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmpRegVNC() {
        flags(n = 1, v = 1, c = 1)
        regs()
        execute(assemble("cmp r5, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun cmpRegCZ() {
        regs(r2 = 0x7FFF_FFFF)
        flags(c = 1, z = 1)
        execute(assemble("cmp r5, r2"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }

    @Test fun andsImmZ() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_0000)
        execute(assemble("ands r8, r6, 0x77000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun andsImmNV() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_ACAC)
        execute(assemble("ands r8, r6, 0x87000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun andsImmC() {
        regs(r6 = 0x8800_ACAC)
        execute(assemble("ands r8, r6, 0x87000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun andImmQGE() {
        status(q = 1, ge = 3)
        regs(r6 = 0x8800_0000)
        execute(assemble("and r6, #0x77000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun andReg1() {
        regs(r6 = 0xBABA_ACAC, r8 = 0xBABA_ACAC)
        execute(assemble("and r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun andReg2() {
        regs(r6 = 0x8800_ACAC, r8 = 0x1628_9058)
        execute(assemble("and r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun andReg3() {
        regs() // clear regs
        execute(assemble("and r8, r7"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun andRegSh() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("and r0, r0, r0, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun andRegShLSR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("and r5, r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun andRegShLSL() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("and r5, r2, r8, LSL r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun andRegShASR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("and r5, r2, r8, ASR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun andRegShROR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("and r5, r2, r8, ROR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun bicsImmZ() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_0000)
        execute(assemble("bics r8, r6, 0x77000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bicsImmNV() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_ACAC)
        execute(assemble("bics r8, r6, 0x87000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bicsImmC() {
        regs(r6 = 0x8800_ACAC)
        execute(assemble("bics r8, r6, 0x87000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bicReg1() {
        regs(r6 = 0xBABA_ACAC, r8 = 0xBABA_ACAC)
        execute(assemble("bic r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bicReg2() {
        regs(r6 = 0x8800_ACAC, r8 = 0x1628_9058)
        execute(assemble("bic r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bicReg3() {
        regs() // clear regs
        execute(assemble("bic r8, r7"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bicRegSh() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("bic r0, r0, r0, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bicRegShLSR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("bic r5, r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bicRegShLSL() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("bic r5, r2, r8, LSL r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bicRegShASR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("bic r5, r2, r8, ASR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bicRegShROR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("bic r5, r2, r8, ROR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun eorsImmZ() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_0000)
        execute(assemble("eors r8, r6, 0x77000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun eorsImmNV() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_ACAC)
        execute(assemble("eors r8, r6, 0x87000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun eorsImmC() {
        regs(r6 = 0x8800_ACAC)
        execute(assemble("eors r8, r6, 0x87000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun eorReg1() {
        regs(r6 = 0xBABA_ACAC, r8 = 0xBABA_ACAC)
        execute(assemble("eor r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun eorReg2() {
        regs(r6 = 0x8800_ACAC, r8 = 0x1628_9058)
        execute(assemble("eor r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun eorReg3() {
        regs() // clear regs
        execute(assemble("eor r8, r7"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun eorRegSh() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("eor r0, r0, r0, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun eorRegShLSR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("eor r5, r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun eorRegShLSL() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("eor r5, r2, r8, LSL r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun eorRegShASR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("eor r5, r2, r8, ASR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun eorRegShROR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("eor r5, r2, r8, ROR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun orrsImmZ() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_0000)
        execute(assemble("orrs r8, r6, 0x77000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun orrsImmNV() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_ACAC)
        execute(assemble("orrs r8, r6, 0x87000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun orrsImmC() {
        regs(r6 = 0x8800_ACAC)
        execute(assemble("orrs r8, r6, 0x87000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun orrReg1() {
        regs(r6 = 0xBABA_ACAC, r8 = 0xBABA_ACAC)
        execute(assemble("orr r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun orrReg2() {
        regs(r6 = 0x8800_ACAC, r8 = 0x1628_9058)
        execute(assemble("orr r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun orrReg3() {
        regs() // clear regs
        execute(assemble("orr r8, r7"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun orrRegSh() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("orr r0, r0, r0, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun orrRegShLSR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("orr r5, r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun orrRegShLSL() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("orr r5, r2, r8, LSL r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun orrRegShASR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("orr r5, r2, r8, ASR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun orrRegShROR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("orr r5, r2, r8, ROR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun teqImmZ() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_0000)
        execute(assemble("teq r6, 0x77000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun teqImmNV() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_ACAC)
        execute(assemble("teq r6, 0x87000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun teqImmC() {
        regs(r6 = 0x8800_ACAC)
        execute(assemble("teq r6, 0x87000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun teqReg1() {
        regs(r6 = 0xBABA_ACAC, r8 = 0xBABA_ACAC)
        execute(assemble("teq r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun teqReg2() {
        regs(r6 = 0x8800_ACAC, r8 = 0x1628_9058)
        execute(assemble("teq r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun teqReg3() {
        regs() // clear regs
        execute(assemble("teq r8, r7"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun teqRegSh() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("teq r0, r0, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun teqRegShLSR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("teq r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun teqRegShLSL() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("teq r2, r8, LSL r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun teqRegShASR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("teq r2, r8, ASR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun teqRegShROR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("teq r2, r8, ROR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun tstImmZ() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_0000)
        execute(assemble("tst r6, 0x77000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun tstImmNV() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_ACAC)
        execute(assemble("tst r6, 0x87000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun tstImmC() {
        regs(r6 = 0x8800_ACAC)
        execute(assemble("tst r6, 0x87000000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun tstReg1() {
        regs(r6 = 0xBABA_ACAC, r8 = 0xBABA_ACAC)
        execute(assemble("tst r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun tstReg2() {
        regs(r6 = 0x8800_ACAC, r8 = 0x1628_9058)
        execute(assemble("tst r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun tstReg3() {
        regs() // clear regs
        execute(assemble("tst r8, r7"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun tstRegSh() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("tst r0, r0, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun tstRegShLSR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("tst r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun tstRegShLSL() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("tst r2, r8, LSL r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun tstRegShASR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("tst r2, r8, ASR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun tstRegShROR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("tst r2, r8, ROR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun movImm1() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_0000)
        execute(assemble("mov r6, 0x770"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movImm2() {
        flags()
        regs(r0 = 0x21)
        execute(assemble("mov r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun movImmZ() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_0000)
        execute(assemble("mov r6, 0x8219"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movImmNV() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_ACAC)
        execute(assemble("mov r6, 0x87"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movImmC() {
        regs(r6 = 0x8800_ACAC)
        execute(assemble("mov r6, 0xC87"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movReg1() {
        regs(r6 = 0xBABA_ACAC, r8 = 0xBABA_ACAC)
        execute(assemble("mov r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movReg2() {
        regs(r6 = 0x8800_ACAC, r8 = 0x1628_9058)
        execute(assemble("mov r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movReg3() {
        regs() // clear regs
        execute(assemble("mov r8, r7"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movRegSh() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("mov r0, r0, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movRegShLSR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("mov r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movRegShLSL() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("mov r2, r8, LSL r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movRegShASR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("mov r2, r8, ASR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movRegShROR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("mov r2, r8, ROR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movtImmZ() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_0000)
        execute(assemble("movt r6, 0x8219"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movtImm() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_0000)
        execute(assemble("movt r6, 0x770"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movtImmNV() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_ACAC)
        execute(assemble("movt r6, 0x87"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun movtImmC() {
        regs(r6 = 0x8800_ACAC)
        execute(assemble("movt r6, 0xC87"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun mvnImm1() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_0000)
        execute(assemble("mvn r6, 0x770"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mvnImm2() {
        flags()
        regs(r0 = 0x21)
        execute(assemble("mvn r0, #0x37"))
        assertRegisters()
        assertFlags()
    }
    @Test fun mvnImmZ() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_0000)
        execute(assemble("mvns r6, 0xFF0000"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mvnImmNV() {
        flags(c = 1, n = 1, v = 1)
        regs(r6 = 0x8800_ACAC)
        execute(assemble("mvns r6, 0x87"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mvnImm3() {
        regs(r6 = 0x8800_ACAC)
        execute(assemble("mvn r6, 0xC800"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mvnReg1() {
        regs(r6 = 0xBABA_ACAC, r8 = 0xBABA_ACAC)
        execute(assemble("mvn r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mvnReg2() {
        regs(r6 = 0x8800_ACAC, r8 = 0x1628_9058)
        execute(assemble("mvn r8, r6"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mvnReg3() {
        regs() // clear regs
        execute(assemble("mvn r8, r7"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mvnRegSh() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("mvn r0, r0, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mvnRegShLSR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("mvn r2, r8, LSR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mvnRegShLSL() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("mvn r2, r8, LSL r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mvnRegShASR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("mvn r2, r8, ASR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mvnRegShROR() {
        flags(n = 1, v = 1)
        regs(r2 = 0x15, r3 = 0x1C, r5 = 0x111F_FACA, r8 = 0x56)
        execute(assemble("mvn r2, r8, ROR r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun mlasN() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("mlas r0, r6, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mlasZ() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("mlas r0, r6, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mla1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("mla r0, r6, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mla2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("mla r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mls1() {
        regs(r6 = 0xCACA, r2 = 2, r3 = 0x3FFF_FFFF, r0 = 0xBABA_CAFE)
        execute(assemble("mls r0, r6, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mls2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("mls r6, r3, r2, r0"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mls3() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("mls r0, r6, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mls4() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("mls r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mulsN() {
        flags(c = 1, v = 1)
        regs(r6 = 0x4000_0000, r2 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("muls r0, r6, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mulsZ() {
        flags(c = 1, v = 1)
        regs(r6 = 0, r2 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("muls r0, r6, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mul1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("mul r0, r6, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun mul2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("mul r6, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun mul3() {
        flags(c = 1, v = 1)
        regs(r3 = 0x7FFF_FFFF, r2 = 2)
        execute(assemble("mul r3, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun smlalsN() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 0xFFFF_FFFF, r3 = 1, r0 = 1)
        execute(assemble("smlals r2, r6, r0, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlalsZ() {
        flags(c = 1, v = 1)
        regs(r6 = 0xFFFF_FFFF, r2 = 0xFFFF_FFFF, r3 = 1, r0 = 1)
        execute(assemble("smlals r2, r6, r0, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlal1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smlal r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlal2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smlal r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlaltt1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smlaltt r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlaltt2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smlaltt r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlaltb1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smlaltb r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlaltb2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smlaltb r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlalbt1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smlalbt r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlalbt2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smlalbt r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlalbb1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smlalbb r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlalbb2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smlalbb r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smullsN() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 0xFFFF_FFFF, r3 = 0xFFFF_FFFF, r0 = 0xFFFF_FFFF)
        execute(assemble("smulls r2, r6, r0, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smullsZ() {
        flags(c = 1, v = 1)
        regs(r6 = 0, r2 = 0, r3 = 0, r0 = 0)
        execute(assemble("smulls r2, r6, r0, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smull1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smull r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smull2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smull r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlawt1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smlawt r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlawt2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smlawt r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlawb1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smlawb r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlawb2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smlawb r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlatt1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smlatt r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlatt2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smlatt r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlatb1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smlatb r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlatb2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smlatb r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlabt1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smlabt r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlabt2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smlabt r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlabb1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smlabb r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smlabb2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smlabb r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smulwt1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smulwt r6, r0, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smulwt2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smulwt r6, r3, r0"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smulwb1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smulwb r6, r0, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smulwb2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smulwb r6, r3, r0"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smultt1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smultt r6, r0, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smultt2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smultt r6, r3, r0"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smultb1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smultb r6, r0, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smultb2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smultb r6, r3, r0"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smulbt1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smulbt r6, r0, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smulbt2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smulbt r6, r3, r0"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smulbb1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("smulbb r6, r0, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun smulbb2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("smulbb r6, r3, r0"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun umlalsN() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 0xFFFF_FFFF, r3 = 0xFFFF_FFFF, r0 = 0xFFFF_FFFF)
        execute(assemble("umlals r2, r6, r0, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun umlalsZ() {
        flags(c = 1, v = 1)
        regs(r6 = 0, r2 = 0, r3 = 0, r0 = 0)
        execute(assemble("umlals r2, r6, r0, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun umlal1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("umlal r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun umlal2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("umlal r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun umaal1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("umaal r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun umaal2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("umaal r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun umullsN() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 0xFFFF_FFFF, r3 = 0xFFFF_FFFF, r0 = 0xFFFF_FFFF)
        execute(assemble("umulls r2, r6, r0, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun umullsZ() {
        flags(c = 1, v = 1)
        regs(r6 = 0, r2 = 0, r3 = 0, r0 = 0)
        execute(assemble("umulls r2, r6, r0, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun umull1() {
        flags(c = 1, v = 1)
        regs(r6 = 0x3FFF_FFFF, r2 = 0xFAFA, r3 = 0xBACA, r0 = 0xBABA_CAFE)
        execute(assemble("umull r6, r0, r2, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun umull2() {
        flags(c = 1, v = 1)
        regs(r6 = 0x7FFF_FFFF, r2 = 2, r3 = 2, r0 = 0xBABA_CAFE)
        execute(assemble("umull r6, r3, r0, r2"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun bfc1(){
        regs(r3 = 0xAAAA_AAAA)
        execute(assemble("bfc r3, #15, #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bfc2(){
        regs(r8 = 0xDA25_78F6)
        execute(assemble("bfc r8, #13, #15"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bfc3(){
        regs(r3 = 0xBABA_CAFE)
        execute(assemble("bfc r3, #15, #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bfc4(){
        regs(r8 = 0x567A_9827)
        execute(assemble("bfc r8, #13, #15"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bfi1(){
        regs(r3 = 0xAAAA_AAAA)
        execute(assemble("bfi r1, r3, #15, #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bfi2(){
        regs(r8 = 0xDA25_78F6)
        execute(assemble("bfi r1, r8, #13, #15"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bfi3(){
        regs(r3 = 0xBABA_CAFE)
        execute(assemble("bfi r1, r3, #15, #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun bfi4(){
        regs(r8 = 0x567A_9827)
        execute(assemble("bfi r1, r8, #13, #15"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sbfx1(){
        regs(r3 = 0xAAAA_AAAA)
        execute(assemble("sbfx r1, r3, #15, #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sbfx2(){
        regs(r8 = 0xDA25_78F6)
        execute(assemble("sbfx r1, r8, #13, #15"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sbfx3(){
        regs(r3 = 0xBABA_CAFE)
        execute(assemble("sbfx r1, r3, #15, #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sbfx4(){
        regs(r8 = 0x567A_9827)
        execute(assemble("sbfx r1, r8, #13, #15"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun ubfx1(){
        regs(r3 = 0xAAAA_AAAA)
        execute(assemble("ubfx r1, r3, #15, #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun ubfx2(){
        regs(r8 = 0xDA25_78F6)
        execute(assemble("ubfx r1, r8, #13, #15"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun ubfx3(){
        regs(r3 = 0xBABA_CAFE)
        execute(assemble("ubfx r1, r3, #15, #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun ubfx4(){
        regs(r8 = 0x567A_9827)
        execute(assemble("ubfx r1, r8, #13, #15"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun rbit1(){
        regs(r3 = 0xAAAA_AAAA)
        execute(assemble("rbit r1, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rbit2(){
        regs(r8 = 0x5555_5555)
        execute(assemble("rbit r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rbit3(){
        regs(r3 = 0xBABA_CAFE)
        execute(assemble("rbit r1, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rbit4(){
        regs(r8 = 0x567A_9827)
        execute(assemble("rbit r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rbit5(){
        regs(r8 = 0xDA25_78F6)
        execute(assemble("rbit r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rbit6(){
        regs() // clear regs
        execute(assemble("rbit r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rbit7(){
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("rbit r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rev1(){
        regs(r3 = 0xAAAA_AAAA)
        execute(assemble("rev r1, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rev2(){
        regs(r8 = 0x5555_5555)
        execute(assemble("rev r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rev3(){
        regs(r3 = 0xBABA_CAFE)
        execute(assemble("rev r1, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rev4(){
        regs(r8 = 0x567A_9827)
        execute(assemble("rev r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rev5(){
        regs(r8 = 0xDA25_78F6)
        execute(assemble("rev r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rev6(){
        regs() // clear regs
        execute(assemble("rev r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rev7(){
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("rev r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rev161(){
        regs(r3 = 0xAAAA_AAAA)
        execute(assemble("rev16 r1, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rev162(){
        regs(r8 = 0x5555_5555)
        execute(assemble("rev16 r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rev163(){
        regs(r3 = 0xBABA_CAFE)
        execute(assemble("rev16 r1, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rev164(){
        regs(r8 = 0x567A_9827)
        execute(assemble("rev16 r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rev165(){
        regs(r8 = 0xDA25_78F6)
        execute(assemble("rev16 r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rev166(){
        regs() // clear regs
        execute(assemble("rev16 r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun rev167(){
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("rev16 r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun revsh1(){
        regs(r3 = 0xAAAA_AAAA)
        execute(assemble("revsh r1, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun revsh2(){
        regs(r8 = 0x5555_5555)
        execute(assemble("revsh r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun revsh3(){
        regs(r3 = 0xBABA_CAFE)
        execute(assemble("revsh r1, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun revsh4(){
        regs(r8 = 0x567A_9827)
        execute(assemble("revsh r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun revsh5(){
        regs(r8 = 0xDA25_78F6)
        execute(assemble("revsh r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun revsh6(){
        regs() // clear regs
        execute(assemble("revsh r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun revsh7(){
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("revsh r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sel1(){
        status(ge = 2)
        regs(r3 = 0xAAAA_AAAA, r5 = 0xB67C_A90C)
        execute(assemble("sel r1, r3, r5"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sel2(){
        status(ge = 1)
        regs(r8 = 0x5555_5555, r5 = 0xB67C_A90C)
        execute(assemble("sel r1, r8, r5"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sel3(){
        regs(r3 = 0xBABA_CAFE, r8 = 0xCAFE_BABA)
        execute(assemble("sel r1, r3, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sel4(){
        status(ge = 8)
        regs(r8 = 0x567A_9827, r0 = 0x5858_CCCC)
        execute(assemble("sel r1, r8, r0"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sel5(){
        status(ge = 14)
        regs(r8 = 0xDA25_78F6)
        execute(assemble("sel r0, r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sel6(){
        execute(assemble("sel lr, r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sel7(){
        status(ge = 10)
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("sel r1, r1, r8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun uxtab1(){
        regs(r3 = 0xAAAA_AAAA, r5 = 0xB67C_A90C)
        execute(assemble("uxtab r1, r3, r5"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtab2(){
        regs(r8 = 0x5555_5555, r5 = 0xB67C_A90C)
        execute(assemble("uxtab r1, r8, r5, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtab3(){
        regs(r3 = 0xBABA_CAFE, r8 = 0xCAFE_BABA)
        execute(assemble("uxtab r1, r3, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtab4(){
        regs(r8 = 0x567A_9827, r0 = 0x5858_CCCC)
        execute(assemble("uxtab r1, r8, r0, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtab5(){
        status(ge = 14)
        regs(r8 = 0xDA25_78F6)
        execute(assemble("uxtab r0, r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtab6(){
        execute(assemble("uxtab lr, r1, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtab7(){
        status(ge = 10)
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("uxtab r1, r1, r8, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtab161(){
        regs(r3 = 0xAAAA_AAAA, r5 = 0xB67C_A90C)
        execute(assemble("uxtab16 r1, r3, r5"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtab162(){
        regs(r8 = 0x5555_5555, r5 = 0xB67C_A90C)
        execute(assemble("uxtab16 r1, r8, r5, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtab163(){
        regs(r3 = 0xBABA_CAFE, r8 = 0xCAFE_BABA)
        execute(assemble("uxtab16 r1, r3, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtab164(){
        regs(r8 = 0x567A_9827, r0 = 0x5858_CCCC)
        execute(assemble("uxtab16 r1, r8, r0, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtab165(){
        status(ge = 14)
        regs(r8 = 0xDA25_78F6)
        execute(assemble("uxtab16 r0, r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtab166(){
        execute(assemble("uxtab16 lr, r1, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtab167(){
        status(ge = 10)
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("uxtab16 r1, r1, r8, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtah1(){
        regs(r3 = 0xAAAA_AAAA, r5 = 0xB67C_A90C)
        execute(assemble("uxtah r1, r3, r5"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtah2(){
        regs(r8 = 0x5555_5555, r5 = 0xB67C_A90C)
        execute(assemble("uxtah r1, r8, r5, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtah3(){
        regs(r3 = 0xBABA_CAFE, r8 = 0xCAFE_BABA)
        execute(assemble("uxtah r1, r3, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtah4(){
        regs(r8 = 0x567A_9827, r0 = 0x5858_CCCC)
        execute(assemble("uxtah r1, r8, r0, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtah5(){
        status(ge = 14)
        regs(r8 = 0xDA25_78F6)
        execute(assemble("uxtah r0, r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtah6(){
        execute(assemble("uxtah lr, r1, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtah7(){
        status(ge = 10)
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("uxtah r1, r1, r8, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtb1(){
        regs(r3 = 0xAAAA_AAAA, r5 = 0xB67C_A90C)
        execute(assemble("uxtb r1, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtb2(){
        regs(r8 = 0x5555_5555, r5 = 0xB67C_A90C)
        execute(assemble("uxtb r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtb3(){
        regs(r3 = 0xBABA_CAFE, r8 = 0xCAFE_BABA)
        execute(assemble("uxtb r8, r3, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtb4(){
        regs(r8 = 0x567A_9827, r0 = 0x5858_CCCC)
        execute(assemble("uxtb r8, r0, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtb5(){
        status(ge = 14)
        regs(r8 = 0xDA25_78F6)
        execute(assemble("uxtb r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtb6(){
        regs() // clear regs
        execute(assemble("uxtb r1, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtb7(){
        status(ge = 10)
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("uxtb r1, r8, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtb161(){
        regs(r3 = 0xAAAA_AAAA, r5 = 0xB67C_A90C)
        execute(assemble("uxtb16 r1, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtb162(){
        regs(r8 = 0x5555_5555, r5 = 0xB67C_A90C)
        execute(assemble("uxtb16 r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtb163(){
        regs(r3 = 0xBABA_CAFE, r8 = 0xCAFE_BABA)
        execute(assemble("uxtb16 r8, r3, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtb164(){
        regs(r8 = 0x567A_9827, r0 = 0x5858_CCCC)
        execute(assemble("uxtb16 r8, r0, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtb165(){
        status(ge = 14)
        regs(r8 = 0xDA25_78F6)
        execute(assemble("uxtb16 r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtb166(){
        regs() // clear regs
        execute(assemble("uxtb16 r1, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxtb167(){
        status(ge = 10)
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("uxtb16 r1, r8, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxth1(){
        regs(r3 = 0xAAAA_AAAA, r5 = 0xB67C_A90C)
        execute(assemble("uxth r1, r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxth2(){
        regs(r8 = 0x5555_5555, r5 = 0xB67C_A90C)
        execute(assemble("uxth r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxth3(){
        regs(r3 = 0xBABA_CAFE, r8 = 0xCAFE_BABA)
        execute(assemble("uxth r8, r3, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxth4(){
        regs(r8 = 0x567A_9827, r0 = 0x5858_CCCC)
        execute(assemble("uxth r8, r0, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxth5(){
        status(ge = 14)
        regs(r8 = 0xDA25_78F6)
        execute(assemble("uxth r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxth6(){
        regs() // clear regs
        execute(assemble("uxth r1, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun uxth7(){
        status(ge = 10)
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("uxth r1, r8, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun sxtab1(){
        regs(r3 = 0xAAAA_AAAA, r5 = 0xB67C_A90C)
        execute(assemble("sxtab r1, r3, r5"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtab2(){
        regs(r8 = 0x5555_5555, r5 = 0xB67C_A90C)
        execute(assemble("sxtab r1, r8, r5, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtab3(){
        regs(r3 = 0xBABA_CAFE, r8 = 0xCAFE_BABA)
        execute(assemble("sxtab r1, r3, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtab4(){
        regs(r8 = 0x567A_9827, r0 = 0x5858_CCCC)
        execute(assemble("sxtab r1, r8, r0, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtab5(){
        status(ge = 14)
        regs(r8 = 0xDA25_78F6)
        execute(assemble("sxtab r0, r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtab6(){
        execute(assemble("sxtab lr, r1, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtab7(){
        status(ge = 10)
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("sxtab r1, r1, r8, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtab161(){
        regs(r3 = 0xAAAA_AAAA, r5 = 0xB67C_A90C)
        execute(assemble("sxtab16 r1, r3, r5"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtab162(){
        regs(r8 = 0x5555_5555, r5 = 0xB67C_A90C)
        execute(assemble("sxtab16 r1, r8, r5, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtab163(){
        regs(r3 = 0xBABA_CAFE, r8 = 0xCAFE_BABA)
        execute(assemble("sxtab16 r1, r3, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtab164(){
        regs(r8 = 0x567A_9827, r0 = 0x5858_CCCC)
        execute(assemble("sxtab16 r1, r8, r0, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtab165(){
        status(ge = 14)
        regs(r8 = 0xDA25_78F6)
        execute(assemble("sxtab16 r0, r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtab166(){
        execute(assemble("sxtab16 lr, r1, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtab167(){
        status(ge = 10)
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("sxtab16 r1, r1, r8, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtah1(){
        regs(r3 = 0xAAAA_AAAA, r5 = 0xB67C_A90C)
        execute(assemble("sxtah r1, r3, r5"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtah2(){
        regs(r8 = 0x5555_5555, r5 = 0xB67C_A90C)
        execute(assemble("sxtah r1, r8, r5, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtah3(){
        regs(r3 = 0xBABA_CAFE, r8 = 0xCAFE_BABA)
        execute(assemble("sxtah r1, r3, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtah4(){
        regs(r8 = 0x567A_9827, r0 = 0x5858_CCCC)
        execute(assemble("sxtah r1, r8, r0, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtah5(){
        status(ge = 14)
        regs(r8 = 0xDA25_78F6)
        execute(assemble("sxtah r0, r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtah6(){
        execute(assemble("sxtah lr, r1, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtah7(){
        status(ge = 10)
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("sxtah r1, r1, r8, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtb1(){
        regs(r3 = 0xAAAA_AAAA, r5 = 0xB67C_A90C)
        execute(assemble("sxtb r3, r5"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtb2(){
        regs(r8 = 0x5555_5555, r5 = 0xB67C_A90C)
        execute(assemble("sxtb r8, r5, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtb3(){
        regs(r3 = 0xBABA_CAFE, r8 = 0xCAFE_BABA)
        execute(assemble("sxtb r3, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtb4(){
        regs(r8 = 0x567A_9827, r0 = 0x5858_CCCC)
        execute(assemble("sxtb r8, r0, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtb5(){
        status(ge = 14)
        regs(r8 = 0xDA25_78F6)
        execute(assemble("sxtb r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtb6(){
        regs() // clear regs
        execute(assemble("sxtb r1, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtb7(){
        status(ge = 10)
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("sxtb r1, r8, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxth1(){
        regs(r3 = 0xAAAA_AAAA, r5 = 0xB67C_A90C)
        execute(assemble("sxth r3, r5"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxth2(){
        regs(r8 = 0x5555_5555, r5 = 0xB67C_A90C)
        execute(assemble("sxth r8, r5, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxth3(){
        regs(r3 = 0xBABA_CAFE, r8 = 0xCAFE_BABA)
        execute(assemble("sxth r3, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxth4(){
        regs(r8 = 0x567A_9827, r0 = 0x5858_CCCC)
        execute(assemble("sxth r8, r0, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxth5(){
        status(ge = 14)
        regs(r8 = 0xDA25_78F6)
        execute(assemble("sxth r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxth6(){
        regs() // clear regs
        execute(assemble("sxth r1, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxth7(){
        status(ge = 10)
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("sxth r1, r8, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtb161(){
        regs(r3 = 0xAAAA_AAAA, r5 = 0xB67C_A90C)
        execute(assemble("sxtb16 r3, r5"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtb162(){
        regs(r8 = 0x5555_5555, r5 = 0xB67C_A90C)
        execute(assemble("sxtb16 r8, r5, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtb163(){
        regs(r3 = 0xBABA_CAFE, r8 = 0xCAFE_BABA)
        execute(assemble("sxtb16 r3, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtb164(){
        regs(r8 = 0x567A_9827, r0 = 0x5858_CCCC)
        execute(assemble("sxtb16 r8, r0, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtb165(){
        status(ge = 14)
        regs(r8 = 0xDA25_78F6)
        execute(assemble("sxtb16 r1, r8, ROR #8"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtb166(){
        regs() // clear regs
        execute(assemble("sxtb16 r1, r8, ROR #16"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun sxtb167(){
        status(ge = 10)
        regs(r8 = 0xFFFF_FFFF)
        execute(assemble("sxtb16 r1, r8, ROR #24"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun strdImmOffset1() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000)
        execute(assemble("strd r0, r1, [r2, #0x10]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdImmPrei1() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000)
        execute(assemble("strd r0, r1, [r2, #0x10]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdImmPosti1() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000)
        execute(assemble("strd r0, r1, [r2], #0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdImmOffset2() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000)
        execute(assemble("strd r0, r1, [r2, #-0x67]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdImmPrei2() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000)
        execute(assemble("strd r0, r1, [r2, #-0xF9]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdImmPosti2() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000)
        execute(assemble("strd r0, r1, [r2], #-0xF3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdImmOffset3() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x15000)
        execute(assemble("strd r0, r1, [r2, #-0x97]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdImmPrei3() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x256)
        execute(assemble("strd r0, r1, [r2, #0x10]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdImmPosti3() {
        regs(r0 = 0x1234_5678, r2 = 0x1000)
        execute(assemble("strd r0, r1, [r2], #-0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdImmOffset4() {
        regs() // clear regs
        execute(assemble("strd r0, r1, [r2, #0]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdImmPrei4() {
        regs() // clear regs
        execute(assemble("strd r0, r1, [r2]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdImmPosti4() {
        regs() // clear regs
        execute(assemble("strd r0, r1, [r2]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdRegOffset1() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000, r3 = 0x500)
        execute(assemble("strd r0, r1, [r2, r3]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdRegPrei1() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000, r3 = 0x500)
        execute(assemble("strd r0, r1, [r2, r3]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdRegPosti1() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000, r3 = 0x500)
        execute(assemble("strd r0, r1, [r2], r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdRegOffset2() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000, r3 = 0x500)
        execute(assemble("strd r0, r1, [r2, -r3]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdRegPrei2() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000, r3 = 0x500)
        execute(assemble("strd r0, r1, [r2, -r3]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdRegPosti2() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000, r3 = 0x500)
        execute(assemble("strd r0, r1, [r2], -r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdRegOffset3() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x15000, r3 = 0x256)
        execute(assemble("strd r0, r1, [r2, -r3]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdRegPrei3() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x256, r3 = 0x10000)
        execute(assemble("strd r0, r1, [r2, r3]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdRegPosti3() {
        regs(r0 = 0x1234_5678, r2 = 0x1000, r3 = 0x10)
        execute(assemble("strd r0, r1, [r2], -r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdRegOffset4() {
        regs() // clear regs
        execute(assemble("strd r0, r1, [r2, r3]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdRegPrei4() {
        regs() // clear regs
        execute(assemble("strd r0, r1, [r2, r3]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strdRegPosti4() {
        regs() // clear regs
        execute(assemble("strd r0, r1, [r2], r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhImmOffset1() {
        regs(r0 = 0x1234_5678, r2 = 0x1000)
        execute(assemble("strh r0, [r2, #0x10]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhImmPrei1() {
        regs(r1 = 0x9AB_CDEF, r2 = 0x1000)
        execute(assemble("strh r1, [r2, #0x10]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhImmPosti1() {
        regs(r0 = 0x1234_5678, r2 = 0x1000)
        execute(assemble("strh r0, [r2], #0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhImmOffset2() {
        regs(r1 = 0x9AB_CDEF, r2 = 0x1000)
        execute(assemble("strh r1, [r2, #-0x67]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhImmPrei2() {
        regs(r0 = 0x1234_5678, r2 = 0x1000)
        execute(assemble("strh r0, [r2, #-0xF9]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhImmPosti2() {
        regs( r1 = 0x9AB_CDEF, r2 = 0x1000)
        execute(assemble("strh r1, [r2], #-0xF3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhImmOffset3() {
        regs(r0 = 0x1234_5678, r2 = 0x15000)
        execute(assemble("strh r0, [r2, #-0x97]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhImmPrei3() {
        regs(r0 = 0x1234_5678, r2 = 0x256)
        execute(assemble("strh r0, [r2, #0x10]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhImmPosti3() {
        regs(r2 = 0x1000)
        execute(assemble("strh r1, [r2], #-0x10"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhImmOffset4() {
        regs() // clear regs
        execute(assemble("strh r1, [r2, #0]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhImmPrei4() {
        regs() // clear regs
        execute(assemble("strh r0, [r2]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhImmPosti4() {
        regs() // clear regs
        execute(assemble("strh r0, [r2]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhRegOffset1() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000, r3 = 0x500)
        execute(assemble("strh r0, [r2, r3]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhRegPrei1() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000, r3 = 0x500)
        execute(assemble("strh r1, [r2, r3]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhRegPosti1() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000, r3 = 0x500)
        execute(assemble("strh r1, [r2], r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhRegOffset2() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000, r3 = 0x500)
        execute(assemble("strh r0, [r2, -r3]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhRegPrei2() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000, r3 = 0x500)
        execute(assemble("strh r1, [r2, -r3]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhRegPosti2() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x1000, r3 = 0x500)
        execute(assemble("strh r0, [r2], -r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhRegOffset3() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x15000, r3 = 0x256)
        execute(assemble("strh r1, [r2, -r3]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhRegPrei3() {
        regs(r0 = 0x1234_5678, r1 = 0x9AB_CDEF, r2 = 0x256, r3 = 0x10000)
        execute(assemble("strh r0, [r2, r3]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhRegPosti3() {
        regs(r0 = 0x1234_5678, r2 = 0x1000, r3 = 0x10)
        execute(assemble("strh r1, [r2], -r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhRegOffset4() {
        regs() // clear regs
        execute(assemble("strh r1, [r2, r3]"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhRegPrei4() {
        regs() // clear regs
        execute(assemble("strh r0, [r2, r3]!"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }
    @Test fun strhRegPosti4() {
        regs() // clear regs
        execute(assemble("strh r1, [r2], r3"))
        assertRegisters()
        assertFlags()
        assertMemory()
    }

    @Test fun ldrImmOffset1() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0, #0x200]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrImmOffset2() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0xE00, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0, #-0x200]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrImmOffset3() {
        regs(r0 = 0xF000, r1 = 0xFFFF_FFFF)
        execute(assemble("ldr r1, [r0, #-0x200]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrImmOffset4() {
        regs() // clear regs
        execute(assemble("ldr r1, [r0, #0x0]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrImmOffset5() {
        regs(r0 = 0x17F01, r1 = 0xFACA_FACA)
        store(0x18000, 0x1234_5678, DWORD)
        execute(assemble("ldr r1, [r0, #0xFF]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrImmPrei1() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0, #0x200]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrImmPrei2() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0xE00, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0, #-0x200]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrImmPrei3() {
        regs(r0 = 0xF000, r1 = 0xFFFF_FFFF)
        execute(assemble("ldr r1, [r0, #-0x200]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrImmPrei4() {
        regs() // clear regs
        execute(assemble("ldr r1, [r0, #0x0]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrImmPrei5() {
        regs(r0 = 0x17F01, r1 = 0xFACA_FACA)
        store(0x18000, 0x1234_5678, DWORD)
        execute(assemble("ldr r1, [r0, #0xFF]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrImmPosti1() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0], #0x200"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrImmPosti2() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0xE00, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0], #-0x200"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrImmPosti3() {
        regs(r0 = 0xF000, r1 = 0xFFFF_FFFF)
        execute(assemble("ldr r1, [r0], #-0x200"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrImmPosti4() {
        regs() // clear regs
        execute(assemble("ldr r1, [r0], #0x0"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrImmPosti5() {
        regs(r0 = 0x17F01, r1 = 0xFACA_FACA)
        store(0x18000, 0x1234_5678, DWORD)
        execute(assemble("ldr r1, [r0], #0xFF"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegOffset1() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF, r2 = 0x200)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0, r2]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegOffset2() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF, r2 = 0x200)
        store(0xE00, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0, -r2]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegOffset3() {
        regs(r0 = 0xF000, r1 = 0xFFFF_FFFF, r2 = 0x20)
        store(0xF200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0, r2, LSL #0x8]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegOffset4() {
        regs() // clear regs
        execute(assemble("ldr r1, [r0, r0]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegOffset5() {
        regs(r0 = 0x17F00, r1 = 0xFACA_FACA, r2 = 0x1)
        store(0x18000, 0x1234_5678, DWORD)
        execute(assemble("ldr r1, [r0, r2, LSL #0x8]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegPrei1() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF, r2 = 0x200)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0, r2]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegPrei2() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF, r2 = 0x200)
        store(0xE00, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0, -r2]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegPrei3() {
        regs(r0 = 0xF000, r1 = 0xFFFF_FFFF, r2 = 0x20)
        store(0xF200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0, r2, LSL #0x8]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegPrei4() {
        regs() // clear regs
        execute(assemble("ldr r1, [r0, r0]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegPrei5() {
        regs(r0 = 0x17F00, r1 = 0xFACA_FACA, r2 = 0x1)
        store(0x18000, 0x1234_5678, DWORD)
        execute(assemble("ldr r1, [r0, r2, LSL #0x8]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegPosti1() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF, r2 = 0x200)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0], r2"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegPosti2() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF, r2 = 0x200)
        store(0xE00, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0], -r2"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegPosti3() {
        regs(r0 = 0xF000, r1 = 0xFFFF_FFFF, r2 = 0x20)
        store(0xF200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [r0], r2, LSL #0x8"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegPosti4() {
        regs() // clear regs
        execute(assemble("ldr r1, [r0], r0"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrRegPosti5() {
        regs(r0 = 0x17F00, r1 = 0xFACA_FACA, r2 = 0x1)
        store(0x18000, 0x1234_5678, DWORD)
        execute(assemble("ldr r1, [r0], r2, LSL #0x8"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrLit() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldr r1, [pc, #-0]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }  // BUGS HERE!!!!

    @Test fun ldrbImmOffset1() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0, #0x200]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbImmOffset2() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0xE00, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0, #-0x200]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbImmOffset3() {
        regs(r0 = 0xF000, r1 = 0xFFFF_FFFF)
        execute(assemble("ldrb r1, [r0, #-0x200]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbImmOffset4() {
        regs() // clear regs
        execute(assemble("ldrb r1, [r0, #0x0]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbImmOffset5() {
        regs(r0 = 0x17F01, r1 = 0xFACA_FACA)
        store(0x18000, 0x1234_5678, DWORD)
        execute(assemble("ldrb r1, [r0, #0xFF]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbImmPrei1() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0, #0x200]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbImmPrei2() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0xE00, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0, #-0x200]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbImmPrei3() {
        regs(r0 = 0xF000, r1 = 0xFFFF_FFFF)
        execute(assemble("ldrb r1, [r0, #-0x200]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbImmPrei4() {
        regs() // clear regs
        execute(assemble("ldrb r1, [r0, #0x0]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbImmPrei5() {
        regs(r0 = 0x17F01, r1 = 0xFACA_FACA)
        store(0x18000, 0x1234_5678, DWORD)
        execute(assemble("ldrb r1, [r0, #0xFF]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbImmPosti1() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0], #0x200"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbImmPosti2() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0xE00, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0], #-0x200"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbImmPosti3() {
        regs(r0 = 0xF000, r1 = 0xFFFF_FFFF)
        execute(assemble("ldrb r1, [r0], #-0x200"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbImmPosti4() {
        regs() // clear regs
        execute(assemble("ldrb r1, [r0], #0x0"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbImmPosti5() {
        regs(r0 = 0x17F01, r1 = 0xFACA_FACA)
        store(0x18000, 0x1234_5678, DWORD)
        execute(assemble("ldrb r1, [r0], #0xFF"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegOffset1() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF, r2 = 0x200)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0, r2]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegOffset2() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF, r2 = 0x200)
        store(0xE00, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0, -r2]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegOffset3() {
        regs(r0 = 0xF000, r1 = 0xFFFF_FFFF, r2 = 0x20)
        store(0xF200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0, r2, LSL #0x8]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegOffset4() {
        regs() // clear regs
        execute(assemble("ldrb r1, [r0, r0]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegOffset5() {
        regs(r0 = 0x17F00, r1 = 0xFACA_FACA, r2 = 0x1)
        store(0x18000, 0x1234_5678, DWORD)
        execute(assemble("ldrb r1, [r0, r2, LSL #0x8]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegPrei1() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF, r2 = 0x200)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0, r2]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegPrei2() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF, r2 = 0x200)
        store(0xE00, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0, -r2]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegPrei3() {
        regs(r0 = 0xF000, r1 = 0xFFFF_FFFF, r2 = 0x20)
        store(0xF200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0, r2, LSL #0x8]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegPrei4() {
        regs() // clear regs
        execute(assemble("ldrb r1, [r0, r0]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegPrei5() {
        regs(r0 = 0x17F00, r1 = 0xFACA_FACA, r2 = 0x1)
        store(0x18000, 0x1234_5678, DWORD)
        execute(assemble("ldrb r1, [r0, r2, LSL #0x8]!"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegPosti1() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF, r2 = 0x200)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0], r2"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegPosti2() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF, r2 = 0x200)
        store(0xE00, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0], -r2"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegPosti3() {
        regs(r0 = 0xF000, r1 = 0xFFFF_FFFF, r2 = 0x20)
        store(0xF200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [r0], r2, LSL #0x8"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegPosti4() {
        regs() // clear regs
        execute(assemble("ldrb r1, [r0], r0"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbRegPosti5() {
        regs(r0 = 0x17F00, r1 = 0xFACA_FACA, r2 = 0x1)
        store(0x18000, 0x1234_5678, DWORD)
        execute(assemble("ldrb r1, [r0], r2, LSL #0x8"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrbLit() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrb r1, [pc, #-0]"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }  // BUGS HERE!!!!

    @Test fun ldrtImm1() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrt r1, [r0], #0x200"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrtImm2() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF)
        store(0xE00, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrt r1, [r0], #-0x200"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrtImm3() {
        regs(r0 = 0xF000, r1 = 0xFFFF_FFFF)
        execute(assemble("ldrt r1, [r0], #-0x200"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrtImm4() {
        regs() // clear regs
        execute(assemble("ldrt r1, [r0], #0x0"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrtImm5() {
        regs(r0 = 0x17F01, r1 = 0xFACA_FACA)
        store(0x18000, 0x1234_5678, DWORD)
        execute(assemble("ldrt r1, [r0], #0xFF"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrtReg1() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF, r2 = 0x200)
        store(0x1200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrt r1, [r0], r2"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrtReg2() {
        regs(r0 = 0x1000, r1 = 0xFFFF_FFFF, r2 = 0x2)
        store(0xE00, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrt r1, [r0], -r2, LSL #8"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrtReg3() {
        regs(r0 = 0xF000, r1 = 0xFFFF_FFFF, r2 = 0x20)
        store(0xF200, 0xBABA_CAFE, DWORD)
        execute(assemble("ldrt r1, [r0], r2, LSL #0x8"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrtReg4() {
        regs() // clear regs
        execute(assemble("ldrt r1, [r0], r0"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
    @Test fun ldrtReg5() {
        regs(r0 = 0x17F00, r1 = 0xFACA_FACA, r2 = 0x1)
        store(0x18000, 0x1234_5678, DWORD)
        execute(assemble("ldrt r1, [r0], r2, LSL #0x8"))
        assertRegisters()
        assertMemory()
        assertFlags()
    }
}
