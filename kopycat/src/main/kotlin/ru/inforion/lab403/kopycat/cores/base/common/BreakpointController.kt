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
import ru.inforion.lab403.common.extensions.plus
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import java.io.Serializable

/**
 * {RU}
 * Менеджер точек останова.
 *
 *
 * @property store хранилище-отображение точек останова
 * {RU}
 *
 * {EN}
 * Manager of breakpoints
 *
 * @property store storage for breakpoints
 * {EN}
 */
class BreakpointController: Serializable {
    companion object {
        @Transient private val log = logger(FINE)
    }

    private val store = mutableListOf<Breakpoint>()

    operator fun iterator() = store.iterator()

    /**
     * {RU}
     * Добавление новой точки останова
     *
     * @param range Адрес точки останова
     * @param access Тип срабатывания точки останова
     * @param comment необязательный комментарий
     * @param onBreak Функция-обработчик срабатывания точки останова
     * @return добавлена или нет точка останова (true/false)
     * {RU}
     *
     * {EN}
     * Add new breakpoint to this BreakpointController
     *
     * @param range new breakpoint address
     * @param access type of action to breakpoint access
     * @param comment optional comment
     * @param onBreak function to process breakpoint action
     * @return is success (true/false)
     * {EN}
     */
    fun add(
        range: ULongRange,
        access: Breakpoint.Access,
        comment: String? = null,
        onBreak: ((ea: ULong) -> Unit)? = null,
    ): Boolean {
        if (store.find { it.range.first == range.first } != null) {
            log.warning { "Breakpoint already setup here range=0x${range.hex8}" }
            return false
        }

        store.add(Breakpoint(range, access, comment, onBreak))
        store.sortBy { it.range.first }
        return true
    }

    /**
     * {RU}
     * Удаление точки останова
     * @param ea адрес точки останова
     * @return удалена или нет точка останова (true/false)
     * {RU}
     *
     * {EN}
     * Delete breakpoint
     * @param ea breakpoint address
     * @return is success (true/false)
     * {EN}
     */
    fun remove(ea: ULong): Boolean {
        for (bp in store.withIndex()) {
            if (bp.value.range.first == ea) {
                store.removeAt(bp.index)
                return true
            }
        }
        return false
    }

    fun oneshot(
        ea: ULong,
        access: Breakpoint.Access,
        comment: String? = null,
        onBreak: (ea: ULong) -> Unit,
    ) = add(ea..ea, access, comment = comment) {
        onBreak(it)
        remove(it)
    }

    fun oneshot(
        range: ULongRange,
        access: Breakpoint.Access,
        comment: String? = null,
        onBreak: (ea: ULong) -> Unit,
    ) = add(range, access, comment = comment) {
        onBreak(it)
        remove(it)
    }

    /**
     * {RU}
     * Получение и проверка точки останова из пула [BreakpointController] по адресу
     *
     * @param addr Адрес точки останова
     * @param size Размер точки останова
     * @param access Тип срабатывания точки останова
     * @return найдена или нет точка остановка по заданному адресу
     * {RU}
     *
     * {EN}
     * Gets and checks a breakpoint from [BreakpointController] store by address
     *
     * @param addr breakpoint address
     * @param size breakpoint size
     * @param access type of action to breakpoint access
     * @return found breakpoint or null is it not found.
     * {EN}
     */
    fun lookup(addr: ULong, size: Int, access: Breakpoint.Access): Breakpoint? {
        val range = addr until addr + size

        for (bp in store) {
            if (bp.range.first <= range.last) {
                if (bp.range.last >= range.first && bp.check(access)) {
                    return bp
                }
            } else {
                break
            }
        }

        return null
    }

    /**
     * {EN}Delete all breakpoints from [BreakpointController]{EN}
     */
    fun clear() = store.clear()
}