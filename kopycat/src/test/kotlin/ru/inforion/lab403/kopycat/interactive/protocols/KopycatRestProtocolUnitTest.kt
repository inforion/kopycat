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
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.interactive.rest.KopycatClient
import ru.inforion.lab403.kopycat.interactive.rest.RegistryClient
import ru.inforion.lab403.kopycat.library.builders.text.ModuleConfig
import ru.inforion.lab403.kopycat.modules.cores.device.TestCore
import ru.inforion.lab403.kopycat.modules.cores.device.TestDebugger
import ru.inforion.lab403.kopycat.modules.cores.device.TestDevice
import ru.inforion.lab403.kopycat.modules.cores.device.instructions.INSN
import ru.inforion.lab403.kopycat.modules.memory.RAM
import kotlin.test.assertFailsWith

class KopycatRestProtocolUnitTest {

    companion object {
        val log = logger()

        const val defaultModuleName = "top"
        const val host = "localhost"
        const val port = 18328
    }

    private val kopycatClient = KopycatClient(host, port)
    private val registryClient = RegistryClient(host, port)

    private fun makeTestDevice(kopycat: Kopycat, program: ByteArray? = null) = with (kopycat) {
        val device = TestDevice(null, defaultModuleName)
        open(device, false, null)
        val actual = program ?: TestCore.program(
                INSN.ADD, // R2 = 5
                INSN.MOV, // R0 = 2
                INSN.MUL, // R2 = 4
                INSN.SUB  // R2 = 0
        )
        core.store(0x1000, actual)
        core.cpu.reg(0, 0x3)
        core.cpu.reg(1, 0x2)
        core.cpu.pc = 0x1000
        device
    }

    @Test
    fun postModuleTest() = withKopycatRest { _, modules ->
        assertEquals(registryClient.module.create(null, defaultModuleName), defaultModuleName)
        assertTrue(modules.any { it.name == defaultModuleName })
    }

    @Test
    fun deleteModuleTest() = withKopycatRest { _, modules ->
        val device = TestDevice(null, defaultModuleName)
        modules.add(device)

        val childModule = Module(device, "childModule")
        modules.add(childModule)

        registryClient.module.delete(device.name, false)

        assertFalse(modules.any { it.name == device.name })
        assertTrue(modules.any { it.name == childModule.name })

    }

    @Test
    fun deleteModuleTest2() = withKopycatRest { _, modules ->
        val device = TestDevice(null, defaultModuleName)
        modules.add(device)

        val childModule = Module(device, "childModule")
        modules.add(childModule)

        registryClient.module.delete(device.name, true)

        assertFalse(modules.any { it.name == device.name })
        assertFalse(modules.any { it.name == childModule.name })
    }

    @Test
    fun deleteModuleTest3() = withKopycatRest { kopycat, modules ->
        // Checking to conditions at once if child module have multiple children
        // and different ways of modules inheritance
        val device = TestDevice(null, defaultModuleName)
        modules.add(device)

        val childModule1 = Module(device, "childModule1")
        modules.add(childModule1)

        // childModule2 is child for childModule1
        val childModule2 = Module(childModule1, "childModule2")
        modules.add(childModule2)

        // childModule3 is child for childModule1
        val parent = modules.find { m -> m.name == defaultModuleName }
        val childModule3 = ModuleConfig("childModule3", "TestCore", "cores", mapOf("frequency" to 4), null).create(kopycat.registry!!, parent)
        modules.add(childModule3)

        registryClient.module.delete(device.name, true)

        assertFalse(modules.any { it.name == device.name })
        assertFalse(modules.any { it.name == childModule1.name })
        assertFalse(modules.any { it.name == childModule2.name })
        assertFalse(modules.any { it.name == childModule3.name })
    }

    @Test
    fun instantiateTest() = withKopycatRest { _, modules ->
        val device = Module(null, defaultModuleName)
        modules.add(device)

        val instantiateCoreResponse = registryClient.instantiate(defaultModuleName, "test", "TestCore", "cores", mapOf("frequency" to 4))
        assertEquals(instantiateCoreResponse, "test")

        val core = device.getComponentsByInstanceName("test").first() as TestCore
        assertEquals("$defaultModuleName.test", core.toString())
        assertEquals(4, core.frequency)

        val instantiateDbgResponse = registryClient.instantiate(defaultModuleName, "dbg", "TestDebugger", "cores")
        assertEquals(instantiateDbgResponse, "dbg")
        val dbg = device.getComponentsByInstanceName("dbg").first() as TestDebugger
        assertEquals("$defaultModuleName.dbg", dbg.toString())

        val instantiateRamResponse = registryClient.instantiate(defaultModuleName, "ram", "RAM", "memory", mapOf("size" to 0x01000000))
        assertEquals(instantiateRamResponse, "ram")
        val ram = device.getComponentsByInstanceName("ram").first() as RAM
        assertEquals("$defaultModuleName.ram", ram.toString())

    }

    @Test
    fun instantiateFailureTest() = withKopycatRest { _, _ ->
        log.warning { "This test should fail... any exception on server side is ok!" }
        assertFailsWith<IllegalStateException> {
            registryClient.instantiate("test", "dbg", "TestDebugger", "cores")
        }
    }

    @Test
    fun busTest() = withKopycatRest { _, modules ->
        val device = Module(null, defaultModuleName)
        modules.add(device)
        val busResponse = kopycatClient.bus(defaultModuleName, "mem", "BUS32")
        assertEquals(busResponse, "mem")
        assertEquals("$defaultModuleName:mem[Bx====]", device.buses[busResponse].toString())
    }

    @Test
    fun portTest() = withKopycatRest { _, modules ->
        val device = Module(null, defaultModuleName)
        modules.add(device)
        val port = kopycatClient.port(defaultModuleName, "port", "Master", "BUS16")
        assertEquals("port", port)
        assertEquals("$defaultModuleName:port[Mx00010000]", device.ports[port].toString())
    }

    @Test
    fun stepTest() = withKopycatRest { kopycat, _ ->
        makeTestDevice(kopycat)

        // stepping by the program defined in 'configureKopycat' method
        assertTrue(kopycatClient.step())
        assertEquals(0x5, kopycat.core.cpu.reg(2))

        assertTrue(kopycatClient.step())
        assertEquals(0x2, kopycat.core.cpu.reg(1))

        assertTrue(kopycatClient.step())
        assertEquals(0x4, kopycat.core.cpu.reg(2))

        assertTrue(kopycatClient.step())
        assertEquals(0x0, kopycat.core.cpu.reg(2))

        // Because in program only 4 instructions, 5th should return False
        assertFalse(kopycatClient.step())
    }

    private fun Kopycat.waitAndAssertRunning(state: Boolean, delay: Long = 100, retries: Int = 10) {
        var retry = 0
        while (isRunning != state && ++retry < retries) {
            log.config { "Waiting Kopycat state = $state retry = $retry/$retries" }
            Thread.sleep(delay)
        }
        assertEquals(state, isRunning)
    }

    @Test
    fun startTest() = withKopycatRest { kopycat, _ ->
        makeTestDevice(kopycat, TestCore.program(INSN.INF))
        kopycatClient.start()
        kopycat.waitAndAssertRunning(true)
        kopycat.halt()
        kopycat.waitAndAssertRunning(false)
    }

    @Test
    fun haltTest() = withKopycatRest { kopycat, _ ->
        makeTestDevice(kopycat, TestCore.program(INSN.INF))
        kopycat.start()
        kopycat.waitAndAssertRunning(true)
        kopycatClient.halt()
        kopycat.waitAndAssertRunning(false)
    }

    @Test
    fun testMemLoad() = withKopycatRest { kopycat, _ ->
        makeTestDevice(kopycat)

        assertEquals(kopycat.memLoad(0x1000, 16, 0).hexlify(), kopycatClient.memLoad(0x1000, 16, 0))
        assertEquals(kopycat.memLoad(0x1000, 18, 0).hexlify(), kopycatClient.memLoad(0x1000, 18, 0))
        assertEquals(kopycat.memLoad(0xFFF, 18, 0).hexlify(), kopycatClient.memLoad(0xFFF, 18, 0))
    }

    @Test
    fun testMemStore() = withKopycatRest { kopycat, _ ->
        makeTestDevice(kopycat)

        kopycatClient.memStore(0x1000, "0F105ACD00351ABDEA", 0)
        assertEquals(kopycat.memLoad(0x1000, "0F105ACD00351ABDEA".length / 2, 0).hexlify(), "0F105ACD00351ABDEA")
        kopycatClient.memStore(0x1000, "00000000", 0)
        assertEquals(kopycat.memLoad(0x1000, "00000000".length / 2, 0).hexlify(), "00000000")
        kopycatClient.memStore(0x1000, "", 0)
        assertEquals(kopycat.memLoad(0x1000, 0, 0).hexlify(), "")
    }

    @Test
    fun regReadTest() = withKopycatRest { kopycat, _ ->
        makeTestDevice(kopycat)

        assertEquals(kopycat.core.cpu.reg(0), kopycatClient.regRead(0))
        assertEquals(kopycat.core.cpu.reg(1), kopycatClient.regRead(1))
        kopycat.core.cpu.reg(2, 0xBEEF)
        assertEquals(kopycat.core.cpu.reg(2), kopycatClient.regRead(2))
    }

    @Test
    fun regWriteTest() = withKopycatRest { kopycat, _ ->
        makeTestDevice(kopycat)

        kopycatClient.regWrite(0, 0xABCD)
        assertEquals(0xABCD, kopycat.core.cpu.reg(0))

        kopycatClient.regWrite(1, 0xFFFF)
        assertEquals(0xFFFF, kopycat.core.cpu.reg(1))

        kopycatClient.regWrite(2, 0xBEEF)
        assertEquals(0xBEEF, kopycat.core.cpu.reg(2))
    }

    @Test
    fun pcReadTest() = withKopycatRest { kopycat, _ ->
        makeTestDevice(kopycat)

        assertEquals(kopycat.core.cpu.pc, kopycatClient.pcRead())
        kopycat.core.pc = 0xBEEF
        assertEquals(kopycat.core.cpu.pc, kopycatClient.pcRead())
    }

    @Test
    fun pcWriteTest() = withKopycatRest { kopycat, _ ->
        makeTestDevice(kopycat)

        kopycatClient.pcWrite(0xABCD)
        assertEquals(0xABCD, kopycat.core.cpu.pc)

        kopycatClient.pcWrite(0xFFFF)
        assertEquals(0xFFFF, kopycat.core.cpu.pc)

        kopycatClient.pcWrite(0)
        assertEquals(0, kopycat.core.cpu.pc)
    }

    @Test
    fun saveSnapshotTest() = withKopycatRest { kopycat, _ ->
        makeTestDevice(kopycat)
        kopycatClient.save("snapshotUnitTest")
        // Changes default values to check correct serialize/deserialize behavior
        kopycat.pcWrite(0x0)
        kopycat.regWrite(1, 0x100)
        kopycat.load("snapshotUnitTest.zip")
        assertEquals(kopycat.core.cpu.pc, 0x1000)
        assertEquals(kopycat.core.cpu.reg(1), 0x2)
    }

    @Test
    fun loadSnapshotTest() = withKopycatRest { kopycat, _ ->
        makeTestDevice(kopycat)
        kopycat.save("snapshotUnitTest")
        // Changes default values to check correct serialize/deserialize behavior
        kopycat.pcWrite(0x0)
        kopycat.regWrite(1, 0x100)
        kopycatClient.load("snapshotUnitTest.zip")
        assertEquals(kopycat.core.cpu.pc, 0x1000)
        assertEquals(kopycat.core.cpu.reg(1), 0x2)
    }

    @Test
    fun resetTest() = withKopycatRest { kopycat, _ ->
        makeTestDevice(kopycat)
        kopycatClient.reset()
        assertEquals(kopycat.core.cpu.pc, 0x0)
        assertEquals(kopycat.core.cpu.reg(0), 0x0)
        assertEquals(kopycat.memLoad(0x1000, 8, 0).hexlify(), "0000000000000000")
    }

    @Test
    fun closeTest() = withKopycatRest { kopycat, _ ->
        kopycatClient.close()
        assertFalse(kopycat.isTopModulePresented)
        assertFalse(kopycat.isGdbServerPresented)
        assertFalse(kopycat.isSerializerPresented)
    }

    @Test
    fun exitTest() = withKopycatRest { kopycat, _ ->
        kopycatClient.exit()
        assertFalse(kopycat.isTopModulePresented)
        assertFalse(kopycat.isGdbServerPresented)
        assertFalse(kopycat.isSerializerPresented)
        assertFalse(kopycat.working)
    }

    @Test
    fun metaInfoTest() = withKopycatRest { kopycat, _ ->
        makeTestDevice(kopycat)
        kopycatClient.save("snapshotUnitTest", "test comment")
        val meta = kopycatClient.getSnapshotMetaInfo("snapshotUnitTest.zip")
        assertEquals(meta.comment, "test comment")
        assertEquals(meta.entry, 0x1000)
    }

    @Test
    fun openTest() = withKopycatRest { kopycat, _ ->
        registryClient.module.create(null, defaultModuleName)
        registryClient.instantiate(defaultModuleName, "core", "TestCore", "cores", mapOf("frequency" to 1))
        registryClient.instantiate(defaultModuleName, "dbg", "TestDebugger", "cores")
        kopycatClient.open(defaultModuleName, 9999, gdbBinaryProto = false, traceable = false)
        assertTrue(kopycat.isTopModulePresented)
        assertTrue(kopycat.isGdbServerPresented)
        assertTrue(kopycat.working)
    }
}