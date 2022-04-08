/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.enums.BreakpointType


@Suppress("INAPPLICABLE_JVM_NAME")
interface IDebugger {
    var isRunning: Boolean

    /**
     * {RU}
     * Получить состояние исключений процессора 'cpu'
     *
     * @return текущее состояние исключение процессора 'cpu'
     * {RU}
     */
    fun exception(): GeneralException?

    /**
     * {RU}
     * Загрузить массив байт размером [size] с заданного адреса [address]
     * с использованием подключенного отладчика 'debugger'
     *
     * @param address адрес начала загрузки
     * @param size количество байт для загрузки
     *
     * @return массив загруженных байт
     * {RU}
     */
    @JvmName("dbgLoad")
    fun dbgLoad(address: ULong, size: Int): ByteArray

    /**
     * {RU}
     * Сохранить массив байт [data] в заданный адрес [address]
     * с использованием подключенного отладчика 'debugger'
     *
     * @param address адрес начала загрузки
     * @param data массив байт для сохранения
     * {RU}
     */
    @JvmName("dbgStore")
    fun dbgStore(address: ULong, data: ByteArray)

    /**
     * {RU}
     * Установить точку останова в эмуляторе с помощью варианта режима доступа в виде GDB_BPT перечисления
     *
     * @param address адрес точки останова
     * @param bpType режим срабатывания точки останова (ACCESS - read или write, EXEC - execute)
     * @param comment комментарий к точки останова
     *
     * @return true - если точка останова была установлена
     * {RU}
     */
    @JvmName("bptSet")
    fun bptSet(bpType: BreakpointType, address: ULong, comment: String? = null): Boolean

    /**
     * {RU}
     * Удалить точку останова в эмуляторе
     *
     * @param address адрес точки останова
     *
     * @return true - если точка останова была снята
     * {RU}
     */
    @JvmName("bptClr")
    fun bptClr(address: ULong): Boolean

    /**
     * {RU}
     * Метод выполняет запуск процессора 'cpu' до тех пор пока не встретится точка останова
     * или не произойдет остановка процессора по причине исключения или необработанного прерывания
     * {RU}
     */
    fun cont(): Status

    /**
     * {RU}Метод выполняет один шаг процессора 'cpu'{RU}
     */
    fun step(): Boolean

    /**
     * {RU}
     * Метод возвращает идентификатор отладчика (схему поддерживаемых регистров)
     * Результат будет использоваться для ответа xmlRegisters в GDB RSP
     * {RU}
     */
    fun ident(): String

    /**
     * {RU}
     * Имя target.xml в ресурсах, если доступно. Данный файл определяет описание регистров.
     * {RU}
     */
    fun target(): String = throw NotImplementedError("target.xml name not available for this debugger")

    /**
     * {RU}Метод возвращает список значений всех регистров (в формате IDA Pro){RU}
     */
    fun registers(): List<ULong>

    /**
     * {RU}
     * Прочитать значение регистра процессора 'cpu' с индексом [index]
     *
     * @param index индекс регистра для чтения
     *
     * @return прочитанное значение регистра
     * {RU}
     */
    @JvmName("regRead")
    fun regRead(index: Int): ULong

    /**
     * {RU}
     * Записать значение регистра процессора 'cpu' с индексом [index]
     *
     * @param index индекс регистра для записи
     * @param value значение регистра для записи
     * {RU}
     */
    @JvmName("regWrite")
    fun regWrite(index: Int, value: ULong)

    /**
     * {RU}
     * Метод должен реализовывать запись значения isRunning в false
     * и ожидание остановки эмуляции в случае многопоточного выполнения
     * {RU}
     */
    fun halt()

    fun regSize(index: Int): Datatype

    fun sizes(): List<Datatype>
}

