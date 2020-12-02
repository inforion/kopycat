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
package ru.inforion.lab403.kopycat.cores.base.operands

import org.junit.Test
import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.modules.cores.device.ATest

class ImmediateTest: ATest() {
    private fun error(value: Long, expected: Long, actual: Long, test: String): String =
            error(value, expected, actual, "immediate", test)

    @Test fun test1_1() {
        value = 0xFEED_BEEF
        actual = immediate(value).ssext
        expected = -17973521L
        assert(error(value, expected, actual, "ssext"), expected, actual)
    }

    @Test fun test1_2() {
        value = 0xFEED_BEEF
        actual = immediate(value).usext
        expected = 0xFEED_BEEF
        assert(error(value, expected, actual, "usext"), expected, actual)
    }

    @Test fun test1_3() {
        value = 0xFEED_BEEF
        actual = immediate(value).zext
        expected = 0xFEED_BEEF
        assert(error(value, expected, actual, "zext"), expected, actual)
    }

    @Test fun test1_4() {
        value = 0xFEED_BEEF
        actual = immediate(value).bit(6).asLong
        expected = 1
        assert(error(value, expected, actual, "bit"), expected, actual)
    }

    @Test fun test1_5() {
        value = 0xFEED_BEEF
        actual = immediate(value).bit(24).asLong
        expected = 0
        assert(error(value, expected, actual, "bit"), expected, actual)
    }

    @Test fun test1_6() {
        value = 0xFEED_BEEF
        actual = immediate(value).msb.asLong
        expected = 1
        assert(error(value, expected, actual, "msb"), expected, actual)
    }

    @Test fun test1_7() {
        value = 0xFEED_BEEF
        actual = immediate(value).lsb.asLong
        expected = 1
        assert(error(value, expected, actual, "lsb"), expected, actual)
    }

    @Test fun test1_8() {
        value = 0xFEED_BEEF
        actual = immediate(value).isNegative.asLong
        expected = 1
        assert(error(value, expected, actual, "is negative"), expected, actual)
    }

    @Test fun test1_9() {
        value = 0xFEED_BEEF
        actual = immediate(value).isNotNegative.asLong
        expected = 0
        assert(error(value, expected, actual, "is not negative"), expected, actual)
    }

    @Test fun test1_10() {
        value = 0xFEED_BEEF
        actual = immediate(value).isZero.asLong
        expected = 0
        assert(error(value, expected, actual, "is zero"), expected, actual)
    }

    @Test fun test1_11() {
        value = 0xFEED_BEEF
        actual = immediate(value).isNotZero.asLong
        expected = 1
        assert(error(value, expected, actual, "is not zero"), expected, actual)
    }

    @Test fun test2_1() {
        value = 0x4A78
        actual = immediate(value, WORD).ssext
        expected = 0x4A78L
        assert(error(value, expected, actual, "ssext"), expected, actual)
    }

    @Test fun test2_2() {
        value = 0x4A78
        actual = immediate(value, WORD).usext
        expected = 0x4A78
        assert(error(value, expected, actual, "usext"), expected, actual)
    }

    @Test fun test2_3() {
        value = 0x4A78
        actual = immediate(value, WORD).zext
        expected = 0x4A78
        assert(error(value, expected, actual, "zext"), expected, actual)
    }

    @Test fun test2_4() {
        value = 0x4A78
        actual = immediate(value, WORD).bit(6).asLong
        expected = 1
        assert(error(value, expected, actual, "bit"), expected, actual)
    }

    @Test fun test2_5() {
        value = 0x4A78
        actual = immediate(value, WORD).msb.asLong
        expected = 0
        assert(error(value, expected, actual, "msb"), expected, actual)
    }

    @Test fun test2_6() {
        value = 0x4A78
        actual = immediate(value, WORD).lsb.asLong
        expected = 0
        assert(error(value, expected, actual, "lsb"), expected, actual)
    }

    @Test fun test2_7() {
        value = 0x4A78
        actual = immediate(value, WORD).lsb.asLong
        expected = 0
        assert(error(value, expected, actual, "lsb"), expected, actual)
    }

    @Test fun test2_8() {
        value = 0x4A78
        actual = immediate(value, WORD).isNegative.asLong
        expected = 0
        assert(error(value, expected, actual, "is negative"), expected, actual)
    }

    @Test fun test2_9() {
        value = 0x4A78
        actual = immediate(value, WORD).isNotNegative.asLong
        expected = 1
        assert(error(value, expected, actual, "is not negative"), expected, actual)
    }

    @Test fun test2_10() {
        value = 0x4A78
        actual = immediate(value, WORD).isZero.asLong
        expected = 0
        assert(error(value, expected, actual, "is zero"), expected, actual)
    }

    @Test fun test2_11() {
        value = 0x4A78
        actual = immediate(value, WORD).isNotZero.asLong
        expected = 1
        assert(error(value, expected, actual, "is not zero"), expected, actual)
    }
}