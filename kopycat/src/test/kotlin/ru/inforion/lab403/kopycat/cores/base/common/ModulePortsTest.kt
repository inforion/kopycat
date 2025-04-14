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
import ru.inforion.lab403.common.extensions.hexlify
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.kopycat.cores.base.Port
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS.R_W
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.LOAD
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.interfaces.*
import ru.inforion.lab403.kopycat.modules.cores.device.TestTopDevice
import ru.inforion.lab403.kopycat.modules.memory.RAM
import java.nio.ByteOrder
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ModulePortsTest : Module(null, "Ports module test") {
    inner class Buses : ModuleBuses(this) {
        val bus = Bus("bus")
        val bus2 = Bus("bus2")
    }

    override val buses = Buses()

    inner class Ports : ModulePorts(this) {
        val proxy1 = Proxy("proxy 1")
        val proxy2 = Proxy("proxy")
        val port1 = Port("port 1")
        val port2 = Port("port 2")
        val port3 = Port("port 3")
        val port4 = Port("port 4")

        fun test1() { Port("ports") }
        fun test2() { Port("buses") }
        fun test3() { Port("port 3"); Port("port 3") }
    }

    override val ports = Ports()

    private class InnerModule(parent: Component?): Module(parent, "Inner Module") {
        inner class Buses : ModuleBuses(this) {
            val bus = Bus("bus")
        }

        override val buses = Buses()

        inner class Ports: ModulePorts(this) {
            val slave = Port("slave")
            val master = Port("master")
            val translator = Translator(
                "translator",
                master,
                AddressTranslator(this@InnerModule, "AT"),
            )
        }
        override val ports = Ports()
    }

    private val innerModule = InnerModule(this)

    init {
        TestTopDevice(this, "DummyTop")
    }

    @Test fun slaveTest1() { assertThrows<ModulePorts.PortDefinitionError> { ports.test1() } }
    @Test fun slaveTest2() { assertThrows<ModulePorts.PortDefinitionError> { ports.test2() } }
    // port name is duplicated
    @Test fun slaveTest3() { assertThrows<ModulePorts.PortDefinitionError> { ports.test3() } }

    @Test fun slaveTest4() {
        ports.port2.add(Memory(ports.port2, 0xFAu, 0x100u, "a", R_W))
        assertEquals(2, ports.port2.areas.size)
    }

    @Test fun slaveTest5() {
        ports.port2.add(Register(ports.port2, 0xFAFAu, DWORD, "a"))
        assertEquals(2, ports.port2.registers.size)
    }

    @Test fun proxyTest1() {
        ports.proxy1.connect(buses.bus)
        assertTrue(ports.proxy1.hasConnection)
    }

    @Test fun proxyTest2() {
        ports.proxy1.connect(innerModule.buses.bus)
        assertTrue(ports.proxy1.hasConnection)
    }

    @Test fun transTest1() {
        buses.connect(innerModule.ports.master, innerModule.ports.slave)
        Memory(innerModule.ports.slave, 0xFAu, 0x100u, "a", R_W)
        initializeAndResetAsTopInstance()
        assertTrue(
            innerModule.ports.translator.find(
                innerModule.ports.master,
                0xFAu,
                0,
                4,
                LOAD,
                0u,
                ByteOrder.LITTLE_ENDIAN,
            ) != null
        )
    }

    @Test fun transTest2() {
        val translator = object : AddressTranslator(this@ModulePortsTest, "translator") {
            override fun translate(ea: ULong, ss: Int, size: Int, LorS: AccessAction) : ULong {
                return ea * 0x10uL
            }
        }

        buses.connect(translator.ports.outp, ports.port1)
        buses.connect(translator.ports.inp, ports.port2)
        Register(ports.port1, 0x100uL, DWORD, "reg", 0xDEADBEEFuL)

        initializeAndResetAsTopInstance()
        assertEquals(0xDEADBEEFuL, ports.port2.read(0x10uL, 0, 4))
    }

    @Test fun portWithoutBus() {
        Register(ports.port1, 0x100uL, DWORD, "reg", 0xDEADBEEFuL)
        initializeAndResetAsTopInstance()
        assertEquals(0xDEADBEEFuL, ports.port1.read(0x100uL, 0, 4))
    }

    @Test fun portWithBus() {
        Register(ports.port1, 0x100uL, DWORD, "reg", 0xDEADBEEFuL)
        buses.connect(ports.port2, ports.port1)
        initializeAndResetAsTopInstance()
        assertEquals(0xDEADBEEFuL, ports.port1.read(0x100uL, 0, 4))
        assertEquals(0xDEADBEEFuL, ports.port2.read(0x100uL, 0, 4))
    }

    @Test fun masterTest1() {
        buses.connect(innerModule.ports.master, innerModule.ports.slave)
        val mem = Memory(innerModule.ports.slave, 0xFAu, 0x100u, "a", R_W)
        initializeAndResetAsTopInstance()
        mem.write(DWORD, 0xFAu, 0xFFFFu)
        val a = innerModule.ports.master.access(0xFAu)
        assertTrue(a)
    }

    @Test fun masterTest2() {
        assertThrows<IllegalAccessError> {
            ports.port1.beforeRead(ports.port1, 0xFFFFu, 2)
        }
    }

    @Test fun masterTest3() {
        assertThrows<IllegalAccessError> {
            ports.port1.beforeWrite(ports.port1, 0xFFFFu, 2, 0u)
        }
    }

    @Test fun masterTest4() {
        assertThrows<MemoryAccessError> {
            ports.port1.read(0xFFFFu, 0 , 4)
        }
    }

    @Test fun masterTest5() {
        assertThrows<MemoryAccessError> {
            ports.port1.write(0xFFFFu, 0 , 4, 0xFFFFu)
        }
    }

    private fun beProxyPropagationCommon(proxyLeft: ByteOrder, proxyRight: ByteOrder) {
        ports.port1.connect(buses.bus)
        ports.port2.connect(buses.bus)
        ports.proxy1.connect(buses.bus, endian = proxyLeft)
        ports.proxy1.connect(buses.bus2, endian = proxyRight)
        ports.port3.connect(buses.bus2)
        ports.port4.connect(buses.bus2, endian = ByteOrder.BIG_ENDIAN)

        Register(ports.port1, 0x10uL, DWORD, "reg1", 0x01uL)
        Register(ports.port2, 0x20uL, DWORD, "reg2", 0x02uL)
        Register(ports.port3, 0x30uL, DWORD, "reg3", 0x03uL)
        Register(ports.port4, 0x40uL, DWORD, "reg4", 0x04uL)
        initializeAndResetAsTopInstance()
    }

    @Test fun beProxyPropagationBEpBE() {
        /*
        Port1                  Port3
         |                       |
        Bus -BE- Proxy -BE- InnerModule.Bus
         |                       |
         |                       BE
         |                       |
        Port2                  Port4
        */

        beProxyPropagationCommon(ByteOrder.BIG_ENDIAN, ByteOrder.BIG_ENDIAN)

        assertEquals(0x01uL, ports.port1.inl(0x10uL))
        assertEquals(0x02uL, ports.port1.inl(0x20uL))
        assertEquals(0x03_00_00_00uL, ports.port1.inl(0x30uL))
        assertEquals(0x04uL, ports.port1.inl(0x40uL)) // double byte swap

        assertEquals(0x01_00_00_00uL, ports.port4.inl(0x10uL))
        assertEquals(0x02_00_00_00uL, ports.port4.inl(0x20uL))
        assertEquals(0x03uL, ports.port4.inl(0x30uL))
        assertEquals(0x04uL, ports.port4.inl(0x40uL))
    }

    @Test fun beProxyPropagationLEpBE() {
        /*
        Port1               Port3
         |                    |
        Bus - Proxy -BE- InnerModule.Bus
         |                    |
         |                    BE
         |                    |
        Port2               Port4
        */

        beProxyPropagationCommon(ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN)

        assertEquals(0x01uL, ports.port1.inl(0x10uL))
        assertEquals(0x02uL, ports.port1.inl(0x20uL))
        assertEquals(0x03_00_00_00uL, ports.port1.inl(0x30uL))
        assertEquals(0x04uL, ports.port1.inl(0x40uL)) // double byte swap

        assertEquals(0x01uL, ports.port4.inl(0x10uL))
        assertEquals(0x02uL, ports.port4.inl(0x20uL))
        assertEquals(0x03uL, ports.port4.inl(0x30uL))
        assertEquals(0x04uL, ports.port4.inl(0x40uL))
    }

    @Test fun beProxyPropagationBEpLE() {
        /*
        Port1               Port3
         |                    |
        Bus -BE- Proxy - InnerModule.Bus
         |                    |
         |                    BE
         |                    |
        Port2               Port4
        */

        beProxyPropagationCommon(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)

        assertEquals(0x01uL, ports.port1.inl(0x10uL))
        assertEquals(0x02uL, ports.port1.inl(0x20uL))
        assertEquals(0x03uL, ports.port1.inl(0x30uL))
        assertEquals(0x04_00_00_00uL, ports.port1.inl(0x40uL))

        assertEquals(0x01_00_00_00uL, ports.port4.inl(0x10uL))
        assertEquals(0x02_00_00_00uL, ports.port4.inl(0x20uL))
        assertEquals(0x03uL, ports.port4.inl(0x30uL))
        assertEquals(0x04uL, ports.port4.inl(0x40uL))
    }

    @Test fun beProxyPropagationLEpLE() {
        /*
        Port1            Port3
         |                 |
        Bus - Proxy - InnerModule.Bus
         |                 |
         |                 BE
         |                 |
        Port2            Port4
        */

        beProxyPropagationCommon(ByteOrder.LITTLE_ENDIAN, ByteOrder.LITTLE_ENDIAN)

        assertEquals(0x01uL, ports.port1.inl(0x10uL))
        assertEquals(0x02uL, ports.port1.inl(0x20uL))
        assertEquals(0x03uL, ports.port1.inl(0x30uL))
        assertEquals(0x04_00_00_00uL, ports.port1.inl(0x40uL))

        assertEquals(0x01uL, ports.port4.inl(0x10uL))
        assertEquals(0x02uL, ports.port4.inl(0x20uL))
        assertEquals(0x03uL, ports.port4.inl(0x30uL))
        assertEquals(0x04uL, ports.port4.inl(0x40uL))
    }

    private fun beProxyXorCommon(endian: Array<ByteOrder>) {
        /*
        Port1 - BE                                                  BE - Port3
                 |                                                  |
        Port2 - Bus -1- Proxy1 -2- InnerModule.Bus -3- Proxy2 -4- Bus2 - Port4
        */

        ports.port1.connect(buses.bus, endian = ByteOrder.BIG_ENDIAN)
        ports.port2.connect(buses.bus)
        ports.proxy1.connect(buses.bus, endian = endian[0])
        ports.proxy1.connect(innerModule.buses.bus, endian = endian[1])
        ports.proxy2.connect(innerModule.buses.bus, endian = endian[2])
        ports.proxy2.connect(buses.bus2, endian = endian[3])
        ports.port3.connect(buses.bus2, endian = ByteOrder.BIG_ENDIAN)
        ports.port4.connect(buses.bus2)

        Register(ports.port1, 0x10uL, DWORD, "reg1", 0x01uL)
        Register(ports.port2, 0x20uL, DWORD, "reg2", 0x02uL)
        Register(ports.port3, 0x30uL, DWORD, "reg3", 0x03uL)
        Register(ports.port4, 0x40uL, DWORD, "reg4", 0x04uL)
        initializeAndResetAsTopInstance()
    }

    @Test fun beProxyXor1() {
        /*
        Port1 - BE                                                      BE - Port3
                 |                                                      |
        Port2 - Bus -BE- Proxy1 -BE- InnerModule.Bus -BE- Proxy2 -BE- Bus2 - Port4
        */
        beProxyXorCommon(
            arrayOf(
                ByteOrder.BIG_ENDIAN,
                ByteOrder.BIG_ENDIAN,
                ByteOrder.BIG_ENDIAN,
                ByteOrder.BIG_ENDIAN,
            )
        )

        arrayOf(ports.port1, ports.port2).forEach { p ->
            assertEquals(
                if (p === ports.port1) {
                    0x01uL
                } else {
                    0x01_00_00_00uL
                },
                p.inl(0x10uL)
            )
            assertEquals(0x02uL, p.inl(0x20uL))
            // {Port1, Port2} -> Proxy1 (becomes BE) -> Proxy2 (becomes LE) -> Port3 (becomes BE)
            assertEquals(0x03_00_00_00uL, p.inl(0x30uL))
            assertEquals(0x04uL, p.inl(0x40uL))
        }

        arrayOf(ports.port3, ports.port4).forEach { p ->
            assertEquals(0x01_00_00_00uL, p.inl(0x10uL))
            assertEquals(0x02uL, p.inl(0x20uL))
            assertEquals(
                if (p === ports.port3) {
                    0x03uL
                } else {
                    0x03_00_00_00uL
                },
                p.inl(0x30uL)
            )
            assertEquals(0x04uL, p.inl(0x40uL))
        }
    }

    @Test fun beProxyXor2() {
        /*
        Port1 - BE                                                     BE - Port3
                 |                                                     |
        Port2 - Bus -BE- - Proxy1 -BE- InnerModule.Bus - Proxy2 -BE- Bus2 - Port4
        */
        beProxyXorCommon(
            arrayOf(
                ByteOrder.BIG_ENDIAN,
                ByteOrder.BIG_ENDIAN,
                ByteOrder.LITTLE_ENDIAN,
                ByteOrder.BIG_ENDIAN,
            )
        )

        arrayOf(ports.port1, ports.port2).forEach { p ->
            assertEquals(
                if (p === ports.port1) {
                    0x01uL
                } else {
                    0x01_00_00_00uL
                },
                p.inl(0x10uL)
            )
            assertEquals(0x02uL, p.inl(0x20uL))
            assertEquals(0x03_00_00_00uL, p.inl(0x30uL))
            assertEquals(0x04uL, p.inl(0x40uL))
        }

        arrayOf(ports.port3, ports.port4).forEach { p ->
            assertEquals(0x01uL, p.inl(0x10uL))
            assertEquals(0x02_00_00_00uL, p.inl(0x20uL))
            assertEquals(
                if (p === ports.port3) {
                    0x03uL
                } else {
                    0x03_00_00_00uL
                },
                p.inl(0x30uL)
            )
            assertEquals(0x04uL, p.inl(0x40uL))
        }
    }

    @Test fun beNeighbourRegTest() {
        // Port1 -BE- Bus - Port2

        ports.port1.connect(buses.bus, endian = ByteOrder.BIG_ENDIAN)
        ports.port2.connect(buses.bus)

        Register(ports.port1, 0x10uL, DWORD, "reg1", 0x01uL)
        Register(ports.port2, 0x20uL, DWORD, "reg2", 0x02uL)
        initializeAndResetAsTopInstance()

        assertEquals(0x02uL, ports.port1.inl(0x20uL))
        assertEquals(0x01_00_00_00uL, ports.port2.inl(0x10uL))
    }

    @Test fun beNeighbourAreaTest() {
        // Port1 -BE- Bus - Port2

        ports.port1.connect(buses.bus, endian = ByteOrder.BIG_ENDIAN)
        ports.port2.connect(buses.bus)

        object : Area(ports.port1, 0x10uL, 0x13uL, "area1") {
            override fun read(ea: ULong, ss: Int, size: Int): ULong = 0x01uL
            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = Unit
        }
        object : Area(ports.port2, 0x20uL, 0x23uL, "area2") {
            override fun read(ea: ULong, ss: Int, size: Int): ULong = 0x02uL
            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = Unit
        }

        initializeAndResetAsTopInstance()

        assertEquals(0x02uL, ports.port1.inl(0x20uL))
        assertEquals(0x01_00_00_00uL, ports.port2.inl(0x10uL))
    }

    @Test fun beBeforeActionTest() {
        // Port1 -BE- Bus - Port2

        ports.port1.connect(buses.bus, endian = ByteOrder.BIG_ENDIAN)
        ports.port2.connect(buses.bus)

        val reg1 = object : Register(ports.port1, 0x10uL, DWORD, "reg1") {
            override fun beforeWrite(from: Port, ea: ULong, size: Int, value: ULong): Boolean {
                assertEquals(0xBEBAFECAuL, value)
                return true
            }
        }

        val reg2 = object : Register(ports.port2, 0x20uL, DWORD, "reg2") {
            override fun beforeWrite(from: Port, ea: ULong, size: Int, value: ULong): Boolean {
                assertEquals(0xDEADBEEFuL, value)
                return true
            }
        }

        initializeAndResetAsTopInstance()

        ports.port1.outl(0x20uL, 0xDEADBEEFuL)
        ports.port2.outl(0x10uL, 0xCAFEBABEuL)

        assertEquals(0xDEADBEEFuL, reg2.data)
        assertEquals(0xBEBAFECAuL, reg1.data)
    }

    @Test fun portTwoBuses1() {
        // Old approach
        buses.connect(ports.port2, ports.port1)
        ports.port1.connect(buses.bus)
        ports.port3.connect(buses.bus)

        Register(ports.port1, 0x10uL, DWORD, "reg1", 0x01uL)
        Register(ports.port2, 0x20uL, DWORD, "reg2", 0x02uL)
        Register(ports.port3, 0x30uL, DWORD, "reg3", 0x03uL)

        initializeAndResetAsTopInstance()
        // Port1 is now connected to two buses simultaneously

        assertEquals(0x01uL, ports.port1.inl(0x10uL))
        assertEquals(0x02uL, ports.port1.inl(0x20uL))
        assertEquals(0x03uL, ports.port1.inl(0x30uL))
    }

    @Test fun portTwoBuses2() {
        buses.connect(ports.port2, ports.port1)
        buses.connect(ports.port3, ports.port1)

        Register(ports.port1, 0x10uL, DWORD, "reg1", 0x01uL)
        Register(ports.port2, 0x20uL, DWORD, "reg2", 0x02uL)
        Register(ports.port3, 0x30uL, DWORD, "reg3", 0x03uL)

        initializeAndResetAsTopInstance()

        assertEquals(0x01uL, ports.port1.inl(0x10uL))
        assertEquals(0x02uL, ports.port1.inl(0x20uL))
        assertEquals(0x03uL, ports.port1.inl(0x30uL))
    }

    @Test fun beRAMAndPort1() {
        // Big endian ram, little endian connection
        val beram = RAM(this, "beram", 0x1000).also {
            it.ports.mem.connect(buses.bus)
            ports.port1.connect(buses.bus)
            it.endian = ByteOrder.BIG_ENDIAN
        }

        // Little endian ram, big endian connection
        val leram = RAM(this, "leram", 0x1000).also {
            it.ports.mem.connect(buses.bus2, endian = ByteOrder.BIG_ENDIAN)
            ports.port2.connect(buses.bus2)
            it.endian = ByteOrder.LITTLE_ENDIAN
        }

        initializeAndResetAsTopInstance()

        beram.store(0uL, "0102030405060708".unhexlify())
        leram.store(0uL, "0102030405060708".unhexlify())

        assertEquals(ports.port1.inq(0uL), ports.port2.inq(0uL))
        assertEquals(0x01_02_03_04_05_06_07_08uL, ports.port2.inq(0uL))
    }

    @Test fun beRAMAndPort2() {
        // Big endian ram, big endian connection
        val beram = RAM(this, "beram", 0x1000).also {
            it.ports.mem.connect(buses.bus, endian = ByteOrder.BIG_ENDIAN)
            ports.port1.connect(buses.bus)
            it.endian = ByteOrder.BIG_ENDIAN
        }

        initializeAndResetAsTopInstance()

        beram.store(0uL, "0102030405060708".unhexlify())
        assertEquals(0x08_07_06_05_04_03_02_01uL, ports.port1.inq(0uL))
    }

    @Test fun crossPrimitiveLETest1() {
        Register(ports.port1, 0uL, BYTE, "reg1", 0xDEuL)
        Register(ports.port1, 1uL, BYTE, "reg2", 0xADuL)
        Register(ports.port1, 2uL, BYTE, "reg3", 0xBEuL)
        Register(ports.port1, 3uL, BYTE, "reg4", 0xEFuL)

        ports.port1.connect(buses.bus)
        ports.port2.connect(buses.bus)

        initializeAndResetAsTopInstance()

        assertEquals(0xEF_BE_AD_DEuL, ports.port2.inl(0uL))
        ports.port2.outl(0uL, 0xCA_FE_BA_BEuL)
        assertEquals(0xCA_FE_BA_BEuL, ports.port2.inl(0uL))
        assertEquals("BEBAFECA", ports.port2.load(0uL, 4).hexlify())
    }

    @Test fun crossPrimitiveLETest2() {
        Register(ports.port1, 0uL, WORD, "reg1", 0xDE_ADuL) // In memory: AD DE
        Register(ports.port1, 2uL, WORD, "reg2", 0xBE_EFuL) // In memory: EF BE

        initializeAndResetAsTopInstance()

        assertEquals(0xDE_ADuL, ports.port1.inw(0uL))
        assertEquals(0xBE_EF_DE_ADuL, ports.port1.inl(0uL))
    }

    @Test fun crossPrimitiveLETest3() {
        val area1 = Memory(ports.port1, 0uL, 1uL, "area1", R_W)
        val area2 = Memory(ports.port1, 2uL, 5uL, "area2", R_W)
        val area3 = Memory(ports.port1, 6uL, 7uL, "area3", R_W)

        initializeAndResetAsTopInstance()

        area1.outw(0uL, 0xDE_ADuL)
        area2.outl(2uL, 0xCA_FE_BA_BEuL)
        area3.outw(6uL, 0xBE_EFuL)

        // In memory: AD DE | BE BA FE CA | EF BE
        assertEquals(0xBE_EF_CA_FE_BA_BE_DE_ADuL, ports.port1.inq(0uL))
        ports.port1.outq(0uL, 0x01_02_03_04_05_06_07_08uL)

        assertEquals(0x01_02_03_04_05_06_07_08uL, ports.port1.fetch(0uL, 0, 8))

        assertEquals(0x07_08uL, area1.inw(0uL))
        assertEquals("0807", area1.load(0uL, 2).hexlify())
        assertEquals("06050403", area2.load(2uL, 4).hexlify())
        assertEquals("0201", area3.load(6uL, 2).hexlify())
    }

    @Test fun crossPrimitiveBETest1() {
        Register(ports.port1, 0uL, BYTE, "reg1", 0xDEuL)
        Register(ports.port1, 1uL, BYTE, "reg2", 0xADuL)
        Register(ports.port1, 2uL, BYTE, "reg3", 0xBEuL)
        Register(ports.port1, 3uL, BYTE, "reg4", 0xEFuL)

        ports.port1.connect(buses.bus, endian = ByteOrder.BIG_ENDIAN)
        ports.port2.connect(buses.bus)

        initializeAndResetAsTopInstance()

        assertEquals(0xDE_AD_BE_EFuL, ports.port2.inl(0uL))
        ports.port2.outl(0uL, 0xCA_FE_BA_BEuL)
        assertEquals(0xCA_FE_BA_BEuL, ports.port2.inl(0uL))
        assertEquals("CAFEBABE", ports.port2.load(0uL, 4).hexlify())
    }

    @Test fun crossPrimitiveBETest2() {
        Register(ports.port1, 0uL, WORD, "reg1", 0xDE_ADuL) // In memory: AD DE
        Register(ports.port1, 2uL, WORD, "reg2", 0xBE_EFuL) // In memory: EF BE

        ports.port1.connect(buses.bus, endian = ByteOrder.BIG_ENDIAN)
        ports.port2.connect(buses.bus)

        initializeAndResetAsTopInstance()

        assertEquals(0xAD_DE_EF_BEuL, ports.port2.inl(0uL))
    }

    @Test fun crossPrimitiveBETest3() {
        val area1 = Memory(ports.port1, 0uL, 1uL, "area1", R_W)
        val area2 = Memory(ports.port1, 2uL, 5uL, "area2", R_W)
        val area3 = Memory(ports.port1, 6uL, 7uL, "area3", R_W)

        ports.port1.connect(buses.bus, endian = ByteOrder.BIG_ENDIAN)
        ports.port2.connect(buses.bus)

        initializeAndResetAsTopInstance()

        area1.outw(0uL, 0xDE_ADuL)
        area2.outl(2uL, 0xCA_FE_BA_BEuL)
        area3.outw(6uL, 0xBE_EFuL)

        assertEquals(0xAD_DE_BE_BA_FE_CA_EF_BEuL, ports.port2.inq(0uL))
        ports.port2.outq(0uL, 0x01_02_03_04_05_06_07_08uL)
        assertEquals(0x01_02_03_04_05_06_07_08uL, ports.port2.fetch(0uL, 0, 8))

        assertEquals(0x02_01uL, area1.inw(0uL))
        assertEquals("0102", area1.load(0uL, 2).hexlify())
        assertEquals("03040506", area2.load(2uL, 4).hexlify())
        assertEquals("0708", area3.load(6uL, 2).hexlify())
    }
}
