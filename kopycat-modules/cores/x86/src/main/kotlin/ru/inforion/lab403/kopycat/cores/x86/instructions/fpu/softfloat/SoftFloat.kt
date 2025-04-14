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
package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.softfloat

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.FWRBank
import ru.inforion.lab403.kopycat.cores.x86.instructions.fpu.LongDouble
import kotlin.math.max
import kotlin.math.min

private typealias Float80 = LongDouble

/** Based on Berkeley SoftFloat v3E */
abstract class SoftFloat {
    companion object {
        private inline val Float.frac get() = (toRawBits() and 0x007FFFFF).uint
        private inline val Float.exp get() = ((toRawBits() ushr 23) and 0xFF).ushort
        inline val Float.sign get() = toRawBits() ushr 31 != 0
        private inline val Float.isSignalingNaN get() = toRawBits().let { bits ->
            (bits and 0x7FC00000) == 0x7F800000 && (bits and 0x003FFFFF) != 0
        }

        private inline val Double.frac get() = (toRawBits() and 0x000F_FFFF_FFFF_FFFFL).ulong
        private inline val Double.exp get() = ((toRawBits() ushr 52) and 0x7FF).ushort
        inline val Double.sign get() = toRawBits() ushr 63 != 0L
        private inline val Double.isSignalingNaN get() = toRawBits().let { bits ->
            (bits and 0x7FF8_0000_0000_0000L) == 0x7FF0_0000_0000_0000L && (bits and 0x0007_FFFF_FFFF_FFFFL) != 0L
        }

        private inline val Float80.exp get() = high and 0x7FFFu

        private inline val CommonNaN.float get() = Float.fromBits(
            ((sign.uint shl 31) or 0x7FC0_0000u or (high ushr 41).uint).int
        )

        private inline val CommonNaN.double get() = Double.fromBits(
            ((sign.ulong shl 63) or 0x7FF8_0000_0000_0000uL or (high ushr 12)).long
        )

        private fun packFloat(sign: Boolean, exp: UShort, frac: UInt) = Float.fromBits(
            ((sign.uint shl 31) + (exp shl 23) + frac).int
        )

        private fun packDouble(sign: Boolean, exp: UShort, frac: ULong) = Double.fromBits(
            ((sign.ulong shl 63) + (exp.ulong_z shl 52) + frac).long
        )

        private fun UInt.normSubnormalF32Sig(): Pair<UShort, UInt> {
            val shiftDist = (countLeadingZeroBits() - 8).byte
            return (1 - shiftDist).ushort to (this shl shiftDist.int_z)
        }

        private fun ULong.normSubnormalF64Sig(): Pair<UShort, ULong> {
            val shiftDist = (countLeadingZeroBits() - 11).byte
            return (1 - shiftDist).ushort to (this shl shiftDist.int_z)
        }

        private fun ULong.normExtF80SigM(): Pair<ULong, Int> {
            val shiftDist = countLeadingZeroBits()
            return this shl shiftDist to -shiftDist
        }

        private fun shiftRightJam32(a: UInt, dist: Int) = if (dist < 31) {
            (a ushr dist) or (a shl ((-dist) and 31) != 0u).uint
        } else {
            (a != 0u).uint
        }

        private fun shiftRightJam64(a: ULong, dist: Int) = if (dist < 63) {
            (a ushr dist) or (a shl ((-dist) and 63) != 0uL).ulong
        } else {
            (a != 0uL).ulong
        }

        private fun shortShiftRightJam64(a: ULong, dist: Int) =
            (a ushr dist) or ((a and ((1uL shl dist) - 1u)) != 0uL).ulong

        private fun shortShiftRightJamM(sizeWords: Int, a: Array<UInt>, aPtr: Int, dist: Int, z: Array<UInt>) {
            val uNegDist = -(dist.byte)
            var wordA = a[aPtr]
            var partWordZ = wordA ushr dist

            if (partWordZ shl dist != wordA) {
                partWordZ = partWordZ or 1u
            }

            val lastIndex = sizeWords - 1
            for (index in 0 until lastIndex) {
                wordA = a[aPtr + index + 1]
                z[index] = wordA shl (uNegDist and 31) or partWordZ
                partWordZ = wordA ushr dist
            }

            z[lastIndex] = partWordZ
        }

        private fun shiftRightJamM(sizeWords: Int, a: Array<UInt>, dist: Int, z: Array<UInt>) {
            var wordJam = 0u
            var wordDist = dist ushr 5
            var zPtr = 0

            if (wordDist != 0) {
                if (sizeWords < wordDist) {
                    wordDist = sizeWords
                }

                zPtr = 0
                var i = wordDist
                do {
                    wordJam = a[zPtr++]
                    if (wordJam != 0u) {
                        break
                    }
                    --i
                } while (i != 0)

                zPtr = 0
            }

            if (wordDist < sizeWords) {
                var aPtr = wordDist
                val innerDist = dist and 31
                if (innerDist != 0) {
                    shortShiftRightJamM(
                        sizeWords - wordDist,
                        a,
                        aPtr,
                        innerDist,
                        z,
                    )
                    if (wordDist == 0) {
                        if (wordJam != 0u) {
                            z[0] = z[0] or 1u
                        }
                        return
                    }
                } else {
                    zPtr = 0
                    for (i in sizeWords - wordDist downTo 1) {
                        z[zPtr++] = a[aPtr++]
                    }
                }
                zPtr = sizeWords - wordDist
            }

            do {
                z[zPtr] = 0u
                --wordDist
            } while (wordDist != 0)

            if (wordJam != 0u) {
                z[0] = z[0] or 1u
            }
        }

        private fun shortShiftLeftM(sizeWords: Int, a: Array<UInt>, dist: Int, z: Array<UInt>, zPtr: Int) {
            val uNegDist = -(dist.byte)
            var partWordZ = a[sizeWords - 1] shl dist

            for (index in sizeWords - 1 downTo 1) {
                val wordA = a[index - 1]
                z[zPtr + index] = partWordZ or (wordA ushr (uNegDist and 31))
                partWordZ = wordA shl dist
            }

            z[zPtr] = partWordZ
        }

        private fun shiftLeftM(sizeWords: Int, a: Array<UInt>, dist: Int, z: Array<UInt>) {
            var wordDist = dist ushr 5

            if (wordDist < sizeWords) {
                val innerDist = dist and 31
                if (innerDist != 0) {
                    shortShiftLeftM(
                        sizeWords - wordDist,
                        a,
                        innerDist,
                        z,
                        wordDist,
                    )

                    if (wordDist == 0) {
                        return
                    }
                } else {
                    var aPtr = sizeWords - wordDist - 1
                    var zPtr = sizeWords - 1
                    for (i in sizeWords - wordDist downTo 1) {
                        z[zPtr--] = a[aPtr--]
                    }
                }
            } else {
                wordDist = sizeWords
            }

            var zPtr = 0
            do {
                z[zPtr++] = 0u
                --wordDist
            } while (wordDist != 0)
        }

        private fun addM(sizeWords: Int, a: Array<UInt>, b: Array<UInt>, z: Array<UInt>) {
            var index = 0
            var carry = 0

            while (true) {
                val wordA = a[index]
                val word = wordA + b[index] + carry
                z[index] = word
                if (index == sizeWords - 1) {
                    break
                }
                if (word != wordA) {
                    carry = (word < wordA).int
                }
                index++
            }
        }

        private fun subM(sizeWords: Int, a: Array<UInt>, b: Array<UInt>, z: Array<UInt>) {
            var index = 0
            val lastIndex = sizeWords - 1
            var borrow = false

            while (true) {
                val wordA = a[index]
                val wordB = b[index]
                z[index] = wordA - wordB - borrow.uint
                if (index == lastIndex) {
                    break
                }
                borrow = if (borrow) {
                    wordA <= wordB
                } else {
                    wordA < wordB
                }
                index++
            }
        }

        private fun compare96M(a: Array<UInt>, b: Array<UInt>): Int {
            var index = 2
            while (true) {
                val wordA = a[index]
                val wordB = b[index]
                if (wordA != wordB) {
                    return if (wordA < wordB) {
                        -1
                    } else {
                        1
                    }
                }
                if (index == 0) break
                --index
            }

            return 0
        }

        private fun mul64To128M(a: ULong, b: ULong): Array<UInt> {
            val a32 = (a ushr 32)[31..0]
            val a0 = a[31..0]
            val b32 = (b ushr 32)[31..0]
            val b0 = b[31..0]

            var z0 = a0 * b0
            val mid1 = a32 * b0
            var mid = mid1 + a0 * b32
            var z64 = a32 * b32

            z64 += ((mid < mid1).ulong shl 32) or (mid ushr 32)
            mid = mid shl 32
            z0 += mid

            z64 += (z0 < mid).ulong
            return arrayOf(z0.uint, (z0 ushr 32).uint, z64.uint, (z64 ushr 32).uint)
        }

        private fun remStepMBy32(
            sizeWords: Int,
            rem: Array<UInt>,
            dist: Int,
            b: Array<UInt>,
            q: UInt,
            z: Array<UInt>,
        ) {
            val lastIndex = sizeWords - 1

            var dwordProd = b[0].ulong_z * q.ulong_z
            var wordRem = rem[0]
            var wordShiftedRem = wordRem shl dist
            var wordProd = dwordProd.uint

            z[0] = wordShiftedRem - wordProd
            if (lastIndex != 0) {
                val uNegDist = -(dist.byte)
                var borrow = wordShiftedRem < wordProd
                var index = 0
                while (true) {
                    wordShiftedRem = wordRem ushr (uNegDist and 31)
                    index++
                    dwordProd = b[index].ulong_z * q + (dwordProd ushr 32)
                    wordRem = rem[index]
                    wordShiftedRem = wordShiftedRem or (wordRem shl dist)
                    wordProd = dwordProd.uint
                    z[index] = wordShiftedRem - wordProd - borrow.uint
                    if (index == lastIndex) {
                        break
                    }
                    borrow = if (borrow) {
                        wordShiftedRem <= wordProd
                    } else {
                        wordShiftedRem < wordProd
                    }
                }
            }
        }

        private fun shortShiftLeft64To96M(a: ULong, dist: Int) = arrayOf(
            (a shl dist).uint,
            (a ushr (32 - dist)).uint,
            ((a ushr (32 - dist)) ushr 32).uint,
        )

        private fun approxRecip32_1(a: UInt): UInt {
            val index = (a ushr 27) and 0x0Fu
            val eps = (a ushr 11).ushort.uint_z
            val r0 = approxRecip1k0s[index] - ((approxRecip1k1s[index] * eps) ushr 20)
            val sigma0 = ((r0 * a.ulong_z) ushr 7).uint.inv()
            val r = ((r0 shl 16) + ((r0 * sigma0.ulong_z) ushr 24)).uint
            val sqrSigma0 = ((sigma0.ulong_z * sigma0.ulong_z) ushr 32).uint
            return r + ((r.ulong_z * sqrSigma0.ulong_z) ushr 48).uint
        }

        private fun sub1XM(sizeWords: Int, z: Array<UInt>) {
            var index = 0
            while (true) {
                val wordA = z[index]
                z[index] = wordA - 1u
                if (wordA != 0u || index == sizeWords - 1) {
                    break
                }
                ++index
            }
        }

        private fun approxRecipSqrt32_1(oddExpA: UInt, a: UInt): UInt {
            val index = (((a ushr 27) and 0x0Eu) + oddExpA).int
            val eps = (a ushr 12).ushort
            val r0 = approxRecipSqrt1k0s[index] - ((approxRecipSqrt1k1s[index] * eps.uint_z) ushr 20)
            var eSqrR0 = r0 * r0
            if (oddExpA == 0u) {
                eSqrR0 = eSqrR0 shl 1
            }
            val sigma0 = ((eSqrR0 * a.ulong_z) ushr 23).uint.inv()
            var r = ((r0 shl 16) + ((r0 * sigma0.ulong_z) ushr 25)).uint
            val sqrSigma0 = ((sigma0.ulong_z * sigma0.ulong_z) ushr 32).uint
            r += ((((r ushr 1) + (r ushr 3) - (r0 shl 14)) * sqrSigma0.ulong_z) ushr 48).uint
            if (r and 0x8000_0000u == 0u) {
                r = 0x8000_0000u
            }
            return r
        }

        private fun propagateNaNF80ReturnLarger(a: Float80, b: Float80): Float80 {
            val expA = a.exp
            val expB = b.exp

            if (expA < expB) return b
            if (expB < expA) return a
            if (a.low < b.low) return b
            if (b.low < a.low) return a
            return if (a.high < b.high) a else b
        }

        private fun compareNonnormExtF80M(a: Float80, b: Float80): Int {
            val signB = b.sign

            if ((a.high xor b.high).uint_z and 0x8000u != 0u) {
                if (a.low or b.low == 0uL) {
                    return 0
                }
                return if (signB) 1 else -1
            }

            var expA = a.exp.int_z
            var expB = b.exp.int_z
            if (expA == 0x7FFF) {
                if (expB == 0x7FFF) {
                    return 0
                }
                return if (!signB) 1 else -1
            }
            if (expB == 0x7FFF) {
                return if (signB) 1 else -1
            }

            if (expA == 0) {
                expA = 1
            }
            if (expB == 0) {
                expB = 1
            }

            var sigA = a.low
            var sigB = b.low

            if (sigA and 0x8000_0000_0000_0000uL == 0uL) {
                if (sigA != 0uL) {
                    sigA.normExtF80SigM().let {
                        sigA = it.first
                        expA += it.second
                    }
                } else {
                    expA = -128
                }
            }

            if (sigB and 0x8000_0000_0000_0000uL == 0uL) {
                if (sigB != 0uL) {
                    sigB.normExtF80SigM().let {
                        sigB = it.first
                        expB += it.second
                    }
                } else {
                    expB = -128
                }
            }

            if (signB) {
                if (expA < expB) {
                    return 1
                }
                if (expB < expA || sigB < sigA) {
                    return -1
                }
            } else {
                if (expB < expA) {
                    return 1
                }
                if (expA < expB || sigA < sigB) {
                    return -1
                }
            }

            return (sigA != sigB).int
        }

        const val DEFAULT_NAN_F80_HIGH: UShort = 0xFFFFu
        const val DEFAULT_NAN_F80_LOW: ULong = 0xC000_0000_0000_0000uL

        const val FLOAT80_INFINITY_HIGH: UShort = 0x7FFFu
        const val FLOAT80_INFINITY_LOW: ULong = 0x8000_0000_0000_0000uL

        private val approxRecip1k0s: Array<UShort> = arrayOf(
            0xFFC4u, 0xF0BEu, 0xE363u, 0xD76Fu, 0xCCADu, 0xC2F0u, 0xBA16u, 0xB201u,
            0xAA97u, 0xA3C6u, 0x9D7Au, 0x97A6u, 0x923Cu, 0x8D32u, 0x887Eu, 0x8417u,
        )

        private val approxRecip1k1s: Array<UShort> = arrayOf(
            0xF0F1u, 0xD62Cu, 0xBFA1u, 0xAC77u, 0x9C0Au, 0x8DDBu, 0x8185u, 0x76BAu,
            0x6D3Bu, 0x64D4u, 0x5D5Cu, 0x56B1u, 0x50B6u, 0x4B55u, 0x4679u, 0x4211u,
        )

        private val approxRecipSqrt1k0s: Array<UShort> = arrayOf(
            0xB4C9u, 0xFFABu, 0xAA7Du, 0xF11Cu, 0xA1C5u, 0xE4C7u, 0x9A43u, 0xDA29u,
            0x93B5u, 0xD0E5u, 0x8DEDu, 0xC8B7u, 0x88C6u, 0xC16Du, 0x8424u, 0xBAE1u,
        )

        private val approxRecipSqrt1k1s: Array<UShort> = arrayOf(
            0xA5A5u, 0xEA42u, 0x8C21u, 0xC62Du, 0x788Fu, 0xAA7Fu, 0x6928u, 0x94B6u,
            0x5CC7u, 0x8335u, 0x52A6u, 0x74E2u, 0x4A3Eu, 0x68FEu, 0x432Bu, 0x5EFDu,
        )
    }

    abstract val roundingMode: RoundingMode
    abstract val f80RoundingPrecision: FWRBank.PrecisionControl
    abstract val exceptionFlags: MutableSet<Flag>?

    private data class CommonNaN(val sign: Boolean, val high: ULong, val low: ULong)

    private fun Flag.raise() {
        exceptionFlags?.add(this)
    }

    private val Float.commonNaN: CommonNaN get() {
        if (isSignalingNaN) {
            Flag.Invalid.raise()
        }

        return CommonNaN(
            sign,
            toRawBits().ulong_z shl 41,
            0uL,
        )
    }

    private val Double.commonNaN: CommonNaN get() {
        if (isSignalingNaN) {
            Flag.Invalid.raise()
        }

        return CommonNaN(
            sign,
            toRawBits().ulong shl 12,
            0uL,
        )
    }

    private val Float80.commonNaN: CommonNaN get() {
        if (isSignalingNaN) {
            Flag.Invalid.raise()
        }

        return CommonNaN(
            sign,
            low shl 1,
            0u,
        )
    }

    private inline val CommonNaN.f80 get() = Float80(
        ((sign.uint shl 15) or 0x7FFFu).ushort,
        DEFAULT_NAN_F80_LOW or (high ushr 1),
        this@SoftFloat,
    )

    internal fun packF80(sign: Boolean, exp: UShort, frac: ULong) = Float80(
        ((sign.uint shl 15) + exp).ushort,
        frac,
        this,
    )

    private fun roundToI32(sign: Boolean, sig: ULong, exact: Boolean, roundingModeOverride: RoundingMode? = null): Int {
        val rounding = roundingModeOverride ?: roundingMode

        var roundIncrement = 0x0800u
        if (rounding != RoundingMode.RoundNearMaxMag && rounding != RoundingMode.RoundNearEven) {
            roundIncrement = 0u

            if (sign) {
                if (rounding == RoundingMode.RoundMin || rounding == RoundingMode.RoundOdd) {
                    roundIncrement = 0x0FFFu
                }
            } else if (rounding == RoundingMode.RoundMax) {
                roundIncrement = 0x0FFFu
            }
        }

        val roundBits = sig and 0x0FFFu
        val newSig = sig + roundIncrement
        if (newSig and 0xFFFF_F000_0000_0000uL != 0uL) {
            Flag.Invalid.raise()
            return -1
        }

        var sig32 = (newSig ushr 12).uint
        if (roundBits == 0x0800uL && rounding == RoundingMode.RoundNearEven) {
            sig32 = sig32 and 1u.inv()
        }

        var z = if (sign) {
            -(sig32.int)
        } else {
            sig32.int
        }

        if (z != 0 && (z < 0) xor sign) {
            Flag.Invalid.raise()
            return -1
        }

        if (roundBits != 0uL) {
            if (rounding == RoundingMode.RoundOdd) {
                z = z or 1
            }
            if (exact) {
                Flag.Inexact.raise()
            }
        }
        return z
    }

    private fun roundToI64(sign: Boolean, extSig: Array<UInt>, exact: Boolean): Long {
        var sig = (extSig[2].ulong_z shl 32) or extSig[1].ulong_z
        val sigExtra = extSig[0]

        val increment = if (roundingMode == RoundingMode.RoundNearMaxMag || roundingMode == RoundingMode.RoundNearEven) {
            0x8000_0000uL <= sigExtra
        } else if (sigExtra != 0u) {
            sign && (roundingMode == RoundingMode.RoundMin || roundingMode == RoundingMode.RoundOdd) ||
                    !sign && roundingMode == RoundingMode.RoundMax
        } else {
            false
        }

        if (increment) {
            ++sig
            if (sig == 0uL) {
                Flag.Invalid.raise()
                return -1
            }
            if (sigExtra == 0x8000_0000u && roundingMode == RoundingMode.RoundNearEven) {
                sig = sig and 1uL.inv()
            }
        }

        var z = if (sign) {
            -(sig.long)
        } else {
            sig.long
        }

        if (z != 0L && (z < 0) xor sign) {
            Flag.Invalid.raise()
            return -1
        }

        if (sigExtra != 0u) {
            if (roundingMode == RoundingMode.RoundOdd) {
                z = z or 1L
            }
            if (exact) {
                Flag.Inexact.raise()
            }
        }

        return z
    }

    private fun roundPackFloat(sign: Boolean, exp: UShort, frac: UInt): Float {
        val roundIncrement = if (roundingMode != RoundingMode.RoundNearEven && roundingMode != RoundingMode.RoundNearMaxMag) {
            if (sign && roundingMode == RoundingMode.RoundMin || !sign && roundingMode == RoundingMode.RoundMax) {
                0x7F
            } else {
                0
            }
        } else {
            0x40
        }

        var sig = frac
        var newExp = exp.short

        var roundBits = sig and 0x7Fu
        if (0xFDu <= exp) {
            if (newExp < 0) {
                val isTiny = newExp < -1 || sig + roundIncrement < 0x8000_0000u
                sig = shiftRightJam32(sig, -newExp)
                newExp = 0
                roundBits = sig and 0x7fu
                if (isTiny && roundBits != 0u) {
                    Flag.Underflow.raise()
                }
            } else if (0xFD < newExp || 0x8000_0000u <= sig + roundIncrement) {
                Flag.Overflow.raise()
                Flag.Inexact.raise()
                return packFloat(sign, 0xFFu, 0u) - (roundIncrement == 0).int
            }
        }

        sig = (sig + roundIncrement) ushr 7
        if (roundBits != 0u) {
            Flag.Inexact.raise()
            if (roundingMode == RoundingMode.RoundOdd) {
                sig = sig or 1u
                return packFloat(sign, newExp.ushort, sig)
            }
        }

        sig = sig and (
            (roundBits xor 0x40u == 0u && roundingMode == RoundingMode.RoundNearEven).uint
        ).inv()

        if (sig == 0u) {
            newExp = 0
        }

        return packFloat(sign, newExp.ushort, sig)
    }

    private fun roundPackDouble(sign: Boolean, exp: UShort, frac: ULong): Double {
        val roundIncrement = if (roundingMode != RoundingMode.RoundNearEven && roundingMode != RoundingMode.RoundNearMaxMag) {
            if (sign && roundingMode == RoundingMode.RoundMin || !sign && roundingMode == RoundingMode.RoundMax) {
                0x03FF
            } else {
                0
            }
        } else {
            0x0200
        }

        var sig = frac
        var newExp = exp.short

        var roundBits = sig and 0x03FFu
        if (0x07FDu <= exp) {
            if (newExp < 0) {
                val isTiny = newExp < -1 || sig + roundIncrement < 0x8000_0000_0000_0000uL
                sig = shiftRightJam64(sig, -newExp)
                newExp = 0
                roundBits = sig and 0x3ffu
                if (isTiny && roundBits != 0uL) {
                    Flag.Underflow.raise()
                }
            } else if (0x7FD < newExp || 0x8000_0000_0000_0000uL <= sig + roundIncrement) {
                Flag.Overflow.raise()
                Flag.Inexact.raise()
                return packDouble(sign, 0x7FFu, 0u) - (roundIncrement == 0).int
            }
        }

        sig = (sig + roundIncrement) ushr 10
        if (roundBits != 0uL) {
            Flag.Inexact.raise()
            if (roundingMode == RoundingMode.RoundOdd) {
                sig = sig or 1u
                return packDouble(sign, newExp.ushort, sig)
            }
        }

        sig = sig and (
            (roundBits xor 0x0200u == 0uL && roundingMode == RoundingMode.RoundNearEven).ulong
        ).inv()

        if (sig == 0uL) {
            newExp = 0
        }

        return packDouble(sign, newExp.ushort, sig)
    }

    private fun propagateNaNF80(a: Float80, b: Float80?): Float80 {
        val aSigNaN = a.isSignalingNaN
        return if (b == null) {
            if (aSigNaN) {
                Flag.Invalid.raise()
            }
            a
        } else {
            val bSigNaN = b.isSignalingNaN
            if (aSigNaN || bSigNaN) {
                Flag.Invalid.raise()

                if (aSigNaN) {
                    if (bSigNaN) {
                        propagateNaNF80ReturnLarger(a, b)
                    } else {
                        if (b.isNaN) {
                            b
                        } else {
                            a
                        }
                    }
                } else {
                    if (a.isNaN) {
                        a
                    } else {
                        b
                    }
                }
            } else {
                propagateNaNF80ReturnLarger(a, b)
            }
        }.let {
            Float80(
                it.high,
                it.low or DEFAULT_NAN_F80_LOW,
                this,
            )
        }
    }

    private fun tryPropagateNaNExtF80(a: Float80, b: Float80) = if (a.isNaN || b.isNaN) {
        propagateNaNF80(a, b)
    } else {
        null
    }

    private fun roundPackF80Precision80(sign: Boolean, argExp: Int, extSig: Array<UInt>): Float80 {
        var exp = argExp
        var sig = (extSig[2].ulong_z shl 32) or extSig[1].ulong_z
        var sigExtra = extSig[0]

        var doIncrement = (0x8000_0000uL <= sigExtra)
        if (roundingMode != RoundingMode.RoundNearEven && roundingMode != RoundingMode.RoundNearMaxMag) {
            doIncrement = sign && roundingMode == RoundingMode.RoundMin ||
                    !sign && roundingMode == RoundingMode.RoundMax
            doIncrement = doIncrement && sigExtra != 0u
        }

        if (0x7FFDu <= exp.uint - 1u) {
            if (exp <= 0) {
                val isTiny = exp < 0 || !doIncrement || sig < 0xFFFF_FFFF_FFFF_FFFFuL
                shiftRightJamM(3, extSig, 1 - exp, extSig)
                sig = (extSig[2].ulong_z shl 32) or extSig[1].ulong_z
                sigExtra = extSig[0]

                if (sigExtra != 0u) {
                    if (isTiny) {
                        Flag.Underflow.raise()
                    }
                    Flag.Inexact.raise()
                    if (roundingMode == RoundingMode.RoundOdd) {
                        sig = sig or 1u
                        return packF80(sign, 0u, sig)
                    }
                }

                doIncrement = (0x8000_0000uL <= sigExtra)
                if (roundingMode != RoundingMode.RoundNearEven && roundingMode != RoundingMode.RoundNearMaxMag) {
                    doIncrement = sign && roundingMode == RoundingMode.RoundMin ||
                            !sign && roundingMode == RoundingMode.RoundMax
                    doIncrement = doIncrement && sigExtra != 0u
                }
                if (doIncrement) {
                    ++sig
                    sig = sig and (
                        sigExtra and 0x7FFF_FFFFu == 0u && roundingMode == RoundingMode.RoundNearEven
                    ).ulong.inv()
                    exp = (sig and 0x8000_0000_0000_0000uL != 0uL).int
                }
                return packF80(sign, exp.ushort, sig)
            }
            if (0x7FFE < exp || exp == 0x7FFE && sig == 0xFFFF_FFFF_FFFF_FFFFuL && doIncrement) {
                Flag.Overflow.raise()
                Flag.Inexact.raise()
                if (
                    roundingMode == RoundingMode.RoundNearEven ||
                    roundingMode == RoundingMode.RoundNearMaxMag ||
                    sign && roundingMode == RoundingMode.RoundMin ||
                    !sign && roundingMode == RoundingMode.RoundMax
                ) {
                    exp = FLOAT80_INFINITY_HIGH.int_z
                    sig = FLOAT80_INFINITY_LOW
                } else {
                    exp = 0x7FFE
                    sig = 0uL.inv()
                }
                return packF80(sign, exp.ushort, sig)
            }
        }

        if (sigExtra != 0u) {
            Flag.Inexact.raise()
            if (roundingMode == RoundingMode.RoundOdd) {
                sig = sig or 1u
                return packF80(sign, exp.ushort, sig)
            }
        }

        if (doIncrement) {
            ++sig
            sig = if (sig == 0uL) {
                ++exp
                0x8000_0000_0000_0000uL
            } else {
                sig and (
                    sigExtra and 0x7FFF_FFFFu == 0u && roundingMode == RoundingMode.RoundNearEven
                ).ulong.inv()
            }
        }
        return packF80(sign, exp.ushort, sig)
    }

    private fun roundPackF80(sign: Boolean, argExp: Int, extSig: Array<UInt>): Float80 {
        var (roundIncrement, roundMask) = when (f80RoundingPrecision) {
            FWRBank.PrecisionControl.Double -> 0x0000_0000_0000_0400uL to 0x07FFuL
            FWRBank.PrecisionControl.Single -> 0x0000_0080_0000_0000uL to 0x0000_00FF_FFFF_FFFFuL
            else -> return roundPackF80Precision80(sign, argExp, extSig)
        }

        var exp = argExp
        var sig = (extSig[2].ulong_z shl 32) or extSig[1].ulong_z

        if (extSig[0] != 0u) {
            sig = sig or 1u
        }

        if (roundingMode != RoundingMode.RoundNearEven && roundingMode != RoundingMode.RoundNearMaxMag) {
            roundIncrement = if (sign && roundingMode == RoundingMode.RoundMin || !sign && roundingMode == RoundingMode.RoundMax) {
                roundMask
            } else {
                0u
            }
        }

        var roundBits = sig and roundMask
        if (0x7FFDu <= exp.uint - 1u) {
            if (exp <= 0) {
                val isTiny = exp < 0 || sig <= (sig + roundIncrement)
                sig = shiftRightJam64(sig, 1 - exp)
                roundBits = sig and roundMask
                if (roundBits != 0uL) {
                    if (isTiny) {
                        Flag.Underflow.raise()
                    }
                    Flag.Inexact.raise()
                    if (roundingMode == RoundingMode.RoundOdd) {
                        sig = sig or (roundMask + 1u)
                    }
                }
                sig += roundIncrement
                exp = ((sig and 0x8000_0000_0000_0000uL) != 0uL).int
                roundIncrement = roundMask + 1u
                if (roundingMode == RoundingMode.RoundNearEven && roundBits shl 1 == roundIncrement) {
                    roundMask = roundMask or roundIncrement
                }
                sig = sig and roundMask.inv()
                return packF80(sign, exp.ushort, sig)
            }
            if (0x7FFE < exp || exp == 0x7FFE && sig + roundIncrement < sig) {
                Flag.Overflow.raise()
                Flag.Inexact.raise()
                if (
                    roundingMode == RoundingMode.RoundNearEven ||
                    roundingMode == RoundingMode.RoundNearMaxMag ||
                    sign && roundingMode == RoundingMode.RoundMin ||
                    !sign && roundingMode == RoundingMode.RoundMax
                ) {
                    exp = FLOAT80_INFINITY_HIGH.int_z
                    sig = FLOAT80_INFINITY_LOW
                } else {
                    exp = 0x7FFE
                    sig = roundMask.inv()
                }
                return packF80(sign, exp.ushort, sig)
            }
        }

        if (roundBits != 0uL) {
            Flag.Inexact.raise()
            if (roundingMode == RoundingMode.RoundOdd) {
                sig = (sig and roundMask.inv()) or (roundMask + 1u)
                return packF80(sign, exp.ushort, sig)
            }
        }

        sig += roundIncrement
        if (sig < roundIncrement) {
            ++exp
            sig = 0x8000_0000_0000_0000uL
        }
        roundIncrement = roundMask + 1u
        if (roundingMode == RoundingMode.RoundNearEven && roundBits shl 1 == roundIncrement) {
            roundMask = roundMask or roundIncrement
        }
        sig = sig and roundMask.inv()
        return packF80(sign, exp.ushort, sig)
    }

    private fun normRoundPack80(sign: Boolean, argExp: Int, extSig: Array<UInt>): Float80 {
        var exp = argExp

        var shiftDist = 0
        var wordSig = extSig[2]

        if (wordSig == 0u) {
            shiftDist = 32
            wordSig = extSig[1]
            if (wordSig == 0u) {
                shiftDist = 64
                wordSig = extSig[0]
                if (wordSig == 0u) {
                    return packF80(sign, 0u, 0u)
                }
            }
        }

        shiftDist += wordSig.countLeadingZeroBits()
        if (shiftDist != 0) {
            exp -= shiftDist
            shiftLeftM(3, extSig, shiftDist, extSig)
        }

        return roundPackF80(sign, exp, extSig)
    }

    private fun invalidF80() = Float80(DEFAULT_NAN_F80_HIGH, DEFAULT_NAN_F80_LOW, this).also {
        Flag.Invalid.raise()
    }

    /* Public methods */

    fun floatToF80(a: Float): Float80 {
        var frac = a.frac
        var exp = a.exp.uint_z
        val sign = a.sign

        if (exp == 0xFFu) {
            if (frac != 0u) {
                return a.commonNaN.f80
            }
            return packF80(sign, FLOAT80_INFINITY_HIGH, FLOAT80_INFINITY_LOW)
        }
        if (exp == 0u) {
            if (frac == 0u) {
                return packF80(sign, 0u, 0u)
            }

            frac.normSubnormalF32Sig().let {
                exp = it.first.uint_z
                frac = it.second
            }
        }

        return packF80(sign, (exp + 0x3F80u).ushort, (frac.ulong_z or 0x0080_0000uL) shl 40)
    }

    fun doubleToF80(a: Double): Float80 {
        var frac = a.frac
        var exp = a.exp.uint_z
        val sign = a.sign

        if (exp == 0x7FFu) {
            if (frac != 0uL) {
                return a.commonNaN.f80
            }
            return packF80(sign, FLOAT80_INFINITY_HIGH, FLOAT80_INFINITY_LOW)
        }
        if (exp == 0u) {
            if (frac == 0uL) {
                return packF80(sign, 0u, 0u)
            }

            frac.normSubnormalF64Sig().let {
                exp = it.first.uint_z
                frac = it.second
            }
        }

        return packF80(sign, (exp + 0x3C00u).ushort, (frac or 0x0010_0000_0000_0000uL) shl 11)
    }

    fun f80ToInt(a: Float80, exact: Boolean, roundingModeOverride: RoundingMode? = null): Int {
        var frac = a.low
        val exp = a.exp.int_z
        val sign = a.sign

        val shiftCount = 0x4032 - exp
        if (shiftCount <= 0) {
            if (frac ushr 32 != 0uL) {
                Flag.Invalid.raise()
                return -1
            }

            if (-32 < shiftCount) {
                frac = frac shl -shiftCount
            } else if (frac.int != 0) {
                Flag.Invalid.raise()
                return -1
            }
        } else {
            frac = shiftRightJam64(frac, shiftCount)
        }
        return roundToI32(sign, frac, exact, roundingModeOverride)
    }

    fun f80ToLong(a: Float80, exact: Boolean): Long {
        val frac = a.low
        val exp = a.exp.int_z
        val sign = a.sign

        val shiftCount = 0x403E - exp
        if (shiftCount < 0) {
            Flag.Invalid.raise()
            return -1
        }

        val extSig = arrayOf(0u, frac.uint, (frac ushr 32).uint)
        if (shiftCount != 0) {
            shiftRightJamM(3, extSig, shiftCount, extSig)
        }
        return roundToI64(sign, extSig, exact)
    }

    fun f80ToFloat(a: Float80): Float {
        var frac = a.low
        var exp = a.exp.int_z
        val sign = a.sign

        if (exp == 0x7FFF) {
            if (frac and 0x7FFF_FFFF_FFFF_FFFFuL != 0uL) {
                return a.commonNaN.float
            }
            return packFloat(sign, 0xFFu, 0u)
        }

        if (frac and 0x8000_0000_0000_0000uL == 0uL) {
            if (frac == 0uL) {
                return packFloat(sign, 0u, 0u)
            }

            frac.normExtF80SigM().let {
                frac = it.first
                exp += it.second
            }
        }

        val sig32 = shortShiftRightJam64(frac, 33)
        exp -= 0x3F81
        if (exp < -0x1000) {
            exp = -0x1000
        }
        return roundPackFloat(sign, exp.ushort, sig32.uint)
    }

    fun f80ToDouble(a: Float80): Double {
        var frac = a.low
        var exp = a.exp.int_z
        val sign = a.sign

        if (exp == 0x7FFF) {
            if (frac and 0x7FFF_FFFF_FFFF_FFFFuL != 0uL) {
                return a.commonNaN.double
            }
            return packDouble(sign, 0x07FFu, 0u)
        }

        if (frac and 0x8000_0000_0000_0000uL == 0uL) {
            if (frac == 0uL) {
                return packDouble(sign, 0u, 0u)
            }

            frac.normExtF80SigM().let {
                frac = it.first
                exp += it.second
            }
        }

        val sig = shortShiftRightJam64(frac, 1)
        exp -= 0x3C01
        if (exp < -0x1000) {
            exp = -0x1000
        }
        return roundPackDouble(sign, exp.ushort, sig)
    }

    fun roundToNearestIntF80(a: Float80, exact: Boolean): Float80 {
        var frac = a.low
        var exp = a.exp.int_z
        val sign = a.sign

        if (frac and 0x8000_0000_0000_0000uL == 0uL && exp != 0x7FFF) {
            if (frac == 0uL) {
                return packF80(a.sign, 0u, 0u)
            }

            frac.normExtF80SigM().let {
                frac = it.first
                exp += it.second
            }
        }

        if (exp <= 0x3FFE) {
            if (exact) {
                Flag.Inexact.raise()
            }

            val zero = when (roundingMode) {
                RoundingMode.RoundNearEven -> frac and 0x7FFF_FFFF_FFFF_FFFFuL == 0uL
                RoundingMode.RoundNearMaxMag -> exp != 0x3FFE
                RoundingMode.RoundMin -> !sign
                RoundingMode.RoundMax -> sign
                RoundingMode.RoundOdd -> false
                else -> true
            }

            if (zero) {
                return packF80(a.sign, 0u, 0u)
            }

            return packF80(a.sign, 0x3FFFu, 0x8000_0000_0000_0000u)
        }

        if (0x403E <= exp) {
            return Float80(
                a.high,
                if (exp == 0x7FFF) {
                    if (frac and 0x7FFF_FFFF_FFFF_FFFFuL != 0uL) {
                        return propagateNaNF80(a, null)
                    }
                    0x8000_0000_0000_0000uL
                } else {
                    frac
                },
                this,
            )
        }

        val lastBitMask = 1uL shl (0x403E - exp)
        val roundBitsMask = lastBitMask - 1uL

        var high = a.high
        var low = a.low

        if (roundingMode == RoundingMode.RoundNearMaxMag) {
            low += lastBitMask ushr 1
        } else if (roundingMode == RoundingMode.RoundNearEven) {
            low += lastBitMask ushr 1
            if (low and roundBitsMask == 0uL) {
                low = low and lastBitMask.inv()
            }
        } else if (sign && roundingMode == RoundingMode.RoundMin || !sign && roundingMode == RoundingMode.RoundMax) {
            low += roundBitsMask
        }

        low = low and roundBitsMask.inv()
        if (low == 0uL) {
            ++high
            low = 0x8000_0000_0000_0000uL
        }
        if (low != frac) {
            if (roundingMode == RoundingMode.RoundOdd) {
                low = low or lastBitMask
            }
            if (exact) {
                Flag.Inexact.raise()
            }
        }

        return Float80(high, low, this)
    }

    fun addF80M(arg1: Float80, arg2: Float80, argNegateB: Boolean): Float80 {
        var a = arg1
        var b = arg2
        var negateB = argNegateB

        var expA = a.exp.int_z
        var expB = b.exp.int_z

        if (expA == 0x7FFF || expB == 0x7FFF) {
            val nan = tryPropagateNaNExtF80(a, b)
            if (nan != null) {
                return nan
            }

            var high = a.high
            if (expB == 0x7FFF) {
                high = b.high xor (negateB.uint shl 15).ushort
                if (expA == 0x7FFF && high != a.high) {
                    return invalidF80()
                }
            }
            return Float80(high, 0x8000_0000_0000_0000uL, this)
        }

        var sign = a.sign
        val signB = b.sign xor negateB
        negateB = sign != signB

        if (expA < expB) {
            sign = signB
            expA = expB.also { expB = expA }
            a = b.also { b = a }
        }

        if (expB == 0) {
            expB = 1
            if (expA == 0) {
                expA = 1
            }
        }

        var sig = a.low
        var sigB = b.low

        var sigExtra: UInt
        var roundPackRoutinePtr = ::roundPackF80
        val extSigX = arrayOf(0u, 0u, 0u)

        val expDiff = expA - expB
        if (expDiff != 0) {
            extSigX[1] = sigB.uint
            extSigX[2] = (sigB ushr 32).uint

            shiftRightJamM(3, extSigX, expDiff, extSigX)
            sigB = (extSigX[2].ulong_z shl 32) or extSigX[1].ulong_z
            if (negateB) {
                sig -= sigB
                sigExtra = extSigX[0]
                if (sigExtra != 0u) {
                    --sig
                    sigExtra = -sigExtra
                }
                if (sig and 0x8000_0000_0000_0000uL == 0uL) {
                    if (sig and 0x4000_0000_0000_0000uL != 0uL) {
                        --expA
                        sig = (sig shl 1) or (sigExtra ushr 31).ulong_z
                        sigExtra = sigExtra shl 1
                    } else {
                        roundPackRoutinePtr = ::normRoundPack80
                    }
                }
            } else {
                sig += sigB
                if (sig and 0x8000_0000_0000_0000uL != 0uL) {
                    extSigX[2] = (sig ushr 32).uint
                    extSigX[1] = sig.uint
                    return roundPackRoutinePtr(sign, expA, extSigX)
                }

                sigExtra = (sig shl 31).uint or (extSigX[0] != 0u).uint
                ++expA
                sig = 0x8000_0000_0000_0000uL or (sig ushr 1)
                extSigX[2] = (sig ushr 32).uint
                extSigX[1] = sig.uint
                extSigX[0] = sigExtra
                return roundPackRoutinePtr(sign, expA, extSigX)
            }
        } else {
            sigExtra = 0u
            if (negateB) {
                if (sig < sigB) {
                    sign = !sign
                    sig = sigB - sig
                } else {
                    sig -= sigB
                    if (sig == 0uL) {
                        sign = roundingMode == RoundingMode.RoundMin
                        return packF80(sign, 0u, 0u)
                    }
                }
                roundPackRoutinePtr = ::normRoundPack80
            } else {
                sig += sigB
                if (sig < sigB) {
                    sigExtra = (sig shl 31).uint
                    ++expA
                    sig = 0x8000_0000_0000_0000uL or (sig ushr 1)
                } else if (sig and 0x8000_0000_0000_0000uL == 0uL) {
                    roundPackRoutinePtr = ::normRoundPack80
                }
            }
        }

        extSigX[2] = (sig ushr 32).uint
        extSigX[1] = sig.uint
        extSigX[0] = sigExtra
        return roundPackRoutinePtr(sign, expA, extSigX)
    }

    fun mulF80M(a: Float80, b: Float80): Float80 {
        var expA = a.exp.int_z
        var expB = b.exp.int_z
        val sign = a.sign xor b.sign

        if (expA == 0x7FFF || expB == 0x7FFF) {
            val nan = tryPropagateNaNExtF80(a, b)
            if (nan != null) {
                return nan
            }
            return packF80(sign, FLOAT80_INFINITY_HIGH, FLOAT80_INFINITY_LOW)
        }

        if (expA == 0) {
            expA = 1
        }

        var sigA = a.low
        if (sigA and 0x8000_0000_0000_0000uL == 0uL) {
            if (sigA == 0uL) {
                return packF80(sign, 0u, 0u)
            }
            sigA.normExtF80SigM().let {
                sigA = it.first
                expA += it.second
            }
        }

        if (expB == 0) {
            expB = 1
        }

        var sigB = b.low
        if (sigB and 0x8000_0000_0000_0000uL == 0uL) {
            if (sigB == 0uL) {
                return packF80(sign, 0u, 0u)
            }

            sigB.normExtF80SigM().let {
                sigB = it.first
                expB += it.second
            }
        }

        var exp = expA + expB - 0x3FFE
        val sigProd = mul64To128M(sigA, sigB)
        if (sigProd[0] != 0u) {
            sigProd[1] = sigProd[1] or 1u
        }
        val extSig = sigProd.copyOfRange(1, 4)
        if (sigProd[3] < 0x8000_0000u) {
            --exp
            addM(3, extSig, extSig, extSig)
        }

        return roundPackF80(sign, exp, extSig)
    }

    fun divF80M(a: Float80, b: Float80): Float80 {
        var expA = a.exp.int_z
        var expB = b.exp.int_z
        val sign = a.sign xor b.sign

        if (expA == 0x7FFF || expB == 0x7FFF) {
            val nan = tryPropagateNaNExtF80(a, b)
            if (nan != null) {
                return nan
            }

            if (expA == 0x7FFF) {
                if (expB == 0x7FFF) {
                    return invalidF80()
                }
                return packF80(sign, FLOAT80_INFINITY_HIGH, FLOAT80_INFINITY_LOW)
            }
            return packF80(sign, 0u, 0u)
        }

        var sigA = a.low
        var x64 = b.low
        if (expB == 0) {
            expB = 1
        }
        if (x64 and 0x8000_0000_0000_0000uL == 0uL) {
            if (x64 == 0uL) {
                if (sigA == 0uL) {
                    return invalidF80()
                }
                Flag.Infinite.raise()
                return packF80(sign, FLOAT80_INFINITY_HIGH, FLOAT80_INFINITY_LOW)
            }
            x64.normExtF80SigM().let {
                x64 = it.first
                expB += it.second
            }
        }

        if (expA == 0) {
            expA = 1
        }
        if (sigA and 0x8000_0000_0000_0000uL == 0uL) {
            if (sigA == 0uL) {
                return packF80(sign, 0u, 0u)
            }
            sigA.normExtF80SigM().let {
                sigA = it.first
                expA = it.second
            }
        }

        var exp = expA - expB + 0x3FFF
        var shiftDist = 29
        if (sigA < x64) {
            --exp
            shiftDist = 30
        }

        val y = shortShiftLeft64To96M(sigA, shiftDist)
        val recip32 = approxRecip32_1((x64 ushr 32).uint)

        val first = (x64 shl 30).uint
        x64 = x64 ushr 2

        val sigB = arrayOf(
            first,
            x64.uint,
            (x64 ushr 32).uint,
        )

        var ix = 2
        val qs = Array(2) { 0u }
        var q: UInt

        while (true) {
            x64 = y[2].ulong_z * recip32.ulong_z
            q = ((x64 + 0x8000_0000u) ushr 32).uint
            --ix
            if (ix < 0) {
                break
            }
            remStepMBy32(3, y, 29, sigB, q, y)
            if (y[2] and 0x8000_0000u != 0u) {
                --q
                addM(3, y, sigB, y)
            }
            qs[ix] = q
        }

        if ((q + 1uL) and 0x003F_FFFFuL < 2uL) {
            remStepMBy32(3, y, 29, sigB, q, y)
            if (y[2] and 0x80000000u != 0u) {
                --q
                addM(3, y, sigB, y)
            } else if (compare96M(sigB, y) <= 0) {
                ++q
                subM(3, y, sigB, y)
            }
            if (y[0] != 0u || y[1] != 0u || y[2] != 0u) {
                q = q or 1u
            }
        }

        x64 = q.ulong_z shl 9
        y[0] = x64.uint
        x64 = (qs[0].ulong_z shl 6) + (x64 ushr 32)
        y[1] = x64.uint
        y[2] = (qs[1] shl 3) + (x64 ushr 32).uint

        return roundPackF80(sign, exp, y)
    }

    fun sqrtF80M(a: Float80): Float80 {
        var expA = a.exp.int_z
        val sign = a.sign
        var rem64 = a.low

        if (expA == FLOAT80_INFINITY_HIGH.int_z) {
            if (rem64 and 0x7FFF_FFFF_FFFF_FFFFuL != 0uL) {
                return propagateNaNF80(a, null)
            }
            if (sign) {
                return invalidF80()
            }
            return packF80(false, expA.ushort, 0x8000_0000_0000_0000uL)
        }

        if (expA == 0) {
            expA = 1
        }
        if (rem64 and 0x8000_0000_0000_0000uL == 0uL) {
            if (rem64 == 0uL) {
                return packF80(sign, 0u, 0u)
            }
            rem64.normExtF80SigM().let {
                rem64 = it.first
                expA += it.second
            }
        }
        if (sign) {
            return invalidF80()
        }

        val exp = ((expA - 0x3FFF) ushr 1) + 0x3FFF
        expA = expA and 1
        val rem96 = shortShiftLeft64To96M(rem64, 30 - expA)
        val sig32A = (rem64 ushr 32).uint
        val recipSqrt32 = approxRecipSqrt32_1(expA.uint, sig32A)
        var sig32Z = ((sig32A.ulong_z * recipSqrt32) ushr 32).uint

        if (expA != 0) {
            sig32Z = sig32Z ushr 1
        }
        rem64 = ((rem96[2].ulong_z shl 32) or (rem96[1].ulong_z)) - sig32Z.ulong_z * sig32Z.ulong_z
        rem96[2] = (rem64 ushr 32).uint
        rem96[1] = rem64.uint

        var q = (((rem64 ushr 2).uint * recipSqrt32.ulong_z) ushr 32).uint
        var sig64Z = (sig32Z.ulong_z shl 32) + (q.ulong_z shl 3)

        val term = Array(4) { 0u }
        val remTmp = Array(3) { 0u }
        var rem32: UInt
        while (true) {
            val x64 = (sig32Z.ulong_z shl 32) + sig64Z
            term[1] = (x64 ushr 32).uint
            term[0] = x64.uint

            remStepMBy32(3, rem96, 29, term, q, remTmp)
            rem32 = remTmp[2]
            if (rem32 and 0x8000_0000u == 0u) {
                break
            }
            --q
            sig64Z -= 1u shl 3
        }

        rem64 = (rem32.ulong_z shl 32) or remTmp[1].ulong_z

        q = ((((rem64 ushr 2).uint * recipSqrt32.ulong_z) ushr 32) + 2u).uint
        if (rem64 ushr 34 != 0uL) {
            q += recipSqrt32
        }

        var x64 = q.ulong_z shl 7
        val extSigZ = Array(3) { 0u }
        extSigZ[0] = x64.uint
        x64 = (sig64Z shl 1) + (x64 ushr 32)
        extSigZ[2] = (x64 ushr 32).uint
        extSigZ[1] = x64.uint

        if (q and 0x00FF_FFFFu <= 2u) {
            q = q and 0xFFFFu.inv()
            extSigZ[0] = q shl 7
            x64 = sig64Z + (q ushr 27)
            term[3] = 0u
            term[2] = (x64 ushr 32).uint
            term[1] = x64.uint
            term[0] = q shl 5
            val rem = arrayOf(0u) + remTmp
            remStepMBy32(4, rem, 28, term, q, rem)
            q = rem[3]
            if (q and 0x8000_0000u != 0u) {
                sub1XM(3, extSigZ)
            } else {
                if (q != 0u || rem[1] != 0u || rem[2] != 0u) {
                    extSigZ[0] = extSigZ[0] or 1u
                }
            }
        }
        return roundPackF80(false, exp, extSigZ)
    }

    fun eqF80(a: Float80, b: Float80) = if (a.isNaN || b.isNaN) {
        if (a.isSignalingNaN || b.isSignalingNaN) {
            Flag.Invalid.raise()
        }
        false
    } else if (a.low == b.low) {
        a.high == b.high || a.low == 0uL
    } else {
        if (a.low and b.low and 0x8000_0000_0000_0000uL == 0uL) {
            compareNonnormExtF80M(a, b) == 0
        } else {
            false
        }
    }

    fun leF80(a: Float80, b: Float80) = if (a.isNaN || b.isNaN) {
        if (a.isSignalingNaN || b.isSignalingNaN) {
            Flag.Invalid.raise()
        }
        false
    } else {
        val signA = a.sign
        val lowOr = a.low or b.low

        if ((a.high xor b.high).uint_z and 0x8000u != 0u) {
            // Signs are different
            signA || lowOr == 0uL
        } else {
            // Signs are the same
            if (lowOr and 0x8000_0000_0000_0000uL == 0uL) {
                compareNonnormExtF80M(a, b) <= 0
            } else {
                if (a.high == b.high) {
                    if (a.low == b.low) {
                        true
                    } else {
                        (a.low < b.low) xor signA
                    }
                } else {
                    (a.high < b.high) xor signA
                }
            }
        }
    }

    /** X86-specific fscale */
    fun fscale(st0: Float80, st1: Float80) = if (st0.isInvalid || st1.isInvalid) {
        invalidF80()
    } else if (st1.isNaN) {
        if (st0.isSignalingNaN) {
            Flag.Invalid.raise()
        }

        if (st1.isSignalingNaN) {
            Flag.Invalid.raise()
            Float80(
                st1.high,
                st1.low or 0xC000_0000_0000_0000uL,
                st1.sf,
            )
        } else {
            st1
        }
    } else if (st1.isInfinity && !st0.isInvalid && !st0.isNaN) {
        if (st1.sign) {
            if (st0.isInfinity) {
                invalidF80()
            } else {
                packF80(st0.sign, 0u, 0u)
            }
        } else {
            if (st0.isZero) {
                invalidF80()
            } else {
                packF80(st0.sign, FLOAT80_INFINITY_HIGH, FLOAT80_INFINITY_LOW)
            }
        }
    } else {
        if (st0.isNaN) {
            Float80(DEFAULT_NAN_F80_HIGH, DEFAULT_NAN_F80_LOW, this)
        } else if (st0.isZero || st0.isInfinity) {
            st0
        } else {
            var exp = st0.exp.int_z
            var sig = st0.low

            if (exp == 0) {
                exp = 1
            }
            if (sig and 0x8000_0000_0000_0000uL == 0uL) {
                sig.normExtF80SigM().let {
                    sig = it.first
                    exp = it.second
                }
            }

            val n = f80ToInt(st1, true, RoundingMode.RoundMinMag)
            exp += min(max(n, -0x1_0000), 0x1_0000)
            packF80(st0.sign, exp.ushort, sig)
        }
    }
}
