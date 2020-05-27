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
package ru.inforion.lab403.kopycat.cores.arm.support

import org.junit.Assert
import org.junit.Test
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.support.Mask
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.support.Stub
import ru.inforion.lab403.kopycat.cores.arm.hardware.systemdc.support.Table
import ru.inforion.lab403.kopycat.cores.base.exceptions.DecoderException



class ARMMaskTest {
    private fun assert(raw: String, offset: Int, value: Long, expected: Boolean) {
        val mask = Mask.create(raw, offset)
        val str = "$raw, $offset - ${value.toString(2)}"
        val res = mask.suit(value)
        Assert.assertEquals(str, res, expected)
    }

    // MOV r9, r0, #4096
    @Test fun testE3017050() {
        val m1 = Mask.create("1", 25)
        val m2 = Mask.create("10000", 20)
        val m3 = Mask.create("-", 4)
        val mres = m1 + m2 + m3
        val isSuit = mres.suit(0xE3017050)
        Assert.assertEquals(true, isSuit)
    }

    @Test fun testNotE3017050() {
        val m1 = Mask.create("1", 25)
        val m2 = Mask.create("not 10xx0", 20)
        val m3 = Mask.create("-", 4)
        val mres = m1 + m2 + m3
        val isSuit = mres.suit(0xE3017050)
        Assert.assertEquals(false, isSuit)
    }

    @Test fun testNegativePositiveMerge() {
        val m1 = Mask.create("xx0x1 not 0x011", 20)
        val m2 = Mask.create("not 1111", 16)
        val mres = m1 + m2
        Assert.assertEquals("[xxxxxxxxx0x1xxxxxxxxxxxxxxxxxxxx|xxxxxxx0x0111111xxxxxxxxxxxxxxxx]", "$mres")
    }

    @Test fun testNegativeEmptyMerge() {
        val m1 = Mask.create("xx0x1", 20)
        val m2 = Mask.create("not 1111", 16)
        val mres = m1 + m2
        Assert.assertEquals("[xxxxxxxxx0x1xxxxxxxxxxxxxxxxxxxx|xxxxxxxxxxxx1111xxxxxxxxxxxxxxxx]", "$mres")
    }

    @Test fun testPositiveXBeginTrue(){
        val raw = "0xx0"
        val offset = 0
        val value = 0b110L
        assert(raw, offset, value, true)
    }

    @Test fun testPositiveXBeginFalse(){
        val raw = "0xxx"
        val offset = 0
        val value = 0b1110L
        assert(raw, offset, value, false)
    }

    @Test fun testPositiveBeginTrue(){
        val raw = "0000000001100"
        val offset = 0
        val value = 0b1100L
        assert(raw, offset, value, true)
    }

    @Test fun testPositiveBeginFalse(){
        val raw = "1000000000000001100"
        val offset = 0
        val value = 0b1100L
        assert(raw, offset, value, false)
    }

    @Test fun testPositiveXMiddleFalse() {
        val raw = "0xx0"
        val offset = 5
        val value = 0b101011001L
        assert(raw, offset, value, false)
    }

    @Test fun testPositiveMiddleTrue() {
        val raw = "1011001"
        val offset = 3
        val value = 0b1011001001L
        assert(raw, offset, value, true)
    }

    @Test fun testPositiveXAllMiddleTrue() {
        val raw = "xxxxxx"
        val offset = 20
        val value = 0b11111111111111111111111111L
        assert(raw, offset, value, true)
    }

    @Test fun testPositiveXAllBeginTrue() {
        val raw = "xxxxxx"
        val offset = 0
        val value = 0b0L
        assert(raw, offset, value, true)
    }

    @Test fun testPositiveXEndTrue() {
        val raw = "1110xxx0xx1x1x100xx00x0x1x0x1x0x"
        val offset = 0
        val value = 0b11100010101010100010000111001100
        assert(raw, offset, value, true)
    }

    @Test fun testNegativeXBeginTrue1(){
        val raw = "not 0xx0"
        val offset = 0
        val value = 0b1L
        assert(raw, offset, value, true)
    }

    @Test fun testNegativeXBeginTrue2() {
        val raw = "not 0xx0"
        val offset = 0
        val value = 0b1011L
        assert(raw, offset, value, true)
    }

    @Test fun testNegativeXMiddleTrue() {
        val raw = "not 0xx0"
        val offset = 3
        val value = 0b1111011L
        assert(raw, offset, value, true)
    }

    @Test (expected = IllegalArgumentException::class)
    fun testNegativeXAllMiddleFalse() {
        val raw = "not xxxxxx"
        val offset = 19
        val value = 0b01010111L
        assert(raw, offset, value, false)
    }

    @Test fun testNegativeXEndTrue() {
        val raw = "not 1110xxx0xx1x1x100xx00x0x1x0x1x0x"
        val offset = 0
        val value = 0b100010101010100010000111001100L
        assert(raw, offset, value, true)
    }

    @Test fun testCombXBeginTrue1() {
        val raw = "10xx0 not x0xx"
        val offset = 0
        val value = 0b10110L
        assert(raw, offset, value, true)
    }

    @Test fun testCombXBeginTrue2() {
        val raw = "10xx0 not x0xx"
        val offset = 0
        val value = 0b10100L
        assert(raw, offset, value, true)
    }

    @Test fun testCombXBeginFalse() {
        val raw = "10xx0 not x0xx"
        val offset = 0
        val value = 0b100L
        assert(raw, offset, value, false)
    }

    @Test (expected = IllegalArgumentException::class)
    fun testCombXAllMiddleFalse() {
        val raw = "xxxx not xxxx"
        val offset = 5
        val value = 0b10100L
        assert(raw, offset, value, false)
    }

    @Test fun testCombXMiddleTrue() {
        val raw = "1x0xx0 not x100x0"
        val offset = 5
        val value = 0b11010000000L
        assert(raw, offset, value, true)
    }

    @Test fun testCombXMiddleFalse() {
        val raw = "1x0xx0 not x100x0"
        val offset = 5
        val value = 0b11000000000L
        assert(raw, offset, value, false)
    }

    @Test fun testCombXEndFalse() {
        val raw = "1x0xx0 not x100x0"
        val offset = 25
        val value = 0b1100000000000000000000000000000L
        assert(raw, offset, value, false)
    }

    @Test fun testCombXEndTrue() {
        val raw = "1x0xx0 not x100x0"
        val offset = 25
        val value = 0b11000000000000000000000000000000L
        assert(raw, offset, value, true)
    }

    @Test fun testLDRLMask() {
        val table = Table("Load/store word and unsigned byte",
                arrayOf(25, 24..20, 4, 19..16),
                arrayOf("0, xx0x0 not 0x010, -,        -" to Stub("stri"),
                        "1, xx0x0 not 0x010, 0,        -" to Stub("strr"),
                        "0, 0x010          , -,        -" to Stub("strti"),
                        "1, 0x010          , 0,        -" to Stub("strtr"),
                        "0, xx0x1 not 0x011, -,     1111" to Stub("ldrl"),
                        "0, xx0x1 not 0x011, -, not 1111" to Stub("ldri"),
                        "1, xx0x1 not 0x011, 0,        -" to Stub("ldrr"),
                        "0, 0x011          , -,        -" to Stub("ldrti"),
                        "1, 0x011          , 0,        -" to Stub("ldrtr"),
                        "0, xx1x0 not 0x110, -,        -" to Stub("strbi"),
                        "1, xx1x0 not 0x110, 0,        -" to Stub("strbr"),
                        "0, 0x110          , -,        -" to Stub("strbti"),
                        "1, 0x110          , 0,        -" to Stub("strbtr"),
                        "0, xx1x1 not 0x111, -,     1111" to Stub("ldrbl"),
                        "0, xx1x1 not 0x111, -, not 1111" to Stub("ldrbi"),
                        "1, xx1x1 not 0x111, 0,        -" to Stub("ldrbr"),
                        "0, 0x111          , -,        -" to Stub("ldrbti"),
                        "1, 0x111          , 0,        -" to Stub("ldrbtr")
                )
        )

        val message = try {
            table.lookup(0xE51F_1000, 0x0000_0000L)
        } catch (exc: DecoderException) {
            exc.message
        }

        Assert.assertEquals("ldrl", message)
    }


}