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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ru.inforion.lab403.common.logging.logger

internal class ElfAccessTest {
    companion object {
        private val log = logger()
    }

    val elfR = ElfAccess(1)
    val elfW = ElfAccess(2)
    val elfRW = ElfAccess(3)
    val elfX = ElfAccess(4)
    val elfRX = ElfAccess(5)
    val elfRWX = ElfAccess(7)
    val elfLoad = ElfAccess(8)

    @Test fun testR() {
        assertTrue(elfR.isRead)
    }

    @Test fun testW() {
        assertTrue(elfW.isWrite)
    }

    @Test fun testRW() {
        assertTrue(elfRW.isRead && elfRW.isWrite)
    }

    @Test fun testX() {
        assertTrue(elfX.isExec)
    }

    @Test fun testRX() {
        assertTrue(elfRX.isRead && elfRX.isExec)
    }

    @Test fun testRWX() {
        assertTrue(elfRWX.isRead && elfRWX.isWrite && elfRWX.isExec)
    }

    @Test fun testLoad() {
        assertTrue(elfLoad.isLoad)
    }

    @Test fun testfromSectionHeader_RW() {
        val elfAcc = ElfAccess.fromSectionHeaderFlags(1)
        assertTrue(elfAcc.isRead && elfAcc.isWrite)
    }

    @Test fun testfromSectionHeader_RWX() {
        val elfAcc = ElfAccess.fromSectionHeaderFlags(5)
        assertTrue(elfAcc.isRead && elfAcc.isWrite && elfAcc.isExec)
    }

    @Test fun testfromSectionHeader_WLoad() {
        val elfAcc = ElfAccess.fromSectionHeaderFlags(3)
        assertTrue(elfAcc.isRead && elfAcc.isWrite && elfAcc.isLoad)
    }

    @Test fun testfromProgramHeader_RX() {
        val elfAcc = ElfAccess.fromProgramHeaderFlags(5)
        assertTrue(elfAcc.isRead && elfAcc.isExec)
    }

    @Test fun testfromProgramHeader_RW() {
        val elfAcc = ElfAccess.fromProgramHeaderFlags(6)
        assertTrue(elfAcc.isRead && elfAcc.isWrite)
    }

}