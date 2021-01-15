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

import org.jline.reader.Completer
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import kotlin.system.measureNanoTime

abstract class AConsole(name: String): Thread(name) {
    companion object {
        @Transient val log = logger(INFO)
    }

    enum class RequestType { EVAL, EXECUTE }

    data class Request(val type: RequestType, val data: String)

    data class Result(val status: Int, val message: String?)

    private val measureInterval = 5 // seconds

    private val qInit = LinkedBlockingQueue<Boolean>(1)
    private val qInput = LinkedBlockingQueue<Request>(1)
    private val qOutput = LinkedBlockingQueue<Result>(1)

    fun reconfigure(): Boolean = onReconfigure()

    fun eval(expression: String): Result {
        qInput.put(Request(RequestType.EVAL, expression))
        return qOutput.take()
    }

    fun execute(statement: String): Result {
        qInput.put(Request(RequestType.EXECUTE, statement))
        return qOutput.take()
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
                        when (request.type) {
                            RequestType.EVAL -> {
                                log.fine { "Execute command line: ${request.data}" }
                                val status = onEval(request.data)
                                qOutput.put(Result(if(status) 0 else -1, null))
                            }
                            RequestType.EXECUTE -> {
                                log.fine { "Execute command line: ${request.data}" }
                                val result = onExecute(request.data)
                                qOutput.put(Result(0, result.toString()))
                            }
                        }
                    } catch (error: Exception) {
                        // TODO: Make configurable print stack trace
                        // log.severe { "Unexpected exception occurred during command execution..." }
                        // error.printStackTrace()
                        qOutput.put(Result(-1, error.toString()))
                    }
                }
            }
        }

        isFinished = true
    }

    open val completer: Completer? = null

    init {
        start()
    }
}