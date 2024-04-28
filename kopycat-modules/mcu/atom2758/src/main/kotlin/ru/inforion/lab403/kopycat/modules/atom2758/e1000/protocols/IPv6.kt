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
package ru.inforion.lab403.kopycat.modules.atom2758.e1000.protocols

import ru.inforion.lab403.common.extensions.*

internal class IPv6(buffer: List<Byte>) : IP() {
    companion object {
        private const val HOP_BY_HOP = 0x00u
        private const val ROUTING = 0x2Bu
        private const val FRAGMENT = 0x2Cu
        private const val AUTHENTICATION = 0x33u
        private const val DESTINATION = 0x3Cu
        private const val MOBILITY = 0x87u
    }

    override fun headerSize() = l4offset
    override fun fullSize() = if (payloadLen == 0) {
        TODO("IPv6 jumbo payload is not implemented")
    } else {
        40 + payloadLen
    }

    private val version = buffer[0][7..4].ubyte
    private val payloadLen = buffer.subList(4, 6).be().int
    private val nextHeader = buffer[6].uint_z
    // val ttl = buffer[7].ubyte
    private val src = buffer.subList(8, 24).map { it.ubyte }.toUByteArray()
    private val dst = buffer.subList(24, 40).map { it.ubyte }.toUByteArray()

    private fun isIPv6ExtensionHeader(next: UInt) = when (next) {
        HOP_BY_HOP, ROUTING, FRAGMENT, AUTHENTICATION, DESTINATION, MOBILITY -> true
        else -> false
    }

    /*
    var rssExDstValid = false
        private set

    var rssExSrcValid = false
        private set

    private var rssExSrc = UByteArray(0)
    private var rssExDst = UByteArray(0)
    */

    override var fragment = false
        private set

    override val proto: UInt
    private var l4offset: Int

    init {
        if (version != 6u.ubyte) {
            throw AssertionError("IPv6 packet version field != 6")
        }

        if (isIPv6ExtensionHeader(nextHeader)) {
            var offset = 40
            var currentHeaderType = nextHeader

            do {
                val len = buffer[offset + 1].int_z

                when (currentHeaderType) {
                    /*
                    ROUTING -> {
                        if (len == 2) {
                            val rtype = buffer[offset + 2].int_z
                            val segleft = buffer[offset + 3].int_z
                            if (rtype == 2 && segleft == 1) {
                                rssExDstValid = true
                                rssExDst = buffer.subList(offset + 8, offset + 24).map { it.ubyte }.toUByteArray()
                            }
                        }
                    }
                    DESTINATION -> {
                        var left = (len + 1) * 8 - 2
                        var optOfft = offset + 2
                        while (left > 2) {
                            val optType = buffer[optOfft].uint_z

                            val optLen = if (optType == 0u) 1 else buffer[optOfft + 1].int_z + 2
                            if (optLen > left) {
                                break
                            }

                            if (optType == 0xC9u) {
                                rssExSrcValid = true
                                rssExSrc = buffer.subList(optOfft + 2, optOfft + 18).map { it.ubyte }.toUByteArray()
                            }

                            optOfft += optLen
                            left -= optLen
                        }
                    }
                    */
                    FRAGMENT -> fragment = true
                }

                currentHeaderType = buffer[offset].uint_z
                offset += (len + 1) * 8
            } while (isIPv6ExtensionHeader(currentHeaderType))

            proto = currentHeaderType
            l4offset = offset
        } else {
            proto = nextHeader
            l4offset = 40
        }
    }

    override val nextLayer = when (proto) {
        PROTO_TCP -> Tcp(buffer.subList(headerSize(), buffer.size))
        PROTO_UDP -> Udp(buffer.subList(headerSize(), buffer.size))
        else -> null
    }

    override val l4Checksum = makeL4Checksum(buffer.asSequence())

    private fun makeL4Checksum(bufferSeq: Sequence<Byte>): UShort {
        val nextLayerSize = if (nextLayer is Udp) {
            nextLayer.length.uint
        } else {
            (fullSize() - l4offset).uint
        }

        val sum = checksumAdd(src.asSequence().map { it.byte }.iterator(), 16) +
                checksumAdd(dst.asSequence().map { it.byte }.iterator(), 16) +
                checksumAdd(
                    sequenceOf(
                        ((nextLayerSize and 0xFF000000u) ushr 24).byte,
                        ((nextLayerSize and 0xFF0000u) ushr 16).byte,
                        ((nextLayerSize and 0xFF00u) ushr 8).byte,
                        (nextLayerSize and 0xFFu).byte,
                    ).iterator(),
                    4,
                ) +
                checksumAdd(sequenceOf(0, proto.byte).iterator(), 2) +
                checksumAdd(bufferSeq.drop(l4offset).iterator(), fullSize() - l4offset)

        return checksumFinish(sum)
    }

    /*
    override fun calcHash(type: IP.Companion.RSSType, key: List<Byte>) = when (type) {
        IP.Companion.RSSType.IPv6 -> toeplitz(
            ubyteArrayOf(
                *src,
                *dst,
            ),
            key,
        )
        IP.Companion.RSSType.IPv6Ex -> toeplitz(
            ubyteArrayOf(
                *if (rssExSrcValid) rssExSrc else src,
                *if (rssExDstValid) rssExDst else dst,
            ),
            key,
        )
        IP.Companion.RSSType.IPv6Tcp -> toeplitz(
            ubyteArrayOf(
                *if (rssExSrcValid) rssExSrc else src,
                *if (rssExDstValid) rssExDst else dst,
                *(nextLayer!! as Tcp).sport,
                *(nextLayer as Tcp).dport,
            ),
            key,
        )
        else -> TODO("IPv6: unknown RSS type")
    }
    */

    private fun UByteArray.ip() = asSequence().chunked(2)
        .joinToString(separator = ":") {
            it.joinToString(separator = "") { it2 ->
                it2.hex2
            }
        }

    override fun toString() = "IPv6: ${src.ip()} -> ${dst.ip()}, protocol: ${protoToString()}, L4 checksum: " +
            if (proto == PROTO_TCP || proto == PROTO_UDP) {
                if (l4Checksum == 0u.ushort || l4Checksum == 0xFFFFu.ushort) {
                    "valid"
                } else {
                    "invalid (${l4Checksum.hex4})"
                }
            } else {
                "N/A"
            }
}
