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