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
package ru.inforion.lab403.kopycat.examples

import org.junit.jupiter.api.Test
import ru.inforion.lab403.common.swarm.parallelize
import ru.inforion.lab403.common.swarm.threadsSwarm
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.modules.examples.stm32f042_example
import java.io.Serializable
import kotlin.test.assertEquals
import kotlin.test.assertFalse


class DistributedTest {
    @JvmInline
    private value class ULongWrapper(val data: ULong) : Serializable

    @Test
    fun stm32DistributedTest() {
        threadsSwarm(2) { swarm ->
            val result = listOf(10, 20, 30).parallelize(swarm).map {
                val device = stm32f042_example(null, "top", "example:gpiox_led", 0, 0)
                Kopycat(null).apply { open(device, null, false) }.use { kopycat ->
                    repeat(it * 2048) { kopycat.step() }

                    ULongWrapper(kopycat.top.core.pc) to kopycat.hasException()
                }
            }
            val uniquePC = result.map { it.first.data }.toSet().size
            val hasException = result.any { it.second }

            assertFalse(hasException)
            assertEquals(uniquePC, 3)
        }
    }
}