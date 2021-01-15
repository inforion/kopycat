/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
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


inline class LongLong(val data: Long) : Comparable<LongLong>  {


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
    inline operator fun compareTo(other: Byte): Int = toLong().compareTo(other.toLong())

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: Short): Int = toLong().compareTo(other)

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: Int): Int = toLong().compareTo(other)
    
    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: Long): Int = toLong().compareTo(other)
    
    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @Suppress("OVERRIDE_BY_INLINE")
    override inline operator fun compareTo(other: LongLong): Int = toLong().compareTo(other.toLong())

    /** Adds the other value to this value. */
    inline operator fun plus(other: Byte): LongLong = LongLong(toLong().plus(other))
    /** Adds the other value to this value. */
    inline operator fun plus(other: Short): LongLong = LongLong(toLong().plus(other))
    /** Adds the other value to this value. */
    inline operator fun plus(other: Int): LongLong = LongLong(toLong().plus(other))
    /** Adds the other value to this value. */
    inline operator fun plus(other: Long): LongLong = LongLong(toLong().plus(other))
    /** Adds the other value to this value. */
    inline operator fun plus(other: LongLong): LongLong = LongLong(toLong().plus(other.toLong()))

    /** Subtracts the other value from this value. */
    inline operator fun minus(other: Byte): LongLong = LongLong(toLong().minus(other))
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: Short): LongLong = LongLong(toLong().minus(other))
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: Int): LongLong = LongLong(toLong().minus(other))
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: Long): LongLong = LongLong(toLong().minus(other))
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: LongLong): LongLong = LongLong(toLong().minus(other.toLong()))

    /** Multiplies this value by the other value. */
    inline operator fun times(other: Byte): LongLong = LongLong(toLong().times(other))
    /** Multiplies this value by the other value. */
    inline operator fun times(other: Short): LongLong = LongLong(toLong().times(other))
    /** Multiplies this value by the other value. */
    inline operator fun times(other: Int): LongLong = LongLong(toLong().times(other))
    /** Multiplies this value by the other value. */
    inline operator fun times(other: Long): LongLong = LongLong(toLong().times(other))
    /** Multiplies this value by the other value. */
    inline operator fun times(other: LongLong): LongLong = LongLong(toLong().times(other.toLong()))

    /** Divides this value by the other value. */
    inline operator fun div(other: Byte): LongLong = LongLong(toLong().div(other))
    /** Divides this value by the other value. */
    inline operator fun div(other: Short): LongLong = LongLong(toLong().div(other))
    /** Divides this value by the other value. */
    inline operator fun div(other: Int): LongLong = LongLong(toLong().div(other))
    /** Divides this value by the other value. */
    inline operator fun div(other: Long): LongLong = LongLong(toLong().div(other))
    /** Divides this value by the other value. */
    inline operator fun div(other: LongLong): LongLong = LongLong(toLong().div(other.toLong()))

    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: Byte): LongLong = LongLong(toLong().rem(other))
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: Short): LongLong = LongLong(toLong().rem(other))
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: Int): LongLong = LongLong(toLong().rem(other))
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: Long): LongLong = LongLong(toLong().rem(other))
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: LongLong): LongLong = LongLong(toLong().rem(other.toLong()))

    /** Increments this value. */
    inline operator fun inc(): LongLong = LongLong(data.inc())
    /** Decrements this value. */
    inline operator fun dec(): LongLong = LongLong(data.dec())

    /** Creates a range from this value to the specified [other] value. */
    inline operator fun rangeTo(other: LongLong): LongRange = LongRange(toLong(), other.toLong())

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
    inline fun toByte(): Byte = data.toByte()
    /**
     * Converts this [LongLong] value to [Int].
     *
     * The resulting `Int` value represents the same numerical value as this `LongLong`.
     *
     * The least significant 16 bits of the resulting `Int` value are the same as the bits of this `LongLong` value,
     * whereas the most significant 16 bits are filled with zeros.
     */
    inline fun toInt(): Int = data.toInt()
    /**
     * Converts this [LongLong] value to [Long].
     *
     * The resulting `Long` value represents the same numerical value as this `LongLong`.
     *
     * The least significant 16 bits of the resulting `Long` value are the same as the bits of this `LongLong` value,
     * whereas the most significant 48 bits are filled with zeros.
     */
    inline fun toLong(): Long = data

    /**
     * Converts this [LongLong] value to [Float].
     *
     * The resulting `Float` value represents the same numerical value as this `LongLong`.
     */
    inline fun toFloat(): Float = data.toFloat()
    /**
     * Converts this [LongLong] value to [Double].
     *
     * The resulting `Double` value represents the same numerical value as this `LongLong`.
     */
    inline fun toDouble(): Double = data.toDouble()
    /** Returns this value. */
    inline fun toLongLong(): LongLong = this

    override fun toString(): String = data.toString()
}
