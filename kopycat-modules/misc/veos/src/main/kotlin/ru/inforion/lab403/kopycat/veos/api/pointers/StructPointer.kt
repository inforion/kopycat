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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.veos.api.pointers

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.veos.kernel.System
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty


 
open class StructPointer(sys: System, address: Long) : Pointer<Byte>(sys, address) {

    inner class bit(val holder: KMutableProperty0<Int>, val n: Int) {
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = holder.get()[n].toBool()
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: Boolean) {
            val data = holder.get()
            holder.set(if (value) data set n else data clr n)
        }
    }

    inner class bool(val offset: Int): IAutoSerializable {
        private var cache: Boolean? = null
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = cache ?: getBoolean(offset).also { cache = it }
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: Boolean) = setBoolean(offset, value).also { cache = value }
    }

    inner class byte(val offset: Int): IAutoSerializable {
        private var cache: Byte? = null
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = cache ?: get(offset).also { cache = it }
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: Byte) = set(offset, value).also { cache = value }
    }

    inner class short(val offset: Int): IAutoSerializable {
        private var cache: Short? = null
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = cache ?: getShort(offset).also { cache = it }
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: Short) = setShort(offset, value).also { cache = value }
    }

    inner class int(val offset: Int): IAutoSerializable {
        private var cache: Int? = null
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = cache ?: getInt(offset).also { cache = it }
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: Int) = setInt(offset, value).also { cache = value }
    }

    inner class long(val offset: Int): IAutoSerializable {
        private var cache: Long? = null
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = cache ?: getLong(offset).also { cache = it }
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: Long) = setLong(offset, value).also { cache = value }
    }

    inner class longlong(val offset: Int): IAutoSerializable {
        private var cache: Long? = null
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = cache ?: getLongLong(offset).also { cache = it }
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: Long) = setLongLong(offset, value).also { cache = value }
    }

    inner class pointer(val offset: Int): IAutoSerializable {
        private var cache: Long? = null
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = cache ?: getPointer(offset).also { cache = it }
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: Long) = setPointer(offset, value).also { cache = value }
    }

    inner class intarray(val offset: Int, val size: Int): IAutoSerializable {
        private var cache: IntArray? = null
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = cache ?: getInts(offset, size).also { cache = it }
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: IntArray) = setInts(offset, value).also { cache = value }
    }

    inner class bytearray(val offset: Int, val size: Int): IAutoSerializable {
        private var cache: ByteArray? = null
        operator fun getValue(thisRef: StructPointer, property: KProperty<*>) = cache ?: getBytes(offset, size).also { cache = it }
        operator fun setValue(thisRef: StructPointer, property: KProperty<*>, value: ByteArray) = setBytes(offset, value).also { cache = value }
    }

    override fun get(index: Int) = sys.abi.readChar(address + index * sys.sizeOf.char).asByte
    override fun set(index: Int, value: Byte) = sys.abi.writeChar(address + index * sys.sizeOf.char, value.asULong)

    inline fun getBoolean(offset: Int) = sys.abi.readChar(address + offset).toBool()
    inline fun setBoolean(offset: Int, value: Boolean) = sys.abi.writeChar(address + offset, value.asLong)

    inline fun getShort(offset: Int) = sys.abi.readShort(address + offset).asShort
    inline fun setShort(offset: Int, value: Short) = sys.abi.writeShort(address + offset, value.asULong)

    inline fun getInt(offset: Int) = sys.abi.readInt(address + offset).asInt
    inline fun setInt(offset: Int, value: Int) = sys.abi.writeInt(address + offset, value.asULong)

    inline fun getLong(offset: Int) = sys.abi.readLong(address + offset)
    inline fun setLong(offset: Int, value: Long) = sys.abi.writeLong(address + offset, value)

    inline fun getLongLong(offset: Int) = sys.abi.readLongLong(address + offset)
    inline fun setLongLong(offset: Int, value: Long) = sys.abi.writeLongLong(address + offset, value)

    inline fun getPointer(offset: Int) = sys.abi.readPointer(address + offset)
    inline fun setPointer(offset: Int, value: Long) = sys.abi.writePointer(address + offset, value)

    inline fun getInts(offset: Int, size: Int) = IntArray(size) { getInt(offset + it * sys.sizeOf.int).asInt }
    inline fun setInts(offset: Int, value: IntArray) = value.forEachIndexed { i, v -> setInt(offset + i * sys.sizeOf.int, v) }

    inline fun getBytes(offset: Int, size: Int) = sys.abi.readBytes(address + offset, size)
    inline fun setBytes(offset: Int, value: ByteArray) = sys.abi.writeBytes(address + offset, value)
}