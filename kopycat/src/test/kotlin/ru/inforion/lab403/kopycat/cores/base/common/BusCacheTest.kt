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
package ru.inforion.lab403.kopycat.cores.base.common

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS.R_W
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.modules.cores.device.TestTopDevice
import ru.inforion.lab403.kopycat.interfaces.*
import kotlin.test.assertEquals

class BusCacheTest : Module(null, "Module Buses Test") {

    inner class Ports : ModulePorts(this) {
        val port1 = Port("port 1")
        val port2 = Port("port 2")
    }

    override val ports = Ports()

    private val testCore = TestTopDevice(this, "Test")

    private fun <T> assert(expected: T, actual: T, type: String = "Bus Cache") =
        assertEquals(expected, actual, "$type error: $expected != $actual")

    @Test fun portTest1() {
        buses.connect(ports.port2, ports.port1)
        val mem1 = Memory(ports.port1, 0xFAu, 0x100u, "a", R_W)
        val mem2 = Memory(ports.port1, 0x1AFAu, 0x2000u, "b", R_W)
        initializeAndResetAsTopInstance()
        mem1.write(DWORD, 0xFAu, 0xFFFFu)
        mem2.write(DWORD, 0x1AFAu, 0xFFFFu)
    }

    @Test fun portTest2() {
        buses.connect(ports.port2, ports.port1)
        val mem1 = Memory(ports.port1, 0xFAu, 0x100u, "a", R_W)
        val mem2 = Memory(ports.port1, 0x100u, 0x200u, "b", R_W)
        initializeAndResetAsTopInstance()
        mem1.write(DWORD, 0xFAu, 0xFFFFu)
        mem2.write(DWORD, 0x1AFu, 0xFFFFu)
    }

    @Test fun portTest3() {
        buses.connect(ports.port2, ports.port1)
        val mem1 = Memory(ports.port1, 0x0u, 0x200u, "a", R_W)
        val mem2 = Memory(ports.port1, 0x100u, 0x300u, "b", R_W)
        initializeAndResetAsTopInstance()
        mem1.write(DWORD, 0x1FAu, 0x1111u)
        mem2.write(DWORD, 0x1FAu, 0xFFFFu)
        val readMem1 = mem1.read(DWORD, 0x1FAu)
        val readMem2 = mem2.read(DWORD, 0x1FAu)
        assert(0x1111uL, readMem1)
        assert(0xFFFFuL, readMem2)
    }

    @Test fun portTest4() {
        assertThrows<MemoryAccessError> {
            buses.connect(ports.port2, ports.port1)
            Memory(ports.port1, 0x0u, 0x200u, "a", R_W)
            Memory(ports.port1, 0x100u, 0x300u, "b", R_W)
            initializeAndResetAsTopInstance()
            ports.port2.read(DWORD, 0x102u)
        }
    }

    @Test fun portTest5() {
        buses.connect(ports.port2, ports.port1)
        Memory(ports.port1, 0x0u, 0x200u, "a", R_W)
        Memory(ports.port1, 0x100u, 0x300u, "b", R_W)
        initializeAndResetAsTopInstance()
        ports.port2.read(DWORD, 0x202u)
        ports.port2.read(DWORD, 0x2u)
    }

    @Test fun registerTest1() {
        buses.connect(ports.port2, ports.port1)
        val reg1 = Register(ports.port1, 0xFAu, DWORD, "a")
        val reg2 = Register(ports.port1, 0x1AFAu, DWORD, "b")
        initializeAndResetAsTopInstance()
        reg1.write(DWORD, 0xFAu, 0xFFFFu)
        reg2.write(DWORD, 0x1AFAu, 0xFFFFu)
    }

    @Test fun registerTest2() {
        buses.connect(ports.port2, ports.port1)
        Register(ports.port1, 0x100u, DWORD, "a")
        Register(ports.port1, 0x100u, DWORD, "b")
        initializeAndResetAsTopInstance()
        assertThrows<MemoryAccessError> {
            ports.port2.write(DWORD, 0x100u, 0xFFFFu)
        }
    }

    @Test fun registerTest3() {
        // In memory: EF BE AD DE
        Register(ports.port1, 0uL, DWORD, "word1", 0xDE_AD_BE_EFuL)
        initializeAndResetAsTopInstance()
        assertEquals(0xBE_EFuL, ports.port1.inw(0uL))
        assertEquals(0xDE_ADuL, ports.port1.inw(2uL))
    }

    @Test fun proxyTest1() {
        initializeAndResetAsTopInstance()
        val value = 0xBABA_DEDAuL
        testCore.core.write(DWORD, 0x20u, value)
        val readed = testCore.core.read(DWORD, 0x20u)
        assert(value, readed)
    }
}
