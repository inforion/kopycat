package ru.inforion.lab403.kopycat.consoles.jep

import ru.inforion.lab403.common.proposal.DynamicClassLoader

class JepInterpreter(redirectOutputStreams: Boolean) {
    private val configCls = DynamicClassLoader.loadClass("jep.JepConfig")
    private val configSetRedirectOutputStreams = configCls.getMethod("setRedirectOutputStreams", Boolean::class.java)

    private val interpCls = DynamicClassLoader.loadClass("jep.SharedInterpreter")
    private val interpSet = interpCls.getMethod("set", String::class.java, Any::class.java)
    private val interpEval = interpCls.getMethod("eval", String::class.java)
    private val interpGetValue = interpCls.getMethod("getValue", String::class.java)

    // Static method
    private val interpSetConfig = interpCls.getMethod("setConfig", configCls)

    private val interp: Any

    fun eval(statement: String) = interpEval.invoke(interp, statement)
    fun getValue(statement: String) = interpGetValue.invoke(interp, statement)
    fun set(name: String, value: Any) = interpSet.invoke(interp, name, value)

    init {
        val cfg = configCls.getDeclaredConstructor().newInstance()
        configSetRedirectOutputStreams.invoke(cfg, redirectOutputStreams)
        interpSetConfig(null, cfg)

        interp = interpCls.getDeclaredConstructor().newInstance()
    }
}