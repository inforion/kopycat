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
package ru.inforion.lab403.kopycat.auxiliary.fields.common

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.auxiliary.fields.interfaces.IMemoryRef
import ru.inforion.lab403.kopycat.auxiliary.fields.interfaces.IReadMemoryRef
import ru.inforion.lab403.kopycat.interfaces.IReadWrite
import ru.inforion.lab403.kopycat.interfaces.IReadable
import ru.inforion.lab403.kopycat.interfaces.IValuable

/**
 * Memory-proxy absolute-field.
 * Uses provided address.
 * and reads/writes a data by the address.
 *
 * Does not own the memory it uses.
 */
open class AbsoluteField(
    override val memory: IReadWrite,
    val name: String,
    open val address: ULong,
    val size: Int,
    val ss: Int = 0
) : IValuable, IMemoryRef {
    constructor(memory: IReadWrite, name: String, address: ULong, datatype: Datatype, ss: Int = 0) :
            this(memory, name, address, datatype.bytes, ss)

    override var data: ULong
        get() = memory.read(address, ss, size)
        set(value) {
            memory.write(address, ss, size, value)
        }
}

/**
 * Memory-proxy read-only absolute-field.
 * Uses provided address
 * and reads a data by the address.
 *
 * Does not own the memory it uses
 */
open class AbsoluteReadField(
    override val memory: IReadable,
    val name: String,
    open val address: ULong,
    val size: Int,
    val ss: Int = 0
) : IReadMemoryRef {
    constructor(memory: IReadable, name: String, address: ULong, datatype: Datatype, ss: Int = 0) :
            this(memory, name, address, datatype.bytes, ss)

    val data: ULong
        get() = memory.read(address, ss, size)
}
