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
package ru.inforion.lab403.kopycat.cores.mips.integration

import org.junit.Test
import ru.inforion.lab403.common.extensions.DynamicClassLoader
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.auxiliary.PerformanceTester
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.mips.enums.GPR
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import ru.inforion.lab403.kopycat.modules.tests.TestMipsDevice
import ru.inforion.lab403.kopycat.modules.tests.TestMipsDeviceNoProxy
import java.util.logging.Level


class MipsSimplePerformanceTest {
    private val fakeCount = 4
    private val entryPoint = 0x800002F8L
    private val exitPoint = 0x800003E4L
    private val stackStart = 0x97000000L
    private val registry = ModuleLibraryRegistry.create()

    private fun test(top: () -> Module) {
//        ModuleLibraryRegistry.log.level = Level.WARNING
//        Module.log.level = Level.SEVERE
//        Kopycat.log.level = Level.SEVERE

        PerformanceTester(exitPoint) {
            top()
        }.afterReset {
            it.core.reg(GPR.SP.id, stackStart)
            it.core.pc = entryPoint
        }.apply { run(5, 1) }
    }

    @Test
    fun memcpyTestProxyNoJson() = test { TestMipsDevice(null, "top", 0, fakeCount) }

    @Test
    fun memcpyTestNoProxyNoJson() = test { TestMipsDeviceNoProxy(null, "top", 0, fakeCount) }

    @Test
    fun idleTestProxyNoJson() = test { TestMipsDevice(null, "top", 1, fakeCount) }

    @Test
    fun idleTestNoProxyNoJson() = test { TestMipsDeviceNoProxy(null, "top", 1, fakeCount) }

    private val testMipsDeviceJson = DynamicClassLoader.getResourceAsStream("modules/TestMipsDeviceJson.json")
    private val testMipsDeviceNoProxyJson = DynamicClassLoader.getResourceAsStream("modules/TestMipsDeviceNoProxyJson.json")

    @Test
    fun memcpyTestProxyJson() = test {
        registry.json(null, testMipsDeviceJson, "top", 0, fakeCount)
    }

    @Test
    fun memcpyTestNoProxyJson() = test {
        registry.json(null, testMipsDeviceNoProxyJson, "top", 0, fakeCount)
    }

    @Test
    fun idleTestProxyJson() = test {
        registry.json(null, testMipsDeviceJson, "top", 1, fakeCount)
    }

    @Test
    fun idleTestNoProxyJson() = test {
        registry.json(null, testMipsDeviceNoProxyJson, "top", 1, fakeCount)
    }
}