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
package ru.inforion.lab403.kopycat.interactive.rest

import ru.inforion.lab403.common.extensions.Krest
import ru.inforion.lab403.kopycat.library.builders.text.PluginConfig
import ru.inforion.lab403.kopycat.library.types.FactoryInfo


class RegistryClient(host: String, port: Int, retries: Int = 10) {
    private val client = Krest(host, port, "registry", retries = retries)

    fun getAvailableTopModules(): Map<String, Map<String, List<FactoryInfo>>> =
            client.get("getAvailableTopModules")

    fun getAvailableAllModules(): Map<String, Map<String, List<FactoryInfo>>> =
            client.get("getAvailableAllModules")

    inner class Module {
        val endpoint = "module"

        fun create(parent: String?, name: String, config : PluginConfig? = null): String =
                client.post(endpoint, config, "parent" to parent, "name" to name)

        /**
         * {RU}
         * Удалить модуль по имени из перечня модулей REST-сервера
         *
         * @param name имя удаляемого модуля
         * @param hierarchy флаг удаление модулей по иерархии, если true,
         *   будут удалены все дочерние компоненты этого модуля
         * {RU}
         */
        fun delete(name: String, hierarchy: Boolean): Unit =
                client.delete(endpoint, emptyMap<String, Any>(), "name" to name, "hierarchy" to hierarchy.toString())
    }

    val module = Module()

    /**
     * {RU}
     * Создать модуль с заданным типом (плагином) в перечне модулей REST-сервера
     *
     * @param parent имя родительского модуля (если нет, то null)
     * @param name имя создаваемого модуля
     * @param plugin тип создаваемого модуля
     * @param library библиотека создаваемого модуля
     * @param params параметры конструктора
     * {RU}
     */
    fun instantiate(parent: String?, name: String, plugin: String, library: String, params: Map<String, Any>? = null): String {
        val body = mapOf("name" to name, "plugin" to plugin, "library" to library, "params" to params)
        return client.post("instantiate", body, "parent" to parent)
    }
}

