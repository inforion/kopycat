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
package ru.inforion.lab403.kopycat.cores.base.common

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.interfaces.IMemoryStream
import ru.inforion.lab403.kopycat.interfaces.IReadWrite

class StackStream(
        private val mem: IReadWrite,
        where: Long,
        val ssr: Int,
        val is16BitOperandMode: Boolean): IMemoryStream {

    enum class StackDirection { Increment, Decrement }

    val discrete = if (is16BitOperandMode) 2 else 4

    override var position: Long = where

    override var mark: Long = where
        @Suppress("UNUSED_PARAMETER")
        set(value) = throw GeneralException("You can't change mark field in class StackStream!")

    override var last: Int = 0

    override fun read(datatype: Datatype): Long {
        val result = peek(datatype)
        if (datatype.bytes == 1)
//            position += discrete + offset
            position += discrete
        else {
            if (!is16BitOperandMode && (datatype.bytes == 2))
                position += 2
//            position += datatype.bytes + offset
            position += datatype.bytes
        }
        return result
    }

    override fun write(datatype: Datatype,  data: Long){
        if (datatype.bytes == 1)
            position -= discrete
        else {
            if (!is16BitOperandMode && (datatype.bytes == 2))
                position -= 2
            position -= datatype.bytes
        }
        mem.write(datatype, position, data, ssr)
    }

    override fun peek(datatype: Datatype): Long = mem.read(datatype, position, ssr)

    override fun rewind() {
        position = mark
    }

    override val offset: Int get() = (position - mark).toInt()

    override val data: ByteArray get() = throw GeneralException("No data in StackStream!")
}