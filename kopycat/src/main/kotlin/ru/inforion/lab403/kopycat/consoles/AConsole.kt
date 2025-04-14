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
package ru.inforion.lab403.kopycat.consoles

import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine
import org.jline.reader.Parser
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

abstract class AConsole(name: String): Thread(name) {
    companion object {
        @Transient val log = logger(INFO)
    }

    sealed class ARequest
    sealed class AResult

    data class EvalRequest(val data: String) : ARequest()
    data class ExecRequest(val data: String) : ARequest()
    data class CompleteRequest(
        val reader: LineReader?,
        val line: ParsedLine?,
        val candidates: MutableList<Candidate>,
    ) : ARequest()

    data class Result(val status: Int, val message: String?) : AResult()
    data object CompleteResult : AResult()

    private val measureInterval = 5 // seconds

    private val qInit = LinkedBlockingQueue<Boolean>(1)
    private val qInput = LinkedBlockingQueue<ARequest>(1)
    private val qOutput = LinkedBlockingQueue<Result>(2)
    private val qCompleteOutput = LinkedBlockingQueue<CompleteResult>(1)

    fun reconfigure(): Boolean = onReconfigure()

    fun eval(expression: String): Result {
        qInput.put(EvalRequest(expression))
        return qOutput.take()
    }

    /**
     * Чтобы использовать REPL внутри REPL-а
     */
    fun evalNoTake(expression: String) {
        qInput.put(EvalRequest(expression))
    }

    fun execute(statement: String): Result {
        qInput.put(ExecRequest(statement))
        return qOutput.take()
    }

    /** Beware of race conditions! */
    protected fun complete(reader: LineReader?, line: ParsedLine?, candidates: MutableList<Candidate>): CompleteResult {
        qInput.put(CompleteRequest(reader, line, candidates))
        return qCompleteOutput.take()
    }

    fun initialized(): Boolean {
        if (isInitDone)
            return isInitSuccess
        return qInit.take()
    }

    private var isInitSuccess = false
    private var isInitDone = false
    private var isFinished = false

    abstract val working: Boolean

    protected abstract fun onReconfigure(): Boolean
    protected abstract fun onInitialize(): Boolean
    protected abstract fun onEval(statement: String): Boolean
    protected abstract fun onExecute(statement: String): Result
    protected open fun onComplete(
        reader: LineReader?,
        line: ParsedLine?,
        candidates: MutableList<Candidate>,
    ) = Unit

    final override fun run() {
        var hostTime = 0L

        isInitSuccess = onInitialize()
        qInit.put(isInitSuccess)
        isInitDone = true

        if (!isInitSuccess) {
            log.warning { "$name initialization error, terminating console..." }
            isFinished = true
            return
        }

        while (working) {
            hostTime += measureNanoTime {
                val request = qInput.poll(1000, TimeUnit.MILLISECONDS)
                if (request != null) {
                    try {
                        when (request) {
                            is EvalRequest -> {
                                log.fine { "Evaluate command line: ${request.data}" }
                                val status = onEval(request.data)
                                if (!qOutput.isEmpty()) {
                                    log.warning { "qOutput is not empty" }
                                } else {
                                    qOutput.put(Result(if (status) 0 else -1, null))
                                }
                            }
                            is ExecRequest -> {
                                log.fine { "Execute command line: ${request.data}" }
                                val result = onExecute(request.data)
                                if (!qOutput.isEmpty()) {
                                    log.warning { "qOutput is not empty" }
                                } else {
                                    qOutput.put(Result(0, result.toString()))
                                }
                            }
                            is CompleteRequest -> {
                                onComplete(request.reader, request.line, request.candidates)
                                if (!qCompleteOutput.isEmpty()) {
                                    log.warning { "qCompleteOutput is not empty" }
                                } else {
                                    qCompleteOutput.put(CompleteResult)
                                }
                            }
                        }
                    } catch (error: Exception) {
                        // TODO: Make configurable print stack trace
                        // log.severe { "Unexpected exception occurred during command execution..." }
                        // error.logStackTrace(log)

                        when (request) {
                            is EvalRequest, is ExecRequest -> if (!qOutput.isEmpty()) {
                                log.warning { "qOutput is not empty" }
                            } else {
                                qOutput.put(Result(-1, error.toString()))
                            }
                            is CompleteRequest -> if (!qOutput.isEmpty()) {
                                log.warning { "qCompleteOutput is not empty" }
                            } else {
                                qCompleteOutput.put(CompleteResult)
                            }
                        }
                    }
                }
            }
        }

        isFinished = true
    }

    open val completer: Completer? = null
    open val parser: Parser? = null

    init {
        start()
    }
}