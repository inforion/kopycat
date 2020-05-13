package ru.inforion.lab403.kopycat.modules.common

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.modules.*
import ru.inforion.lab403.kopycat.serializer.loadEnum
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*
import java.util.logging.Level



class SD(parent: Module,
         name: String,
         cid: String,
         csd: String,
         capacity: Int = 0x1DF8000,
         private val page: Int = 0x2000
) : Module(parent, name) {
    companion object {
        private val log = logger(Level.INFO)
    }

    constructor(parent: Module, name: String, cid: String, csd: String, capacity: Int, page: Int, content: InputStream) :
            this(parent, name, cid, csd, capacity, page) {
        content.readInto(memory)
    }
    constructor(parent: Module, name: String, cid: String, csd: String, capacity: Int, page: Int, content: Resource) :
            this(parent, name, cid, csd, capacity, page, content.inputStream())

    constructor(parent: Module, name: String, cid: String, csd: String, capacity: Int, page: Int, content: File) :
            this(parent, name, cid, csd, capacity, page, content.inputStream())

    constructor(parent: Module, name: String, cid: String, csd: String, capacity: Int, page: Int, content: String) :
            this(parent, name, cid, csd, capacity, page, File(content))

    private fun allocate(size: Int): ByteBuffer = ByteBuffer.allocate(size).apply { order(ByteOrder.LITTLE_ENDIAN) }

    inner class Ports : ModulePorts(this) {
        val sd = Slave("sd", SD_BUS_SIZE)
    }

    override val ports = Ports()

    private val memory = allocate(capacity)

    private val cidBuf = allocate(16).put(cid.unhexlify())
    private val csdBuf = allocate(16).put(csd.unhexlify())

    private var buffer = allocate(0)

    private val dirtyPages = HashSet<Int>(capacity / page)
    private val pageMask = (page - 1).inv()
    private val emptyPage = ByteArray(page)

    private val regDataIO = object : Register(ports.sd, SD_ARGUMENT, DWORD, "SD_ARGUMENT") {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            super.read(ea, ss, size)
            return onDataRead()
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            onDataWrite(value)
        }
    }

    private val regArgumentOut = Register(ports.sd, SD_ARGUMENT, DWORD, "SD_ARGUMENT")

    private val regCommandOut = object : Register(ports.sd, SD_COMMAND, DWORD, "SD_COMMAND") {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            onCommandWrite(value)
        }
    }

    private val regStatusIn = Register(ports.sd, SD_STATUS, DWORD, "SD_STATUS")

    private val regControlIO = object : Register(ports.sd, SD_CONTROL, DWORD, "SD_CONTROL") {
        override fun read(ea: Long, ss: Int, size: Int): Long {
            super.read(ea, ss, size)
            return onControlReadIO()
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            onControlWriteIO(value)
        }
    }

    private val regResponseIn = object : Register(ports.sd, SD_RESPONSE, DWORD, "SD_RESPONSE") {
        val values = Array(4) { 0L }

        fun clear() = values.fill(0)

        override fun read(ea: Long, ss: Int, size: Int): Long = values[ss]
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            values[ss] = value
        }

        override fun reset() {
            super.reset()
            clear()
        }

        override fun serialize(ctxt: GenericSerializer): Map<String, Any> =
                super.serialize(ctxt) + mapOf("values" to values)

        @Suppress("UNCHECKED_CAST")
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            super.deserialize(ctxt, snapshot)
            (snapshot["values"] as ArrayList<Long>).forEachIndexed { index, l -> values[index] = l }
        }
    }

    enum class Command(val id: Long) {
        CMD_NOP(-1),
        CMD_GO_IDLE_STATE(0),
        CMD_MMC_SEND_OPCOND(1),
        CMD_ALL_SEND_CID(2),
        CMD_SET_RELATIVE_ADDR(3),
        CMD_SD_CMD_SET_DSR(4),
        CMD_SELECT_DESELECT_CARD(7),
        CMD_SEND_CSD(9),
        CMD_SEND_CID(10),
        CMD_SEND_STATUS(13),
        CMD_SET_BLOCKLEN(16),
        CMD_READ_SINGLE_BLOCK(17),
        CMD_READ_MULTIPLE_BLOCK(18),
        CMD_WRITE_SINGLE_BLOCK(24),
        CMD_WRITE_MULTIPLE_BLOCK(25),
        CMD_SET_FIRST_GRP_ADDR(35),
        CMD_SET_LAST_GRP_ADDR(36),
        CMD_ERASE_SELECTED(38);

        companion object {
            fun from(id: Long): Command = Command.values().first { it.id == id }
        }
    }

    private val OPCOND_MAGIC_READY: Long = 0x80FFC000
    //    private val COMMAND_EXEC_TIME: Long = 2 // ms
    private val COMMAND_EXEC_START_MASK: Long = 0xBFC6.inv()
    private var execCommand = Command.CMD_NOP
//    private var executionStartTime = 0L

    fun onControlReadIO(): Long {
//        if (dev.timer.timestamp(Time.ms) - executionStartTime > COMMAND_EXEC_TIME) {
//
//        }
        regControlIO.data = regControlIO.data or 0x04
        return regControlIO.data
    }

    fun onControlWriteIO(value: Long) {
        if (value != 0xFFFFFFFF) {
            log.warning { "Unexpected control register value = %08X".format(value) }
            return
        }
//        ports.sd.write(DWORD, SD_CONTROL, value and COMMAND_EXEC_START_MASK)
        regControlIO.data = value and COMMAND_EXEC_START_MASK
//        executionStartTime = dev.timer.timestamp(Time.ms)
    }

    fun onDataRead(): Long {
        if (buffer.remaining() == 0) {
            log.warning { "Try to read of empty buffer" }
            return 0xFFFFFFFF
        }
        return buffer.int.toULong()
    }

    fun onDataWrite(value: Long) {
        if (buffer.remaining() == 0) {
            log.warning { "Writing data %08X to data register when buffer is full and written".format(value) }
        } else {
            buffer.putInt(value.toInt())
            if (buffer.remaining() == 0) {
                log.fine { "%s write [%08X]: %s".format(name, memory.position(), buffer.array().hexlify()) }
                dirtyPages.add(memory.position() and pageMask)
                memory.put(buffer.array())
                regStatusIn.data = clearBit(regStatusIn.data, 9)
            }
        }
    }

    fun onCommandWrite(value: Long) {
        regResponseIn.clear()
        regStatusIn.data = 0

        val stopBit = value[31]

        if (stopBit != 1L) {
            return
        }

        val prevCommand = execCommand
        execCommand = Command.from(value[5..0])

        log.finest { "%s -> %s arg = %08X".format(name, execCommand, regArgumentOut.data) }

        when (execCommand) {
            Command.CMD_GO_IDLE_STATE -> {
                regResponseIn.clear()
            }

            Command.CMD_MMC_SEND_OPCOND -> {
                regResponseIn.values[0] = OPCOND_MAGIC_READY
            }

            Command.CMD_ALL_SEND_CID, Command.CMD_SEND_CID -> {
                cidBuf.rewind()
                repeat(regResponseIn.values.size) { regResponseIn.values[it] = cidBuf.int.asULong }
            }

            Command.CMD_SEND_CSD -> {
                csdBuf.rewind()
                repeat(regResponseIn.values.size) { regResponseIn.values[it] = csdBuf.int.asULong }
            }

            Command.CMD_SET_BLOCKLEN -> {
                buffer = ByteBuffer.allocate(regArgumentOut.data.toInt()).apply { order(ByteOrder.LITTLE_ENDIAN) }
            }

            Command.CMD_READ_SINGLE_BLOCK -> {
                if (buffer.limit() == 0) {
                    log.warning { "Trying to read uninitialized SD flash" }
                    if (isDebuggerPresent) debugger.isRunning = false
                    return
                }
                val offset = regArgumentOut.data.toInt()
                memory.position(offset)
                memory.get(buffer.array())
                // System.arraycopy(memory.array(), offset, buffer.array(), 0, buffer.limit())
                buffer.rewind()
                // Magic ready for controller
                regStatusIn.data = regStatusIn.data or (buffer.limit().ushr(2).shl(0x11).toULong())
                log.fine { "%s read [%08X]: %s".format(name, offset, buffer.array().hexlify()) }
            }

            Command.CMD_WRITE_SINGLE_BLOCK -> {
                if (buffer.limit() == 0) {
                    log.warning { "Trying to write to uninitialized SD flash" }
                    if (isDebuggerPresent) debugger.isRunning = false
                    return
                }
                val offset = regArgumentOut.data.toInt()
                memory.position(offset)
                buffer.rewind()
                // Magic not ready for controller
                regStatusIn.data = setBit(regStatusIn.data, 9)
                regControlIO.data = regControlIO.data or 0x04
            }

            Command.CMD_SEND_STATUS -> {
                // Magic after write don't know whether it used elsewhere
                if (prevCommand == Command.CMD_WRITE_SINGLE_BLOCK) {
                    regResponseIn.values[0] = 0x800
                } else {
                    log.warning { "Request status not after write command -> response unchanged" }
                }
            }

            else -> {
                log.warning { "Command <%s> was not implemented".format(execCommand) }
            }
        }
    }

    override fun reset() {
        super.reset()
        regArgumentOut.reset()
        regCommandOut.reset()
        regControlIO.reset()
        regDataIO.reset()
        regResponseIn.reset()
        regStatusIn.reset()
        dirtyPages.forEach { pageAddr ->
            memory.position(pageAddr)
            memory.put(emptyPage)
        }
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super.serialize(ctxt) + mapOf(
                "buffer" to buffer.array().hexlify(),
                "flashCID" to cidBuf.array().hexlify(),
                "flashCSD" to csdBuf.array().hexlify(),
                "execCommand" to execCommand.toString()
        ) + ctxt.storeBinary("sd.bin", memory) +
                ctxt.storeByteBufferData("buffer", buffer) +
                ctxt.storeByteBufferData("flashCID", cidBuf) +
                ctxt.storeByteBufferData("flashCSD", csdBuf)

    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        restoreCommon(ctxt, snapshot)

        if (!ctxt.loadBinary(snapshot, "sd.bin", memory))
            throw IllegalStateException("Can't load sd.bin")
    }

    @Suppress("UNCHECKED_CAST")
    override fun restore(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        restoreCommon(ctxt, snapshot)

        ctxt.restoreBinary(snapshot, "sd.bin", memory, dirtyPages, page)
        dirtyPages.clear()
    }

    private fun restoreCommon(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        execCommand = loadEnum(snapshot, "execCommand")

        buffer = ByteBuffer.wrap((snapshot["buffer"] as String).unhexlify()).apply { order(ByteOrder.LITTLE_ENDIAN) }
        cidBuf.put((snapshot["flashCID"] as String).unhexlify())
        csdBuf.put((snapshot["flashCSD"] as String).unhexlify())

        ctxt.restoreByteBufferData(snapshot, "buffer", buffer)
        ctxt.restoreByteBufferData(snapshot, "flashCID", cidBuf)
        ctxt.restoreByteBufferData(snapshot, "flashCSD", csdBuf)
    }
}