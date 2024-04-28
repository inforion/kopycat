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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.veos.api.datatypes

import ru.inforion.lab403.common.extensions.*


@JvmInline
value class LongLong constructor(val data: ULong) : Comparable<LongLong>  {


    companion object {
        /**
         * A constant holding the minimum value an instance of LongLong can have.
         */
        const val MIN_VALUE: Long = -9223372036854775807L - 1L

        /**
         * A constant holding the maximum value an instance of LongLong can have.
         */
        const val MAX_VALUE: Long = 9223372036854775807L

        /**
         * The number of bytes used to represent an instance of LongLong in a binary form.
         */
        const val SIZE_BYTES: Int = 8

        /**
         * The number of bits used to represent an instance of LongLong in a binary form.
         */
        const val SIZE_BITS: Int = 64
    }

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: Byte): Int = long.compareTo(other.long_z)

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: Short): Int = long.compareTo(other)

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: Int): Int = long.compareTo(other)
    
    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: Long): Int = long.compareTo(other)
    
    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @Suppress("OVERRIDE_BY_INLINE")
    override inline operator fun compareTo(other: LongLong): Int = long.compareTo(other.long)

    /** Adds the other value to this value. */
    inline operator fun plus(other: Byte): LongLong = LongLong(ulong.plus(other))
    /** Adds the other value to this value. */
    inline operator fun plus(other: Short): LongLong = LongLong(ulong.plus(other))
    /** Adds the other value to this value. */
    inline operator fun plus(other: Int): LongLong = LongLong(ulong.plus(other))
    /** Adds the other value to this value. */
    inline operator fun plus(other: Long): LongLong = LongLong(ulong.plus(other))
    /** Adds the other value to this value. */
    inline operator fun plus(other: LongLong): LongLong = LongLong(ulong.plus(other.ulong))

    /** Subtracts the other value from this value. */
    inline operator fun minus(other: Byte): LongLong = LongLong(ulong.minus(other))
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: Short): LongLong = LongLong(ulong.minus(other))
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: Int): LongLong = LongLong(ulong.minus(other))
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: Long): LongLong = LongLong(ulong.minus(other))
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: LongLong): LongLong = LongLong(ulong.minus(other.ulong))

    /** Multiplies this value by the other value. */
    inline operator fun times(other: Byte): LongLong = LongLong(ulong.times(other))
    /** Multiplies this value by the other value. */
    inline operator fun times(other: Short): LongLong = LongLong(ulong.times(other))
    /** Multiplies this value by the other value. */
    inline operator fun times(other: Int): LongLong = LongLong(ulong.times(other))
    /** Multiplies this value by the other value. */
    inline operator fun times(other: Long): LongLong = LongLong(ulong.times(other))
    /** Multiplies this value by the other value. */
    inline operator fun times(other: LongLong): LongLong = LongLong(ulong.times(other.ulong))

    /** Divides this value by the other value. */
    inline operator fun div(other: Byte): LongLong = LongLong(ulong.div(other))
    /** Divides this value by the other value. */
    inline operator fun div(other: Short): LongLong = LongLong(ulong.div(other))
    /** Divides this value by the other value. */
    inline operator fun div(other: Int): LongLong = LongLong(ulong.div(other))
    /** Divides this value by the other value. */
    inline operator fun div(other: Long): LongLong = LongLong(ulong.div(other))
    /** Divides this value by the other value. */
    inline operator fun div(other: LongLong): LongLong = LongLong(ulong.div(other.ulong))

    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: Byte): LongLong = LongLong(ulong.rem(other))
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: Short): LongLong = LongLong(ulong.rem(other))
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: Int): LongLong = LongLong(ulong.rem(other))
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: Long): LongLong = LongLong(ulong.rem(other))
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: LongLong): LongLong = LongLong(ulong.rem(other.ulong))

    /** Increments this value. */
    inline operator fun inc(): LongLong = LongLong(data.inc())
    /** Decrements this value. */
    inline operator fun dec(): LongLong = LongLong(data.dec())

    /** Creates a range from this value to the specified [other] value. */
    inline operator fun rangeTo(other: LongLong): ULongRange = ULongRange(ulong, other.ulong)

    /** Performs a bitwise AND operation between the two values. */
    inline infix fun and(other: LongLong): LongLong = LongLong(data and other.data)
    /** Performs a bitwise OR operation between the two values. */
    inline infix fun or(other: LongLong): LongLong = LongLong(data or other.data)
    /** Performs a bitwise XOR operation between the two values. */
    inline infix fun xor(other: LongLong): LongLong = LongLong(data xor other.data)
    /** Inverts the bits in this value. */
    inline fun inv(): LongLong = LongLong(data.inv())

    /**
     * Converts this [LongLong] value to [Byte].
     *
     * If this value is less than or equals to [Byte.MAX_VALUE], the resulting `Byte` value represents
     * the same numerical value as this `LongLong`.
     *
     * The resulting `Byte` value is represented by the least significant 8 bits of this `LongLong` value.
     */
    inline val byte: Byte get() = data.byte
    /**
     * Converts this [LongLong] value to [Int].
     *
     * The resulting `Int` value represents the same numerical value as this `LongLong`.
     *
     * The least significant 16 bits of the resulting `Int` value are the same as the bits of this `LongLong` value,
     * whereas the most significant 16 bits are filled with zeros.
     */
    inline val int: Int get() = data.int
    /**
     * Converts this [LongLong] value to [Long].
     *
     * The resulting `Long` value represents the same numerical value as this `LongLong`.
     *
     * The least significant 16 bits of the resulting `Long` value are the same as the bits of this `LongLong` value,
     * whereas the most significant 48 bits are filled with zeros.
     */
    inline val long: Long get() = data.long
    /**
     * Converts this [LongLong] value to [ULong].
     *
     * The resulting `Long` value represents the same numerical value as this `LongLong`.
     *
     * The least significant 16 bits of the resulting `Long` value are the same as the bits of this `LongLong` value,
     * whereas the most significant 48 bits are filled with zeros.
     */
    inline val ulong: ULong get() = data

    /**
     * Converts this [LongLong] value to [Float].
     *
     * The resulting `Float` value represents the same numerical value as this `LongLong`.
     */
    inline val float: Float get() = data.float
    /**
     * Converts this [LongLong] value to [Double].
     *
     * The resulting `Double` value represents the same numerical value as this `LongLong`.
     */
    inline val double: Double get() = data.double
    /** Returns this value. */
    inline val longlong: LongLong get() = this

    override fun toString(): String = data.toString()
}
