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

import org.junit.Assert
import org.junit.Test
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS.R_W
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.modules.cores.device.TestTopDevice
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.interfaces.*

class BusCacheTest: Module(null, "Module Buses Test") {
    class PortsModule(parent: Module): Module(parent, "Ports module") {
        inner class Ports : ModulePorts(this) {
            val proxy32 = Proxy("proxy 32", BUS32)
            val proxy16 = Proxy("proxy 16", BUS16)
            val slave32 = Slave("slave 32", BUS32)
            val slave16 = Slave("slave 16", BUS16)
            val master32 = Master("master 32", BUS32)
            val master16 = Master("master 16", BUS16)
        }
        override val ports = Ports()
    }
    private val module = PortsModule(this)
    inner class Buses : ModuleBuses(this) {
        val outerBus32 = Bus("outer bus 32", BUS32)
        val innerBus32 = Bus("inner bus 32", BUS32)
    }
    override val buses = Buses()

    val testCore = TestTopDevice(this, "Test")

    private fun <T> assert(expected: T, actual: T, type: String = "Bus Cashe") =
            Assert.assertEquals("$type error: $expected != $actual", expected, actual)

    @Test fun masterSlaveTest1() {
        buses.connect(module.ports.master16, module.ports.slave16)
        val mem1 = Memory(module.ports.slave16, 0xFAu, 0x100u, "a", R_W)
        val mem2 = Memory(module.ports.slave16, 0x1AFAu, 0x2000u, "b", R_W)
        initializeAndResetAsTopInstance()
        mem1.write(DWORD, 0xFAu, 0xFFFFu)
        mem2.write(DWORD, 0x1AFAu, 0xFFFFu)
    }

    @Test fun masterSlaveTest2() {
        buses.connect(module.ports.master16, module.ports.slave16)
        val mem1 = Memory(module.ports.slave16, 0xFAu, 0x100u, "a", R_W)
        val mem2 = Memory(module.ports.slave16, 0x100u, 0x200u, "b", R_W)
        initializeAndResetAsTopInstance()
        mem1.write(DWORD, 0xFAu, 0xFFFFu)
        mem2.write(DWORD, 0x1AFu, 0xFFFFu)
    }

    @Test fun masterSlaveTest3() {
        buses.connect(module.ports.master16, module.ports.slave16)
        val mem1 = Memory(module.ports.slave16, 0x0u, 0x200u, "a", R_W)
        val mem2 = Memory(module.ports.slave16, 0x100u, 0x300u, "b", R_W)
        initializeAndResetAsTopInstance()
        mem1.write(DWORD, 0x1FAu, 0x1111u)
        mem2.write(DWORD, 0x1FAu, 0xFFFFu)
        val readMem1 = mem1.read(DWORD, 0x1FAu)
        val readMem2 = mem2.read(DWORD, 0x1FAu)
        assert(0x1111uL, readMem1)
        assert(0xFFFFuL, readMem2)
    }

    @Test(expected = IllegalStateException::class) fun masterSlaveTest4() {
        buses.connect(module.ports.master16, module.ports.slave16)
        Memory(module.ports.slave16, 0x0u, 0x200u, "a", R_W)
        Memory(module.ports.slave16, 0x100u, 0x300u, "b", R_W)
        initializeAndResetAsTopInstance()
        module.ports.master16.read(DWORD, 0x102u)
    }

    @Test fun masterSlaveTest5() {
        buses.connect(module.ports.master16, module.ports.slave16)
        Memory(module.ports.slave16, 0x0u, 0x200u, "a", R_W)
        Memory(module.ports.slave16, 0x100u, 0x300u, "b", R_W)
        initializeAndResetAsTopInstance()
        module.ports.master16.read(DWORD, 0x202u)
        module.ports.master16.read(DWORD, 0x2u)
    }

    @Test fun registerTest1() {
        buses.connect(module.ports.master16, module.ports.slave16)
        val reg1 = Register(module.ports.slave16, 0xFAu, DWORD, "a")
        val reg2 = Register(module.ports.slave16, 0x1AFAu, DWORD, "b")
        initializeAndResetAsTopInstance()
        reg1.write(DWORD, 0xFAu, 0xFFFFu)
        reg2.write(DWORD, 0x1AFAu, 0xFFFFu)
    }

    @Test fun registerTest2() {
        buses.connect(module.ports.master16, module.ports.slave16)
        val reg1 = Register(module.ports.slave16, 0xFAu, DWORD, "a")
        val reg2 = Register(module.ports.slave16, 0x200u, DWORD, "b")
        initializeAndResetAsTopInstance()
        reg1.write(DWORD, 0xFAu, 0xFFFFu)
        reg2.write(DWORD, 0x1AFu, 0xFFFFu)
    }

    @Test fun registerTest3() {
        buses.connect(module.ports.master16, module.ports.slave16)
        val reg1 = Register(module.ports.slave16, 0x100u, DWORD, "a")
        val reg2 = Register(module.ports.slave16, 0x100u, DWORD, "b")
        initializeAndResetAsTopInstance()
        reg1.write(DWORD, 0x1FAu, 0x1111u)
        reg2.write(DWORD, 0x1FAu, 0xFFFFu)
        val readMem1 = reg1.read(DWORD, 0x1FAu)
        val readMem2 = reg2.read(DWORD, 0x1FAu)
        assert(0x1111uL, readMem1)
        assert(0xFFFFuL, readMem2)
    }

    @Test(expected = MemoryAccessError::class) fun registerTest4() {
        buses.connect(module.ports.master16, module.ports.slave16)
        Register(module.ports.slave16, 0x100u, DWORD, "a")
        Register(module.ports.slave16, 0x100u, DWORD, "b")
        initializeAndResetAsTopInstance()
        module.ports.master16.write(DWORD, 0x100u, 0xFFFFu)
    }

    @Test fun proxyTest1() {
        initializeAndResetAsTopInstance()
        val value = 0xBABA_DEDAuL
        testCore.core.write(DWORD, 0x20u, value)
        val readed = testCore.core.read(DWORD, 0x20u)
        assert(value, readed)
    }
}