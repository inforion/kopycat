package ru.inforion.lab403.gradle.kodegen

import ru.inforion.lab403.gradle.common.safeWriteCode
import ru.inforion.lab403.gradle.kodegen.abstracts.CompositeToken
import ru.inforion.lab403.gradle.kodegen.tokens.KPackage
import java.io.File


class Kodegen constructor(lazy: Boolean = false, config: Config, val init: Kodegen.() -> Unit) :
        CompositeToken(config, false) {

    constructor(lazy: Boolean = false, indentSize: Int = 4, init: Kodegen.() -> Unit):
            this(lazy, Config(indentSize), init)

    fun pkg(name: String, init: KPackage.() -> Unit) = addToken(KPackage(config, name), init)

    init {
        if (!lazy) {
            this.init()
        }
    }

    fun write(file: File) = file.safeWriteCode(this)
}