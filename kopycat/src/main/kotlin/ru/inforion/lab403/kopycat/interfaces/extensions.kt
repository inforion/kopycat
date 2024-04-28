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

package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import java.math.BigInteger

/**
 * {RU}Метод используется для упрощения доступа на чтение.{RU}
 */
@JvmName("read")
inline fun IReadable.read(dtyp: Datatype, ea: ULong, ss: Int = 0) = read(ea, ss, dtyp.bytes)

/**
 * {RU}
 * Прочитать один байт с указанного адреса [ss]:[ea]
 *
 * @param ea адрес по котормоу происходит чтение
 * @param ss дополнительная часть адреса (может быть использована как segment selector)
 *
 * @return прочитанный байт (1 байт)
 * {RU}
 */
@JvmName("inb")
inline fun IReadable.inb(ea: ULong, ss: Int = 0): ULong = read(Datatype.BYTE, ea, ss)  // in byte

/**
 * {RU}
 * Прочитать два байта с указанного адреса [ss]:[ea]
 *
 * @param ea адрес по котормоу происходит чтение
 * @param ss дополнительная часть адреса (может быть использована как segment selector)
 *
 * @return прочитанное полуслово (2 байт)
 * {RU}
 */
@JvmName("inw")
inline fun IReadable.inw(ea: ULong, ss: Int = 0): ULong = read(Datatype.WORD, ea, ss)  // in word

/**
 * {RU}
 * Прочитать четыре байта с указанного адреса [ss]:[ea]
 *
 * @param ea адрес по которому происходит чтение
 * @param ss дополнительная часть адреса (может быть использована как segment selector)
 *
 * @return прочитанное слово (4 байт)
 * {RU}
 */
@JvmName("inl")
inline fun IReadable.inl(ea: ULong, ss: Int = 0): ULong = read(Datatype.DWORD, ea, ss)  // in long

/**
 * {RU}
 * Прочитать восемь байтов с указанного адреса [ss]:[ea]
 *
 * @param ea адрес по которому происходит чтение
 * @param ss дополнительная часть адреса (может быть использована как segment selector)
 *
 * @return прочитанное двойное слово (8 байт)
 * {RU}
 */
@JvmName("inq")
inline fun IReadable.inq(ea: ULong, ss: Int = 0): ULong = read(Datatype.QWORD, ea, ss)  // in quad


@JvmName("ine")
inline fun IReadable.ine(ea: ULong, size: Int, ss: Int = 0) = BigInteger(1, load(ea, size, ss).reversedArray())  // in extended

/**
 * {RU}Метод используется для упрощения доступа на запись.{RU}
 */
@JvmName("write")
inline fun IWritable.write(dtyp: Datatype, ea: ULong, value: ULong, ss: Int = 0) = write(ea, ss, dtyp.bytes, value)

/**
 * {RU}
 * Записать один байт данных [value] в указанный адрес [ss]:[ea]
 *
 * @param ea адрес по которому происходит запись
 * @param value записываемое значение
 * @param ss дополнительная часть адреса (может быть использована как segment selector)
 * {RU}
 */
@JvmName("outb")
inline fun IWritable.outb(ea: ULong, value: ULong, ss: Int = 0) = write(Datatype.BYTE, ea, value, ss)  // out byte

/**
 * {RU}
 * Записать два байта данных [value] в указанный адрес [ss]:[ea]
 *
 * @param ea адрес по которому происходит запись
 * @param value записываемое значение
 * @param ss дополнительная часть адреса (может быть использована как segment selector)
 * {RU}
 */
@JvmName("outw")
inline fun IWritable.outw(ea: ULong, value: ULong, ss: Int = 0) = write(Datatype.WORD, ea, value, ss)  // out word

/**
 * {RU}
 * Записать четыре байта данных [value] в указанный адрес [ss]:[ea]
 *
 * @param ea адрес по которому происходит запись
 * @param value записываемое значение
 * @param ss дополнительная часть адреса (может быть использована как segment selector)
 * {RU}
 */
@JvmName("outl")
inline fun IWritable.outl(ea: ULong, value: ULong, ss: Int = 0) = write(Datatype.DWORD, ea, value, ss)  // out long

/**
 * {RU}
 * Записать восемь байтов данных [value] в указанный адрес [ss]:[ea]
 *
 * @param ea адрес по которому происходит запись
 * @param value записываемое значение
 * @param ss дополнительная часть адреса (может быть использована как segment selector)
 * {RU}
 */
@JvmName("outq")
inline fun IWritable.outq(ea: ULong, value: ULong, ss: Int = 0) = write(Datatype.QWORD, ea, value, ss)  // out quad


@JvmName("oute")
inline fun IWritable.oute(ea: ULong, value: BigInteger, size: Int, ss: Int = 0) =
    store(ea, value.toByteArray().reversedArray().copyOf(size), ss)  // out extended

