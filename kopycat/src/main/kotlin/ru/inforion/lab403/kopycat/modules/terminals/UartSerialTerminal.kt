package ru.inforion.lab403.kopycat.modules.terminals

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import ru.inforion.lab403.common.extensions.asUInt
import ru.inforion.lab403.common.extensions.hex2
import ru.inforion.lab403.kopycat.cores.base.common.Module
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 *  @param parent родительский модуль для данного модуля
 *  @param name имя инстанции модуля
 *  @param tty путь к устройству (терминалу) /dev/tty
 *          Чтобы создать псевдо-порт необходимо выполнить команду в Linux:
 *           socat -d -d pty,raw,echo=0 pty,raw,echo=0
 *
 *          тогда терминал один будет создан например в /dev/ttys002, а терминал два в /dev/ttys003
 *          они будут полностью связаны, то есть запись в терминал один будет передана в терминал
 *          два и наоборот.
 *
 *         Чтобы автоматически создать виртуальный порт с помощью socat необходимо указать имя терминала tty:
 *          socat:/dev/ttyMyName
 *         Тогда будет создан внутренний терминал для эмулятора с именнем /dev/ttyMyName_in и внешний для подключения
 *          /dev/ttyMyName <- к этому устройство можно подключаться извне.
 *         Если же указать только socat: то путь будет выбран автоматически
 *
 *  @param baudRate скорость передачи данных реального порта
 *  @param parity четность реального порта
 *  @param numStopBits количество стоповых битов
 *  @param numDataBits количество битов данных
 */
class UartSerialTerminal(
        parent: Module,
        name: String,
        val tty: String?,
        val baudRate: Int,
        val parity: Int,
        val numStopBits: Int,
        val numDataBits: Int
) : UartTerminal(parent, name) {
    constructor(parent: Module, name: String, tty: String?) : this(parent, name, tty, 115200, SerialPort.NO_PARITY, SerialPort.ONE_STOP_BIT, 8)

    data class Socat(val process: Process, val pty0: String, val pty1: String) {
        companion object {
            private const val SOCAT_PREFIX = "socat:"

            /**
             * {RU}
             * Парсит строку вида:
             *  2018/12/13 10:21:14 socat[9302] N PTY is /dev/ttys000
             * и извлекает из нее путь к созданому терминалу
             *
             * @param line строка вывода socat
             *
             * @return путь к терминалу
             * {RU}
             */
            private fun parseSocatOutput(line: String): String {
                val sign = "N PTY is "

                val tmp = line.split(sign)
                if (tmp.size < 2)
                    throw RuntimeException("Socat output is invalid: $line")

                return tmp[1]
            }

            /**
             * {RU}
             * Создает процесс socat и возвращает псевдо-терминалы для обмена данными
             *
             * @param url путь к tty устройству, который нужно создать с префиксом "socat:" иначе будет возвращен null
             *
             * @return класс Socat, в который включены терминалы и сам процесс
             * {RU}
             */
            fun createPseudoTerminal(module: Module, url: String): Socat? {
                if (!url.startsWith(SOCAT_PREFIX))
                    return null

                val tty = url.substringAfter(SOCAT_PREFIX)

                val osName = System.getProperty("os.name")
                if (osName.toLowerCase().startsWith("win")) {
                    throw NotImplementedError("Automatic creation of virtual terminal for Windows systems not supported!")
                }

                val comm = if (tty.isNotBlank())
                    "socat -d -d pty,raw,echo=0,link=${tty}_in pty,raw,echo=0,link=$tty"
                else
                    "socat -d -d pty,raw,echo=0 pty,raw,echo=0"

                val runtime = Runtime.getRuntime()
                val process = runtime.exec(comm.split(" ").toTypedArray())

                // wait if any errors occurred
                process.waitFor(100, TimeUnit.MILLISECONDS)

                val reader = process.errorStream.bufferedReader()

                if (!process.isAlive) {
                    val error = reader.readLine()
                    throw RuntimeException("Command execution failed:\n$comm\n$error")
                }

                val pty0 = parseSocatOutput(reader.readLine())
                val pty1 = parseSocatOutput(reader.readLine())

                log.warning { "Pseudo-terminals created for $module: $pty0 and $pty1" }

                Runtime.getRuntime().addShutdownHook(thread(false) {
                    log.info { "Stop forcibly Socat: $process" }
                    process.destroyForcibly()
                })

                return Socat(process, pty0, pty1)
            }
        }
    }

    /**
     * {RU}
     * Функция пытается открыть реальный ком-порт, если имя не задано
     * или если произошла ошибка при открытии, то выбрасывается исключениеь.
     *
     * @param name имя реального com-порта
     * @param baudRate реальная скорость порта
     * @param parity четность реального порта
     * @param numStopBits количество стоп-бит реального порта
     * @param numDataBits количество дата-бит реального порта
     * {RU}
     */
    private fun openComPort(
            name: String,
            baudRate: Int,
            parity: Int = SerialPort.NO_PARITY,
            numStopBits: Int = SerialPort.ONE_STOP_BIT,
            numDataBits: Int = 8
    ): SerialPort {
        val comPort = SerialPort.getCommPort(name)
        comPort.baudRate = baudRate
        comPort.parity = parity
        comPort.numStopBits = numStopBits
        comPort.numDataBits = numDataBits

        if (!comPort.openPort()) {
            throw RuntimeException("Can't open port: $name")
        }

        val listener = object : SerialPortDataListener {
            override fun getListeningEvents() = SerialPort.LISTENING_EVENT_DATA_AVAILABLE

            override fun serialEvent(event: SerialPortEvent) {
                if (event.eventType != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    return
                val available = comPort.bytesAvailable()
                if (available < 0)
                    return
                val newData = ByteArray(available)
                comPort.readBytes(newData, newData.size.toLong())
                newData.forEach { addByteToReceiveBuffer(it) }
            }
        }

        comPort.addDataListener(listener)

        return comPort
    }

    /**
     * {EN}
     * Be careful serial output stream is blocking on write also. When write data to this stream it can block it
     * forever. Possible when data not read from another side of stream (i.e. in case of socat)
     * {EN}
     */
    override fun onByteTransmitReady(byte: Byte) {
        comPort?.outputStream?.write(byte.asUInt)
        log.finest { "$this comPort byte ${byte.toChar()}[${byte.hex2}] written" }
    }

    val socat = if (tty != null) Socat.createPseudoTerminal(this, tty) else null

    private val terminal = socat?.pty0 ?: tty
    private val comPort = if (terminal != null)
        openComPort(terminal, baudRate, parity, numStopBits, numDataBits)
    else {
        log.severe { "Terminal wasn't opened for $this tty=$tty..." }
        null
    }
}