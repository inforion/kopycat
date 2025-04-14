/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.Port
import ru.inforion.lab403.kopycat.cores.base.ProxyPort
import ru.inforion.lab403.kopycat.cores.base.TranslatorPort
import ru.inforion.lab403.kopycat.cores.base.common.Module.Companion.log
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts.APort
import ru.inforion.lab403.kopycat.cores.base.exceptions.ConnectionError
import java.io.Serializable
import java.nio.ByteOrder

/**
 * {RU}
 * Класс, который является контейнером шин [ModuleBuses.Bus] в каждом модуле. Контейнером является [HashMap],
 * в качестве ключа используется имя порта [ModuleBuses.Bus.name], а значением сама шина [ModuleBuses.Bus]
 * Экземпляр класса [ModuleBuses] есть у любого из модулей [Module], по умолчанию в нем не содержится ни одной шины.
 * Чтобы добавить в какой-либо класс, следует реализовать следующую конструкцию при реализации класса:
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
open class ModuleBuses(val module: Module) {
    private val container = Dictionary<String, ModuleBuses.Bus>()

    operator fun get(name: String) = container[name]

    /**
     * {RU}
     * Этот метод используется для соединения двух портов при помощи текущей [ModuleBuses].
     * При подключении будет создана новая шина для соединения этих двух портов.
     *
     * ВНИМАНИЕ: Нельзя соединять напрямую два порта разного размера (разной ширины бит). Для этого необходимо
     * использовать промежуточную шину и к ней подключить оба порта.
     *
     * @param base первый порт для подключения
     * @param port второй порт для подключения
     * @param offset смещение, при подключении к шине ВТОРОГО порта [port]
     * @param endian порядок байтов
     *
     * @return шина, которая была создана при подключении
     * {RU}
     */
    fun connect(base: APort, port: APort, offset: ULong = 0u, endian: ByteOrder = ByteOrder.LITTLE_ENDIAN): Bus {
        ConnectionError.on(base === port) { "Connected ports are pointing to the same object!" }

        val hasNameSimple = base.name in container
        val name = if (hasNameSimple) "${base.name}<->${port.name}" else base.name

        log.fine { "Creating virtual bus to connect port $base and $port" }

        val bus = Bus(name)

        base.connect(bus, 0u, endian)
        port.connect(bus, offset, endian)

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
    fun connect(bases: Array<out APort>, ports: Array<out APort>, offset: ULong = 0u): Array<Bus> {
        require(bases.size == ports.size) { "bases.size != ports.size" }
        return bases.indices.map { connect(bases[it], ports[it], offset) }.toTypedArray()
    }

    internal fun disconnect(bus: Bus) {
        container.remove(bus.name)
    }

    /**
     * {RU}
     * Методы [resolveSlaves] и [resolveProxies] используются для создания кэша примитивов и разрешения прокси портов
     * для всех шин, которые содержатся в текущем [ModuleBuses]
     * {RU}
     */
    internal fun resolveSlaves() = container.values.forEach { it.cache.resolveSlaves() }
    internal fun resolveProxies() = container.values.forEach { it.cache.resolveProxies() }

    fun logMemoryMap() = container.forEach { (name, bus) ->
        val memoryMap = bus.cache.getPrintableMemoryMap()

        if (memoryMap.isNotBlank())
            log.info { "Memory map for bus '$name' -> $bus: $memoryMap" }
    }

    /**
     * {EN}Bus to port connection (there is also port to bus connection in [ModulePorts]){EN}
     */
    internal data class Connection<T: APort>(val port: T, val offset: ULong, val endian: ByteOrder): Serializable {
        override fun equals(other: Any?) = other is Connection<*> && port === other.port && offset == other.offset
        override fun hashCode() = 31 * port.hashCode() + offset.hashCode()
    }

    internal fun <T: APort>connections() = mutableListOf<Connection<T>>()

    /**
     * {RU}
     * Функция позволяет создать массив [Bus] шин
     *
     * @param count количество шин
     * @param prefix префикс для каждой шины
     * {RU}
     */
    fun buses(count: Int, prefix: String) = Array(count) { Bus("$prefix$it") }

    class BusDefinitionError(message: String) : Exception(message)

    /**
     * {RU}
     * Шина это сущность для соединения нескольких портов для обмена данными.
     * По своей сути аналогична шинам в реальном железе.
     *
     * @param name имя шины (должно совпадать с именем переменной, в случае, если задается из кода)
     * {RU}
     */
    inner class Bus internal constructor(val name: String, internal val dummy: Boolean = false): Serializable {
        val module = this@ModuleBuses.module

        constructor(name: String) : this(name, false)

        internal val ports = connections<Port>()
        internal val proxies = connections<ProxyPort>()
        internal val translators = connections<TranslatorPort>()

        internal val cache = BusCache(this)

        override fun toString(): String {
            return "$module:$name[Bus]"
        }

        private fun indent(): String {
            val nestlvl = module.getNestingLevel()
            return if (nestlvl == 0) "" else "%${2*nestlvl}s".format(" ")
        }

        /**
         * {EN}
         * Method called when [port] connected to [this] bus with specified [offset].
         * Bus adds specified [port] to one of containers [proxies], [translators]
         *
         * @param port connecting port
         * @param offset connection offset
         * @param endian endianness
         * {EN}
         *
         * {RU}
         * Метод вызывается при событии подсоединения порта [port] к текущей шине по заданному смещению [offset].
         * Метод производит логгирование события подключения и добавляет текущий порт в один из своих контейнеров:
         * [proxies], [translators] - для проки портов, мастер-портов, слэйв-портов и
         * трансляторов соответственно.
         *
         * @param port порт, который осуществил подключение к шине
         * @param offset смещение, по которому осуществляется подключение
         * @param endian порядок байтов
         * {RU}
         */
        internal fun onPortConnected(port: APort, offset: ULong, endian: ByteOrder) {
            log.fine { "${indent()}Connect: %-30s to %-30s at %08X as %6s".format(port, this, offset.long) }

            when (port) {
                is ProxyPort -> {
                    ConnectionError.on(offset != 0uL) {
                        "Proxy ports ($port) must be connected without offset (${offset.hex8})!"
                    }
                    proxies.add(Connection(port, offset, endian))
                }
                is Port -> ports.add(Connection(port, offset, endian))
                is TranslatorPort -> {
                    ConnectionError.on(offset != 0uL) {
                        "Translator ports ($port) must be connected without offset (${offset.hex8})!"
                    }
                    translators.add(Connection(port, offset, endian))
                }
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
        internal fun onPortDisconnect(port: APort, offset: ULong) {
            log.fine { "${indent()}Disconnect: %-30s to %-30s at %08X".format(port, this, offset) }

            val wasDisconnected = when (port) {
                is ProxyPort -> proxies.removeIf { it.port == port && it.offset == offset }
                is Port -> ports.removeIf { it.port == port && it.offset == offset }
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
            errorIf(name in Module.RESERVED_NAMES) { "$this -> bad bus name '$name'" }
            errorIf(name in container) { "$this -> bus name '$name' is duplicated in module $module" }
            container[name] = this@Bus
        }
    }
}