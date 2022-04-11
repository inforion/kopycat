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

import ru.inforion.lab403.common.extensions.byte
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.kopycat.cores.base.HardwareErrorHandler
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import java.math.BigInteger

@Suppress("INAPPLICABLE_JVM_NAME")
interface IReadable {
    /**
     * {RU}
     * Метод вызывается перед доступом к шине/порту на чтение.
     * В начале вызывается метод [beforeRead], после этого [read].
     * Если устройство в настоящее время недоступно, то метод может вернуть false
     *
     * @param from порт от которого пришел запрос на чтение
     * @param ea адрес по которому будет происходить чтение
     * {RU}
     */
    @JvmName("beforeRead")
    fun beforeRead(from: MasterPort, ea: ULong): Boolean = true

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
    @JvmName("read")
    fun read(ea: ULong, ss: Int, size: Int): ULong

    /**
     * {RU}
     * Метод описывает поведение при блочном чтении данных размером [size] из различных компонентов
     * эмулятора (шина, порт, регистр, область и т.д.) при доступе к указанному адресу [ss]:[ea].
     *
     * Данный метод используется отладчиком для чтения данных через отладочные механизмы.
     *
     * @param ea адрес по которому происходит чтение
     * @param size количество байт для чтения
     * @param ss дополнительная часть адреса (может быть использована как segment selector)
     * @param onError обработчик ошибки доступа к памяти, по умолчанию будет выброшено исключение [HardwareException]
     * {RU}
     */
    @JvmName("load")
    fun load(ea: ULong, size: Int, ss: Int = 0, onError: HardwareErrorHandler? = null): ByteArray {
        val result = ByteArray(size)

        if (onError == null) {
            for (k in 0 until size)
                result[k] = read(ea + k.uint, ss, 1).byte
        } else {
            for (k in 0 until size) {
                try {
                    result[k] = read(ea + k.uint, ss, 1).byte
                } catch (error: HardwareException) {
                    result[k] = onError(error).byte
                }
            }
        }

        return result
    }
}