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
package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.HardwareErrorHandler
import ru.inforion.lab403.kopycat.cores.base.Port
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError


@Suppress("INAPPLICABLE_JVM_NAME")
interface IWritable {
    /**
     * {RU}
     * Метод вызывается перед доступом к шине/порту на запись.
     * В начале вызывается метод [beforeWrite], после этого [write].
     * Если устройство в настоящее время недоступно, то метод может вернуть false
     *
     * @param from порт от которого пришел запрос на запись
     * @param ea адрес по которому будет происходить запись
     * @param size количество байт, которое необходимо записать
     * @param value значение, которое будет записано
     * {RU}
     */
    @JvmName("beforeWrite")
    fun beforeWrite(from: Port, ea: ULong, size: Int, value: ULong): Boolean = true

    /**
     * {RU}
     * Метод описывает поведение при записи данных [size] байтов в различные компоненты
     * эмулятора (шина, порт, регистр, область и т.д.) при доступе к указанному адресу [ss]:[ea].
     * Обычно это событие возникает при различных обращения с CPU к шине.
     *
     * @param ea адрес по которому происходит запись
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     * @param size количество байт, которое необходимо записать (должно быть меньше 16)
     * @param value записываемое значение
     * {RU}
     */
    @JvmName("write")
    fun write(ea: ULong, ss: Int, size: Int, value: ULong)

    /**
     * {RU}
     * Метод описывает поведение при записи блока данных из буфера [data] в различные компоненты
     * эмулятора (шина, порт, регистр, область и т.д.) при доступе к указанному адресу [ss]:[ea].
     *
     * Данный метод используется отладчиком для записи данных через отладочные механизмы.
     *
     * @param ea адрес по которому происходит запись
     * @param data байты для записи по указанному адрес
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     * @param onError обработчик ошибки доступа к памяти, по умолчанию будет выброшено исключение [MemoryAccessError]
     * {RU}
     */
    @JvmName("store")
    fun store(ea: ULong, data: ByteArray, ss: Int = 0, onError: HardwareErrorHandler? = null) {
        if (onError == null) {
            for (k in data.indices)
                write(ea + k.uint, ss, 1, data[k].ulong_z)
        } else {
            for (k in data.indices) {
                try {
                    write(ea + k.uint, ss, 1, data[k].ulong_z)
                } catch (error: HardwareException) {
                    onError(error)
                }
            }
        }
    }
}