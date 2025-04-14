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
package ru.inforion.lab403.kopycat.cores.x86.instructions

import org.junit.jupiter.api.Test
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.math.round
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.FWRBank
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.LongDouble
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.longDouble
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.softfloat.Flag
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.softfloat.RoundingMode
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.softfloat.SoftFloat
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.softfloat.SoftFloat.Companion.sign
import kotlin.math.pow
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LongDoubleTest {
    private fun mkSoftFloat(
        r: RoundingMode = RoundingMode.RoundNearEven,
        p: FWRBank.PrecisionControl = FWRBank.PrecisionControl.ExtendedDouble,
    ) = object : SoftFloat() {
        override val roundingMode = r
        override val f80RoundingPrecision = p
        override val exceptionFlags = mutableSetOf<Flag>()
    }

    private fun Float.maybeRound(precision: Float?) = if (precision != null) {
        round(precision)
    } else {
        this
    }

    private fun Double.maybeRound(precision: Double?) = if (precision != null) {
        round(precision)
    } else {
        this
    }

    @Test fun convertFloat() {
        val sf = mkSoftFloat()
        fun run(what1: Float, what2: Double) {
            assertEquals(what1, what1.longDouble(sf).float)
            assertEquals(what2, what2.longDouble(sf).double)
        }

        run(0f, 0.0)
        run(-0f, -0.0)
        run(1f, 1.0)
        run(FLOAT_MIN, DOUBLE_MIN)
        run(FLOAT_MAX, DOUBLE_MAX)
        run(Float.NaN, Double.NaN)
        run(-Float.NaN, -Double.NaN)
        run(Float.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        run(Float.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
        run(1e-43f, 1e-200)
        run(1e37f, 1e200)
    }

    @Test fun convertInt() {
        fun SoftFloat.run(what1: Int, what2: Long) {
            assertEquals(what1, what1.longDouble(this).int)
            assertEquals(what2, what2.longDouble(this).long)
        }

        RoundingMode.values().forEach { r ->
            val sf = mkSoftFloat(r)
            sf.run(0, 0)
            sf.run(INT_MIN, LONG_MIN)
            sf.run(INT_MAX, LONG_MAX)
        }
    }

    // Note: RoundOdd and RoundNearMaxMag are not tested since they are not used
    @Test fun convertFloatToInt() {
        fun SoftFloat.run(what1: Double, what2: Long) {
            assertEquals(what2, what1.longDouble(this).long)
            assertEquals(what2, what1.float.longDouble(this).long)

            val nearestInt = what1.longDouble(this).roundedToNearestInt
            assertEquals(what2.double, nearestInt.double)
            assertEquals(what2.float, nearestInt.float)
        }

        mkSoftFloat(RoundingMode.RoundNearEven).let {
            it.run(123.5, 124)
            it.run(124.5, 124)
            it.run(-123.5, -124)
            it.run(-124.5, -124)
        }

        mkSoftFloat(RoundingMode.RoundMinMag).let {
            it.run(123.4, 123)
            it.run(123.5, 123)
            it.run(123.6, 123)
            it.run(-123.4, -123)
            it.run(-123.5, -123)
            it.run(-123.6, -123)
        }

        mkSoftFloat(RoundingMode.RoundMin).let {
            it.run(123.4, 123)
            it.run(123.5, 123)
            it.run(123.6, 123)
            it.run(-123.4, -124)
            it.run(-123.5, -124)
            it.run(-123.6, -124)
        }

        mkSoftFloat(RoundingMode.RoundMax).let {
            it.run(123.4, 124)
            it.run(123.5, 124)
            it.run(123.6, 124)
            it.run(-123.4, -123)
            it.run(-123.5, -123)
            it.run(-123.6, -123)
        }
    }

    @Test fun arithm() {
        fun LongDouble.assertEq(other: LongDouble) {
            assertEquals(high, other.high)
            assertEquals(low, other.low)
            if (!isNaN && !other.isNaN) {
                assertEquals(0, compareTo(other))
            }
        }

        fun LongDouble.fixMinusZero() = if (isZero && sign && sf.roundingMode == RoundingMode.RoundMin) {
            // Softfloat inverts signs of zeros when RoundMin is used
            -this
        } else {
            this
        }

        fun Float.fixMinusZero(sf: SoftFloat) = if (this == 0.0f && sign && sf.roundingMode == RoundingMode.RoundMin) {
            // Softfloat inverts signs of zeros when RoundMin is used
            -this
        } else {
            this
        }

        fun Double.fixMinusZero(sf: SoftFloat) = if (this == 0.0 && sign && sf.roundingMode == RoundingMode.RoundMin) {
            // Softfloat inverts signs of zeros when RoundMin is used
            -this
        } else {
            this
        }

        fun SoftFloat.run(
            what1: Pair<Float, Float>,
            what2: Pair<Double, Double>,
            precision1: Float? = null,
            precision2: Double? = null,
        ) {
            val ldPair1 = what1.first.longDouble(this) to what1.second.longDouble(this)
            val ldPair2 = what2.first.longDouble(this) to what2.second.longDouble(this)

            (ldPair1.first + ldPair1.second).let { sum ->
                sum.assertEq(ldPair1.second + ldPair1.first)
                assertEquals(
                    (what1.first + what1.second).fixMinusZero(this).maybeRound(precision1),
                    sum.fixMinusZero().float.maybeRound(precision1),
                )
            }

            (ldPair1.first * ldPair1.second).let { prod ->
                prod.assertEq(ldPair1.second * ldPair1.first)
                assertEquals((what1.first * what1.second).maybeRound(precision1), prod.float.maybeRound(precision1))
            }

            assertEquals(
                (what1.first - what1.second).fixMinusZero(this).maybeRound(precision1),
                (ldPair1.first - ldPair1.second).fixMinusZero().float.maybeRound(precision1),
            )

            assertEquals(
                (what1.second - what1.first).fixMinusZero(this).maybeRound(precision1),
                (ldPair1.second - ldPair1.first).fixMinusZero().float.maybeRound(precision1),
            )

            assertEquals(
                (what1.first / what1.second).maybeRound(precision1),
                (ldPair1.first / ldPair1.second).float.maybeRound(precision1),
            )

            assertEquals(
                (what1.second / what1.first).maybeRound(precision1),
                (ldPair1.second / ldPair1.first).float.maybeRound(precision1),
            )

            if (f80RoundingPrecision != FWRBank.PrecisionControl.Single) {
                (ldPair2.first + ldPair2.second).let { sum ->
                    sum.assertEq(ldPair2.second + ldPair2.first)
                    assertEquals(
                        (what2.first + what2.second).fixMinusZero(this).maybeRound(precision2),
                        sum.fixMinusZero().double.maybeRound(precision2),
                    )
                }

                (ldPair2.first * ldPair2.second).let { prod ->
                    prod.assertEq(ldPair2.second * ldPair2.first)
                    assertEquals(
                        (what2.first * what2.second).maybeRound(precision2),
                        prod.double.maybeRound(precision2)
                    )
                }

                assertEquals(
                    (what2.first - what2.second).fixMinusZero(this).maybeRound(precision2),
                    (ldPair2.first - ldPair2.second).fixMinusZero().double.maybeRound(precision2),
                )

                assertEquals(
                    (what2.second - what2.first).fixMinusZero(this).maybeRound(precision2),
                    (ldPair2.second - ldPair2.first).fixMinusZero().double.maybeRound(precision2),
                )

                assertEquals(
                    (what2.first / what2.second).maybeRound(precision2),
                    (ldPair2.first / ldPair2.second).double.maybeRound(precision2),
                )

                assertEquals(
                    (what2.second / what2.first).maybeRound(precision2),
                    (ldPair2.second / ldPair2.first).double.maybeRound(precision2),
                )
            }
        }

        RoundingMode.values().forEach { r ->
            FWRBank.PrecisionControl.values().forEach { p ->
                if (p != FWRBank.PrecisionControl.Invalid) {
                    val sf = mkSoftFloat(r, p)
                    sf.run(0f to 0f, 0.0 to 0.0)
                    sf.run(0f to -0f, 0.0 to -0.0)
                    sf.run(
                        1.2f to 2.3f,
                        4.5 to 6.7,
                        0.000001f,
                        0.000001,
                    )
                    sf.run(
                        123.2f to 235.3f,
                        123.2 to 235.3,
                        0.000001f,
                        0.000001,
                    )
                    sf.run(
                        0.0031f to 0.0425f,
                        0.00000051 to 0.000008471,
                        0.000002f,
                        0.000000001,
                    )
                    sf.run(0f to FLOAT_MIN, 0.0 to DOUBLE_MIN)
                    sf.run(0f to FLOAT_MAX, 0.0 to DOUBLE_MAX)
                    sf.run(-0f to FLOAT_MAX, -0.0 to DOUBLE_MAX)
                    sf.run(Float.NaN to -Float.NaN, Double.NaN to -Double.NaN)
                    sf.run(0f to -Float.NaN, 0.0 to -Double.NaN)
                    sf.run(-0f to Float.NaN, -0.0 to Double.NaN)
                    sf.run(Float.NaN to FLOAT_MIN, Double.NaN to DOUBLE_MIN)
                    sf.run(Float.NaN to FLOAT_MAX, Double.NaN to DOUBLE_MAX)
                    sf.run(
                        Float.POSITIVE_INFINITY to Float.NEGATIVE_INFINITY,
                        Double.POSITIVE_INFINITY to Double.NEGATIVE_INFINITY,
                    )
                    sf.run(Float.POSITIVE_INFINITY to Float.NaN, Double.POSITIVE_INFINITY to Double.NaN)
                    sf.run(Float.NaN to Float.NEGATIVE_INFINITY, Double.NaN to Double.NEGATIVE_INFINITY)
                }
            }
        }
    }

    @Test fun unaryMinus() {
        val sf = mkSoftFloat()

        fun run(what: Double) {
            assertEquals(-what, (-what.longDouble(sf)).double)
            assertEquals(what, (-(-what).longDouble(sf)).double)
        }

        run(0.0)
        run(Double.NaN)
        run(123.4)
    }

    @Test fun compare() {
        val sf = mkSoftFloat()

        assert(234.5.longDouble(sf) > 123.4.longDouble(sf))
        assert(123.4.longDouble(sf) < 234.5.longDouble(sf))
        assert(123.4.longDouble(sf) == 123.4.longDouble(sf))
        assertNotEquals(123.4.longDouble(sf), 234.5.longDouble(sf))

        assertEquals(0.0.longDouble(sf), (-0.0).longDouble(sf))
        Double.NaN.longDouble(sf).let { nan ->
            assertNotEquals(nan, nan)
            assertNotEquals(-nan, nan)
        }

        val sorted = arrayOf(
            0.0f,
            Double.MIN_VALUE, Double.MAX_VALUE,
            Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
            234.5f, 123.4, 123.42,
            Float.MIN_VALUE, Float.MAX_VALUE,
            Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY,
        ).map {
            if (it is Float) {
                it.longDouble(sf)
            } else {
                (it as Double).longDouble(sf)
            }
        }.sorted().map {
            it.double
        }

        val expected = listOf(
            Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
            0.0, Double.MIN_VALUE, Float.MIN_VALUE.double,
            123.4, 123.42, 234.5,
            Float.MAX_VALUE.double, Double.MAX_VALUE,
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
        )

        assertEquals(expected, sorted)
    }

    @Test fun abs() {
        val sf = mkSoftFloat()
        fun run(what: Double) {
            assertEquals(what, what.longDouble(sf).abs.double)
            assertEquals(what, (-what).longDouble(sf).abs.double)
            assertEquals(what, (-(what.longDouble(sf))).abs.double)
        }

        run(0.0)
        run(123.4)
        run(Double.NaN)
        run(Double.MIN_VALUE)
        run(Double.MAX_VALUE)
    }

    @Test fun sqrt() {
        val sf = mkSoftFloat()
        fun run(what: Double, precision: Double? = null) {
            val sqrt = what.longDouble(sf).sqrt
            if (what < 0.0) {
                assert(sqrt.isNaN)
            } else {
                assertEquals(kotlin.math.sqrt(what).maybeRound(precision), sqrt.double.maybeRound(precision))
            }
        }

        run(0.0)
        run(-0.0)
        run(Double.NaN)
        run(-Double.NaN)
        run(25.0)
        run(-25.0)
        run(1.0)
        run(2.0, 1e-10)
        run(Double.MAX_VALUE, 1e-10)
    }

    @Test fun fscale() {
        val sf = mkSoftFloat()
        fun run(a: Double, n: Double) {
            assertEquals(a * (2.0.pow(kotlin.math.floor(n))), a.longDouble(sf).fscale(n.longDouble(sf)).double)
        }

        run(10.0, -0.0)
        run(10.0, 0.3)
        run(10.0, 2.2)
        run(10.0, 2.5)
        run(10.0, 2.8)
        run(10.0, 3.0)
        run(10.0, Double.NaN)
        run(10.0, Double.POSITIVE_INFINITY)
        run(Double.POSITIVE_INFINITY, 10.0)
        run(Double.NaN, Double.NaN)
    }
}
