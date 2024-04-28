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
package ru.inforion.lab403.kopycat.modules.syscores.sha1

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.interfaces.IResettable
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues
import java.nio.ByteOrder.BIG_ENDIAN
import kotlin.experimental.or

class SHA1Engine : IAutoSerializable, IResettable {
    companion object {
        const val H0 = 0x67452301u
        const val H1 = 0xEFCDAB89u
        const val H2 = 0x98BADCFEu
        const val H3 = 0x10325476u
        const val H4 = 0xC3D2E1F0u

        fun padding(data: ByteArray, expand: Boolean): ByteArray {
            require(data.size <= 55) { "Can't pad array with size longer than 55 bytes (${data.size})" }

            val size = if (expand) 55 else data.size

            return ByteArray(64).apply {
                putArray(0, data)
                putUInt8(size, 0x80u)
                putUInt64(56, size.ulong_z * 8u, BIG_ENDIAN)
            }
        }
    }

    internal var A = 0u
    internal var B = 0u
    internal var C = 0u
    internal var D = 0u
    internal var E = 0u

    private var round = 0

    private val W = UIntArray(80)

    private fun byteArray(a: UInt, b: UInt, c: UInt, d: UInt, e: UInt) = ByteArray(20).apply {
        putUInt32(0, a.ulong_z, BIG_ENDIAN)
        putUInt32(4, b.ulong_z, BIG_ENDIAN)
        putUInt32(8, c.ulong_z, BIG_ENDIAN)
        putUInt32(12, d.ulong_z, BIG_ENDIAN)
        putUInt32(16, e.ulong_z, BIG_ENDIAN)
    }

    fun init(data: ByteArray) {
        update(data)

        A = H0
        B = H1
        C = H2
        D = H3
        E = H4
    }

    fun update(data: ByteArray) {
        round = 0

        W.fill(0u)

        for (i in 0..15)
            W[i] = uint32(data[4 * i + 0].ubyte, data[4 * i + 1].ubyte, data[4 * i + 2].ubyte, data[4 * i + 3].ubyte)

        for (i in 16..79)
            W[i] = (W[i - 3] xor W[i-8] xor W[i-14] xor W[i-16]) rotl32 1

//        W.forEachIndexed { i, w -> println("[$i]$w") }
    }

    private inline fun round(r: Int, f: (UInt, UInt, UInt) -> UInt) {
        val tmp = (A rotl32 5) + f(B, C, D) + E + W[r]
        E = D
        D = C
        C = B rotl32 30
        B = A
        A = tmp
//        println("[i = $r] A=$A, B=$B, C=$C, D=$D, E=$E")
    }

    fun calc(rounds: Int): SHA1Engine {
        var remain = rounds

        while (remain != 0) {
            when (round) {
                in 0..19 -> round(round) { b, c, d -> ((b and c) or ((b xor 0xFFFF_FFFFu) and d)) + 0x5A827999u }
                in 20..39 -> round(round) { b, c, d -> (b xor c xor d) + 0x6ED9EBA1u }
                in 40..59 -> round(round) { b, c, d ->
                    val t1 = b and c
                    val t2 = b and d
                    val t3 = c and d
                    (t1 or t2 or t3) + 0x8F1BBCDCu
                }
                in 60..79 -> round(round) { b, c, d -> (b xor c xor d) + 0xCA62C1D6u }
            }
            round += 1
            remain -= 1
        }

        return this
    }

    fun final() = byteArray(A + H0, B + H1, C + H2, D + H3, E + H4)

    override fun reset() {
        A = 0u
        B = 0u
        C = 0u
        D = 0u
        E = 0u
        W.fill(0u)
        round = 0
    }

//    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
//            "W" to W, "round" to round, "A" to A, "B" to B, "C" to C, "D" to D, "E" to E)
//
//    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
//        super.deserialize(ctxt, snapshot)
//        val tmpW = loadValue<List<UInt>>(snapshot, "W")
//        repeat(tmpW.size) { W[it] = tmpW[it] }
//        round = loadValue(snapshot, "round")
//        A = loadValue(snapshot, "A")
//        B = loadValue(snapshot, "B")
//        C = loadValue(snapshot, "C")
//        D = loadValue(snapshot, "D")
//        E = loadValue(snapshot, "E")
//    }
}
