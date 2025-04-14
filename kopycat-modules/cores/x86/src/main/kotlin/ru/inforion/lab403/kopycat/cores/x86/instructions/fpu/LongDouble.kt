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
package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.softfloat.SoftFloat
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger
import java.nio.ByteOrder
import kotlin.math.absoluteValue

class LongDouble internal constructor(val high: UShort, val low: ULong, val sf: SoftFloat) : Comparable<LongDouble> {
    companion object {
        @Transient val log = logger(INFO)

        fun zero(sf: SoftFloat) = LongDouble(0u, 0u, sf)
        fun log2_e(sf: SoftFloat) = LongDouble(0x3fffu, 0xb8aa3b295c17f0bcuL, sf)
        fun log10_2(sf: SoftFloat) = LongDouble(0x3ffdu, 0x9a209a84fbcff799uL, sf)
        fun log2_10(sf: SoftFloat) = LongDouble(0x4000u, 0xd49a784bcd1b8afeuL, sf)
        fun loge_2(sf: SoftFloat) = LongDouble(0x3ffeu, 0xb17217f7d1cf79acuL, sf)
        fun one(sf: SoftFloat) = LongDouble(0x3fffu, 0x8000000000000000uL, sf)
        fun pi(sf: SoftFloat) = LongDouble(0x4000u, 0xc90fdaa22168c235uL, sf)
        // fun nan(sf: SoftFloat) = LongDouble(SoftFloat.DEFAULT_NAN_F80_HIGH, SoftFloat.DEFAULT_NAN_F80_LOW, sf)
        // fun infinity(sf: SoftFloat) = LongDouble(SoftFloat.FLOAT80_INFINITY_HIGH, SoftFloat.FLOAT80_INFINITY_LOW, sf)
    }

    internal val sign by lazy { high ushr 15 != 0u }
    internal val isSignalingNaN by lazy {
        (high and 0x7FFFu).uint_z == 0x7FFFu &&
                (low and 0x4000_0000_0000_0000uL) == 0uL &&
                (low and 0x3FFF_FFFF_FFFF_FFFFuL) != 0uL
    }

    internal val isNaN by lazy {
        ((high and 0x7FFFu).uint_z == 0x7FFFu) &&
                (low and 0x7FFF_FFFF_FFFF_FFFFuL) != 0uL
    }

    internal val isInvalid by lazy {
        low and 0x8000_0000_0000_0000uL == 0uL &&
                (high and 0x7FFFu).uint_z != 0u
    }

    constructor(a: BigInteger, sf: SoftFloat) : this(a[79..64].ushort, a[63..0].ulong, sf)
    private constructor(a: LongDouble, sf: SoftFloat) : this(a.high, a.low, sf)
    constructor(a: Float, sf: SoftFloat) : this(sf.floatToF80(a), sf)
    constructor(a: Double, sf: SoftFloat) : this(sf.doubleToF80(a), sf)
    constructor(a: Short, sf: SoftFloat) : this(a.int_s, sf)
    constructor(a: Int, sf: SoftFloat) : this(
        if (a == 0) {
            zero(sf)
        } else {
            val abs = a.absoluteValue
            val shiftCount = abs.countLeadingZeroBits() + 32
            sf.packF80(a < 0, (0x403E - shiftCount).ushort, (abs.long_z shl shiftCount).ulong)
        },
        sf,
    )
    constructor(a: Long, sf: SoftFloat) : this(
        if (a == 0L) {
            sf.packF80(false, 0u, 0u)
        } else {
            val abs = a.absoluteValue
            val shiftCount = abs.countLeadingZeroBits()
            sf.packF80(a < 0, (0x403E - shiftCount).ushort, (abs shl shiftCount).ulong)
        },
        sf,
    )

    operator fun plus(other: LongDouble) = LongDouble(sf.addF80M(this, other, false), sf)
    operator fun minus(other: LongDouble) = LongDouble(sf.addF80M(this, other, true), sf)
    operator fun times(other: LongDouble) = LongDouble(sf.mulF80M(this, other), sf)
    operator fun div(other: LongDouble) = LongDouble(sf.divF80M(this, other), sf)
    operator fun unaryMinus() = LongDouble(high xor 0x8000u, low, sf)

    override operator fun compareTo(other: LongDouble) = if (sf.eqF80(this, other)) {
        0
    } else if (sf.leF80(this, other)) {
        -1
    } else {
        1
    }

    inline val byteArrayLe get() = ByteArray(10).also {
        it.putUInt64(0, low, ByteOrder.LITTLE_ENDIAN)
        it.putUInt16(8, high.ulong_z, ByteOrder.LITTLE_ENDIAN)
    }

    fun ieee754AsUnsigned() = BigInteger(1, byteArrayLe.reversedArray())
    val isZero by lazy { (high and 0x7fffu).uint_z == 0u && low == 0uL}
    val isInfinity by lazy {
        (high and 0x7fffu) == SoftFloat.FLOAT80_INFINITY_HIGH &&
                low == SoftFloat.FLOAT80_INFINITY_LOW
    }

    val short by lazy { int.short }
    val int by lazy { sf.f80ToInt(this, true) }
    val long by lazy { sf.f80ToLong(this, true) }
    val float by lazy { sf.f80ToFloat(this) }
    val double by lazy { sf.f80ToDouble(this) }

    val roundedToNearestInt by lazy { sf.roundToNearestIntF80(this, true) }
    fun fscale(st1: LongDouble) = sf.fscale(this, st1)
    val abs by lazy { LongDouble(high and 0x7fffu, low, sf) }
    val sqrt by lazy { sf.sqrtF80M(this) }

    override fun toString() = double.toString()
    override fun equals(other: Any?) = other is LongDouble && compareTo(other) == 0
    override fun hashCode() = 31 * high.hashCode() + low.hashCode()
}

// Float to long double. -1.0 -> -1.0
fun Float.longDouble(sf: SoftFloat) = LongDouble(this, sf)
fun Double.longDouble(sf: SoftFloat) = LongDouble(this, sf)

// Signed to long double. -1 -> -1.0
fun Short.longDouble(sf: SoftFloat) = LongDouble(this, sf)
fun Int.longDouble(sf: SoftFloat) = LongDouble(this, sf)
fun Long.longDouble(sf: SoftFloat) = LongDouble(this, sf)

fun BigInteger.longDouble(sf: SoftFloat) = LongDouble(this, sf)

/** Depending on the size of the operand, converts either float or double to long double */
fun AOperand<x86Core>.longDouble(x86: x86Core, sf: SoftFloat) = when (this.dtyp) {
    Datatype.DWORD -> value(x86).uint.ieee754().longDouble(sf)
    Datatype.QWORD -> value(x86).ieee754().longDouble(sf)
    else -> throw RuntimeException("Wrong operand size")
}
