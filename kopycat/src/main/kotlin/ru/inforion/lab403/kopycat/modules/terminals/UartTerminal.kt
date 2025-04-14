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
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package ru.inforion.lab403.kopycat.modules.terminals

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.iobuffer.BlockingCircularBytesIO
import ru.inforion.lab403.common.iobuffer.isEmpty
import ru.inforion.lab403.common.logging.logStackTrace
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.utils.lazyTransient
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.Port
import ru.inforion.lab403.kopycat.cores.base.abstracts.ATerminal
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts.ErrorAction.IGNORE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareNotReadyException
import ru.inforion.lab403.kopycat.modules.*
import java.io.Serializable
import java.lang.IllegalArgumentException
import kotlin.concurrent.thread

/**
 * {RU}
 *  Базовый терминал интерфейса UART при приеме и передаче данных логирует их.
 *
 *  Может быть использован в реализации терминалов для конкретных оконечных устройств.
 *
 *  Замечание: оконечное устройство - это файл, tty или tcp.
 *
 *  @param parent родительский модуль для данного модуля
 *  @param name имя инстанции модуля
 *  @param receiveBufferSize размер буфера приема данных от оконечного устройства к UART или -1 если неограниченно
 *  @param transmitBufferSize размер буфера передачи данных от UART к оконечному устройству или -1 если неограниченно
 *  {RU}
 */
open class UartTerminal(
    parent: Module,
    name: String,
    val receiveBufferSize: Int = 1024,
    val transmitBufferSize: Int = 1024
) : ATerminal(parent, name) {

    companion object {
        @Transient val log = logger()

        const val transmitterTimeout: Long = 10  // ms
        const val ioRegisterTimeout: Long = 10  // ms
    }

    inner class Ports : ModulePorts(this) {
        /**
         * {RU}Terminal slave/UART master{RU}
         */
        val term_s = Port("term_s")

        /**
         * {RU}
         * Terminal master/UART slave
         *
         * Должен быть обязательно подключен, если основному устройству нужно подтверждение отправки данных
         * {RU}
         */
        val term_m = Port("term_m", IGNORE)
    }

    final override val ports = Ports()

    /**
     * {RU}Сообщение мастеру о том, что данные были приняты из оконечного устройства{RU}
     */
    private fun dataReceivedRequest() = ports.term_m.write(UART_SLAVE_BUS_REQUEST, UART_SLAVE_DATA_RECEIVED, 1, 1u)

    /**
     * {RU}Сообщение мастеру о том, что данные были пересланы терминалом в оконечное устройство{RU}
     */
    private fun dataTransmittedRequest() = ports.term_m.write(UART_SLAVE_BUS_REQUEST, UART_SLAVE_DATA_TRANSMITTED, 1, 1u)

    private class LoggingBuffer(
        val name: String,
        val postfix: String,
        val core: AGenericCore
    ) : Serializable {
        private var buffer = StringBuffer()

        fun message(string: String) = log.info { "[0x${core.pc.hex}] UART[$name] $postfix: $string" }

        fun add(byte: Byte) {
            val ch = byte.char
            if (ch == '\r' || ch == '\n') {
                if (buffer.isNotBlank()) {
                    message(buffer.toString())
                    buffer = StringBuffer()
                }
            } else {
                buffer.append(ch)
            }
        }

        fun clear() = buffer.setLength(0)
    }

    private val REG_UART_DATA = object : Register(ports.term_s, UART_MASTER_BUS_DATA, BYTE, "REG_UART_DATA") {
        private val logBufferTx by lazy { LoggingBuffer(name, "send", core) }
        private val logBufferRx by lazy { LoggingBuffer(name, "recv", core) }

        override fun beforeRead(from: Port, ea: ULong, size: Int) = terminalReceiveEnabled
        override fun beforeWrite(from: Port, ea: ULong, size: Int, value: ULong) = terminalTransmitEnabled

        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            val byte = rxBuffer.poll(1, ioRegisterTimeout).firstOrNull()
                ?: throw HardwareNotReadyException(core.pc, "UART[$name] reading empty UART -> rxBuffer underflow")

            log.finest { "UART[$name] receive byte ${byte.char}[${byte.hex2}]" }

            logBufferRx.add(byte)

            if (!rxUnderflow)
                dataReceivedRequest()

            return byte.ulong_z
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            logBufferTx.add(value.byte)

            log.finest { "UART[$name] transmit bytes ${value.char}[${value.hex2}]" }

            if (txBuffer.offer(byteArrayOf(value.byte), timeout = ioRegisterTimeout) != 1)
                throw HardwareNotReadyException(core.pc, "UART[$name] writing full UART -> txBuffer overflow")

            dataTransmittedRequest()
        }

        override fun reset() {
            super.reset()
            logBufferTx.clear()
            logBufferRx.clear()
        }
    }

    private val REG_UART_PARAM = object : Register(ports.term_s, UART_MASTER_BUS_PARAM, DWORD, "REG_UART_PARAM") {
        override fun read(ea: ULong, ss: Int, size: Int) = when (ss) {
            UART_MASTER_ENABLE -> uartTerminalEnabled.ulong
            UART_MASTER_RX_ENABLE -> terminalReceiveEnabled.ulong
            UART_MASTER_TX_ENABLE -> terminalTransmitEnabled.ulong
            UART_MASTER_RX_UNDERFLOW -> rxUnderflow.ulong
            UART_MASTER_TX_OVERFLOW -> txOverflow.ulong
            else -> throw GeneralException("Unknown UART[$name] parameter requested: $ss")
        }
    }

    override fun initialize(): Boolean {
        if (!super.initialize()) return false
        if (!tx.isAlive) tx.start()
        return true
    }

    override fun reset() {
        super.reset()
        rxBuffer.clear()
        txBuffer.clear()
    }

    override fun terminate() {
        super.terminate()
        uartTerminalEnabled = false
        terminalReceiveEnabled = false
        terminalTransmitEnabled = false
        // don't wait while thread closed due to high performance degradation
    }

    private val rxBuffer = BlockingCircularBytesIO(receiveBufferSize)
    private val txBuffer = BlockingCircularBytesIO(transmitBufferSize)

    // thread start in initialize() section, this required to workaround binary serialization issue
    private val tx by lazyTransient {
        log.config { "Create transmitter UART terminal thread: '$this'" }
        thread(start = false, name = "$this") {
            runCatching {
                while (uartTerminalEnabled && terminalTransmitEnabled) {
                    txBuffer
                        .poll(1, transmitterTimeout)
                        .firstOrNull()
                        .ifItNotNull {
                            onByteTransmitReady(it)
                        }
                }
                log.finer { "$this finished main loop" }
            }.onFailure {
                it.logStackTrace(log)
            }
        }
    }

    /**
     * {RU}Переменная указывает включен или нет терминал UART{RU}
     */
    var uartTerminalEnabled = true
        protected set

    /**
     * {RU}Переменная указывает, что могут приниматься данные через терминал UART{RU}
     */
    var terminalReceiveEnabled = true
        protected set

    /**
     * {RU}Переменная указывает, что могут передаваться данные через терминал UART{RU}
     */
    var terminalTransmitEnabled = true
        protected set

    /**
     * {RU}Свойство возвращает false, если в буфере приема есть данные{RU}
     */
    val rxUnderflow get() = rxBuffer.readAvailable == 0

    /**
     * {RU}Свойство возвращает true, если буфер отправки переполнен{RU}
     */
    val txOverflow get() = txBuffer.writeAvailable == 0

    /**
     * {RU}
     * Обработчик, вызываемый когда в буфере передачи данных есть данные,
     * которые должны быть отправлены в оконечное устройство (файл и т.п.)
     *
     * В данном обработчике должна быть реализована непосредственна отправка данных.
     *
     * @param byte байт данных для отправки в оконечное устройство или файл
     * {RU}
     */
    open fun onByteTransmitReady(byte: Byte) = Unit

    /**
     * {RU}
     * Функция должна быть вызвана, когда был получен байт данных от оконечного устройства
     *
     * @param byte байт данных полученный от оконечного устройства
     * {RU}
     */
    fun write(byte: Byte) {
        rxBuffer.put(byteArrayOf(byte))
        dataReceivedRequest()
    }

    /**
     * {RU}
     * Функция должна быть вызвана, когда был получен байт данных от оконечного устройства
     *
     * @param char символ данных полученный от оконечного устройства
     * {RU}
     */
    fun write(char: Char) = write(char.byte)

    /**
     * {EN}
     * Function to transmit to UART a string of bytes
     *
     * @since 0.3.2
     *
     * @param string String to add to transmit buffer
     * {EN}
     */
    fun write(string: String) {
        rxBuffer.put(string.bytes)
        dataReceivedRequest()
    }

    /**
     * {EN}
     * Function to transmit to UART an array of bytes
     *
     * @since 0.3.3
     *
     * @param bytes Byte array to add to transmit buffer
     * {EN}
     */
    fun write(bytes: ByteArray) {
        rxBuffer.put(bytes)
        dataReceivedRequest()
    }

    /**
     * Non-blocking Nullable read
     */
    fun read(count: Int = 1): Byte? = try {
        rxBuffer.read(count).getOrNull(0)
    } catch (e: IllegalArgumentException) {
        null
    }

    fun writeFromDev(char: Char) = writeFromDev(char.byte)
    fun writeFromDev(byte: Byte) = writeFromDev(byteArrayOf(byte))
    fun writeFromDev(string: String) = writeFromDev(string.bytes)
    fun writeFromDev(bytes: ByteArray) {
        txBuffer.put(bytes)
        dataTransmittedRequest()
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        require(rxBuffer.isEmpty()) { "Can't serialize UartTerminal if any data not flushed!" }
        require(txBuffer.isEmpty()) { "Can't serialize UartTerminal if any data not flushed!" }
        return emptyMap()
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        rxBuffer.clear()
        txBuffer.clear()
    }
}