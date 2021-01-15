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
package ru.inforion.lab403.kopycat.veos.kernel

import ru.inforion.lab403.common.extensions.first
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.enums.ArgType
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.api.abstracts.APIFunction
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import java.lang.System.currentTimeMillis
import kotlin.time.Duration


class System(val os: VEOS<*>): IAutoSerializable {

    companion object {
        @Transient val log = logger()
    }

    val conf get() = os.conf
    /**
        +-----------------------------+ +----------------------------+
        |        filesystem           | |         network            |
        +-------------+---------------+ +------------+---------------+
                      |                              |
                      |                              |
        +------------------------------------------------------------+
        |             |             System           |               |
        +------------------------------------------------------------+
                      |                              |
                      v                              v
        +-------------+------------------------------+--------------+
        |                         ioSystem                          |
        +-----------------------------------------------------------+
    * */
    // TODO: Direct pass to filesystem and network?
    val ioSystem get() = os.ioSystem
    val filesystem get() = os.filesystem
    val network get() = os.network

    inner class SizeOf : IAutoSerializable {
        val char get() = os.abi.types.char.bytes
        val short get() = os.abi.types.short.bytes
        val int get() = os.abi.types.int.bytes
        val long get() = os.abi.types.long.bytes
        val longLong get() = os.abi.types.longLong.bytes
        val pointer get() = os.abi.types.pointer.bytes
    }

    val sizeOf = SizeOf()

    val currentProcess get() = os.currentProcess

    val abi get() = os.abi

    // TODO: add datetime and force PosixAPI to use it
    val time get() = currentTimeMillis()

    // Allocations
    private val currentAllocator get() = os.currentProcess.allocator

    fun allocate(size: Int, allocator: Allocator = currentAllocator) = allocator.allocate(size)
    fun free(address: Long, allocator: Allocator = currentAllocator) = allocator.free(address)
    fun allocatedBlockSize(address: Long, allocator: Allocator = currentAllocator) = allocator.blockSize(address)

    fun allocateClean(size: Int, allocator: Allocator = currentAllocator)
            = allocate(size, allocator).also { memorySet(it, 0, size) }

    fun allocateAsciiString(string: String, allocator: Allocator = currentAllocator) =
            allocate(string.length + 1, allocator).also { writeAsciiString(it, string) }

    fun allocateArray(data: ByteArray, allocator: Allocator = currentAllocator) =
            allocate(data.size, allocator).also { os.abi.writeBytes(it, data) }

    fun allocateArray(datatype: Datatype, values: List<Long>, allocator: Allocator = currentAllocator): Long {
        val sizeof = datatype.bytes
        val base = allocate(sizeof * values.size, allocator)
        values.forEachIndexed { k, v ->
            os.abi.writeMemory(base + k * sizeof, v, datatype)
        }
        return base
    }

    fun allocateArray(sizeof: Int, values: List<Long>, allocator: Allocator = currentAllocator): Long {
        val datatype = first<Datatype> { it.bytes == sizeof }
        return allocateArray(datatype, values, allocator)
    }

    fun allocatePointersArray(vararg values: Long, allocator: Allocator = currentAllocator) =
            allocateArray(sizeOf.pointer, values.toList(), allocator)

    fun mapFileToMemory(fd: Int, size: Long, offset: Int = 0): Long {
        val fileDescriptor = os.filesystem.share(fd)
        val range = os.currentMemory.fileByAlignment("mmap($fd)", size, fileDescriptor, offset)
        checkNotNull(range) { "Not enough memory for mapping file" }
        return range.first
    }

    // TODO: bug:
    //  unmap() Posix implementation (i think) cat unmap any memory
    //  Name of this function is "unmapFileFromMemory", but VirtualMemory.unmap() also can remove any kind of memory
    //  so rename them or check that requested region is FileSegment
    fun unmapFileFromMemory(start: Long, length: Int) = os.currentMemory.unmap(start, length)

    // Memory utils
    // TODO: is it need? All functions use abi directly
    fun memorySet(address: Long, value: Byte, size: Int) = os.abi.writeBytes(address, ByteArray(size) { value })

    fun readArrayString(address: Long): Array<String> {
        val result = mutableListOf<String>()
        var ea = address
        while (true) {
            val next = os.abi.readPointer(ea)
            if (next == 0L)
                break
            result.add(readAsciiString(next))
            ea += os.abi.types.pointer.bytes
        }
        return result.toTypedArray()
    }

    fun readAsciiString(address: Long): String {
        var result = ""
        var ptr = address
        while (true) {
            val symbol = os.abi.readChar(ptr)
            if (symbol == 0L)
                break
            result += symbol.toChar()
            ptr++
        }
        return result
    }

    fun readWideString(address: Long): String {
        var result = ""
        var ptr = address
        while (true) {
            val symbol = os.abi.readShort(ptr)
            if (symbol == 0L)
                break
            result += symbol.toChar()
            ptr += 2
        }
        return result
    }

    fun writeAsciiString(address: Long, string: String, terminate: Boolean = true) {
        var data = string.toByteArray(Charsets.US_ASCII)
        if (terminate) data += 0
        os.abi.writeBytes(address, data)
    }

    // Undefined reference catch

    private val undefinedReference  = object : APIFunction("undefinedReference") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            throw GeneralException("Undefined reference to $name")
        }
    }

    fun undefinedReferenceHandler(name: String, address: Long) = Handler(name, address, undefinedReference)

    // System subroutines

    val processExitAddress by lazy { os.systemData.word() }
    val threadExitAddress by lazy { os.systemData.word() }
    val idleProcessAddress by lazy { os.systemData.word() }
    val returnerAddress by lazy { os.systemData.word() }
    val restoratorsCount = 6
    val restoratorAddress by lazy { Array(restoratorsCount) { os.systemData.word() } }

    val restoratorResult by lazy { os.systemData.word() }

    val processExit  = object : APIFunction("processExit") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.finer { "System EXIT" }
            return terminate(0)
        }
    }

    val threadExit  = object : APIFunction("threadExit") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            log.finer { "Thread exit" }
            return threadexit()
        }
    }

    val idleEntry  = object : APIFunction("iddleEntry") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long) = throw GeneralException("Idle task should not be executed")
    }

    val returner  = object : APIFunction("returner") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long) = void()
    }

    class Restorator(val os: VEOS<*>, val count: Int) : APIFunction("restorator$count") {
        override val args = emptyArray<ArgType>()
        override fun exec(name: String, vararg argv: Long): APIResult {
            os.abi.writeInt(os.sys.restoratorResult, os.abi.returnValue) // TODO: Long return? // TODO: use context

            os.abi.stackPointerValue += count

            val size = os.abi.pop()
            val args = (0 until size).map {
                os.abi.pop()
            }.reversed().toTypedArray()
            val ra = os.abi.pop()
            val pc = os.abi.pop()

            val stackPointer = os.abi.stackPointerValue
            os.abi.setArgs(args, true)
            val stackDifference = os.abi.stackPointerValue - stackPointer
            check ((stackDifference) % os.sys.sizeOf.int == 0L) { "Not int-aligned stack" }
            while (os.abi.stackPointerValue != stackPointer)
                os.abi.pop()

            os.abi.returnAddressValue = ra
            return redirect(pc)
        }
    }

    val restorator = Array(restoratorsCount) { Restorator(os, it) }

    private val systemHandlers by lazy {
        (
                listOf(
                        Handler(processExit.name, processExitAddress, processExit),
                        Handler(threadExit.name, threadExitAddress, threadExit),
                        Handler(idleEntry.name, idleProcessAddress, idleEntry),
                        Handler(returner.name, returnerAddress, returner)
                ) + (restorator zip restoratorAddress).map { (function, address) -> Handler(function.name, address, function) }
                ).associateBy { it.ea }
    }
    fun getHandler(address: Long) = systemHandlers[address]

    // Environment variables

    private val environmentVars = mutableMapOf<String, Long>()

    fun allocateEnvironmentVariable(name: String, value: String) {
        environmentVars[name] = allocateAsciiString(value, os.systemData)
    }

    fun getEnvironmentVariable(name: String): Long = environmentVars.getOrDefault(name, 0)

    // TODO: may cause memory overuse
    fun allocateEnvironmentArray() = environmentVars.map {
        os.sys.allocateAsciiString("${it.key}=${os.sys.readAsciiString(it.value)}", os.systemData)
    } + 0L

    // Symbols
    // TODO: move to Process
    class SymbolHolder: IAutoSerializable, IConstructorSerializable {
        private val symbols = mutableMapOf<String, Symbol>()

        operator fun get(name: String) = symbols[name]
        operator fun set(name: String, symbol: Symbol) {
            symbols[name] = symbol
        }
        operator fun plusAssign(from: Map<String, Symbol>) = symbols.putAll(from)
    }

    private val symbols = mutableMapOf<String, SymbolHolder>()
    private val systemSymbols = SymbolHolder()
    private val currentSymbols
        get() = symbols.getOrPut(os.currentMemory.name) { SymbolHolder() }

    fun registerSymbols(newSymbols: Map<String, Symbol>) {
        val filtered = newSymbols.filter {
            val sym = currentSymbols[it.key]
            sym == null || (sym.isExternal && it.value.isLocal && it.value.address != 0L) // TODO: is it possible: Local & 0
        }
        currentSymbols += filtered
        os.setHandlers(filtered.values)
    }

    fun addressOfSymbol(name: String) = (systemSymbols[name] ?: currentSymbols[name])?.address


    fun registerSystemSymbol(
            name: String,
            address: Long = 0L,
            ind: Int = 0,
            type: Symbol.Type = Symbol.Type.Local,
            entity: Symbol.Entity = Symbol.Entity.Function
    ) {
        systemSymbols[name] = Symbol(name, address, ind, type, entity)
    }

    fun allocateSystemSymbol(
            name: String,
            datatype: Datatype,
            ind: Int = 0,
            type: Symbol.Type = Symbol.Type.Local,
            entity: Symbol.Entity = Symbol.Entity.Function
    ) = allocate(datatype.bytes, os.systemData).also { registerSystemSymbol(name, it, ind, type, entity) }
}