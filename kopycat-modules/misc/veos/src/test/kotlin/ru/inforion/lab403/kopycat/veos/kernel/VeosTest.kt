/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.veos.kernel

import org.junit.Test
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.cores.base.common.ComponentTracer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.modules.cores.AARMCore
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.modules.cores.MipsDebugger
import ru.inforion.lab403.kopycat.modules.cores.arm1176jzs.ARM1176JZS
import ru.inforion.lab403.kopycat.modules.veos.ARMApplication
import ru.inforion.lab403.kopycat.modules.veos.MIPSApplication
import ru.inforion.lab403.kopycat.modules.veos.x86WindowsApplication
import ru.inforion.lab403.kopycat.veos.UnixVEOS
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIFunction
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class VeosTest {
    companion object {
        val log = logger()

        const val maxStepsCount = 600uL
        const val mipsLastValidAddressValue = 0x00400A8CuL
        const val armLastValidAddressValue = 0x00010798uL
    }

    class TestAPI(os: VEOS<*>) : API(os) {
        val receive = object : APIFunction("receive") {
            override val args = arrayOf(ArgType.Pointer, ArgType.Int)

            override fun exec(name: String, vararg argv: ULong): APIResult {
                val data = "C0BAAA0D".unhexlify() + "Hello world!\n".bytes
                os.abi.writeBytes(argv[0], data)
                return retval(data.size.ulong_z)
            }
        }

        val uart_send_char = object : APIFunction("uart_send_char") {
            override val args = arrayOf(ArgType.Int)

            private val data = mutableListOf<Byte>()

            override fun exec(name: String, vararg argv: ULong): APIResult {

                if (argv[0].char == '\n') {
                    log.warning { data.toByteArray().string }
                    data.clear()
                } else {
                    data.add(argv[0].byte)
                }

                return void()
            }
        }
    }

    class TestMipsUnixApp(rootDirectory: String) : Module(null, "top") {
        inner class Buses : ModuleBuses(this) {
            val mem = Bus("mem")
        }

        override val buses = Buses()

        val mips = MipsCore(
                this,
                "mips",
                useMMU = false,
                frequency = 50.MHz,
                multiplier = 9,
                ArchitectureRevision = 1,
                countOfShadowGPR = 0,
                ipc = 1.0,
                PABITS = 32,
                PRId = 0x55ABCC01u) // PRId from my imagination

        val veos = UnixVEOS<MipsCore>(this, "veos").apply {
            conf.rootDirectory = rootDirectory
        }
        val dbg = MipsDebugger(this, "dbg")
        val trc = ComponentTracer<MipsCore>(this, "trc")

        fun exec(path: String) {
            veos.addApi(TestAPI(veos))
            veos.initProcess(path)
        }

        init {
            mips.ports.mem.connect(buses.mem)
            veos.ports.mem.connect(buses.mem)
            dbg.ports.reader.connect(buses.mem)
            dbg.ports.breakpoint.connect(buses.mem)

            buses.connect(dbg.ports.trace, trc.ports.trace)

            trc.addTracer(veos)
        }
    }

    class TestARMUnixApp(rootDirectory: String) : Module(null, "top") {
        inner class Buses : ModuleBuses(this) {
            val mem = Bus("mem")
        }

        override val buses = Buses()

        val arm = ARM1176JZS(
                this,
                "arm",
                frequency = 50.MHz,
                ipc = 1.0,
        )

        val veos = UnixVEOS<AARMCore>(this, "veos").apply {
            conf.rootDirectory = rootDirectory
        }
        val dbg = MipsDebugger(this, "dbg")
        val trc = ComponentTracer<AARMCore>(this, "trc")

        fun exec(path: String) {
            veos.addApi(TestAPI(veos))
            veos.initProcess(path)
        }


        init {
            arm.ports.mem.connect(buses.mem)
            veos.ports.mem.connect(buses.mem)
            dbg.ports.reader.connect(buses.mem)
            dbg.ports.breakpoint.connect(buses.mem)

            buses.connect(dbg.ports.trace, trc.ports.trace)

            trc.addTracer(veos)
        }
    }

    @Test
    fun simpleMipsMemoVEOSTestWithHook() {
        val executable = "memo-mips.elf"
        val root = VeosTest.getResourceUrl(executable).toURI().resolve(".").path
        val top = TestMipsUnixApp(root)
        val kopycat = Kopycat(null).also { it.open(top, null, false) }

        top.exec(executable)

        var lastValidAddressFound = false

        kopycat.run { step, core ->
            val where = core.cpu.pc
            if (where == mipsLastValidAddressValue)
                lastValidAddressFound = true
            step < maxStepsCount
        }

        assertFalse { kopycat.hasException() }
        assertTrue { lastValidAddressFound }
        assertTrue { top.veos.state == VEOS.State.Exit }
    }

    @Test
    fun simpleMipsMemoVEOSTestWithSteps() {
        val executable = "memo-mips.elf"
        val root = VeosTest.getResourceUrl(executable).toURI().resolve(".").path
        val top = TestMipsUnixApp(root)
        val kopycat = Kopycat(null).also { it.open(top, null, false) }

        top.exec(executable)

        var lastValidAddressFound = false

        collect(maxStepsCount.int).first {
            val where = kopycat.pcRead()

            if (where == mipsLastValidAddressValue)
                lastValidAddressFound = true

            !kopycat.step()
        }

        assertFalse { kopycat.hasException() }
        assertTrue { lastValidAddressFound }
        assertTrue { top.veos.state == VEOS.State.Exit }
    }


    @Test
    fun simpleARMMemoVEOSTestWithHook() {
        val executable = "memo-arm.elf"
        val root = VeosTest.getResourceUrl(executable).toURI().resolve(".").path
        val top = TestARMUnixApp(root)
        val kopycat = Kopycat(null).also { it.open(top, null, false) }

        top.exec(executable)

        var lastValidAddressFound = false

        kopycat.run { step, core ->
            val where = core.cpu.pc
            if (where == armLastValidAddressValue)
                lastValidAddressFound = true
            step < maxStepsCount
        }

        assertFalse { kopycat.hasException() }
        assertTrue { lastValidAddressFound }
        assertTrue { top.veos.state == VEOS.State.Exit }
    }

    @Test
    fun simpleARMMemoVEOSTestWithSteps() {
        val executable = "memo-arm.elf"
        val root = VeosTest.getResourceUrl(executable).toURI().resolve(".").path
        val top = TestARMUnixApp(root)
        val kopycat = Kopycat(null).also { it.open(top, null, false) }

        top.exec(executable)

        var lastValidAddressFound = false

        collect(maxStepsCount.int).first {
            val where = kopycat.pcRead()

            if (where == armLastValidAddressValue)
                lastValidAddressFound = true

            !kopycat.step()
        }

        assertFalse { kopycat.hasException() }
        assertTrue { lastValidAddressFound }
        assertTrue { top.veos.state == VEOS.State.Exit }
    }

    @Test
    fun pthreadVEOStestARM() {
        val executable = "pthreads.arm"
        val root = VeosTest.getResourceUrl(executable).toURI().resolve(".").path

        val top = ARMApplication(null, "top", root, executable)
        val kopycat = Kopycat(null).also { it.open(top, null, false) }

        val resultAddress = 0x00021074uL

        kopycat.run { step, core ->
            step < 1000uL
        }

        repeat(10) {i ->
            val data = kopycat.memRead(resultAddress + i*4, 4)
            assertEquals(1uL, data)
        }

        assertFalse { kopycat.hasException() }
        assertTrue { top.veos.state == VEOS.State.Exit }
    }

    @Test
    fun pthreadVEOStestMIPS() {
        val executable = "pthreads.mips"
        val root = VeosTest.getResourceUrl(executable).toURI().resolve(".").path

        val top = MIPSApplication(null, "top", root, executable)
        val kopycat = Kopycat(null).also { it.open(top, null, false) }

        val resultAddress = 0x00410A88uL

        kopycat.run { step, core ->
            step < 1000u
        }

        repeat(10) {i ->
            val data = kopycat.memRead(resultAddress + i*4, 4)
            assertEquals(1uL, data)
        }

        assertFalse { kopycat.hasException() }
        assertTrue { top.veos.state == VEOS.State.Exit }
    }

    @Test
    fun helloworldVeosTestWindowsX86() {
        val executable = "helloworld.exe"
        val root = VeosTest.getResourceUrl(executable).toURI().resolve(".").path

        val top = x86WindowsApplication(null, "top", root, executable)
        val kopycat = Kopycat(null).also { it.open(top, null, false) }

        kopycat.run { step, core -> step < 1000u }
        assertFalse { kopycat.hasException() }
        assertTrue { top.veos.state == VEOS.State.Exit }
    }
}