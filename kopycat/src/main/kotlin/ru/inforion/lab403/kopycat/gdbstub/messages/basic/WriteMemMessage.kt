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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.gdbstub.parser.Context
import ru.inforion.lab403.kopycat.gdbstub.parser.Packet
import ru.inforion.lab403.kopycat.gdbstub.messages.AbstractMessage
import ru.inforion.lab403.kopycat.gdbstub.sendErrorResponse
import ru.inforion.lab403.kopycat.gdbstub.sendOkResponse

internal class WriteMemMessage(val address: ULong, val size: Int, val data: ByteArray) : AbstractMessage() {
    companion object {
        // write mem: X1ed7c7,2:aa --> addr,size:binary
        fun parseBinary(packet: Packet, context: Context): WriteMemMessage {
            val params = packet.body.split(',', ':', limit = 3)

            require(params.size == 3) { "Got wrong number of parameters while writing mem: [$packet]" }

            val address = params[0].ulongByHex
            val size = params[1].intByHex
            val data = params[2].bytes

            // starting sequence of X command support (GDB client should send empty memory write request
            // if response is empty then server doesn't support binary exchange
            require(size != 0 || context.server.binaryProtoEnabled) {
                "GDB Client try to intercourse using binary protocol but " +
                        "due to SystemWorkbench/arm-none-eabi-gdb bug it was disabled by default " +
                        "to enable it please start Kopycat with option --gdb-bin-proto"
            }

            require(size == data.size) { "Requested size $size not equals data size ${data.size}: [$packet]" }

            return WriteMemMessage(address, size, data)
        }

        // write mem: M1ed7c7,1:aa --> addr,size:text
        fun parseText(packet: Packet): WriteMemMessage {
            val params = packet.body.split(',', ':', limit = 3)
            require(params.size == 3) { "Got wrong number of parameters while writing mem: [$packet]" }

            val address = params[0].ulongByHex
            val size = params[1].intByHex
            val data = params[2].unhexlify()

            require(size == data.size) { "Got wrong size and data size for writing mem: [$packet]" }

            return WriteMemMessage(address, size, data)
        }
    }

    override fun Context.process() {
        debugger
            .runCatching { dbgStore(address, data) }
            .onSuccess {
                socket.sendOkResponse()
            }.onFailure {
                socket.sendErrorResponse(2)
            }
    }

    override fun toString() = "${super.toString()}(address=0x${address.hex} size=$size data=${data.hexlify()})"
}