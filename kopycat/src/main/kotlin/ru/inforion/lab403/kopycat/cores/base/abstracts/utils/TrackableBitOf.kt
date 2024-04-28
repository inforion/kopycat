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

import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.common.extensions.set
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.settings
import kotlin.reflect.KProperty

class TrackableBitOf<T: AGenericCore>(register: CpuRegister<T>, bit: Int, val core: T): BitOf<T>(register, bit) {
    override operator fun getValue(thisRef: CpuRegister<T>, property: KProperty<*>) = with(core) {
        if (settings.trackBitAccess)
            info.flagsAccessed = info.flagsAccessed set bit
        super.getValue(thisRef, property)
    }

    override operator fun setValue(thisRef: CpuRegister<T>, property: KProperty<*>, value: Boolean) = with(core) {
        if (settings.trackBitAccess)
            info.flagsChanged = info.flagsChanged set bit
        super.setValue(thisRef, property, value)
    }
}