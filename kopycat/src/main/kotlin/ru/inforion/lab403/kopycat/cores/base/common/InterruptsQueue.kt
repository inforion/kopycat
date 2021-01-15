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
package ru.inforion.lab403.kopycat.cores.base.common

import gnu.trove.set.hash.THashSet
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInterrupt
import ru.inforion.lab403.kopycat.interfaces.IResettable
import ru.inforion.lab403.kopycat.interfaces.ISerializable
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level

/**
 * {RU}
 * Очередь прерываний.
 * (Является в действительности не очередью, а коллекцией для поддержки прерываний)
 *
 * @property log
 * @property name имя очереди прерываний
 * @property interrupts контейнер-отображение прерываний (HashSet)
 * {RU}
 *
 * {EN}Not a really queue but collection for support interrupts{EN}
 */
class InterruptsQueue(val core: AGenericCore): IResettable, ISerializable {
    @Transient private val log = logger(Level.INFO)

    val name: String = "Interrupts Queue"

    private val interrupts = THashSet<AInterrupt>(192)
    private val lock = AtomicInteger()

    private inline fun <R>safe(block: () -> R): R {
        while (!lock.compareAndSet(0, 1)) {

        }
        val result = block()
        lock.set(0)
        return result
    }

    /**
     * {RU}
     * Проверка ожидающего прерывания (содержится ли прерывание в текущей коллекции)
     *
     * @param interrupt прерывание для проверки
     * {RU}
     *
     * {EN}Check whether interrupt is currently pending (i.e. in queue){EN}
     */
    fun isInterruptPending(interrupt: AInterrupt) = safe { interrupt in interrupts }

    /**
     * {RU}
     * Запрос прерывания.
     * Если прерывания нет в очереди, оно добавляется в очередь.
     * (Прерывание не добавляется повторно)
     * @param interrupt прерывание для добавления в очередь
     * {RU}
     *
     * {EN}
     * If interrupt not in queue add it.
     * NOTE: If interrupt already pending then it won't be added second time
     * {EN}
     */
    fun requestInterrupt(interrupt: AInterrupt) = safe {
        core.cpu.halted = false
        interrupts.add(interrupt)
    }

    /**
     * {RU}
     * Получение наиболее приоритетного прерывания из очереди.
     * @param ie флаг для выбора немаскируемых прерываний
     *
     * Флаг [ie] (Global Interrupt Enabled) используется для работы с механизмом NMI, соответствует аппаратным флагам
     * (MIPS - IE, x86 - IF, Renesas - !ID и т.д.)
     * Возвращается прерывание, у которого выставлен флаг nmi или выставлен глобальный флаг ie.
     *
     * Примечание: выбранное прерывание не будет удалено из очереди.
     * {RU}
     *
     * {EN}
     * Get the most priority enabled interrupt
     *
     * Global Interrupt Enabled (ie) flag should be used for supporting of NMI mechanism and fully correspond
     * hardware flag (MIPS - IE, x86 - IF, Renesas - !ID, etc)
     * Interrupt will returned from queue either it nmi or ie flag set
     *
     * NOTE: Interrupt won't removed from queue when taken!!!
     * {EN}
     */
    fun take(ie: Boolean) = safe {
        var min = Int.MAX_VALUE
        var result: AInterrupt? = null

        for (interrupt in interrupts) {
            if (!interrupt.masked
                    && interrupt.priority < min
                    && (ie || interrupt.nmi)) {
                min = interrupt.priority
                result = interrupt
            }
        }

        result
    }

    /**
     * {RU}
     * Удаление прерывания из очереди ожидания.
     * @param interrupt прерывание, которое следует удалить из очереди ожидания
     * {RU}
     *
     * {EN}
     * Removes interrupt from pending
     * {EN}
     */
    fun clearInterrupt(interrupt: AInterrupt) = safe { interrupts.remove(interrupt) }

    // Interfaces implementations

    /**
     * {RU}Сброс очереди (удаление всех прерываний){RU}
     *
     * {EN}Reset queue and clear all interrupts{EN}
     */
    override fun reset() = interrupts.forEach { clearInterrupt(it) }
}