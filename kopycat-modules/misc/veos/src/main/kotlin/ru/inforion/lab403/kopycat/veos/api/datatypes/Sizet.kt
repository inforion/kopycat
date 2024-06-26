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
value class Sizet constructor(val data: ULong) : Comparable<Sizet>  {

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: Byte): Int = ulong.compareTo(other.ulong_z)

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: Short): Int = ulong.compareTo(other.ulong_z)

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: Int): Int = ulong.compareTo(other.ulong_z)

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: Long): Int = ulong.compareTo(other.ulong)

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: UByte): Int = ulong.compareTo(other.ulong_z)

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: UShort): Int = ulong.compareTo(other.ulong_z)

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: UInt): Int = ulong.compareTo(other.ulong_z)

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    inline operator fun compareTo(other: ULong): Int = ulong.compareTo(other)

    /**
     * Compares this value with the specified value for order.
     * Returns zero if this value is equal to the specified other value, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @Suppress("OVERRIDE_BY_INLINE")
    override inline operator fun compareTo(other: Sizet): Int = ulong.compareTo(other.ulong)

    /** Adds the other value to this value. */
    inline operator fun plus(other: Byte): Sizet = plus(other.ulong_z)
    /** Adds the other value to this value. */
    inline operator fun plus(other: Short): Sizet = plus(other.ulong_z)
    /** Adds the other value to this value. */
    inline operator fun plus(other: Int): Sizet = plus(other.ulong_z)
    /** Adds the other value to this value. */
    inline operator fun plus(other: Long): Sizet = plus(other.ulong)
    /** Adds the other value to this value. */
    inline operator fun plus(other: UByte): Sizet = plus(other.ulong_z)
    /** Adds the other value to this value. */
    inline operator fun plus(other: UShort): Sizet = plus(other.ulong_z)
    /** Adds the other value to this value. */
    inline operator fun plus(other: UInt): Sizet = plus(other.ulong_z)
    /** Adds the other value to this value. */
    inline operator fun plus(other: ULong): Sizet = Sizet(ulong.plus(other))
    /** Adds the other value to this value. */
    inline operator fun plus(other: Sizet): Sizet = plus(other.ulong)

    /** Subtracts the other value from this value. */
    inline operator fun minus(other: Byte): Sizet = minus(other.ulong_z)
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: Short): Sizet = minus(other.ulong_z)
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: Int): Sizet = minus(other.ulong_z)
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: Long): Sizet = minus(other.ulong)
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: UByte): Sizet = minus(other.ulong_z)
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: UShort): Sizet = minus(other.ulong_z)
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: UInt): Sizet = minus(other.ulong_z)
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: ULong): Sizet = Sizet(ulong.minus(other))
    /** Subtracts the other value from this value. */
    inline operator fun minus(other: Sizet): Sizet = minus(other.ulong)

    /** Multiplies this value by the other value. */
    inline operator fun times(other: Byte): Sizet = times(other.ulong_z)
    /** Multiplies this value by the other value. */
    inline operator fun times(other: Short): Sizet = times(other.ulong_z)
    /** Multiplies this value by the other value. */
    inline operator fun times(other: Int): Sizet = times(other.ulong_z)
    /** Multiplies this value by the other value. */
    inline operator fun times(other: Long): Sizet = times(other.ulong)
    /** Multiplies this value by the other value. */
    inline operator fun times(other: UByte): Sizet = times(other.ulong_z)
    /** Multiplies this value by the other value. */
    inline operator fun times(other: UShort): Sizet = times(other.ulong_z)
    /** Multiplies this value by the other value. */
    inline operator fun times(other: UInt): Sizet = times(other.ulong_z)
    /** Multiplies this value by the other value. */
    inline operator fun times(other: ULong): Sizet = Sizet(ulong.times(other))
    /** Multiplies this value by the other value. */
    inline operator fun times(other: Sizet): Sizet = times(other.ulong)

    /** Divides this value by the other value. */
    inline operator fun div(other: Byte): Sizet = div(other.ulong_z)
    /** Divides this value by the other value. */
    inline operator fun div(other: Short): Sizet = div(other.ulong_z)
    /** Divides this value by the other value. */
    inline operator fun div(other: Int): Sizet = div(other.ulong_z)
    /** Divides this value by the other value. */
    inline operator fun div(other: Long): Sizet = div(other.ulong)
    /** Divides this value by the other value. */
    inline operator fun div(other: UByte): Sizet = div(other.ulong_z)
    /** Divides this value by the other value. */
    inline operator fun div(other: UShort): Sizet = div(other.ulong_z)
    /** Divides this value by the other value. */
    inline operator fun div(other: UInt): Sizet = div(other.ulong_z)
    /** Divides this value by the other value. */
    inline operator fun div(other: ULong): Sizet = Sizet(ulong.div(other))
    /** Divides this value by the other value. */
    inline operator fun div(other: Sizet): Sizet = div(other.ulong)

    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: Byte): Sizet = rem(other.ulong_z)
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: Short): Sizet = rem(other.ulong_z)
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: Int): Sizet = rem(other.ulong_z)
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: Long): Sizet = rem(other.ulong)
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: UByte): Sizet = rem(other.ulong_z)
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: UShort): Sizet = rem(other.ulong_z)
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: UInt): Sizet = rem(other.ulong_z)
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: ULong): Sizet = Sizet(ulong.rem(other))
    /** Calculates the remainder of dividing this value by the other value. */
    inline operator fun rem(other: Sizet): Sizet = rem(other.ulong)

    /** Increments this value. */
    inline operator fun inc(): Sizet = Sizet(data.inc())
    /** Decrements this value. */
    inline operator fun dec(): Sizet = Sizet(data.dec())

    /** Creates a range from this value to the specified [other] value. */
    inline operator fun rangeTo(other: Sizet): ULongRange = ULongRange(ulong, other.ulong)

    /** Performs a bitwise AND operation between the two values. */
    inline infix fun and(other: Sizet): Sizet = Sizet(data and other.data)
    /** Performs a bitwise OR operation between the two values. */
    inline infix fun or(other: Sizet): Sizet = Sizet(data or other.data)
    /** Performs a bitwise XOR operation between the two values. */
    inline infix fun xor(other: Sizet): Sizet = Sizet(data xor other.data)
    /** Inverts the bits in this value. */
    inline fun inv(): Sizet = Sizet(data.inv())

    /**
     * Converts this [Sizet] value to [Byte].
     */
    inline val byte: Byte get() = data.byte
    /**
     * Converts this [Sizet] value to [Short].
     */
    inline val short: Short get() = data.short
    /**
     * Converts this [Sizet] value to [Int].
     */
    inline val int: Int get() = data.int
    /**
     * Converts this [Sizet] value to [Long].
     */
    inline val long: Long get() = data.long
    /**
     * Converts this [Sizet] value to [UByte].
     */
    inline val ubyte: UByte get() = data.ubyte
    /**
     * Converts this [Sizet] value to [UShort].
     */
    inline val ushort: UShort get() = data.ushort
    /**
     * Converts this [Sizet] value to [UInt].
     */
    inline val uint: UInt get() = data.uint
    /**
     * Converts this [Sizet] value to [ULong].
     */
    inline val ulong: ULong get() = data

    /**
     * Converts this [Sizet] value to [Float].
     *
     * The resulting `Float` value represents the same numerical value as this `SizeT`.
     */
    inline val float: Float get() = data.float
    /**
     * Converts this [Sizet] value to [Double].
     *
     * The resulting `Double` value represents the same numerical value as this `SizeT`.
     */
    inline val double: Double get() = data.double

    /** Returns this value. */
    inline val size_t: Sizet get() = this

    override fun toString(): String = data.toString()
}