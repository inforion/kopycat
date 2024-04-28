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
import ru.inforion.lab403.kopycat.cores.base.enums.BreakpointType
import ru.inforion.lab403.kopycat.gdbstub.parser.Context
import ru.inforion.lab403.kopycat.gdbstub.parser.Packet
import ru.inforion.lab403.kopycat.gdbstub.sendOkResponse

internal class SetBreakpointMessage(val address: ULong, val count: Int, val type: BreakpointType) : BreakpointMessage() {
    companion object {
        // set bp: z type,address,count
        fun parse(packet: Packet): SetBreakpointMessage {
            val params = packet.body.split(',')

            require(params.size == 3) { "Got wrong number of parameters while setting BP: [$packet]" }

            val typeValue = params[0].intByHex
            val address = params[1].ulongByHex
            val count = params[2].intByHex
            val type = convert<BreakpointType>(typeValue)

            return SetBreakpointMessage(address, count, type)
        }
    }

    override fun Context.process() {
        server.breakpoints.add(address)
        debugger.bptSet(type, address)
        socket.sendOkResponse()
    }

    override fun toString() = "${super.toString()}(address=0x${address.hex} count=$count type=$type)"
}