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

import ru.inforion.lab403.common.extensions.buffers.BlockingCircularBytesIO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.veos.filesystems.interfaces.ISocketFile
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread

class TCPClientSocketFile(private val client: Socket) : ISocketFile {
    companion object {
        val log = logger()

        const val circularCapacity = 1024 * 1024
        const val socketReadSize = 1024
    }

    private var desc = -1
    private var aborted = false

    private val inputStream = client.getInputStream()
    private val outputStream = client.getOutputStream()

    private val buffer = BlockingCircularBytesIO(circularCapacity)

    override val address = client.localSocketAddress as InetSocketAddress

    override fun read(data: ByteArray) = buffer.poll(data.size).also { it.copyInto(data) }.size

    override fun write(data: ByteArray) {
        outputStream.runCatching { write(data) }
    }

    override fun available() = buffer.readAvailable

    override fun readable() = !aborted && !client.isInputShutdown && available() > 0

    override fun writable() = !aborted && !client.isOutputShutdown

    override fun open(fd: Int) {
        desc = fd
        val tmp = ByteArray(socketReadSize)
        thread {
            while (!client.isClosed) {
                val count = try {
                    inputStream.read(tmp)
                } catch (error: IOException) {
                    break
                }
                if (count == -1) break
                buffer.put(tmp, 0, count)
            }
            aborted = true
        }
    }

    override fun shutdown(read: Boolean, write: Boolean) {
        if (read) client.shutdownInput()
        if (write) client.shutdownOutput()
    }

    override fun close() {
        desc = -1
        inputStream.close()
        outputStream.close()
        client.close()
    }
}