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
package ru.inforion.lab403.kopycat.cores.arm.support

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.inforion.lab403.common.extensions.MHz
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.modules.cores.ARMv7Core
import kotlin.test.assertEquals


class ARMBankTest: Module(null, "ARM Bank Test") {
    private fun assert(expected: ULong, actual: ULong) {
        assertEquals(expected, actual)
    }

    private val arm = ARMv7Core(this, "arm", 48.MHz, 1.0)
    private val bank = arm.cpu.sregs.cpsr

    @BeforeEach
    fun resetTest() {
        arm.reset()
        bank.reset()
    }

    @Test fun test1() {
        bank.ITSTATE = 0b1010_1010u
        // initial mode for ARM is SVC mode CPSR[7..0] = 0x13
        assert(0b0000_0100_0000_0000_1010_1000_0001_0011u, arm.cpu.sregs.cpsr.value)
        assertEquals(bank.ITSTATE, 0b1010_1010uL)
    }

    @Test fun test2() {
        bank.ge = 0b0110u
        bank.m  = 0b10011u
        bank.ITSTATE = 0b1110_0011u
        bank.f  = true
        assert(0b0000_0110_0000_0110_1110_0000_0101_0011u, arm.cpu.sregs.cpsr.value)
        assert(0b0000_0110_0000_0110_1110_0000_0101_0011u, arm.cpu.sregs.cpsr.value)
        assertEquals(bank.ge,0b0110uL)
        assertEquals(bank.m, 0b10011uL)
        assertEquals(bank.ITSTATE,0b1110_0011uL)
        assertEquals(bank.f, true)
    }
}