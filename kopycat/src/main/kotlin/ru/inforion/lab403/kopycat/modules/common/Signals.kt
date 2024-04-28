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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts

class Signals(parent: Module, name: String, val size: ULong, val value: ULong) : Module(parent, name) {

    companion object {
        @Transient private val log = logger(FINE)
    }

    inner class Ports : ModulePorts(this) {
        val wires = Slave("wires", this@Signals.size)
    }

    override val ports = Ports()

    val area = object : Area(ports.wires, 0u, size - 1u, "SIGNALS") {
        override fun fetch(ea: ULong, ss: Int, size: Int): ULong = throw IllegalAccessException("Can't fetch $name")
        override fun read(ea: ULong, ss: Int, size: Int): ULong = value[ea.int]
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = Unit
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> = mapOf(
            "size" to size.hex8,
            "value" to value.hex16
    )

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>){
        val sizeSnapshot = (snapshot["size"] as String).ulongByHex
        check(sizeSnapshot == size) { "size: %08X != %08X".format(size, sizeSnapshot) }

        val valueSnapshot = (snapshot["value"] as String).ulongByHex
        check(valueSnapshot == value) { "value: %16X != %16X".format(value, valueSnapshot) }
    }
}