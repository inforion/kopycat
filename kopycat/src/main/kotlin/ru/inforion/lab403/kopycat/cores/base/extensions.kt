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

package ru.inforion.lab403.kopycat.cores.base

import ru.inforion.lab403.common.extensions.byte
import ru.inforion.lab403.common.extensions.mask
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.common.extensions.ushr
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACPU
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACore
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import kotlin.io.path.Path
import kotlin.reflect.KClass

fun ByteArray.putNumberLE(position: Int, value: ULong, size: Int) {
    when (size) {
        1 -> {
            this[position] = value.byte
        }

        2 -> {
            this[position] = value.byte
            this[position + 1] = (value ushr 8).byte
        }

        3 -> {
            this[position] = value.byte
            this[position + 1] = (value ushr 8).byte
            this[position + 2] = (value ushr 16).byte
        }

        4 -> {
            this[position] = value.byte
            this[position + 1] = (value ushr 8).byte
            this[position + 2] = (value ushr 16).byte
            this[position + 3] = (value ushr 24).byte
        }

        5 -> {
            this[position] = value.byte
            this[position + 1] = (value ushr 8).byte
            this[position + 2] = (value ushr 16).byte
            this[position + 3] = (value ushr 24).byte
            this[position + 4] = (value ushr 32).byte
        }

        6 -> {
            this[position] = value.byte
            this[position + 1] = (value ushr 8).byte
            this[position + 2] = (value ushr 16).byte
            this[position + 3] = (value ushr 24).byte
            this[position + 4] = (value ushr 32).byte
            this[position + 5] = (value ushr 40).byte
        }

        7 -> {
            this[position] = value.byte
            this[position + 1] = (value ushr 8).byte
            this[position + 2] = (value ushr 16).byte
            this[position + 3] = (value ushr 24).byte
            this[position + 4] = (value ushr 32).byte
            this[position + 5] = (value ushr 40).byte
            this[position + 6] = (value ushr 48).byte
        }

        8 -> {
            this[position] = value.byte
            this[position + 1] = (value ushr 8).byte
            this[position + 2] = (value ushr 16).byte
            this[position + 3] = (value ushr 24).byte
            this[position + 4] = (value ushr 32).byte
            this[position + 5] = (value ushr 40).byte
            this[position + 6] = (value ushr 48).byte
            this[position + 7] = (value ushr 56).byte
        }

        else -> throw IllegalArgumentException("ByteArray.putNumberLE Wrong int size=$size")
    }
}

fun ByteArray.getNumberLE(position: Int, size: Int): ULong {
    return when (size) {
        1 -> this[position].ulong_z
        2 -> this[position].ulong_z or (this[position + 1].ulong_z shl 8)
        3 -> this[position].ulong_z or (this[position + 1].ulong_z shl 8) or (this[position + 2].ulong_z shl 16)
        4 -> this[position].ulong_z or (this[position + 1].ulong_z shl 8) or (this[position + 2].ulong_z shl 16) or (this[position + 3].ulong_z shl 24)
        5 -> this[position].ulong_z or (this[position + 1].ulong_z shl 8) or (this[position + 2].ulong_z shl 16) or (this[position + 3].ulong_z shl 24) or (this[position + 4].ulong_z shl 32)
        6 -> this[position].ulong_z or (this[position + 1].ulong_z shl 8) or (this[position + 2].ulong_z shl 16) or (this[position + 3].ulong_z shl 24) or (this[position + 4].ulong_z shl 32) or (this[position + 5].ulong_z shl 40)
        7 -> this[position].ulong_z or (this[position + 1].ulong_z shl 8) or (this[position + 2].ulong_z shl 16) or (this[position + 3].ulong_z shl 24) or (this[position + 4].ulong_z shl 32) or (this[position + 5].ulong_z shl 40) or (this[position + 6].ulong_z shl 48)
        8 -> this[position].ulong_z or (this[position + 1].ulong_z shl 8) or (this[position + 2].ulong_z shl 16) or (this[position + 3].ulong_z shl 24) or (this[position + 4].ulong_z shl 32) or (this[position + 5].ulong_z shl 40) or (this[position + 6].ulong_z shl 48) or (this[position + 7].ulong_z shl 56)
        else -> throw IllegalArgumentException("ByteArray.getNumberLE Wrong int size=$size")
    }
}


inline infix fun ULong.like(dtyp: Datatype): ULong = this mask dtyp.bits
inline infix fun Long.like(dtyp: Datatype): Long = this mask dtyp.bits
inline infix fun Int.like(dtyp: Datatype): Int = this mask dtyp.bits
inline infix fun Short.like(dtyp: Datatype): Short = this mask dtyp.bits
inline infix fun Byte.like(dtyp: Datatype): Byte = this mask dtyp.bits

fun <R : ACore<R, U, P>, U : ACPU<U, R, *, *>, P : ACOP<P, R>> ACore<R, U, P>.stepOrFail() {
    step().also { status ->
        if (!status.resume) {
            info.dump()
            throw GeneralException("Unable to continue the execution")
        }
    }
}

fun <R : ACore<R, U, P>, U : ACPU<U, R, *, *>, P : ACOP<P, R>> ACore<R, U, P>.stepOrFailWhile(block: () -> Boolean) {
    while (block()) {
        stepOrFail()
    }
}

fun <R : ACore<R, U, P>, U : ACPU<U, R, *, *>, P : ACOP<P, R>> ACore<R, U, P>.stepOrFailToPc(expectedPc: ULong) {
    return stepOrFailWhile {
        pc != expectedPc
    }
}

fun <T> ULong.letOrNull(block: (ULong) -> T): T? = if (this == 0uL) {
    null
} else {
    this.let(block)
}

fun <T> ULong.letOrFailIfNull(block: (ULong) -> T): T = if (this == 0uL) {
    throw IllegalStateException("Value is null")
} else {
    this.let(block)
}

val KClass<*>.classResourcePath get() = Path(this.java.`package`.name.replace('.', '/'))