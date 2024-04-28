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
import ru.inforion.lab403.kopycat.cores.x86.hardware.registers.FWRBank
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.math.BigInteger
import java.nio.file.Paths
import kotlin.io.path.exists

class LongDouble private constructor(private var buffer: ByteArray, private val cwr: FWRBank.CWR) {
    companion object {
        @Transient val log = logger(INFO)

        init {
            val paths = listOf(

                "libs",
            ).map {
                Paths.get(it / System.mapLibraryName("longdouble"))
            }.filter { it.exists() }

            try {
                paths.forEach {
                    System.load(it.toAbsolutePath().toString())
                    log.info { "LibLongDouble has been loaded from path: $it" }
                }
            } catch (_: UnsatisfiedLinkError) {
                log.severe { "Unable to load LibLongDouble by path" }
                // java.library.path for instruction tests is set in build.gradle
                System.loadLibrary("longdouble")
            }
        }

        fun zero(cwr: FWRBank.CWR) = LongDouble(loadZero(cwr.pc.cw, cwr.rc.cw), cwr)
        fun log2_e(cwr: FWRBank.CWR) = LongDouble(loadLog2e(cwr.pc.cw, cwr.rc.cw), cwr)
        fun log10_2(cwr: FWRBank.CWR) = LongDouble(loadLog102(cwr.pc.cw, cwr.rc.cw), cwr)
        fun log2_10(cwr: FWRBank.CWR) = LongDouble(loadLog210(cwr.pc.cw, cwr.rc.cw), cwr)
        fun loge_2(cwr: FWRBank.CWR) = LongDouble(loadLogE2(cwr.pc.cw, cwr.rc.cw), cwr)
        fun one(cwr: FWRBank.CWR) = LongDouble(loadOne(cwr.pc.cw, cwr.rc.cw), cwr)
        fun pi(cwr: FWRBank.CWR) = LongDouble(loadPi(cwr.pc.cw, cwr.rc.cw), cwr)

        private fun ByteArray.pad(to: Int) = if (this.size < to) {
            val padded = ByteArray(to)
            copyInto(padded, to - size)
            padded
        } else this

        @JvmStatic private external fun loadZero(pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun loadLog2e(pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun loadLog102(pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun loadLog210(pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun loadLogE2(pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun loadOne(pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun loadPi(pc: Int, rc: Int): ByteArray

        @JvmStatic private external fun toShort(a: ByteArray, pc: Int, rc: Int): Short
        @JvmStatic private external fun toInt(a: ByteArray, pc: Int, rc: Int): Int
        @JvmStatic private external fun toLong(a: ByteArray, pc: Int, rc: Int): Long
        @JvmStatic private external fun toFloat(a: ByteArray, pc: Int, rc: Int): Float
        @JvmStatic private external fun toDouble(a: ByteArray, pc: Int, rc: Int): Double

        @JvmStatic private external fun fromShort(a: Short, pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun fromInt(a: Int, pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun fromLong(a: Long, pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun fromFloat(a: Float, pc: Int, rc: Int):  ByteArray
        @JvmStatic private external fun fromDouble(a: Double, pc: Int, rc: Int): ByteArray

        @JvmStatic private external fun add(a: ByteArray, b: ByteArray, pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun sub(a: ByteArray, b: ByteArray, pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun mul(a: ByteArray, b: ByteArray, pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun div(a: ByteArray, b: ByteArray, pc: Int, rc: Int): ByteArray

        @JvmStatic private external fun neg(a: ByteArray, pc: Int, rc: Int): ByteArray

        @JvmStatic private external fun gt(a: ByteArray, b: ByteArray, pc: Int, rc: Int): Boolean
        @JvmStatic private external fun lt(a: ByteArray, b: ByteArray, pc: Int, rc: Int): Boolean
        @JvmStatic private external fun ge(a: ByteArray, b: ByteArray, pc: Int, rc: Int): Boolean
        @JvmStatic private external fun le(a: ByteArray, b: ByteArray, pc: Int, rc: Int): Boolean

        @JvmStatic private external fun roundToNearestInt(a: ByteArray, pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun scale(a: ByteArray, b: ByteArray, pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun abs(a: ByteArray, pc: Int, rc: Int): ByteArray
        @JvmStatic private external fun sqrt(a: ByteArray, pc: Int, rc: Int): ByteArray

        @JvmStatic private external fun isZero(a: ByteArray, pc: Int, rc: Int): Boolean
    }

    constructor(i: BigInteger, cwr: FWRBank.CWR) :
            this((i mask 80..0).toByteArray().pad(10).reversedArray(), cwr)
    constructor(a: Float, cwr: FWRBank.CWR) : this(fromFloat(a, cwr.pc.cw, cwr.rc.cw), cwr)
    constructor(a: Double, cwr: FWRBank.CWR) : this(fromDouble(a, cwr.pc.cw, cwr.rc.cw), cwr)
    constructor(a: Short, cwr: FWRBank.CWR) : this(fromShort(a, cwr.pc.cw, cwr.rc.cw), cwr)
    constructor(a: Int, cwr: FWRBank.CWR) : this(fromInt(a, cwr.pc.cw, cwr.rc.cw), cwr)
    constructor(a: Long, cwr: FWRBank.CWR) : this(fromLong(a, cwr.pc.cw, cwr.rc.cw), cwr)

    operator fun plus(other: LongDouble) = LongDouble(add(buffer, other.buffer, cwr.pc.cw, cwr.rc.cw), cwr)
    operator fun minus(other: LongDouble) = LongDouble(sub(buffer, other.buffer, cwr.pc.cw, cwr.rc.cw), cwr)
    operator fun times(other: LongDouble) = LongDouble(mul(buffer, other.buffer, cwr.pc.cw, cwr.rc.cw), cwr)
    operator fun div(other: LongDouble) = LongDouble(div(buffer, other.buffer, cwr.pc.cw, cwr.rc.cw), cwr)
    operator fun unaryMinus() = LongDouble(neg(buffer, cwr.pc.cw, cwr.rc.cw), cwr)

    operator fun compareTo(other: LongDouble): Int {
        if (gt(buffer, other.buffer, cwr.pc.cw, cwr.rc.cw)) return 1
        if (lt(buffer, other.buffer, cwr.pc.cw, cwr.rc.cw)) return -1
        return 0
    }

    fun ieee754AsUnsigned() = BigInteger(1, buffer.reversedArray())
    val byteBuffer get() = buffer.reversedArray()
    val byteBufferLe get() = buffer
    fun isZero() = isZero(buffer, cwr.pc.cw, cwr.rc.cw)
    val short get() = toShort(buffer, cwr.pc.cw, cwr.rc.cw)
    val int get() = toInt(buffer, cwr.pc.cw, cwr.rc.cw)
    val long get() = toLong(buffer, cwr.pc.cw, cwr.rc.cw)
    val float get() = toFloat(buffer, cwr.pc.cw, cwr.rc.cw)
    val double get() = toDouble(buffer, cwr.pc.cw, cwr.rc.cw)

    /** Rounds to nearest integer according to [FWRBank.CWR.rc] */
    fun roundToNearestInt() = LongDouble(roundToNearestInt(buffer, cwr.pc.cw, cwr.rc.cw), cwr)

    /** Used by fscale */
    fun scale(st1: LongDouble) = LongDouble(scale(buffer, st1.buffer, cwr.pc.cw, cwr.rc.cw), cwr)

    /** Returns absolute value */
    fun abs() = LongDouble(abs(buffer, cwr.pc.cw, cwr.rc.cw), cwr)

    /** Square root */
    fun sqrt() = LongDouble(sqrt(buffer, cwr.pc.cw, cwr.rc.cw), cwr)

    override fun toString() = double.toString()
    override fun equals(other: Any?) = other is LongDouble && compareTo(other) == 0
    override fun hashCode(): Int = buffer.hashCode()
}

// Float to long double. -1.0 -> -1.0
fun Float.longDouble(cwr: FWRBank.CWR) = LongDouble(this, cwr)
fun Double.longDouble(cwr: FWRBank.CWR) = LongDouble(this, cwr)

// Signed to long double. -1 -> -1.0
fun Short.longDouble(cwr: FWRBank.CWR) = LongDouble(this, cwr)
fun Int.longDouble(cwr: FWRBank.CWR) = LongDouble(this, cwr)
fun Long.longDouble(cwr: FWRBank.CWR) = LongDouble(this, cwr)

/** Converts **buffer** to long double */
fun BigInteger.longDouble(cwr: FWRBank.CWR) = LongDouble(this, cwr)

/** Depending on the size of the operand, converts either float or double to long double */
fun AOperand<x86Core>.longDouble(x86: x86Core, cwr: FWRBank.CWR) = when (this.dtyp) {
    Datatype.DWORD -> value(x86).uint.ieee754().longDouble(cwr)
    Datatype.QWORD -> value(x86).ieee754().longDouble(cwr)
    else -> throw RuntimeException("Wrong operand size")
}
