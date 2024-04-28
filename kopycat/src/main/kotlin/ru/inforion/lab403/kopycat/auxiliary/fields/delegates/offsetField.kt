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
package ru.inforion.lab403.kopycat.auxiliary.fields.delegates

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.auxiliary.LazyStore
import ru.inforion.lab403.kopycat.auxiliary.fields.common.OffsetField
import ru.inforion.lab403.kopycat.auxiliary.fields.interfaces.IOffsetable
import kotlin.reflect.KProperty

/**
 * Delegate version of `OffsetField`
 */
open class offsetField<in T : IOffsetable>(
    val name: String,
    val offset: ULong,
    val size: Int,
    val ss: Int = 0
) {
    constructor(name: String, offset: ULong, datatype: Datatype, ss: Int = 0) :
            this(name, offset, datatype.bytes, ss)

    protected val field = LazyStore<OffsetField>();
    protected fun getField(thisRef: T) = field.getOrCreate {
        return@getOrCreate OffsetField(
            thisRef.memory,
            thisRef.baseAddress,
            name,
            offset,
            size,
            ss
        )
    }

    operator fun getValue(thisRef: T, property: KProperty<*>): ULong =
        getField(thisRef).data

    operator fun setValue(thisRef: T, property: KProperty<*>, value: ULong) {
        getField(thisRef).data = value
    }
}
