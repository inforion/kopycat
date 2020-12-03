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
package ru.inforion.lab403.kopycat.modules.common

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.*
import ru.inforion.lab403.kopycat.serializer.*
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.util.*
import java.util.logging.Level
import java.util.zip.GZIPInputStream
import java.util.zip.ZipException

@Suppress("MemberVisibilityCanBePrivate", "unused")


class NAND constructor(
        parent: Module,
        name: String,
        val id: String,
        val blockCount: Int = 1024,
        val colLen: Int,
        val rowLen: Int,
        val pageSize: Int = 2048,
        val spareSize: Int = 64,
        val pagesInBlock: Int = 64
): Module(parent, name) {
    companion object {
        @Transient private val log = logger(Level.WARNING)
    }

    constructor(parent: Module, name: String, id: String, blockCount: Int, colLen: Int, rowLen: Int, pageSize: Int, spareSize: Int, pagesInBlock: Int, bank1: InputStream, bank2: InputStream?) :
            this(parent, name, id, blockCount, colLen, rowLen, pageSize, spareSize, pagesInBlock) { load(bank1, bank2) }
    constructor(parent: Module, name: String, id: String, blockCount: Int, colLen: Int, rowLen: Int, pageSize: Int, spareSize: Int, pagesInBlock: Int, bank1: Resource, bank2: Resource?) :
            this(parent, name, id, blockCount, colLen, rowLen, pageSize, spareSize, pagesInBlock, bank1.inputStream(), bank2?.inputStream())
    constructor(parent: Module, name: String, id: String, blockCount: Int, colLen: Int, rowLen: Int, pageSize: Int, spareSize: Int, pagesInBlock: Int, bank1: File, bank2: File?) :
            this(parent, name, id, blockCount, colLen, rowLen, pageSize, spareSize, pagesInBlock, bank1.inputStream(), bank2?.inputStream())
    constructor(parent: Module, name: String, id: String, blockCount: Int, colLen: Int, rowLen: Int, pageSize: Int, spareSize: Int, pagesInBlock: Int, bank1: String, bank2: String?) :
            this(parent, name, id, blockCount, colLen, rowLen, pageSize, spareSize, pagesInBlock, File(bank1), if(bank2 == null) null else File(bank2))
    constructor(parent: Module, name: String, id: String, blockCount: Int, colLen: Int, rowLen: Int, pageSize: Int, spareSize: Int, pagesInBlock: Int, bank1: ByteBuffer, bank2: ByteBuffer?) :
            this(parent, name, id, blockCount, colLen, rowLen, pageSize, spareSize, pagesInBlock) { load(bank1, bank2) }

    constructor(parent: Module, name: String, id: String, blockCount: Int, colLen: Int, rowLen: Int, pageSize: Int, spareSize: Int, pagesInBlock: Int, bank1: InputStream) :
            this(parent, name, id, blockCount, colLen, rowLen, pageSize, spareSize, pagesInBlock, bank1, null)
    constructor(parent: Module, name: String, id: String, blockCount: Int, colLen: Int, rowLen: Int, pageSize: Int, spareSize: Int, pagesInBlock: Int, bank1: Resource) :
            this(parent, name, id, blockCount, colLen, rowLen, pageSize, spareSize, pagesInBlock, bank1, null)
    constructor(parent: Module, name: String, id: String, blockCount: Int, colLen: Int, rowLen: Int, pageSize: Int, spareSize: Int, pagesInBlock: Int, bank1: File) :
            this(parent, name, id, blockCount, colLen, rowLen, pageSize, spareSize, pagesInBlock, bank1, null)
    constructor(parent: Module, name: String, id: String, blockCount: Int, colLen: Int, rowLen: Int, pageSize: Int, spareSize: Int, pagesInBlock: Int, bank1: String) :
            this(parent, name, id, blockCount, colLen, rowLen, pageSize, spareSize, pagesInBlock, bank1, null)
    constructor(parent: Module, name: String, id: String, blockCount: Int, colLen: Int, rowLen: Int, pageSize: Int, spareSize: Int, pagesInBlock: Int, bank1: ByteBuffer) :
            this(parent, name, id, blockCount, colLen, rowLen, pageSize, spareSize, pagesInBlock, bank1, null)
//
    inner class Ports : ModulePorts(this) {
        val nand = Slave("nand", NAND_BUS_SIZE)  // command
    }

    override val ports = Ports()

    private val blockSize = (pageSize + spareSize) * pagesInBlock

    private fun allocate(size: Int): ByteBuffer = ByteBuffer.allocate(size).apply { order(LITTLE_ENDIAN) }

    enum class Mode { SINGLE, DUAL }

    private var canBeDual = false
    private var mode = Mode.SINGLE

    private val totalSize = blockSize * blockCount

    private val banks = arrayOf(allocate(0), allocate(0))
    private val devId = allocate(5).put(id.unhexlify())

    private val dirtyPages = HashSet<Int>(totalSize / pageSize)
    private val pageMask = (pageSize - 1).inv()
    private val emptyPage = ByteArray(pageSize)

    val realAddress: String
        get() {
            val row = banks[0].position() / (pageSize + spareSize)
            val column = banks[0].position() % (pageSize + spareSize)
            if (column >= pageSize)
                return "OOB: ${row.hex}:${column.hex} = ${banks[0].position().hex8}"
            return "DATA: ${row.hex}:${column.hex} = ${(pageSize*row + column).hex8}"
        }

    enum class Command(val data: Long) {
        NOP(-1),

        RESET_EXECUTE(0xFF),
        READ_ID_INIT(0x90),
        READ_STATUS_EXECUTE(0x70),

        READ_INIT(0x00),
        READ_EXECUTE(0x30),

        READ_RANDOM_INIT(0x05),
        READ_RANDOM_EXECUTE(0xE0),

        PAGE_PROGRAM_INIT(0x80),
        PAGE_PROGRAM_EXECUTE(0x10),

        BLOCK_ERASE_INIT(0x60),
        BLOCK_ERASE_EXECUTE(0xD0);

        companion object {
            fun from(data: Long): Command = Command.values().first { it.data == data }
        }
    }

    fun loadBank(path: String, number: Int) {
        banks[number] = allocate(totalSize)
        var fs = FileInputStream(path)
        try {
            GZIPInputStream(fs).readInto(banks[number])
        } catch (exc: ZipException) {
            fs.close()
            //why the hell fs.reset doesn't work??
            fs = FileInputStream(path)
            fs.readInto(banks[number])
            fs.close()
        }
    }

    private fun loadBank(stream: InputStream, number: Int) {
        banks[number] = allocate(totalSize)
        stream.readInto(banks[number])
    }

    private fun loadBank(stream: ByteBuffer, number: Int) {
        banks[number] = allocate(totalSize)
        banks[number].position(0)
        stream.position(0)
        banks[number].put(stream)
    }

    fun load(stream1: ByteBuffer, stream2: ByteBuffer? = null) {
        loadBank(stream1, 0)
        if (stream2 != null) {
            canBeDual = true
            loadBank(stream2, 1)
        }
    }

    fun load(pathBank1: String, pathBank2: String? = null) {
        loadBank(pathBank1, 0)
        if (pathBank2 != null) {
            canBeDual = true
            loadBank(pathBank2, 1)
        }
    }

    fun load(stream1: InputStream, stream2: InputStream? = null) {
        loadBank(stream1, 0)
        if (stream2 != null) {
            canBeDual = true
            loadBank(stream2, 1)
        }
    }

    private inner class Status : ICoreUnit {
        override val name: String = "NAND Status"

        private val PASS_FAIL = 0
        private val READY_BUSY = 6
        private val WRITE_PROTECT = 7

        var failed = 0
            private set
        var ready = 0
            private set
        var writeProtect = 0
            private set

        fun change(ready: Int = -1, failed: Int = -1, writeProtect: Int = -1) {
            if (ready != -1) {
                this.ready = ready
            }
            if (failed != -1) {
                this.failed = failed
            }
            if (writeProtect != -1) {
                this.writeProtect = writeProtect
            }
//            log.finer { "%s <%s> change status to %s".format(name, state, this) }
        }

        val value: Int get() = failed.shl(PASS_FAIL) or ready.shl(READY_BUSY) or writeProtect.shl(WRITE_PROTECT)

        override fun toString(): String {
            val result = arrayListOf<String>()
            result.add(if (failed == 1) "FAIL" else "PASS")
            result.add(if (ready == 1) "READY" else "BUSY")
            result.add(if (writeProtect == 1) "READONLY" else "WRITABLE")
            return result.joinToString("|")
        }

        override fun reset() {
            super.reset()
            failed = 0
            ready = 1
            writeProtect = 0
            dirtyPages.forEach { pageAddr ->
//                memory.position(pageAddr)
//                memory.put(emptyPage)
            }
        }

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
            return mapOf(
                    "failed" to failed.toString(),
                    "ready" to ready.toString(),
                    "writeProtect" to writeProtect.toString()
            )
        }

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            failed = (snapshot["failed"] as String).toInt()
            ready = (snapshot["ready"] as String).toInt()
            writeProtect = (snapshot["writeProtect"] as String).toInt()
        }
    }

    private inner class Address : ICoreUnit {
        override val name: String = "NAND Address"

        val buffer: ByteBuffer = ByteBuffer.allocate(5).apply { order(LITTLE_ENDIAN) }
        var latched = false
        var value: Int = -1
            private set

        fun latch(transform: (buffer: ByteBuffer) -> Int) {
            value = transform(buffer)
            latched = true
//            log.finer { "%s <%s> address latched = %08X".format(name, state, value) }
        }

        override fun reset() {
            super.reset()
            buffer.clear()
            latched = false
            value = -1
        }

        override fun toString(): String {
            return "Address(%08X, latched = %s)".format(value, latched)
        }

        override fun serialize(ctxt: GenericSerializer) = storeValues(
                "buffer" to storeByteBuffer(buffer),
                "latched" to latched,
                "value" to value)

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            loadByteBuffer(snapshot, "buffer", buffer)
            latched = loadValue(snapshot, "latched")
            value = loadValue(snapshot, "value")
        }
    }

    private val regStatus = object : Register(ports.nand, NAND_STATUS, DWORD, "regStatus") {
        override fun read(ea: Long, ss: Int, size: Int): Long = onWoofStatusRead()
    }

    private val regData = object : Register(ports.nand, NAND_IO, DWORD,"regData") {
        override fun read(ea: Long, ss: Int, size: Int): Long = onDataRead(size)
        override fun write(ea: Long, ss: Int, size: Int, value: Long) = onDataWrite(value, size)
    }

    private val regCmd = object : Register(ports.nand, NAND_CMD, DWORD,"regCmd") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) = onCommandWrite(value)
    }

    private val regAddress = object : Register(ports.nand, NAND_ADDRESS, DWORD,"regAddress") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) = onAddressWrite(value)
    }

    private var state: Command = Command.NOP
    private var address = Address()
    private var status = Status()

    private fun toReadyForAddress() {
        status.change(ready = 0, failed = 0)
        address.reset()
    }

    private fun toFailState(size: Int = 1): Long {
        if (status.failed != 1) {
            log.warning { "%s <%s> error at %s address transient to failed state...".format(name, state, address) }
        }
        state = Command.NOP
        status.change(ready = 1, failed = 1)
        address.reset()
        return bitMask(8 * size) // make FF..FF
    }

    private fun onWoofStatusRead(): Long {
        return status.value.toULong()
    }

    private fun isExceeded(): Boolean {
        return banks.first().remaining() == 0
    }

    private fun onDataRead(size: Int): Long {
        val result = when (state) {
            Command.READ_ID_INIT -> { // if (devId.remaining() == 0) toFailState(dtyp) else devId.get().toULong()
                if (devId.remaining() == 0)
                    toFailState(size)
                else when (size) {
                    WORD.bytes -> when (mode) {
                        Mode.DUAL -> {
                            val boo = devId.get().toULong()
                            (boo shl 8) or boo
                        }
                        Mode.SINGLE -> devId.get().toULong()
                    }
                    BYTE.bytes -> devId.get().toULong()
                    else -> toFailState(size)
                }
            }

            Command.READ_STATUS_EXECUTE -> status.value.toULong()

            Command.READ_EXECUTE, Command.READ_RANDOM_EXECUTE ->

                if (isExceeded()) {
                    toFailState(size)
                } else when (size) {
                    DWORD.bytes -> when (mode) {
                        Mode.SINGLE -> banks[0].int.toULong()
                        Mode.DUAL -> {
                            val short0 = banks[0].short
                            val short1 = banks[1].short
                            val b3 = short1[15..8].toULong()
                            val b2 = short1[7..0].toULong()
                            val b1 = short0[15..8].toULong()
                            val b0 = short0[7..0].toULong()
                            (b3 shl 24) or (b1 shl 16) or (b2 shl 8) or b0
                        }
                    }
                    WORD.bytes -> (banks[1].get().toULong() shl 8) or (banks[0].get().toULong())
                    BYTE.bytes -> banks[0].get().toULong()
                    else -> toFailState(size)
                }

            Command.NOP -> bitMask(8 * size) // make FF..FF

            else -> toFailState(size)
        }

//        log.finest { "%s <%s> read %d bytes from %08X = %08X".format(name, state, dtyp, memory.position(), result) }
        return result
    }

    private fun position(): Int = banks[0].position()

    private fun onDataWrite(value: Long, size: Int) {
        when (state) {
            Command.PAGE_PROGRAM_EXECUTE -> {
                if (isExceeded()) {
                    toFailState(size)
                } else when (size) {
                    BYTE.bytes -> {
                        dirtyPages.add(position() and pageMask)
                        banks[0].put(value[7..0].toByte())
                    }
                    WORD.bytes -> {
                        dirtyPages.add(position() and pageMask)
                        banks[1].put(value[15..8].toByte())
                        banks[0].put(value[7..0].toByte())
                    }
                    DWORD.bytes -> when (mode) {
                        Mode.SINGLE -> {
                            dirtyPages.add(position() and pageMask)
                            banks[0].putInt(value.toInt())
                        }
                        Mode.DUAL -> {
                            dirtyPages.add(position() and pageMask)
                            val b0 = value[7..0]
                            val b2 = value[15..8]
                            val b1 = value[23..16]
                            val b3 = value[31..24]
                            val short1 = (b3 shl 8) or b2
                            val short0 = (b1 shl 8) or b0
                            banks[0].putShort(short0.toShort())
                            banks[1].putShort(short1.toShort())
                        }
                    }
                    else -> toFailState(size)
                }
            }
            else -> { }
        }
    }

    private fun onCommandWrite(value: Long) {
        val lobyte = value[7..0]
        val hibyte = value[15..8]

        mode = if (canBeDual && hibyte == lobyte) Mode.DUAL else Mode.SINGLE

        log.finest { "%s <%s> command area written %02X".format(name, state, lobyte) }

        val oldState = state
        state = Command.from(lobyte)

        when (state) {
            Command.RESET_EXECUTE -> {
                log.fine { "%s <%s> reset".format(name, state) }
                registers.forEach { it.data = 0 }
                state = Command.NOP
                status.reset()
                address.reset()
            }

            Command.READ_ID_INIT -> {
                log.fine { "%s <%s> id = %s".format(name, state, devId.array().hexlify()) }
                toReadyForAddress()
                status.change(ready = 1)
            }

            Command.READ_STATUS_EXECUTE -> { }

            Command.READ_INIT -> toReadyForAddress()
            Command.READ_EXECUTE -> {
                if (oldState == Command.READ_INIT) {
//                    log.severe { realAddress}
                    log.finer { "%s <%s> address = %s".format(name, state, address) }
                    status.change(ready = 1, failed = 0)
                } else {
                    toFailState()
                }
            }


            Command.READ_RANDOM_INIT -> toReadyForAddress()
            Command.READ_RANDOM_EXECUTE -> {
                if (oldState == Command.READ_RANDOM_INIT) {
//                    log.severe { realAddress }
                    log.finer { "%s <%s> address = %s".format(name, state, address) }
                    status.change(ready = 1, failed = 0)
                } else {
                    toFailState()
                }
            }

            Command.PAGE_PROGRAM_INIT -> toReadyForAddress()
            Command.PAGE_PROGRAM_EXECUTE -> { }

            Command.BLOCK_ERASE_INIT -> toReadyForAddress()
            Command.BLOCK_ERASE_EXECUTE -> {
                if (oldState == Command.BLOCK_ERASE_INIT) {
                    log.fine { "%s <%s> address = %s".format(name, state, address) }
                    banks[0].put(ByteArray(blockSize))
                    if (mode == Mode.DUAL)
                        banks[1].put(ByteArray(blockSize))
                    status.change(ready = 1, failed = 0)
                } else {
                    toFailState()
                }
            }

            Command.NOP -> { }
        }
    }

    private fun limit(): Int = banks[0].limit()

    private fun onAddressWrite(value: Long) {
        val byte = value[7..0].toByte()

//        log.finest { "%s <%s> address area written %02X".format(name, state, byte) }

        if (address.buffer.remaining() == 0) {
//            log.finer { "%s buffer address is full".format(name) }
            toFailState()
            return
        }

        address.buffer.put(byte)

        when (state) {
            Command.READ_ID_INIT -> {
                if (address.buffer.position() == 1) {
                    address.latch { buf -> buf.get(0).toInt() }
                    devId.rewind()
                }
            }

            Command.READ_INIT, Command.PAGE_PROGRAM_INIT -> {
                if (address.buffer.position() == colLen + rowLen) {
                    address.latch { buf ->
                        val col = getData(buf, colLen)
                        val row = getData(buf, rowLen, colLen)
                        row * (pageSize + spareSize) + col
                    }
                    if (address.value < 0 || address.value >= limit()) {
                        log.warning { "%s setup wrong %s limit: %08X".format(name, address, limit()) }
                        toFailState()
                        return
                    }
                    banks[0].position(address.value)
                    if (mode == Mode.DUAL)
                        banks[1].position(address.value)
//                    log.fine { "%s <%s> memory position = %08X".format(name, state, memory.position()) }
                    if (state == Command.PAGE_PROGRAM_INIT) {
                        state = Command.PAGE_PROGRAM_EXECUTE
                        log.fine { "%s <%s> address = %s".format(name, state, address) }
                        status.change(ready = 1, failed = 0)
                    }
                }
            }

            Command.READ_RANDOM_INIT ->  {
                if (address.buffer.position() == colLen) {
                    address.latch { buf ->
                        val col = getData(buf, colLen)
                        val row = banks[0].position() / (pageSize + spareSize)
                        row * (pageSize + spareSize) + col
                    }
                    if (address.value < 0 || address.value >= limit()) {
                        log.warning { "%s setup wrong %s limit: %08X".format(name, address, limit()) }
                        toFailState()
                        return
                    }
                    banks[0].position(address.value)
                    if (mode == Mode.DUAL)
                        banks[1].position(address.value)
//                    log.fine { "%s <%s> memory position = %08X".format(name, state, memory.position()) }
                }
            }


            Command.BLOCK_ERASE_INIT -> {
                if (address.buffer.position() == 2) {
                    address.latch { buf ->
                        val row = buf.getShort(0).toUInt()
                        row * (pageSize + spareSize)
                    }
                    if (address.value < 0 || address.value >= limit()) {
                        log.warning { "%s setup wrong %s limit: %08X".format(name, address, limit()) }
                        toFailState()
                        return
                    }
                    banks[0].position(address.value)
                    if (mode == Mode.DUAL)
                        banks[1].position(address.value)
//                    log.finer { "%s <%s> memory position = %08X".format(name, state, memory.position()) }
                }
            }

            else -> {
                toFailState()
                return
            }
        }
    }

    private fun  getData(buf: ByteBuffer, len: Int, offset: Int = 0): Int = when(len) {
        1 -> buf.get(offset).toUInt()
        2 -> buf.getShort(offset).toUInt()
        3 -> buf.get(offset).toUInt() or (buf.get(offset+1).toUInt() shl 8) or (buf.get(offset+2).toUInt() shl 16)
        4 -> buf.getInt(offset)
        else -> throw Exception("Wrong address len!")
    }

    override fun reset() {
        super.reset()
        registers.forEach { it.reset() }
        state = Command.NOP
        status.reset()
        address.reset()
    }

    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
            "devId" to storeByteBuffer(devId),
            "status" to status.serialize(ctxt),
            "address" to address.serialize(ctxt),
            "state" to state.toString(),
            "canBeDual" to canBeDual,
            "nand1" to ctxt.storeBinary("nand1", banks[0]),
            "nand2" to ctxt.storeBinary("nand2", banks[1]))

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        restoreCommon(ctxt, snapshot)

        if (banks[0].limit() == 0)
            banks[0] = allocate(totalSize)

        if (!ctxt.loadBinary(snapshot, "nand1", banks[0]))
            throw IllegalStateException("Can't load nand1!")

        if (canBeDual && ctxt.isBinaryExists("nand2")) {
            if (banks[1].limit() == 0)
                banks[1] = allocate(totalSize)

            ctxt.loadBinary(snapshot, "nand2", banks[1])
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun restore(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        restoreCommon(ctxt, snapshot)

        ctxt.restoreBinary(snapshot, "nand1", banks[0], dirtyPages, pageSize)
        if (canBeDual && ctxt.isBinaryExists("nand2")) {
            ctxt.restoreBinary(snapshot, "nand2", banks[1], dirtyPages, pageSize)
        }

        dirtyPages.clear()
    }

    @Suppress("UNCHECKED_CAST")
    private fun restoreCommon(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        loadByteBuffer(snapshot, "devId", devId)
        canBeDual = loadValue(snapshot, "canBeDual") { false }
        status.deserialize(ctxt, snapshot["status"] as Map<String, Any>)
        address.deserialize(ctxt, snapshot["address"] as Map<String, Any>)
        state = loadEnum(snapshot, "state")
    }
}