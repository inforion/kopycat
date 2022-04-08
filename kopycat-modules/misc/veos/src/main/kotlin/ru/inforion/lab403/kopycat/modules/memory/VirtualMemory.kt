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
package ru.inforion.lab403.kopycat.modules.memory

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.HardwareErrorHandler
import ru.inforion.lab403.kopycat.cores.base.SlavePort
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.FETCH
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.LOAD
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable
import ru.inforion.lab403.kopycat.veos.filesystems.interfaces.IRandomAccessFile
import java.nio.ByteOrder
import java.nio.ByteOrder.BIG_ENDIAN
import java.nio.ByteOrder.LITTLE_ENDIAN


class VirtualMemory constructor(
        parent: Module,
        name: String,
        val start: ULong,
        val size: ULong,
        var bigEndian: Boolean = false
): Module(parent, name), IConstructorSerializable {

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem")
    }

    private var shareCount = 1

    val isUnused get() = shareCount == 0

    fun share() {
        ++shareCount
    }
    fun unshare() {
        --shareCount
        if (shareCount == 0)
            reset() // REVIEW: better way is to make var and assign null
    }

    val endian: ByteOrder get() = if (bigEndian) BIG_ENDIAN else LITTLE_ENDIAN

    override val ports = Ports()

    companion object {
        @Transient val log = logger(CONFIG)
    }

    private fun range(start: ULong, size: Int) = ULongRange(start, start + size - 1u)

    override fun reset() {
        super.reset()
        // it's ...king crazy
        reconnect {
            // map makes copy to prevent modification during iteration
            areas.map { it }.forEach { it.remove() }
        }
    }

    fun allocate(name: String, start: ULong, size: Int, access: ACCESS = ACCESS.R_W, data: ByteArray? = null): Area {
        val end = size.uint + start - 1u
        val logname = if (name.length <= 16) name.stretch(16) else "${name.take(7)}..${name.takeLast(7)}"
        log.config { "Allocate: $logname [${start.hex8}..${end.hex8}] size=${size.hex8}" }
        return reconnect { Memory(ports.mem, start, end, name, access, false, endian) }.also {
            if (data != null) it.store(start, data)
        }
    }

    // TODO: access?
    fun file(name: String, start: ULong, size: Int, fd: IRandomAccessFile, offset: Int = 0) =
            reconnect { FileSegment(ports.mem, name, start, size, fd, offset, endian) }

    fun unmap(name: String) = reconnect { areas.filter { it.name == name }.forEach { it.remove() } }

    // TODO: bad implementation!!!
    //  Should not use intersections!
    fun unmap(start: ULong, size: Int) = reconnect { areas.filter { it.range() isIntersect range(start, size) }.forEach { it.remove() } }

    private val freeRanges: List<ULongRange> get() {
        val freeRange = mutableListOf(start until (size - start))

        areas.forEach {
            val cutRange = freeRange.find { free -> it.start in free && it.end in free }
            checkNotNull(cutRange) { "It seems like some areas got intersection" }
            freeRange.remove(cutRange)
            if (cutRange.first != it.start)
                freeRange.add(cutRange.first until it.start)
            if (cutRange.last != it.end)
                freeRange.add((it.end + 1u)..cutRange.last)
        }
        return freeRange
    }

    fun aligned(data: ULong, alignment: Int) = (data / alignment) * alignment

    fun freeRangeByAlignment(alignment: Int = 1) = freeRanges.map {
        val floored = aligned(it.first, alignment)
        val start = if (floored == it.first)
            it.first
        else {
            val ceiled = floored + alignment
            if (ceiled < it.last) ceiled else it.last
        }
        start..it.last
    }.filter { it.length >= alignment.ulong_z }.minByOrNull { it.length }

    fun freeRangeBySize(size: ULong = 0u) = freeRanges.filter { it.length >= size }.minByOrNull { it.length }


    fun allocateBySize(name: String, size: ULong, access: ACCESS = ACCESS.R_W, data: ByteArray? = null): ULongRange? {
        val range = freeRangeBySize(size) ?: return null
        allocate(name, range.first, size.int, access, data)
        return range.first until (range.first + size)
    }
    fun allocateByAlignment(name: String, alignment: Int = 1, access: ACCESS = ACCESS.R_W, data: ByteArray? = null): ULongRange? {
        val range = freeRangeByAlignment(alignment) ?: return null
        allocate(name, range.first, alignment, access, data)
        return range.first until (range.first + alignment)
    }

    fun fileByAlignment(name: String, size: ULong, fd: IRandomAccessFile, offset: Int = 0): ULongRange? {
        val range = freeRangeBySize(size) ?: return null
        file(name, range.first, size.int, fd, offset)
        return range.first until (range.first + size)
    }


    inner class FileSegment(
            port: SlavePort,
            name: String,
            start: ULong,
            size: Int,
            val file: IRandomAccessFile,
            val offset: Int = 0,
            val endian: ByteOrder = LITTLE_ENDIAN
    ) : Area(port, start, start + size - 1u, name) {

        private fun underflow(ea: ULong, access: AccessAction) =
                MemoryAccessError(-1uL, ea, access, "Can't read ${size.hex8} bytes from ${ea.hex8}")

        @Suppress("NOTHING_TO_INLINE")
        private inline fun seekOffset(ea: ULong) = ea - start + offset

        private fun fetchOrRead(ea: ULong, size: Int, access: AccessAction): ULong {
            val data = ByteArray(size)
            file.seek(seekOffset(ea))
            val read = file.read(data)
            if (read < size) throw underflow(ea, access)
            return data.getUInt(0, size, endian)
        }

        override fun fetch(ea: ULong, ss: Int, size: Int) = fetchOrRead(ea, size, FETCH)
        override fun read(ea: ULong, ss: Int, size: Int) = fetchOrRead(ea, size, LOAD)
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            file.seek(seekOffset(ea))
            file.write(value.pack(size, endian))
        }

        override fun load(ea: ULong, size: Int, ss: Int, onError: HardwareErrorHandler?): ByteArray {
            val data = ByteArray(size)
            file.seek(seekOffset(ea))
            val read = file.read(data)
            if (read < size) throw underflow(ea, LOAD)
            return data
        }

        override fun store(ea: ULong, data: ByteArray, ss: Int, onError: HardwareErrorHandler?) {
            file.seek(seekOffset(ea))
            file.write(data)
        }

        override fun beforeRemove() = file.close()

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
            TODO("Not implemented")
        }

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            TODO("Not implemented")
        }
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        val snapshot = super<Module>.serialize(ctxt)
        val areasMap = snapshot["areas"] as ArrayList<Map<String, MutableMap<String, Any>>>

        areasMap.forEachIndexed { i, it ->
            val data = it.values.first()
            val area = areas[i]
            data += when(area) {
                is Memory ->  mapOf(
                        "type" to "Memory",
                        "name" to area.name,
                        "start" to area.start,
                        "size" to area.size,
                        "access" to area.access.name
                )
//                is FileSegment -> "FileSegment"
                else -> TODO("Unknown")
            }
        }
        return snapshot
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        if (!ctxt.doRestore) {
            val areasMap = snapshot["areas"] as ArrayList<Map<String, MutableMap<String, Any>>>
            areasMap.forEach {
                check(it.size == 1) { "Bad logic" }
                val data = it.values.first()
                when (data["type"]) {
                    "Memory" -> allocate(
                        ctxt.deserializePrimitive(data["name"]),
                        ctxt.deserializePrimitive(data["start"]),
                        ctxt.deserializePrimitive(data["size"]),
                        ctxt.deserializePrimitive(data["access"])
                    )
                    else -> error("Undefined snapshot type ${data["type"]}")
                }
            }
        }
        super<Module>.deserialize(ctxt, snapshot)
    }

    val areasAsImmutable: List<Area> get() = areas
}