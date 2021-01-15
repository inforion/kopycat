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
package ru.inforion.lab403.kopycat.veos.filesystems

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.veos.exceptions.io.IOConnectionAborted
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONotConnected
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONotReadyError
import ru.inforion.lab403.kopycat.veos.filesystems.interfaces.ISocketFile
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread


class TCPServerSocketFile : ISocketFile {
    companion object {
        val log = logger()
    }

    private var desc = -1

    private var server: ServerSocket? = null
    private val backlogQueue = LinkedBlockingQueue<Socket>()
    private var aborted = false

    override lateinit var address: InetSocketAddress
        private set

    private fun blockingAccept() = TCPClientSocketFile(backlogQueue.take())

    private fun nonblockingAccept(): TCPClientSocketFile {
        if (aborted) throw IOConnectionAborted(desc)
        val client = backlogQueue.poll() ?: throw IONotReadyError(desc)
        return TCPClientSocketFile(client)
    }

    override fun accept(nonblocking: Boolean) = run {
        checkNotNull(this) { "Socket must be bind() before call accept()!" }
        if (nonblocking) nonblockingAccept() else blockingAccept()
    }

    override fun bind(address: InetSocketAddress) {
        server = ServerSocket()
        this.address = address
    }

    override fun listen(backlog: Int) = with (server) {
        checkNotNull(this) { "Socket must be bind() before call listen()!" }
        bind(address, backlog)
        // reset internalAddress to real acquired address if InetAddress(0) was specified (i.e. dynamic)
        address = localSocketAddress as InetSocketAddress

        thread {
            while (!isClosed) {
                val socket = try {
                    accept()
                } catch (error: IOException) {
                    log.warning { "Connection seems aborted for server socket fd=$desc" }
                    aborted = true
                    break
                }
                backlogQueue.add(socket)
            }
        }

        address.port
    }

    override fun read(data: ByteArray) = throw IONotConnected(desc)

    override fun available() = 0

    override fun readable() = backlogQueue.isNotEmpty()

    override fun writable() = false

    override fun open(fd: Int) = run { desc = fd }

    override fun shutdown(read: Boolean, write: Boolean) = throw IONotConnected(desc)

    override fun close() {
        desc = -1
        server?.close()
    }
}