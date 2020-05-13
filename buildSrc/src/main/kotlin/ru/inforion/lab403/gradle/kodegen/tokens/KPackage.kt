package ru.inforion.lab403.gradle.kodegen.tokens

import ru.inforion.lab403.gradle.kodegen.Config
import ru.inforion.lab403.gradle.kodegen.abstracts.CompositeToken

class KPackage(
        config: Config,
        val name: String
) : CompositeToken(config, false) {

    fun obj(name: String, init: KObject.() -> Unit) = addToken(KObject(config, name), init)

    override fun render(builder: StringBuilder, indent: String) = builder.apply {
        append("${indent}package $name\n")
        super.render(builder, indent)
    }
}