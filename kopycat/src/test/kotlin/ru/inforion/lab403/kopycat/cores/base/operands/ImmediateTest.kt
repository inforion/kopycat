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
package ru.inforion.lab403.kopycat.cores.base.operands

import org.junit.Test
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.common.extensions.unaryMinus
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.modules.cores.device.ATest

class ImmediateTest: ATest() {
    private fun error(value: ULong, expected: ULong, actual: ULong, test: String): String =
            error(value, expected, actual, "immediate", test)

    @Test fun test1_1() {
        value = 0xFEED_BEEFu
        actual = immediate(value).ssext.ulong
        expected = -17973521uL
        assert(error(value, expected, actual, "ssext"), expected, actual)
    }

    @Test fun test1_2() {
        value = 0xFEED_BEEFu
        actual = immediate(value).usext
        expected = 0xFEED_BEEFu
        assert(error(value, expected, actual, "usext"), expected, actual)
    }

    @Test fun test1_3() {
        value = 0xFEED_BEEFu
        actual = immediate(value).value
        expected = 0xFEED_BEEFu
        assert(error(value, expected, actual, "zext"), expected, actual)
    }

    @Test fun test1_4() {
        value = 0xFEED_BEEFu
        actual = immediate(value).bit(6).ulong_z
        expected = 1u
        assert(error(value, expected, actual, "bit"), expected, actual)
    }

    @Test fun test1_5() {
        value = 0xFEED_BEEFu
        actual = immediate(value).bit(24).ulong_z
        expected = 0u
        assert(error(value, expected, actual, "bit"), expected, actual)
    }

    @Test fun test1_6() {
        value = 0xFEED_BEEFu
        actual = immediate(value).msb.ulong_z
        expected = 1u
        assert(error(value, expected, actual, "msb"), expected, actual)
    }

    @Test fun test1_7() {
        value = 0xFEED_BEEFu
        actual = immediate(value).lsb.ulong_z
        expected = 1u
        assert(error(value, expected, actual, "lsb"), expected, actual)
    }

    @Test fun test1_8() {
        value = 0xFEED_BEEFu
        actual = immediate(value).isNegative.ulong
        expected = 1u
        assert(error(value, expected, actual, "is negative"), expected, actual)
    }

    @Test fun test1_9() {
        value = 0xFEED_BEEFu
        actual = immediate(value).isNotNegative.ulong
        expected = 0u
        assert(error(value, expected, actual, "is not negative"), expected, actual)
    }

    @Test fun test1_10() {
        value = 0xFEED_BEEFu
        actual = immediate(value).isZero.ulong
        expected = 0u
        assert(error(value, expected, actual, "is zero"), expected, actual)
    }

    @Test fun test1_11() {
        value = 0xFEED_BEEFu
        actual = immediate(value).isNotZero.ulong
        expected = 1u
        assert(error(value, expected, actual, "is not zero"), expected, actual)
    }

    @Test fun test2_1() {
        value = 0x4A78u
        actual = immediate(value, WORD).ssext.ulong
        expected = 0x4A78uL
        assert(error(value, expected, actual, "ssext"), expected, actual)
    }

    @Test fun test2_2() {
        value = 0x4A78u
        actual = immediate(value, WORD).usext like DWORD
        expected = 0x4A78u
        assert(error(value, expected, actual, "usext"), expected, actual)
    }

    @Test fun test2_3() {
        value = 0x4A78u
        actual = immediate(value, WORD).value
        expected = 0x4A78u
        assert(error(value, expected, actual, "zext"), expected, actual)
    }

    @Test fun test2_4() {
        value = 0x4A78u
        actual = immediate(value, WORD).bit(6).ulong_z
        expected = 1u
        assert(error(value, expected, actual, "bit"), expected, actual)
    }

    @Test fun test2_5() {
        value = 0x4A78u
        actual = immediate(value, WORD).msb.ulong_z
        expected = 0u
        assert(error(value, expected, actual, "msb"), expected, actual)
    }

    @Test fun test2_6() {
        value = 0x4A78u
        actual = immediate(value, WORD).lsb.ulong_z
        expected = 0u
        assert(error(value, expected, actual, "lsb"), expected, actual)
    }

    @Test fun test2_7() {
        value = 0x4A78u
        actual = immediate(value, WORD).lsb.ulong_z
        expected = 0u
        assert(error(value, expected, actual, "lsb"), expected, actual)
    }

    @Test fun test2_8() {
        value = 0x4A78u
        actual = immediate(value, WORD).isNegative.ulong
        expected = 0u
        assert(error(value, expected, actual, "is negative"), expected, actual)
    }

    @Test fun test2_9() {
        value = 0x4A78u
        actual = immediate(value, WORD).isNotNegative.ulong
        expected = 1u
        assert(error(value, expected, actual, "is not negative"), expected, actual)
    }

    @Test fun test2_10() {
        value = 0x4A78u
        actual = immediate(value, WORD).isZero.ulong
        expected = 0u
        assert(error(value, expected, actual, "is zero"), expected, actual)
    }

    @Test fun test2_11() {
        value = 0x4A78u
        actual = immediate(value, WORD).isNotZero.ulong
        expected = 1u
        assert(error(value, expected, actual, "is not zero"), expected, actual)
    }
}