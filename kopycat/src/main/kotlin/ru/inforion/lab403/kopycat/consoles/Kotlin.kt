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

import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.impl.completer.AggregateCompleter
import org.jline.reader.impl.completer.StringsCompleter
import org.jline.utils.AttributedString
import ru.inforion.lab403.common.proposal.kotlinScriptEngine
import ru.inforion.lab403.common.proposal.stringify
import ru.inforion.lab403.kopycat.Kopycat
import javax.script.ScriptContext.ENGINE_SCOPE
import javax.script.ScriptEngine


class Kotlin(val kopycat: Kopycat) : AConsole("Kotlin") {

    private lateinit var engine: ScriptEngine

    override fun onInitialize(): Boolean {
        try {
            engine = kotlinScriptEngine(
                    "kopycat" to kopycat,
                    "kc" to kopycat
            ).apply {
                eval("import kotlin.system.*")
                eval("import ru.inforion.lab403.common.extensions.*")
                eval("import ru.inforion.lab403.common.logging.*")
                eval("import ru.inforion.lab403.common.proposal.*")
                eval("fun exit(status: Int = 0) { exitProcess(status) }")
            }
        } catch (error: Throwable) {
            error.printStackTrace()
            return false
        }

        return true
    }

    override fun onReconfigure(): Boolean = true

    override fun onEval(statement: String) =
            engine.runCatching { eval(statement) }
                    .onSuccess { if (it != null) println(it) }
                    .onFailure { println(it.message) }
                    .isSuccess

    override fun onExecute(statement: String): Result {
        val result = engine.eval(statement)
        log.info { "result = $result" }
        return Result(0, result.toString())
    }

    override val working get() = kopycat.working

    private fun String.stripAnsi() = AttributedString.stripAnsi(this)

    private fun candidate(name: String, domain: String?, descr: String?): Candidate {
        val value = if (domain != null) "$domain.$name" else name
        return Candidate(value.stripAnsi(), name, domain, descr, null, null, true)
    }

    override val completer = AggregateCompleter(
            StringsCompleter(
                    "as", "class", "break", "continue", "do", "else",
                    "for", "fun", "false", "if", "in", "interface",
                    "super", "return", "object", "package", "null", "is",
                    "try", "throw", "true", "this", "typeof", "typealias",
                    "when", "while", "val", "var"),
            Completer { _, line, candidates ->
                val word = line.word()

                if ("." !in word) {
                    val bindings = engine.context.getBindings(ENGINE_SCOPE)
                    val variables = bindings.keys.map { candidate(it, null, null) }
                    candidates.addAll(variables)
                } else {
                    val domain = word.substringBeforeLast(".")
                    val name = word.substringAfterLast(".")

                    engine.runCatching { eval(domain) }.onSuccess { obj ->
                        if (obj != null) {
                            val members = obj::class.members.filter { it.name.startsWith(name) }
                            val result = if (members.size < 10) {
                                members.map { candidate(it.name, domain, it.stringify()) }
                            } else {
                                members.map { candidate(it.name, domain, null) }
                            }
                            candidates.addAll(result)
                        }
                    }.onFailure {
                        log.finest { "Can't eval(domain):\n${it.stackTraceToString()}" }
                    }
                }
            }
    )
}