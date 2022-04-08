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
@file:Suppress("UNCHECKED_CAST")

package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.common.extensions.cast
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.serializer.forEachClass
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.starProjectedType

interface IAutoSerializable : ISerializable {
    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        val result = mutableMapOf<String, Any?>()
        forEachClass {
            if (it.isSubclassOf(IAutoSerializable::class)) {
                if (it.starProjectedType != IAutoSerializable::class.starProjectedType) {
                    result += ctxt.serializeObject(this, it)
                    true
                } else {
                    false
                }
            } else if (it.isSubclassOf(ISerializable::class)) {
                if (it.starProjectedType != ISerializable::class.starProjectedType) {
                    val mt = MethodType.methodType(Map::class.java, ctxt::class.java)
                    val ctor = MethodHandles.Lookup::class.java.getDeclaredConstructor(Class::class.java)
                    ctor.isAccessible = true
                    try {
                        result += ctor.newInstance(it.java)
                            .findSpecial(it.java, "serialize", mt, it.java)
                            .invoke(this, ctxt) as Map<String, Any>
                    } catch (ex: IllegalAccessException) {

                    }
                }
                false
            } else {
                false
//            throw IllegalArgumentException("Non-serializable class: $it")
            }
        }
        return result.cast()
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        forEachClass {
            if (it.isSubclassOf(IAutoSerializable::class)) {
                if (it.starProjectedType != IAutoSerializable::class.starProjectedType) {
                    ctxt.deserializeObject(snapshot, this, it)
                    true
                } else {
                    false
                }
            } else if (it.isSubclassOf(ISerializable::class)) {
                if (it.starProjectedType != ISerializable::class.starProjectedType) {
                    // Void, Unit and "java void" all are different types!?!?
                    val baseFunc = it.java.methods.first { method -> method.name == "deserialize" }
                    val mt = MethodType.methodType(
                        baseFunc.returnType, baseFunc.parameterTypes[0], baseFunc.parameterTypes[1])

                    val ctor = MethodHandles.Lookup::class.java.getDeclaredConstructor(Class::class.java)
                    ctor.isAccessible = true
                    try {
                        ctor.newInstance(it.java)
                            .findSpecial(it.java, "deserialize", mt, it.java)
                            .invoke(this, ctxt, snapshot)
                    } catch (ex: IllegalAccessException) {

                    }
                }
                false
            } else {
                false
//            throw IllegalArgumentException("Non-serializable class: $it")
            }
        }
    }
}