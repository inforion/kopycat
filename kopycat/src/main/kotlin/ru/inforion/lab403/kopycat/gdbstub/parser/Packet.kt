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
package ru.inforion.lab403.kopycat.gdbstub.parser

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.gdbstub.checksum


internal class Packet private constructor(
    val data: String,
    val checksum: Int = -1,
    val isService: Boolean = false
) {
    companion object {
        @Transient val log = logger()

        fun message(data: String, checksum: Int = -1): Packet {
            val value = if (checksum == -1) data.bytes.checksum() else checksum
            return Packet(data, value)
        }

        fun interrupt(interrupt: Int) = message("T${interrupt.hex2}")
        fun error(error: Int) = message("E${error.hex2}")
        fun service(cmd: Char) = Packet("$cmd", isService = true)

        val ack = service('+')
        val rej = service('-')
        val ok = message("OK")
        val empty = message("")
    }

    val isEmpty get() = data.isBlank() && !isService

    val isChecksumValid get() = data.bytes.checksum() == checksum

    val cmd get() = data[0]
    val body get() = data.substring(1)

    fun build(noBinary: Boolean = false): String {
        val prefix = if (!isService) "$" else ""
        val postfix = if (!isService) "#${checksum.hex2}" else ""
        val payload = if (noBinary && data.startsWith('X')) "${data.split(':', limit = 2)[0]}..." else data
        return "$prefix$payload$postfix"
    }

    override fun toString(): String = build(true)
}