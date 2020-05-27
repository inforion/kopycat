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
package ru.inforion.lab403.gradle.kodegen.utils

import ru.inforion.lab403.gradle.kodegen.Kodegen
import ru.inforion.lab403.gradle.kodegen.tokens.KObject
import ru.inforion.lab403.gradle.kodegen.types.Raw
import java.io.File

/**
 * SIMPLE(!) source code loader
 */
class KotlinSourceLoader(val file: File) {
    private fun extractNameValue(string: String): Pair<String, Any?> {
        val tokens = string.split("=").map { it.trim() }
        val name = tokens[0].substringAfterLast(" ")

        val tmp = tokens[1]
        val value: Any? = when {
            tmp.startsWith("\"") -> tmp.removeSurrounding("\"")
            tmp.startsWith("'") -> tmp.removeSurrounding("'").toCharArray().first()
            else -> Raw(tmp)
        }

        return Pair(name, value)
    }

    private fun KObject.parseObjectBody(body: String) {
        body.lines().forEach {
            if (it.isBlank()) {
                newlines(1)
            } else {
                val line = it.trim()
                when {
                    line.startsWith("const val ") -> {
                        val (name, value) = extractNameValue(line.removePrefix("const val "))
                        constval(name, value)
                    }
                    line.startsWith("inline val ") -> {
                        val (name, value) = extractNameValue(line.removePrefix("const val "))
                        inlineval(name, value)
                    }
                    line.startsWith("//") -> {
                        -line.removePrefix("//")
                    }
                }
            }
        }
    }

    fun parse(): Kodegen {
        val text = file.readText()

        val packageName = text.substringAfter("package").substringBefore("\n").trim()
        val className = text.substringAfter("object").substringBefore("{").trim()

        val body = text.substringAfter("{").substringBefore("}")

        return Kodegen {
            pkg(packageName) {
                obj(className) {
                    parseObjectBody(body)
                }
            }
        }
    }
}