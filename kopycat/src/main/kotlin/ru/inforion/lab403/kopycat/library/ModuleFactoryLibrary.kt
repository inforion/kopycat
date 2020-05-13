package ru.inforion.lab403.kopycat.library

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.common.Component
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.library.ModuleFactoryLibrary.ConstructorSatisfying.ExcessArgs
import ru.inforion.lab403.kopycat.library.ModuleFactoryLibrary.ConstructorSatisfying.ParamSatisfying
import ru.inforion.lab403.kopycat.library.ModuleFactoryLibrary.InputParamInfo.Companion.parseParam
import ru.inforion.lab403.kopycat.library.builders.api.AFileModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactory
import ru.inforion.lab403.kopycat.library.builders.api.IModuleFactoryBuilder
import ru.inforion.lab403.kopycat.library.builders.api.ModuleParameterInfo
import ru.inforion.lab403.kopycat.library.enumerators.IFactoriesEnumerator
import ru.inforion.lab403.kopycat.library.enumerators.InternalFactoriesEnumerator
import ru.inforion.lab403.kopycat.library.enumerators.PluginFactoriesEnumerator
import ru.inforion.lab403.kopycat.library.exceptions.AmbiguousConstructorException
import ru.inforion.lab403.kopycat.library.exceptions.NoConstructorException
import ru.inforion.lab403.kopycat.library.exceptions.ParsingConstructorException
import ru.inforion.lab403.kopycat.library.types.ModuleInfo
import ru.inforion.lab403.kopycat.library.types.Resource
import ru.inforion.lab403.kopycat.settings
import java.io.File
import java.util.logging.Level

class ModuleFactoryLibrary(val name: String, val enumerator: IFactoriesEnumerator) {
    companion object {
        val log = logger(Level.INFO)

        fun loadInternal(name: String, directory: String) =
                ModuleFactoryLibrary(name, InternalFactoriesEnumerator(directory))

        fun loadPlugins(name: String, directory: String) =
                ModuleFactoryLibrary(name, PluginFactoriesEnumerator(directory))
    }

    /**
     * {EN}
     * This class is used for contain information about received argument (value and type, is type is specified, else - null)
     * By default received argument from JSON looks like "<name>" to <value> or "<name>:<type>" to <value>. It is not useful,
     * so this class parse this construction to get data class.
     * Method [parseParam] receives Map<String, Any?> (name to value) (map of arguments),
     * parse them and returns Map<String, [InputParamInfo]> (name to data class (value, type)).
     * {EN}
     *
     * {RU}
     * Класс используетс для хранения инормации о полученном аргументе (значение и типа, если тип явно указал, иначе - null)
     * Изначально из JSON получают аргумент вида "<name>" to <value> или "<name>:<type>" to <value>.
     * Для дальнейшего анализа это не удобно, поэтому был  создан этот класс, который обрабатывает эти конструкции.
     * Метод [parseParam] принимает Map<String, Any?> (name to value) (map аргументов), парсит их и возвращает
     * Map<String, [InputParamInfo]> (имя к дата классу (value, type)).
     * {RU}
     */
    data class InputParamInfo(val value: Any?, val type: String?) {
        class ParameterParseException(val name: String) : Exception()

        companion object {
            fun parseParam(params: Map<String, Any?>): Map<String, InputParamInfo> {
                return params.map { param ->
                    val parts = param.key.replace(" ", "").split(":")
                    val name = parts.first()
                    val type = parts.getOrNull(1)
                    if (parts.size > 2 || type != null && type.toLowerCase() !in settings.availableTypes.map { it.toLowerCase() })
                        throw ParameterParseException(name)
                    name to InputParamInfo(param.value, type)
                }.toMap()
            }
        }
    }

    /**
     * {EN}
     * This class contains information about satisfying of current [factory] and received constructor params.
     * Current [factory] contains number of parameters [ModuleParameterInfo] name and types of current constructor.
     * This class is used for the following tasks:
     * - Get information about which constructor parameters from [factory] were found in the received parameters
     * - Get information about with received parameters was excess for current [factory]
     * - Get information on whether the received parameters satisfied to the [factory] constructor.
     * - Get printable information about parameters satisfies
     *
     * @property params - contains array of [ParamSatisfying] (information about satisfying current constructor parameter and received parameters)
     * @property excessParams - contains array of [ExcessArgs] (information about excess received parameters)
     * @property isSatisfying - if true - current [factory] can be created with received list of parameters, else - false
     * {EN}
     *
     * {RU}
     * Этот класс содержит информацию о соответствии текущего [factory] и полученного списка параметров, из
     * которых будет создан экземпляр модуля. Текущий [factory] содержит массив [ModuleParameterInfo] (имя, тип и т.д.)
     * Для этого массива проверяется, соответствуют ли полученные параметры массиву [ModuleParameterInfo].
     * Этот класс используется для:
     * - Получения информации о том, какие из параметров конструктора [factory] были найдены в полученных параметров
     * - Получения информации о том, какие полученные параметры были лишние для текущего конструктора [factory]
     * - Получения информации о том, соответствуют ли полученные параметры выбранному конструктору [factory]
     * - Получения текстовой информации о соответствии параметров (какие подходят, а какие нет).
     *
     * @property params Массив [ParamSatisfying] - Для каждого [factory] содержит информацию о том, найден ли параметр среди полученных для этого аргуменат конструктора
     * @property excessParams Массив [ExcessArgs] - Содержит массив полученных параметров, которые оказались "лишними" для текущего конструктора [factory]
     * @property isSatisfying true - если текущая [factory] может быть создана из полученных параметров, иначе - false.
     * {RU}
     */
    class ConstructorSatisfying(val factory: IModuleFactory, val parameters: Map<String, InputParamInfo>) {
        data class ParamSatisfying(val param: ModuleParameterInfo, val founded: Boolean)
        data class ExcessArgs(val argName: String, val type: String?)

        private val params = ArrayList<ParamSatisfying>()
        private val excessParams = ArrayList<ExcessArgs>()

        val isSatisfying: Boolean
            get() = excessParams.size == 0 && params.all { it.founded }

        fun getPrintableError(): String {
            val paramsInfo = if (params.isEmpty()) "<no params>" else
                params.joinToString(separator = ",\n") { arg ->
                    "\t${arg.param.name} [${arg.param.type}] -> ${if(arg.founded) 'V' else 'X'}" }
            val excessInfo = if(excessParams.isEmpty()) "\n" else
                excessParams.joinToString(prefix = "Excess params:\n", separator = "\n", postfix = "\n") { arg ->
                    "\t${arg.argName} [${arg.type ?: "auto type"}]"
                }
            return "$paramsInfo\n$excessInfo"
        }

        init {
            var leftParameters = parameters
            log.finer { "Checking factory ${factory.getPrintableParams()}" }

            factory.parameters.forEach { param ->
                val candidates = leftParameters.filter {
                    val name = it.key
                    val type = it.value.type

                    if(type != null) {
                        name == param.name && param.type.toLowerCase() == type.toLowerCase()
                    } else {
                        name == param.name
                    }
                }
                params.add(ConstructorSatisfying.ParamSatisfying(param, candidates.size == 1))
                leftParameters = leftParameters.minus(candidates.map { it.key })
            }
            excessParams.addAll(leftParameters.map { param -> ConstructorSatisfying.ExcessArgs(param.key, param.value.type) })
        }
    }

    private lateinit var registry: ModuleLibraryRegistry

    private val modules = mutableMapOf<String, ModuleInfo>()

    fun register(newRegistry: ModuleLibraryRegistry) {
        log.fine { "Register module library $name" }
        registry = newRegistry
    }

    fun preload() {
        log.finer { "Preload module library $name" }
        enumerator.preload()
    }

    fun load() {
        log.finer { "Load module library $name" }
        modules += enumerator.load().map { (name, builder) -> name to ModuleInfo(name, builder) }
    }

    /**
     * {EN}
     * Create from [value] a parameter type of [type]
     *
     * @name name of parameter to convert
     * @value value of parameter to convert
     * @type name of type to convert
     *
     * @return converted value with type of [type]
     * {EN}
     */
    private fun convertParameterType(name: String, value: Any?, type: String): Any? {
        if (value == null)
            return null

        if (value == "null")
            return null

        return when (type) {
            "String" -> when (value) {
                is String -> value
                else -> value.toString()
            }
            "char" -> when (value) {
                is Char -> value
                is String -> value.toCharArray().first()
                else -> null
            }
            "int" -> when (value) {
                is Int -> value
                is String -> when {
                    value.startsWith("0x") -> value.removePrefix("0x").hexAsInt
                    else -> value.replace(" ", "").toInt()
                }
                is Long -> value.toInt()
                else -> null
            }
            "long" -> when (value) {
                is Long -> value
                is String -> when {
                    value.startsWith("0x") -> value.removePrefix("0x").hexAsULong
                    else -> value.replace(" ", "").toLong()
                }
                is Int -> value.toLong()
                else -> null
            }
            "float" -> when (value) {
                is Float -> value
                is Double -> value.toFloat()
                is Int -> value.toFloat()
                is Long -> value.toFloat()
                is String -> value.toFloat()
                else -> null
            }
            "double" -> when (value) {
                is Double -> value
                is Float -> value.toDouble()
                is Int -> value.toDouble()
                is Long -> value.toDouble()
                is String -> value.toDouble()
                else -> null
            }
            "boolean" -> when (value) {
                is Boolean -> value
                is String -> value.toBoolean()
                else -> null
            }
            "File" -> when (value) {
                is File -> value
                is String -> File(value)
                else -> null
            }
            "Resource" -> when (value) {
                is Resource -> value
                is String -> Resource(value)
                else -> null
            }
            "byte[]" -> when (value) {
                is ByteArray -> value
                is String -> value.unhexlify()
                else -> null
            }
            else -> throw IllegalArgumentException("Can't convert to type $type. " +
                    "Use on of available types: ${settings.availableTypes.joinToString(separator = ", ")}")
        } ?: throw IllegalArgumentException("Can't parse $name as $type value = $value")
    }

    /**
     * {EN}Create from [parameters] array of parameters for [factory] constructor.{EN}
     */
    private fun parseParametersAsMap(factory: IModuleFactory, parameters: Map<String, InputParamInfo>): Array<out Any?> {
        return factory.parameters.map {
            val value = if (!parameters.containsKey(it.name))
                it.default ?: throw IllegalArgumentException( "Can't parse parameter ${it.name}, value not specified and default not found!")
            else
                parameters[it.name]!!.value
            convertParameterType(it.name, value, it.type)
        }.toTypedArray()
    }

    /**
     * {EN}
     * Parse input line to map of parameters
     * @line Input line with parameters (for example "arg0=100,arg1=0x200,arg2=/path/to/something")
     * @return Map with parameters (for example "arg0" to "100", "arg1" to "0x200", "arg2" to "/path/to/something"
     * {EN}
     */
    private fun parseParametersAsString(line: String): Map<String, Any> = line
            .split(",")
            .filter { it.isNotBlank() }
            .map {
                val tmp = it.split("=")
                val name = tmp[0]
                val value = tmp[1]
                name to value
            }.toMap()

    private fun getPeripheralFactoryBuilder(name: String) = modules[name]?.builder
            ?: throw ClassNotFoundException("Module $name wasn't found!")

    /**
     * {EN}
     * Return all available modules in library.
     * Module consists of name and builder. Builder is special class inherited from [IModuleFactoryBuilder]
     * that contain information about how to create module and load it.
     * module.
     *
     * @return List of [ModuleInfo] - data class with name and builder of each module
     * {EN}
     */
    fun getAvailableAllModules() = modules.values

    fun getAvailableTopModules(registry: ModuleLibraryRegistry) =
            getAvailableAllModules().filter { module ->
                module.builder.factory(module.name, registry).all { it.canBeTop }
            }

    private fun buildPeripheralFactory(name: String) = getPeripheralFactoryBuilder(name).factory(name, registry)

    private fun getHelpConstructorString(factories: List<ConstructorSatisfying>) =
            factories.joinToString("\n") { factory ->
                val params = factory.getPrintableError()
                "Constructor #${factories.indexOf(factory) + 1}\n$params"

            }

    fun instantiate(parent: Component?, name: String, instanceName: String, parameters: Array<out Any>): Module {
        val factory = buildPeripheralFactory(name)
        return factory[0].create(parent, instanceName, *parameters) // TODO
    }

    fun instantiate(parent: Component?, builder: AFileModuleFactoryBuilder?, name: String, instanceName: String, parameters: Map<String, Any?>): Module {
        log.info {
            val nestLevel = parent?.getNestingLevel() ?: 0
            val tabs = if (nestLevel == 0) "" else "%${2 * nestLevel}s".format(" ")
            val pstr = if (parameters.isNotEmpty()) ", " + parameters.map { "${it.key}=${it.value}" }.joinToString(", ") else ""
            "$tabs$name(${parent?.name}, $instanceName$pstr)"
        }
        val factories = buildPeripheralFactory(name).asReversed()
        val parsedParams = try {
            InputParamInfo.parseParam(parameters)
        } catch (ex: InputParamInfo.ParameterParseException) {
            throw ParsingConstructorException(ex.name, instanceName, name, builder?.getFilePath(), settings.availableTypes)
        }

        val processedFactories = factories.map { factory -> ConstructorSatisfying(factory, parsedParams) }
        val filteredFactories = processedFactories.filter { it.isSatisfying }

        when (filteredFactories.size) {
            0 -> throw NoConstructorException(instanceName, name, builder?.getFilePath(), getHelpConstructorString(processedFactories))
            1 -> {
                val factory = filteredFactories.first().factory
                log.fine { "Selected constructor is: ${factory.getPrintableParams()}" }
                val ordered = parseParametersAsMap(factory, parsedParams)
                return factory.create(parent, instanceName, *ordered)
            }
            else -> throw AmbiguousConstructorException(instanceName, name, builder?.getFilePath(), getHelpConstructorString(filteredFactories))
        }
    }

    fun instantiate(parent: Component?, builder: AFileModuleFactoryBuilder?, name: String, instanceName: String, parameters: String? = null): Module =
            instantiate(parent, builder, name, instanceName, parseParametersAsString(parameters ?: ""))

    fun instantiate(name: String, builder: AFileModuleFactoryBuilder?, instanceName: String, parameters: String? = null): Module =
            instantiate(null, builder, name, instanceName, parameters)

    fun instantiate(name: String, instanceName: String, parameters: String? = null): Module =
            instantiate(null, null, name, instanceName, parameters)

    override fun toString(): String = "PeripheralFactoryLibrary[$name]"
}