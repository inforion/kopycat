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
import ru.inforion.lab403.kopycat.modules.atom2758.e1000.Protocol

internal abstract class IP : Protocol() {
    companion object {
        const val PROTO_TCP = 0x06u
        const val PROTO_UDP = 0x11u

        /*
        enum class RSSType {
            None,
            IPv4,
            IPv4Tcp,
            IPv6,
            IPv6Ex,
            IPv6Tcp,
        }
        */
    }

    abstract val proto: UInt
    abstract val fragment: Boolean
    abstract val nextLayer: Protocol?
    abstract val l4Checksum: UShort

    protected fun checksumAdd(buf: Iterator<Byte>, size: Int): UInt {
        var sum1 = 0u
        var sum2 = 0u

        var ctr = 0
        while (ctr < size - 1) {
            sum1 += buf.next().ubyte
            sum2 += buf.next().ubyte
            ctr += 2
        }

        if (ctr < size) {
            sum1 += buf.next().ubyte
        }

        return sum2 + (sum1 shl 8)
    }

    protected fun checksumFinish(sum: UInt): UShort {
        var result = sum

        while ((result ushr 16).truth) {
            result = (result and 0xFFFFu) + (result ushr 16)
        }

        return result.inv().ushort
    }

    protected fun protoToString() = when (proto) {
        PROTO_TCP -> "TCP"
        PROTO_UDP -> "UDP"
        else -> proto.hex2
    }

    /*
    abstract fun calcHash(type: RSSType, key: List<Byte>): UInt
    protected fun toeplitz(input: UByteArray, keyBuf: List<Byte>): UInt {
        var key = keyBuf.subList(0, 4).be().uint
        var ret = 0u
        for (i in input.indices) {
            for (j in 0 until 8) {
                if (input[i][7 - j].truth) {
                    ret = ret xor key
                }
                key = key shl 1
                if (i + 4 < 40 && keyBuf[i + 4][7 - j].truth) {
                    key = key or 1u
                }
            }
        }

        return ret
    }
    */
}
