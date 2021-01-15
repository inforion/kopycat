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

package ru.inforion.lab403.kopycat.interactive.protocols

import io.javalin.Javalin
import io.javalin.core.plugin.Plugin
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import ru.inforion.lab403.kopycat.library.builders.text.ModuleConfig
import ru.inforion.lab403.kopycat.library.builders.text.PluginConfig
import ru.inforion.lab403.kopycat.library.types.LibraryInfo

class RegistryRestProtocol(private val registry: ModuleLibraryRegistry?, val modules: MutableList<Module>): Plugin {
    companion object {
        val log = logger()
    }

    val name = "registry"

    private inline val registryOrThrow get() = registry.sure { "registry wasn't initialized" }

    /**
     * {EN}
     * Returns information about libraries content in the following format:
     * ```
     * "library0": {
     *     "module0": [
     *         {  // factory0 i.e. class constructor0
     *             "top": true,
     *             "parameters": [
     *                 { // parameter <index>
     *                     "index": Int,
     *                     "name": String,
     *                     "type": String,
     *                     "optional": Boolean,
     *                     "default": Any? = Unit
     *                 },
     *
     *                 { // parameter <index>
     *                     "index": Int,
     *                     "name": String,
     *                     "type": String,
     *                     "optional": Boolean,
     *                     "default": Any? = Unit
     *                 }
     *             ]
     *         },
     *
     *         {  // factory1  i.e. class constructor1
     *          ...
     *         }
     *     ],
     * }
     * ```
     * {EN}
     */
    private fun List<LibraryInfo>.getContentInfo() = associate { library ->
        library.name to library.modules.associate { module ->
            module.name to module.factories
        }
    }

    override fun apply(app: Javalin) = app.applyRoutes {

        getAny("$name/getAvailableTopModules") {
            registryOrThrow.getAvailableTopModules().getContentInfo()
        }

        getAny("$name/getAvailableAllModules") {
            registryOrThrow.getAvailableAllModules().getContentInfo()
        }

        postAny("$name/module") {
            val parentName = it.header("parent")
            val moduleName = it.header("name")

            require(moduleName != null) { "module name parameter must be specified" }
            val parent = if (parentName == null) null else modules.find { it.name == parentName }

            if (it.body().isNotBlank()) {
                val config = it.bodyAsClass(PluginConfig::class.java)
                registryOrThrow.json(parent, moduleName, config)
            } else {
                Module(parent, moduleName)
            }.also { modules.add(it) }.name
        }

        /**
         *  {RU}
         *  Запрос на удаление модуля из периферии REST
         *  Модуль, который уже был открыт через запрос open, останется в системе.
         *  Для закрытия используйте запрос close
         *
         *  ВНИМАНИЕ: С установленным флагом hierarchy будут удалены все дочерние компоненты модуля
         *
         *  Пример тела запроса
         *  {
         *      name:       "testModule",
         *      hierarchy:   true
         *  }
         *  {RU}
         */
        deleteVoid("$name/module") {
            val name = it.header("name")
            val hierarchy = it.header("hierarchy")

            require(hierarchy != null) { "hierarchy parameter must be specified" }

            val top = modules.find { it.name == name }
            require(top != null) { "Module wasn't found '${name}'" }

            modules.removeIf { it.name == top.name }
            if (hierarchy.toBoolean()) {
                val children = top.getAllComponents().map { it.name }.toMutableSet()
                // remove method returns true if value was removed from the Set and true result in removing it from modules
                modules.removeAll { children.remove(it.name) }
                if (children.isNotEmpty()) {
                    log.warning { "Not all children of '${top.name}' was in REST modules: $children" }
                }
            }
        }

        /**
         * {RU}
         * Пример тела и параметров запроса для instantiate
         *  {
         *      "name":     "mips",
         *      "plugin":   "MipsCore",
         *      "library":  "cores",
         *      "params":
         *      {
         *          "frequency": 4000000,
         *          "ipc":       0.25,
         *          "PRId":      0xDEADCAFFE,
         *          "PABITS":    32
         *      }
         *  }
         *
         * Params:
         * {
         *      "parent": "device"
         * }
         *
         * @return имя созданного компонента
         * {RU}
         */
        postAny("$name/instantiate") {
            val parentName = it.header("parent")
            val module = it.bodyAsClass(ModuleConfig::class.java)
            val parent = if (parentName == null) null else modules.find { m -> m.name == parentName }
                    .sure { "Parent module '$parentName' not found in already instantiated modules in REST" }
            module.create(registryOrThrow, parent).also { modules.add(it) }.name
        }
    }
}