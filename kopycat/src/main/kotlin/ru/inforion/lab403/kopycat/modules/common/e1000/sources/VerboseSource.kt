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
package ru.inforion.lab403.kopycat.modules.common.e1000.sources

import ru.inforion.lab403.common.extensions.hexlify
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.modules.common.e1000.E1000
import ru.inforion.lab403.kopycat.modules.common.e1000.IPacketSource
import ru.inforion.lab403.kopycat.modules.common.e1000.PacketSourceAlreadyStartedException
import ru.inforion.lab403.kopycat.modules.common.e1000.PacketSourceNotStartedException

/**
 * Packet source that logs the network data.
 * May be started by default
 */
@Suppress("unused")
class VerboseSource(defaultStartValue: Boolean = false) : IPacketSource {
    companion object {
        @Transient
        val log = logger(FINE)
    }

    override var started = defaultStartValue

    override fun start(e1000: E1000) {
        if (started) {
            throw PacketSourceAlreadyStartedException(this)
        }
        started = true
        log.info { "Started VerboseSource" }
    }

    override fun stop(e1000: E1000) {
        if (!started) {
            throw PacketSourceNotStartedException(this)
        }
        started = false
        log.info { "Stopped VerboseSource" }
    }

    override fun send(packet: ArrayList<Byte>) {
        if (!started) {
            throw PacketSourceNotStartedException(this)
        }
        if (packet.isEmpty()) {
            log.info { "Sent empty VerboseSource packet" }
        } else {
            log.info { "Sent VerboseSource packet:\n${packet.toByteArray().hexlify()}" }
        }
    }
}
