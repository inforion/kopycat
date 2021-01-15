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
package ru.inforion.lab403.kopycat.veos.mips.lighttpd

import org.junit.Test
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.modules.veos.MIPSApplication
import ru.inforion.lab403.kopycat.veos.VEOS
import ru.inforion.lab403.kopycat.veos.filesystems.PseudoSocketFile
import java.io.File
import java.net.Socket
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.text.Charsets.ISO_8859_1



// TODO: use single Test class and repo
internal class MipsLighttpd {
    companion object {
        val log = logger()

        const val maxStepsCount = 6000000
        const val fdeventPollAddress = 0x00404988L
        const val failAddress = 0x0043AEC8L
        const val connectionCloseAddress = 0x00407C08L
        const val httpOk = "HTTP/1.0 200 OK\r\n"
    }

    fun constructTop(): MIPSApplication {
        val executable = "bin/lighttpd"

        // IDK but getResourceUrl for folder got classes folder not resource :(
        val root = File(getResourceUrl("bin").toURI()).parent

        return MIPSApplication(
                null,
                "top",
                root,
                executable,
                "-D -f /etc/lighttpd/lightttpd.default.conf"
        ).also { it.veos.conf.dynamicPortMapping = true }
    }

    @Test
    fun lighttpdVeosTestVulnerability() {
        val top = constructTop()
        val kopycat = Kopycat(null).also { it.open(top, false, null) }

        var socket: Socket? = null
        val data = "GET / HTTP/1.1\r\n${"Connection: keep-alive\r\n"*2} ${"a"*128}\r\n\r\n".convertToBytes()
        kopycat.run { step, core ->
            if (core.pc == fdeventPollAddress && socket == null) {
                val tcpSocket = top.veos.network.socketByPort(80)
                assertNotNull(tcpSocket)

                socket = Socket("localhost", tcpSocket.address.port).also { it.outputStream.write(data) }
                log.warning { "Don't worry about the following exception. It's expected" }
            }

            step < maxStepsCount
        }
        assertTrue { top.veos.state == VEOS.State.Exception }
        assertEquals(failAddress, top.core.pc)
    }

    @Test
    fun lighttpdVeosTestGet() {
        val top = constructTop()
        val kopycat = Kopycat(null).also { it.open(top, false, null) }

        var socket: Socket? = null
        val data = "GET / HTTP/1.0\r\n\r\n".convertToBytes()
        var found = false
        kopycat.run { step, core ->
            if (core.pc == fdeventPollAddress && socket == null) {
                val tcpSocket = top.veos.network.socketByPort(80)
                assertNotNull(tcpSocket)

                socket = Socket("localhost", tcpSocket.address.port)
                log.info { "Send data to $socket" }

                socket!!.outputStream.write(data)
            }
            if (socket != null) {
                val inputStream = socket!!.inputStream
                if (inputStream.available() > 1000) { // To receive body of response
                    val response = inputStream.readNBytes(inputStream.available()).convertToString()
                    log.info { response }
                    assertTrue { response.startsWith(httpOk) }
                    found = true
                }
            }

            step < maxStepsCount && !found
        }
        assertTrue { found }
    }

    @Test
    fun lighttpdVeosTestSnapshot() {
        val tempDir = createTempDir()
        val top = constructTop()
        val kopycat = Kopycat(null).also {
            it.setSnapshotsDirectory(tempDir.absolutePath)
            it.open(top, false, null)
        }

        val server = PseudoSocketFile(80)
        top.veos.network.addVirtualSocket("server", server)

        kopycat.run { step, core ->
            step < maxStepsCount && core.pc != fdeventPollAddress
        }

        assertEquals(fdeventPollAddress, kopycat.pcRead())
        assertFalse { kopycat.hasException() }

        kopycat.save()
        kopycat.restore()

        val acceptor = (top.veos.network.getVirtualSocketByName("server") as PseudoSocketFile).control.acceptor(1234)
        acceptor.control.append("GET / HTTP/1.0\r\n\r\n".convertToBytes())

        kopycat.run { step, core ->
            step < maxStepsCount && core.pc != connectionCloseAddress
        }

        assertEquals(connectionCloseAddress, kopycat.pcRead())
        assertFalse { kopycat.hasException() }
        assertTrue { acceptor.control.get().convertToString().startsWith(httpOk) } // TODO: bug: too early access
    }
}