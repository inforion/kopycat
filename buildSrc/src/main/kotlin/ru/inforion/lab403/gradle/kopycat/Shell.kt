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