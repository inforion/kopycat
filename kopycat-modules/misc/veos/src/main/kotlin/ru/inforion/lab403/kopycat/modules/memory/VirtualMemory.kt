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
import ru.inforion.lab403.kopycat.serializer.deserializePrimitive
import ru.inforion.lab403.kopycat.veos.filesystems.interfaces.IRandomAccessFile
import java.nio.ByteOrder
import java.nio.ByteOrder.BIG_ENDIAN
import java.nio.ByteOrder.LITTLE_ENDIAN


class VirtualMemory constructor(
        parent: Module,
        name: String,
        val start: Long,
        val size: Long,
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

    private fun range(start: Long, size: Int) = LongRange(start, start + size - 1)

    override fun reset() {
        super.reset()
        // it's ...king crazy
        reconnect {
            // map makes copy to prevent modification during iteration
            areas.map { it }.forEach { it.remove() }
        }
    }

    fun allocate(name: String, start: Long, size: Int, access: ACCESS = ACCESS.R_W, data: ByteArray? = null): Area {
        val end = size + start - 1
        log.config { "Allocate: ${name.stretch(10)} [${start.hex8}..${end.hex8}] size=${size.hex8}" }
        return reconnect { Memory(ports.mem, start, end, name, access) }.also {
            if (data != null) it.store(start, data)
        }
    }

    // TODO: access?
    fun file(name: String, start: Long, size: Int, fd: IRandomAccessFile, offset: Int = 0) =
            reconnect { FileSegment(ports.mem, name, start, size, fd, offset, endian) }

    fun unmap(name: String) = reconnect { areas.filter { it.name == name }.forEach { it.remove() } }

    // TODO: bad implementation!!!
    //  Should not use intersections!
    fun unmap(start: Long, size: Int) = reconnect { areas.filter { it.range() isIntersect range(start, size) }.forEach { it.remove() } }

    private val freeRanges: List<LongRange> get() {
        val freeRange = mutableListOf(start until (size - start))

        areas.forEach {
            val cutRange = freeRange.first { free -> it.start in free && it.end in free }
            freeRange.remove(cutRange)
            if (cutRange.first != it.start)
                freeRange.add(cutRange.first until it.start)
            if (cutRange.last != it.end)
                freeRange.add((it.end + 1)..cutRange.last)
        }
        return freeRange
    }

    fun aligned(data: Long, alignment: Int) = (data / alignment) * alignment

    fun freeRangeByAlignment(alignment: Int = 1) = freeRanges.map {
        val floored = aligned(it.first, alignment)
        val start = if (floored == it.first)
            it.first
        else {
            val ceiled = floored + alignment
            if (ceiled < it.last) ceiled else it.last
        }
        start..it.last
    }.filter { it.length >= alignment }.minByOrNull { it.length }

    fun freeRangeBySize(size: Long = 0) = freeRanges.filter { it.length >= size }.minByOrNull { it.length }


    fun allocateBySize(name: String, size: Long, access: ACCESS = ACCESS.R_W, data: ByteArray? = null): LongRange? {
        val range = freeRangeBySize(size) ?: return null
        allocate(name, range.first, size.asInt, access, data)
        return range.first until (range.first + size)
    }
    fun allocateByAlignment(name: String, alignment: Int = 1, access: ACCESS = ACCESS.R_W, data: ByteArray? = null): LongRange? {
        val range = freeRangeByAlignment(alignment) ?: return null
        allocate(name, range.first, alignment, access, data)
        return range.first until (range.first + alignment)
    }

    fun fileByAlignment(name: String, size: Long, fd: IRandomAccessFile, offset: Int = 0): LongRange? {
        val range = freeRangeBySize(size) ?: return null
        file(name, range.first, size.asInt, fd, offset)
        return range.first until (range.first + size)
    }


    inner class FileSegment(
            port: SlavePort,
            name: String,
            start: Long,
            size: Int,
            val file: IRandomAccessFile,
            val offset: Int = 0,
            val endian: ByteOrder = LITTLE_ENDIAN
    ) : Area(port, start, start + size - 1, name) {

        private fun underflow(ea: Long, access: AccessAction) =
                MemoryAccessError(-1, ea, access, "Can't read ${size.hex8} bytes from ${ea.hex8}")

        @Suppress("NOTHING_TO_INLINE")
        private inline fun seekOffset(ea: Long) = ea - start + offset

        private fun fetchOrRead(ea: Long, size: Int, access: AccessAction): Long {
            val data = ByteArray(size)
            file.seek(seekOffset(ea))
            val read = file.read(data)
            if (read < size) throw underflow(ea, access)
            return data.getInt(0, size, endian)
        }

        override fun fetch(ea: Long, ss: Int, size: Int) = fetchOrRead(ea, size, FETCH)
        override fun read(ea: Long, ss: Int, size: Int) = fetchOrRead(ea, size, LOAD)
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            file.seek(seekOffset(ea))
            file.write(value.pack(size, endian))
        }

        override fun load(ea: Long, size: Int, ss: Int, onError: HardwareErrorHandler?): ByteArray {
            val data = ByteArray(size)
            file.seek(seekOffset(ea))
            val read = file.read(data)
            if (read < size) throw underflow(ea, LOAD)
            return data
        }

        override fun store(ea: Long, data: ByteArray, ss: Int, onError: HardwareErrorHandler?) {
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
        val areasMap = snapshot["areas"] as ArrayList<Map<String, MutableMap<String, Any>>>
        areasMap.forEach {
            check(it.size == 1) { "Bad logic" }
            val data = it.values.first()
            when (data["type"]) {
                "Memory" -> {
                    val name = deserializePrimitive(ctxt, data["name"], String::class.java) as String
                    val start = deserializePrimitive(ctxt, data["start"], Long::class.java) as Long
                    val size = deserializePrimitive(ctxt, data["size"], Long::class.java) as Long
                    val access = deserializePrimitive(ctxt, data["access"], ACCESS::class.java) as ACCESS
                    allocate(name, start, size.toInt(), access)
                }
                else -> error("Undefined snapshot type ${data["type"]}")
            }
        }
        
        super<Module>.deserialize(ctxt, snapshot)
    }

    val areasAsImmutable: List<Area> get() = areas
}