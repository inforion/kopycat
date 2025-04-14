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
package ru.inforion.lab403.kopycat.modules.common.e1000

import ru.inforion.lab403.kopycat.modules.common.e1000.protocols.Ethernet
import ru.inforion.lab403.kopycat.modules.common.e1000.protocols.IP
import ru.inforion.lab403.kopycat.modules.common.e1000.protocols.IPv4
import ru.inforion.lab403.kopycat.modules.common.e1000.protocols.IPv6
import ru.inforion.lab403.kopycat.modules.common.e1000.protocols.Tcp
import ru.inforion.lab403.kopycat.modules.common.e1000.protocols.Udp

internal class Dissection private constructor(eth: Ethernet) {
    companion object {
        fun dissect(buffer: List<Byte>) = Ethernet.dissect(buffer)?.let {
            Dissection(it)
        }
    }

    val l2 = eth
    private val l3 = l2.nextLayer
    private val l4 = if (l3 is IP) l3.nextLayer else null

    val ip: IP? = if (l3 is IP) l3 else null
    val ip4: IPv4? = if (ip is IPv4) ip else null
    val ip6: IPv6? = if (ip is IPv6) ip else null

    val tcp: Tcp? = if (l4 is Tcp) l4 else null
    val udp: Udp? = if (l4 is Udp) l4 else null

    val fullSize = l2.fullSize()
    private val l3offset = l2.headerSize()
    val l4offset = l3offset + l3.headerSize()
    val l5offset = if (l4 != null) l4offset + l4.headerSize() else null

    override fun toString() = "Dissection(\n" +
            "  l2: $l2\n" +
            "  l3: $l3\n" +
            "  l4: $l4\n" +
            "  ip: ${ip != null}, ip4: ${ip4 != null}, ip6: ${ip6 != null}, tcp: ${tcp != null}, udp: ${udp != null}\n" +
            "  size: $fullSize, l3 offset: $l3offset, l4 offset: $l4offset, l5 offset: $l5offset\n" +
            ")"
}
