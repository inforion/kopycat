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
@file:Suppress("PrivatePropertyName", "unused")

package ru.inforion.lab403.kopycat.modules.virtarm

import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.common.extensions.truth
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.bit
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.SystemClock
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.extensions.request
import ru.inforion.lab403.kopycat.serializer.storeValues

class Timer(parent: Module, name: String, private val divider: Long) : Module(parent, name) {
    companion object {
        @Transient private val log = logger(FINE)
    }

    inner class Ports : ModulePorts(this) {
        val mem = Port("mem")
        val irq = Port("irq")
    }

    override val ports = Ports()

    private inner class Timer : SystemClock.PeriodicalTimer("Timer Counter") {
        var mode = false

        override fun trigger() {
            super.trigger()
            CURRENT_VALUE_REG.data = (CURRENT_VALUE_REG.data - 1u) mask 32

            if (CURRENT_VALUE_REG.data == 0uL) {
                CURRENT_VALUE_REG.data = LOAD_COUNT_REG.data

                if (CONTROL_REG.interruptMask == 0 && EOI_REG.data == 0uL) {
                    EOI_REG.data = 1u
                    ports.irq.request(0)
                }
            }
        }
    }

    private val timer = Timer()

    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
            "timer" to timer.serialize(ctxt)
    )

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        timer.deserialize(ctxt, snapshot["timer"] as Map<String, Any>)
    }

    override fun initialize(): Boolean {
        if (!super.initialize()) return false
        core.clock.connect(timer, divider, false)
        return true
    }

    inner class CONTROL : Register(ports.mem, 0x08u, Datatype.DWORD, "CONTROL_REG") {
        var enabled by bit(0)
        var mode by bit(1)
        var interruptMask by bit(2)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            super.write(ea, ss, size, value)

            timer.enabled = enabled.truth
            timer.mode = mode.truth
            // TODO: configure interrupt
        }
    }

    private var LOAD_COUNT_REG = Register(ports.mem, 0x00u, Datatype.DWORD, "LOAD_COUNT_REG")
    private var CURRENT_VALUE_REG = Register(ports.mem, 0x04u, Datatype.DWORD, "CURRENT_VALUE_REG")
    private var CONTROL_REG = CONTROL()
    private var EOI_REG = object : Register(ports.mem, 0x0Cu, Datatype.DWORD, "EOI_REG", writable = false) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            val ret = super.read(ea, ss, size)
            write(ea, ss, size, 0uL)
            return ret
        }
    }

    private var INT_STATUS_REG = Register(ports.mem, 0x10u, Datatype.DWORD, "INT_STATUS_REG")
}