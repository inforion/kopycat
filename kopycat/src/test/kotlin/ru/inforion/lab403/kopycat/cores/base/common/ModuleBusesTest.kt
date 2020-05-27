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

import org.junit.Test
import ru.inforion.lab403.kopycat.cores.base.exceptions.ConnectionError
import ru.inforion.lab403.kopycat.modules.BUS16
import ru.inforion.lab403.kopycat.modules.BUS32

class ModuleBusesTest: Module(null, "Module Buses Test") {
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
    @Test fun connectExceptionTest1() { buses.connect(module.ports.proxy32, module.ports.master32) }
    @Test fun connectExceptionTest2() { buses.connect(module.ports.master16, module.ports.slave16) }
    @Test(expected = ConnectionError::class) fun connectExceptionTest3() {
        buses.connect(module.ports.master32, module.ports.master32)
    }
    @Test(expected = ConnectionError::class) fun connectExceptionTest4() {
        buses.connect(module.ports.master16, module.ports.master32)
    }
    @Test(expected = ConnectionError::class)  fun connectOuterTest1() {
        module.ports.proxy32.connect(buses.outerBus32)
        buses.connect(module.ports.proxy32, module.ports.master32)
    }
    @Test(expected = ConnectionError::class) fun connectInnerTest1() {
        module.ports.master32.connect(buses.innerBus32)
        buses.connect(module.ports.proxy32, module.ports.master32)
    }
    @Test(expected = ConnectionError::class) fun connectInnerTest2() {
        module.ports.slave32.connect(buses.innerBus32)
        buses.connect(module.ports.proxy32, module.ports.slave32)
    }
    @Test(expected = ConnectionError::class) fun connectInnerTest3() {
        module.ports.slave32.connect(buses.innerBus32)
        buses.connect(module.ports.master32, module.ports.slave32)
    }
    @Test(expected = ConnectionError::class) fun connectInnerTest4() {
        module.ports.slave32.connect(buses.innerBus32)
        buses.connect(module.ports.proxy16, module.ports.slave32)
    }
    @Test(expected = ConnectionError::class) fun connectArrayTest1() {
        val base = module.ports.slaves(2, "test_slave", BUS32)
        val port = module.ports.masters(2, "test_master", BUS16)
        buses.connect(base, port)
    }
    @Test fun connectArrayTest2() {
        val base = module.ports.slaves(2, "test_slave", BUS32)
        val port = module.ports.masters(2, "test_master", BUS32)
        buses.connect(base, port)
    }
}