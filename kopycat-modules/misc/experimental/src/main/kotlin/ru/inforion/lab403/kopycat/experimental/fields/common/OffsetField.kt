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
package ru.inforion.lab403.kopycat.experimental.fields.common

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.experimental.fields.interfaces.IOffsetable
import ru.inforion.lab403.kopycat.experimental.fields.interfaces.IReadOffsetable
import ru.inforion.lab403.kopycat.interfaces.IReadWrite
import ru.inforion.lab403.kopycat.interfaces.IReadable

/**
 * Memory-proxy offset-field.
 * Calculates address using `baseAddress + offset`
 * and reads/writes a data by the address.
 *
 * Does not own the memory it uses.
 */
open class OffsetField(
    memory: IReadWrite,
    baseAddress: ULong,
    name: String,
    open val offset: ULong,
    size: Int,
    ss: Int = 0
) : AbsoluteField(memory, name, baseAddress + offset, size, ss) {
    constructor(memory: IReadWrite, baseAddress: ULong, name: String, offset: ULong, datatype: Datatype, ss: Int = 0) :
            this(memory, baseAddress, name, offset, datatype.bytes, ss)

    constructor(offsetable: IOffsetable, name: String, offset: ULong, size: Int, ss: Int = 0) :
            this(offsetable.memory, offsetable.baseAddress, name, offset, size, ss)

    constructor(offsetable: IOffsetable, name: String, offset: ULong, datatype: Datatype, ss: Int = 0) :
            this(offsetable.memory, offsetable.baseAddress, name, offset, datatype.bytes, ss)

    /**
     * Calculated absolute address
     */
    override val address: ULong get() = super.address
}

/**
 * Memory-proxy read-only offset-field.
 * Calculates address using `baseAddress + offset`
 * and reads a data by the address.
 *
 * Does not own the memory it uses.
 */
open class OffsetReadField(
    memory: IReadable,
    baseAddress: ULong,
    name: String,
    open val offset: ULong,
    size: Int,
    ss: Int = 0
) : AbsoluteReadField(memory, name, baseAddress + offset, size, ss) {
    constructor(memory: IReadWrite, baseAddress: ULong, name: String, offset: ULong, datatype: Datatype, ss: Int = 0) :
            this(memory, baseAddress, name, offset, datatype.bytes, ss)

    constructor(offsetable: IReadOffsetable, name: String, offset: ULong, size: Int, ss: Int = 0) :
            this(offsetable.memory, offsetable.baseAddress, name, offset, size, ss)

    constructor(offsetable: IReadOffsetable, name: String, offset: ULong, datatype: Datatype, ss: Int = 0) :
            this(offsetable.memory, offsetable.baseAddress, name, offset, datatype.bytes, ss)
}
