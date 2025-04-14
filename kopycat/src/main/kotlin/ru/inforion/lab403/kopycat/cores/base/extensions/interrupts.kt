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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.cores.base.extensions

import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.Port
import ru.inforion.lab403.kopycat.interfaces.*


/**
 * {RU}Функции расширения Master-порта для работы с прерываниями{RU}
 *
 * {EN}IRQ support function definition{EN}
 */
const val IRQ_REQUEST_AREA = 0
const val IRQ_ENABLE_AREA = 1
const val IRQ_INSERVICE_AREA = 2

/**
 * {RU}
 * Функция выполняет запрос прерывания по заданному смещению
 *
 * @param pin номер прерывания
 * {RU}
 *
 * {EN}Request the specified interrupt{EN}
 */
inline fun Port.request(pin: Int) = outb(pin.ulong_z, 1u, IRQ_REQUEST_AREA)

/**
 * {EN}Check whether interrupt is requested now (pending){EN}
 *
 * {RU}
 * Функция проверяет запрошено или нет прерывание
 *
 * @param pin номер прерывания
 *
 * @return true - если прерывание ожидает обработки
 * {RU}
 */
inline fun Port.pending(pin: Int) = inb(pin.ulong_z, IRQ_REQUEST_AREA) == 1uL

/**
 * {EN}Check whether interrupt is in service now (in fact in handler){EN}
 *
 * {RU}
 * Функция проверяет находится ли сейчас в обработке прерывание
 *
 * @param pin номер прерывания
 *
 * @return true - если прерывание в обработке
 * {RU}
 */
inline fun Port.inservice(pin: Int) = inb(pin.ulong_z, IRQ_INSERVICE_AREA) == 1uL

/**
 * {EN}Enable or disable interrupt{EN}
 *
 * {RU}
 * Функция изменяет состояние прерывание (включено/выключено)
 *
 * @param pin номер прерывания
 * @param value включить или выключить
 * {RU}
 */
inline fun Port.enabled(pin: Int, value: Boolean) = outb(pin.ulong_z, value.ulong, IRQ_ENABLE_AREA)

/**
 * {EN}Check whether interrupt enabled{EN}
 *
 * {RU}
 * Функция проверяет включено или нет прерывание
 *
 * @param pin номер прерывания
 *
 * @return true - если прерывание включено
 * {RU}
 */
inline fun Port.enabled(pin: Int): Boolean = inb(pin.ulong_z, IRQ_ENABLE_AREA) == 1uL

/**
 * {EN}Enable interrupt{EN}
 *
 * {RU}
 * Функция включает прерывание
 *
 * @param pin номер прерывания
 * {RU}
 */
inline fun Port.enable(pin: Int) = enabled(pin, true)

/**
 * {EN}Disable interrupt{EN}
 *
 * {RU}
 * Функция выключает прерывание
 *
 * @param pin номер прерывания
 * {RU}
 */
inline fun Port.disable(pin: Int) = enabled(pin, false)