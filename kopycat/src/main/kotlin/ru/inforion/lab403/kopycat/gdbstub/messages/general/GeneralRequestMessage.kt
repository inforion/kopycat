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
import ru.inforion.lab403.kopycat.gdbstub.parser.Packet
import ru.inforion.lab403.kopycat.gdbstub.messages.AbstractMessage

internal abstract class GeneralRequestMessage : AbstractMessage() {
    companion object {
        fun parse(packet: Packet): GeneralRequestMessage {
            val chunks = packet.body.split(":", ",", ";")

            require(chunks.isNotEmpty()) { "Got wrong number of parameters: [$packet]" }

            return when (val req = chunks[0]) {
                "Rcmd" -> {
                    val submsg = chunks[1]
                    val command = submsg.unhexlify().string
                    RemoteCommandMessage(command)
                }
                // qfThreadInfo
                "fThreadInfo" -> ThreadInfoMessage
                // qC current thread ID
                "C" -> CurrentThreadIdMessage
                // someone also do so ... https://searchcode.com/codesearch/view/72032224/
                "Symbol" -> SymbolMessage
                "Supported" -> SupportedMessage(chunks)
                "Attached" -> AttachedMessage
                "Offsets" -> OffsetsMessage
                "TStatus" -> TStatusMessage
                "MustReplyEmpty" -> MustReplyEmptyMessage
                "Xfer" -> XFerFeaturesRead(chunks)
                else -> when (req[0]) {
                    'L', 'P' -> OtherRequestMessage
                    else -> error("Unknown remote request: $req")
                }
            }
        }
    }
}