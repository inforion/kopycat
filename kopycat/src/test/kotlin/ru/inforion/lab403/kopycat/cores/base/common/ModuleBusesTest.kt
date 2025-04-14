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
import ru.inforion.lab403.kopycat.cores.base.exceptions.ConnectionError

class ModuleBusesTest: Module(null, "Module Buses Test") {
    inner class Ports : ModulePorts(this) {
        val proxy = Proxy("proxy 32")
        val port1 = Port("port 1")
        val port2 = Port("port 2")
    }

    override val ports = Ports()

    inner class Buses : ModuleBuses(this) {
        val bus = Bus("bus")
    }

    override val buses = Buses()

    @Test fun connectExceptionTest1() { buses.connect(ports.proxy, ports.port1) }
    @Test fun connectExceptionTest2() { buses.connect(ports.port1, ports.port2) }

    // Connected ports are pointing to the same object
    @Test fun connectExceptionTest3() {
        assertThrows<ConnectionError> {
            buses.connect(ports.port1, ports.port1)
        }
    }

    @Test fun connectInnerTest1() {
        ports.port1.connect(buses.bus)
        buses.connect(ports.proxy, ports.port1)
    }

    @Test fun connectInnerTest2() {
        ports.port2.connect(buses.bus)
        buses.connect(ports.port1, ports.port2)
    }

    @Test fun connectArrayTest1() {
        val base = ports.ports(2, "test_slave")
        val port = ports.ports(2, "test_master")
        buses.connect(base, port)
    }
}
