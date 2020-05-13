package ru.inforion.lab403.gradle.kodegen.abstracts

import ru.inforion.lab403.gradle.kodegen.types.Raw

abstract class VariableToken(val name: String, val value: Any?) : FinalToken() {

    private fun stringify(name: String, value: Any?) = when(value) {
        null -> "null"

        is Char -> "\'$value\'"
        is String -> "\"$value\""

        is Byte, is Short, is Int, is Long, is Boolean -> value.toString()

        is Raw -> value.string

        else -> throw IllegalStateException("Unsupported type $name: ${value.javaClass.simpleName} = $value")
    }

    override fun render(builder: StringBuilder, indent: String) =
            builder.apply { append(stringify(name, value)) }
}