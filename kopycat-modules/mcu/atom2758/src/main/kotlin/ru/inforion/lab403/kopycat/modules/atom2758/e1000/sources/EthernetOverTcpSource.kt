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
package ru.inforion.lab403.kopycat.modules.atom2758.e1000.sources

import ru.inforion.lab403.common.logging.WARNING
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.modules.atom2758.e1000.*
import ru.inforion.lab403.kopycat.modules.atom2758.e1000.Dissection
import java.io.InputStream
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

@Suppress("unused")
class EthernetOverTcpSource(host: String, port: Int, private val bufSize: Int = 1024) : IPacketSource {
    companion object {
        @Transient
        val log = logger(WARNING)
    }

    private val socket = Socket(host, port)
    private val packetCache = ArrayList<Byte>()

    override val started get() = packetThread != null

    private var packetThread: Thread? = null
    private var stopThread = AtomicBoolean(false)

    private fun threadSocketReadInner(e1000: E1000, inputStream: InputStream, buf: ByteArray) {
        val bytes = inputStream.read(buf)
        if (bytes > 0) {
            packetCache.addAll(buf.asSequence().take(bytes))

            var dissection = Dissection.dissect(packetCache)
            while (dissection != null) {
                val packet = packetCache.subList(0, dissection.fullSize)

                e1000.receive(ArrayList(packet), dissection)
                while (e1000.receiveBacklogFull) {
                    Thread.sleep(100)
                }

                packet.clear()
                dissection = Dissection.dissect(packetCache)
            }
        }
    }

    override fun start(e1000: E1000) {
        if (started) {
            throw PacketSourceAlreadyStartedException(this)
        }
        log.info { "Starting the EthernetOverTcpSource thread" }
        stopThread.set(false);

        packetThread = thread(true, name = "eth-over-tcp") {
            val buf = ByteArray(bufSize)
            val inputStream = socket.inputStream

            while (!stopThread.get()) {
                threadSocketReadInner(e1000, inputStream, buf)
            }
        }
        log.info { "Started the EthernetOverTcpSource thread" }
    }

    override fun stop(e1000: E1000) {
        if (!started) {
            throw PacketSourceNotStartedException(this)
        }
        log.info { "Stopping the EthernetOverTcpSource thread" }
        stopThread.set(true)
        packetThread?.join()
        log.info { "Stopped the EthernetOverTcpSource thread" }

        packetThread = null
    }

    override fun send(packet: ArrayList<Byte>) {
        if (!started) {
            throw PacketSourceNotStartedException(this)
        }
        socket.outputStream.write(packet.toByteArray())
    }
}
