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
package ru.inforion.lab403.kopycat.library.builders.text

import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts

class PortConfig(val name: String, val type: String, val size: String) {

    fun create(module: Module): ModulePorts.APort {
        val typeValue = moduleTextParser.getPortType(type)
        val sizeValue = moduleTextParser.getBusPortSize(size)
        return when (typeValue) {
            ModulePorts.Type.Master -> module.ports.Master(name, sizeValue)
            ModulePorts.Type.Slave -> module.ports.Slave(name, sizeValue)
            ModulePorts.Type.Proxy -> module.ports.Proxy(name, sizeValue)
            else -> throw IllegalArgumentException("Only Master, Slave and Proxy can be created!")
        }
    }
}