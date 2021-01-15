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
package ru.inforion.lab403.kopycat.cores.base.common

import org.junit.Assert
import org.junit.Test
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS.R_W
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.modules.cores.device.TestTopDevice
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.BUS32

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
        val mem1 = Memory(module.ports.slave16, 0xFA, 0x100, "a", R_W)
        val mem2 = Memory(module.ports.slave16, 0x1AFA, 0x2000, "b", R_W)
        initializeAndResetAsTopInstance()
        mem1.write(DWORD, 0xFA, 0xFFFF)
        mem2.write(DWORD, 0x1AFA, 0xFFFF)
    }

    @Test fun masterSlaveTest2() {
        buses.connect(module.ports.master16, module.ports.slave16)
        val mem1 = Memory(module.ports.slave16, 0xFA, 0x100, "a", R_W)
        val mem2 = Memory(module.ports.slave16, 0x100, 0x200, "b", R_W)
        initializeAndResetAsTopInstance()
        mem1.write(DWORD, 0xFA, 0xFFFF)
        mem2.write(DWORD, 0x1AF, 0xFFFF)
    }

    @Test fun masterSlaveTest3() {
        buses.connect(module.ports.master16, module.ports.slave16)
        val mem1 = Memory(module.ports.slave16, 0x0, 0x200, "a", R_W)
        val mem2 = Memory(module.ports.slave16, 0x100, 0x300, "b", R_W)
        initializeAndResetAsTopInstance()
        mem1.write(DWORD, 0x1FA, 0x1111)
        mem2.write(DWORD, 0x1FA, 0xFFFF)
        val readMem1 = mem1.read(DWORD, 0x1FA)
        val readMem2 = mem2.read(DWORD, 0x1FA)
        assert(0x1111, readMem1)
        assert(0xFFFF, readMem2)
    }

    @Test(expected = IllegalStateException::class) fun masterSlaveTest4() {
        buses.connect(module.ports.master16, module.ports.slave16)
        Memory(module.ports.slave16, 0x0, 0x200, "a", R_W)
        Memory(module.ports.slave16, 0x100, 0x300, "b", R_W)
        initializeAndResetAsTopInstance()
        module.ports.master16.read(DWORD, 0x102)
    }

    @Test fun masterSlaveTest5() {
        buses.connect(module.ports.master16, module.ports.slave16)
        Memory(module.ports.slave16, 0x0, 0x200, "a", R_W)
        Memory(module.ports.slave16, 0x100, 0x300, "b", R_W)
        initializeAndResetAsTopInstance()
        module.ports.master16.read(DWORD, 0x202)
        module.ports.master16.read(DWORD, 0x2)
    }

    @Test fun registerTest1() {
        buses.connect(module.ports.master16, module.ports.slave16)
        val reg1 = Register(module.ports.slave16, 0xFA, DWORD, "a")
        val reg2 = Register(module.ports.slave16, 0x1AFA, DWORD, "b")
        initializeAndResetAsTopInstance()
        reg1.write(DWORD, 0xFA, 0xFFFF)
        reg2.write(DWORD, 0x1AFA, 0xFFFF)
    }

    @Test fun registerTest2() {
        buses.connect(module.ports.master16, module.ports.slave16)
        val reg1 = Register(module.ports.slave16, 0xFA, DWORD, "a")
        val reg2 = Register(module.ports.slave16, 0x200, DWORD, "b")
        initializeAndResetAsTopInstance()
        reg1.write(DWORD, 0xFA, 0xFFFF)
        reg2.write(DWORD, 0x1AF, 0xFFFF)
    }

    @Test fun registerTest3() {
        buses.connect(module.ports.master16, module.ports.slave16)
        val reg1 = Register(module.ports.slave16, 0x100, DWORD, "a")
        val reg2 = Register(module.ports.slave16, 0x100, DWORD, "b")
        initializeAndResetAsTopInstance()
        reg1.write(DWORD, 0x1FA, 0x1111)
        reg2.write(DWORD, 0x1FA, 0xFFFF)
        val readMem1 = reg1.read(DWORD, 0x1FA)
        val readMem2 = reg2.read(DWORD, 0x1FA)
        assert(0x1111, readMem1)
        assert(0xFFFF, readMem2)
    }

    @Test(expected = MemoryAccessError::class) fun registerTest4() {
        buses.connect(module.ports.master16, module.ports.slave16)
        Register(module.ports.slave16, 0x100, DWORD, "a")
        Register(module.ports.slave16, 0x100, DWORD, "b")
        initializeAndResetAsTopInstance()
        module.ports.master16.write(DWORD, 0x100, 0xFFFF)
    }

    @Test fun proxyTest1() {
        initializeAndResetAsTopInstance()
        val value = 0xBABA_DEDA
        testCore.core.write(DWORD, 0x20, value)
        val readed = testCore.core.read(DWORD, 0x20)
        assert(value, readed)
    }
}