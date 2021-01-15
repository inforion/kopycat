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
package ru.inforion.lab403.kopycat.modules.terminals

import ru.inforion.lab403.common.extensions.asByte
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.hex2
import ru.inforion.lab403.common.extensions.toLong
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.proposal.lazyTransient
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.MasterPort
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
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
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
    }

    inner class Ports : ModulePorts(this) {
        /**
         * {RU}Terminal slave/UART master{RU}
         */
        val term_s = Slave("term_s", UART_MASTER_BUS_SIZE)

        /**
         * {RU}
         * Terminal master/UART slave
         *
         * Должен быть обязательно подключен, если основному устройству нужно подтверждение отправки данных
         * {RU}
         */
        val term_m = Master("term_m", UART_SLAVE_BUS_SIZE, IGNORE)
    }

    final override val ports = Ports()

    /**
     * {RU}Сообщение мастеру о том, что данные были приняты из оконечного устройства{RU}
     */
    private fun dataReceivedRequest() = ports.term_m.write(UART_SLAVE_BUS_REQUEST, UART_SLAVE_DATA_RECEIVED, 1, 1)

    /**
     * {RU}Сообщение мастеру о том, что данные были пересланы терминалом в оконечное устройство{RU}
     */
    private fun dataTransmittedRequest() = ports.term_m.write(UART_SLAVE_BUS_REQUEST, UART_SLAVE_DATA_TRANSMITTED, 1, 1)

    private class LoggingBuffer(val name: String, val postfix: String): Serializable {
        private var buffer = StringBuffer()

        fun message(string: String) = log.info { "UART[$name] $postfix: $string" }

        fun add(byte: Byte) {
            val ch = byte.toChar()
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
        private val logBufferTx = LoggingBuffer(name, "send")
        private val logBufferRx = LoggingBuffer(name, "recv")

        override fun beforeRead(from: MasterPort, ea: Long) = terminalReceiveEnabled
        override fun beforeWrite(from: MasterPort, ea: Long, value: Long) = terminalTransmitEnabled

        override fun read(ea: Long, ss: Int, size: Int): Long {
            val byte = rxBuffer.poll(10, TimeUnit.MILLISECONDS)
                    ?: throw HardwareNotReadyException(core.pc, "UART[$name] reading empty UART -> rxBuffer underflow")

            log.finest { "UART[$name] receive byte ${byte.toChar()}[${byte.hex2}]" }

            logBufferRx.add(byte)

            if (!rxUnderflow)
                dataReceivedRequest()

            return byte.asULong
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            logBufferTx.add(value.asByte)

            log.finest { "UART[$name] transmit bytes ${value.toChar()}[${value.hex2}]" }

            if (!txBuffer.offer(value.asByte, 10, TimeUnit.MILLISECONDS))
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
        override fun read(ea: Long, ss: Int, size: Int) = when (ss) {
            UART_MASTER_ENABLE -> uartTerminalEnabled.toLong()
            UART_MASTER_RX_ENABLE -> terminalReceiveEnabled.toLong()
            UART_MASTER_TX_ENABLE -> terminalTransmitEnabled.toLong()
            UART_MASTER_RX_UNDERFLOW -> rxUnderflow.toLong()
            UART_MASTER_TX_OVERFLOW -> txOverflow.toLong()
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
        log.config { "Waiting for UART $tx thread stop..." }
        tx.join()
    }

    private fun getBlockingQueue(size: Int) = if (size < 0) LinkedBlockingQueue<Byte>(size) else LinkedBlockingQueue()

    private val rxBuffer = getBlockingQueue(receiveBufferSize)
    private val txBuffer = getBlockingQueue(transmitBufferSize)

    // thread start in initialize() section, this required to workaround binary serialization issue
    private val tx by lazyTransient {
        log.config { "Create transmitter UART terminal thread: '$this'" }
        thread(start = false, name = "$this") {
            runCatching {
                while (uartTerminalEnabled and terminalTransmitEnabled) {
                    val byte = txBuffer.poll(1000, TimeUnit.MILLISECONDS)
                    if (byte != null) onByteTransmitReady(byte)
                }
                log.finer { "$this finished main loop" }
            }.onFailure {
                log.severe { it.stackTraceToString() }
            }
        }
    }

    /**
     * {RU}Переменная указывает включен или нет терминал UART{RU}
     */
    var uartTerminalEnabled = true
        protected set

    /**
     * {RU}Переменная указывает могут приниматься данные через терминал UART{RU}
     */
    var terminalReceiveEnabled = true
        protected set

    /**
     * {RU}Переменная указывает могут передаваться данные через терминал UART{RU}
     */
    var terminalTransmitEnabled = true
        protected set

    /**
     * {RU}Свойство возвращает false, если в буфере приема есть данные{RU}
     */
    val rxUnderflow get() = rxBuffer.size == 0

    /**
     * {RU}Свойство возвращает true, если буфер отправки переполнен{RU}
     */
    val txOverflow get() = txBuffer.remainingCapacity() == 0

    /**
     * {RU}
     * Обработчик, вызываемый когда в буфере передачи данных есть данные,
     * которые должны быть отправлены в оконечное устройство (файл и т.п.)
     *
     * В данном обработчике должна быть реализована непосредствена отправка данных.
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
        rxBuffer.put(byte)
        dataReceivedRequest()
    }

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
        string.forEach { rxBuffer.put(it.asByte) }
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
        bytes.forEach { rxBuffer.put(it) }
        dataReceivedRequest()
    }

    private fun LinkedBlockingQueue<Byte>.read() = takeWhile { isNotEmpty() }.toByteArray()
    private fun LinkedBlockingQueue<Byte>.write(bytes: ByteArray) = bytes.forEach { put(it) }

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