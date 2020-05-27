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
package ru.inforion.lab403.kopycat.modules.stm32f042

import org.junit.Test
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.kopycat.modules.stm32f042.GPIOx.RegisterType
import kotlin.test.expect


abstract class GPIOxTest(register: RegisterType) {
    val u1 = GPIOxModule(register).also { it.initializePortsAndBuses() }

    @Test
    fun shellPass() {
//        made just to whole class look like a test
    }
}

abstract class GPIOxTestWithLock(register: RegisterType) : GPIOxTest(register) {
    @Test
    fun modeOfLockedPinCantChange() {
        u1.write(0x0000_0001)
        expect(0x0000_0001) { u1.read() }

        u1.setLock()
        expect(0x0000_0001) { u1.read() }

        u1.write(0x0000_0002)
        expect(0x0000_0001) { u1.read() }
    }

    @Test
    fun modeLockResetTest() {
        u1.reset()
        u1.write(0x0000_0001)
        u1.setLock()
        expect(0x0000_0001) { u1.read() }

        u1.reset()
        u1.write(0x0000_0002)
        u1.setLock()
        expect(0x0000_0002) { u1.read() }
    }

    @Test
    fun lockWriteNotAffectToModeChange() {
        u1.write(0x0000_0001)
        expect(0x0000_0001) { u1.read() }

        u1.writeLockValues()
        expect(0x0000_0001) { u1.read() }

        u1.write(0x0000_0002)
        expect(0x0000_0002) { u1.read() }

    }
}

class GPIOx_MODER_Test : GPIOxTestWithLock(RegisterType.MODER)

class GPIOx_OTYPER_Test : GPIOxTestWithLock(RegisterType.OTYPER)

class GPIOx_OSPEEDR_Test : GPIOxTestWithLock(RegisterType.OSPEEDR)

class GPIOx_PUPDR_Test : GPIOxTestWithLock(RegisterType.PUPDR)

class GPIOx_IDR_Test : GPIOxTest(RegisterType.IDR) {
    @Test
    fun inputSetTest() {
        u1.ioWrite(0x0000_0005)
        expect(0x0000_0005) { u1.read() }
    }
}

class GPIOx_BSRR_Test : GPIOxTest(RegisterType.BSRR) {
    @Test
    fun setResetTest() {
        u1.write(0x0000_FFFE)
        expect(0x0000_FFFE) { u1.memRead(0x14) }

        u1.write(0xE000_0001)
        expect(0x0000_1FFF) { u1.memRead(0x14) }
    }
}

class GPIOx_LCKR_Test : GPIOxTest(RegisterType.LCKR) {
    @Test
    fun lockSetTest() {
        expect(0L) { u1.read()[16] }
        u1.setLock()
        expect(1L) { u1.read()[16] }
    }

    @Test
    fun lockNoChangeTest() {
        u1.setLock()

        u1.write(0x00011110)
        expect(0b0000_0000_0000_0001__0000_0000_0000_0011) { u1.read() }
    }

    @Test
    fun lockResetTest() {
        u1.setLock()
        u1.reset()
        expect(0L) { u1.read()[16] }

        u1.setLock()
        expect(1L) { u1.read()[16] }
    }
}

class GPIOx_AFRL_Test : GPIOxTestWithLock(RegisterType.AFRL)

class GPIOx_AFRH_Test : GPIOxTestWithLock(RegisterType.AFRH)

class GPIOx_BRR_Test : GPIOxTest(RegisterType.BRR) {
    @Test
    fun resetTest() {
        u1.memWrite(0x14, 0x0000_FFFF)
        u1.write(0x0000_0001)
        expect(0x0000_FFFE) { u1.memRead(0x14) }
    }
}