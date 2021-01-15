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
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.modules.cores.device.ATest

class DisplacementTest: ATest() {
    private fun error(value: Long, expected: Long, actual: Long, test: String): String =
            error(value, expected, actual, "displacement", test)

    @Test fun test1_1() {
        actual = displacement(register(0), immediate(0)).value(testCore)
        assert(error(value, expected, actual, "empty mem"), expected, actual)
    }

    @Test fun test1_2() {
        address = 0x1000
        value = 0xBEEF
        expected = 0xFFFF_BEEF
        store(address, value, WORD)
        regs(r0 = address)
        actual = displacement(register(0), immediate(0), WORD).usext(testCore)
        assert(error(address, expected, actual, "imm zero"), expected, actual)
    }

    @Test fun test1_3() {
        val sub = 0xFFL
        address = 0x1000
        expected = 0xBEEF
        store(address, expected, WORD)
        regs(r0 = address - sub)
        actual = displacement(register(0), immediate(sub), WORD).value(testCore)
        assert(error(address, expected, actual, "count address"), expected, actual)
    }

    @Test fun test2_1() {
        val sub = 0x1_7FACL
        address = 0x8_AB56
        expected = 0xDEAD_BEEF
        store(address, expected)
        regs(r1 = address - sub)
        actual = displacement(register(1), immediate(sub)).value(testCore)
        assert(error(address, expected, actual, "count address 2"), expected, actual)
    }

    @Test fun test2_2() {
        val sub = 0x1_7FACL
        address = 0x8_AB56
        value = 0xDEAD_BEEF
        store(address, value)
        regs(r1 = address - sub)
        expected = -559038737
        actual = displacement(register(1), immediate(sub)).ssext(testCore)
        assert(error(address, expected, actual, "ssext"), expected, actual)
    }

    @Test fun test2_3() {
        val sub = 0x1_7FACL
        address = 0x8_AB56
        value = 0xDEAD_BEEF
        store(address, value)
        regs(r1 = address - sub)
        expected = 0xDEAD_BEEF
        actual = displacement(register(1), immediate(sub)).usext(testCore)
        assert(error(address, expected, actual, "usext"), expected, actual)
    }

    @Test fun test2_4() {
        val sub = 0x1_7FACL
        address = 0x8_AB56
        value = 0xDEAD_BEEF
        store(address, value)
        regs(r1 = address - sub)
        expected = 0xDEAD_BEEF
        actual = displacement(register(1), immediate(sub)).zext(testCore)
        assert(error(address, expected, actual, "zext"), expected, actual)
    }

    @Test fun test2_5() {
        val sub = 0x1_7FACL
        address = 0x8_AB56
        value = 0xDEAD_BEEF
        store(address, value)
        regs(r1 = address - sub)
        expected = 0xDEAD_BEF0
        val displacement = displacement(register(1), immediate(sub))
        displacement.inc(testCore)
        actual = displacement.value(testCore)
        assert(error(address, expected, actual, "inc"), expected, actual)
    }

    @Test fun test2_6() {
        val sub = 0x1_7FACL
        address = 0x8_AB56
        value = 0xDEAD_BEEF
        store(address, value)
        regs(r1 = address - sub)
        expected = 0xDEAD_BEEE
        val displacement = displacement(register(1), immediate(sub))
        displacement.dec(testCore)
        actual = displacement.value(testCore)
        assert(error(address, expected, actual, "dec"), expected, actual)
    }

    @Test fun test3_1() {
        val sub1 = 0x1_7FACL
        val address1 = 0x8_AB56L
        value = 0xDEAD_0000
        store(address1, value)
        val sub2 = 0xFAL
        val address2 = 0x8_BB56L
        value = 0xBEEF
        store(address2, value)
        regs(r0 = address1 - sub1, r1 = address2 - sub2)
        expected = 0xDEAD_BEEF
        val displacement1 = displacement(register(0), immediate(sub1))
        val displacement2 = displacement(register(1), immediate(sub2))
        displacement1.plus(testCore, displacement2.value(testCore))
        actual = displacement1.value(testCore)
        assert(error(address, expected, actual, "plus"), expected, actual)
    }

    @Test fun test3_2() {
        val sub1 = 0x1_7FACL
        val address1 = 0x8_AB56L
        value = 0xDEAD_BEEF
        store(address1, value)
        val sub2 = 0xFAL
        val address2 = 0x8_BB56L
        value = 0xBEEF
        store(address2, value)
        regs(r0 = address1 - sub1, r1 = address2 - sub2)
        expected = 0xDEAD_0000
        val displacement1 = displacement(register(0), immediate(sub1))
        val displacement2 = displacement(register(1), immediate(sub2))
        displacement1.minus(testCore, displacement2.value(testCore))
        actual = displacement1.value(testCore)
        assert(error(address, expected, actual, "minus"), expected, actual)
    }
}