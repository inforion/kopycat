/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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

package ru.inforion.lab403.common.proposal

import ru.inforion.lab403.common.extensions.*
import java.math.BigInteger

inline fun inv(data: BigInteger) = data.inv()

inline fun ubitMaskBigint(size: Int): BigInteger {
    return (BigInteger.ONE shl size) - BigInteger.ONE
}

inline fun ubitMaskBigint(range: IntRange) = if (range.last == 0) ubitMaskBigint(range.first + 1) else
    ubitMaskBigint(range.first + 1) and inv(ubitMaskBigint(range.last))



inline infix fun BigInteger.bzero(range: IntRange) = this and inv(ubitMaskBigint(range))

inline infix fun BigInteger.mask(size: Int) = this and ubitMaskBigint(size)
inline infix fun BigInteger.mask(range: IntRange) = this and ubitMaskBigint(range)

//inline infix fun BigInteger.like(dtyp: Datatype): ULong = this mask dtyp.bits

inline fun BigInteger.xbits(high: Int, low: Int) = (this ushr low) and ((BigInteger.ONE shl (high - low + 1)) - BigInteger.ONE)

inline infix fun BigInteger.xbit(index: Int) = (this ushr index) and BigInteger.ONE

inline operator fun BigInteger.get(range: IntRange) = xbits(range.first, range.last)

inline operator fun BigInteger.get(index: Int) = xbit(index)

inline fun insertBit(dst: BigInteger, value: BigInteger, index: Int): BigInteger {
    val ins = value shl index
    val mask = inv(BigInteger.ONE shl index)
    return dst and mask or ins
}

inline fun insertField(dst: BigInteger, src: BigInteger, range: IntRange) = (dst bzero range) or ((src shl range.last) mask range)

inline fun BigInteger.insert(value: BigInteger, index: Int): BigInteger = insertBit(this, value, index)
inline fun BigInteger.insert(value: ULong, index: Int): BigInteger = insertBit(this, value.bigint, index)
inline fun BigInteger.insert(value: UInt, index: Int): BigInteger = insert(value.bigint, index)
inline fun BigInteger.insert(value: Int, index: Int): BigInteger = insert(value.bigint, index)
inline fun BigInteger.insert(value: Boolean, index: Int): BigInteger = insert(value.int.bigint, index)

inline fun BigInteger.insert(data: BigInteger, range: IntRange): BigInteger = insertField(this, data, range)
inline fun BigInteger.insert(data: ULong, range: IntRange): BigInteger = insertField(this, data.bigint, range)


inline infix fun BigInteger.clr(range: IntRange) = this bzero range

inline infix fun BigInteger.clr(index: Int) = this and inv(BigInteger.ONE shl index)

inline infix fun BigInteger.set(range: IntRange) = this or ubitMaskBigint(range)

inline infix fun BigInteger.set(index: Int) = this or (BigInteger.ONE shl index)
