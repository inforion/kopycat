package ru.inforion.lab403.kopycat.consoles

import net.sourceforge.argparse4j.inf.ArgumentParser
import ru.inforion.lab403.common.extensions.argparser
import ru.inforion.lab403.common.extensions.parse
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.interfaces.IInteractive

class Argparse(val kopycat: Kopycat) : AConsole("Argparse") {
    private lateinit var parser: ArgumentParser

    override fun onInitialize(): Boolean = reconfigure()

    override fun onReconfigure(): Boolean {
        parser = argparser("kopycat").apply { kopycat.configure(this) }
        return true
    }

    override fun onEval(statement: String): Boolean {
        val args = statement.split(" ")
        val options = parser.parse(args)
        if (options != null) {
            val context = IInteractive.Context(options)
            return kopycat.process(context)
        }
        return false
    }

    override fun onExecute(statement: String): Result {
        val args = statement.split(" ")
        val options = parser.parse(args)
        if (options != null) {
            val context = IInteractive.Context(options)
            val isOk = kopycat.process(context)
            return Result(if (isOk) 0 else -1, context.result)
        }
        return Result(-1, null)
    }

    override val working get() = kopycat.working
}