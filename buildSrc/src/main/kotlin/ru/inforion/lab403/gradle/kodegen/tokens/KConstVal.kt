package ru.inforion.lab403.gradle.kodegen.tokens

import ru.inforion.lab403.gradle.kodegen.abstracts.VariableToken

class KConstVal(name: String, value: Any?) : VariableToken(name, value) {
    override fun render(builder: StringBuilder, indent: String) = builder.apply {
        append("${indent}const val $name = ")
        super.render(builder, indent)
    }
}