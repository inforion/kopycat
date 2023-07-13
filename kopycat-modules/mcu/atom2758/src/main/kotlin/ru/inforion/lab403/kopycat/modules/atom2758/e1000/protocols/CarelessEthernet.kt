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
package ru.inforion.lab403.kopycat.modules.atom2758.e1000.protocols

import ru.inforion.lab403.common.extensions.byte
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.kopycat.modules.atom2758.e1000.Protocol
import java.lang.RuntimeException
import kotlin.experimental.and

internal open class CarelessEthernet(buffer: List<Byte>) : Protocol() {
    companion object {
        enum class PacketType {
            Unicast,
            Broadcast,
            Multicast,
        }

        fun dissect(buffer: List<Byte>) = runCatching {
            CarelessEthernet(buffer)
        }.getOrNull()
    }

    override fun headerSize() = 14
    override fun fullSize(): Int = throw RuntimeException("fullSize() requires next layer; use Ethernet instead")

    protected val dest = buffer.subList(0, 6).toByteArray()
    protected val src = buffer.subList(6, 12).toByteArray()
    protected val proto = buffer.subList(12, 14).be().uint
    protected val packetBuffer by lazy { buffer.subList(14, buffer.size) }

    val packetType = if (dest.all { it == 0xff.byte }) {
        PacketType.Broadcast
    } else if (dest[0] and 0x01 > 0) {
        PacketType.Multicast
    } else {
        PacketType.Unicast
    }
}
