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
package ru.inforion.lab403.kopycat.auxiliary.fields.common

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.interfaces.IReadWrite
import ru.inforion.lab403.kopycat.interfaces.IReadable


/**
 * Memory-proxy dynamic offset-field.
 * Uses provided callback to receive the base address.
 * Reads/writes a data by the calculated `baseAddress() + offset` address.
 *
 * Does not own the memory it uses.
 */
open class DynamicOffsetField(
    override val memory: IReadWrite,
    name: String,
    offset: ULong,
    size: Int,
    ss: Int = 0,
    private val baseAddressCallback: () -> ULong,
) : DynamicAbsoluteField(
    memory,
    name,
    size,
    ss,
    { baseAddressCallback() + offset }
) {
    constructor(
        memory: IReadWrite,
        name: String,
        offset: ULong,
        datatype: Datatype,
        ss: Int = 0,
        baseAddressCallback: () -> ULong
    ) : this(memory, name, offset, datatype.bytes, ss, baseAddressCallback)
}

/**
 * Memory-proxy dynamic offset-field.
 * Uses provided callback to receive the base address.
 * Reads a data by the calculated `baseAddress() + offset` address.
 *
 * Does not own the memory it uses.
 */
open class DynamicOffsetReadField(
    override val memory: IReadable,
    name: String,
    offset: ULong,
    size: Int,
    ss: Int = 0,
    private val baseAddressCallback: () -> ULong,
) : DynamicAbsoluteReadField(
    memory,
    name,
    size,
    ss,
    { baseAddressCallback() + offset }
) {
    constructor(
        memory: IReadable,
        name: String,
        offset: ULong,
        datatype: Datatype,
        ss: Int = 0,
        baseAddressCallback: () -> ULong
    ) : this(memory, name, offset, datatype.bytes, ss, baseAddressCallback)
}
