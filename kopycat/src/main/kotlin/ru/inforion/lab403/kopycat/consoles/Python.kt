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
package ru.inforion.lab403.kopycat.consoles

import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.consoles.jep.JepInterpreter
import ru.inforion.lab403.kopycat.consoles.jep.JepLoader


class Python(val kopycat: Kopycat, val python: String = "python") : AConsole("Python") {

    private lateinit var jep: JepInterpreter

    override fun onInitialize() = runCatching {
        JepLoader.load(python)
        jep = JepInterpreter(true)
        jep.set("kopycat", kopycat)
        jep.eval("kc = kopycat")
        jep.eval("print(\"Jep starting successfully!\")")
    }.onFailure {
        val caused = if (it.cause != null) " caused by ${it.cause}" else ""
        log.severe { "Can't load JEP -> $it$caused" }
    }.isSuccess

    override fun onReconfigure(): Boolean = true

    override fun onEval(statement: String): Boolean {
        jep.eval(statement)
        return true
    }

    override fun onExecute(statement: String): Result {
        val result = jep.getValue(statement)
        log.info { "result = $result" }
        return Result(0, result.toString())
    }

    override val working get() = kopycat.working
}