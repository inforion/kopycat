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
package ru.inforion.lab403.kopycat.gdbstub.messages.basic

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.gdbstub.*
import ru.inforion.lab403.kopycat.gdbstub.parser.Packet
import ru.inforion.lab403.kopycat.gdbstub.messages.AbstractMessage
import ru.inforion.lab403.kopycat.gdbstub.parser.Context

internal class RegisterReadMessage(val index: Int) : AbstractMessage() {
    companion object {
        fun parse(packet: Packet) = RegisterReadMessage(packet.body.intByHex)
    }

    override fun Context.process() {
        val value = debugger.regRead(index)
        val message = when (debugger.regSize(index)) {
            XMMWORD -> value.swap128().lhex32
            FPU80 -> value.swap80().lhex20
            QWORD -> value.ulong.swap64().long.lhex16
            DWORD -> value.ulong.swap32().long.lhex8
            else -> throw NotImplementedError("Unknown register data type")
        }
        socket.sendMessageResponse(message)
    }

    override fun toString() = "${super.toString()}(index=$index)"
}
