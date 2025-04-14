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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS.R_W
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.cores.ARMv7Core
import ru.inforion.lab403.kopycat.modules.cores.ARMDebugger
import ru.inforion.lab403.kopycat.modules.memory.RAM
import ru.inforion.lab403.kopycat.interfaces.*
import kotlin.test.assertEquals

class ModuleTest: Module(null, "Module Test") {
    inner class Buses: ModuleBuses(this) {
        val mem = Bus("mem")
    }

    override val buses = Buses()

    inner class Ports : ModulePorts(this) {
        val port = Port("port")
    }

    override val ports = Ports()

    private val arm = ARMv7Core(this, "arm", 48.MHz, 1.0)
    private val ram1 = RAM(this, "ram1", 0x1000, Resource("binaries/strcpy.bin"))
    private val boot = RAM(this, "boot", 0x1000)
    private val dbg = ARMDebugger(this, "dbg")


    init {
        arm.ports.mem.connect(buses.mem)
        ram1.ports.mem.connect(buses.mem)
        boot.ports.mem.connect(buses.mem, 0x0800_0000u)
        arm.cpu.pc = 0x0u
        arm.cpu.status.ISETSTATE = 1u
        dbg.ports.breakpoint.connect(buses.mem)
        initializeAndResetAsTopInstance()
    }

    private fun assert(expected: Boolean, actual: Boolean, type: String = "Module") =
            assertEquals(expected, actual, "$type error: $expected != $actual")

    private fun assert(expected: ULong, actual: ULong, type: String = "Module") =
            assertEquals(expected, actual, "$type error: $expected != $actual")

    private fun assert(expected: String, actual: String, type: String = "Module") =
            assertEquals(expected, actual, "$type error: $expected != $actual")

    @Test fun isCorePresentTest() {
        assert(true, isCorePresent)
    }
    @Test fun isDebuggerPresentTest() {
        assert(true, isDebuggerPresent)
    }
    @Test fun isTracerPresentTest() {
        assert(false, isTracerPresent)
    }
    @Test fun voidTest1() {
        val mem = Void(ports.port, 0x10u, 0x20u, "a", R_W)
        this.assert(0u, mem.read(DWORD, 0x18u))
    }
    @Test fun voidTest2() {
        val mem = Void(ports.port, 0x10u, 0x20u, "a", R_W)
        mem.write(DWORD, 0x18u, 0xFFFF_FFFFu)
        this.assert(0u, mem.read(DWORD, 0x18u))
    }
    @Test fun memoryTest1() {
        val mem = Memory(ports.port, 0x10u, 0x20u, "a", R_W)
        mem.write(BYTE, 0x18u, 0xFAu)
        this.assert(0xFAu, mem.read(BYTE, 0x18u))
    }
    @Test fun memoryTest2() {
        val mem = Memory(ports.port, 0x10u, 0x20u, "a", R_W)
        mem.write(WORD, 0x18u, 0xFEEDu)
        this.assert(0xFEEDu, mem.read(WORD, 0x18u))
    }
    @Test fun memoryTest3() {
        val mem = Memory(ports.port, 0x10u, 0x20u, "a", R_W)
        mem.write(DWORD, 0x18u, 0xFEED_BEEFu)
        this.assert(0xFEED_BEEFu, mem.read(DWORD, 0x18u))
    }
    @Test fun memoryTest4() {
        val mem = Memory(ports.port, 0x10u, 0x20u, "a", R_W)
        mem.write(QWORD, 0x18u, 0xEE_1234_FFFF_FFFFu)
        this.assert(0xEE_1234_FFFF_FFFFu, mem.read(QWORD, 0x18u))
    }
    @Test fun memoryTest5() {
        val mem = Memory(ports.port, 0x10u, 0x20u, "a", R_W)
        mem.write(FWORD, 0x18u, 0xFAFA_DADA_BABAu)
        this.assert(0xFAFA_DADA_BABAu, mem.read(FWORD, 0x18u))
    }
    @Test fun memoryTest6() {
        val mem = Memory(ports.port, 0x10u, 0x20u, "a", R_W)
        mem.write(FWORD, 0x18u, 0xFAFA_DADA_BABAu)
        this.assert(0xFAFA_DADA_BABAu, mem.read(FWORD, 0x18u))
    }
    @Test fun memoryTest7() {
        assertThrows<IllegalArgumentException> {
            val mem = Memory(ports.port, 0x10u, 0x20u, "a", R_W)
            mem.write(0x18uL, 0, 17, 0xFAFA_DADA_BABAu)
        }
    }
    @Test fun memoryTest8() {
        assertThrows<IllegalArgumentException> {
            val mem = Memory(ports.port, 0x10u, 0x20u, "a", R_W)
            mem.read(0x18u, 0, 17)
        }
    }
    @Test fun memoryTest9() {
        val mem = Memory(ports.port, 0x10u, 0x20u, "a", R_W)
        mem.store( 0x18u, "Hello".toByteArray(Charsets.UTF_8))
        val str = mem.load(0x18u, 5).toString(Charsets.UTF_8)
        assert("Hello", str)
    }
    @Test fun memoryTest10() {
        val mem = Memory(ports.port, 0x10u, 0x20u, "a", R_W)
        mem.write( 0x18u, "Hello".toByteArray(Charsets.UTF_8).inputStream())
        val str = mem.load(0x18u, 5).toString(Charsets.UTF_8)
        assert("Hello", str)
    }
    @Test fun registerTest1() {
        val mem = Register(ports.port, 0xFAFAu, DWORD, "a")
        mem.write(BYTE, 0x18u, 0xFAu)
        assert(0xFAu, mem.read(BYTE, 0x18u))
    }
    @Test fun registerTest2() {
        val mem = Register(ports.port, 0xFAFAu, DWORD, "a")
        assert("Module Test:port[Port]->a@FAFA[DWORD]", mem.toString())
    }
}
