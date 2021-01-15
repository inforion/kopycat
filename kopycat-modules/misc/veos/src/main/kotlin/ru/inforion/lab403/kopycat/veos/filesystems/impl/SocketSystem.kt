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
package ru.inforion.lab403.kopycat.veos.filesystems.impl

import org.jetbrains.kotlin.utils.sure
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.veos.exceptions.io.IOAddressInUse
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONotSocket
import ru.inforion.lab403.kopycat.veos.filesystems.TCPServerSocketFile
import ru.inforion.lab403.kopycat.veos.filesystems.interfaces.ISocketFile
import ru.inforion.lab403.kopycat.veos.kernel.System
import java.net.InetSocketAddress


class SocketSystem(val sys: System): IAutoSerializable {

    companion object {
        val log = logger(INFO)
    }

    // TODO: UDP

    private val virtualSocketByPort = mutableMapOf<Int, ISocketFile>()
    private val virtualSocketByName = mutableMapOf<String, ISocketFile>()

    private val bindPorts = mutableMapOf<Int, Int>()

    fun addVirtualSocket(name: String, file: ISocketFile) {
        val port = file.address.port
        check(port !in bindPorts) { "Socket with port '$port' already bind!" }
        check(port !in virtualSocketByPort) { "Socket with port '$port' already register!" }
        virtualSocketByPort[port] = file
        virtualSocketByName[name] = file
    }

    fun getVirtualSocketByName(name: String) = virtualSocketByName[name].sure { "Unknown virtual socket with name '$name'" }

    fun socket(sock: Int) = sys.ioSystem.descriptor(sock) as? ISocketFile ?: throw IONotSocket(sock)

    fun socketByPort(port: Int): ISocketFile? {
        val sock = bindPorts[port] ?: return null
        return socket(sock)
    }

    // posix compatibility layer

    fun socket(): Int {
        val file = TCPServerSocketFile()
        return sys.ioSystem.register(file).also {
            log.fine { "socket() -> $it" }
            file.open(it)
        }
    }

    fun bind(sockfd: Int, address: InetSocketAddress) {
        check(address.port != 0) { "Can't listen on port == 0!" }

        if (address.port in bindPorts) {
            throw IOAddressInUse(sockfd)
        }

        val virtual = virtualSocketByPort[address.port]

        val file = if (virtual != null) {
            sys.ioSystem.close(sockfd)
            sys.ioSystem.reserve(virtual, sockfd)
            virtual.also { it.open(sockfd) }  // TODO: old fd may erased
        } else socket(sockfd)

        val internalAddress = if (sys.conf.dynamicPortMapping) InetSocketAddress(0) else address
        file.bind(internalAddress)

        bindPorts[address.port] = sockfd
    }

    fun connect(sockfd: Int, address: InetSocketAddress) {
        TODO("Not Implemented: not forget about virtualSockets")
    }

    fun listen(sockfd: Int, backlog: Int = 50) {
        val port = socket(sockfd).listen(backlog)
        log.info { "Listening on port $port" }
    }

    fun accept(sockfd: Int): Pair<Int, InetSocketAddress> {
        val nonblocking = sys.ioSystem.isNonBlocking(sockfd)
        val server = socket(sockfd)
        val client = server.accept(nonblocking)
        val fd = sys.ioSystem.register(client)
        log.fine { "accept() -> fd = $fd inet = ${client.address}" }
        client.open(fd)
        return fd to client.address
    }

    fun recv(sockfd: Int, len: Int) = sys.ioSystem.read(sockfd, len)

    fun send(sockfd: Int, data: ByteArray) = sys.ioSystem.write(sockfd, data)

    fun shutdown(sockfd: Int, read: Boolean, write: Boolean) = socket(sockfd).shutdown(read, write)

    fun close(sockfd: Int) = sys.ioSystem.close(sockfd)

    fun reset() = Unit
}