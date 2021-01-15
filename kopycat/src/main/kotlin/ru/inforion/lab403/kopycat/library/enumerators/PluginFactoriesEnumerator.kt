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
package ru.inforion.lab403.kopycat.library.enumerators

import ru.inforion.lab403.common.extensions.flatten
import ru.inforion.lab403.common.extensions.walk
import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.library.builders.*
import ru.inforion.lab403.kopycat.library.builders.api.AFileModuleFactoryBuilder
import java.io.File
import java.nio.file.Files

class PluginFactoriesEnumerator(private val userPluginsDirectory: String) : IFactoriesEnumerator {
    companion object {
        @Transient val log = logger(FINER)
    }

    private val buildersClasses = arrayOf(
            ::JsonModuleFactoryBuilder,
            ::ClassModuleFactoryBuilder,
            ::JarModuleFactoryBuilder,
            ::XmlModuleFactoryBuilder,
            ::ProtobufModuleFactoryBuilder)

    private val builders = ArrayList<AFileModuleFactoryBuilder>()

    override fun preload() = walk(File(userPluginsDirectory).absoluteFile)
            .filter { Files.isRegularFile(it.toPath()) }
            .map { it.absolutePath }
            .toMutableSet()
            .forEach { path ->
                builders += buildersClasses
                        .map { makeBuilder -> makeBuilder(path, null) }
                        .filter { builder -> builder.preload() }
            }

    override fun load() = builders
            .filter { it.load() }
            .map { builder -> builder.plugins.associateWith { builder } }
            .flatten()
}
