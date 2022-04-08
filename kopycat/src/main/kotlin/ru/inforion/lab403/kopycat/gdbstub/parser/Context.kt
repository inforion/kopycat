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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.gdbstub.parser

import ru.inforion.lab403.common.extensions.bytes
import ru.inforion.lab403.common.extensions.char
import ru.inforion.lab403.common.logging.logStackTrace
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.gdbstub.*
import ru.inforion.lab403.kopycat.gdbstub.messages.AbstractMessage
import ru.inforion.lab403.kopycat.gdbstub.sendRejectResponse
import ru.inforion.lab403.kopycat.interfaces.IDebugger
import java.net.Socket
import kotlin.time.measureTimedValue

internal class Context constructor(
    val socket: Socket,
    val debugger: IDebugger,
    val server: GDBServer
) {
    companion object {
        @Transient val log = logger()
    }

    inline fun parseAndProcess(packet: Packet) {
        when {
            packet.isService -> when (packet.cmd) {
                acknowledgeChar.char -> log.finest { "Received acknowledge message +" }
                rejectChar.char -> log.warning { "Client rejected last message!" }
                interruptChar.char -> {
                    log.info { "GDB Request target halt!" }
                    debugger.halt()
                }
            }

            packet.isEmpty -> socket.sendRejectResponse()

            else -> {
                // ATTENTION: Message must be processed and after then sync signal sent to IDA!!!
                // Otherwise, the worst performance would be reached!!!
                // Continue message executed in other thread

                if (packet.isChecksumValid) {
                    socket.sendAckResponse()

                    AbstractMessage
                        .runCatching {
                            measureTimedValue { parse(packet, this@Context) }
                        }.onFailure { error ->
                            error.logStackTrace(log, "Parse packet [$packet] failed for [$socket]")
                            socket.sendErrorResponse(1)
                        }.onSuccess { (message, time) ->
                            log.finer { "Packet [$packet] parsed within $time" }

                            message
                                .runCatching {
                                    log.finest { this }
                                    // message processing
                                    measureTimedValue { process() }
                                }.onFailure { error ->
                                    error.logStackTrace(log, "Processing message [$packet] failed for [$socket]")
                                    socket.sendErrorResponse(1)
                                }.onSuccess { (_, time) ->
                                    log.finer { "Packet [$packet] processed within $time" }
                                }
                        }
                } else {
                    log.warning { "Packet [$packet] checksum incorrect: ${packet.data.bytes.checksum()} != ${packet.checksum}" }
                    socket.sendRejectResponse()
                }
            }
        }
    }
}