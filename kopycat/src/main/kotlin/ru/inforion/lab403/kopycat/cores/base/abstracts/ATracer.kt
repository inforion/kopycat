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

import ru.inforion.lab403.common.extensions.asLong
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.extensions.*
import ru.inforion.lab403.kopycat.interfaces.ITracer
import ru.inforion.lab403.kopycat.modules.BUS32

/**
 * {RU}
 * Абстрактный класс трассировщика.
 * Служит для выполнения дополнительных пре- и пост-обработок в процессе выполнения инструкции.
 *
 *
 * @param parent родительский модуль, в который встраивается Ядро
 * @param name произвольное имя объекта Ядра
 * @property ports набор портов
 * @property status статус трассировщика
 * @property io регистр для взаимодействия с трассировщиком
 * {RU}
 */
abstract class ATracer<R: AGenericCore>(
        parent: Module?,
        name: String,
        val memoryBusSize: Long = BUS32
): Module(parent, name), ITracer<R> {

    /**
     * {RU}Объект-логгер{RU}
     */
    companion object {
        @Transient val log = logger(FINE)
    }

    /**
     * {RU}
     * Внутренний класс набора портов
     *
     * @property mem порт памяти
     * @property trace порт трейсера
     * {RU}
     */
    inner class Ports : ModulePorts(this) {
        val mem = Proxy("mem", memoryBusSize)
        val trace = Slave("trace", TRACER_BUS_SIZE)
    }

    final override val ports = Ports()

    private val status = Status.values()

    /**
     * {RU}
     * Переменная указывает включен трейсер или нет.
     * Должна быть выставлена в нужное состояние до запуска отладчика.
     * Основное назначение для отключение компонентного трассировщика,
     *   если в нем нет дополнительных трассировщиков.
     * {RU}
     */
    protected var working = true

    private val io = object : Register(ports.trace, TRACER_REGISTER_EA, DWORD, "TRACE_IO") {
        /**
         * {EN}
         * @param ss - Trace command
         * @param size - Core execute status
         * {EN}
         */
        @Suppress("UNCHECKED_CAST")
        override fun read(ea: Long, ss: Int, size: Int) = when (ss) {
            TRACER_EVENT_PRE_EXECUTE -> preExecute(core as R)
            TRACER_EVENT_POST_EXECUTE -> postExecute(core as R, status[size])
            TRACER_EVENT_START -> TRACER_STATUS_SUCCESS.also { onStart(core as R) }
            TRACER_EVENT_STOP -> TRACER_STATUS_SUCCESS.also { onStop() }
            TRACER_EVENT_WORKING -> working.asLong
            else -> throw IllegalArgumentException("Unknown tracer command!")
        }
    }

    /**
     * {EN}This method is called to run simulation{EN}
     *
     * {RU}Метод вызывается для начала симуляции устройства{RU}
     */
    @Deprecated(
            message = "Start and stop executed on debugger.cont() and this method is redundant and should be avoided",
            level = DeprecationLevel.ERROR)
    inline fun run(block: () -> Unit) {
        onStart(null as R)  // Because this function completely deprecated use this workaround and Kotlin eat this
        block()
        onStop()
    }
}