/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2020 INFORION, LLC
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Non-free licenses may also be purchased from INFORION, LLC, 
 * for users who do not want their programs protected by the GPL. 
 * Contact us for details kopycat@inforion.ru
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package ru.inforion.lab403.kopycat.cores.base.common

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.common.extensions.hex4
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.ProxyPort
import ru.inforion.lab403.kopycat.cores.base.SlavePort
import ru.inforion.lab403.kopycat.cores.base.TranslatorPort
import ru.inforion.lab403.kopycat.cores.base.common.Module.Companion.log
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts.APort
import ru.inforion.lab403.kopycat.cores.base.exceptions.ConnectionError
import ru.inforion.lab403.kopycat.modules.BUS32
import java.io.Serializable
import kotlin.collections.set

/**
 * {RU}
 * Класс, который является контейнером шин [ModuleBuses.Bus] в каждом модуле. Контейнером является [HashMap],
 * в качестве ключа используется имя порта [ModuleBuses.Bus.name], а значением сама шина [ModuleBuses.Bus]
 * Экземпляр класса [ModuleBuses] есть у любого из модулей [Module], по умолчанию в нем не содержится ни одной шины.
 * Для того, чтобы добавить в какой либо класс модулей шины следует реализовать следующую конструкцию при реализации класса:
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
     * наружней стороной к шине [ConnectionType.OUTER] - для портов всех типов
     * внутренней стороной к шине [ConnectionType.INNER] - только для [ModulePorts.Proxy].
     * В примере порт является прокси портом, который принадлежит модулю MODULE_1,
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
                port.module -> ConnectionError.on(port.hasInnerConnection) { "Port $this has inner connection already" }
                port.module.parent -> ConnectionError.on(port.hasOuterConnection) { "Port $this has outer connection already" }
                else -> ConnectionError.raise { "Buses of module $module can't connect port $port of other module!" }
            }
            else -> {
                ConnectionError.on(port.module.parent != module) { "Bus '$this' of module '$module' can't be connected to port $port of other module!" }
                ConnectionError.on(port.hasOuterConnection) { "Port $port has outer connection already to ${port.outerConnections}!" }
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
        ConnectionError.on(base === port) { "Connected ports are pointing to the same object!" }

        validatePortConnection(base)
        validatePortConnection(port)

        // Ограничение введено в силу того, что при таком соединении неясно какого размера должна быть шина
        // Шина может быть размером как базовый порт, так и подключаемый порт
        ConnectionError.on(base.size != port.size) {
            "Connected ports ($base and $port) has different size [${base.size} != ${port.size}]!"
        }

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
    ): Serializable

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

    class BusDefinitionError(message: String) : Exception(message)

    /**
     * {RU}
     * Шина это сущность для соединения нескольких портов для обмена данными.
     * По своей сути аналогична шинам в реальном железе.
     *
     * @param name имя шины (должно совпадать с именем переменной, в случае, если задается из кода)
     * @param size максимальное количество доступных адресов порта (внимание это не ширина шины!)
     * {RU}
     */
    inner class Bus constructor(val name: String, val size: Long = BUS32): Serializable {
        constructor(name: String, size: Int) : this(name, size.asULong)

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

        private fun indent(): String {
            val nestlvl = module.getNestingLevel()
            return if (nestlvl == 0) "" else "%${2*nestlvl}s".format(" ")
        }

        /**
         * {EN}
         * Method called when [port] connected to [this] bus with specified [offset].
         * Bus adds specified [port] to one of containers [proxies], [masters], [slaves], [translators]
         *
         * @param port connecting port
         * @param offset connection offset
         * @param type connection type [INNER or OUTER]
         * {EN}
         *
         * {RU}
         * Метод вызывается при событии подсоединения порта [port] к текущей шине по заданному смещению [offset].
         * Метод производит логгирование события подключения и добавляет текущий порт в один из своих контейнеров:
         * [proxies], [masters], [slaves], [translators] - для проки портов, мастер-портов, слэйв-портов и
         * трансляторов соответственно.
         *
         * @param port порт, который осуществил подключение к шине
         * @param offset смещение, по которому осуществляется подключение
         * @param type тип подключения - порт может быть подключен либо с внутренней стороны, либо с внешней.
         * {RU}
         */
        internal fun onPortConnected(port: APort, offset: Long, type: ConnectionType) {
            log.fine { "${indent()}Connect: %-30s to %-30s at %08X as %6s".format(port, this, offset, type) }

            when (port) {
                is ProxyPort -> {
                    ConnectionError.on(offset != 0L) { "Proxy ports ($port) must be connected without offset (${offset.hex8})!" }
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

        /**
         * {EN}
         * Method called when [port] disconnected from bus [this] at specified [offset]
         *
         * @param port disconnected port
         * @param offset disconnection offset
         * {EN}
         */
        internal fun onPortDisconnect(port: APort, offset: Long) {
            log.fine { "${indent()}Disconnect: %-30s to %-30s at %08X".format(port, this, offset) }

            val wasDisconnected = when (port) {
                is ProxyPort -> proxies.removeIf { it.port == port && it.offset == offset }
                is MasterPort -> masters.removeIf { it.port == port && it.offset == offset }
                is SlavePort -> slaves.removeIf { it.port == port && it.offset == offset }
                is TranslatorPort -> translators.removeIf { it.port == port && it.offset == offset }
                else -> throw ConnectionError("Unknown port type $port for disconnect from bus $this")
            }

            ConnectionError.on(!wasDisconnected) { "Port $port was not connected to bus $this" }

            port.module.onPortDisconnect(port, this, offset)
        }

        private inline fun errorIf(condition: Boolean, message: () -> String) {
            if (condition) throw BusDefinitionError(message())
        }

        init {
            errorIf(size <= 0) { "$this -> wrong bus size $size should be > 0" }
            errorIf(name in Module.RESERVED_NAMES) { "$this -> bad bus name '$name'" }
            errorIf(name in this@ModuleBuses.keys) { "$this -> bus name '$name' is duplicated in module $module" }
            this@ModuleBuses[name] = this@Bus
        }
    }
}