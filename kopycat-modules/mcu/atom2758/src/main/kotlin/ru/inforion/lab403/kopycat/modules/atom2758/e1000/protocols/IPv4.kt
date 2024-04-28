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

internal class IPv4(val buffer: List<Byte>) : IP() {
    companion object {
        private val MORE_FRAGMENTS_FLAG = 1u.ubyte
    }

    override fun headerSize() = headerLength shl 2
    override fun fullSize() = totalLength

    // DWORD 1
    private val headerLength = buffer[0][3..0]
    private val version = buffer[0][7..4].ubyte
    // val tos = buffer[1].ubyte
    private val totalLength = buffer.subList(2, 4).be().int

    // DWORD 2
    val id = buffer.subList(4, 6).be().uint
    private val flags = buffer[6][7..5].ubyte
    private val fragmentOfft = (buffer.subList(6, 8).be() mask 12..0).uint

    // DWORD 3
    // val ttl = buffer[8].ubyte
    override val proto = buffer[9].uint_z
    // val checksum = buffer.subList(10, 12).be().uint

    // DWORD 4
    private val src = buffer.subList(12, 16).map { it.ubyte }.toUByteArray()

    // DWORD 5
    private val dst = buffer.subList(16, 20).map { it.ubyte }.toUByteArray()

    override val fragment: Boolean
        get() = fragmentOfft.truth || (flags and MORE_FRAGMENTS_FLAG).truth

    override val nextLayer = when (proto) {
        PROTO_TCP -> Tcp(buffer.subList(headerSize(), buffer.size))
        PROTO_UDP -> Udp(buffer.subList(headerSize(), buffer.size))
        else -> null
    }

    val checksum = checksumFinish(checksumAdd(buffer.iterator(), headerSize()))

    override val l4Checksum = makeL4Checksum(buffer.asSequence())

    fun makeL4Checksum(bufferSeq: Sequence<Byte>): UShort {
        val nextLayerSize = if (nextLayer is Udp) {
            nextLayer.length.ushort
        } else {
            (totalLength - headerSize()).ushort
        }

        val sum = checksumAdd(src.asSequence().map { it.byte }.iterator(), 4) +
                checksumAdd(dst.asSequence().map { it.byte }.iterator(), 4) +
                checksumAdd(sequenceOf(0, proto.byte).iterator(), 2) +
                checksumAdd(
                    sequenceOf(
                        ((nextLayerSize and 0xFF00u) ushr 8).byte,
                        (nextLayerSize and 0xFFu).byte,
                    ).iterator(),
                    2,
                ) +
                checksumAdd(bufferSeq.drop(headerSize()).iterator(), totalLength - headerSize())

        return checksumFinish(sum)
    }

    /*
    override fun calcHash(type: IP.Companion.RSSType, key: List<Byte>) = when (type) {
        IP.Companion.RSSType.IPv4 -> toeplitz(
            ubyteArrayOf(
                *src,
                *dst,
            ),
            key,
        )
        IP.Companion.RSSType.IPv4Tcp -> toeplitz(
            ubyteArrayOf(
                *src,
                *dst,
                *(nextLayer!! as Tcp).sport,
                *(nextLayer as Tcp).dport,
            ),
            key,
        )
        else -> TODO("IPv4: unknown RSS type")
    }
    */

    init {
        if (version != 4u.ubyte) {
            throw AssertionError("IPv4 packet version field != 4")
        }
    }

    private fun UByteArray.ip() = joinToString(separator = ".") { it.toString() }

    override fun toString() = "IPv4: ${src.ip()} -> ${dst.ip()}, protocol: ${protoToString()}, L4 checksum: " +
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
