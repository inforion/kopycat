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
package ru.inforion.lab403.kopycat.cores.base.abstracts.utils

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBankNG
import java.io.Serializable
import kotlin.reflect.KProperty

class FieldOf<T : AGenericCore>(
    val register: CpuRegister<T>,
    vararg val list: Pair<IntRange, IntRange>
) : Serializable {
    operator fun getValue(thisRef: ARegistersBankNG<T>.Register, property: KProperty<*>): ULong =
        list.fold(0u) { acc, (src, dst) ->
            acc.insert(register.value[src.first..src.last], dst.first..dst.last)
        }

    operator fun setValue(thisRef: ARegistersBankNG<T>.Register, property: KProperty<*>, newValue: ULong) {
        register.value = list.fold(register.value) { acc, (src, dst) ->
            acc.insert(newValue[dst.first..dst.last], src.first..src.last)
        }
    }
}