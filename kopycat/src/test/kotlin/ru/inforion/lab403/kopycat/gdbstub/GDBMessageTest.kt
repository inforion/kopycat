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
package ru.inforion.lab403.kopycat.gdbstub

import org.junit.Assert
import org.junit.Test
import ru.inforion.lab403.common.extensions.hex2



class GDBMessageTest {
    private fun msgTest(msg: String, expectedCRC: String) {
        val newCRC = GDBMessage.calcChecksum(msg).hex2
        val error = "GDBMessage CRCTest expected $expectedCRC, but got $newCRC for message $msg"
        Assert.assertEquals(error, expectedCRC, newCRC)
    }

    private fun buildMsgTest(msg: String, expectedMsg: String) {
        val newMsg = GDBMessage.message(msg).toString()
        val error = "GDBMessage buildMessageTest expected $expectedMsg, but got $newMsg"
        Assert.assertEquals(error, expectedMsg, newMsg)
    }

    @Test fun crcTest() {
        msgTest("T02", "B6") // interrupt
        msgTest("c", "63") // process continue
        msgTest("Z00,1000,1", "04") // set bpt
        msgTest("z00,1000,1", "24") // clr bpt
        msgTest("s", "73") // do step
        msgTest("g", "67") // read all registers
        msgTest("p0", "A0") // read register
        msgTest("p000000000000000000000000", "F0") // read register
        msgTest("p00=12345678", "B1") // write register
        msgTest("m00000000,12345678", "BD") // read memory
        msgTest(0x03.toString(), "33") // process suspend
        msgTest("?", "3F") // halt reason
    }

    @Test fun buildMessageTest() {
        buildMsgTest("T02", "\$T02#B6")
        buildMsgTest("m00000000,12345678", "\$m00000000,12345678#BD")
        buildMsgTest("Z00,1000,1", "\$Z00,1000,1#04")
        buildMsgTest("p000000000000000000000000", "\$p000000000000000000000000#F0")
    }
}

