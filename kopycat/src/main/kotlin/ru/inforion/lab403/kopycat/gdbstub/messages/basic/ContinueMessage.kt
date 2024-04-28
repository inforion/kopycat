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

import ru.inforion.lab403.common.extensions.intByHex
import ru.inforion.lab403.common.logging.logStackTrace
import ru.inforion.lab403.kopycat.gdbstub.parser.Context
import ru.inforion.lab403.kopycat.gdbstub.parser.Packet
import ru.inforion.lab403.kopycat.gdbstub.messages.AbstractMessage
import ru.inforion.lab403.kopycat.gdbstub.sendInterruptRequest
import kotlin.concurrent.thread

internal class ContinueMessage(val signal: Int) : AbstractMessage() {
    companion object {
        fun parse(packet: Packet) = ContinueMessage(packet.body.intByHex)

        fun parse() = ContinueMessage(-1)
    }

    override fun Context.process() {
        if (!debugger.isRunning) {
            thread {
                debugger.runCatching { cont() }.onFailure { it.logStackTrace(log) }
                socket.sendInterruptRequest(debugger.exception())
            }
        } else {
            log.warning { "GDB: Target already running...halt the target!" }
            debugger.halt()
        }
    }

    override fun toString() = "${super.toString()}(signal=$signal)"
}