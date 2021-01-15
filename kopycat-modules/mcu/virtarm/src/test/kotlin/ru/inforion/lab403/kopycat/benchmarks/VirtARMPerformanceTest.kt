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
package ru.inforion.lab403.kopycat.benchmarks

import org.junit.Test
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.auxiliary.PerformanceTester
import ru.inforion.lab403.kopycat.modules.virtarm.VirtARM


class VirtARMPerformanceTest {
    companion object {
        @Transient val log = logger(FINE)
    }

    @Test
    fun ubootPerformanceFast() {
        PerformanceTester(0xAFFCC7C0) { VirtARM(null, "top") }.run(3, 1)
    }

    @Test
    fun linuxPerformance() {
        val tester = PerformanceTester(-1, 150_000_000) { VirtARM(null, "top") }
        tester.stopWhenTerminalReceive(tester.top.term.socat!!.pty1, "buildroot login:").run(1, 0)
//        val top = VirtARM(null, "top")
//        val kopycat = Kopycat(null).apply { open(top, false, GDBServer(6666, true, false)) }
//        kopycat.bptSet(0xC001FEA8, "rx")
//        kopycat.debugger.cont()
//        log.warning { "connect debugger..." }
//        kopycat.gdb.join()
    }
}