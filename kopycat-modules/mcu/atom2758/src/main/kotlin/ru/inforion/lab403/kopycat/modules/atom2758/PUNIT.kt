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
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import java.util.logging.Level

class PUNIT(parent: Module, name: String) : Module(parent, name) {
    companion object {
        const val BUS_SIZE = 2048
        const val BUS_INDEX = 7
    }

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", BUS_SIZE)

        val msg = Slave("msg", MESSAGE_PORT_SIZE)
    }

    override val ports = Ports()

    private val SERVICE = MESSAGE_BUS_SERVICE_REGISTER(ports.msg) { _, ss, _ ->
        when (ss) {
            6, 0x10 -> MESSAGE_BUS_READ_OPERATION
            7, 0x11 -> MESSAGE_BUS_WRITE_OPERATION
            else -> error("Unknown opcode: 0x${ss.hex}")
        }
    }

    private val REG_05 = object : Register(ports.msg, 0x05u, BYTE, "REG_05", level = Level.CONFIG) {
        override fun read(ea: ULong, ss: Int, size: Int): ULong = 2u
    }
    private val REG_06 = Register(ports.msg, 0x06u, BYTE, "REG_05", level = Level.CONFIG)

    private val AREA_80_9F = Memory(ports.msg, 0x80u, 0x9Fu, "AREA_80_9F", access = ACCESS.R_W)
    private val AREA_B0_DF = Memory(ports.msg, 0xB0u, 0xDFu, "AREA_B0_DF", access = ACCESS.R_W)

    private val REG_7C = Register(ports.msg, 0x7Cu, DWORD, "REG_7C", level = Level.CONFIG)
}
