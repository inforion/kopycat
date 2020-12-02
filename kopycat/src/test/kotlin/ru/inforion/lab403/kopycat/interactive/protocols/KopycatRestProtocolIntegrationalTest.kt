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
package ru.inforion.lab403.kopycat.interactive.protocols

import org.junit.Assert.*
import org.junit.Test
import ru.inforion.lab403.common.extensions.hexlify
import ru.inforion.lab403.kopycat.interactive.rest.KopycatClient
import ru.inforion.lab403.kopycat.interactive.rest.RegistryClient
import ru.inforion.lab403.kopycat.modules.cores.device.instructions.INSN
import ru.inforion.lab403.kopycat.modules.cores.device.TestCore
import ru.inforion.lab403.kopycat.modules.cores.device.TestDebugger
import ru.inforion.lab403.kopycat.modules.memory.RAM


class KopycatRestProtocolIntegrationalTest {
    companion object {
        const val defaultModuleName = "top"
        const val host = "localhost"
        const val port = 18328
    }

    private val client = KopycatClient(host, port)
    private val registry = RegistryClient(host, port)

    @Test
    fun kopycatDefaultScenarioTest() = withKopycatRest { kopycat, modules ->

        val moduleResponse = registry.module.create(null, defaultModuleName)
        assertEquals(defaultModuleName, modules.first { it.name == moduleResponse }.name)

        val top = modules.first { it.name == defaultModuleName }

        val instantiateCoreResponse = registry.instantiate(defaultModuleName, "test", "TestCore", "cores", mapOf("frequency" to 40000))
        assertEquals(instantiateCoreResponse, "test")
        val core = top.getComponentsByInstanceName("test").first() as TestCore
        assertEquals("$defaultModuleName.test", core.toString())
        assertEquals(40000, core.frequency)

        val instantiateDbgResponse = registry.instantiate(defaultModuleName, "dbg", "TestDebugger", "cores")
        assertEquals(instantiateDbgResponse, "dbg")
        val dbg = top.getComponentsByInstanceName("dbg").first() as TestDebugger
        assertEquals("$defaultModuleName.dbg", dbg.toString())

        val instantiateRamResponse = registry.instantiate(defaultModuleName, "ram", "RAM", "memory", mapOf("size" to 0x01000000))
        assertEquals(instantiateRamResponse, "ram")
        val ram = top.getComponentsByInstanceName("ram").first() as RAM
        assertEquals("$defaultModuleName.ram", ram.toString())

        val busResponse = client.bus(defaultModuleName, "mem", "BUS32")
        assertEquals(busResponse, "mem")
        assertEquals("$defaultModuleName:mem[Bx====]", top.buses["mem"].toString())

        client.connect(defaultModuleName, "dbg.ports.breakpoint", "test.buses.mem", 0)
        client.connect(defaultModuleName, "dbg.ports.reader", "test.buses.mem", 0)
        client.connect(defaultModuleName, "test.ports.mem", "buses.mem", 0)
        client.connect(defaultModuleName, "ram.ports.mem", "buses.mem", 0)

        client.open(defaultModuleName, 6553, gdbBinaryProto = false, traceable = false)
        assert(kopycat.isTopModulePresented)
        assert(kopycat.isGdbServerPresented)

        val code = TestCore.program(
                INSN.ADD, // R2 = 6
                INSN.MOV, // R0 = 2
                INSN.MUL, // R2 = 4
                INSN.SUB  // R2 = 0
        ).hexlify()
        kopycat.core.cpu.reg(0, 0x4)
        kopycat.core.cpu.reg(1, 0x2)
        kopycat.core.cpu.pc = 0x1234

        client.memStore(0x1000L, code, 0)
        assertEquals(code, kopycat.memLoad(0x1000L, code.length / 2, 0).hexlify())

        val memLoadResponse = client.memLoad(0x1000, 8, 0)
        assertEquals(code.substring(0, 16), memLoadResponse)

        val regResponse = client.regRead(2)
        assertEquals(regResponse, 0x0)

        val pcResponse = client.pcRead()
        assertEquals(pcResponse, 0x1234)

        client.pcWrite(0x1000)
        assertEquals(kopycat.core.pc, 0x1000)

        assertTrue(client.step())
        assertTrue(client.step())
        assertTrue(client.step())
        assertTrue(client.step())
        assertFalse(client.step())

        val pc2Response = client.pcRead()
        assertEquals(pc2Response, 0x1010)

        val reg2Response = client.regRead(2)
        assertEquals(reg2Response, 0)

    }
}