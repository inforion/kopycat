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
package ru.inforion.lab403.kopycat.interactive.protocols

import io.javalin.Javalin
import io.javalin.core.plugin.Plugin
import ru.inforion.lab403.common.extensions.applyRoutes
import ru.inforion.lab403.common.extensions.getAny
import ru.inforion.lab403.common.extensions.postAny
import ru.inforion.lab403.kopycat.consoles.AConsole

class ConsoleRestProtocol(private val console: AConsole): Plugin {
    override fun apply(app: Javalin) = app.applyRoutes {
        getAny("console/name") { console.name }

        postAny("console/eval") {
            val expression = it.bodyAsClass(String::class.java)
            console.eval(expression)
        }

        postAny("console/execute") {
            val statement = it.bodyAsClass(String::class.java)
            console.execute(statement)
        }
    }
}

