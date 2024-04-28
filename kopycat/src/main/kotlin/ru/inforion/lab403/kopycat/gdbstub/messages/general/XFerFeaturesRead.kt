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
package ru.inforion.lab403.kopycat.gdbstub.messages.general

import ru.inforion.lab403.common.extensions.getResourceText
import ru.inforion.lab403.common.extensions.int
import ru.inforion.lab403.kopycat.gdbstub.parser.Context
import ru.inforion.lab403.kopycat.gdbstub.sendEmptyResponse
import ru.inforion.lab403.kopycat.gdbstub.sendMessageResponse
import ru.inforion.lab403.kopycat.serializer.classname

internal class XFerFeaturesRead(val chunks: List<String>): GeneralRequestMessage() {
    private fun Context.featureRead(name: String, offset: Int) {
        val actual = if (name != "target.xml") name else
            debugger.runCatching { target() }.onFailure {
                log.severe { "target() method not implemented for ${debugger.classname()}" }
            }.getOrNull() ?: return socket.sendEmptyResponse()

        val xml = debugger.runCatching { getResourceText(actual) }.onFailure {
            log.severe { "Can't load requested resource file $actual for register description" }
        }.getOrNull() ?: return socket.sendEmptyResponse()

        val result = xml.drop(offset)

        socket.sendMessageResponse("l${result}")
    }

    override fun Context.process() {
        val type = chunks[1]
        val operation = chunks[2]
        when (type) {
            "features" -> when (operation) {
                "read" -> featureRead(chunks[3], chunks[4].int)
                else -> throw NotImplementedError("Xfer operation=$operation not implemented")
            }
            else -> throw NotImplementedError("Xfer type=$type not implemented")
        }
    }

    override fun toString() = "${super.toString()}($chunks)"
}