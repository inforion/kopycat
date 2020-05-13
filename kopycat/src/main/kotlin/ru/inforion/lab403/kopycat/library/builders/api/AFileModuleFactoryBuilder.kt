package ru.inforion.lab403.kopycat.library.builders.api

import ru.inforion.lab403.common.logging.logger
import java.util.logging.Level

abstract class AFileModuleFactoryBuilder(val path: String) : IModuleFactoryBuilder {
    companion object {
        val log = logger(Level.INFO)
    }

    override fun toString(): String = "${javaClass.simpleName}[$path]"

    abstract fun getFilePath(): String
}