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
package ru.inforion.lab403.kopycat.modules.terminals

import ru.inforion.lab403.common.extensions.int_z
import ru.inforion.lab403.common.network.Network
import ru.inforion.lab403.kopycat.cores.base.common.Module
import java.io.Closeable
import java.io.OutputStream

/**
 * {RU}
 * @param parent родительский модуль
 * @param name произвольное имя объекта модуля
 * @param port номер TCP порта
 * @param host имя хоста, к которому будет привязан сервер
 * @param bufferSize размер буфера
 * @param dummy если `true`, то не открывает никаких сокетов
 * {RU}
 *
 * {EN}
 * @param parent parent module
 * @param name module name
 * @param port desired TCP port
 * @param host host name to bind to
 * @param bufferSize buffer size
 * @param dummy does not open any sockets if `true`
 * {EN}
 */
class UartNetworkTerminal(
    parent: Module,
    name: String,
    port: Int? = null,
    host: String = "127.0.0.1",
    bufferSize: Int = 1024,
    dummy: Boolean = false,
) : UartTerminal(parent, name), Closeable {
    companion object {
        const val DEFAULT_PORT = 64130
    }

    val port = port ?: DEFAULT_PORT
    private var outputStream: OutputStream? = null
    private val network = if (!dummy) {
        Network("$name-network", this.port, host, bufferSize, 1).onConnect {
            this@UartNetworkTerminal.outputStream = outputStream
            true
        }.onReceive { bytes ->
            bytes.forEach { write(it) }
            true
        }.onDisconnect {
            this@UartNetworkTerminal.outputStream = null
        }
    } else {
        null
    }

    override fun onByteTransmitReady(byte: Byte) {
        outputStream?.write(byte.int_z)
    }

    override fun close() = network?.close() ?: Unit

    override fun terminate() {
        super.terminate()
        close()
    }
}
