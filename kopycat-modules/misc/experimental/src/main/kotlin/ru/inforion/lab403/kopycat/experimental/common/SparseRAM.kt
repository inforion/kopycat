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

    private inner class Holder : Module(this@SparseRAM, "holder") {
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

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            val regs = snapshot["regions"] as List<Long>
            regs.forEach { getOrPut(it.ulong) }
            super.deserialize(ctxt, snapshot)
        }
    }

    private val holder = Holder()

    inner class SparseArea(name: String) : Area(ports.mem, 0uL, ramSize - 1u, name) {

        private fun isCrossRegion(ea: ULong, size: Int) = size != 0 && ea / regionSize != (ea + size - 1u) / regionSize

        private fun putBytesFastPath(ea: ULong, data: ByteArray) {
            if (holder.get(ea) == null && data.all { it.int_s == 0 }) {
                // Already zero, do not create memory
                return
            }

            holder.getOrPut(ea).store(ea, data)
        }

        fun putBytes(ea: ULong, data: ByteArray) {
            val size = data.size
            if (!isCrossRegion(ea, size)) {
                // Single holder is affected; fast path
                putBytesFastPath(ea, data)
            } else {
                var curAddr = ea
                var curDataOfft = 0
                while (curAddr < ea + size) {
                    val nextRegion = ((curAddr / regionSize) + 1uL) * regionSize
                    val pieceSize = min((nextRegion - curAddr).int, data.size - curDataOfft)
                    putBytesFastPath(curAddr, data.sliceArray(curDataOfft until curDataOfft + pieceSize))
                    curAddr += pieceSize
                    curDataOfft += pieceSize
                }
            }
        }

        private fun getBytesFastPath(ea: ULong, size: Int) = holder.get(ea)?.load(ea, size) ?: ByteArray(size)

        fun getBytes(ea: ULong, size: Int): ByteArray {
            return if (!isCrossRegion(ea, size)) {
                // Fast path
                getBytesFastPath(ea, size)
            } else {
                val data = ByteArray(size)
                var curAddr = ea
                var curDataOfft = 0
                while (curAddr < ea + size) {
                    val nextRegion = ((curAddr / regionSize) + 1uL) * regionSize
                    val pieceSize = min((nextRegion - curAddr).int, size - curDataOfft)
                    data.putArray(curDataOfft, getBytesFastPath(curAddr, pieceSize))
                    curAddr += pieceSize
                    curDataOfft += pieceSize
                }
                data
            }
        }

        override fun fetch(ea: ULong, ss: Int, size: Int) = read(ea, ss, size)

        override fun read(ea: ULong, ss: Int, size: Int) = if (!isCrossRegion(ea, size)) {
            holder.get(ea)?.read(ea, ss, size) ?: 0uL
        } else {
            getBytes(ea, size).getUInt(0, size, endian)
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            if (!isCrossRegion(ea, size)) {
                if (value == 0uL && holder.get(ea) == null) {
                    // Not cross-region; already zero, do not create memory
                    return
                }
                holder.getOrPut(ea).write(ea, ss, size, value)
            } else {
                putBytes(ea, value.pack(size, endian))
            }
        }

    }

    val area = SparseArea("SparseArea")

    /**
     * {RU}
     * Была ли создана память для адреса [ea].
     * В начальном состоянии для любого адреса результатом будет false.
     * {RU}
     */
    fun hasMemoryForAddress(ea: ULong) = holder.get(ea) != null


    override fun serialize(ctxt: GenericSerializer) = holder.serialize(ctxt)

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) = holder.deserialize(ctxt, snapshot)

    fun put(addr: ULong, data: ByteArray) = area.putBytes(addr, data)
}
