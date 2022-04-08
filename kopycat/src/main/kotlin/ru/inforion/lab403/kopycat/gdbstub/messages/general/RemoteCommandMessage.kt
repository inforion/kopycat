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
package ru.inforion.lab403.kopycat.gdbstub.messages.general

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.gdbstub.*
import ru.inforion.lab403.kopycat.gdbstub.parser.Context
import ru.inforion.lab403.kopycat.gdbstub.sendOkResponse
import ru.inforion.lab403.kopycat.interactive.REPL

internal class RemoteCommandMessage(val command: String) : GeneralRequestMessage() {
    override fun Context.process() {
        val result = REPL.eval(command)
        log.info { "Remote command executed[${result.status}]: $command" }
        if (result.status == 0) {
            if (result.message != null) {
                val output = result.message.bytes.hexlify()
                socket.sendMessageResponse(output)
            } else {
                socket.sendOkResponse()
            }
        } else {
            socket.sendErrorResponse(result.status)
        }
    }

    override fun toString() = "${super.toString()}(command=$command)"
}