package ru.inforion.lab403.kopycat.cores.base.common

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.hex4
import ru.inforion.lab403.common.extensions.hex8
import kotlin.collections.HashMap
import kotlin.collections.contains
import kotlin.collections.forEach
import kotlin.collections.map
import kotlin.collections.set
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.ProxyPort
import ru.inforion.lab403.kopycat.cores.base.SlavePort
import ru.inforion.lab403.kopycat.cores.base.TranslatorPort
import ru.inforion.lab403.kopycat.cores.base.common.Module.Companion.log
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts.APort
import ru.inforion.lab403.kopycat.cores.base.exceptions.BusDefinitionError
import ru.inforion.lab403.kopycat.cores.base.exceptions.ConnectionError
import ru.inforion.lab403.kopycat.modules.BUS32

/**
 * {RU}
 * Класс, который является контейнером шин [ModuleBuses.Bus] в каждом модуле. Контейнером является [HashMap],
 * в качестве ключа используется имя порта [ModuleBuses.Bus.name], а значением сама шина [ModuleBuses.Bus]
 * Экземпляр класса [ModuleBuses] есть у любого из модулей [Module], по умолчанию в нем не содержится ни одной шины.
 * Для того, чтобы добавть в какой либо класс модулей шины следует реализовать следующую конструкцию при реализации класса:
 * ```
 * inner class Buses : ModuleBuses(this) {
 *     val mem = Bus("mem")
 *     val io = Bus("io")
 * }
 * override val buses = Buses()
 * ```
 * Далее можно обратиться к созданным шинам внутри модуля:
 * buses.mem и buses.io
 *
 * @property module - модуль, которому принадлежит текущий класс с контейнером шин.
 * {RU}
 */
open class ModuleBuses(val module: Module): HashMap<String, ModuleBuses.Bus>() {
    /**
     * {RU}
     * Класс, описывающий, с какой стороны порта к нему полключена текущая шина. Могут быть следующие варианты:
     * наруженй стороной к шине [ConnectionType.OUTER] - для портов всех типов
     * внутренней стороной к шине [ConnectionType.INNER] - только для [ModulePorts.Proxy].
     * В примере порт является прокси порторм, который принадлежит модулую MODULE_1,
     * BUS_1 имеет внутреннее подключение, BUS_2 наружное подключение.
     * ```
     *  __________________<-- MODULE_1
     * |   BUS_1         |    BUS_2
     * | ----------------|||----------------
     * |_________________|ProxyPort
     * ```
     * {RU}
     */
    enum class ConnectionType { INNER, OUTER }

    private fun validatePortConnection(port: APort) {
        when (port) {
            is ProxyPort -> when (module) {
                port.module -> if (port.hasInnerConnection)
                    throw ConnectionError("Port $this has inner connection already")
                port.module.parent -> if (port.hasOuterConnection)
                    throw ConnectionError("Port $this has outer connection already")
                else -> throw ConnectionError("Buses of module $module can't connect port $port of other module!")
            }
            else -> {
                if (port.module.parent != module)
                    throw ConnectionError("Buses of module $module can't connect port $port of other module!")
                if (port.hasOuterConnection)
                    throw ConnectionError("Port $port has inner connection already")
            }
        }
    }

    /**
     * {RU}
     * Этот метод используется для соединения двух портов при помощи текущей [ModuleBuses].
     * При подключении будет создана новая шина для соединения этих двух портов. Перед подключением осуществляется
     * проверка двух портов при помощи метода [ModuleBuses.validatePortConnection]
     *
     * ВНИМАНИЕ: Нельзя соединять напрямую два порта разного размера (разной ширины бит). Для этого необходимо
     * использовать промежуточную шину и к ней подключить оба порта.
     *
     * @param base первый порт для подключения
     * @param port второй порт для подключения
     * @param offset смещение, при подключении к шине ВТОРОГО порта [port]
     *
     * @return шина, которая была создана при подключении
     * {RU}
     */
    fun connect(base: APort, port: APort, offset: Long = 0): Bus {
        if(base === port)
            throw ConnectionError("Connected ports are pointing to the same object!")

        validatePortConnection(base)
        validatePortConnection(port)

        // Ограничение введено в силу того, что при таком соединении неясно какого размера должна быть шина
        // Шина может быть размером как базовый порт, так и подключаемый порт
        if (base.size != port.size)
            throw ConnectionError("Connected ports ($base and $port) has different size [${base.size} != ${port.size}]!")

        val hasNameSimple = base.name in this
        val name = if (hasNameSimple) "${base.name}<->${port.name}" else base.name

        log.fine { "Creating virtual bus to connect port $base and $port" }

        val bus = Bus(name, base.size)

        base.connect(bus, 0)
        port.connect(bus, offset)

        return bus
    }

    /**
     * {RU}
     * Осуществляет попарное соединение портов из двух входных массивов по заданному смещению.
     * Вызывает функцию [ModuleBuses.connect] при работе.
     *
     * @param bases массив портов, каждый из которых будет подключен к портам из второго массива с соответствующим индексом.
     * @param ports массив портов, каждый из которых будет подключен к портам из первого массива с соответствующим индексом
     * @param offset смещение, при подключении к шине порта из второго массива [ports] к первому
     *
     * @return массив шин [Bus], которые были созданы при подключении
     * {RU}
     */
    fun connect(bases: Array<out APort>, ports: Array<out APort>, offset: Long = 0): Array<Bus> {
        require(bases.size == ports.size) { "bases.size != ports.size" }
        return bases.indices.map { connect(bases[it], ports[it], offset) }.toTypedArray()
    }

    /**
     * {RU}
     * Методы [resolveSlaves] и [resolveProxies] используются для создания кэша примитивов и разрешения прокси портов
     * для всех шин, которые содержатся в текущем [ModuleBuses]
     * {RU}
     */
    internal fun resolveSlaves() = values.forEach { it.cache.resolveSlaves() }
    internal fun resolveProxies() = values.forEach { it.cache.resolveProxies() }

    fun logMemoryMap() = values.forEach {
        val memoryMap = it.cache.getPrintableMemoryMap()

        if (memoryMap.isNotBlank())
            log.info { "Memory map for bus $it: $memoryMap" }
    }

    /**
     * {EN}Bus to port connection (there is also port to bus connection in [ModulePorts]){EN}
     */
    internal data class Connection<T: APort>(
            val port: T,
            val offset: Long,
            val type: ConnectionType
    )
    internal fun <T: APort>connections() = mutableListOf<Connection<T>>()

    /**
     * {RU}
     * Функция позволяет создать массив [Bus] шин
     *
     * @param count количество шин
     * @param prefix префикс для каждой шины
     * @param size размер адресного пространства каждой шины
     * {RU}
     */
    fun buses(count: Int, prefix: String, size: Long = BUS32) = Array(count) { Bus("$prefix$it", size) }

    /**
     * {RU}
     * Шина это сущность для соединения нескольких портов для обмена данными.
     * По своей сути аналогична шинам в реальном железе.
     *
     * @param name имя шины (должно совпадать с именем переменной, в случае, если задается из кода)
     * @param size максимальное количество доступных адресов порта (внимание это не ширина шины!)
     * {RU}
     */
    inner class Bus constructor(val name: String, val size: Long = BUS32) {
        constructor(name: String, size: Int) : this(name, size.asULong)
        constructor(name: String, size: Long, accessErrorAction: String) : this(name, size) {
            log.severe { "accessErrorAction not supported now!" }
        }

        val module = this@ModuleBuses.module

        private val masters = connections<MasterPort>()
        internal val slaves = connections<SlavePort>()
        internal val proxies = connections<ProxyPort>()
        internal val translators = connections<TranslatorPort>()

        internal val cache = BusCache(this)

        override fun toString(): String {
            val size = if (size < 0xFFFF) size.hex4 else "===="
            return "$module:$name[Bx$size]"
        }

        /**
         * {RU}
         * Метод вызывается при событии подсоединения порта [port] к текущей шине по заданносу смещению [offset].
         * Метод производит логгирование события подключения и добавляет текущий порт в один из своих контейнеров:
         * [proxies], [masters], [slaves], [translators] - для проки портов, мастер-портов, слэйв-портов и
         * трансляторов соответственно.
         *
         * @param port порт, который осуществил подключение к шине
         * @param offset смещение, по которому осуществляется подключение
         * @param type тип подключения - порт пожет быть подключен либо с внутренней стороны, либо с внешней.
         * {RU}
         */
        internal fun onPortConnected(port: APort, offset: Long, type: ConnectionType) {
            log.fine {
                val nestlvl = module.getNestingLevel()
                val tabs = if (nestlvl == 0) "" else "%${2*nestlvl}s".format(" ")
                "${tabs}Connect: %-30s to %-30s at %08X as %6s".format(port, this, offset, type)
            }

            when (port) {
                is ProxyPort -> {
                    if (offset != 0L)
                        throw ConnectionError("Proxy ports ($port) must be connected without offset (${offset.hex8})!")
                    proxies.add(Connection(port, offset, type))
                }
                is MasterPort -> {
//                    if (port.hasOuterConnection)
//                        throw ConnectionError("Master port ($port) can't be connected twice!")
                    masters.add(Connection(port, offset, type))
                }
                is SlavePort -> slaves.add(Connection(port, offset, type))
                is TranslatorPort -> translators.add(Connection(port, offset, type))
            }

            port.module.onPortConnected(port, this, offset)
        }

        init {
            if (size <= 0)
                throw BusDefinitionError(this, "Wrong bus size $size > 0")
            if (name in Module.RESERVED_NAMES)
                throw BusDefinitionError(this, "Bad bus name: $name")
            if (name in this@ModuleBuses.keys)
                throw BusDefinitionError(this, "Bus name $name is duplicated in module $module")

            this@ModuleBuses[name] = this@Bus
        }
    }
}