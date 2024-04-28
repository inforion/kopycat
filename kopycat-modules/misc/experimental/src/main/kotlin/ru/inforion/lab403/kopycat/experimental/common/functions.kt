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
package ru.inforion.lab403.kopycat.experimental.common

import ru.inforion.lab403.common.logging.logger.Logger
import java.net.URI

class PacketSourceData(
    val host: String,
    val port: Int
) {
    override fun toString(): String {
        return "${host}:${port}"
    }

//    fun a(logger: Logger, ) {
//        Module.log.info { "Using EthernetOverTcpSource with host='${this}' as atom2758.e1000.packetSource" }
//
//        val source = EthernetOverTcpSource(host, port)
//        Module.log.info { "Stopping existing atom2758.e1000.packetSource" }
//        atom2758.e1000.packetSource.stop(atom2758.e1000)
//
//        source.start(atom2758.e1000)
//        atom2758.e1000.packetSource = source
//        Module.log.info { "Changed atom2758.e1000.packetSource successfully" }
//    }
}

// https://stackoverflow.com/a/2347356
fun parsePacketSourceData(packetSource: String?, logger: Logger): PacketSourceData? = packetSource
    ?.let { host -> URI("my://${host}") }
    ?.let { uri ->
        if (uri.host == null) {
            logger.warning { "Unable to parse packetSource='${packetSource}' host" }
            return@let null
        }
        if (uri.port < 0) {
            logger.warning { "Unable to parse packetSource='${packetSource}' port" }
            return@let null
        }
        PacketSourceData(uri.host, uri.port)
    } ?: run {
    logger.info { "No packetSource provided. The E1000 will use the default packetSource" }
    null
}