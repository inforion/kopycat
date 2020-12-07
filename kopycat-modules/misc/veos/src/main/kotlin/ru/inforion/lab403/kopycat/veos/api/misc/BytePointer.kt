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
package ru.inforion.lab403.kopycat.veos.api.misc

import ru.inforion.lab403.common.extensions.asByte
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.veos.kernel.System



open class BytePointer(val sys: System, address: Long) : Pointer<Byte>(address) {
    override fun get(index: Int) = sys.fullABI.readChar(address + index * sys.sizeOf.char).asByte
    override fun set(index: Int, value: Byte) = sys.fullABI.writeChar(address + index * sys.sizeOf.char, value.asULong)
    fun load(size: Int, offset: Int = 0) = sys.fullABI.readBytes(address + offset, size)
    fun store(data: ByteArray, offset: Int = 0) = sys.fullABI.writeBytes(address + offset, data)
}