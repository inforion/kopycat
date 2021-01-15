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
package ru.inforion.lab403.kopycat.library.builders

import ru.inforion.lab403.common.extensions.getInternalFileURL
import ru.inforion.lab403.common.extensions.parseJson
import ru.inforion.lab403.common.proposal.*
import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import ru.inforion.lab403.kopycat.library.builders.api.AFileModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactory
import ru.inforion.lab403.kopycat.library.builders.api.ModuleParameterInfo
import ru.inforion.lab403.kopycat.library.builders.text.PluginConfig
import ru.inforion.lab403.kopycat.settings
import java.io.File

class JsonModuleFactoryBuilder(path: String, val jar: File?) : AFileModuleFactoryBuilder(path) {
    private lateinit var config: PluginConfig

    override val plugins get() = setOf(config.plugin)

    override fun load(): Boolean {
        val name = File(path).nameWithoutExtension
        val extension = File(path).extension

        if (extension != "json")
            return false

        val stream = jar.getInternalFileURL(path).openStream()

        return stream.runCatching {
            log.fine { "Loading $name from $jar" }
            config = parseJson()
        }.onFailure {
            log.warning { "Error loading $name from $path\n${it.message}" }
        }.isSuccess
    }

    class JsonModule constructor(
            builder: JsonModuleFactoryBuilder?,
            registry: ModuleLibraryRegistry,
            parent: Component?,
            name: String,
            val config: PluginConfig,
            vararg val parameters: Any?
    ) : Module(parent, name, config.plugin) {

        constructor(
                builder: JsonModuleFactoryBuilder?,
                registry: ModuleLibraryRegistry,
                parent: Component?,
                name: String,
                config: PluginConfig,
                parameters: Map<String, Any?>
        ) : this(builder, registry, parent, name, config, config.params.map { parameters[it.name] })

        private val engine by lazy {
            val definitions = config.definitions
            val objects = listOf("module" to this) + definitions.map { it.name to parameters[it.index] }
            kotlinScriptEngine(*objects.toTypedArray()).apply {
                eval("import kotlin.system.*")
                eval("import ru.inforion.lab403.common.extensions.*")
                eval("import ru.inforion.lab403.common.proposal.*")
                eval("import ru.inforion.lab403.kopycat.modules.common.*")
                eval("import ru.inforion.lab403.kopycat.modules.memory.*")
                eval("import ru.inforion.lab403.kopycat.modules.terminals.*")
                eval("import ru.inforion.lab403.kopycat.modules.*")
            }
        }

        override fun reset() {
            super.reset()
            config.reset?.let {
                val script = it.joinToString(System.lineSeparator())
                engine.eval(script)
            }
        }

        init {
            config.ports.forEach { it.create(this) }
            config.buses.forEach { it.create(this) }
            config.modules.forEach { module ->
                val params = if (module.params == null) emptyMap() else
                    config.substituteParameterValues(module.params) { parameters[it.index] }
                module.create(registry, this, builder, params)
            }
            config.connections.forEach { it.create(this) }
        }
    }

    override fun factory(name: String, registry: ModuleLibraryRegistry): List<IModuleFactory> = listOf(
            object : IModuleFactory {
                override val canBeTop = config.top

                override val parameters = config.definitions

                override fun create(parent: Component?, name: String, parameters: Map<String, Any?>): Module {
                    val builder = this@JsonModuleFactoryBuilder
                    val module = JsonModule(builder, registry, parent, name, config, parameters)

                    val end = if (parent != null) "within $parent" else "as top"
                    log.info { "Module $module successfully created $end" }

                    return module
                }
            }
    )

    override val filePath get() = if (jar == null) path else "$path@$jar"
}