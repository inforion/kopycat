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
package ru.inforion.lab403.kopycat.cores.base.abstracts

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.proposal.toSerializable
import ru.inforion.lab403.kopycat.cores.base.AGenericCOP
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.interfaces.ICoreUnit
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues
import java.util.logging.Level.FINER

/**
 * {RU}
 * Абстрактный класс Прерывание.
 * Используется для работы с аппаратными и программными прерываниями.
 * Объекты этого класса должны быть неизменными всё время работы эмулятора.
 *
 * @param postfix суффикс для формирования имени объекта прерывания
 * @property irq номер прерывания
 * @property enabled флаг активности прерывания (по умолчанию, false - выключено)
 * @property cop сопроцессор, обрабатывающий прерывание
 * @property name имя объекта Прерывания
 * @property nmi флаг "немаскируемое прерывание" (по умолчанию, false)
 * @property pending флаг "ожидающее прерывание" (находится в очереди прерываний)
 * @property masked флаг "маскируемое прерывание"
 * @property vector вектор прерывания (адрес обработчика прерывания)
 * @property priority приоритет прерывания
 * {RU}
 *
 * {EN}
 * Class used to working with hardware and software interrupts
 * These objects should be perpetual thought all emulator lifetime
 * {EN}
 */
abstract class AInterrupt(
        val irq: Int,
        postfix: String,
        val prefix: String = "INT",
        var enabled: Boolean = false) : ICoreUnit {

    companion object {
        @Transient val log = logger(FINER)
    }

    override fun hashCode(): Int {
        var result = irq
        result = 31 * result + prefix.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AInterrupt) return false

        if (irq != other.irq) return false
        if (prefix != other.prefix) return false

        return true
    }

    abstract val cop: AGenericCOP

    final override val name = "$prefix#$irq [$postfix]"

    /**
     * {RU}Реализация должна возвращать, является ли это прерывание NMI (Non-Maskable) или нет{RU}
     *
     * {EN}Implementation should return is this interrupt NMI (Non-Maskable) or not{EN}
     */
    open val nmi: Boolean = false

    /**
     * {RU}Реализация должна возвращать true когда прерывание ожидает (в InterruptQueue){RU}
     *
     * {EN}Implementation should return true when interrupt pending (in InterruptQueue){EN}
     */
    open var pending: Boolean
        get() = cop.interrupt(this)
        set(value) {
            cop.interrupt(this, value)
        }

    /**
     * {RU}Возвращает является ли данное прерыванием маскируемым{RU}
     *
     * {EN}Return is currently interrupt masked{EN}
     */
    open val masked get() = !enabled

    abstract val vector: Int
    abstract val priority: Int

    open val cause: Int = -1

    open var inService = false

    /**
     * {RU}Действия при извлечении прерывания из очереди (Interrupt Queue){RU}
     *
     * {EN}What to do when interrupt taken from the "Interrupt Queue"{EN}
     */
    open fun onInterrupt() {
        pending = false
    }

    /**
     * {RU}Сброс прерывания{RU}
     */
    override fun reset() {
        super.reset()
        enabled = false
        pending = false
    }

    /**
     * {RU}
     * Строковое представление объекта прерывания
     *
     * ВНИМАНИЕ: Из метода убраны все состояния, так чтобы получить pending необходимо захватит Lock.
     * Так как дебагер пытается привести к строке HashSet с прерывания - под дебагером это
     * приведет к бесконечному блокированию.
     *
     * см. https://youtrack.lab403.inforion.ru/issue/KC-1629
     *
     * Текстовое описание вынесено в метод [stringify], то же самое касается и классов потомков
     * {RU}
     */
    final override fun toString() = name

    /**
     * {RU}
     * Выводит полное строковое описание прерывание вместе с его состояниями.
     *
     * ВНИМАНИЕ: Необходимо быть аккуратным при вызове этого метода внутри [InterruptsQueue]
     * так как это может привести к бесконечно блокировке.
     * {RU}
     */
    override fun stringify() = "$name v=${vector.hex8} p=$pending e=$enabled r=$priority"

    /**
     * {RU}
     * Сохранение состояния (сериализация)
     *
     * @param ctxt контекст объекта-сериализатора
     * @return отображение сохраняемых свойств объекта
     * {RU}
     */
    override fun serialize(ctxt: GenericSerializer) = storeValues(
            "irq" to irq,
            "name" to name,
            "nmi" to nmi,
            "pending" to pending,
            "enabled" to enabled,
            "priority" to priority)

    /**
     * {RU}
     * Восстановление состояния (десериализация)
     *
     * @param ctxt контекст объекта-сериализатора
     * @param snapshot отображение восстанавливаемых свойств объекта
     * {RU}
     */
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        pending = loadValue(snapshot, "pending")
        enabled = loadValue(snapshot, "enabled")
    }
}