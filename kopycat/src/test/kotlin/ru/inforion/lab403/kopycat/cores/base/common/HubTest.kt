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
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.modules.cores.device.TestCore
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.common.Hub



class HubTest : Module(null, "Test hub device") {
    companion object {
        private const val VALUE = 0xDEADBEAFuL
    }

    class ModulePeriph(parent: Module, name: String, registers: ArrayList<ULong>, areas: ArrayList<ULongRange>) :
            Module(parent, name) {
        inner class Ports : ModulePorts(this) {
            val mem = Slave("mem")
        }
        override val ports = Ports()

        init {
            registers.forEach { address ->
                Register(ports.mem, address, Datatype.DWORD, "Register_${address.hex}")
            }
            areas.forEach { range ->
                Memory(ports.mem, range.first, range.last, "Memory_$range", ACCESS.R_W)
            }
        }
    }

    inner class Buses : ModuleBuses(this) {
        val busMaster = Bus("mater")
        val busSlave1 = Bus("slave1")
        val busSlave2 = Bus("slave2")
    }
    override val buses = Buses()

    private val master = TestCore(this, "Test core")

    private val device1 = ModulePeriph(this,
            "device 1",
            arrayListOf(0x0000u, 0x0010u, 0x0020u),
            arrayListOf(0x1000uL..0x2000uL, 0x6000uL..0x8000uL))

    private val device2 = ModulePeriph(this,
            "device 2",
            arrayListOf(0x0000u, 0x0018u, 0x0030u),
            arrayListOf(0x1000uL..0x3000uL, 0x8000uL..0xA000uL))

    private val hub = Hub(this, "hub", "out0" to BUS32, "out1" to BUS32)

    init {
        master.ports.mem.connect(buses.busMaster)
        hub.ports.input.connect(buses.busMaster)

        device1.ports.mem.connect(buses.busSlave1)
        hub.ports.outputs[0].connect(buses.busSlave1)

        device2.ports.mem.connect(buses.busSlave2)
        hub.ports.outputs[1].connect(buses.busSlave2)

        initializeAndResetAsTopInstance()
    }

    @Test
    fun writeTo2Registers() {
        master.cpu.ports.mem.write(0u, 0, 4, VALUE)
        Assert.assertEquals(hub.ports.outputs[0].read(0u, 0, 4), VALUE)
        Assert.assertEquals(hub.ports.outputs[1].read(0u, 0, 4), VALUE)
    }

    @Test
    fun writeTo1Registers() {
        master.cpu.ports.mem.write(0x10u, 0, 4, VALUE)
        Assert.assertEquals(hub.ports.outputs[0].read(0x10u, 0, 4), VALUE)
    }

    @Test(expected = MemoryAccessError::class)
    fun writeTo0Registers() {
        master.cpu.ports.mem.write(0x50u, 0, 4, VALUE)
    }

    @Test
    fun writeTo2Areas() {
        master.cpu.ports.mem.write(0x1500u, 0, 4, VALUE)
        Assert.assertEquals(hub.ports.outputs[0].read(0x1500u, 0, 4), VALUE)
        Assert.assertEquals(hub.ports.outputs[1].read(0x1500u, 0, 4), VALUE)
    }

    @Test
    fun writeTo1Areas() {
        master.cpu.ports.mem.write(0x7000u, 0, 4, VALUE)
        Assert.assertEquals(hub.ports.outputs[0].read(0x7000u, 0, 4), VALUE)
    }

    @Test(expected = MemoryAccessError::class)
    fun writeTo0Areas() {
        master.cpu.ports.mem.write(0xB000u, 0, 4, VALUE)
    }

    @Test(expected = MemoryAccessError::class)
    fun readFromFrom2Registers() {

        hub.ports.outputs[0].write(0x10u, 0, 4, VALUE)
        hub.ports.outputs[1].write(0x10u, 0, 4, VALUE)
        val data = master.cpu.ports.mem.read(0x10u, 0, 4)
        Assert.assertEquals(data, VALUE)
    }

    @Test
    fun readFromFrom1Registers() {
        hub.ports.outputs[0].write(0x10u, 0, 4, VALUE)
        val data = master.cpu.ports.mem.read(0x10u, 0, 4)
        Assert.assertEquals(data, VALUE)
    }

    @Test(expected = MemoryAccessError::class)
    fun readFromFrom0Registers() {
        master.cpu.ports.mem.read(0x50u, 0, 4)
    }
}