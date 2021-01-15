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
package ru.inforion.lab403.kopycat.auxiliary

import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.proposal.toSerializable
import java.net.*
import java.util.logging.Level


abstract class ANetworkThread(
        val desiredPort: Int,
        name: String,
        val bufSize: Int = 1024,
        start: Boolean = false,
        isDaemon: Boolean = false) : Thread(name), AutoCloseable {

    companion object {
        @Transient private val log = logger(CONFIG)
    }

    private val server = ServerSocket()
    private var client: Socket? = null
    private var running = false

    init {
        this.isDaemon = isDaemon

        if (start) {
            this.bind()
            this.start()
        }
    }

    override fun start() {
        if (!isAlive) {
            running = true
            super.start()
        }
    }

    fun bind() {
        server.bind(InetSocketAddress(desiredPort))
    }

    val address: String get() = InetAddress.getLocalHost().hostAddress
    val port get() = server.localPort

    abstract fun onConnect(): Boolean
    abstract fun onReceive(data: ByteArray): Boolean
    open fun onDisconnect() = Unit

    override fun run() {
        while (running) {
            try {
                log.info { "$name waited for clients on [$address:$port]" }
                client = server.accept()
            } catch (e: SocketException) {
                log.info { "$name thread connection closed" }
                running = false
            }

            client?.let { client ->
                // See https://en.wikipedia.org/wiki/Nagle%27s_algorithm
                client.tcpNoDelay = true

                log.info { "Client $client connected to $name" }
                var connected = try {
                    onConnect()
                } catch (e: SocketException) {
                    log.info { "Client $client close connection with $name" }
                    false
                }

                val buf = ByteArray(bufSize)

                while (connected) {
                    try {
                        var bytes = client.inputStream.read(buf)
                        while (client.inputStream.available() > 0 && bytes < bufSize)
                            bytes += client.inputStream.read(buf, bytes, bufSize - bytes)

                        connected = if (bytes > 0) {
                            val data = ByteArray(bytes)
                            System.arraycopy(buf, 0, data, 0, bytes)
                            onReceive(data)
                        } else false
                    } catch (exc: SocketException) {
                        log.info { "$this -> $exc" }
                        connected = false
                    } catch (exc: Exception) {
                        log.severe { "$this -> $exc" }
                        exc.printStackTrace()
                        connected = false
                    }
                }

                log.info { "Client $client close connection with $name" }
                client.close()
                onDisconnect()
            }
        }
        log.info { "$name thread stopped" }
    }

    fun send(data: ByteArray) {
        // log.finer { "Send data: ${data.hexlify()}" }
        client?.outputStream?.write(data)
    }

    fun disconnect() {
        client?.close()
        if (this.isAlive) {
            this.join()
        }
    }

    override fun close() {
        running = false
        server.close()
        disconnect()
    }
}