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

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts


class SYS(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem")
    }

    override val ports = Ports()

    val CFGCON = ComplexRegister(ports.mem, 0x0000, "CFGCON")
    val DEVID = ComplexRegister(ports.mem, 0x0020, "DEVID")
    val SYSKEY = ComplexRegister(ports.mem, 0x0030, "SYSKEY")
    val CFGEBIA = ComplexRegister(ports.mem, 0x00C0, "CFGEBIA")
    val CFGEBIC = ComplexRegister(ports.mem, 0x00D0, "CFGEBIC")
    val CFGPG = ComplexRegister(ports.mem,0x00E0, "CFGPG")
}