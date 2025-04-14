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
package ru.inforion.lab403.kopycat.modules.atom2758

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import java.util.*

class MBD18(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val msg = Port("msg")
    }

    override val ports = Ports()

    private val SERVICE = MESSAGE_BUS_SERVICE_REGISTER(ports.msg) { _, ss, _ ->
        when (ss) {
            6 -> MESSAGE_BUS_READ_OPERATION
            7 -> MESSAGE_BUS_WRITE_OPERATION
            else -> error("Unknown MBD18 opcode: 0x${ss.hex}")
        }
    }

    private val REG_7858 = object : Register(ports.msg, 0x7858u, WORD, "REG_7858", level = CONFIG) {
        private val values = LinkedList<ULong>(listOf(4u, 2u, 8u))

        override fun read(ea: ULong, ss: Int, size: Int): ULong = if (values.isEmpty()) 0u else values.pop()
    }
}