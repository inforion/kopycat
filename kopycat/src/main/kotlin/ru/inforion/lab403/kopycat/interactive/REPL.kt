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
package ru.inforion.lab403.kopycat.interactive

import org.jline.reader.LineReaderBuilder
import org.jline.terminal.TerminalBuilder
import ru.inforion.lab403.common.extensions.sure
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.consoles.AConsole
import java.util.logging.Level
import kotlin.concurrent.thread


object REPL {
    @Transient val log = logger(INFO)

    private val repl = thread(false, name = "REPL") {
        console.sure { "Console wasn't set but REPL thread started!" }.run {
            val terminal = TerminalBuilder.terminal()
            val reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .build()

            while (working) {
                val line = reader.readLine("$name > ")
                eval(line)
            }
            log.info { "Goodbye! See you..." }
        }
    }

    private var console: AConsole? = null
    private val lock = Object()

    fun eval(line: String): AConsole.Result = synchronized(lock) {
        val c = console ?: return AConsole.Result(-1, "Console wasn't set in REPL!")
        return c.eval(line)
    }

    // Constructor
    operator fun invoke(console: AConsole): REPL {
        if (repl.isAlive) {
            log.warning { "REPL thread already started, can't change console! Console is ${this.console!!.name}!" }
            return this
        }

        this.console = console
        repl.start()

        return this
    }
}