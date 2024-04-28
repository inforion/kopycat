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
package ru.inforion.lab403.kopycat.gdbstub.messages.basic

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.hexlify
import ru.inforion.lab403.common.extensions.intByHex
import ru.inforion.lab403.common.extensions.ulongByHex
import ru.inforion.lab403.kopycat.gdbstub.parser.Context
import ru.inforion.lab403.kopycat.gdbstub.parser.Packet
import ru.inforion.lab403.kopycat.gdbstub.messages.AbstractMessage
import ru.inforion.lab403.kopycat.gdbstub.sendErrorResponse
import ru.inforion.lab403.kopycat.gdbstub.sendMessageResponse

internal class ReadMemTextMessage(val address: ULong, val size: Int) : AbstractMessage() {
    companion object {
        fun parse(packet: Packet): ReadMemTextMessage {
            val params = packet.body.split(',')

            require(params.size == 2) { "Got wrong number of parameters while reading mem: [$packet]" }

            val address = params[0].ulongByHex
            val length = params[1].intByHex

            return ReadMemTextMessage(address, length)
        }
    }

    override fun Context.process() {
        debugger
            .runCatching { dbgLoad(address, size) }
            .onSuccess {
                val response = it.hexlify(false)
                socket.sendMessageResponse(response)
            }.onFailure {
                socket.sendErrorResponse(2)
            }
    }

    override fun toString() = "${super.toString()}(address=0x${address.hex} size=$size)"
}