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

import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD

class GPIO(parent: Module, name: String) : Module(parent, name) {
    companion object {
        const val BUS_SIZE = 256
        const val BUS_INDEX = 2
    }

    inner class Ports : ModulePorts(this) {
        val io = Port("io")
    }

    override val ports = Ports()

    private val SC_USE_SEL = Register(ports.io, 0x00u, DWORD, "SC_USE_SEL", level = CONFIG)
    private val SC_IO_SEL = Register(ports.io, 0x04u, DWORD, "SC_IO_SEL", level = CONFIG)
    private val SC_GP_LVL = Register(ports.io, 0x08u, DWORD, "SC_GP_LVL", level = CONFIG)
    private val SC_TPE = Register(ports.io, 0x0Cu, DWORD, "SC_TPE", level = CONFIG)
    private val SC_TNE = Register(ports.io, 0x10u, DWORD, "SC_TNE", level = CONFIG)
    private val SC_TS = Register(ports.io, 0x14u, DWORD, "SC_TS", level = CONFIG)

    private val SUS_USE_SEL = Register(ports.io, 0x80u, DWORD, "SUS_USE_SEL", level = CONFIG)
    private val SUS_IO_SEL = Register(ports.io, 0x84u, DWORD, "SUS_IO_SEL", level = CONFIG)
    private val SUS_GP_LVL = Register(ports.io, 0x88u, DWORD, "SUS_GP_LVL", level = CONFIG)
    private val SUS_TPE = Register(ports.io, 0x8Cu, DWORD, "SUS_TPE", level = CONFIG)
    private val SUS_TNE = Register(ports.io, 0x90u, DWORD, "SUS_TNE", level = CONFIG)
    private val SUS_TS = Register(ports.io, 0x94u, DWORD, "SUS_TS", level = CONFIG)
    private val SUS_WAKE_EN = Register(ports.io, 0x98u, DWORD, "SUS_WAKE_EN", level = CONFIG)
}