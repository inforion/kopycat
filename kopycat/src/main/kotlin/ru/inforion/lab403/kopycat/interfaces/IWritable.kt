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
package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.cores.base.HardwareErrorHandler
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError


interface IWritable {
    /**
     * {RU}
     * Метод вызываются перед доступом к шине/порту на запись.
     * В начале вызывается метод [beforeWrite], после этого [write].
     * Если устройство в настоящее время недоступно, то метод может вернуть false
     *
     * @param from порт от которого пришел запрос на запись
     * @param ea адрес по которому будет происходит запись
     * @param value значение, которое будет записано
     * {RU}
     */
    fun beforeWrite(from: MasterPort, ea: Long, value: Long): Boolean = true

    /**
     * {RU}
     * Метод описывает поведение при записи данных [size] байтов в различные компоненты
     * эмулятора (шина, порт, регистр, область и т.д.) при доступе к указанному адресу [ss]:[ea].
     * Обычно это событие возникает при различных обращения с CPU к шине.
     *
     * @param ea адрес по котормоу происходит запись
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     * @param size количество байт, которое необходимо записать (должно быть меньше 16)
     * @param value записываемое значение
     * {RU}
     */
    fun write(ea: Long, ss: Int, size: Int, value: Long)

    /**
     * {RU}Метод используются для упрощения доступа на запись.{RU}
     */
    fun write(dtyp: Datatype, ea: Long, value: Long, ss: Int = 0) = write(ea, ss, dtyp.bytes, value)

    /**
     * {RU}
     * Метод описывает поведение при записи блока данных из буфера [data] в различные компоненты
     * эмулятора (шина, порт, регистр, область и т.д.) при доступе к указанному адресу [ss]:[ea].
     *
     * Данный метод используются отладчиком для записи данных через отладочные механизмы.
     *
     * @param ea адрес по котормоу происходит запись
     * @param data байты для записи по указанному адрес
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     * @param onError обработчик ошибки доступа к памяти, по умолчанию будет выброшено исключение [MemoryAccessError]
     * {RU}
     */
    fun store(ea: Long, data: ByteArray, ss: Int = 0, onError: HardwareErrorHandler? = null) {
        if (onError == null) {
            for (k in data.indices)
                write(ea + k, ss, 1, data[k].asULong)
        } else {
            for (k in data.indices) {
                try {
                    write(ea + k, ss, 1, data[k].asULong)
                } catch (error: HardwareException) {
                    onError(error)
                }
            }
        }
    }

    /**
     * {RU}
     * Записать один байт данных [value] в указанный адрес [ss]:[ea]
     *
     * @param ea адрес по котормоу происходит запись
     * @param value записываемое значение
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     * {RU}
     */
    fun outb(ea: Long, value: Long, ss: Int = 0) = write(BYTE, ea, value, ss)  // out byte

    /**
     * {RU}
     * Записать два байта данных [value] в указанный адрес [ss]:[ea]
     *
     * @param ea адрес по котормоу происходит запись
     * @param value записываемое значение
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     * {RU}
     */
    fun outw(ea: Long, value: Long, ss: Int = 0) = write(WORD, ea, value, ss)  // out word

    /**
     * {RU}
     * Записать четыре байта данных [value] в указанный адрес [ss]:[ea]
     *
     * @param ea адрес по котормоу происходит запись
     * @param value записываемое значение
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     * {RU}
     */
    fun outl(ea: Long, value: Long, ss: Int = 0) = write(DWORD, ea, value, ss)  // out long

    /**
     * {RU}
     * Записать восемь байт данных [value] в указанный адрес [ss]:[ea]
     *
     * @param ea адрес по котормоу происходит запись
     * @param value записываемое значение
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     * {RU}
     */
    fun outq(ea: Long, value: Long, ss: Int = 0) = write(QWORD, ea, value, ss)  // out quad
}