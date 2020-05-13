package ru.inforion.lab403.kopycat.interactive

import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.consoles.AConsole
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.logging.Level
import kotlin.concurrent.thread



object REPL {
    val log = logger(Level.INFO)

    private val repl = thread(false, name = "REPL") {
        val c = console ?: throw IllegalStateException("Console wasn't set but REPL thread started!")
        val istream = InputStreamReader(System.`in`)
        val reader = BufferedReader(istream)
        reader.use {
            while (c.working) {
                print("${c.name} > ")
                val line = reader.readLine()
                eval(line)
            }
            log.info { "Goodbye! See you..." }
        }
    }

    private var console: AConsole? = null
    private val lock = java.lang.Object()

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