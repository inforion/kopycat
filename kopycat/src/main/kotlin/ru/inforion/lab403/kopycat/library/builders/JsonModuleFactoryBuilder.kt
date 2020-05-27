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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import ru.inforion.lab403.common.extensions.getInternalFileURL
import ru.inforion.lab403.common.extensions.hexAsULong
import ru.inforion.lab403.common.extensions.toInt
import ru.inforion.lab403.common.extensions.toLong
import ru.inforion.lab403.kopycat.cores.base.APort
import ru.inforion.lab403.kopycat.cores.base.Bus
import ru.inforion.lab403.kopycat.cores.base.StackOfStrings
import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts
import ru.inforion.lab403.kopycat.cores.base.exceptions.ConnectionError
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import ru.inforion.lab403.kopycat.library.builders.api.AFileModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactory
import ru.inforion.lab403.kopycat.library.builders.api.ModuleParameterInfo
import ru.inforion.lab403.kopycat.settings
import java.io.File
import java.io.InputStream

class JsonModuleFactoryBuilder(path: String, val jar: File?) : AFileModuleFactoryBuilder(path) {
    companion object {
        private fun getConnector(items: StackOfStrings, module: Module): Any {
            val first = items.pop()
            return when (first) {
                "ports" -> {
                    val name = items.pop()
                    module.ports[name] ?: throw ConnectionError("Port [$name] not found in [$module]!")
                }
                "buses" -> {
                    val name = items.pop()
                    module.buses[name] ?: throw ConnectionError("Bus [$name] not found in [$module]!")
                }
                else -> {
                    val sub = module[first] ?: throw ConnectionError("Submodule [$first] not found in [$module]")
                    getConnector(items, sub as Module)
                }
            }
        }

        private fun getConnector(desc: String, module: Module) =
                getConnector(StackOfStrings(desc.split(".")), module)

        private fun getBusPortSize(size: String): Long = when {
            size == "PIN" -> 1L
            size.startsWith("BUS") -> 1L shl size.removePrefix("BUS").toInt()
            size.startsWith("0x") -> size.removePrefix("0x").hexAsULong
            else -> size.toLong()
        }

        private fun getOffset(data: String): Long {
            if (data.startsWith("0x"))
                return data.removePrefix("0x").hexAsULong
            return data.toLong()
        }

        private inline fun <reified T : Any> parseJson(json: InputStream): T = jacksonObjectMapper().apply {
            configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true)
        }.readValue(json)
    }

    private class BusConfig(val name: String, val size: String)
    private class ParameterConfig(val name: String, val type: String, val default: Any? = null)

    private class ActionConfig : HashMap<String, Any>()

    private class ModuleConfig(
            val name: String,
            val plugin: String,
            val library: String,
            val params: Map<String, Any>?,
            val actions: Array<ActionConfig>?)

    private class PortConfig(val name: String, val type: String, val size: String)

    private class PluginConfig(
            val top: Boolean,
            val plugin: String,
            val library: String,
            val params: Array<ParameterConfig>,
            val ports: Array<PortConfig>,
            val buses: Array<BusConfig>,
            val modules: Array<ModuleConfig>,
            val connections: Array<Array<String>>)

    private lateinit var config: PluginConfig

    override fun plugins(): Set<String> = setOf(config.plugin)

    override fun load(): Boolean {
        val name = File(path).nameWithoutExtension
        val extension = File(path).extension

        if (extension != "json")
            return false

        val stream = jar.getInternalFileURL(path).openStream()

        try {
            log.fine { "Loading $name from $jar" }
            config = parseJson(stream)
            return true
        } catch (error: Throwable) {
            log.warning { "Error loading $name from $path\n${error.message}" }
            return false
        }
    }

    override fun factory(pluginName: String, registry: ModuleLibraryRegistry): List<IModuleFactory> {
        return listOf(object : IModuleFactory {
            private fun getParameterValue(name: String, value: Any?, values: Array<out Any?>): Pair<String, Any?> {
                val tmp = value?.toString() ?: return name to null  // everything can be cast to string
                return if (tmp.startsWith(settings.jsonParamsSectionName)) {
                    val parameterName = tmp.removePrefix(settings.jsonParamsSectionName)
                    val info = this.parameters.first { it.name == parameterName }
                    name to values[info.index]
                } else name to value
            }

            private fun substituteParameterValues(params: Map<String, Any>, values: Array<out Any?>): Map<String, Any?> =
                    params.map { getParameterValue(it.key, it.value, values) }.toMap()

            override val canBeTop = config.top

            override val parameters = config.params.mapIndexed { k, v -> ModuleParameterInfo(k, v.name, v.default, v.type) }

            override fun create(parent: Component?, name: String, vararg parameters: Any?): Module {
//                log.finer { "Creating $pluginName(${parent?.name}, $name)" }
                val module = Module(parent, name, config.plugin)

                config.ports.forEach {
                    val type = ModulePorts.Type.valueOf(it.type)
                    val size = getBusPortSize(it.size)
                    when (type) {
                        ModulePorts.Type.Master -> module.ports.Master(it.name, size)
                        ModulePorts.Type.Slave -> module.ports.Slave(it.name, size)
                        ModulePorts.Type.Proxy -> module.ports.Proxy(it.name, size)
                        else -> throw IllegalArgumentException("Only Master, Slave and Proxy can be created!")
                    }
                }

                config.buses.forEach {
                    module.buses.Bus(it.name, getBusPortSize(it.size))
                }

                config.modules.forEach {
                    val params = if (it.params != null) substituteParameterValues(it.params, parameters) else emptyMap()
                    registry[it.library].instantiate(module, this@JsonModuleFactoryBuilder, it.plugin, it.name, params)
                }

                config.connections.forEach {
                    val conn1 = getConnector(it[0], module)
                    val conn2 = getConnector(it[1], module)

                    val offset = if (it.size == 3) getOffset(it[2]) else 0

                    when {
                        conn1 is APort && conn2 is Bus -> conn1.connect(conn2, offset)
                        conn1 is APort && conn2 is APort -> module.buses.connect(conn1, conn2, offset)
                        else -> throw ConnectionError("Can't connect($conn1, $conn2, $offset)\n" +
                                "Use connection: \n" +
                                "1. Port, Bus, [Offset|def=0]\n" +
                                "2. Port, Port, [Offset|def=0]")
                    }
                }

                val end = if (parent != null) "within $parent!" else "as top"
                log.info { "Module $module successfully created $end" }

                return module
            }
        })
    }

    override fun getFilePath(): String =
            if (jar == null)
                "Json file: $path"
            else
                "Json File: $path@$jar"
}