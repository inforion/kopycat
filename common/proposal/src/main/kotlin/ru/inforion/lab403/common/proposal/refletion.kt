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
package ru.inforion.lab403.common.proposal

import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.javaType

/**
 * {EN}
 * Implements 'reverse' [Class.isAssignableFrom] for type of [KProperty1] field
 *
 * @param cls basic Java class
 *
 *
 * @return true if type of field is subtype of specified class [cls]
 * {EN}
 */
infix fun <T, R>KProperty1<T, R>.isTypeSubtypeOf(cls: Class<*>): Boolean {
    val type = returnType.javaType as? Class<*> ?: return false
    return cls.isAssignableFrom(type)
}