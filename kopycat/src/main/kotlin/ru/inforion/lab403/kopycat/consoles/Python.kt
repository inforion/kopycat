package ru.inforion.lab403.kopycat.consoles

import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.consoles.jep.JepInterpreter
import ru.inforion.lab403.kopycat.consoles.jep.JepLoader


class Python(val kopycat: Kopycat, val python: String = "python") : AConsole("Python") {

    private lateinit var jep: JepInterpreter

    override fun onInitialize(): Boolean {
        try {
            JepLoader.load(python)
            jep = JepInterpreter(true)
            jep.set("kopycat", kopycat)
            jep.eval("kc = kopycat")
            jep.eval("print(\"Jep starting successfully!\")")
        } catch (error: Throwable) {
            error.printStackTrace()
            return false
        }
        return true
    }

    override fun onReconfigure(): Boolean = true

    override fun onEval(statement: String): Boolean {
        jep.eval(statement)
        return true
    }

    override fun onExecute(statement: String): Result {
        val result = jep.getValue(statement)
        log.info { "result = $result" }
        return Result(0, result.toString())
    }

    override val working get() = kopycat.working
}