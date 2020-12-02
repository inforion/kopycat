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
package ru.inforion.lab403.kopycat.library.builders.api

import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry

interface IModuleFactoryBuilder {
    /**
     * {EN}Method should do preload of current factory builder{EN}
     */
    fun preload() = true

    /**
     * {EN}This function should perform a check whether or not plugin can be loaded{EN}
     *
     * {RU}Выполняет загрузку модуля и возвращает true в случае успеха и false, если модуль не может быть загружен{RU}
     */
    fun load(): Boolean

    /**
     * {EN}
     * Returns list of available factories for this builder.
     * Methods returns list because module may have several constructors.
     * Also [name] parameters required for cases when builder contains several factories i.e. jar-files.
     *
     * @param name module name to get available constructors/factories
     * @param registry registry to use to look up specified module
     * {EN}
     *
     * {RU}
     * Возвращает список доступных "конструкторов" для модуля.
     *
     * Данные метод отдает список, так как это необходимо, если есть перегруженные конструкторы у исходного класса.
     *
     * @param name имя модуля для получение его "конструкторов"
     * @param registry реестр, из которого необходимо получить "конструкторы"
     * {RU}
     */
    fun factory(name: String, registry: ModuleLibraryRegistry): List<IModuleFactory>

    /**
     * {EN}
     * Returns list of available module's plugins found during loading
     * {EN}
     */
    val plugins: Set<String>

    override fun toString(): String
}

