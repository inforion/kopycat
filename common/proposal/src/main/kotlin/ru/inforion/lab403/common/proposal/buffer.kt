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
@file:Suppress("NOTHING_TO_INLINE", "HasPlatformType")

package ru.inforion.lab403.common.proposal

import ru.inforion.lab403.common.logging.logger
import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.ByteOrder.*
import kotlin.system.exitProcess

inline val ByteOrder.name get() = toString()

inline fun byteOrder(name: String) = when (name) {
    BIG_ENDIAN.name -> BIG_ENDIAN
    LITTLE_ENDIAN.name -> LITTLE_ENDIAN
    else -> throw IllegalArgumentException(name)
}

inline fun byteBuffer(capacity: Int, order: ByteOrder, isDirect: Boolean): ByteBuffer {
    val result = if (isDirect) ByteBuffer.allocateDirect(capacity) else ByteBuffer.allocate(capacity)
    return result.also { it.order(order) }
}

class SerializableByteBuffer constructor(): Externalizable {

    companion object {
        @Transient val log = logger()

        const val CHUNK_SIZE = 64 * 1024 * 1024
        const val END_MARKER = 0x6EADBEEF
    }

    constructor(obj: ByteBuffer): this() {
        this.obj = obj
    }

    lateinit var obj: ByteBuffer
        private set

    override fun writeExternal(out: ObjectOutput) {
        runCatching {
            out.writeObject(obj.order().name)
            out.writeBoolean(obj.isDirect)
            out.writeInt(obj.limit())
            out.writeInt(obj.position())
            val oldPosition = obj.position()
            obj.position(0)
            val array = ByteArray(CHUNK_SIZE)
            while (obj.remaining() != 0) {
                val size = minOf(obj.remaining(), array.size)
                obj.get(array, 0, size)
                out.write(array, 0, size)
            }
            out.writeInt(END_MARKER)
            obj.position(oldPosition)
        }.onFailure {
            it.printStackTrace()
            exitProcess(-1)
        }
    }

    override fun readExternal(`in`: ObjectInput) {
        runCatching {
            val order = `in`.readObject() as String
            val isDirect = `in`.readBoolean()
            val limit = `in`.readInt()
            val position = `in`.readInt()

            obj = byteBuffer(limit, byteOrder(order), isDirect)

            val array = ByteArray(CHUNK_SIZE)
            while (obj.remaining() != 0) {
                val remain = obj.remaining()
                val size = if (remain > array.size) array.size else remain
                val count = `in`.read(array, 0, size)
                obj.put(array, 0, count)
            }

            check(`in`.readInt() == END_MARKER) { "$this serialization marker != $END_MARKER" }

            obj.position(position)
        }.onFailure {
            it.printStackTrace()
            exitProcess(-1)
        }
    }


    inline fun array() = obj.array()

    inline fun order() = obj.order()
    inline fun order(bo: ByteOrder) = obj.order(bo)

    inline fun position() = obj.position()
    inline fun position(newPosition: Int) = obj.position(newPosition)

    inline fun rewind() = obj.rewind()

    inline fun limit() = obj.limit()

    inline fun get() = obj.get()
    inline fun get(index: Int) = obj.get(index)
    inline fun getShort(index: Int) = obj.getShort(index)
    inline fun getInt(index: Int) = obj.getInt(index)
    inline fun getLong(index: Int) = obj.getLong(index)

    inline fun get(array: ByteArray) = obj.get(array)
    inline fun get(array: ByteArray, offset: Int, length: Int) = obj.get(array, offset, length)

    inline fun put(b: Byte) = obj.put(b)
    inline fun put(index: Int, b: Byte) = obj.put(index, b)
    inline fun putShort(index: Int, value: Short) = obj.putShort(index, value)
    inline fun putInt(index: Int, value: Int) = obj.putInt(index, value)
    inline fun putLong(index: Int, value: Long) = obj.putLong(index, value)

    inline fun put(array: ByteArray) = obj.put(array)
    inline fun put(array: ByteArray, offset: Int, length: Int) = obj.put(array, offset, length)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SerializableByteBuffer) return false

        if (obj != other.obj) return false

        return true
    }

    override fun hashCode() = obj.hashCode()
}

fun ByteBuffer.toSerializable() = SerializableByteBuffer(this)

fun ByteArray.asBuffer() = ByteBuffer.wrap(this)

fun ByteArray.asSerializableBuffer() = asBuffer().toSerializable()