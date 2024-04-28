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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.gdbstub

import ru.inforion.lab403.common.extensions.bytes
import ru.inforion.lab403.common.extensions.int_z
import ru.inforion.lab403.common.extensions.sumOf
import ru.inforion.lab403.common.network.send
import ru.inforion.lab403.kopycat.cores.base.exceptions.BreakpointException
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.gdbstub.enums.Signal
import ru.inforion.lab403.kopycat.gdbstub.parser.Packet
import java.net.Socket

inline fun ByteArray.checksum() = sumOf { it }.int_z

internal val GeneralException?.signal get(): Signal = when (this) {
    null -> Signal.SIGTRAP   // when nothing happen should be BREAKPOINT for IDA
    is BreakpointException -> Signal.SIGTRAP
    is MemoryAccessError -> Signal.SIGSEGV
    else -> Signal.SIGSYS
}

internal inline fun Socket.sendGdbPacket(packet: Packet) {
    val data = packet.build()
    GDBServer.log.finest { "SEND: [$data]" }
    send(data.bytes)
}

internal inline fun Socket.sendMessageResponse(data: String) = sendGdbPacket(Packet.message(data))

internal inline fun Socket.sendInterruptRequest(error: GeneralException?) {
    val signal = error.signal
    val message = Packet.interrupt(signal.id)
    sendGdbPacket(message)
}

internal inline fun Socket.sendErrorResponse(error: Int) = sendGdbPacket(Packet.error(error))
internal inline fun Socket.sendAckResponse() = sendGdbPacket(Packet.ack)
internal inline fun Socket.sendRejectResponse() = sendGdbPacket(Packet.rej)
internal inline fun Socket.sendOkResponse() = sendGdbPacket(Packet.ok)
internal inline fun Socket.sendEmptyResponse() = sendGdbPacket(Packet.empty)