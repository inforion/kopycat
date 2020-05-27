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

import ru.inforion.lab403.kopycat.cores.base.Bus
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.modules.PIN

/**
 * {RU}
 * Базовый класс аппаратного регистра.
 * Представляет собой надстройку над простым Регистром [Module.Register]
 *
 *
 * @param parent Родительский модуль
 * @param name Произвольное имя регистра
 *
 * @property ports Доступные порты Аппаратного Регистра
 * @property reg Внутренний объект-регистр
 * @property io Флаг чтения/записи (true/false)
 * {RU}
 *
 * {EN}
 * Base class of hardware register
 * It is wrapper of basic register class [Module.Register]
 *
 * @param parent parent component
 * @param name register name
 *
 * @property ports avaliable ports of this module
 * @property reg inner register
 * @property io Read/write flag
 * {EN}
 */
open class HardwareRegister(parent: Module, name: String) : Module(parent, name) {
    inner class Ports : ModulePorts(this) {
        val io = Slave("io", PIN)
    }

    final override val ports = Ports()

    protected val reg = object : Register(ports.io, 0, DWORD, "REG") {
        override fun read(ea: Long, ss: Int, size: Int): Long = this@HardwareRegister.read(ea, size)

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            super.write(ea, ss, size, value)
            this@HardwareRegister.write(ea, size, value)
        }
    }

    /**
     * {RU}
     * Подключение порта регистра [ports.io] к заданной шине [bus]
     *
     * @param bus Шина для подключения порта регистра
     * @param offset смещение на шине
     * {RU}
     *
     * {EN}
     * Connect this module's port [ports.io] to selected bus [bus]
     *
     * @param bus Bus to connect
     * @param offset Bus offset
     * {EN}
     */
    fun connect(bus: Bus, offset: Long) = ports.io.connect(bus, offset)

    /**
     * {RU}
     * Чтение данных регистра
     *
     * @param ea Адрес
     * @param size Размер данных для чтения
     *
     * @return Данные регистра
     * {RU}
     *
     * {EN}
     * Read register's data
     *
     * @param ea address
     * @param size Size to read (in bytes)
     *
     * @return Data
     * {EN}
     */
    open fun read(ea: Long, size: Int): Long = reg.data

    /**
     * {RU}
     * Базовый метод записи данных регистра
     *
     * @param ea Адрес
     * @param size Размер данных для записи
     * @param value Значение для записи в регистр
     * {RU}
     *
     * {EN}
     * Base method to write data in register
     *
     * @param ea address
     * @param size Size to write (in bytes)
     * @param value Value to write
     * {EN}
     */
    open fun write(ea: Long, size: Int, value: Long) = Unit

    /**
     * {RU}
     * Сохранение состояния (сериализация)
     *
     * @param ctxt контекст объекта-сериализатора
     *
     * @return отображение сохраняемых свойств объекта
     * {RU}
     */
    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return super.serialize(ctxt) + mapOf("reg" to reg.serialize(ctxt))
    }

    /**
     * {RU}
     * Восстановление состояния (десериализация)
     *
     * @param ctxt контекст объекта-сериализатора
     * @param snapshot отображение восстанавливаемых свойств объекта
     * {RU}
     */
    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)
        reg.deserialize(ctxt, snapshot["reg"] as Map<String, Any>)
    }
}