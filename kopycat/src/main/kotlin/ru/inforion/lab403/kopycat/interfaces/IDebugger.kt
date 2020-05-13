package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.gdbstub.GDB_BPT


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
    fun dbgLoad(address: Long, size: Int): ByteArray

    /**
     * {RU}
     * Сохранить массив байт [data] в заданный адрес [address]
     * с использованием подключенного отладчика 'debugger'
     *
     * @param address адрес начала загрузки
     * @param data массив байт для сохранения
     * {RU}
     */
    fun dbgStore(address: Long, data: ByteArray)

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
    fun bptSet(bpType: GDB_BPT, address: Long, comment: String? = null): Boolean

    /**
     * {RU}
     * Удалить точку останова в эмуляторе
     *
     * @param address адрес точки останова
     *
     * @return true - если точка останова была снята
     * {RU}
     */
    fun bptClr(address: Long): Boolean

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
     * {RU}Метод возращает список значений всех регистров (в формате IDA Pro){RU}
     */
    fun registers(): List<Long>

    /**
     * {RU}
     * Прочитать значение регистра процессора 'cpu' с индексом [index]
     *
     * @param index индекс регистра для чтения
     *
     * @return прочитанное значение регистра
     * {RU}
     */
    fun regRead(index: Int): Long

    /**
     * {RU}
     * Записать значение регистра процессора 'cpu' с индексом [index]
     *
     * @param index индекс регистра для записи
     * @param value значение регистра для записи
     * {RU}
     */
    fun regWrite(index: Int, value: Long)

    /**
     * {RU}
     * Метод должен реализовывать запись значения isRunning в false
     * и ожидание остановки эмуляции в случае многопоточного выполнения
     * {RU}
     */
    fun halt()
}

