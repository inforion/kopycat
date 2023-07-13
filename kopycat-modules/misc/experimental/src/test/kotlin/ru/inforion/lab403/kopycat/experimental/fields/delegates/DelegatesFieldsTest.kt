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
package ru.inforion.lab403.kopycat.experimental.delegateFields.delegates

import org.junit.Before
import org.junit.Test
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.experimental.fields.FieldsTopFixture
import kotlin.test.assertEquals

class DelegatesFieldsTest : Module(null, "Module Buses Test") {
    val top = FieldsTopFixture()

    @Before
    fun beforeTest() {
        top.reset()
    }

    @Test
    fun testAbsoluteFieldRead() {
        assertEquals(0x13121110uL, top.delegateFields.absoluteData.field10)
        assertEquals(0x23222120uL, top.delegateFields.absoluteData.field20)
    }

    @Test
    fun testAbsoluteFieldWrite() {
        top.delegateFields.absoluteData.field10 = 0xEEDDCCAAuL

        assertEquals(0xEEDDCCAAuL, top.delegateFields.absoluteData.field10)
        assertEquals(0xEEDDCCAAuL, top.ramPrepared.read(0x10uL, 0, 4))
    }

    @Test
    fun testOffsetFieldRead() {
        assertEquals(0x67666564_63626160uL, top.delegateFields.offsetData.offset10)
        assertEquals(0x77767574_73727170uL, top.delegateFields.offsetData.offset20)
    }

    @Test
    fun testOffsetFieldWrite() {
        top.delegateFields.offsetData.offset10 = 0xEEFFCCAAuL

        assertEquals(0xEEFFCCAAuL, top.delegateFields.offsetData.offset10)
        assertEquals(0xEEFFCCAAuL, top.ramPrepared.read(0x60uL, 0, 4))
    }

    @Test
    fun testDynamicAbsoluteFieldRead() {
        top.delegateFields.offsetData.dynamicBaseAddress = 0x20uL;
        assertEquals(0x4F4E4D4CuL, top.delegateFields.dynamicAbsoluteData.fieldA10)
        assertEquals(0x4B4A4948uL, top.delegateFields.dynamicAbsoluteData.fieldA20)

        top.delegateFields.offsetData.dynamicBaseAddress = 0x40uL;
        assertEquals(0x8F8E8D8CuL, top.delegateFields.dynamicAbsoluteData.fieldA10)
        assertEquals(0x8B8A8988uL, top.delegateFields.dynamicAbsoluteData.fieldA20)
    }

    @Test
    fun testDynamicAbsoluteWrite() {
        top.delegateFields.offsetData.dynamicBaseAddress = 0x20uL;
        top.delegateFields.dynamicAbsoluteData.fieldA10 = 0xAABBCCDDuL
        assertEquals(0xAABBCCDDuL, top.delegateFields.dynamicAbsoluteData.fieldA10)
        assertEquals(0xAABBCCDDuL, top.ramPrepared.read(0x4CuL, 0, 4))

        top.delegateFields.offsetData.dynamicBaseAddress = 0x40uL;
        top.delegateFields.dynamicAbsoluteData.fieldA10 = 0xAABB77DDuL
        assertEquals(0xAABB77DDuL, top.delegateFields.dynamicAbsoluteData.fieldA10)
        assertEquals(0xAABB77DDuL, top.ramPrepared.read(0x8CuL, 0, 4))
    }

    @Test
    fun testDynamicOffsetFieldRead() {
        top.delegateFields.offsetData.dynamicBaseAddress = 0x90uL;
        assertEquals(0xA7A6A5A4_A3A2A1A0uL, top.delegateFields.dynamicOffsetData.fieldDOffset10)
        assertEquals(0xB7B6B5B4_B3B2B1B0uL, top.delegateFields.dynamicOffsetData.fieldDOffset20)

        top.delegateFields.offsetData.dynamicBaseAddress = 0x10uL;
        assertEquals(0x27262524_23222120uL, top.delegateFields.dynamicOffsetData.fieldDOffset10)
        assertEquals(0x37363534_33323130uL, top.delegateFields.dynamicOffsetData.fieldDOffset20)
    }

    @Test
    fun testDynamicOffsetFieldWrite() {
        top.delegateFields.offsetData.dynamicBaseAddress = 0x60uL;
        top.delegateFields.dynamicOffsetData.fieldDOffset10 = 0xEE44CCAAuL
        assertEquals(0xEE44CCAAuL, top.delegateFields.dynamicOffsetData.fieldDOffset10)
        assertEquals(0xEE44CCAAuL, top.ramPrepared.read(0x70uL, 0, 4))

        top.delegateFields.offsetData.dynamicBaseAddress = 0x10uL;
        top.delegateFields.dynamicOffsetData.fieldDOffset10 = 0xEE66CCAAuL
        assertEquals(0xEE66CCAAuL, top.delegateFields.dynamicOffsetData.fieldDOffset10)
        assertEquals(0xEE66CCAAuL, top.ramPrepared.read(0x20uL, 0, 4))
    }
}
