package ru.inforion.lab403.gradle.kodegen.tokens

import ru.inforion.lab403.gradle.kodegen.abstracts.VariableToken

class KInlineVal(name: String, value: Any?) : VariableToken(name, value) {
    override fun render(builder: StringBuilder, indent: String) = builder.apply {
        append("${indent}inline val $name get() = ")
        super.render(builder, indent)
    }
}