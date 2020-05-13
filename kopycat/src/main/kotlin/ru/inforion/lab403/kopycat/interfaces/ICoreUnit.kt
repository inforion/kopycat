package ru.inforion.lab403.kopycat.interfaces

import ru.inforion.lab403.common.logging.logger
import java.util.logging.Level


interface ICoreUnit : IResettable, ITerminatable, ISerializable, IStringable, IInteractive {
    companion object {
        val log = logger(Level.CONFIG)
    }

    val name: String

    override fun describe(): String = ""

    override fun stringify(): String = name

    override fun reset() {
        log.finest { "Reset %s".format(name) }
    }
}