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
package ru.inforion.lab403.kopycat.modules.common

import ru.inforion.lab403.common.extensions.ULONG_MAX
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.modules.BUS32
import java.nio.ByteOrder

/**
 * ```
 *           _______
 *          |  HUB ||>-->-->-->
 *  Master  |      |
 * -->->->->|      ||>-->-->-->
 *   Bus    |      |
 *          |______||>-->-->-->
 * ```
 */

class Hub(parent: Module, name: String, vararg val outs: String) : Module(parent, name) {

    companion object {
        @Transient val log = logger(CONFIG)
    }

    inner class Ports : ModulePorts(this) {
        val input = Port("input")
        val outputs = outs.map { Port(it) }
    }

    override val ports = Ports()

    val MEM_SPACE = object : Area(ports.input, 0uL, BUS32 - 1u, "HUB_AREA") {
        override fun fetch(ea: ULong, ss: Int, size: Int) = throw IllegalAccessException("$name may not be fetched!")

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            val ports = ports.outputs.filter { output ->
                output.find(output, ea, ss, size, AccessAction.LOAD, 0u, ByteOrder.LITTLE_ENDIAN) != null
            }
            if (ports.size != 1) {
                throw MemoryAccessError(
                    ULONG_MAX, ea, AccessAction.LOAD,
                        if (ports.isEmpty())
                            "No area or register found at address ${ea.hex8} at hub ${this.name}"
                        else
                            "More then one area or register found at address ${ea.hex8} at hub ${this.name}")
            }
            return ports.first().read(ea, ss, size)
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val areas = ports.outputs.filter {
                output -> output.find(output, ea, ss, size, AccessAction.STORE, value, ByteOrder.LITTLE_ENDIAN) != null
            }
            if (areas.isEmpty())
                throw MemoryAccessError(ULONG_MAX, ea, AccessAction.STORE, "No area or register found at address ${ea.hex8} at hub ${this.name}")
            areas.forEach { it.write(ea, ss, size, value) }
        }
    }
}