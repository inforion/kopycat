@file:Suppress("NOTHING_TO_INLINE", "EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package ru.inforion.lab403.common.proposal

import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.extensions.toULong

/**
 * Signed value to unsigned conversion
 */
val Long.ulong get() = toULong()
val Int.ulong get() = toULong()
val Short.ulong get() = toULong()
val Byte.ulong get() = toULong()

val Long.uint get() = toUInt()
val Int.uint get() = toUInt()
val Short.uint get() = toUInt()
val Byte.uint get() = toUInt()

val Long.ushort get() = toUShort()
val Int.ushort get() = toUShort()
val Short.ushort get() = toUShort()
val Byte.ushort get() = toUShort()

val Long.ubyte get() = toUByte()
val Int.ubyte get() = toUByte()
val Short.ubyte get() = toUByte()
val Byte.ubyte get() = toUByte()

/**
 * Unsigned value to signed conversion
 */
val ULong.long get() = toLong()
val ULong.int get() = toInt()
val ULong.short get() = toShort()
val ULong.byte get() = toByte()

val UInt.long get() = toLong()
val UInt.int get() = toInt()
val UInt.short get() = toShort()
val UInt.byte get() = toByte()

val UShort.long get() = toLong()
val UShort.int get() = toInt()
val UShort.short get() = toShort()
val UShort.byte get() = toByte()

val UByte.long get() = toLong()
val UByte.int get() = toInt()
val UByte.short get() = toShort()
val UByte.byte get() = toByte()

/**
 * Unsigned value to unsigned conversion
 */
val ULong.ulong get() = toULong()
val ULong.uint get() = toUInt()
val ULong.ushort get() = toUShort()
val ULong.ubyte get() = toUByte()

val UInt.ulong get() = toULong()
val UInt.uint get() = toUInt()
val UInt.ushort get() = toUShort()
val UInt.ubyte get() = toUByte()

val UShort.ulong get() = toULong()
val UShort.uint get() = toUInt()
val UShort.ushort get() = toUShort()
val UShort.ubyte get() = toUByte()

val UByte.ulong get() = toULong()
val UByte.uint get() = toUInt()
val UByte.ushort get() = toUShort()
val UByte.ubyte get() = toUByte()

inline fun bitMask(size: Int): ULong {
    require(size in 1..64) { "Size must be in range 1..64" }
    return ULong.MAX_VALUE shr (64 - size)
}

inline fun bitMask(range: IntRange): ULong = if (range.last == 0) bitMask(range.first + 1) else
    bitMask(range.first + 1) and bitMask(range.last).inv()

/**
 * Calculate inverse value
 */
inline fun inv(data: ULong) = data.inv()
inline fun inv(data: UInt) = data.inv()
inline fun inv(data: UShort) = data.inv()
inline fun inv(data: UByte) = data.inv()

/**
 * Fill with zeros bit outside the specified range (from msb to 0)
 */
inline infix fun ULong.mask(size: Int) = this and bitMask(size).ulong
inline infix fun UInt.mask(size: Int) = this and bitMask(size).uint
inline infix fun UShort.mask(size: Int) = this and bitMask(size).ushort
inline infix fun UByte.mask(size: Int) = this and bitMask(size).ubyte

/**
 * Fill with zeros bit outside the specified range (from msb to lsb)
 */
inline infix fun ULong.mask(range: IntRange) = this and bitMask(range).ulong
inline infix fun UInt.mask(range: IntRange) = this and bitMask(range).uint
inline infix fun UShort.mask(range: IntRange) = this and bitMask(range).ushort
inline infix fun UByte.mask(range: IntRange) = this and bitMask(range).ubyte

/**
 * Fill with zero specified bit range (from msb to lsb)
 */
inline infix fun ULong.bzero(range: IntRange) = this and inv(bitMask(range)).ulong
inline infix fun UInt.bzero(range: IntRange) = this and inv(bitMask(range)).uint
inline infix fun UShort.bzero(range: IntRange) = this and inv(bitMask(range)).ushort
inline infix fun UByte.bzero(range: IntRange) = this and inv(bitMask(range)).ubyte

inline fun insertField(dst: ULong, src: ULong, range: IntRange) = (dst bzero range) or ((src shl range.last) mask range)
inline fun insertField(dst: UInt, src: UInt, range: IntRange) = (dst bzero range) or ((src shl range.last) mask range)

inline fun insertBit(dst: ULong, value: ULong, indx: Int): ULong {
    val ins = value shl indx
    val mask = (1UL shl indx).inv()
    return dst and mask or ins
}

inline fun insertBit(dst: UInt, value: UInt, indx: Int): UInt {
    val ins = value shl indx
    val mask = (1U shl indx).inv()
    return dst and mask or ins
}

inline fun ULong.insert(value: ULong, indx: Int) = insertBit(this, value, indx)
inline fun UInt.insert(value: UInt, indx: Int) = insertBit(this, value, indx)

inline fun ULong.insert(data: ULong, range: IntRange) = insertField(this, data, range)
inline fun UInt.insert(data: UInt, range: IntRange) = insertField(this, data, range)

inline fun insert(value: ULong, indx: Int) = 0UL.insert(value, indx)
inline fun insert(value: UInt, indx: Int) = 0U.insert(value, indx)

inline fun insert(data: ULong, range: IntRange) = 0UL.insert(data, range)
inline fun insert(data: UInt, range: IntRange) = 0U.insert(data, range)

val ULong.hex get() = toString(16)
val ULong.hex2 get() = "%02X".format(long)
val ULong.hex4 get() = "%04X".format(long)
val ULong.hex8 get() = "%08X".format(long)
val ULong.hex16 get() = "%016X".format(long)

val UInt.hex get() = toString(16)
val UInt.hex2 get() = "%02X".format(int)
val UInt.hex4 get() = "%04X".format(int)
val UInt.hex8 get() = "%08X".format(int)

val UShort.hex get() = toString(16)
val UShort.hex2 get() = "%02X".format(short)
val UShort.hex4 get() = "%04X".format(short)

val UByte.hex get() = toString(16)
val UByte.hex2 get() = "%02X".format(byte)
