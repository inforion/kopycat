package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.common.extensions.asByte
import ru.inforion.lab403.kopycat.cores.base.HardwareErrorHandler
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException

interface IReadable {
    /**
     * {RU}
     * Метод вызываются перед доступом к шине/порту на чтение.
     * В начале вызывается метод [beforeRead], после этого [read].
     * Если устройство в настоящее время недоступно, то метод может вернуть false
     *
     * @param from порт от которого пришел запрос на чтение
     * @param ea адрес по которому будет происходит чтение
     * {RU}
     */
    fun beforeRead(from: MasterPort, ea: Long): Boolean = true

    /**
     * {RU}
     * Метод описывает поведение при чтении данных [size] байтов из различных компонентов
     * эмулятора (шина, порт, регистр, область и т.д.) при доступе к указанному адресу [ss]:[ea].
     * Обычно это событие возникает при различных обращения с CPU к шине.
     * Для корректной работы должны быть перегружены.
     *
     * @param ea адрес с которого происходит чтение
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     * @param size количество байт, которое необходимо считать (должно быть меньше 16)
     *
     * @return считанные байты в endian-формате устройства, которое реализует чтение
     * {RU}
     */
    fun read(ea: Long, ss: Int, size: Int): Long

    /**
     * {RU}Метод используются для упрощения доступа на чтение.{RU}
     */
    fun read(dtyp: Datatype, ea: Long, ss: Int = 0) = read(ea, ss, dtyp.bytes)

    /**
     * {RU}
     * Метод описывает поведение при блочном чтении данных размером [size] из различных компонентов
     * эмулятора (шина, порт, регистр, область и т.д.) при доступе к указанному адресу [ss]:[ea].
     *
     * Данный метод используются отладчиком для чтения данных через отладочные механизмы.
     *
     * @param ea адрес по котормоу происходит чтение
     * @param size количество байт для чтения
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     * @param onError обработчик ошибки доступа к памяти, по умолчанию будет выброшено исключение [HardwareException]
     * {RU}
     */
    fun load(ea: Long, size: Int, ss: Int = 0, onError: HardwareErrorHandler? = null): ByteArray {
        val result = ByteArray(size)

        if (onError == null) {
            for (k in 0 until size)
                result[k] = read(ea + k, ss, 1).asByte
        } else {
            for (k in 0 until size) {
                try {
                    result[k] = read(ea + k, ss, 1).asByte
                } catch (error: HardwareException) {
                    result[k] = onError(error).asByte
                }
            }
        }

        return result
    }

    /**
     * {RU}
     * Прочитать один байт с указанного адреса [ss]:[ea]
     *
     * @param ea адрес по котормоу происходит чтение
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     *
     * @return прочитанный байт (1 байт)
     * {RU}
     */
    fun inb(ea: Long, ss: Int = 0): Long = read(BYTE, ea, ss)  // in byte

    /**
     * {RU}
     * Прочитать два байт с указанного адреса [ss]:[ea]
     *
     * @param ea адрес по котормоу происходит чтение
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     *
     * @return прочитанное полуслово (2 байт)
     * {RU}
     */
    fun inw(ea: Long, ss: Int = 0): Long = read(WORD, ea, ss)  // in word

    /**
     * {RU}
     * Прочитать четыре байт с указанного адреса [ss]:[ea]
     *
     * @param ea адрес по котормоу происходит чтение
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     *
     * @return прочитанное слово (4 байт)
     * {RU}
     */
    fun inl(ea: Long, ss: Int = 0): Long = read(DWORD, ea, ss)  // in long

    /**
     * {RU}
     * Прочитать восемь байт с указанного адреса [ss]:[ea]
     *
     * @param ea адрес по котормоу происходит чтение
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     *
     * @return прочитанное двойное слово (8 байт)
     * {RU}
     */
    fun inq(ea: Long, ss: Int = 0): Long = read(QWORD, ea, ss)  // in quad
}