package ru.inforion.lab403.common.proposal

import ru.inforion.lab403.common.logging.logger
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import java.util.logging.Level.FINE

class Shell(vararg val cmd: String, val timeout: Long = -1) {
    companion object {
        val log = logger(FINE)
    }

    var status: Int = 0
    var stdout = String()
    var stderr = String()

    private fun readout(stream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(stream))
        return reader.readLines().joinToString("\n")
    }

    fun execute(): Shell {
        log.finer { "Executing shell command: ${cmd.joinToString(" ")}" }
        val process = Runtime.getRuntime().exec(cmd)

        if (timeout == -1L)
            process.waitFor()
        else
            process.waitFor(timeout, TimeUnit.MILLISECONDS)

        status = process.exitValue()
        stdout = readout(process.inputStream)
        stderr = readout(process.errorStream)
        return this
    }
}