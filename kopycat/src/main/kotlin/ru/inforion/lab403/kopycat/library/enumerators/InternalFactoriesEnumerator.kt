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

import org.reflections.util.ClasspathHelper
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.proposal.subtypesScan
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.builders.ClassModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactoryBuilder
import java.util.logging.Level

class InternalFactoriesEnumerator(private val internalClassDirectory: String) : IFactoriesEnumerator {
    companion object {
        @Transient val log = logger(Level.INFO)

        val anonymousClassPattern = Regex("""\$.*\$""")
    }

    private lateinit var builders: List<ClassModuleFactoryBuilder>

    override fun preload() {
        builders = subtypesScan<Module>(internalClassDirectory)
                .filter { it.name.startsWith(internalClassDirectory) && !it.name.contains(anonymousClassPattern)}
                .map { ClassModuleFactoryBuilder(it) }
    }

    override fun load(): Map<String, IModuleFactoryBuilder> {
        val result = builders
                .filter { it.load() }
                .fold(listOf<Pair<String, IModuleFactoryBuilder>>()) { acc, builder ->
                    acc + builder.plugins.map { it to builder }
                }

        // Name clashes verification
        val uniqueNames = result.map { it.first }.toSet()
        uniqueNames.forEach { name ->
            if (result.count { it.first == name } > 1)
                log.warning { "$name duplicated during loading factories builders!" }
        }

        return result.toMap()
    }
}
