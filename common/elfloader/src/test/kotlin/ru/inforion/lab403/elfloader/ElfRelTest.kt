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
package ru.inforion.lab403.elfloader

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.elfloader.ElfRel.Companion.elfRel
import java.nio.ByteBuffer
import java.nio.ByteOrder

internal class ElfRelTest {
    companion object {
        val log = logger()
    }

    private val data = "3400000001A73A003800000001493D003C00000001DB3B00"
    private val dataBytes: ByteBuffer
    private val reloc: ElfRel

    init {
        dataBytes = ByteBuffer.wrap(data.unhexlify()).apply { order(ByteOrder.LITTLE_ENDIAN) }
        reloc = dataBytes.elfRel(0, 0, 0x08,  false)
    }

    @Test
    fun testRelocVaddr() {
        assertEquals(0x00000034, reloc.vaddr)
    }

    @Test
    fun testRelocType() {
        assertEquals(0x01, reloc.type)
    }

    @Test
    fun testRelocSym() {
        assertEquals(0x00003AA7, reloc.sym)
    }

    @Test
    fun testRelocAddend() {
        assertEquals(0, reloc.addend)
    }


}