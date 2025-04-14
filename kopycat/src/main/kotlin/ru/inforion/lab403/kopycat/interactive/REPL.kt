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
package ru.inforion.lab403.kopycat.interactive

import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.PrintAboveWriter
import org.jline.reader.UserInterruptException
import org.jline.reader.impl.DefaultParser
import org.jline.terminal.TerminalBuilder
import ru.inforion.lab403.common.extensions.ifItNotNull
import ru.inforion.lab403.common.extensions.sure
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.consoles.AConsole
import java.io.File
import java.io.PrintWriter
import java.io.Writer
import java.nio.file.Path
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock


class REPL private constructor(
    private var console: AConsole,
    private var initScript: File? = null,
    private val historyFilePath: Path? = null,
    private val onWriterAvailable: (PrintWriter) -> Unit = { },
) {
    val repl = thread(false, name = "REPL") {
        console.sure { "Console wasn't set but REPL thread started!" }.run {
            val terminal = TerminalBuilder.terminal()
            val reader = LineReaderBuilder.builder()
                .variable(
                    LineReader.HISTORY_FILE,
                    historyFilePath
                )
                .terminal(terminal)
                .completer(completer)
                .parser(parser ?: DefaultParser())
                .build()

            reader.option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
            reader.unsetOpt(LineReader.Option.INSERT_TAB)
            reader.autosuggestion = LineReader.SuggestionType.COMPLETER

            // TODO: if file exist
            try {
                initScript ifItNotNull {
                    log.severe { "Run initial script: ${initScript?.absolutePath}" }
                    silentEval(initScript?.readText() + "\n")
                }
            } catch (e: java.io.FileNotFoundException) {
                log.severe { "${e.message}" }
            }

            onWriterAvailable(PrintWriter(PrintAboveWriter(reader), false))
            while (working) {
                try {
                    reader.readLine("$name > ")
                } catch (e: UserInterruptException) {
                    log.severe { "catch ctrl+c interrupt"}
                    e.printStackTrace()
                    null
                } catch (e: Exception) {
                    log.severe { "Something went wrong ($e): ${e.message}"}
                    null
                }?.also { line -> eval(line) }
            }
            log.info { "Goodbye! See you..." }
        }
    }

    private val lock = ReentrantLock()

    fun eval(line: String): AConsole.Result = lock.also {
        if (it.isLocked) {
            log.warning { "Evaluation multi-thread lock is already locked, waiting!" }
        }
    }.withLock {
        val c = instance?.console ?: return AConsole.Result(-1, "Console wasn't set in REPL!")
        return c.eval(line)
    }

    /**
     * Нужен для многопоточного выполнения (чтобы использовать REPL внутри REPL-а)
     */
    fun silentEval(line: String): AConsole.Result {
        if (lock.isLocked) {
            throw IllegalStateException("Evaluation multi-thread lock is locked. Cannot do nested REPL evals")
        } else {
            lock.withLock {
                val c = instance?.console ?: return AConsole.Result(-1, "Console wasn't set in REPL!")
                c.evalNoTake(line)
                return AConsole.Result(0, "Silent eval complete")
            }
        }
    }

    companion object {
        @Transient
        val log = logger(INFO)

        private var instance: REPL? = null

        fun getOrNull(): REPL? = instance

        fun get(): REPL = instance ?: let {
            throw IllegalStateException("REPL does not exist")
        }

        fun getOrCreate(
            console: AConsole,
            script: File? = null,
            historyFilePath: Path,
            onWriterAvailable: (Writer) -> Unit = { },
        ) = instance ?: create(console, script, historyFilePath, onWriterAvailable)

        fun create(
            console: AConsole,
            script: File? = null,
            historyFilePath: Path?,
            onWriterAvailable: (Writer) -> Unit = { },
        ) = if (instance != null) {
            throw IllegalStateException("REPL has been already created")
        } else {
            REPL(console, script, historyFilePath, onWriterAvailable).also {
                instance = it
                it.repl.start()
            }
        }
    }
}
