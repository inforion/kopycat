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
package ru.inforion.lab403.kopycat.auxiliary

import ru.inforion.lab403.kopycat.cores.base.common.Module
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread



data class Socat(val process: Process, val pty0: String, val pty1: String) {
    companion object {
        private const val SOCAT_PREFIX = "socat:"

        /**
         * {RU}
         * Парсит строку вида:
         *  2018/12/13 10:21:14 socat[9302] N PTY is /dev/ttys000
         * и извлекает из нее путь к созданому терминалу
         *
         * @param line строка вывода socat
         *
         * @return путь к терминалу
         * {RU}
         */
        private fun parseSocatOutput(line: String): String {
            val sign = "N PTY is "

            val tmp = line.split(sign)
            if (tmp.size < 2)
                throw RuntimeException("Socat output is invalid: $line")

            return tmp[1]
        }

        /**
         * {RU}
         * Создает процесс socat и возвращает псевдо-терминалы для обмена данными
         *
         * @param url путь к tty устройству, который нужно создать с префиксом "socat:" иначе будет возвращен null
         *
         * @return класс Socat, в который включены терминалы и сам процесс
         * {RU}
         */
        fun createPseudoTerminal(module: Module, url: String): Socat? {
            if (!url.startsWith(SOCAT_PREFIX))
                return null

            val tty = url.substringAfter(SOCAT_PREFIX)

            val osName = System.getProperty("os.name")
            if (osName.toLowerCase().startsWith("win")) {
                throw NotImplementedError("Automatic creation of virtual terminal for Windows systems not supported!")
            }

            val comm = if (tty.isNotBlank())
                "socat -d -d pty,raw,echo=0,link=${tty}_in pty,raw,echo=0,link=$tty"
            else
                "socat -d -d pty,raw,echo=0 pty,raw,echo=0"

            val runtime = Runtime.getRuntime()
            val process = runtime.exec(comm.split(" ").toTypedArray())

            // wait if any errors occurred
            process.waitFor(100, TimeUnit.MILLISECONDS)

            val reader = process.errorStream.bufferedReader()

            if (!process.isAlive) {
                val error = reader.readLine()
                throw RuntimeException("Command execution failed:\n$comm\n$error")
            }

            val pty0 = parseSocatOutput(reader.readLine())
            val pty1 = parseSocatOutput(reader.readLine())

            Module.log.warning { "Pseudo-terminals created for $module: $pty0 and $pty1" }

            Runtime.getRuntime().addShutdownHook(thread(false) {
                Module.log.info { "Stop forcibly Socat: $process" }
                process.destroyForcibly()
            })

            return Socat(process, pty0, pty1)
        }
    }
}
