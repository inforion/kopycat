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
package ru.inforion.lab403.kopycat.modules.stm32f042

import ru.inforion.lab403.common.extensions.asByte
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.emptyString
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.modules.*
import ru.inforion.lab403.kopycat.modules.terminals.UartTerminal
import java.util.concurrent.TimeoutException
import java.util.logging.Level

class BT(parent: Module, name: String) : Module(parent, name) {

    companion object {
        @Transient private val log = logger(Level.ALL)

        private class Buffer(capacity: Int) {
            private val buffer: ByteArray = ByteArray(capacity) { 0 }
            private var length = 0

            fun add(byte: Byte) {
                buffer[length++] = byte
            }

            fun reset() {
                length = 0
                (0.until(buffer.size)).forEach { buffer[it] = 0 }
            }

            fun length(): Int = length
            fun isFull(): Boolean = buffer.size == length

            fun contentToString(): String = String(buffer, 0, length)
            override fun toString(): String = String(buffer)
        }

        private enum class BluetoothReceiveCommand {
            ShowConnection,
            SPPDisconnect,
            DefaultLocalName,
            Config,
            EnableBond,
        }

        private enum class BluetoothTransmitCommand {
            SPPConnectionClosed,
            LocalNameOk,
            BondEnabled,
            ConnectionUp
        }

        private fun String.removeCRSuffix(): String = this.removeSuffix("\r")
    }

    inner class Ports : ModulePorts(this) {

        val usart_m = Proxy("usart_m", UART_MASTER_BUS_SIZE)
        val usart_s = Proxy("usart_s", UART_SLAVE_BUS_SIZE)

        val bt_m = Master("bt_m", UART_MASTER_BUS_SIZE)
        val bt_s = Slave("bt_s", UART_SLAVE_BUS_SIZE)
    }

    override val ports = Ports()

    @Volatile
    private var isTransmitToUsart = false

    private val buffer = Buffer(64)
    private var isCommandMode = false
    private var isConnected = false
    private val connection = "0 4cb199dccd22 Connected IAP"
    private var defaultLocalName = "BT module v1.0"

    private fun btTermWrite(byte: Byte): Unit = ports.bt_m.write(UART_MASTER_BUS_DATA, 0, 1, byte.asULong)
    private fun btTermRead(): Byte = ports.bt_m.read(UART_MASTER_BUS_DATA, 0, 1).asByte
    private fun btTermRxUnderflow(): Boolean = ports.bt_m.read(UART_MASTER_BUS_PARAM, UART_MASTER_RX_UNDERFLOW, 0).toBool()

    private fun usartWrite(timeoutMillis: Long = 10000, checkRate: Long = 100, transmit: (Unit) -> Unit) {
        val waitStarted = System.currentTimeMillis()
        while (isTransmitToUsart) {
            if ((System.currentTimeMillis() - waitStarted) >= timeoutMillis) {
                throw TimeoutException("Timed out after [$timeoutMillis]")
            }
            Thread.sleep(checkRate)
        }
        isTransmitToUsart = true
        transmit.invoke(Unit)
        isTransmitToUsart = false
    }

    val bluetoothTerminalProxy = object : Register(
            ports.bt_s,
            UART_SLAVE_BUS_REQUEST,
            Datatype.DWORD,
            "TERMINAL_REQUEST_REG",
            readable = false,
            level = Level.SEVERE
    ) {
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            when (ss) {
                UART_SLAVE_DATA_RECEIVED -> {
                    dataReceived()
                }
            }
        }

        private fun dataReceived() {
            usartWrite {
                while (!btTermRxUnderflow()) {
                    bluetoothProxy.write(btTermRead())
                }
            }
        }
    }

    private val bluetoothProxy = object : UartTerminal(this@BT, "${this@BT.name} AT&AB proxy") {
        override fun onByteTransmitReady(byte: Byte) {
            buffer.add(byte)

//            log.info("get byte [$byte]")
//            log.info("buffer: [$buffer]")
//            log.info("buffer: [${buffer.contentToString()}]")

            btTermWrite(byte)
            processMessage(buffer.contentToString())
        }

        private fun processMessage(msg: String) {
            processBluetoothCommand(msg)

            if (msg.endsWith("\r") || msg.endsWith("\n") || msg.endsWith("\r\n") || msg.endsWith("\n\r")) {
                buffer.reset()
            }

        }

        private fun processBluetoothCommand(msg: String) {
            when (msg) {
                """^#^$^%""" -> {
                    isCommandMode = true
                    respondCommand("-CommandMode-")
                    buffer.reset()
                }
                """@#@$@%""" -> {
                    isCommandMode = false
                    respondCommand("RemoteMode.")
                    buffer.reset()
                }
            }

            if (isCommandMode && msg.startsWith("AT+AB") && msg.endsWith('\r')) {

                val chunks = msg.removeCRSuffix().split(" ")
                val command = chunks[1]
                if (BluetoothReceiveCommand.values().any { it.name == command }) {
                    when (BluetoothReceiveCommand.valueOf(command)) {

                        BluetoothReceiveCommand.ShowConnection -> {
                            if (isConnected) {
                                respond(connection)
                            } else {
                                respond("No Device Connected")
                            }
                        }
                        BluetoothReceiveCommand.SPPDisconnect -> {
                            isConnected = false
                            respondCommand(BluetoothTransmitCommand.SPPConnectionClosed)
                        }
                        BluetoothReceiveCommand.DefaultLocalName -> {
                            defaultLocalName = chunks[2]
                            respondCommand(BluetoothTransmitCommand.LocalNameOk)
                        }
                        BluetoothReceiveCommand.Config -> {
                            respond(emptyString)
                        }
                        BluetoothReceiveCommand.EnableBond -> {
                            respondCommand(BluetoothTransmitCommand.BondEnabled)
                            respondCommand(BluetoothTransmitCommand.ConnectionUp)
                            respondCommand("-BypassMode-")
                        }
                    }
                }
            }
        }

        private fun respondCommand(command: String) {
            respond("AT-AB $command")
        }

        private fun respondCommand(command: BluetoothTransmitCommand) {
            respondCommand(command.name)
        }

        private fun respond(msg: String) {
            usartWrite {
                msg.toByteArray().forEach { byte ->
                    this.write(byte)
                }
                this.write('\r'.toByte())
            }
        }
    }

    init {
        buses.connect(ports.usart_m, bluetoothProxy.ports.term_s)
        buses.connect(ports.usart_s, bluetoothProxy.ports.term_m)
    }

}