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

import com.fazecast.jSerialComm.SerialPort
import com.fazecast.jSerialComm.SerialPortDataListener
import com.fazecast.jSerialComm.SerialPortEvent
import ru.inforion.lab403.common.extensions.asUInt
import ru.inforion.lab403.common.extensions.hex2
import ru.inforion.lab403.kopycat.auxiliary.Socat
import ru.inforion.lab403.kopycat.cores.base.common.Module

/**
 * {RU}
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
 * {RU}
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
    constructor(parent: Module, name: String, tty: String?) :
            this(parent, name, tty, 115200, SerialPort.NO_PARITY, SerialPort.ONE_STOP_BIT, 8)

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

        check(comPort.openPort()) { "Can't open port: $name" }

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
                newData.forEach { write(it) }
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