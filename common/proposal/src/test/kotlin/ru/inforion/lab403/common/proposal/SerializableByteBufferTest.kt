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

import org.junit.Test
import org.junit.jupiter.api.assertThrows
import ru.inforion.lab403.common.extensions.hexlify
import java.io.*
import java.lang.IllegalStateException
import java.nio.ByteBuffer
import kotlin.test.assertEquals

internal class SerializableByteBufferTest {
    private fun <T : Serializable> T.serialize(output: OutputStream) =
            ObjectOutputStream(output).apply { writeUnshared(this@serialize) }

    private fun <T : Serializable> InputStream.deserialize(): T =
            ObjectInputStream(this).readUnshared() as T

    private fun <T: Serializable> ByteArray.deserialize(): T = inputStream().deserialize()

    @Test
    fun bufferHeapSerializationTest() {
        val buffer = ByteBuffer.allocate(0x100_0000).toSerializable()

        repeat(100) { buffer.put(it.toByte()) }

        val stream = ByteArrayOutputStream()
        100.serialize(stream)
        buffer.serialize(stream)
        200.serialize(stream)

        val array = stream.toByteArray().inputStream()

        array.deserialize<Int>()
        val result = array.deserialize<SerializableByteBuffer>()
        array.deserialize<Int>()

        assertEquals(buffer.array().hexlify(), result.array().hexlify())
    }

    @Test
    fun bufferDirectedSerializationTest() {
        val buffer = ByteBuffer.allocateDirect(0x100_0000).toSerializable()

        repeat(100) { buffer.put(it.toByte()) }

        buffer.position(0)
        val array0 = ByteArray(buffer.limit()) { buffer.get() }

        val stream = ByteArrayOutputStream()
        buffer.serialize(stream)
        val result = stream.toByteArray().deserialize<SerializableByteBuffer>()

        result.position(0)
        val array1 = ByteArray(result.limit()) { result.get() }

        assertEquals(array0.hexlify(), array1.hexlify())
    }

   @Test

    fun bufferDirectedThrowTest() {
        val buffer = ByteBuffer.allocateDirect(0x4000_0000).toSerializable()
        val stream = ByteArrayOutputStream()
        buffer.serialize(stream)
        assertThrows  ( OutOfMemoryError  ()) {
            stream.toByteArray().deserialize<SerializableByteBuffer>()

        }
    }

    private fun assertThrows(outOfMemoryError: OutOfMemoryError, function: () -> SerializableByteBuffer) {

    }
}
