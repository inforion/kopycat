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
package ru.inforion.lab403.kopycat.cores.base.operands

import org.junit.jupiter.api.Test
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.modules.cores.device.ATest

class MemoryTest: ATest() {
    private fun error(value: ULong, expected: ULong, actual: ULong, test: String): String =
            error(value, expected, actual, "memory", test)

    @Test fun test1_1() {
        address = 0x8_AB56u
        value = 0xDEAD_BEEFu
        store(address, value)
        expected = 0xDEAD_BEEFu
        actual = memory(address).value(testCore)
        assert(error(address, expected, actual, "value"), expected, actual)
    }

    @Test fun test1_2() {
        address = 0x8_AB56u
        value = 0xDEAD_BEEFu
        store(address, value)
        expected = 1u
        actual = memory(address, atyp = WORD).isZero(testCore).ulong
        assert(error(address, expected, actual, "value"), expected, actual)
    }

    @Test fun test2_1() {
        address = 0x8_AB56u
        value = 0x4A78u
        store(address, value)
        actual = memory(address).usext(testCore)
        expected = 0x4A78uL
        assert(error(value, expected, actual, "ssext"), expected, actual)
    }

    @Test fun test2_2() {
        address = 0x8_AB56u
        value = 0x4A78u
        store(address, value)
        actual = memory(address).value(testCore)
        expected = 0x4A78u
        assert(error(value, expected, actual, "usext"), expected, actual)
    }

    @Test fun test2_3() {
        address = 0x8_AB56u
        value = 0x4A78u
        store(address, value)
        actual = memory(address).zext(testCore)
        expected = 0x4A78u
        assert(error(value, expected, actual, "zext"), expected, actual)
    }

    @Test fun test2_4() {
        address = 0x8_AB56u
        value = 0x4A78u
        store(address, value)
        actual = memory(address).bit(testCore, 6).ulong_z
        expected = 1u
        assert(error(value, expected, actual, "bit"), expected, actual)
    }

    @Test fun test2_5() {
        address = 0x8_AB56u
        value = 0x4A78u
        store(address, value)
        actual = memory(address).msb(testCore).ulong_z
        expected = 0u
        assert(error(value, expected, actual, "msb"), expected, actual)
    }

    @Test fun test2_6() {
        address = 0x8_AB56u
        value = 0x4A78u
        store(address, value)
        actual = memory(address).lsb(testCore).ulong_z
        expected = 0u
        assert(error(value, expected, actual, "lsb"), expected, actual)
    }

    @Test fun test2_7() {
        address = 0x8_AB56u
        value = 0x4A78u
        store(address, value)
        actual = memory(address).lsb(testCore).ulong_z
        expected = 0u
        assert(error(value, expected, actual, "lsb"), expected, actual)
    }
}