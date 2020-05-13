package ru.inforion.lab403.kopycat.consoles

import ru.inforion.lab403.common.logging.logger
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import kotlin.system.measureNanoTime

abstract class AConsole(name: String): Thread(name) {
    companion object {
        val log = logger(Level.INFO)
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

    private var isInitSuccess: Boolean = false
    private var isInitDone: Boolean = false
    private var isFinished: Boolean = false

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
                        log.severe { "Unexpected exception occurred during command execution..." }
                        error.printStackTrace()
                        qOutput.put(Result(-1, error.message))
                    }
                }
            }
        }

        isFinished = true
    }

    init {
        start()
    }
}