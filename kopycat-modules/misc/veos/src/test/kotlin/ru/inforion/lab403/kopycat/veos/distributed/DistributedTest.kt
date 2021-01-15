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
package ru.inforion.lab403.kopycat.veos.distributed

import org.junit.Test
import ru.inforion.lab403.common.extensions.getResourceUrl
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.modules.veos.ARMApplication
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.swarm.parallelize
import ru.inforion.lab403.swarm.threadsSwarm
import kotlin.test.assertFalse


class DistributedTest {
    // TODO: move to extension
    // Do not convert it into property - getResourceUrl is caller-sensitive
    inline fun containingDirectory(filename: String): String = getResourceUrl(filename).toURI().resolve(".").path

    @Test
    fun ARMApplicationDistributedTest() {
        val executable = "arm-linux-gnueabi-readelf"
        val root = containingDirectory(executable)

        threadsSwarm(2) { swarm ->

            val device = ARMApplication(null, "top", root, executable, "-S arm-linux-gnueabi-as")
            Kopycat(null).apply { open(device, false, null) }

            val result = listOf("arm-linux-gnueabi-as", "arm-linux-gnueabi-objdump", "arm-linux-gnueabi-ranlib").parallelize(swarm).map {

                val kopycat = Kopycat(null).apply { open(device, false, null) }

                // REVIEW: stack cleanup and api reinit causes memory leaks
                device.veos.loader.loadArguments(arrayOf(executable, "-S", it))

                kopycat.run { step, core -> true }

                kopycat.hasException() to (kopycat.top as ARMApplication).veos.state
            }

            val veosExitFailed = result.any { it.second != VEOS.State.Exit }
            val hasException = result.any { it.first }

            assertFalse(veosExitFailed)
            assertFalse(hasException)
        }
    }

}