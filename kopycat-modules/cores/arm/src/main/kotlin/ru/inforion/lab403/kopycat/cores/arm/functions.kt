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

package ru.inforion.lab403.kopycat.cores.arm

import com.google.common.math.IntMath.pow
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unpredictable

enum class SRType {SRType_LSL, SRType_LSR, SRType_ASR, SRType_ROR, SRType_RRX}

inline fun UInt(x: Long, N: Int): Long = x mask N
inline fun SInt(x: Long, N: Int): Long = if (N != 64) signext(x, N).asLong else x

inline fun Align(x: Long, y: Int): Long = y * (x / y)

inline fun LSL_C(x: Long, N: Int, shift: Int): Pair<Long, Int> {
//    assert(shift in 1..63)
    val result = x shl shift
    val carry_out = result[N].asInt
    return Pair(result mask N, carry_out)
}

inline fun LSR_C(x: Long, N: Int, shift: Int): Pair<Long, Int> {
//    assert(shift in 1..63)
    val result = x ushr shift
    val carry_out = x[shift - 1].asInt
    return Pair(result mask N, carry_out)
}

inline fun ASR_C(x: Long, N: Int, shift: Int): Pair<Long, Int> {
//    assert(shift in 1..63)
    val result = ASR(x, N, shift)
    val carry_out = if (shift >= N) 1 else x[shift - 1].asInt
    return Pair(result, carry_out)
}

inline fun ASR(x: Long, N: Int, shift: Int): Long {
//    assert(shift in 0..63)
    return (x.asInt shr shift).asLong mask N
}

inline fun ROR_C(x: Long, N: Int, shift: Int): Pair<Long, Int> {
//    assert(shift != 0 && shift < 64)
    val result = ROR(x, N, shift)
    val carry_out = result[N - 1].asInt
    return Pair(result, carry_out)
}

inline fun ROR(x: Long, N: Int, shift: Int): Long {
//    assert(shift in 0..63)
    return (x to N) rotr shift
}

inline fun RRX_C(x: Long, N: Int, carry_in: Int): Pair<Long, Int> {
    val result = cat(carry_in.asULong, x[N-1..1], N-1)
    val carry_out = x[0].asInt
    return Pair(result, carry_out)
}

inline fun RRX(x: Long, N: Int, carry_in: Int): Long {
    val (result, _) = RRX_C(x, N, carry_in)
    return result
}

fun DecodeImmShift(type: Long, imm5: Long): Pair<SRType, Long> {
    return when (type) {
        0b00L -> Pair(SRType.SRType_LSL, imm5)
        0b01L -> Pair(SRType.SRType_LSR, if (imm5 == 0b00000L) 32 else imm5)
        0b10L -> Pair(SRType.SRType_ASR, if (imm5 == 0b00000L) 32 else imm5)
        0b11L -> if (imm5 == 0b00000L) Pair(SRType.SRType_RRX, 1L) else Pair(SRType.SRType_ROR, imm5)
        else -> throw IllegalArgumentException("Can't decode type = ${type.toString(2)}")
    }
}

fun DecodeRegShift(type: Long): SRType = when (type) {
    0b00L -> SRType.SRType_LSL
    0b01L -> SRType.SRType_LSR
    0b10L -> SRType.SRType_ASR
    0b11L -> SRType.SRType_ROR
    else -> throw IllegalArgumentException("Can't decode type = ${type.toString(2)}")
}

fun ARMExpandImm_C(imm12: Long, carry_in: Int): Pair<Long, Int> {
    val unrotated_value = imm12[7..0]
    val amount = 2 * UInt(imm12[11..8], 32).asInt
    val (imm32, carry_out) = Shift_C(unrotated_value, 32, SRType.SRType_ROR, amount, carry_in)
    return Pair(imm32, carry_out)
}

fun ARMExpandImm(imm12: Long): Long {
    val (imm32, _) = ARMExpandImm_C(imm12, 0)
    return imm32
}

fun ThumbExpandImm_C(imm12: Long, carry_in: Int): Pair<Long, Int> {
    val imm32: Long
    val carry_out: Int
    return if(imm12[11..10] == 0L) {
        carry_out = carry_in
        when(imm12[9..8]){
            0b00L -> imm32 = imm12[7..0]
            0b01L -> {
                if(imm12[7..0] == 0L) throw Unpredictable
                imm32 = (imm12[7..0] shl 16) + imm12[7..0]
            }
            0b10L -> {
                if(imm12[7..0] == 0L) throw Unpredictable
                imm32 = (imm12[7..0] shl 24) + (imm12[7..0] shl 8)
            }
            0b11L -> {
                if(imm12[7..0] == 0L) throw Unpredictable
                imm32 = (imm12[7..0] shl 24) + (imm12[7..0] shl 16) + (imm12[7..0] shl 8) + imm12[7..0]
            }
            else -> throw Unpredictable
        }
        Pair(imm32, carry_out)
    } else {
        val unrotated_value = 0b1000_0000 + imm12[6..0]
        ROR_C(unrotated_value, 32, imm12[11..7].asInt)
    }
}

fun ThumbExpandImm(imm12: Long): Long {
    val (imm32, _) = ThumbExpandImm_C(imm12, 0)
    return imm32
}

fun Shift_C(value: Long, N: Int, type: SRType, amount: Int, carry_in: Int): Pair<Long, Int> {
//    assert(!(type == SRType.SRType_RRX && amount != 1))
    return if (amount == 0)
        Pair(value, carry_in)
    else when (type) {
        SRType.SRType_LSL -> LSL_C(value, N, amount)
        SRType.SRType_LSR -> LSR_C(value, N, amount)
        SRType.SRType_ASR -> ASR_C(value, N, amount)
        SRType.SRType_ROR -> ROR_C(value, N, amount)
        SRType.SRType_RRX -> RRX_C(value, N, amount)
    }
}

fun Shift(value: Long, N: Int, type: SRType, amount: Int, carry_in: Int): Long {
    val (result, _) = Shift_C(value, N, type, amount, carry_in)
    return result
}

fun AddWithCarry(N: Int, x: Long, y: Long, carry_in: Int): Triple<Long, Int, Int> {
    val unsigned_sum = UInt(x, N) + UInt(y, N) + carry_in
    val signed_sum = SInt(x, N) + SInt(y, N) + carry_in
    val result = unsigned_sum[N-1..0]
    val carry_out = if (UInt(result, N) == unsigned_sum) 0 else 1
    val overflow = if (SInt(result, N) == signed_sum) 0 else 1
    return Triple(result, carry_out, overflow)
}

fun HaveLPAE(): Boolean = false

fun SignedSatQ(i: Int, N: Int): Pair<Int, Boolean>{
    val result: Int
    val saturated: Boolean
    when {
        i > pow(2, N - 1) - 1 -> {
            result = pow(2, N - 1) - 1
            saturated = true
        }
        i < -pow(2, N - 1) -> {
            result = -pow(2, N - 1)
            saturated = true
        }
        else -> {
            result = i
            saturated = false
        }
    }
    return Pair(result[N-1..0], saturated)
}

fun UnsignedSatQ(i: Int, N: Int): Pair<Int, Boolean>{
    val result: Int
    val saturated: Boolean
    when {
        i > pow(2, N) - 1 -> {
            result = pow(2, N) - 1
            saturated = true
        }
        i < 0 -> {
            result = 0
            saturated = true
        }
        else -> {
            result = i
            saturated = false
        }
    }
    return Pair(result[N-1..0], saturated)
}

fun SignedSat(i: Int, N: Int): Int{
    val (result, _) = SignedSatQ(i, N)
    return result
}

fun UnsignedSat(i: Int, N: Int): Int{
    val (result, _) = UnsignedSatQ(i, N)
    return result
}

fun BitCount(i: Int): Int {
    var count = 0
    while (i > 0){
        count += i and 1
        i shr 1
    }
    return count
}