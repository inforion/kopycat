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
package ru.inforion.lab403.kopycat.modules.memory

import ru.inforion.lab403.common.extensions.gzipInputStreamIfPossible
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.HardwareErrorHandler
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.ACCESS
import ru.inforion.lab403.kopycat.interfaces.IFetchReadWrite
import ru.inforion.lab403.kopycat.library.types.Resource
import java.io.File
import java.io.InputStream
import java.nio.ByteOrder

abstract class AMemory(
    parent: Module,
    name: String,
    val size: Int,
    access: ACCESS,
    verbose: Boolean,
    vararg items: Pair<Any, Int>
) : Module(parent, name), IFetchReadWrite {

    @Suppress("RemoveRedundantSpreadOperator")
    constructor(
        parent: Module,
        name: String,
        size: Int,
        access: ACCESS,
        verbose: Boolean
    ) :
            this(parent, name, size, access, verbose, *emptyArray())

    constructor(
        parent: Module,
        name: String,
        size: Int,
        access: ACCESS,
        verbose: Boolean,
        data: ByteArray
    ) :
            this(parent, name, size, access, verbose, data to 0)

    constructor(
        parent: Module,
        name: String,
        size: Int,
        access: ACCESS,
        verbose: Boolean,
        data: InputStream
    ) :
            this(parent, name, size, access, verbose, data.readBytes())

    constructor(
        parent: Module,
        name: String,
        size: Int,
        access: ACCESS,
        verbose: Boolean,
        data: File
    ) :
            this(parent, name, size, access, verbose, gzipInputStreamIfPossible(data.path))

    constructor(
        parent: Module,
        name: String,
        size: Int,
        access: ACCESS,
        verbose: Boolean,
        data: Resource
    ) :
            this(parent, name, size, access, verbose, data.openStream())

    inner class Ports : ModulePorts(this) {
        val mem = Slave("mem", this@AMemory.size)
    }

    final override val ports = Ports()

    private val prefix = javaClass.simpleName

    private val records = items.associate {
        val result = when (val item = it.first) {
            is ByteArray -> item
            is InputStream -> item.readBytes()
            is File -> gzipInputStreamIfPossible(item.path).readBytes()
            is Resource -> item.readBytes()
            else -> throw IllegalArgumentException("Can't read data from $item as ${item.javaClass}")
        }
        it.second to result
    }

    private val memory = Memory(ports.mem, 0u, size.ulong_z - 1u, "${prefix}_MEMORY", access, verbose)

    var endian: ByteOrder
        get() = memory.endian
        set(value) {
            memory.endian = value
        }

    override fun reset() {
        super.reset()
        records.forEach { (offset, data) -> store(offset.ulong_z, data) }
    }

    override fun read(ea: ULong, ss: Int, size: Int): ULong = memory.read(ea, ss, size)
    override fun write(ea: ULong, ss: Int, size: Int, value: ULong): Unit = memory.write(ea, ss, size, value)
    override fun fetch(ea: ULong, ss: Int, size: Int): ULong = memory.fetch(ea, ss, size)

    // TODO: loss performance, need testing
//    override fun load(ea: ULong, size: Int, ss: Int, onError: HardwareErrorHandler?): ByteArray = memory.load(ea, size, ss, onError)
//    override fun store(ea: ULong, data: ByteArray, ss: Int, onError: HardwareErrorHandler?) = memory.store(ea, data, ss, onError)
}