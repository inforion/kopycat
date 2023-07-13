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
import ru.inforion.lab403.common.proposal.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.gdbstub.parser.Context
import ru.inforion.lab403.kopycat.gdbstub.parser.Packet
import ru.inforion.lab403.kopycat.gdbstub.messages.AbstractMessage
import ru.inforion.lab403.kopycat.gdbstub.sendOkResponse
import java.math.BigInteger

internal class RegisterWriteMessage(val index: Int, val value: BigInteger) : AbstractMessage() {
    companion object {
        fun parse(packet: Packet): RegisterWriteMessage {
            val params = packet.body.split('=')

            val reg = params[0].intByHex
            val value = params[1].bigintByHex

            return RegisterWriteMessage(reg, value)
        }
    }

    override fun Context.process() {
        val swapped = when (debugger.regSize(index)) {
            XMMWORD -> value.swap128()
            FPU80 -> value.swap80()
            QWORD -> value.swap64()
            DWORD -> value.swap32()
            else -> throw NotImplementedError("Unknown register data type")
        }
        debugger.regWrite(index, swapped)
        socket.sendOkResponse()
    }

    override fun toString() = "${super.toString()}(index=$index, value=0x${value.hex})"
}
