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

import org.jetbrains.kotlin.backend.common.pop
import ru.inforion.lab403.common.extensions.hexlify
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONotConnected
import ru.inforion.lab403.kopycat.veos.exceptions.io.IONotReadyError
import ru.inforion.lab403.kopycat.veos.filesystems.interfaces.ISocketFile
import java.net.InetSocketAddress


class PseudoSocketFile(val port: Int, val isClient: Boolean = false) : ISocketFile {

    companion object {
        val log = logger(INFO)
    }

    override val address get() = InetSocketAddress(port)

    val isServer get() = !isClient

    private var desc = -1

    // emulate of stream to be fully serializable
    private var read = 0

    private var sent = byteArrayOf()
    private var received = byteArrayOf()

    private var inputShutdown = false
    private var outputShutdown = false

    private val remain get() = sent.size - read

    private val acceptors = mutableListOf<ISocketFile>()

    inner class Control {
        fun flush() {
            check(isClient) { "Can't flush data for server socket"}
            sent = byteArrayOf()
            received = byteArrayOf()
        }

        fun append(data: ByteArray) {
            check(isClient) { "Can't add data to server socket" }
            sent += data
        }

        fun get(): ByteArray {
            check(isClient) { "Can't add data to server socket" }
            return received.copyOf()
        }

        fun acceptor(port: Int): PseudoSocketFile {
            check(isServer) { "Can't add acceptor to client socket" }
            return PseudoSocketFile(port, true).also { acceptors.add(it) }
        }
    }

    @DontAutoSerialize
    val control = Control()

    override fun bind(address: InetSocketAddress) {
        check(isServer) { "Bind non-server socket forbidden" }
        log.fine { "[${this.address}]: Bind to address: $address" }
    }

    override fun listen(backlog: Int): Int {
        check(isServer) { "Listen for non-server socket forbidden" }
        log.fine { "[$address]: Listen on port ${address.port}" }
        return address.port
    }

    override fun accept(nonblocking: Boolean): ISocketFile {
        check(isServer) { "Accept for non-server socket forbidden" }
        check(acceptors.isNotEmpty() || nonblocking) { "Unexpected accept call" }
        if (acceptors.isEmpty()) // && nonblocking
            throw IONotReadyError(desc)
        log.fine { "[$address]: Accepted client ${acceptors.first().address}" }
        return acceptors.pop()
    }

    override fun read(data: ByteArray): Int {
        check(isClient) { "Can't read data from server socket" }
        val size = data.size.coerceAtMost(remain)
        sent.copyInto(data, 0, read, read + size)
        log.fine { "[$address]: read -> ${data.take(size).toByteArray().hexlify()}" }
        read += size
        return size
    }

    override fun write(data: ByteArray) {
        check(isClient) { "Can't write data to server socket" }
        log.fine { "[$address]: write -> ${data.hexlify()}" }
        received += data
    }

    override fun available() = if (isClient) remain else 0

    override fun readable() = if (isClient) (remain > 0 && !inputShutdown) else acceptors.isNotEmpty()

    override fun writable() = isClient && !outputShutdown

    override fun open(fd: Int) = run { desc = fd }

    override fun shutdown(read: Boolean, write: Boolean) {
        if (isServer) throw IONotConnected(desc)
        if (read) inputShutdown = true
        if (write) outputShutdown = true
    }

    override fun close() {
        desc = -1
        log.fine { "[$address]: Close connection" }
    }
}