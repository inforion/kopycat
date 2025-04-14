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

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.cores.base.exceptions.BreakpointException
import ru.inforion.lab403.kopycat.interactive.protocols.KopycatRestProtocolUnitTest.Companion.defaultModuleName
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import ru.inforion.lab403.kopycat.modules.cores.device.TestDevice
import kotlin.test.assertEquals

class WatchpointTest {
    private val registry by lazy { ModuleLibraryRegistry.create() }

    private fun test(fn: (Kopycat) -> Unit) = Kopycat(registry).use { kc ->
        val device = TestDevice(null, defaultModuleName)
        kc.open(device, null, false)

        device.debugger.isRunning = true // so that breakpoints are triggered
        fn(kc)
    }

    private fun Kopycat.assertRunning() = assertTrue(debugger.isRunning)
    private fun Kopycat.assertStopped() = assertFalse(debugger.isRunning)

    private fun readWatchpointTest(bpOfft: ULong) = test { kc ->
        kc.bptSet(0x1000uL + bpOfft, "r")

        kc.core.write(0x1000uL, 0, 4, 0xDEADBEEFuL)
        kc.assertRunning()

        assertEquals(0xDEADBEEFuL, kc.core.fetch(0x1000uL, 0, 4))
        kc.assertRunning()

        assertEquals(0xDEADBEEFuL, kc.core.read(0x1000uL, 0, 4))
        kc.assertStopped()
    }

    private fun writeWatchpointTest(bpOfft: ULong) = test { kc ->
        kc.core.write(0x1000uL, 0, 4, 0xDEADBEEFuL)
        kc.assertRunning()

        kc.bptSet(0x1000uL + bpOfft, "w")

        assertEquals(0xDEADBEEFuL, kc.core.fetch(0x1000uL, 0, 4))
        kc.assertRunning()

        assertEquals(0xDEADBEEFuL, kc.core.read(0x1000uL, 0, 4))
        kc.assertRunning()

        kc.core.write(0x1000uL, 0, 4, 0xDEADBEEFuL)
        kc.assertStopped()
    }

    private fun breakpointTest(bpOfft: ULong) = test { kc ->
        kc.bptSet(0x1000uL + bpOfft, "x")

        kc.core.write(0x1000uL, 0, 4, 0xDEADBEEFuL)
        kc.assertRunning()

        assertEquals(0xDEADBEEFuL, kc.core.read(0x1000uL, 0, 4))
        kc.assertRunning()

        assertThrows<BreakpointException> {
            kc.core.fetch(0x1000uL, 0, 4)
        }
    }

    private fun rwxTest(bpOfft: ULong) = test { kc ->
        kc.core.write(0x1000uL, 0, 4, 0xDEADBEEFuL)
        kc.assertRunning()

        kc.bptSet(0x1000uL + bpOfft, "rwx")

        assertEquals(0xDEADBEEFuL, kc.core.read(0x1000uL, 0, 4))
        kc.assertStopped()

        kc.debugger.isRunning = true; kc.assertRunning()

        kc.core.write(0x1000uL, 0, 4, 0xDEADBEEFuL)
        kc.assertStopped()

        kc.debugger.isRunning = true; kc.assertRunning()

        assertThrows<BreakpointException> {
            kc.core.fetch(0x1000uL, 0, 4)
        }
    }

    @Test fun readWatchpointSimple() = readWatchpointTest(0uL)
    @Test fun writeWatchpointSimple() = writeWatchpointTest(0uL)
    @Test fun breakpointSimple() = breakpointTest(0uL)
    @Test fun rwxSimple() = rwxTest(0uL)

    @Test fun readWatchpointMid() = readWatchpointTest(2uL)
    @Test fun writeWatchpointMid() = writeWatchpointTest(2uL)
    @Test fun breakpointMid() = breakpointTest(2uL)
    @Test fun rwxMid() = rwxTest(2uL)
}
