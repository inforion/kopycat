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
package ru.inforion.lab403.kopycat.consoles.jep

import ru.inforion.lab403.common.extensions.DynamicClassLoader

class JepInterpreter(redirectOutputStreams: Boolean) {
    private val configCls = DynamicClassLoader.loadClass("jep.JepConfig")
    private val configSetRedirectOutputStreams = configCls.getMethod("setRedirectOutputStreams", Boolean::class.java)

    private val interpCls = DynamicClassLoader.loadClass("jep.SharedInterpreter")
    private val interpSet = interpCls.getMethod("set", String::class.java, Any::class.java)
    private val interpEval = interpCls.getMethod("eval", String::class.java)
    private val interpGetValue = interpCls.getMethod("getValue", String::class.java)

    // Static method
    private val interpSetConfig = interpCls.getMethod("setConfig", configCls)

    private val interp: Any

    fun eval(statement: String) = interpEval.invoke(interp, statement)
    fun getValue(statement: String) = interpGetValue.invoke(interp, statement)
    fun set(name: String, value: Any) = interpSet.invoke(interp, name, value)

    init {
        val cfg = configCls.getDeclaredConstructor().newInstance()
        configSetRedirectOutputStreams.invoke(cfg, redirectOutputStreams)
        interpSetConfig(null, cfg)

        interp = interpCls.getDeclaredConstructor().newInstance()
    }
}