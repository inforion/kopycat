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
package ru.inforion.lab403.kopycat.modules.memory

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import java.io.InputStream


class SparseRAM(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem")
    }

    override val ports = Ports()
    private val segments = ArrayList<Memory>()

    fun addSegment(start: Long,
                   size: Int,
                   stream: InputStream,
                   name: String? = null,
                   access: ACCESS = ACCESS.R_W,
                   verbose: Boolean = false) {
        val end = start + size - 1
        val actualName = name ?: "RAM_MEMORY_${start.hex8}_${end.hex8}"
        val segment = Memory(ports.mem,
                start,
                end,
                actualName,
                access,
                verbose).apply {
            write(start, stream)
        }
        log.info { "Added memory segment $actualName (${start.hex8}:${end.hex8}) to SparseRAM ${this.name}" }
        segments.add(segment)
    }

    fun addSegment(start: Long, size: Int, data: ByteArray, name: String? = null, access: ACCESS = ACCESS.R_W, verbose: Boolean = false) {
        addSegment(start, size, data.inputStream(), name, access, verbose)
    }

    fun addSegment(start: Long, size: Int, name: String? = null, access: ACCESS = ACCESS.R_W, verbose: Boolean = false) {
        addSegment(start, size, ByteArray(size).inputStream(), name, access, verbose)
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) { TODO() }
    override fun serialize(ctxt: GenericSerializer): Map<String, Any> { TODO() }
    override fun restore(ctxt: GenericSerializer, snapshot: Map<String, Any>) { TODO() }
    override fun reset() { TODO() }

}