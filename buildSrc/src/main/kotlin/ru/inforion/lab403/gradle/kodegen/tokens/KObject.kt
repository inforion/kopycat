package ru.inforion.lab403.gradle.kodegen.tokens

import ru.inforion.lab403.gradle.kodegen.Config
import ru.inforion.lab403.gradle.kodegen.abstracts.CompositeToken

class KObject(config: Config, val name: String) : CompositeToken(config, true) {

    fun inlineval(name: String, value: Any?)  = addToken(KInlineVal(name, value))
    fun constval(name: String, value: Any?) = addToken(KConstVal(name, value))

    override fun render(builder: StringBuilder, indent: String) = builder.apply {
        append("${indent}object $name {\n")
        super.render(builder, indent)
        append("${indent}}")
    }
}