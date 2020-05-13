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

