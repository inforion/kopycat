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
package ru.inforion.lab403.kopycat.cores.base.common

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.common.Breakpoint.Access.*
import java.io.Serializable


/**
 * {RU}
 * Базовый класс точки останова.
 * Программные и аппаратные точки останова для процесса отладки.
 *
 *
 * @property range Интервал адресов точки останова
 * @property access Тип активации точки доступа
 * @property comment необязательный комментарий
 * @property onBreak Обработчик срабатывания точки останова
 * {RU}
 *
 * {EN}
 * Class for breakpoint
 * Software and hardware breakpoint for debug purposes
 *
 * @property range breakpoint address range
 * @property access breakpoint activation access type
 * @property comment optional comment
 * @property onBreak function to process on breakpoint activation
 * {EN}
 */
data class Breakpoint(
    val range: ULongRange,
    val access: Access,
    val comment: String? = null,
    val onBreak: ((ea: ULong) -> Unit)?
): Serializable {

    /**
     * {RU}
     * Класс-перечисление "Тип доступа" точки останова
     *
     * @property flags битовые флаги разрешений
     * @property READ активация на чтение
     * @property WRITE активация на запись
     * @property EXEC активация на исполнение
     * @property RW активация на чтение и запись
     * @property RE активация на чтение и исполнение
     * @property WE активация на запись и исполнение
     * @property RWE активация на чтение, запись и исполнение
     * {RU}
     *
     * {EN}
     * Enum-class "access type" of breakpoint
     *
     * @property flags bit flags of access
     * @property READ activate on read
     * @property WRITE activate on write
     * @property EXEC activate on execute
     * @property RW activate on read and write
     * @property RE activate on read and execute
     * @property WE activate on write and execute
     * @property RWE activate on read, write and execute
     * {EN}
     */
    enum class Access(val flags: Int) {
        READ(0x01),
        WRITE(0x02),
        EXEC(0x04),
        RW(READ.flags or WRITE.flags),
        RE(READ.flags or EXEC.flags),
        WE(WRITE.flags or EXEC.flags),
        RWE(READ.flags or WRITE.flags or EXEC.flags);
    }

    /**
     * {RU}
     * Проверка характеристик точки останова
     *
     * @param bpAccess тип доступа
     * {RU}
     *
     * {EN}
     * Check breakpoint characteristic
     *
     * @param bpAccess access type
     * {EN}
     */
    fun check(bpAccess: Access) = access.flags and bpAccess.flags != 0

    /**
     * {RU}
     * Строковое представление объекта
     *
     * @return строка-представление объекта
     * {RU}
     *
     * {EN}
     * Create string representation for this object
     *
     * @return object string representation
     * {EN}
     */
    override fun toString() = if (comment == null) {
        "Breakpoint[0x${range.hex8}, $access]"
    } else {
        "Breakpoint[0x${range.hex8}, $access, $comment]"
    }
}