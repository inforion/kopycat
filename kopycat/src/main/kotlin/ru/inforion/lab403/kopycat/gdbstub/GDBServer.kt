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
package ru.inforion.lab403.kopycat.gdbstub

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.auxiliary.ANetworkThread
import ru.inforion.lab403.kopycat.cores.base.exceptions.BreakpointException
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.interactive.REPL
import ru.inforion.lab403.kopycat.interfaces.IDebugger
import java.util.logging.Level
import kotlin.concurrent.thread

@Suppress("UNUSED_PARAMETER")
/**
 * {RU}
 *
 * @param port номер порта для GDB
 * @param start запустить GDB-сервер при создании
 * @param binaryProtoEnabled разрешить использование бинароного протокола GDB (команда X)
 * {RU}
 */
class GDBServer constructor(
        port: Int,
        start: Boolean,
        val binaryProtoEnabled: Boolean
) : ANetworkThread(port, "GDB_SERVER", bufSize = 0x1000, start = start, isDaemon = false) {

    companion object {
        @Transient val log = logger(Level.INFO)
    }

    // ida pro gdb server don't clear breakpoints
    private val breakpoints = object {
        private val items = mutableListOf<Long>()

        fun add(bpType: GDB_BPT, address: Long): Boolean {
            items.add(address)
            return debugger.bptSet(bpType, address, null)
        }

        fun remove(address: Long): Boolean {
            val result = debugger.bptClr(address)
            items.remove(address)
            return result
        }

        fun clear() {
            items.forEach { debugger.bptClr(it) }
            items.clear()
        }

        fun count() = items.size
    }

    override fun toString(): String = "$name(port=$port,alive=$isAlive)"

    /**
     * This property made to simple code of checking on null-safety everywhere.
     * Here checking isn't required because on top level placed exception handler
     * that handle and respond to client.
     *
     * TODO: nullableInternalDebugger should be refactored
     */
    private var nullableInternalDebugger: IDebugger? = null

    private val debugger get() = nullableInternalDebugger!!

    fun isDebuggerInit() = nullableInternalDebugger != null

    fun debuggerModule() = debugger

    fun debuggerModule(newDebugger: IDebugger?) {
        if (clientProcessing && isDebuggerInit()) {
            log.severe { "Can't set debugger module=$newDebugger due to GDB client is now processing... disconnect first" }
            return
        }

        log.info { "Set new debugger module $newDebugger for $this" }
        nullableInternalDebugger = newDebugger
    }

    private var messageProcessing = false
    var clientProcessing = false

    override fun onConnect(): Boolean {
        if (!isDebuggerInit())
            log.severe { "GDB Client connected but debugger wasn't initialized!" }

        clientProcessing = true
        log.info { "Setup breakpoints count ${breakpoints.count()}" }
        return true
    }

    override fun onDisconnect(){
        clientProcessing = false
        breakpoints.clear()
    }

    private var buffer = String()

    private fun send(msg: GDBMessage) {
        val data = msg.build()
        log.finest { "GDB STUB -> $data" }
        send(data.convertToBytes())
    }

    private fun processMessageBuffer(data: ByteArray): List<GDBMessage> {
        buffer += data.convertToString()
        val result = mutableListOf<GDBMessage>()
        while (true) {
            var start = buffer.indexOf('$')
            if (start > 0 || (start == -1 && buffer.isNotEmpty())) {
                result.add(GDBMessage.service(buffer[0]))
                buffer = buffer.substring(1)
                continue
            }

            if (start == -1)
                return result

            var end = buffer.indexOf('#')
            if (end == -1)
                return result

            start += 1
            end += 2
            if (start >= buffer.length || end >= buffer.length)
                return result

            val tmp = buffer.substring(start..end)
            val msgAndCrc = tmp.split('#', limit = 2) // limit for binary extension of GDB RSP
            val msg = if (msgAndCrc.size != 2) {
                log.warning { "GDB Message [$tmp] has wrong format [msg#crc], skipping..." }
                GDBMessage.empty
            } else {
                val msg = msgAndCrc[0]
                val crc = msgAndCrc[1]
                GDBMessage.message(msg, crc.hexAsUInt)
            }
            result.add(msg)
            buffer = buffer.substring(end + 1)
        }
    }

    override fun onReceive(data: ByteArray): Boolean {
        if (!isDebuggerInit()) {
            log.severe { "GDB Client received data but debugger wasn't initialized!" }
            return true
        }

        // we won't process any packets until we get any response from system, except 0x03, interrupt request
        if (messageProcessing) {
            log.warning { "Message ignored due to processing now..." }
            return true
        }

        processMessageBuffer(data).forEach { msg ->
            if (msg.service) {
                when (msg.cmd) {
                    '+' -> log.finest { "Received acknowledge message +" }
                    '-' -> log.warning { "Client rejected last message!" }
                    0x03.toChar() -> {
                        log.info { "GDB Request target halt!" }
                        debugger.halt()
                    }
                }
            } else if (msg != GDBMessage.empty) {
                val chksum = GDBMessage.calcChecksum(msg.packet)
                if (msg.checksum == chksum) {
                    // ATTENTION: Message must be processed and after then sync signal sent to IDA!!!
                    // Otherwise the worst performance would be reached!!!
                    // Continue message executed in other thread
                    try {
                        sendAckResponse()
                        log.finer { "Message [$msg]" }
                        processMsg(msg)

//                        val time = measureNanoTime { processMsg(msg) }
//                        log.finer { "Elapsed %,d ns on message".format(time) + " [$msg]" }
                    } catch (error: Exception) {
                        log.severe { "Processing message [$msg] failed!" }
                        error.printStackTrace()
                        sendRejectResponse()
                    }
                } else {
                    log.warning { "GDB Message [$msg] checksum incorrect: $chksum != ${msg.checksum}" }
                    sendRejectResponse()
                }
            } else {
                sendRejectResponse()
            }
        }
        return true
    }

    private val signalByException get(): GDB_SIGNAL = when (debugger.exception()) {
        null -> GDB_SIGNAL.SIGTRAP   // when nothing happen should be BREAKPOINT for IDA
        is BreakpointException -> GDB_SIGNAL.SIGTRAP
        is MemoryAccessError -> GDB_SIGNAL.SIGSEGV
        else -> GDB_SIGNAL.SIGSYS
    }

    //this is done in separate thread only to allow client to request interrupt with async 0x03
    private fun processMsg(msg: GDBMessage) {
        messageProcessing = true

//        TODO: Unknown message received: [$S0b#E5]

        when (msg.cmd) {
            '!' -> processExtendedDebugRequest()
            '?' -> processHaltReason()
            'q' -> processGeneralRequest(msg)
            'v' -> processExtendedRequest(msg)
            'p' -> processRegisterRead(msg)
            'P' -> processRegisterWrite(msg)
            'g' -> processAllRegistersRead()
            'c' -> processContinue(null)
            'C' -> processContinue(msg)
            's' -> processStep()
            'H' -> processSetThreadInfo(msg)
            'm' -> processReadMemText(msg)
            'M' -> processWriteMemText(msg)
            'X' -> processWriteMemBinary(msg)
            'k' -> processKillRequest()
            'z' -> processClearBpt(msg)
            'Z' -> processSetBpt(msg)
            'B' -> processToggleBpt(msg)
            'D' -> processDetach(msg)

            else -> {
                log.warning { "Unknown message received: [$msg]" }
                sendEmptyResponse()
            }
        }
        messageProcessing = false
    }

    private fun processRegisterWrite(msg: GDBMessage) {
        val params = msg.data.split('=')

        val reg = params[0].hexAsInt
        val value = params[1].hexAsULong.swap32()

        log.fine { "GDB Request register $reg write 0x${value.hex8}" }
        try {
            debugger.regWrite(reg, value)
            sendOkResponse()
        } catch (e: Exception) {
            log.warning { "GDB Failed to set register [$reg] = 0x${value.hex8}" }
            sendErrorResponse(1)
        }
    }

    private fun processStep() {
        log.fine { "GDB Request single step" }
        if (!debugger.isRunning) {
            debugger.step()
        } else {
            log.warning { "GDB Step requested when target running...halt the target!" }
            debugger.halt()
        }
        sendInterruptRequest(signalByException.id)
    }

    private fun processSetThreadInfo(msg: GDBMessage) {
        // Inform that further operations applies to some thread
        // Hc - continue operations
        // Hg - all operations
        val op = msg.data[0]
        val thId = msg.data.substring(1).hexAsInt
        when (op) {
            'c' -> processSetContinueThread(thId)
            'g' -> processSetAllOpThread(thId)
            else -> {
                log.warning { "Got wrong op argument in message [$msg]" }
                sendEmptyResponse()
            }
        }
    }

    private fun processSetAllOpThread(thId: Int) {
//        log.fine { "GDB All further operations applies to thread [$thId]" }
        sendOkResponse()
    }

    private fun processSetContinueThread(thId: Int) {
//        log.fine { "GDB Further continue operations applies to thread [$thId]" }
        sendOkResponse()
    }

    private fun processContinue(msg: GDBMessage?) {
        val signal = msg?.data?.hexAsInt ?: 5
        log.fine { "GDB Request continue with signal 0x${signal.hex2}" }
        if (!debugger.isRunning) {
            thread {
                debugger.cont()
                sendInterruptRequest(signalByException.id)
            }
        } else {
            log.warning { "GDB: Target already running...halt the target!" }
            debugger.halt()
        }
    }

    private fun processReadMemText(msg: GDBMessage) {
        // read mem: m addr,len

        val params = msg.data.split(',')

        if (params.size < 2) {
            log.warning { "Got wrong number of parameters while reading mem: [$msg]" }
            sendEmptyResponse()
            return
        }

        val address = params[0].hexAsULong
        val len = params[1].hexAsUInt

        log.fine { "GDB Request $len bytes at 0x${address.hex8}" }
        try {
            val data = debugger.dbgLoad(address, len)
            val response = data.joinToString(separator = "") { it.lhex2 }
            sendMessageResponse(response)
        } catch (error: Exception) {
            log.warning { "GDB: Error reading $len bytes at 0x${address.hex8} -> $error" }
            //TODO: replace err magic value with something
            sendErrorResponse(1)
        }

    }

    private fun processWriteMemIntern(address: Long, data: ByteArray) {
        log.fine { "GDB Request write ${data.size} bytes at 0x${address.hex8} -> ${data.hexlify()}" }
        try {
            debugger.dbgStore(address, data)
            sendOkResponse()
        } catch(error: Exception) {
            log.warning { "GDB: Error writing ${data.size} bytes at 0x${address.hex8} (${data.hexlify()}) -> $error" }
            sendErrorResponse(1)
        }
    }

    private fun processWriteMemText(msg: GDBMessage) {
        // write mem: M1ed7c7,1:aa --> addr,size:text

        val params = msg.data.split(',', ':', limit = 3)
        if (params.size != 3) {
            log.warning { "Got wrong number of parameters while writing mem: [$msg]" }
            sendEmptyResponse()
            return
        }

        val address = params[0].hexAsULong
        val size = params[1].hexAsUInt
        val data = params[2].unhexlify()

        if (size != data.size) {
            log.warning { "Got wrong size and data size for writing mem: [$msg]" }
            sendEmptyResponse()
            return
        }

        processWriteMemIntern(address, data)
    }

    private fun processWriteMemBinary(msg: GDBMessage) {
        // write mem: X1ed7c7,2:aa --> addr,size:binary
        val params = msg.data.split(',', ':', limit = 3)

        if (params.size != 3) {
            log.warning { "Got wrong number of parameters while writing mem: [$msg]" }
            sendEmptyResponse()
            return
        }

        val address = params[0].hexAsULong
        val size = params[1].hexAsUInt
        val memory = params[2].convertToBytes()

        // starting sequence of X command support (GDB client should send empty memory write request
        // if response is empty then server doesn't support binary exchange
        if (size == 0 && !binaryProtoEnabled) {
            log.severe { "GDB Client try to intercourse using binary protocol but " +
                    "due to SystemWorkbench/arm-none-eabi-gdb bug it was disabled by default " +
                    "to enable it please start Kopycat with option --gdb-bin-proto" }
            sendEmptyResponse()
            return
        }

        if (size != memory.size ) {
            log.severe { "Requested size $size not equals data size ${memory.size}: [$msg]" }
            sendEmptyResponse()
            return
        }

        processWriteMemIntern(address, memory)
    }

    private fun processKillRequest() = debugger.halt()

    private fun checkSizeAndWarn(ea: Long, size: Int) {
//        if (size > 1) log.warning { "Breakpoint ea=0x${ea.hex} size=$size > 1 set/clear for the first address only!" }
    }

    private fun processClearBpt(msg: GDBMessage) {
        // clear bp: z type,address,count

        val params = msg.data.split(',')

        if (params.size < 3) {
            log.warning { "Got wrong number of parameters while clearing BP: [$msg]" }
            sendEmptyResponse()
            return
        }

        val bpTypeValue = params[0].hexAsUInt
        val address = params[1].hexAsULong
        val count = params[2].hexAsUInt
        val bpType = convert<GDB_BPT>(bpTypeValue)

        log.fine { "GDB Request clearing $count bytes ${bpType.name} breakpoint at 0x${address.hex8}" }
        checkSizeAndWarn(address, count)
        if (breakpoints.remove(address)) sendOkResponse() else sendEmptyResponse()
    }

    private fun processSetBptIntern(bpType: GDB_BPT, address: Long, count: Int) {
        log.fine { "GDB Request setting $count bytes ${bpType.name} breakpoint at 0x${address.hex8}" }
        checkSizeAndWarn(address, count)
        breakpoints.add(bpType, address)
        sendOkResponse()
    }

    private fun processSetBpt(msg: GDBMessage) {
        // set bp: z type,address,count
        val params = msg.data.split(',')

        if (params.size < 3) {
            log.warning { "Got wrong number of parameters while setting BP: [$msg]" }
            sendEmptyResponse()
            return
        }

        val bpTypeValue = params[0].hexAsUInt
        val address = params[1].hexAsULong
        val count = params[2].hexAsUInt
        val bpType = convert<GDB_BPT>(bpTypeValue)

        processSetBptIntern(bpType, address, count)
    }

    private fun processToggleBpt(msg: GDBMessage) {
        val params = msg.data.split(',')

        if (params.size < 2) {
            log.warning { "Got wrong number of parameters while setting deprecated BP: [$msg]" }
            sendEmptyResponse()
            return
        }

        val address = params[0].hexAsULong
        val setReset = params[1]

        if (setReset == "S") {
            processSetBptIntern(GDB_BPT.SOFTWARE, address, 1)
        } else {
            log.severe { "Unknown last argument for deprecated BP: [$msg]" }
            sendEmptyResponse()
        }
    }

    private fun processDetach(msg: GDBMessage) {
        sendEmptyResponse()
        clientProcessing = false
    }

    private fun processExtendedRequest(msg: GDBMessage) = sendEmptyResponse()

    private fun processGeneralRequest(msg: GDBMessage) {
        val chunks = msg.data.split(":", ",", ";")
        if (chunks.isEmpty()) {
            log.warning { "Got wrong number of parameters: [$msg]" }
            sendErrorResponse(1)
            return
        }

        val req = chunks[0]
        when (req) {
            "Rcmd" -> {
                val submsg = chunks[1]
                val command = submsg.unhexlify().convertToString()
                val result = REPL.eval(command)
                log.info { "Remote command executed[${result.status}]: $command" }
                if (result.status == 0) {
                    if (result.message != null) {
                        val output = result.message.convertToBytes().hexlify()
                        sendMessageResponse(output)
                    } else {
                        sendOkResponse()
                    }
                } else {
                    sendErrorResponse(result.status)
                }
            }
            // qfThreadInfo
            "fThreadInfo" -> sendEmptyResponse()
            // qC current thread ID
            "C" -> sendEmptyResponse()
            "Symbol" -> sendOkResponse()  // someone also do so ... https://searchcode.com/codesearch/view/72032224/
            "Supported" -> sendMessageResponse("qSupported:swbreak+;hwbreak+;xmlRegisters=${debugger.ident()}")
            "Attached" -> sendMessageResponse("1")
            "Offsets" -> sendMessageResponse("Text=0;Data=0;Bss=0;")
            "TStatus" -> sendEmptyResponse()
            "MustReplyEmpty" -> sendEmptyResponse()
            else -> when(req[0]) {
                'L', 'P' -> sendEmptyResponse()
                else -> {
                    log.warning { "Unknown remote request: $req" }
                    sendErrorResponse(0x01)
                }
            }
        }
    }

    private fun processAllRegistersRead() {
        log.fine { "GDB Request read all registers" }
        val regVals = debugger.registers()
        val response = regVals.joinToString(separator = "") { it.swap32().lhex8 }
        sendMessageResponse(response)
    }

    private fun processRegisterRead(msg: GDBMessage) {
        val reg = msg.data.hexAsInt
        log.fine { "GDB Request register $reg read" }
        val value = debugger.regRead(reg)
        sendMessageResponse(value.swap32().lhex8)
    }

    private fun processHaltReason() {
        log.fine { "GDB Request halt reason" }
        sendInterruptRequest(signalByException.id)
    }

    private fun processExtendedDebugRequest() = sendOkResponse()

    private fun sendMessageResponse(data: String) = send(GDBMessage.message(data))
    private fun sendInterruptRequest(interrupt: Int) = send(GDBMessage.interrupt(interrupt))
    private fun sendErrorResponse(error: Int) = send(GDBMessage.error(error))
    private fun sendAckResponse() = send(GDBMessage.ack)
    private fun sendRejectResponse() = send(GDBMessage.rej)
    private fun sendOkResponse() = send(GDBMessage.ok)
    private fun sendEmptyResponse() = send(GDBMessage.empty)

    override fun close() {
        log.info { "GDB Request close -> stopping target if it was running..." }
        if (isDebuggerInit()) {
            debugger.halt()
        } else {
            log.warning { "Debugger was not initialized, so it will not be halted by gdb server" }
        }
        clientProcessing = false
        super.close()
    }
}
