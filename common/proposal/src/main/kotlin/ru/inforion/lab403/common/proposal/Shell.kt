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