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

package ru.inforion.lab403.kopycat.gdbstub.messages

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.gdbstub.parser.Context
import ru.inforion.lab403.kopycat.gdbstub.parser.Packet
import ru.inforion.lab403.kopycat.gdbstub.messages.basic.*
import ru.inforion.lab403.kopycat.gdbstub.messages.basic.ContinueMessage
import ru.inforion.lab403.kopycat.gdbstub.messages.basic.DetachMessage
import ru.inforion.lab403.kopycat.gdbstub.messages.basic.ExtendedDebugRequestMessage
import ru.inforion.lab403.kopycat.gdbstub.messages.basic.ReadMemTextMessage
import ru.inforion.lab403.kopycat.gdbstub.messages.basic.RegisterWriteMessage
import ru.inforion.lab403.kopycat.gdbstub.messages.general.GeneralRequestMessage
import ru.inforion.lab403.kopycat.gdbstub.messages.thread.SetThreadInfoMessage

internal abstract class AbstractMessage {
    companion object {
        @Transient val log = logger()

        inline fun parse(packet: Packet, context: Context): AbstractMessage = when (packet.cmd) {
            '!' -> ExtendedDebugRequestMessage
            '?' -> HaltReasonMessage
            'q' -> GeneralRequestMessage.parse(packet)
            'v' -> ExtendedRequestMessage
            'p' -> RegisterReadMessage.parse(packet)
            'P' -> RegisterWriteMessage.parse(packet)
            'g' -> AllRegistersReadMessage
            'c' -> ContinueMessage.parse()
            'C' -> ContinueMessage.parse(packet)
            's' -> StepMessage
            'H' -> SetThreadInfoMessage.parse(packet)
            'm' -> ReadMemTextMessage.parse(packet)
            'M' -> WriteMemMessage.parseText(packet)
            'X' -> WriteMemMessage.parseBinary(packet, context)
            'k' -> KillRequestMessage
            'z' -> ClearBreakpointMessage.parse(packet)
            'Z' -> SetBreakpointMessage.parse(packet)
            'B' -> BreakpointMessage.parse(packet)
            'D' -> DetachMessage

            else -> error("Unknown packet received: [$packet]")
        }
    }

    abstract fun Context.process()

    override fun toString() = "${this::class.simpleName}"
}