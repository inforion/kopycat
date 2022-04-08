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
package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.common.extensions.sure
import ru.inforion.lab403.common.reflection.findFieldRecursive
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import kotlin.reflect.KProperty1
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.kotlinProperty


interface IConstructorSerializable : ISerializable {

    fun serializeConstructor(ctxt: GenericSerializer): Array<Any?>? {
        val cls = this::class.java
        val ctor = cls.kotlin.primaryConstructor ?: return null
        return ctor.parameters.map {
            val thiz = this
            val field = cls.findFieldRecursive(it.name!!).sure { "Field ${it.name} not found in ${cls.name}" }
            val property = field.kotlinProperty!! as KProperty1<Any, Any?>
            field.isAccessible = true
            property.isAccessible = true
            val value = try {
                property.get(thiz)
            } catch (error: IllegalArgumentException) {
                field.get(thiz)
            }
            ctxt.serializeItem(value, field.name)
        }.toTypedArray()
    }
}