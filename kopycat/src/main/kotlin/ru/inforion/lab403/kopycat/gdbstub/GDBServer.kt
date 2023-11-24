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
package ru.inforion.lab403.kopycat.gdbstub

import ru.inforion.lab403.common.extensions.ifNotNull
import ru.inforion.lab403.common.extensions.otherwise
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.network.Network
import ru.inforion.lab403.kopycat.gdbstub.parser.Context
import ru.inforion.lab403.kopycat.gdbstub.parser.Packet
import ru.inforion.lab403.kopycat.gdbstub.parser.PacketIterator.Companion.packetSequence
import ru.inforion.lab403.kopycat.interfaces.IDebugger
import java.io.Closeable
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.time.measureTimedValue

@Suppress("UNUSED_PARAMETER")
/**
 * {RU}
 *
 * @param port номер порта для GDB
 * @param binaryProtoEnabled разрешить использование бинарного протокола GDB (команда X)
 * {RU}
 */
class GDBServer constructor(
    port: Int,
    host: String = "0.0.0.0",
    val packetSize: Int = 0x4000,
    val binaryProtoEnabled: Boolean = false
) : Closeable {

    companion object {
        @Transient val log = logger(INFO)
    }

    private val network = Network("GDB_SERVER", port, host, 0x1000, 1).onConnect {
        if (debugger == null)
            log.severe { "GDB Client connected but debugger wasn't initialized!" }

        hasClient = true

        thread {
            debugger ifNotNull {
                val context = Context(this@onConnect, this, this@GDBServer)

                do {
                    val packet = packets.poll(1000, TimeUnit.MILLISECONDS) ?: continue
                    log.finest { "RECV: [$packet]" }
                    context.parseAndProcess(packet)
                } while (hasClient)
            } otherwise {
                log.severe { "Can't process client due to GDB debugger wasn't set!" }
                sendRejectResponse()
            }
        }

        inputStream
            .packetSequence()
            .forEach { packets.put(it) }

        debugger ifNotNull {
            breakpoints.forEach { bptClr(it) }
            breakpoints.clear()
        }

        hasClient = false

        packets.clear()

        // do not process client, all processing in this function
        false
    }

    // required to remove breakpoints because ida don't do it
    internal val breakpoints = mutableListOf<ULong>()

    private var debugger: IDebugger? = null

    fun debuggerModule(newDebugger: IDebugger?) {
        require(!hasClient || debugger == null) {
            "Can't set debugger module=$newDebugger due to GDB client is now processing message, please wait a while"
        }

        log.info { "Set new debugger module $newDebugger for $this" }
        debugger = newDebugger
    }

    var hasClient: Boolean = false
        private set

    private val packets = LinkedBlockingQueue<Packet>()

    override fun close() {
        log.info { "GDB Request close -> stopping target if it was running..." }
        debugger?.halt()
        hasClient = false
        packets.clear()
        network.close()
    }

    override fun toString() = network.toString()
}
