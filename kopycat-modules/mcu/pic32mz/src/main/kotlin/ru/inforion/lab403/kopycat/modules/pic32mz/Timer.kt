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
package ru.inforion.lab403.kopycat.modules.pic32mz

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.cores.base.field
import ru.inforion.lab403.kopycat.modules.PIN


class Timer(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem")
        val irq = Master("irq", PIN)
        val osc = Slave("osc", PIN)
    }

    // Example how to get another module ... not a good way :\ ports better?
    val osc by lazy {
        val modules = getComponentsByPluginName("OSC")
        if (modules.size != 1) throw GeneralException("Too many OSC!")
        modules.first() as Module
    }

    override val ports = Ports()

    private val counter = object : SystemClock.PeriodicalTimer("Timer $name Counter") {
        override fun trigger() {
            super.trigger()
            log.finest { "%s triggered at %,d us".format(name, core.clock.time()) }
            TMRx.data += 1
            if (TMRx.data == PRx.data) {
                TMRx.data = 0
                ports.irq.request(0)
            }
        }
    }

    val TxCON = object : ComplexRegister(ports.mem,0x0000, "TxCON") {
        val ON by bit(15)
        val SIDL by bit(13)
        val TWDIS by bit(12)
        val TWIP by bit(11)
        val TGATE by bit(7)
        val TCKPS by field(5..4)
        val TSYNC by bit(2)
        val TCS by bit(1)

        private var clockPrescaler: Int = 1
        private var peripheralBusPrescaler: Int = 1

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            log.config { "TxCON[ON=$ON SIDL=$SIDL TWDIS=$TWDIS TWIP=$TWIP TGATE=$TGATE TCKPS=$TCKPS TSYNC=$TSYNC TCS=$TCS]" }

            val newClockPrescaler = when (TCKPS.asInt) {
                3 -> 256  //
                2 -> 64
                1 -> 8
                0 -> 1
                else -> throw GeneralException("Wrong TCKPS value: $TCKPS")
            }
            if (clockPrescaler != newClockPrescaler) {
                clockPrescaler = newClockPrescaler
                core.clock.connect(counter, clockPrescaler.asLong * peripheralBusPrescaler.asLong)
            }

            val newPeripheralBusPrescaler: Int = osc.variables["PB3DIV"]
            if (peripheralBusPrescaler != newPeripheralBusPrescaler) {
                peripheralBusPrescaler = newPeripheralBusPrescaler
                core.clock.connect(counter, clockPrescaler.asLong * peripheralBusPrescaler.asLong)
            }

            if (SIDL != 0) log.severe { "SIDL: Stop in Idle Mode unsupported!" }
            if (TWDIS != 0) log.severe { "TWDIS: Asynchronous Timer Write unsupported!" }
            if (TWIP != 0) log.severe { "TWIP: Asynchronous Timer Write in Progress unsupported!" }
            if (TGATE != 0) log.severe { "TGATE: Timer Gated Time Accumulation unsupported!" }
            if (TSYNC != 0) log.severe { "TSYNC: Timer External Clock Input Synchronization unsupported!" }
            if (TCS != 0) log.severe { "TCS: Timer Clock Source selection unsupported!" }

            counter.enabled = (ON == 1)
        }
    }

    val TMRx = ComplexRegister(ports.mem, 0x0010, "TMRx") // Current timer ticks
    val PRx = ComplexRegister(ports.mem, 0x0020, "PRx") // Match register
}