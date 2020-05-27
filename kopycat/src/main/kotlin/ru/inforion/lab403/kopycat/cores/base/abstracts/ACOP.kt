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

import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.cores.base.common.InterruptsQueue
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException

/**
 * {RU}
 * Абстрактный класс сопроцессора.
 * Сопроцессор управляет прерываниями и исключениями.
 *
 * @param [name] произвольное имя объекта Сопроцессора
 * @property [core] ядро, в которое встраивается Сопроцессор
 * @property [qInterrupts] очередь прерываний
 * {RU}
 */

abstract class ACOP<
        P: ACOP<P, R>,       // Recursive generic resolution
        R: ACore<R, *, P>>(val core: R, name: String): Component(core, name) {

    private val qInterrupts = InterruptsQueue(core)

    open fun createException(name: String, where: Long, vAddr: Long, action: AccessAction): HardwareException =
            throw NotImplementedError("Make exception not implemented!")

    /**
     * {RU}
     * Обработка прерываний.
     * Этот метод вызывается каждый шаг работы эмулятора аналогично cpu.execute()
     * {RU}
     *
     * {EN}
     * What to do when process interrupts requested
     * This function executed each emulator core loop step
     *
     * NOTE: It's similar to cpu.execute() function
     * {EN}
     **/
    abstract fun processInterrupts()

    /**
     * {RU}
     * Обработка исключений.
     *
     * Когда возникает исключительная ситуация, сопроцессор должен попытаться обработать её.
     * Если сопроцессор может обработать исключение, то должно было быть возращено значение NULL,
     * иначе возвращаемое значение будет установлено как новое исключение.
     * Обычно сопроцессор должен обрабатывать только аппаратные ислючительные ситуации (HardwareException).
     *
     * @param [exception] исключение, которое необходимо обработать
     * {RU}
     *
     * {EN}
     * When the exception is comming Coprocessor should try to handle it.
     * If Coprocessor can handle exception then null should be returned otherwise whatever returned
     * would be set as a new exceptions. Usually Coprocessor should work out just HardwareException
     * especially <device>HardwareException, where <device> is CPU Coprocessor belongs to.
     * {EN}
     **/
    abstract fun handleException(exception: GeneralException?): GeneralException?

    /**
     * {RU}
     * Запрос ожидающего прерывания
     *
     * @param [ie] флаг глобального разрешения прерываний (может быть использован для немаскируемых прерываний)
     * @return прерывание с наибольшим приоритетом или NULL при отсутствии прерываний
     * {RU}
     *
     * {EN}
     * Return pending interrupt with highest priority or null if no interrupt pending
     * @param ie - flag of global interrupt enable status (may be used for NMI processing)
     * {EN}
     */
    fun pending(ie: Boolean) = qInterrupts.take(ie)

    /**
     * {RU}
     * Запросить/очистить прерывание
     *
     * @param interrupt внутренний дескриптор прерывания эмулятора
     * @param value запросить (true) или очистить (false) прерывание
     * {RU}
     *
     * {EN}
     * Request/clear interrupt
     * @param interrupt - internal emulator descriptor
     * @param value - request (true) or clear (false) interrupt
     * {EN}
     **/
    fun interrupt(interrupt: AInterrupt, value: Boolean) =
            if (value) qInterrupts.requestInterrupt(interrupt) else
                qInterrupts.clearInterrupt(interrupt)

    /**
     * {RU}
     * Запрос состояния прерывания.
     *
     * @param interrupt внутренний дескриптор прерывания эмулятора
     * @return true - ожидающее прерывание / false - неожидающее прерывание
     * {RU}
     *
     * {EN}
     * Get interrupt pending state by interrupt descriptor
     *
     * @param interrupt - internal emulator descriptor
     * {EN}
     **/
    fun interrupt(interrupt: AInterrupt) = qInterrupts.isInterruptPending(interrupt)

    /**
     * {RU}
     * Сброс сопроцессора и очереди прерываний.
     * {RU}
     *
     * {EN}
     * Reset a coprocessor state
     * {EN}
     **/
    override fun reset() {
        super.reset()
        qInterrupts.reset()
    }
}