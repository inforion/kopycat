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
package ru.inforion.lab403.kopycat.cores.base.abstracts

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.SlavePort
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.extensions.IRQ_ENABLE_AREA
import ru.inforion.lab403.kopycat.cores.base.extensions.IRQ_INSERVICE_AREA
import ru.inforion.lab403.kopycat.cores.base.extensions.IRQ_REQUEST_AREA
import java.util.*

abstract class APIC(parent: Module, name: String): Module(parent, name) {

    /**
     * {RU}
     * Внутренний класс Набора Прерываний.
     * Предназначен для хранения информации о диапазоне адресов и контроля доступа к этим адресам.
     *
     * @param port порт, связанный с адресным пространством
     * @param name произвольное имя объекта адресного пространства
     * @param args массив прерываний (vararg)
     *
     * @property table отображение номеров прерываний на объекты прерываний (Map)
     * {RU}
     **/
    inner class Interrupts<T: AInterrupt>(
            port: SlavePort,
            name: String,
            vararg args: T
    ) : Area(port, 0u, port.size - 1u, name) {

        private val count = args.map { it.irq }.maxOrNull()?.inc() ?: 0

        private val table = List(count) { index -> args.firstOrNull { it.irq == index } }

        /**
         * {RU}
         * Доступ к прерыванию по его номеру
         * @param irq номер прерывания
         * @return прерывание из таблицы прерываний
         * @throws IllegalArgumentException при неверном номере прерывания
         * {RU}
         */
        operator fun get(irq: Int): T = table[irq].sure { "Wrong interrupt irq: $irq!" }

        override fun fetch(ea: ULong, ss: Int, size: Int) = throw IllegalAccessException("$name may not be fetched!")

        /**
         * {RU}
         * Получение состояния прерывания
         *
         * @param ea номер прерывания (irq)
         * @param ss тип запроса к прерыванию [IRQ_REQUEST_AREA], [IRQ_ENABLE_AREA], [IRQ_INSERVICE_AREA]
         * @param size размер адресного пространства (не используется)
         * @return флаг включенности (0/1) соответствующего статуса прерывания
         * @throws IllegalArgumentException при неверном номере прерывания
         * {RU}
         */
        override fun read(ea: ULong, ss: Int, size: Int): ULong = read(ea.int, ss).ulong

        fun read(irq: Int, area: Int) = when (area) {
            IRQ_REQUEST_AREA -> this[irq].pending
            IRQ_ENABLE_AREA -> this[irq].enabled
            IRQ_INSERVICE_AREA -> TODO("In service check currently not supported")
            else -> throw IllegalArgumentException("Wrong IRQ area: $area")
        }

        /**
         * {RU}
         * Изменение состояния прерывания
         *
         * @param ea номер прерывания (irq)
         * @param ss тип запроса к прерыванию [IRQ_REQUEST_AREA], [IRQ_ENABLE_AREA], [IRQ_INSERVICE_AREA]
         * @param size размер адресного пространства (не используется)
         * @param value числовое значение для состояния прерывания (в большинстве )
         * @throws IllegalArgumentException при неверном номере прерывания
         * {RU}
         */
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = write(ea.int, ss, value.truth)

        fun write(irq: Int, area: Int, value: Boolean) {
//            log.severe { "[${core.cpu.pc.hex8}] PIC: action=$area for vector=0x${irq.hex2} int=${this[irq]}" }
            when (area) {
                IRQ_REQUEST_AREA -> this[irq].pending = value
                IRQ_ENABLE_AREA -> this[irq].enabled = value
                IRQ_INSERVICE_AREA -> TODO("In service check currently not supported")
                else -> throw IllegalArgumentException("Wrong IRQ area: $area")
            }
        }

        override fun beforeRead(from: MasterPort, ea: ULong): Boolean = table[ea.int] != null
        override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong): Boolean = table[ea.int] != null

        /**
         * {RU}
         * Сохранение состояния (сериализация)
         *
         * @param ctxt контекст объекта-сериализатора
         * @return отображение сохраняемых свойств объекта
         * {RU}
         */
        override fun serialize(ctxt: GenericSerializer): Map<String, Any> =
                mapOf("table" to table.filterNotNull().map { it.serialize(ctxt) })

        /**
         * {RU}
         * Восстановление состояния (десериализация)
         *
         * @param ctxt контекст объекта-сериализатора
         * @param snapshot отображение восстанавливаемых свойств объекта
         * {RU}
         */
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            @Suppress("UNCHECKED_CAST")
            val tableSnapshot = snapshot["table"] as ArrayList<Map<String, String>>
            tableSnapshot.forEach {snp ->
                table.filterNotNull().first { it.name == snp["name"] }.deserialize(ctxt, snp)
            }
        }

        /**
         * {RU}Сброс прерываний{RU}
         */
        override fun reset() {
            super.reset()
            table.filterNotNull().forEach { it.reset() }
        }
    }
}