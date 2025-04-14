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
package ru.inforion.lab403.kopycat.modules.pic32mz

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts


class GPIO(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val mem = Port("mem")
    }

    override val ports = Ports()

    inner class PORT_REGISTER(offset: ULong, name: String) :
            ComplexRegister(ports.mem, offset, "PORT_$name")

    val ANSEL = PORT_REGISTER(0x0000u, "ANSEL")
    val TRIS = PORT_REGISTER(0x0010u, "TRIS")
    val PORT = PORT_REGISTER(0x0020u, "PORT")
    val LAT = PORT_REGISTER(0x0030u, "LAT")
    val ODC = PORT_REGISTER(0x0040u, "ODC")
    val CNPU = PORT_REGISTER(0x0050u, "CNPU")
    val CNPD = PORT_REGISTER(0x0060u, "CNPD")
    val CNCON = PORT_REGISTER(0x0070u, "CNCON")
    val CNEN = PORT_REGISTER(0x0080u, "CNEN")
    val CNSTAT = PORT_REGISTER(0x0090u, "CNSTAT")
    val CNNE = PORT_REGISTER(0x00A0u, "CNNE")
    val CNF = PORT_REGISTER(0x00B0u, "CNF")
}