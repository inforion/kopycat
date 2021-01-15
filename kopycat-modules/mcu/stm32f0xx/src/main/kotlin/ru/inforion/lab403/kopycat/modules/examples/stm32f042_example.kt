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
package ru.inforion.lab403.kopycat.modules.examples

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.stm32f042.LED
import ru.inforion.lab403.kopycat.modules.stm32f042.STM32F042
import ru.inforion.lab403.kopycat.modules.terminals.UartSerialTerminal
import java.io.File


@Suppress("PrivatePropertyName")
class stm32f042_example(
        parent: Module?,
        name: String,
        firmware: String,
        tty1: String? = null,
        tty2: String? = null
) : Module(parent, name) {

    companion object {
        const val LEDS_COUNT = 16

        const val EXAMPLE = "example:"
        const val FILE = "file:"
        const val RESOURCE = "resource:"
        const val BYTES = "bytes:"

        private fun createUsingResource(parent: Module, name: String, path: String) = STM32F042(parent, name, Resource(path))
        private fun createUsingFile(parent: Module, name: String, path: String) = STM32F042(parent, name, File(path))
        private fun createUsingBytes(parent: Module, name: String, data: String) = STM32F042(parent, name, data.unhexlify())

        private fun createMCU(parent: Module, name: String, firmware: String) = when {
            firmware.startsWith(EXAMPLE) -> createUsingResource(parent, name, "binaries/${firmware.removePrefix(EXAMPLE)}.bin")
            firmware.startsWith(FILE) -> createUsingFile(parent, name, firmware.removePrefix(FILE))
            firmware.startsWith(BYTES) -> createUsingBytes(parent, name, firmware.removePrefix(BYTES))
            firmware.startsWith(RESOURCE) -> createUsingResource(parent, name, firmware.removePrefix(RESOURCE))
            else -> throw IllegalArgumentException("Wrong firmware prefix should be '$EXAMPLE', '$FILE', '$RESOURCE', '$BYTES', provided: $firmware")
        }
    }

    inner class Buses : ModuleBuses(this) {
        val gpioa_leds = Bus("gpioa_leds", LEDS_COUNT)
    }

    override val buses = Buses()

    val stm32f042 = createMCU(this, "stm32f042", firmware)
    val term1 = UartSerialTerminal(this, "term1", tty1)
    val term2 = UartSerialTerminal(this, "term2", tty2)
    val leds = Array(LEDS_COUNT) { LED(this, "led_$it") }

    fun sendStringIntoUART1(string: String) = term1.write(string)
    fun sendStringIntoUART2(string: String) = term2.write(string)

    init {
        buses.connect(stm32f042.ports.usart1_m, term1.ports.term_s)
        buses.connect(stm32f042.ports.usart1_s, term1.ports.term_m)

        buses.connect(stm32f042.ports.usart2_m, term2.ports.term_s)
        buses.connect(stm32f042.ports.usart2_s, term2.ports.term_m)

        stm32f042.ports.gpioa_out.connect(buses.gpioa_leds)

        leds.forEachIndexed { offset, led -> led.ports.pin.connect(buses.gpioa_leds, offset.asULong) }
    }
}