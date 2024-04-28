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
package ru.inforion.lab403.kopycat.experimental.runtime

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.interfaces.IReadWrite
import ru.inforion.lab403.kopycat.interfaces.inb
import java.nio.charset.Charset

typealias ReaderPredicateType = (value: ULong, index: Int, offset: ULong) -> Boolean

class DataUtils(private val memory: IReadWrite, safemode: Boolean = true) {

//    fun coreSafeRead(ea:)

    // TODO: hexdump

    companion object {
        fun isAsciiPrintable(char: Int) = char in (0x20..0x7E)
    }

    fun readDataWhile(ea: ULong, ss: Int, size: Int, predicate: ReaderPredicateType) = readDataWhileN(
        ea, ss, size,
        INT_MAX, predicate
    )

    // TODO: safe mode with runCatching
    fun readDataWhileN(ea: ULong, ss: Int, size: Int, amount: Int, predicate: ReaderPredicateType) =
        sequence {
            var offset = 0uL;
            for (i in (0 until amount)) {
                val value = memory.read(ea + offset, ss, size)
                if (!predicate(value, i, offset)) {
                    break;
                }

                yield(value)
                offset += size.ulong_z;
            }
        }

    fun inbWhile(ea: ULong, ss: Int, predicate: ReaderPredicateType) =
        readDataWhile(ea, ss, 1, predicate).map { it.ubyte }

    fun inwWhile(ea: ULong, ss: Int, predicate: ReaderPredicateType) =
        readDataWhile(ea, ss, 2, predicate).map { it.ushort }

    fun inlWhile(ea: ULong, ss: Int, predicate: ReaderPredicateType) =
        readDataWhile(ea, ss, 4, predicate).map { it.uint }

    fun inqWhile(ea: ULong, ss: Int, predicate: ReaderPredicateType) =
        readDataWhile(ea, ss, 8, predicate)

    fun inbWhileN(ea: ULong, ss: Int, amount: Int, predicate: ReaderPredicateType) =
        readDataWhileN(ea, ss, 1, amount, predicate).map { it.ubyte }

    fun inwWhileN(ea: ULong, ss: Int, amount: Int, predicate: ReaderPredicateType) =
        readDataWhileN(ea, ss, 2, amount, predicate).map { it.ushort }

    fun inlWhileN(ea: ULong, ss: Int, amount: Int, predicate: ReaderPredicateType) =
        readDataWhileN(ea, ss, 4, amount, predicate).map { it.uint }

    fun inqWhileN(ea: ULong, ss: Int, amount: Int, predicate: ReaderPredicateType) =
        readDataWhileN(ea, ss, 8, amount, predicate)

    fun Sequence<UByte>.mapCharToString(charset: Charset = Charsets.UTF_8) = this
        .map { c -> c.byte }
        .toList()
        .toByteArray()
        .toString(charset)

    fun Sequence<UShort>.mapWCharToString(charset: Charset = Charsets.UTF_16LE) = this
        .flatMap { c -> listOf(c.byte, (c ushr 8).byte) }
        .toList()
        .toByteArray()
        .toString(charset)

    fun readStringN(ea: ULong, ss: Int, maxLength: Int, charset: Charset = Charsets.UTF_8) =
        inbWhileN(ea, ss, maxLength) { value, _, _ ->
            value != 0uL
        }.mapCharToString(charset)

    fun readWStringN(ea: ULong, ss: Int, maxLength: Int, charset: Charset = Charsets.UTF_16LE) =
        inwWhileN(ea, ss, maxLength) { value, _, _ ->
            value != 0uL
        }.mapWCharToString(charset)

    fun readPrintableStringOrNullN(ea: ULong, ss: Int, maxLength: Int, charset: Charset = Charsets.UTF_8): String? {
        val string = inbWhileN(ea, ss, maxLength) { value, _, _ ->
            (value != 0uL) && isAsciiPrintable(value.int)
        }.mapCharToString(charset);

        if (string.length >= maxLength) {
            return string
        }

        // check the breaking char
        if (memory.inb(ea + string.length, ss) != 0uL) {
            return null;
        }
        return string;
    }

    fun getStringLengthN(ea: ULong, ss: Int, maxLength: Int) =
        inbWhileN(ea, ss, maxLength) { value, _, _ ->
            value != 0uL
        }.count()

    fun getWStringLengthN(ea: ULong, ss: Int, maxLength: Int) =
        inwWhileN(ea, ss, maxLength) { value, _, _ ->
            value != 0uL
        }.count()
}
