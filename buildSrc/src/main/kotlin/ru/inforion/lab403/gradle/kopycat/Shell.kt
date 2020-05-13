package ru.inforion.lab403.gradle.kopycat

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class Shell(vararg val cmd: String) {
    var status: Int = 0
    var stdout = String()
    var stderr = String()

    private fun readout(stream: InputStream): String {
        val reader = BufferedReader(InputStreamReader(stream))
        return reader.readLines().joinToString("\n")
    }

    fun execute(): Shell {
        val process = try {
            Runtime.getRuntime().exec(cmd)
        } catch (error: IOException) {
            status = -1
            stdout = ""
            error.message?.let { stderr = it }
            return this
        }
        status = process.waitFor()
        stdout = readout(process.inputStream)
        stderr = readout(process.errorStream)
        return this
    }
}