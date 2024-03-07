/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.experimental.common

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.modules.BUS64
import java.nio.ByteOrder
import kotlin.math.min

class SparseRAM(
    parent: Module,
    name: String,
    val ramSize: ULong,
    val regionSize: ULong,
    val endian: ByteOrder = ByteOrder.LITTLE_ENDIAN,
) : Module(parent, name), IAutoSerializable {

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", ramSize)
    }

    @DontAutoSerialize
    override val ports = Ports()

    inner class Holder : Module(this@SparseRAM, "holder") {
        inner class Ports : ModulePorts(this) {
            val mem = Slave("mem", BUS64)
        }

        override val ports = Ports()

        private fun aligned(ea: ULong) = (ea / regionSize) * regionSize

        private val regions = mutableMapOf<ULong, Memory>()

        fun get(ea: ULong) = regions[aligned(ea)]

        fun getOrPut(ea: ULong): Memory {
            val base = aligned(ea)

            return regions[base] ?: reconnect {
                Memory(ports.mem, base, base + regionSize - 1u, "Region[${base.hex16}]", ACCESS.R_W).apply {
                    endian = this@SparseRAM.endian
                }
            }.also { regions[base] = it }
        }

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
            return super.serialize(ctxt) + mapOf("regions" to regions.keys.toList())
        }

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            val regs = snapshot["regions"] as List<Long>
            regs.forEach { getOrPut(it.ulong) }
            super.deserialize(ctxt, snapshot)
        }
    }

    private val holder = Holder()

    val area = object : Area(ports.mem, 0uL, ramSize - 1u, "SparseArea") {

        override fun fetch(ea: ULong, ss: Int, size: Int) = read(ea, ss, size)

        override fun read(ea: ULong, ss: Int, size: Int): ULong = holder.get(ea)?.read(ea, ss, size) ?: 0uL

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            if (value == 0uL && holder.get(ea) == null) {
                // Already zero, do not create memory
                return
            }

            val entry = holder.getOrPut(ea)
            entry.write(ea, ss, size, value)
        }

    }

    /**
     * {RU}
     * Была ли создана память для адреса [ea].
     * В начальном состоянии для любого адреса результатом будет false.
     * {RU}
     */
    fun hasMemoryForAddress(ea: ULong) = holder.get(ea) != null


    override fun serialize(ctxt: GenericSerializer) = holder.serialize(ctxt)

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) = holder.deserialize(ctxt, snapshot)

    fun put(addr: ULong, data: ByteArray) {
        var curAddr = addr
        var curDataOfft = 0
        while (curAddr < addr + data.size.ulong_z) {
            val mem = holder.getOrPut(curAddr)
            val size = min((mem.start + mem.size - curAddr).int, data.size - curDataOfft)
            mem.store(curAddr, data.sliceArray(curDataOfft until curDataOfft + size))
            curAddr += size
            curDataOfft += size
        }
    }
}
