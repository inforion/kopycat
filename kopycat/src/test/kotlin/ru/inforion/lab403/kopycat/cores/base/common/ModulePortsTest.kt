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
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS.R_W
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.LOAD
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.ConnectionError
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.modules.cores.device.TestCore
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.BUS32

class ModulePortsTest: Module(null, "Ports module test") {
    class AnotherClass: Module(null, "Another module test") {
        inner class Buses : ModuleBuses(this) {
            val outerBus16 = Bus("outer bus 16", BUS16)
            val outerBus32 = Bus("outer bus 32", BUS32)
            val innerBus16 = Bus("inner bus 16", BUS16)
        }
        override val buses = Buses()
    }

    inner class Ports: ModulePorts(this) {
        val proxy32 = Proxy("proxy 32", BUS32)
        val proxy16 = Proxy("proxy 16", BUS16)
        val slave32 = Slave("slave 32", BUS32)
        val slave16 = Slave("slave 16", BUS16)
        val master32 = Master("master 32", BUS32)
        val master16 = Master("master 16", BUS16)
        val translator16 = Translator("translator 16", master16, BUS16,
                AddressTranslator(this@ModulePortsTest, "AT16", 16, 16))
        val translator32 = Translator("translator 32", master32, BUS32,
                AddressTranslator(this@ModulePortsTest, "AT32", 32, 32))

        fun test1() { Slave("ports", BUS32) }
        fun test2() { Slave("buses", BUS32) }
        fun test3() { Slave("slave", 0) }
        fun test4() { Slave("slave", BUS32); Slave("slave", BUS32) }
    }

    inner class Buses : ModuleBuses(this) {
        val outerBus32 = Bus("outer bus 32", BUS32)
        val innerBus32 = Bus("inner bus 32", BUS32)
    }

    class InnerPorts(parent: Component?): Module(parent, "Inner ports test") {
        inner class Ports: ModulePorts(this) {
            val slave32 = Slave("slave 32", BUS32)
            val slave16 = Slave("slave 16", BUS16)
            val master32 = Master("master 32", BUS32)
            val master16 = Master("master 16", BUS16)
            val proxy16 = Proxy("proxy 16", BUS16)
            val translator16 = Translator("translator 16", master16, BUS16,
                    AddressTranslator(this@InnerPorts, "AT16", 16, 16))
        }
        override val ports = Ports()
    }

    override val buses = Buses()
    override val ports = Ports()
    private val innerPorts = InnerPorts(this)
    private val anotherModule = AnotherClass()
    val testCore = TestCore(this, "Test")

    private fun assert(expected: Boolean, actual: Boolean, type: String = "Module") =
            Assert.assertEquals("$type error: $expected != $actual", expected, actual)

    private fun assert(expected: Long, actual: Long, type: String = "Module") =
            Assert.assertEquals("$type error: $expected != $actual", expected, actual)

    private fun assert(expected: String, actual: String, type: String = "Module") =
            Assert.assertEquals("$type error: $expected != $actual", expected, actual)

    @Test(expected = ModulePorts.PortDefinitionError::class) fun slaveTest1() { ports.test1() }
    @Test(expected = ModulePorts.PortDefinitionError::class) fun slaveTest2() { ports.test2() }
    @Test(expected = ModulePorts.PortDefinitionError::class) fun slaveTest3() { ports.test3() }
    @Test(expected = ModulePorts.PortDefinitionError::class) fun slaveTest4() { ports.test4() }
    @Test(expected = ConnectionError::class) fun slaveTest5() { ports.slave32.connect(buses.innerBus32) }
    @Test(expected = ConnectionError::class) fun slaveTest6() { ports.slave32.connect(anotherModule.buses.outerBus16) }
    @Test fun slaveTest7() {
        ports.slave16.add(Memory(ports.slave16, 0xFA, 0x100, "a", R_W))
        assert(2, ports.slave16.areas.size.asLong)
    }
    @Test fun slaveTest8() {
        ports.slave16.add(Register(ports.slave16, 0xFAFA, DWORD, "a"))
        assert(2, ports.slave16.registers.size.asLong)
    }

    @Test(expected = ConnectionError::class) fun proxyTest1() { ports.proxy32.connect(anotherModule.buses.outerBus16) }
    @Test fun proxyTest2() {
        ports.proxy32.connect(buses.innerBus32)
        assert(ports.proxy32.hasInnerConnection, true)
    }
    @Test fun proxyTest3() {
        ports.proxy32.connect(anotherModule.buses.outerBus32)
        assert(ports.proxy32.hasOuterConnection, true)
    }

    @Test(expected = ConnectionError::class) fun transTest1() {
        anotherModule.buses.connect(ports.master16, ports.slave16)
        Memory(ports.slave16, 0xFA, 0x100, "a", R_W)
        ports.translator16.find(ports.master16, 0xFA, 0, 4, LOAD, 0)
    }
    @Test fun transTest2() {
        buses.connect(innerPorts.ports.master16, innerPorts.ports.slave16)
        Memory(innerPorts.ports.slave16, 0xFA, 0x100, "a", R_W)
        initializeAndResetAsTopInstance()
        assert(true,
                innerPorts.ports.translator16.find(innerPorts.ports.master16, 0xFA, 0, 4, LOAD, 0) != null)

    }

    @Test fun masterTest1() {
        buses.connect(innerPorts.ports.master16, innerPorts.ports.slave16)
        val mem = Memory(innerPorts.ports.slave16, 0xFA, 0x100, "a", R_W)
        initializeAndResetAsTopInstance()
        mem.write(DWORD, 0xFA, 0xFFFF)
        val a = innerPorts.ports.master16.access(0xFA)
        assert(true, a)
    }

    @Test(expected = IllegalAccessError::class) fun masterTest2(){ ports.master16.beforeRead(ports.master16, 0xFFFF) }
    @Test(expected = IllegalAccessError::class) fun masterTest3(){ ports.master16.beforeWrite(ports.master16, 0xFFFF, 0) }
    @Test(expected = MemoryAccessError::class ) fun masterTest4(){ ports.master16.read(0xFFFF, 0 , 4) }
    @Test(expected = MemoryAccessError::class ) fun masterTest5(){ ports.master16.write(0xFFFF, 0 , 4, 0xFFFF) }
}