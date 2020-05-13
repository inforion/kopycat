package ru.inforion.lab403.kopycat.cores.base.common

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.hex8
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.Set
import kotlin.collections.any
import kotlin.collections.map
import kotlin.collections.mapTo
import kotlin.collections.set
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.common.BusCache.*
import ru.inforion.lab403.kopycat.cores.base.common.Module.Companion.RESERVED_NAMES
import ru.inforion.lab403.kopycat.cores.base.common.Module.Companion.log
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses.Bus
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts.ErrorAction.*
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.ConnectionError
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.base.exceptions.PortDefinitionError
import ru.inforion.lab403.kopycat.interfaces.IFetchReadWrite
import ru.inforion.lab403.kopycat.modules.BUS32

/**
 * {EN}
 * Collection of module's ports
 * If some module has port, they must be decelerated like this:
 *
 * inner class Ports : ModulePorts(this) {
 *      val mem = Proxy("mem")
 * }
 * override val ports = Ports()
 * Ports used
 * {EN}
 *
 * {RU}
 * Класс контейнер содержащий набор портов модуля.
 * Если в устройстве (модуле) присутвствуют порты, то они должны быть определены следующим образом:
 *
 *  * inner class Ports : ModulePorts(this) {
 *      val mem = Proxy("mem")
 * }
 * override val ports = Ports()
 * Порты используются для соединения различных модулей друг с другом через шины.
 * Соединения должны проводиться только в рамках одного модуля,
 * то есть нельзя выполнять соединение внутрь другого модуля.
 * {RU}
 */
open class ModulePorts(val module: Module): HashMap<String, ModulePorts.APort>() {
    /**
     * {RU}Перечисление определяющее действие в случае не обнаружения примитива для заданного адреса{RU}
     */
    enum class ErrorAction { EXCEPTION, LOGGING, IGNORE }

    /**
     * {RU}Перечисление для определения типа порта{RU}
     */
    enum class Type { Master, Slave, Proxy, Translator, Channel }

    /**
     * {RU}
     * Функция позволяет создать массив [Proxy] портов
     *
     * @param count количество портов
     * @param prefix префикс для каждого порта
     * @param size размер адресного пространства каждого порта
     * {RU}
     */
    fun proxies(count: Int, prefix: String, size: Long = BUS32) = Array(count) { Proxy("$prefix$it", size) }

    /**
     * {RU}
     * Функция позволяет создать массив [Slave] портов
     *
     * @param count количество портов
     * @param prefix префикс для каждого порта
     * @param size размер адресного пространства каждого порта
     * {RU}
     */
    fun slaves(count: Int, prefix: String, size: Long = BUS32) = Array(count) { Slave("$prefix$it", size) }

    /**
     * {RU}
     * Функция позволяет создать массив [Master] портов
     *
     * @param count количество портов
     * @param prefix префикс для каждого порта
     * @param size размер адресного пространства каждого порта
     * {RU}
     */
    fun masters(count: Int, prefix: String, size: Long = BUS32, onError: ErrorAction = EXCEPTION)
            = Array(count) { Master("$prefix$it", size, onError) }

    internal fun hasWarnings(logging: Boolean) = values.map {
        var hasWarnings = false
        if (it.hasOuterConnection) {
            if (logging)
                log.warning { "${it.type} port $it has no outer bus connection!" }
            hasWarnings = true
        }
        if (it is Proxy && it.hasInnerConnection) {
            if (logging)
                log.warning { "${it.type} port $it has no inner bus connection!" }
            hasWarnings = true
        }
        hasWarnings
    }.any()

    /**
     * {EN}Port to bus connection (there is also similar class for bus to port connection in [ModuleBuses]){EN}
     */
    data class Connection(val bus: Bus, val offset: Long)

    @Suppress("LeakingThis")
    /**
     * {RU}
     * Класс абстрактного порта, в котором собраны основные характеристики портов.
     * Порт используется для соединения различных модулей друг с другом с помощью шин [ModuleBuses.Bus].
     * К порту устройство может подключить различные области: [Module.Area], [Module.Register]
     *
     * @param name имя порта (должно совпадать с именем переменно!)
     * @param size максимальное количество доступных адресов порта (внимание это не ширина шины!)
     * @param type тип порта ([Master], [Slave], [Proxy], [Translator])
     * {RU}
     */
    abstract inner class APort(val name: String, val size: Long, val type: Type) {
        val module = this@ModulePorts.module

        internal val outerConnections = mutableListOf<Connection>()

        internal val registers = ArrayList<Module.Register>()
        internal val areas = ArrayList<Module.Area>()

        /**
         * {EN}
         * This method will get set of modules, which connected to this port. (Modules have some registers or areas,
         * that can be connected to current port, so this  method get set of this modules)
         *
         * @return Set of modules, which connected to this port
         * {EN}
         *
         * {RU}
         * Этот метод генерирует сет модулей, которые подключены к этому порту. (Модули включают в себя примитивы -
         * регистры и области, которые, в свою очередь могут быть подключеы к текущему порту)
         *
         * @return Множество модулей, которые подключены к текущему порту
         * {RU}
         */
        fun getConnectedModules(): Set<Module> = with(HashSet<Module>()) {
            registers.mapTo(this) { it.module }
            areas.mapTo(this) { it.module }
        }

        /**
         * {EN}
         * This method connect current port with bus at current offset.
         *
         * @param bus - Bus to connect to.
         * @param offset - Value of offset of port connection to bus.
         *
         * For example, if you call port.connect(bus, 0x8000_0000L), so port will be at offset at 0x8000_0000 on bus
         * {EN}
         *
         * {RU}
         * Этот метод осуществляет соединение текущего порта и шины по заданному смещению.
         *
         * @param bus - Шина, с которой осуществляется соединение.
         * @param offset - Значение смещения, по которому будет присоединен текущий порт.
         *
         * Например, если вызвать метод port.connect(bus, 0x8000_0000L),
         * то порт будет находится на шине со смещением 0x8000_0000
         * {RU}
         */
        open fun connect(bus: Bus, offset: Long = 0) {
            if (bus.module == module)
                throw ConnectionError("Can't connect bus $bus to port $this because bus is inner!")
            connectOuter(bus, offset)
        }

        fun connect(vararg connection: Pair<Bus, Long>) = connection.forEach { connect(it.first, it.second) }

        fun connect(bus: Bus, vararg offsets: Long) = offsets.forEach { connect(bus, it) }

        /**
         * {RU}
         * Свойство возвращает, подключен или нет порт к шине с внешней стороны
         * (обычное и единственно-возможное подлючение для всех портов, кроме [Proxy])
         *
         * Именно с помощью этого свойства должно проверяться наличие подключения у всех типов портов кроме [Proxy],
         * порт типа [Proxy] имеет еще одно подлючение с внутренней стороны модуля, оно проверяется с помощью
         * свойства [Proxy.hasInnerConnection]
         * {RU}
         */
        val hasOuterConnection get() = outerConnections.isNotEmpty()

        /**
         * {EN}
         * Method returns connection with index [index] from outerBuses
         *
         * @param index index of connection to get
         *
         * @return connection [Connection] with specified index
         * {EN}
         */
        fun connection(index: Int = 0) = outerConnections[index]

        override fun toString(): String {
            val sym = type.name[0]
            val size = if (size <= 0xFFFF_FFFF) size.hex8 else "========"
            return "$module:$name[${sym}x$size]"
        }

        protected fun connectOuter(bus: Bus, offset: Long = 0) {
            if (outerConnections.find { it.bus == bus && it.offset == offset } != null)
                throw ConnectionError("Port $this already has the same connection to $bus offset=${offset.hex}!")

            if (offset + size > bus.size)
                throw ConnectionError("Port $this with size=0x${size.hex} offset=0x${offset.hex} extent bus $bus size=0x${bus.size.hex}!")

            // connect and validate
            bus.onPortConnected(this, offset, ModuleBuses.ConnectionType.OUTER)

            outerConnections.add(Connection(bus, offset))
        }

        init {
            if (size <= 0)
                throw PortDefinitionError(this, "Wrong bus size $size > 0")
            if (name in RESERVED_NAMES)
                throw PortDefinitionError(this, "Bad port name: $name")
            if (name in this@ModulePorts.keys)
                throw PortDefinitionError(this, "Port name $name is duplicated in module $module")

            this@ModulePorts[name] = this@APort
        }
    }

    /**
     * {RU}
     * Порт используется для того, вызвать какое-либо действие (чтение или запись на шине).
     * Обычно этот порт используется для активных устройств: CPU, DMA, Debugger.
     *
     * Этот порт инициирует операции чтения и записи данных.
     *
     * @param name имя порта (должно совпадать с именем переменной!)
     * @param size максимальное количество доступных адресов порта (внимание это не ширина шины!)
     * @param onError определяет действие порта в том случае, если не было найдено никакого примитива для заданного адреса (по умолчанию выбрасывается исключение)
     * {RU}
     */
    inner class Master constructor(name: String, size: Long = BUS32, val onError: ErrorAction = EXCEPTION) :
            APort(name, size, Type.Master), IFetchReadWrite {
        constructor(name: String, size: Int, onError: ErrorAction = EXCEPTION) : this(name, size.asULong, onError)

        /**
         * {RU}
         * Проверяет наличие примитива подключенного к указанному адресу [ea] и селектору [ss]
         *
         * @param ea адрес для проверки
         * @param ss селектор для проверки
         * @param size размер области
         * @param LorS тип действия (LOAD или STORE)
         *
         * @return true - если найден регион или false - если нет.
         * {RU}
         */
        fun access(ea: Long, ss: Int = 0, size: Int = 0, LorS: AccessAction = LOAD) =
                find(this, ea, ss, size, LorS, 0) != null

        /**
         * {RU}
         * Выполняет поиск региона по заданного адреса [ea] и сегмента селектора [ss] и на заданное действие [LorS]
         *
         * @param source порт источник запроса
         * @param ea адрес запроса
         * @param ss селектор сегмента
         * @param size размер запрашиваемой области
         * @param LorS действие
         *
         * @return область для чтения/записи или null, если не найдено
         * {RU}
         */
        internal fun find(source: MasterPort, ea: Long, ss: Int, size: Int, LorS: AccessAction, value: Long): Entry? {
            val (bus, offset) = outerConnections.firstOrNull() ?: return null // exception will handle in outer function
            return bus.cache.find(source, ea + offset, ss, size, LorS, value)
        }

        override fun beforeFetch(from: MasterPort, ea: Long): Boolean =
                throw IllegalAccessError("This method should not be called")

        override fun beforeRead(from: MasterPort, ea: Long): Boolean =
                throw IllegalAccessError("This method should not be called")

        override fun beforeWrite(from: MasterPort, ea: Long, value: Long): Boolean =
                throw IllegalAccessError("This method should not be called")

        override fun fetch(ea: Long, ss: Int, size: Int): Long {
            val found = find(this, ea, ss, size, FETCH, 0)
                    ?: throw MemoryAccessError(-1, ea, FETCH, "Nothing connected at $ss:${ea.hex8} port $this")
            return found.fetch(ss, size)
        }

        override fun read(ea: Long, ss: Int, size: Int): Long {
            val found = find(this, ea, ss, size, LOAD, 0) ?: return when (onError) {
                EXCEPTION ->
                    throw MemoryAccessError(-1, ea, LOAD, "Nothing connected at $ss:${ea.hex8} port $this")
                LOGGING -> {
                    log.severe { "LOAD ignored ea=$ss:${ea.hex8} port=$this result=0x00000000" }
                    0
                }
                else -> 0
            }

            return found.read(ss, size)
        }

        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            val found = find(this, ea, ss, size, STORE, value) ?: return when (onError) {
                EXCEPTION ->
                    throw MemoryAccessError(-1, ea, STORE, "Nothing connected at $ss:${ea.hex8} port $this")
                LOGGING ->
                    log.severe { "STORE ignored ea=$ss:${ea.hex8} port=$this value=0x${value.hex8}" }
                else -> return
            }
            found.write(ss, size, value)
        }
    }

    /**
     * {RU}
     * Порт используется для того, чтобы зарегистрировать на нем области,
     * при обращении к которым будут вызваны соответствующие обработчики.
     *
     * Порт работает является пассивным, то есть выдает и записывает данные только по запросу извне.
     *
     * @param name имя порта (должно совпадать с именем переменной!)
     * @param size максимальное количество доступных адресов порта (внимание это не ширина шины!)
     * {RU}
     */
    inner class Slave constructor(name: String, size: Long = BUS32) : APort(name, size, Type.Slave) {
        constructor(name: String, size: Int) : this(name, size.asULong)

        /**
         * {RU}
         * Этот метод добавляет регистр [Module.Register] на текущей порт. После этого к текущему
         * регистру можно будет получить доступ через этот порт.
         *
         * @param register регистр, который должен быть добавлен
         * {RU}
         */
        fun add(register: Module.Register): Boolean = registers.add(register)

        /**
         * {RU}
         * Этот метод добавляет область памяти [Module.Area] на текущий порт. После этого к текущей области
         * памяти можно будет получить доступ через этот порт.
         *
         * @param area область памяти, которая должна быть добавлена
         * {RU}
         */
        fun add(area: Module.Area): Boolean = areas.add(area)
    }

    /**
     * {RU}
     * Порт используется для того, чтобы вытащить внутреннюю шину одного модуля во вне.
     *
     * Порт проксирует все области с внутренней шины на внешнюю шину и наоборот.
     * Фактически выступает соединителем двух шин.
     *
     * @param name имя порта (должно совпадать с именем переменной!)
     * @param size максимальное количество доступных адресов порта (внимание это не ширина шины!)
     * {RU}
     */
    inner class Proxy constructor(name: String, size: Long = BUS32) : APort(name, size, Type.Proxy) {
        constructor(name: String, size: Int) : this(name, size.asULong)

        var innerBus: Bus? = null
            private set

        override fun connect(bus: Bus, offset: Long) {
            if (offset != 0L)
                throw ConnectionError("Error in connection $bus to $this, proxy port connection offset should be 0")

            if (bus.size != size)
                throw ConnectionError("Size of inner bus $bus and port $this must be the same! [${bus.size.hex8} != ${size.hex8}]")

            if (bus.module == module) {
                connectInner(bus)
            } else {
                connectOuter(bus)
            }
        }

        private fun connectInner(bus: Bus) {
            innerBus = bus
            bus.onPortConnected(this, 0, ModuleBuses.ConnectionType.INNER)
        }

        /**
         * {RU}
         * Свойство возвращает, подключен или нет [Proxy] порт к шине с внутренней стороны.
         * Такое подключение возможно только для порта типа [Proxy]. Все остальные порты могут быть подключены
         * к шине только с внешней стороны, для проверки этого подключения используется свойство общее для всех
         * портов [APort.hasOuterConnection]. Порт [Proxy] может быть подключен и с внешней и с внутренней стороны.
         * {RU}
         */
        val hasInnerConnection get() = innerBus != null
    }

    /**
     * {RU}
     * Специальный порт, используется для трансляции адресов.
     *
     * @param name имя порта (должно совпадать с именем переменной!)
     * @param size максимальное количество доступных адресов порта (внимание это не ширина шины!)
     * {RU}
     */
    inner class Translator(
            name: String,
            val master: Master,
            size: Long,
            private val translator: AddressTranslator
    ) : APort(name, size, Type.Translator) {
        /**
         * {RU}
         * Метод используется для поиска примитивов сквозь сущность траслятора (от Slave-порта до Master-порта)
         * с учетом преобразования адресов.
         *
         * @param source - мастер порт, который изначально инициировал порт
         * @param ea - текущий адрес, который будет преобразован
         * @param ss - значение добавочного адреса (используется в некоторых процессорных архитектурах, например, x86)
         * @param size - количество байт, которые будут записаны или прочитаны в примитив
         * @param LorS - действие, которое будет совершено с найденным примитивом - чтение или запись
         * @param value - только для сохранение (значение, которое будет записано в регистр)
         *
         * @return BusCache.Entry в случае, если был найден примитив по другую сторону прокси порта, null - в случае, если не найден
         * {RU}
         */
        internal fun find(source: MasterPort, ea: Long, ss: Int, size: Int, LorS: AccessAction, value: Long) =
                master.find(source, translator.translate(ea, ss, size, LorS), ss, size, LorS, value)
    }
}