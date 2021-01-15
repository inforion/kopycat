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
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.modules.virtarm.VirtARM

class VirtARMSerializationTest {
    companion object {
        @Transient val log = logger()
    }

    private fun execute() {
        val exitPoint = 0xAFFCC7C0

        val kopycat = Kopycat(null).apply {
            val top = VirtARM(null, "top")
            setSnapshotsDirectory("temp")
            open(top, false, null)
        }

        kopycat.reset()

        kopycat.save("VirtARMSerializationTest.zip")
        kopycat.run { _, core -> core.pc != exitPoint }
        assert(!kopycat.hasException()) { "fault = ${kopycat.exception()}" }

        // This part call true restore
        kopycat.restore()
        kopycat.run { _, core -> core.pc != exitPoint }
        assert(!kopycat.hasException()) { "fault = ${kopycat.exception()}" }

        kopycat.load("VirtARMSerializationTest.zip")
        kopycat.run { _, core -> core.pc != exitPoint }
        assert(!kopycat.hasException()) { "fault = ${kopycat.exception()}" }

        kopycat.save("VirtARMSerializationTest_TLB.zip")
        kopycat.load("VirtARMSerializationTest_TLB.zip")
        kopycat.load("VirtARMSerializationTest.zip")

        kopycat.run { step, core -> core.pc != exitPoint }
        assert(!kopycat.hasException()) { "fault = ${kopycat.exception()}" }
    }

    @Test
    fun virtARMSerializationTest() = execute()
}