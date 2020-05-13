package ru.inforion.lab403.kopycat.modules.terminals

import ru.inforion.lab403.common.extensions.asByte
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.hex2
import ru.inforion.lab403.common.extensions.toLong
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.abstracts.ATerminal
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts.ErrorAction.IGNORE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.BYTE
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Level.*
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

    private class LoggingBuffer(val name: String, val postfix: String) {
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
        private var logBufferTx = LoggingBuffer(name, "send")
        private var logBufferRx = LoggingBuffer(name, "recv")

        override fun beforeRead(from: MasterPort, ea: Long): Boolean = terminalReceiveEnabled && !rxUnderflow
        override fun beforeWrite(from: MasterPort, ea: Long, value: Long): Boolean = terminalTransmitEnabled && !txOverflow

        override fun read(ea: Long, ss: Int, size: Int): Long {
            // Небольшой gap на всякий случай, должен быть не null, так как читать этот регистр можно
            // только если выставлен флаг в ISR-регистре, что в RDR что-то есть
            val byte = rxBuffer.poll(10, TimeUnit.MILLISECONDS)
                    ?: throw GeneralException("UART[$name] reading empty UART -> something went totally wrong...")

            log.finest { "UART[$name] receive byte ${byte.toChar()}[${byte.hex2}]" }

            logBufferRx.add(byte)

            if (!rxUnderflow)
                dataReceivedRequest()

            return byte.asULong
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            logBufferTx.add(value.asByte)

            log.finest { "UART[$name] transmit bytes ${value.toChar()}[${value.hex2}]" }
            // Небольшой gap на всякий случай, offer должен вернуть true, так как писать этот регистр можно
            // только если выставлен флаг в ISR-регистре, что в TDR передача завершена
            if (!txBuffer.offer(value.asByte, 10, TimeUnit.MILLISECONDS))
                throw GeneralException("UART[$name] writing full UART -> something went totally wrong...")
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
        log.warning { "Waiting for UART $tx thread stop..." }
        tx.join()
    }

    private fun getBlockingQueue(size: Int) = if (size < 0) LinkedBlockingQueue<Byte>(size) else LinkedBlockingQueue()

    private val rxBuffer = getBlockingQueue(receiveBufferSize)
    private val txBuffer = getBlockingQueue(transmitBufferSize)

    private val tx = thread(name="$this") {
        try {
            while (uartTerminalEnabled and terminalTransmitEnabled) {
                val byte = txBuffer.poll(1000, TimeUnit.MILLISECONDS)
                if (byte != null) {
                    onByteTransmitReady(byte)
                    dataTransmittedRequest()
                }
            }
            log.finer { "$this finished main loop" }
        } catch (exc: Throwable) {
            exc.printStackTrace()
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
    fun addByteToReceiveBuffer(byte: Byte) {
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
    fun addStringToReceiveBuffer(string: String) {
        string.forEach { rxBuffer.put(it.asByte) }
        dataReceivedRequest()
    }
}