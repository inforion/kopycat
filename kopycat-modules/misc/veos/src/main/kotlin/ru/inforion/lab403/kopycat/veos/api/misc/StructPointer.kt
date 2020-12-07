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
package ru.inforion.lab403.kopycat.veos.api.misc

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.veos.kernel.System
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty


 
open class StructPointer(val sys: System, address: Long) : Pointer<Byte>(address) {

    inner class bit(val holder: KMutableProperty0<Int>, val n: Int) {
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = holder.get()[n].toBool()
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: Boolean) {
            val data = holder.get()
            holder.set(if (value) data set n else data clr n)
        }
    }

    inner class bool(val offset: Int) {
        private var cache: Boolean? = null
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = cache ?: getBoolean(offset).also { cache = it }
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: Boolean) = setBoolean(offset, value).also { cache = value }
    }

    inner class short(val offset: Int) {
        private var cache: Short? = null
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = cache ?: getShort(offset).also { cache = it }
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: Short) = setShort(offset, value).also { cache = value }
    }

    inner class int(val offset: Int) {
        private var cache: Int? = null
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = cache ?: getInt(offset).also { cache = it }
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: Int) = setInt(offset, value).also { cache = value }
    }

    inner class long(val offset: Int) {
        private var cache: Long? = null
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = cache ?: getLong(offset).also { cache = it }
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: Long) = setLong(offset, value).also { cache = value }
    }

    inner class pointer(val offset: Int) {
        private var cache: Long? = null
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = cache ?: getPointer(offset).also { cache = it }
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: Long) = setPointer(offset, value).also { cache = value }
    }

    override fun get(index: Int) = sys.fullABI.readChar(address + index * sys.sizeOf.char).asByte
    override fun set(index: Int, value: Byte) = sys.fullABI.writeChar(address + index * sys.sizeOf.char, value.asULong)

    fun getBoolean(offset: Int) = sys.fullABI.readChar(address + offset).toBool()
    fun setBoolean(offset: Int, value: Boolean) = sys.fullABI.writeChar(address + offset, value.asLong)

    fun getShort(offset: Int) = sys.fullABI.readShort(address + offset).asShort
    fun setShort(offset: Int, value: Short) = sys.fullABI.writeShort(address + offset, value.asULong)

    fun getInt(offset: Int) = sys.fullABI.readInt(address + offset).asInt
    fun setInt(offset: Int, value: Int) = sys.fullABI.writeInt(address + offset, value.asULong)

    fun getLong(offset: Int) = sys.fullABI.readLongLong(address + offset)
    fun setLong(offset: Int, value: Long) = sys.fullABI.writeLongLong(address + offset, value)

    fun getPointer(offset: Int) = sys.fullABI.readPointer(address + offset)
    fun setPointer(offset: Int, value: Long) = sys.fullABI.writePointer(address + offset, value)
}