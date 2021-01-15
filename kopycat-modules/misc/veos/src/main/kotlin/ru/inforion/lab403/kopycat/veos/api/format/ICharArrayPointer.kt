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
package ru.inforion.lab403.kopycat.veos.api.format


interface ICharArrayPointer : ICharArrayConstPointer {
    val hasRemaining: Boolean
    val remaining: Int

    fun prev()

    fun set(c: Char)
    fun write(value: String) = value.forEach {
        write(it)
    }
    fun write(c: Char) {
        set(c)
        next()
    }
    operator fun plus(n: Int): ICharArrayPointer
    operator fun minus(n: Int): ICharArrayPointer
    operator fun plusAssign(n: Int)
    operator fun minusAssign(n: Int)

    val copy: ICharArrayPointer

    fun isNotEmpty(): Boolean

    fun readLast(count: Int): String
}