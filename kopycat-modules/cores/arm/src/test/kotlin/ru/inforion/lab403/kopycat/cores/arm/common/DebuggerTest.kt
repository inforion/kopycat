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
package ru.inforion.lab403.kopycat.cores.arm.common

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.BreakpointType
import ru.inforion.lab403.kopycat.cores.base.enums.BreakpointType.*
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.cores.ARMv7Core
import ru.inforion.lab403.kopycat.modules.cores.ARMDebugger
import ru.inforion.lab403.kopycat.modules.memory.RAM
import ru.inforion.lab403.kopycat.interfaces.*


class DebuggerTest: Module(null, "ARM Debugger Test") {
    inner class Buses: ModuleBuses(this) { val mem = Bus("mem") }
    override val buses = Buses()
    private val arm = ARMv7Core(this, "arm", 48.MHz, 1.0)
    private val ram1 = RAM(this, "ram1", 0x1000, Resource("binaries/strcpy.bin"))
    private val boot = RAM(this, "boot", 0x1000)
    private val dbg = ARMDebugger(this, "dbg")

    init {
        arm.ports.mem.connect(buses.mem)
        ram1.ports.mem.connect(buses.mem)
        boot.ports.mem.connect(buses.mem, 0x0800_0000u)
        dbg.ports.breakpoint.connect(buses.mem)
        initializeAndResetAsTopInstance()
        arm.cpu.BXWritePC(0x1u)  // binary compiled as Thumb ARMv7
    }

    private fun prepareStrings() {
        val str = "Hello World"
        val address1 = 0x100uL
        val address2 = 0x200uL
        store(address1, str)
        regs(r0 = address1, r1 = address2)
    }

    private fun regs(r0: ULong = 0u, r1: ULong = 0u, r2: ULong = 0u, r3: ULong = 0u, r4: ULong = 0u,
                     r5: ULong = 0u, r6: ULong = 0u, r7: ULong = 0u, r8: ULong = 0u, r9: ULong = 0u,
                     r10: ULong = 0u, r11: ULong = 0u, r12: ULong = 0u, r13: ULong = 0u, r14: ULong = 0u) {
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
    }

    private fun flags(n: ULong = 0u, z: ULong = 0u, c: ULong = 0u, v: ULong = 0u) {
        arm.cpu.flags.n = n.truth
        arm.cpu.flags.z = z.truth
        arm.cpu.flags.c = c.truth
        arm.cpu.flags.v = v.truth
    }

    private fun status(q: ULong = 0u, ge: ULong = 0u) {
        arm.cpu.status.q = q.truth
        arm.cpu.status.ge = ge
    }

    private fun load(address: ULong, size: Int): String = arm.load(address, size).hexlify()
    private fun load(address: ULong, dtyp: Datatype): ULong = arm.read(dtyp, address, 0)
    private fun store(address: ULong, data: String) = arm.store(address, data.unhexlify())
    private fun store(address: ULong, data: ULong, dtyp: Datatype) = arm.write(dtyp, address, data, 0)

    private fun assertPC(expected: ULong, actual: ULong, type: String = "PC") =
            Assert.assertEquals("$type error: 0x${expected.hex8} != 0x${actual.hex8}", expected, actual)

    @Before fun resetTest() {
        arm.reset()
        dbg.reset()
        prepareStrings()
    }

    private fun setBreakpoint(address: ULong, btyp: BreakpointType = SOFTWARE) = dbg.bptSet(btyp, address)
    private fun deleteBreakpoint(address: ULong) = dbg.bptClr(address)

    @Test fun bptExecTest() {
        val expected = 0x6uL
        setBreakpoint(expected)
        dbg.cont()
        assertPC(expected, dbg.cpu.pc)
        dbg.step()
        assertPC(expected + 2u, arm.pc)
    }

    @Test fun bptWriteTest() {
        val expected = 0x8uL
        setBreakpoint(0x100u, WRITE)
        dbg.cont()
        assertPC(expected, dbg.cpu.pc)
    }

    @Test fun bptReadTest() {
        val expected = 0x4uL
        setBreakpoint(0x200u, READ)
        dbg.cont()
        assertPC(expected, dbg.cpu.pc)
    }

    @Test fun bptAddDelete() {
        val expected = 0x8uL
        setBreakpoint(0x100u, WRITE)
        setBreakpoint(0x200u, READ)
        setBreakpoint(0x200u, READ)
        setBreakpoint(0x200u, READ)
        deleteBreakpoint(0x200u)
        deleteBreakpoint(0x200u)
        deleteBreakpoint(0x200u)
        dbg.cont()
        assertPC(expected, dbg.cpu.pc)
    }

}