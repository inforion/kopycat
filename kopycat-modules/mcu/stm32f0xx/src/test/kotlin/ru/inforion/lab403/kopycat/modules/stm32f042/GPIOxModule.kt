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
package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.extensions.hex4
import ru.inforion.lab403.common.logging.ALL
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.modules.BUS04

class GPIOxModule(val register: GPIOx.RegisterType) : Module(null, "GPIOx_test_module") {
    companion object {
        @Transient private val log = logger(ALL)
    }

    inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem")
    }

    override val buses = Buses()

    class SimpleControllerTestModule(parent: Module?) : Module(parent, "controller_test_module") {
        inner class Ports : ModulePorts(this) {
            val mem = Port("mem")
            val pin_input = Port("pin_input")
            val pin_output = Port("pin_output")
        }

        override val ports = Ports()
    }

    val controller = SimpleControllerTestModule(this)
    val gpio = GPIOx(this, "gpio", 1)

    val pins = object : Area(controller.ports.pin_output, 0u, BUS04 - 1u, "GPIO_INPUT", ACCESS.I_W) {
        override fun fetch(ea: ULong, ss: Int, size: Int): ULong = TODO("not implemented... never be")
        override fun read(ea: ULong, ss: Int, size: Int): ULong = 0uL
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            log.info { "gpio send output signal [${value.hex4}]" }
        }
    }

    fun memWrite(ea: ULong, value: ULong) = controller.ports.mem.write(ea, 0, 0, value)
    fun memRead(ea: ULong) = controller.ports.mem.read(ea, 0, 0)

    fun ioWrite(value: ULong) = controller.ports.pin_input.write(0u, 0, 0, value)

    fun writeLockValues() {
        memWrite(GPIOx.RegisterType.LCKR.offset, 0b0000_0000_0000_0001__0000_0000_0000_0011u)
        memWrite(GPIOx.RegisterType.LCKR.offset, 0b0000_0000_0000_0000__0000_0000_0000_0011u)
        memWrite(GPIOx.RegisterType.LCKR.offset, 0b0000_0000_0000_0001__0000_0000_0000_0011u)
    }

    fun setLock() {
        writeLockValues()
        memRead(GPIOx.RegisterType.LCKR.offset)
    }

    fun write(value: ULong) = memWrite(register.offset, value)
    fun read() = memRead(register.offset)

    init {
        // can't connect different size port directly
        gpio.ports.mem.connect(buses.mem)
        controller.ports.mem.connect(buses.mem)

        buses.connect(gpio.ports.pin_input, controller.ports.pin_input)
        buses.connect(gpio.ports.pin_output, controller.ports.pin_output)
    }
}