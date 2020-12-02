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
import ru.inforion.lab403.common.extensions.DynamicClassLoader
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import ru.inforion.lab403.kopycat.library.builders.api.AFileModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactory
import ru.inforion.lab403.kopycat.library.exceptions.WrongModuleNameError
import ru.inforion.lab403.kopycat.settings
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.util.jar.JarEntry
import java.util.jar.JarInputStream


class JarModuleFactoryBuilder(path: String, val jar: File?) : AFileModuleFactoryBuilder(path) {
    private lateinit var builders: Map<String, AFileModuleFactoryBuilder>

    private fun enumJarEntries(jar: File): Sequence<JarEntry> {
        val jis = JarInputStream(jar.inputStream())
        return generateSequence { jis.nextJarEntry }
    }

    override val plugins get() = builders.keys

    override fun preload(): Boolean {
        val jar = File(path)

        if (jar.extension != settings.jarFileExt && jar.extension != settings.zipFileExt) {
            log.finest { "Can't load file ${jar.path} -> only jar and zip supported" }
            return false
        }

        DynamicClassLoader.loadIntoClasspath(jar)

        return true
    }

    override fun load(): Boolean {
        val jar = File(path)

        log.finer { "Loading $jar" }

        val exportFileUrl = jar.getInternalFileURL(settings.exportFilename)

        val dirs = try {
            val stream = exportFileUrl.openStream()
            val reader = InputStreamReader(stream)
            reader.readLines()
        } catch (error: FileNotFoundException) {
            log.warning { "Jar $jar doesn't contain export.txt file, so it can be performance disgrace!" }
            emptyList()
        }

        builders = enumJarEntries(jar)
                .filter {
                    !it.isDirectory
                            // $ in filename is bad sign (used by Kotlin in nested classes)
                            && "$" !in it.name
                            // Module not in export directory
                            && (dirs.isEmpty() || dirs.any { d -> it.name.startsWith(d) })
                }
                .mapNotNull {
                    when {
                        it.name.endsWith(settings.classFileExt) -> ClassModuleFactoryBuilder(it.name, jar)
                        it.name.endsWith(settings.jsonFileExt) -> JsonModuleFactoryBuilder(it.name, jar)
                        else -> null
                    }
                }
                .filter { it.load() }
                .associateBy { it.plugins.first() }

        return builders.isNotEmpty()
    }

    fun getClasspath(module: String): String {
        val builder = builders[module] ?: throw WrongModuleNameError(module)
        return builder.path.replace(File.separator, ".").removeSuffix(".class")
    }

    override fun factory(name: String, registry: ModuleLibraryRegistry): List<IModuleFactory> {
        val builder = builders[name] ?: throw WrongModuleNameError(name)
        return builder.factory(name, registry)
    }
}