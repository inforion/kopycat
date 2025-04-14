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

class MBD64(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val msg = Port("msg")
    }

    override val ports = Ports()

    private val SERVICE = MESSAGE_BUS_SERVICE_REGISTER(ports.msg) { _, ss, _ ->
        when (ss) {
            6, 0x10 -> MESSAGE_BUS_READ_OPERATION
            7, 0x11 -> MESSAGE_BUS_WRITE_OPERATION
            else -> error("Unknown MBD64 opcode: 0x${ss.hex}")
        }
    }

    private val REG_11C = object : Register(ports.msg, 0x11Cu, WORD, "REG_11C", level = CONFIG) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong = 0x1000u
    }
    private val REG_28 = Register(ports.msg, 0x28u, WORD, "REG_28", level = CONFIG)
}