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
package ru.inforion.lab403.kopycat.library

import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.builders.api.AFileModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.enumerators.IFactoriesEnumerator
import ru.inforion.lab403.kopycat.library.enumerators.InternalFactoriesEnumerator
import ru.inforion.lab403.kopycat.library.enumerators.PluginFactoriesEnumerator
import ru.inforion.lab403.kopycat.library.types.SatisfactionChecker
import ru.inforion.lab403.kopycat.library.builders.api.InputParameterInfo
import ru.inforion.lab403.kopycat.library.types.FactoryInfo
import ru.inforion.lab403.kopycat.library.types.ModuleInfo
import ru.inforion.lab403.kopycat.settings

class ModuleFactoryLibrary(val name: String, val enumerator: IFactoriesEnumerator) {
    companion object {
        @Transient
        val log = logger(INFO)

        fun loadInternal(name: String, directory: String) =
                ModuleFactoryLibrary(name, InternalFactoriesEnumerator(directory))

        fun loadPlugins(name: String, directory: String) =
                ModuleFactoryLibrary(name, PluginFactoriesEnumerator(directory))
    }

    private lateinit var registry: ModuleLibraryRegistry

    private val modules = mutableMapOf<String, ModuleInfo>()

    fun register(newRegistry: ModuleLibraryRegistry) {
        log.fine { "Register module library $name" }
        registry = newRegistry
    }

    fun preload() {
        log.finer { "Preload module library $name" }
        enumerator.preload()
    }

    fun load() {
        log.finer { "Load module library $name" }
        modules += enumerator.load().map { (name, builder) ->
            val factories = builder.factory(name, registry).map { FactoryInfo(it.canBeTop, it.parameters) }
            name to ModuleInfo(name, builder, factories)
        }
    }

    private fun getPeripheralFactoryBuilder(name: String) = modules[name]?.builder
            ?: throw ClassNotFoundException("Module '$name' wasn't found! Use -top or --modules-registry-top-info to show available top modules.")

    /**
     * {EN}
     * Return all available modules in library.
     * Module consists of name and builder. Builder is special class inherited from [IModuleFactoryBuilder]
     * that contain information about how to create module and load it.
     * module.
     *
     * @return List of [ModuleInfo] - data class with name and builder of each module
     * {EN}
     */
    fun getAvailableAllModules() = modules.values

    fun getAvailableTopModules() = getAvailableAllModules().filter { module -> module.factories.all { it.top } }

    private fun buildPeripheralFactory(name: String) = getPeripheralFactoryBuilder(name).factory(name, registry)

    private fun getHelpConstructorString(factories: List<SatisfactionChecker>) =
            factories.joinToString("\n") { factory ->
                val params = factory.getPrintableError()
                "Constructor #${factories.indexOf(factory) + 1}\n$params"
            }

    private fun getModuleDescString(builder: AFileModuleFactoryBuilder?, module: String, designator: String): String {
        val filePath = builder?.filePath ?: "undefined"
        return "module '$module'[path:'$filePath'] from library '$name' with designator '$designator'"
    }

    /**
     * {EN}
     * Instantiate new module into device hierarchy into [parent] with module name [module]
     * and instance name [designator]. Parameter [builder] used just for exception handling and may be null.
     *
     * @param parent: parent module where to instantiate current
     * @param builder: file builder where module placed
     * @param module: plugin module name
     * @param designator: instance name
     * @param parameters: parameters for new module constructor
     * {EN}
     */
    fun instantiate(
            parent: Component?,
            builder: AFileModuleFactoryBuilder?,
            module: String,
            designator: String,
            parameters: Map<String, Any?>): Module {

        log.info {
            val nestLevel = parent?.getNestingLevel() ?: 0
            val tabs = if (nestLevel == 0) "" else "%${2 * nestLevel}s".format(" ")
            val pstr = if (parameters.isNotEmpty()) ", " + parameters.map { "${it.key}=${it.value}" }.joinToString(", ") else ""
            "$tabs$module(${parent?.name}, $designator$pstr)"
        }

        val factories = buildPeripheralFactory(module).asReversed()

        // TODO: this lovely piece of code is better then previous revision but still require refactoring...

        val preprocessed = parameters
                .map { (key, value) ->
                    // convert to InputParameter structure and check it validity
                    InputParameterInfo.fromKeyValue(key, value).also {
                        require(it.isTypeValid) {
                            "Incorrect parameter '${it.name}' and type '${it.type}' definition for ${getModuleDescString(builder, module, designator)}" +
                                    "\nUse only parameter name (for example 'data') or parameter name and type (for example 'data:String')" +
                                    "\nAvailable types is: ${settings.availableTypes.joinToString(separator = ", ")}"
                        }
                    }
                }.also { preprocessed ->
                    // validate that parameter present only one time
                    preprocessed
                            .groupBy { it.name }
                            .forEach { (name, values) ->
                                require(values.size == 1) {
                                    "Duplicate parameter name '$name' for ${getModuleDescString(builder, module, designator)}" +
                                            "\nParameters: $values"
                                }
                            }
                }

        val candidates = factories.map { factory -> SatisfactionChecker(factory, preprocessed) }

        val parsedFilteredFactories = candidates.filter { it.isSatisfy && it.parse() }

        require(parsedFilteredFactories.isNotEmpty()) {
            "No correct constructor for ${getModuleDescString(builder, module, designator)}" +
                    "\nUse one of the following constructors:" +
                    "\n${getHelpConstructorString(candidates)}"
        }

        val satisfied = parsedFilteredFactories.singleOrNull()

        require(satisfied != null) {
            "Ambiguous constructor definition for ${getModuleDescString(builder, module, designator)}" +
                    "\nBelow are the available options. " +
                    "\n${getHelpConstructorString(parsedFilteredFactories)}" +
                    "\nPlease specify one of these using the type definition." +
                    "\nIn JSON file you could use \"<name>:<type>\": \"<value>\" notation\n"
        }

        return with(satisfied) {
            log.fine { "Selected constructor is: (${factory.getPrintableParams()})" }
            factory.create(parent, designator, ordered)
        }
    }

    /**
     * {EN}
     * Instantiate new module into device hierarchy into [parent] with module name [module] and instance name [designator]
     *
     * @param parent: parent module where to instantiate current
     * @param module: plugin module name
     * @param designator: instance name
     * @param parameters: parameters for new module constructor as map
     * {EN}
     */
    fun instantiate(parent: Component?, module: String, designator: String, parameters: Map<String, Any?>) =
            instantiate(parent, null, module, designator, parameters)

    /**
     * {EN}
     * Instantiate new module into device hierarchy into [parent] with module name [module] and instance name [designator]
     *
     * @param parent: parent module where to instantiate current
     * @param module: plugin module name
     * @param designator: instance name
     * @param parameters: parameters for new module constructor as string in format "param1=value1,param2=value2"
     * {EN}
     */
    fun instantiate(parent: Component?, module: String, designator: String, parameters: String? = null) =
            instantiate(parent, module, designator, parseParametersAsString(parameters ?: ""))

    override fun toString(): String = "PeripheralFactoryLibrary[$name]"
}