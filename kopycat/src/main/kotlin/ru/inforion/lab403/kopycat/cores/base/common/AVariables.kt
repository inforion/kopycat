package ru.inforion.lab403.kopycat.cores.base.common

/**
 * {RU}
 * Базовый класс набора переменных
 *
 *
 * @property args Набор входных переменных
 * @property container Отображение для набора переменных (HashMap)
 * {RU}
 *
 * {EN}
 * Base class variable set
 *
 * @author a.glakikh
 * @since 18.07.2018
 *
 * @property args input variable set
 * @property container container for variable set (HashMap)
 * {EN}
 **/
open class AVariables(val args: Array<out Pair<String, Any>> = emptyArray()) {

    private val container = HashMap<String, Variable<*>>()

    @Suppress("UNCHECKED_CAST")
    operator fun <T>get(key: String): T {
        val tmp = container[key] ?: throw IllegalArgumentException("Can't get $key!")
        return tmp.value as T
    }

    inline fun <reified T>array(size: Int, prefix: String, default: T) =
            Array(size) { Variable("$prefix$it", default) }

    /**
     * {RU}
     * Класс Переменная
     *
     * @property name Произвольное имя переменной
     * @property default Значение по умолчанию (по умолчанию, null)
     * @property required Флаг необходимости переменной (по умолчанию, true)
     * @property value Значение переменной
     * {RU}
     *
     * {EN}
     * Class to contain some variable
     *
     * @property name name of variable
     * @property default default value
     * @property required necessity flag variable
     * @property value variable value
     * {EN}
     **/
    @Suppress("UNCHECKED_CAST")
    inner class Variable<T>(val name: String, val default: T? = null, val required: Boolean = true) {
        private fun load(): T {
            val pair = args.find { it.first == name }
            return if (pair == null) {
                if (default == null && required)
                    throw IllegalArgumentException("Parameter not $name not found and default isn't specified!")
                Module.log.warning { "Parameter $name not found at init. use default = $default" }
                default as T
            } else pair.second as T
        }

        var value = load()

        init {
            container[name] = this
        }
    }
}