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
package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.common.extensions.sure
import ru.inforion.lab403.common.proposal.findFieldRecursive
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.serializer.serializeItem
import kotlin.reflect.full.primaryConstructor


interface IConstructorSerializable: ISerializable {
    fun serializeConstructor(ctxt: GenericSerializer): Array<Any?>? {
        val cls = this::class.java
        val ctor = cls.kotlin.primaryConstructor ?: return null
        return ctor.parameters.map {
            val field = cls.findFieldRecursive(it.name!!).sure { "Field ${it.name} not found in ${cls.name}" }
            field.isAccessible = true
            serializeItem(ctxt, field.get(this), field.name)
        }.toTypedArray()
    }
}