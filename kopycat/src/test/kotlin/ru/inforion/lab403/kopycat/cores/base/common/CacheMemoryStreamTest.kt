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
package ru.inforion.lab403.kopycat.cores.base.common

import org.junit.Test
import ru.inforion.lab403.common.extensions.unhexlify
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.test.assertEquals

class CacheMemoryStreamTest {
    val buffer =
        "0102030405060708090A0B0C0D0E0F00"
            .unhexlify()
            .let { ByteBuffer.wrap(it) }
            .apply { order(ByteOrder.LITTLE_ENDIAN) }

    @Test
    fun `ByteBuffer getNumber`() {
        assertEquals(0x01uL, buffer.getNumber(0, 1))
        assertEquals(0x0201uL, buffer.getNumber(0, 2))
        assertEquals(0x030201uL, buffer.getNumber(0, 3))
        assertEquals(0x04030201uL, buffer.getNumber(0, 4))
        assertEquals(0x050_4030201uL, buffer.getNumber(0, 5))
        assertEquals(0x0605_04030201uL, buffer.getNumber(0, 6))
        assertEquals(0x070605_04030201uL, buffer.getNumber(0, 7))
        assertEquals(0x08070605_04030201uL, buffer.getNumber(0, 8))

        assertEquals(0x08uL, buffer.getNumber(7, 1))
        assertEquals(0x0908uL, buffer.getNumber(7, 2))
        assertEquals(0x0A0908uL, buffer.getNumber(7, 3))
        assertEquals(0x0B0A0908uL, buffer.getNumber(7, 4))
        assertEquals(0x0C_0B0A0908uL, buffer.getNumber(7, 5))
        assertEquals(0x0D0C_0B0A0908uL, buffer.getNumber(7, 6))
        assertEquals(0x0E0D0C_0B0A0908uL, buffer.getNumber(7, 7))
        assertEquals(0x0F0E0D0C_0B0A0908uL, buffer.getNumber(7, 8))
    }

    @Test
    fun `ByteBuffer setNumber`() {
        buffer.putNumber(0, 0xD8D7D6D5_D4D3D2D1uL, 1)
        assertEquals(0x08070605040302D1uL, buffer.getNumber(0, 8))
        buffer.putNumber(0, 0xD8D7D6D5_D4D3D2D1uL, 2)
        assertEquals(0x080706050403D2D1uL, buffer.getNumber(0, 8))
        buffer.putNumber(0, 0xD8D7D6D5_D4D3D2D1uL, 3)
        assertEquals(0x0807060504D3D2D1uL, buffer.getNumber(0, 8))
        buffer.putNumber(0, 0xD8D7D6D5_D4D3D2D1uL, 4)
        assertEquals(0x08070605D4D3D2D1uL, buffer.getNumber(0, 8))
        buffer.putNumber(0, 0xD8D7D6D5_D4D3D2D1uL, 5)
        assertEquals(0x080706D5D4D3D2D1uL, buffer.getNumber(0, 8))
        buffer.putNumber(0, 0xD8D7D6D5_D4D3D2D1uL, 6)
        assertEquals(0x0807D6D5D4D3D2D1uL, buffer.getNumber(0, 8))
        buffer.putNumber(0, 0xD8D7D6D5_D4D3D2D1uL, 7)
        assertEquals(0x08D7D6D5D4D3D2D1uL, buffer.getNumber(0, 8))
        buffer.putNumber(0, 0xD8D7D6D5_D4D3D2D1uL, 8)
        assertEquals(0xD8D7D6D5D4D3D2D1uL, buffer.getNumber(0, 8))
    }

}