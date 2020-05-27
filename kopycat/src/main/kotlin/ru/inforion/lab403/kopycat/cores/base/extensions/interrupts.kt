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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.cores.base.extensions

import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.cores.base.MasterPort

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
inline fun MasterPort.request(pin: Int) = outb(pin.asULong, 1, IRQ_REQUEST_AREA)

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
inline fun MasterPort.pending(pin: Int) = inb(pin.asULong, IRQ_REQUEST_AREA) == 1L

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
inline fun MasterPort.inservice(pin: Int) = inb(pin.asULong, IRQ_INSERVICE_AREA) == 1L

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
inline fun MasterPort.enabled(pin: Int, value: Boolean) = outb(pin.asULong, value.asLong, IRQ_ENABLE_AREA)

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
inline fun MasterPort.enabled(pin: Int): Boolean = inb(pin.asULong, IRQ_ENABLE_AREA) == 1L

/**
 * {EN}Enable interrupt{EN}
 *
 * {RU}
 * Функция включает прерывание
 *
 * @param pin номер прерывания
 * {RU}
 */
inline fun MasterPort.enable(pin: Int) = enabled(pin, true)

/**
 * {EN}Disable interrupt{EN}
 *
 * {RU}
 * Функция выключает прерывание
 *
 * @param pin номер прерывания
 * {RU}
 */
inline fun MasterPort.disable(pin: Int) = enabled(pin, false)