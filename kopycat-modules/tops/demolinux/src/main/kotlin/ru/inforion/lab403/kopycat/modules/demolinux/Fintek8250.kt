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
package ru.inforion.lab403.kopycat.modules.demolinux

import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.BUS16
import java.util.logging.Level

class Fintek8250(parent: Module, name: String) : Module(parent, name) {

    companion object {
        @Transient val log = logger(FINE)
    }

    inner class Ports : ModulePorts(this) {
        val io = Slave("io", BUS16)
    }

    override val ports = Ports()

    private val ADDR_PORT_4 = Register(ports.io, 0x004Eu, Datatype.BYTE, "ADDR_PORT_4", level= Level.WARNING)
    private val DATA_PORT_4 = Register(ports.io, 0x004Fu, Datatype.BYTE, "DATA_PORT_4", level= Level.WARNING)
    private val ADDR_PORT_2 = Register(ports.io, 0x002Eu, Datatype.BYTE, "ADDR_PORT_2", level= Level.WARNING)
    private val DATA_PORT_2 = Register(ports.io, 0x002Fu, Datatype.BYTE, "DATA_PORT_2", level= Level.WARNING)
}