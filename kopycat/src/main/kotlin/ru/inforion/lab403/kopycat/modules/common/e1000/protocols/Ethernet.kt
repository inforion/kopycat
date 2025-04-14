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
package ru.inforion.lab403.kopycat.modules.common.e1000.protocols

import ru.inforion.lab403.common.extensions.*

internal class Ethernet private constructor(buffer: List<Byte>) : CarelessEthernet(buffer) {
    companion object {
        private const val PROTO_IP = 0x0800u
        private const val PROTO_IPV6 = 0x86ddu
        private const val PROTO_ARP = 0x0806u
        private const val PROTO_VLAN = 0x8100u
        private const val PROTO_DVLAN = 0x88a8u

        /**
         * Разбирает ethernet фрейм
         * @return [Ethernet] или `null`, если разобрать фрейм не удалось
         * @throws NotImplementedError если не реализован разбор вложенного пакета
         * @throws AssertionError если поле не соответствует требуемому значению
         */
        fun dissect(buffer: List<Byte>) = try {
            val ethernet = Ethernet(buffer)

            if (ethernet.fullSize() > buffer.size) {
                null
            } else {
                ethernet
            }
        } catch (e: Throwable) {
            when (e) {
                is IndexOutOfBoundsException, is NoSuchElementException -> null
                else -> throw e
            }
        }
    }

    override fun headerSize() = 14
    override fun fullSize() = 14 + nextLayer.fullSize()

    val nextLayer = when (proto) {
        PROTO_IP -> IPv4(packetBuffer)
        PROTO_IPV6 -> IPv6(packetBuffer)
        PROTO_ARP -> ARP()
        PROTO_VLAN, PROTO_DVLAN -> TODO("VLANs are not supported")
        else -> TODO("Unsupported packet type: ${proto.hex4}")
    }

    private fun protoToString() = when (proto) {
        PROTO_IP -> "IPv4"
        PROTO_IPV6 -> "IPv6"
        PROTO_ARP -> "ARP"
        PROTO_VLAN -> "VLAN"
        PROTO_DVLAN -> "DVLAN"
        else -> proto.hex4
    }

    override fun toString() =
        "Ethernet: ${src.hexlify(separator = ':')} -> ${dest.hexlify(separator = ':')}, protocol: ${protoToString()}"
}
