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

import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import ru.inforion.lab403.kopycat.library.builders.api.AFileModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactory
import ru.inforion.lab403.kopycat.library.builders.api.ModuleParameterInfo
import java.io.File

class XmlModuleFactoryBuilder(path: String, val jar: File?) : AFileModuleFactoryBuilder(path) {
    override fun plugins(): Set<String> = emptySet()

    override fun load(): Boolean = false

    override fun factory(pluginName: String, registry: ModuleLibraryRegistry): List<IModuleFactory> {
        return listOf(object : IModuleFactory {
            override val canBeTop: Boolean
                get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

            override val parameters: List<ModuleParameterInfo>
                get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

            override fun create(parent: Component?, name: String, vararg parameters: Any?): Module {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
    }

    override fun getFilePath(): String = TODO("This case is unexpected.")
}