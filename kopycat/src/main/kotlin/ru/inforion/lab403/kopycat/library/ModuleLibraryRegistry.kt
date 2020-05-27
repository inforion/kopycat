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

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import ru.inforion.lab403.common.extensions.walk
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.exceptions.LibraryNotFoundError
import ru.inforion.lab403.kopycat.library.types.LibraryInfo
import ru.inforion.lab403.kopycat.settings
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Level


class ModuleLibraryRegistry constructor(vararg libraries: ModuleFactoryLibrary) {

    companion object {
        val log = logger(Level.INFO)

        private fun getSystemLibraries(internalClassDirectory: String): Array<ModuleFactoryLibrary> {
            val helper = ClasspathHelper.forPackage(internalClassDirectory)
            val reflections = Reflections(helper, SubTypesScanner())
            val types = reflections.getSubTypesOf(Module::class.java)
            val packages = types
                    .filter { it.name.startsWith("$internalClassDirectory.") }
                    .map { it.name
                            .removePrefix("$internalClassDirectory.")
                            .substringBefore(".")
                    }.toSet()
            return packages.map {
                ModuleFactoryLibrary.loadInternal(it, "$internalClassDirectory.$it")
            }.toTypedArray()
        }

        private fun getUserLibraries(paths: Map<String, String>): Array<ModuleFactoryLibrary> =
                paths.map { ModuleFactoryLibrary.loadPlugins(it.key, Paths.get(it.value).toString()) }.toTypedArray()

        private fun parsePluginsConfigLine(line: String): Map<String, String> = line
                .split(settings.librariesSeparator)
                .filter { it.isNotBlank() }
                .map {
                    val name = it.substringBefore(settings.libraryPathSeparator)
                    val value = it.substringAfter(settings.libraryPathSeparator)
                    name to value
                }.toMap()

        private fun parseRegistriesConfigLine(line: String): List<String> = line
                .split(",")
                .filter { it.isNotBlank() }

        /**
         * {EN}Enumerate specified plugins path and return string in format lib1:path/to/lib1,lib2:path/to/lib2{EN}
         *
         * {RU}Обходит заданный путь и создает строку для загрузки библиотек в формате lib1:path/to/lib1,lib2:path/to/lib2{RU}
         */
        private fun makePluginsConfigLine(line: String?): String {
            if (line == null)
                return ""

            val dirs = parseRegistriesConfigLine(line)

            return dirs.mapNotNull { path ->
                val dir = File(path)
                if (dir.isDirectory) dir else {
                    log.severe { "Directory '$dir' doesn't exist!" }
                    null
                }
            }.joinToString(separator = ",") { dir ->
                walk(dir, 0)
                        .filter { Files.isDirectory(it.toPath()) }
                        .toSet()
                        .joinToString(separator = ",") {
                            val libname = it.path.substringAfterLast(File.separator)
                            "$libname:${it.path}"
                        }
            }
        }

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
            val config = (libCfgLine ?: "") + "," + makePluginsConfigLine(regCfgLine)
            log.info { "Library configuration line: $config" }
            val paths = parsePluginsConfigLine(config)
            val userLibraries = getUserLibraries(paths)
            val libraries = if (system == null) userLibraries else userLibraries + getSystemLibraries(system)
            return ModuleLibraryRegistry(*libraries).load()
        }
    }

    private val libraries = libraries.associateBy { it.name }

    operator fun get(name: String) = libraries[name] ?: throw LibraryNotFoundError(name)

    /**
     * {EN}Get registry top modules only{EN}
     */
    fun getAvailableTopModules() = libraries
            .map { (name, lib) -> LibraryInfo(name, lib, lib.getAvailableTopModules(this)) }
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
}