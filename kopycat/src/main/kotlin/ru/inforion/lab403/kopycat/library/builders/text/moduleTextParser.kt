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

import ru.inforion.lab403.common.extensions.hexAsULong
import ru.inforion.lab403.common.extensions.toInt
import ru.inforion.lab403.common.extensions.toLong
import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.base.StackOfStrings
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.exceptions.ConnectionError

object moduleTextParser {
    fun getConnector(items: StackOfStrings, module: Module): Any {
        return when (val first = items.pop()) {
            "ports" -> {
                val name = items.pop()
                module.ports[name] ?: throw ConnectionError("Port [$name] not found in [$module]!")
            }
            "buses" -> {
                val name = items.pop()
                module.buses[name] ?: throw ConnectionError("Bus [$name] not found in [$module]!")
            }
            else -> {
                val sub = module[first] ?: throw ConnectionError("Submodule [$first] not found in [$module]")
                getConnector(items, sub as Module)
            }
        }
    }

    fun getConnector(desc: String, module: Module) = getConnector(StackOfStrings(desc.split(".")), module)

    fun getBusPortSize(size: Any) = when (size) {
        is String -> when {
            size == "PIN" -> 1L
            size.startsWith("BUS") -> 1L shl size.removePrefix("BUS").toInt()
            size.startsWith("0x") -> size.removePrefix("0x").hexAsULong
            else -> size.toLong()
        }
        is Int -> size.toULong()
        is Long -> size
        else -> throw IllegalArgumentException("getBusPortSize accepts only: Int, Long, String")
    }

    fun getPortType(type: String) = ModulePorts.Type.valueOf(type)

    fun getOffset(data: Any) = when (data) {
        is Int -> data.toULong()
        is Long -> data
        is String -> {
            if (data.startsWith("0x"))
                data.removePrefix("0x").hexAsULong
            else data.toLong()
        }
        else -> throw IllegalArgumentException("getOffset accepts only: Int, Long, String")
    }
}