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

import org.reflections.util.ClasspathHelper
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.common.proposal.subtypesScan
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.builders.JsonModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.text.PluginConfig
import ru.inforion.lab403.kopycat.library.exceptions.LibraryNotFoundError
import ru.inforion.lab403.kopycat.library.types.LibraryInfo
import ru.inforion.lab403.kopycat.settings
import java.io.File
import java.io.InputStream
import java.util.logging.Level


class ModuleLibraryRegistry constructor(
        val regCfgLine: String?,
        val libCfgLine: String?,
        vararg libraries: ModuleFactoryLibrary
) {
    companion object {
        @Transient
        val log = logger(CONFIG)

        private val systemJarOrClassPath = this::class.java.location

        private fun getLibraryNameFromClasspath(cls: Class<*>, prefix: String) =
                cls.name.removePrefix("$prefix.").substringBefore(".")

        private fun loadSystemLibraries(internalClassDirectory: String): Array<ModuleFactoryLibrary> {
            val packages = subtypesScan<Module>(internalClassDirectory)
                    .filter {
                        // we should not load in system loader any classes that not in .../main or kopycat-X.Y.AB.jar
                        it.name.startsWith("$internalClassDirectory.") && (
                                it.location == systemJarOrClassPath ||
                                        it.location.endsWith(settings.systemModulesMainPath) ||
                                        it.location.endsWith(settings.systemModulesTestPath))
                    }.map {
                        getLibraryNameFromClasspath(it, internalClassDirectory)
                    }.distinct()

            return packages.map { lib ->
                ModuleFactoryLibrary.loadInternal(lib, "$internalClassDirectory.$lib")
            }.toTypedArray()
        }

        private fun loadUserLibraries(paths: Map<String, String>) = paths.map { (lib, where) ->
            if (where.isDirectory())
                ModuleFactoryLibrary.loadPlugins(lib, where)
            else
                ModuleFactoryLibrary.loadInternal(lib, where)
        }.toTypedArray()

        private fun parseFilesystemRegistry(dir: String) = dir
                .listdir { it.isDirectory }
                .map { it.path.substringAfterLast(File.separator) to it.path }
                .distinct()

        private fun <T>Class<out T>.isModuleClasspath(dir: String) =
                name.startsWith("$dir.") && location != systemJarOrClassPath

        private fun parseClasspathRegistry(dir: String) = subtypesScan<Module>(dir)
                .filter { cls ->
                    cls.isModuleClasspath(dir).also { log.finest { "Found class: $cls -> module: $it" } }
                }.map {
                    val name = getLibraryNameFromClasspath(it, dir)
                    name to "$dir.$name"
                }.distinct()

        /**
         * {EN}
         * Enumerate specified plugins path and return string in format lib1:path/to/lib1,lib2:path/to/lib2
         *
         * Registry can be filesystem path or classpath in Java, i.e.
         *   options 1: path/to/registry
         *   options 2: path.to.registry
         *
         * @param line registries string separated by comma, e.g.: path/to/registry1,path.to.registry2
         * {EN}
         *
         * {RU}
         * Обходит заданный путь и создает строку для загрузки библиотек в формате lib1:path/to/lib1,lib2:path/to/lib2
         *
         * Реестр может быть как путем в файловой системе, так и путем в пакетах Java, то есть
         *   вариант 1: path/to/registry
         *   вариант 2: path.to.registry
         *
         * @param line строка с реестрами заданными через запятую, например: path/to/registry1,path.to.registry2
         * {RU}
         */
        private fun parseRegistriesLine(line: String) = line
                .splitBy(settings.registriesSeparator)
                .distinct()
                .mapNotNull { where ->
                    when {
                        where.isDirectory() -> parseFilesystemRegistry(where)
                        ClasspathHelper.forPackage(where).isNotEmpty() -> parseClasspathRegistry(where)
                        else -> null
                    }
                }.flatten().toMap()

        private fun parseLibrariesLine(line: String) = line
                .splitBy(settings.librariesSeparator)
                .map {
                    val name = it.substringBefore(settings.libraryPathSeparator)
                    val value = it.substringAfter(settings.libraryPathSeparator)
                    name to value
                }.toMap()

        /**
         * {EN}Loading module library registry from specified comma-separated plugins path and system class path{EN}
         *
         * {RU}
         * Загружает и создает реестр модулей и плагинов для использования в эмуляторе из системного classpath
         * и из пути указанного в [libCfgLine] в виде lib1:path/to/lib1,lib2:path/to/lib2 и [regCfgLine] в виде
         * path/to/reg1,path/to/reg2,path/to/reg3
         * {RU}
         */
        fun create(
                regCfgLine: String? = null,
                libCfgLine: String? = null,
                system: String? = settings.internalModulesClasspath
        ): ModuleLibraryRegistry {
            // parse registries and libraries lines specified by user and append to it
            // build-in registry path to load only module that placed outside(!) main jar file
            val registries = if (regCfgLine != null) "$regCfgLine,${settings.internalModulesClasspath}" else settings.internalModulesClasspath

            val specifiedRegistriesPaths = parseRegistriesLine(registries)
            val specifiedLibrariesPaths = if (libCfgLine != null) parseLibrariesLine(libCfgLine) else emptyMap()

            // mix it all up together
            val paths = specifiedRegistriesPaths + specifiedLibrariesPaths

            log.fine { "Loading user libraries: $paths" }

            // load libraries outside main jar
            val externalLibraries = loadUserLibraries(paths)
            // load internal system libraries
            val systemLibraries = if (system != null) loadSystemLibraries(system) else emptyArray()

            val libraries = externalLibraries + systemLibraries
            return ModuleLibraryRegistry(regCfgLine, libCfgLine, *libraries).load()
        }
    }

    private val libraries = libraries.associateBy { it.name }

    operator fun get(name: String): ModuleFactoryLibrary {
        val library = libraries[name]
        if (library == null) {
            val modules = getAvailableAllModules()
            log.severe { modules.joinToString("\n") }
            throw LibraryNotFoundError(name)
        }
        return library
    }

    /**
     * {EN}Get registry top modules only{EN}
     */
    fun getAvailableTopModules() = libraries
            .map { (name, lib) -> LibraryInfo(name, lib, lib.getAvailableTopModules()) }
            .filter { it.modules.isNotEmpty() }

    /**
     * {EN}Get registry all modules{EN}
     */
    fun getAvailableAllModules() = libraries
            .map { (name, lib) -> LibraryInfo(name, lib, lib.getAvailableAllModules()) }
            .filter { it.modules.isNotEmpty() }

    fun load(): ModuleLibraryRegistry {
        with (libraries.values) {
            forEach { it.register(this@ModuleLibraryRegistry) }
            forEach { it.preload() }
            forEach { it.load() }
        }
        return this
    }

    /**
     * {EN}Load module with [name] from specified json in [stream] and parameters [parameters]{EN}
     */
    fun json(parent: Module?, stream: InputStream, name: String, vararg parameters: Any?) =
            JsonModuleFactoryBuilder.JsonModule(null, this, parent, name, stream.parseJson(), *parameters)

    /**
     * {EN}Load module with [name] from specified json with path [path] and parameters [parameters]{EN}
     */
    fun json(parent: Module?, path: String, name: String, vararg parameters: Any?) =
            json(parent, path.toFile().inputStream(), name, *parameters)

    fun json(parent: Module?, name: String, config: PluginConfig, vararg parameters: Any?) =
            JsonModuleFactoryBuilder.JsonModule(null, this, parent, name, config, *parameters)
}