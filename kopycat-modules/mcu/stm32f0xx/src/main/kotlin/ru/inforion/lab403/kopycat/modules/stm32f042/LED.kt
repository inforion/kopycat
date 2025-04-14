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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS

class LED(parent: Module, name: String) : Module(parent, name) {
    companion object {
        @Transient val log = logger(INFO)
    }

    enum class STATE(val id: Int) {
        OFF     (0),
        ON      (1),
        UNKNOWN (-1)
    }

    inner class Ports : ModulePorts(this) {
        val pin = Port("pin")
    }

    override val ports = Ports()

    private val ledControl = object : Area(ports.pin, 0u, 0u, "GPIO_INPUT", ACCESS.R_W) {
        override fun fetch(ea: ULong, ss: Int, size: Int) = throw IllegalAccessException("$name may not be fetched!")
        override fun read(ea: ULong, ss: Int, size: Int) = state.id.ulong_z
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            state = find<STATE> { it.id == value.int } ?: STATE.UNKNOWN
        }
    }

    var state = STATE.UNKNOWN
        set(value) {
            if (field != value && core.clock.time() != 0uL) log.info { stringify() }
            field = value
        }

    override fun stringify(): String {
        val time = if (isCorePresent) core.clock.time() else -1uL
        return "LED [ %5s ] state is %s @ %,d us".format(name, state, time.long)
    }

    override fun reset() {
        super.reset()
        state = STATE.OFF
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> = mapOf("state" to state.toString())

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        state = STATE.valueOf(snapshot["state"] as String)
    }
}
