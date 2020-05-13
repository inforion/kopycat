package ru.inforion.lab403.kopycat.interfaces

import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import ru.inforion.lab403.common.extensions.getParserCommandStack
import ru.inforion.lab403.common.extensions.subparser
import ru.inforion.lab403.common.extensions.unhexlify
import ru.inforion.lab403.common.logging.logger
import java.util.logging.Level


interface IInteractive {
    companion object {
        val log = logger(Level.CONFIG)
    }

    class Context(val options: Namespace) {
        private val commands = options.getParserCommandStack()
        var result: String? = null

        fun isEmpty() = commands.isEmpty()
        fun isNotEmpty() = commands.isNotEmpty()

        fun command() = commands.first

        fun pop(): Boolean {
            if (commands.isNotEmpty()) {
                commands.pop()
                return true
            }
            return false
        }

        operator fun <T> get(name: String) = options.get<T>(name)

        fun getString(name: String) = options.getString(name)

        fun getBytes(name: String) = options.getString(name).unhexlify()

        fun getBoolean(name: String) = options.getBoolean(name)
    }

    fun command(): String? = null

    fun describe(): String

    /**
     * {EN}Create subparser and configure it if required{EN}
     *
     * {RU}Создать subparser и настроить его при необходимости{RU}
     */
    fun configure(parent: ArgumentParser?, useParent: Boolean = false): ArgumentParser? {
        if (parent == null)
            return null

        if (useParent)
            return parent

        val command = command()
        if (command != null) {
            val description = describe()
//            log.config { "Creating subparser: $command $description" }
            return parent.subparser(command, description)
        }

        return null
    }

    /**
     * {EN}Process commands after parsing{EN}
     *
     * {RU}Исполнение команды после разбора{RU}
     */
    fun process(context: Context): Boolean {
        context.pop()
        return false
    }
}