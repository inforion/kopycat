/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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
package ru.inforion.lab403.common.proposal

import ru.inforion.lab403.common.utils.DynamicClassLoader
import javax.script.*
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.jsr223.KotlinJsr223ScriptEngineImpl
import kotlin.script.experimental.util.PropertiesCollection

fun getScriptEngineByName(name: String): ScriptEngine = ScriptEngineManager().getEngineByName(name)

fun scriptEngineBindings(vararg values: Pair<String, Any?>) = SimpleBindings().apply { putAll(values) }

fun ScriptEngine.setBindings(vararg values: Pair<String, Any?>) =
        scriptEngineBindings(*values).also { setBindings(it, ScriptContext.ENGINE_SCOPE) }

fun kotlinScriptEngine(vararg objects: Pair<String, Any?>): ScriptEngine {
    val engine = getScriptEngineByName("kotlin").apply {
        setBindings(*objects)
    } as KotlinJsr223ScriptEngineImpl

    @Suppress("unchecked_cast")
    val properties = PropertiesCollection::class.java.getDeclaredField("properties").also {
        it.isAccessible = true
    }.get(engine.evaluationConfiguration) as MutableMap<PropertiesCollection.Key<*>, Any?>

    properties[ScriptingHostConfiguration.jvm.baseClassLoader] = DynamicClassLoader

    return engine
}