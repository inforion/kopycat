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
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*

class POST(parent: Module, name: String) : Module(parent, name) {

    inner class Ports : ModulePorts(this) {
        val io = Port("io")
    }

    override val ports = Ports()

    private val post0 = Register(ports.io, 0x80u, BYTE, "post0", level = CONFIG)
    private val post1 = Register(ports.io, 0x81u, BYTE, "post1", level = CONFIG)
    private val post2 = Register(ports.io, 0x82u, BYTE, "post2", level = CONFIG)
    private val post3 = Register(ports.io, 0x83u, BYTE, "post3", level = CONFIG)
    private val post4 = Register(ports.io, 0x84u, BYTE, "post4", level = CONFIG)
    private val post5 = Register(ports.io, 0x85u, BYTE, "post5", level = CONFIG)
    private val post6 = Register(ports.io, 0x86u, BYTE, "post6", level = CONFIG)
    private val post7 = Register(ports.io, 0x87u, BYTE, "post7", level = CONFIG)
    private val post8 = Register(ports.io, 0x88u, BYTE, "post8", level = CONFIG)
    private val post9 = Register(ports.io, 0x89u, BYTE, "post9", level = CONFIG)
    private val posta = Register(ports.io, 0x8au, BYTE, "posta", level = CONFIG)
    private val postb = Register(ports.io, 0x8bu, BYTE, "postb", level = CONFIG)
    private val postc = Register(ports.io, 0x8cu, BYTE, "postc", level = CONFIG)
    private val postd = Register(ports.io, 0x8du, BYTE, "postd", level = CONFIG)
    private val poste = Register(ports.io, 0x8eu, BYTE, "poste", level = CONFIG)
    private val postf = Register(ports.io, 0x8fu, BYTE, "postf", level = CONFIG)
}