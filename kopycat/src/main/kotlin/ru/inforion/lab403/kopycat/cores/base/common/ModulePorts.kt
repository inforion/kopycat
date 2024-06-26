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
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.annotations.ExperimentalWarning
import ru.inforion.lab403.kopycat.cores.base.MasterPort
import ru.inforion.lab403.kopycat.cores.base.common.BusCache.Entry
import ru.inforion.lab403.kopycat.cores.base.common.Module.Companion.RESERVED_NAMES
import ru.inforion.lab403.kopycat.cores.base.common.Module.Companion.log
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses.Bus
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts.ErrorAction.EXCEPTION
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts.ErrorAction.LOGGING
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.ConnectionError
import ru.inforion.lab403.kopycat.cores.base.exceptions.CrossPageAccessException
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.interfaces.IFetchReadWrite
import ru.inforion.lab403.kopycat.modules.BUS32
import java.io.Serializable
import java.nio.ByteOrder

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
 * Соединения должны проводиться только в рамках одного модуля (верхнего уровня?),
 * то есть нельзя выполнять соединение внутрь другого модуля.
 * {RU}
 */
open class ModulePorts(val module: Module) {
    private val container = Dictionary<String, ModulePorts.APort>()

    operator fun get(name: String) = container[name]

    /**
     * {RU}Перечисление, определяющее действие в случае не обнаружения примитива для заданного адреса{RU}
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
    fun proxies(count: Int, prefix: String, size: ULong = BUS32) = Array(count) { Proxy("$prefix$it", size) }

    /**
     * {RU}
     * Функция позволяет создать массив [Slave] портов
     *
     * @param count количество портов
     * @param prefix префикс для каждого порта
     * @param size размер адресного пространства каждого порта
     * {RU}
     */
    fun slaves(count: Int, prefix: String, size: ULong = BUS32) = Array(count) { Slave("$prefix$it", size) }

    /**
     * {RU}
     * Функция позволяет создать массив [Master] портов
     *
     * @param count количество портов
     * @param prefix префикс для каждого порта
     * @param size размер адресного пространства каждого порта
     * {RU}
     */
    fun masters(count: Int, prefix: String, size: ULong = BUS32, onError: ErrorAction = EXCEPTION)
            = Array(count) { Master("$prefix$it", size, onError) }

    internal fun hasWarnings(logging: Boolean) = container.values.map {
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
    data class Connection(val bus: Bus, val offset: ULong): Serializable {
        override fun toString(): String {
            return super.toString() + offset.hex
        }
    }

    class PortDefinitionError(message: String) : Exception(message)

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
    abstract inner class APort(val name: String, val size: ULong, val type: Type): Serializable {
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
         * регистры и области, которые, в свою очередь, могут быть подключены к текущему порту)
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
         * то порт будет находиться на шине со смещением 0x8000_0000
         * {RU}
         */
        open fun connect(bus: Bus, offset: ULong = 0u) {
            ConnectionError.on(bus.module == module) {
                "\nCan't connect port $this to bus $bus because these bus and port belong to the same module '$module'" +
                "\nSuch a connection is only possible if port is Proxy port but $this is '$type'"
            }
            connectOuter(bus, offset)
        }

        fun connect(vararg connection: Pair<Bus, ULong>) = connection.forEach { connect(it.first, it.second) }

        fun connect(bus: Bus, vararg offsets: ULong) = offsets.forEach { connect(bus, it) }

        /**
         * {EN}
         * Disconnects this port from [bus] at any connected offset
         *
         * @param bus bus from which disconnect port
         * {EN}
         */
        open fun disconnect(bus: Bus) {
            outerConnections.filter { it.bus == bus }.forEach { disconnectOuter(it) }
        }

        /**
         * {RU}
         * Свойство возвращает, подключен или нет порт к шине с внешней стороны
         * (обычное и единственно-возможное подключение для всех портов, кроме [Proxy])
         *
         * Именно с помощью этого свойства должно проверяться наличие подключения у всех типов портов кроме [Proxy],
         * порт типа [Proxy] имеет еще одно подключение с внутренней стороны модуля, оно проверяется с помощью
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
            return "$module:$name[${sym}x${size.hex}]"
        }

        protected fun connectOuter(bus: Bus, offset: ULong = 0u) {
            val connection = outerConnections.find { it.bus == bus && it.offset == offset }
            ConnectionError.on(connection != null) {
                "Port $this already has the same connection to $bus offset=${offset.hex}!"
            }

            ConnectionError.on(offset + size > bus.size) {
                "Port $this with size=0x${size.hex} offset=0x${offset.hex} extent bus $bus size=0x${bus.size.hex}!"
            }

            // connect and validate
            bus.onPortConnected(this, offset, ModuleBuses.ConnectionType.OUTER)

            outerConnections.add(Connection(bus, offset))
        }

        /**
         * {EN}
         * Removes specified port-bus connection also call [Bus.onPortDisconnect] method
         *
         * @param connection connection to remove
         * {EN}
         */
        protected fun disconnectOuter(connection: Connection) {
            outerConnections.remove(connection)
            connection.bus.onPortDisconnect(this, connection.offset)
        }

        private inline fun errorIf(condition: Boolean, message: () -> String) {
            if (condition) throw PortDefinitionError(message())
        }

        init {
            errorIf(size <= 0u) { "$this -> wrong bus size $size > 0" }
            errorIf(name in RESERVED_NAMES) { "$this -> bad port name: $name" }
            errorIf(name in container) { "$this -> port name $name is duplicated in module $module" }
            container[name] = this@APort
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
     * @param onError определяет действие порта в том случае, если не было найдено никакого примитива для заданного адреса
     * (по умолчанию выбрасывается исключение)
     * {RU}
     */
    open inner class Master constructor(name: String, size: ULong = BUS32, val onError: ErrorAction = EXCEPTION) :
            APort(name, size, Type.Master), IFetchReadWrite {
        constructor(name: String, size: Int, onError: ErrorAction = EXCEPTION) : this(name, size.ulong_z, onError)

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
        fun access(ea: ULong, ss: Int = 0, size: Int = 0, LorS: AccessAction = LOAD) =
                find(this, ea, ss, size, LorS, 0u) != null

        fun moduleName(ea: ULong, ss: Int = 0) = find(this, ea, ss, 1, LOAD, 0u)?.toString()

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
        internal fun find(source: MasterPort, ea: ULong, ss: Int, size: Int, LorS: AccessAction, value: ULong): Entry? {
            val (bus, offset) = outerConnections.firstOrNull() ?: return null // exception will handle in outer function
            return bus.cache.find(source, ea + offset, ss, size, LorS, value)
        }

        override fun beforeFetch(from: MasterPort, ea: ULong): Boolean =
                throw IllegalAccessError("This method should not be called")

        override fun beforeRead(from: MasterPort, ea: ULong): Boolean =
                throw IllegalAccessError("This method should not be called")

        override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong): Boolean =
                throw IllegalAccessError("This method should not be called")

        private fun fetchInternal(ea: ULong, ss: Int, size: Int): ULong {
            val found = find(this, ea, ss, size, FETCH, 0u)
                ?: throw MemoryAccessError(ULONG_MAX, ea, FETCH, "Nothing connected at $ss:${ea.hex16} port $this")
            return found.fetch(ss, size)
        }

        private fun fetchSupportedSize(ea: ULong, ss: Int, size: Int, swap: Boolean) = when (size) {
            WORD.bytes,
            FWORD.bytes,
            DWORD.bytes,
            WORD.bytes,
            BYTE.bytes -> readInternal(ea, ss, size, swap)
            3 -> {
                readInternal(ea, ss, WORD.bytes, swap) or
                        (readInternal(ea + WORD.bytes, ss, BYTE.bytes, swap) shl WORD.bits)
            }
            5 -> {
                readInternal(ea, ss, DWORD.bytes, swap) or
                        (readInternal(ea + DWORD.bytes, ss, BYTE.bytes, swap) shl DWORD.bits)
            }
            7 -> {
                readInternal(ea, ss, DWORD.bytes, swap) or
                        (readInternal(ea + DWORD.bytes, ss, WORD.bytes, swap) shl DWORD.bits) or
                        (readInternal(ea + DWORD.bytes + WORD.bytes, ss, BYTE.bytes, swap) shl (DWORD.bits + WORD.bits))
            }
            else -> throw GeneralException("Unreachable size: $size")
        }

        override fun fetch(ea: ULong, ss: Int, size: Int) = try {
            fetchInternal(ea, ss, size)
        }
        catch (ex: CrossPageAccessException) {
            val nextPage = (ea + size - 1u) and ex.mask
            val firstSize = (nextPage - ea).int
            val secondSize = size - firstSize

            val bigEndian = ex.order == ByteOrder.BIG_ENDIAN
            val result = fetchSupportedSize(ea, ss, firstSize, bigEndian) or (
                    fetchSupportedSize(nextPage, ss, secondSize, bigEndian) shl (firstSize * BYTE_BITS)
            )

            if (bigEndian) {
                result.swap(size)
            } else {
                result
            }
        }

        private fun readInternal(ea: ULong, ss: Int, size: Int, swap: Boolean = false): ULong {
            val found = find(this, ea, ss, size, LOAD, 0u) ?: return when (onError) {
                EXCEPTION ->
                    throw MemoryAccessError(ULONG_MAX, ea, LOAD, "Nothing connected at $ss:${ea.hex16} port $this")
                LOGGING -> {
                    log.severe { "LOAD ignored ea=$ss:${ea.hex16} port=$this result=0x00000000" }
                    0u
                }
                else -> 0u
            }
            return found.read(ss, size).run {
                if (swap) {
                    swap(size)
                } else {
                    this
                }
            }
        }

        private fun readSupportedSize(ea: ULong, ss: Int, size: Int, swap: Boolean) = when (size) {
            WORD.bytes,
            DWORD.bytes,
            WORD.bytes,
            BYTE.bytes -> readInternal(ea, ss, size, swap)
            3 -> {
                readInternal(ea, ss, WORD.bytes, swap) or
                (readInternal(ea + WORD.bytes, ss, BYTE.bytes, swap) shl WORD.bits)
            }
            5 -> {
                readInternal(ea, ss, DWORD.bytes, swap) or
                (readInternal(ea + DWORD.bytes, ss, BYTE.bytes, swap) shl DWORD.bits)
            }
            6 -> {
                readInternal(ea, ss, DWORD.bytes, swap) or
                (readInternal(ea + DWORD.bytes, ss, WORD.bytes, swap) shl DWORD.bits)
            }
            7 -> {
                readInternal(ea, ss, DWORD.bytes, swap) or
                (readInternal(ea + DWORD.bytes, ss, WORD.bytes, swap) shl DWORD.bits) or
                (readInternal(ea + DWORD.bytes + WORD.bytes, ss, BYTE.bytes, swap) shl (DWORD.bits + WORD.bits))
            }
            else -> throw GeneralException("Unreachable size: $size")
        }

        override fun read(ea: ULong, ss: Int, size: Int) = try {
             readInternal(ea, ss, size)
        }
        catch (ex: CrossPageAccessException) {
            val nextPage = (ea + size - 1u) and ex.mask
            val firstSize = (nextPage - ea).int
            val secondSize = size - firstSize

            val bigEndian = ex.order == ByteOrder.BIG_ENDIAN
            val result = readSupportedSize(ea, ss, firstSize, bigEndian) or (
                    readSupportedSize(nextPage, ss, secondSize, bigEndian) shl (firstSize * BYTE_BITS)
            )

            if (bigEndian) {
                result.swap(size)
            } else {
                result
            }
        }

        private fun writeInternal(ea: ULong, ss: Int, size: Int, value: ULong, swap: Boolean = false) {
            val swapped = if (swap) {
                value.swap(size)
            } else {
                value
            }

            val found = find(this, ea, ss, size, STORE, swapped) ?: return when (onError) {
                EXCEPTION ->
                    throw MemoryAccessError(ULONG_MAX, ea, STORE, "Nothing connected at $ss:${ea.hex16} port $this")
                LOGGING ->
                    log.severe { "STORE ignored ea=$ss:${ea.hex16} port=$this value=0x${swapped.hex16}" }
                else -> return
            }
            found.write(ss, size, swapped)
        }

        private fun writeSupportedSize(ea: ULong, ss: Int, size: Int, value: ULong, swap: Boolean) = when (size) {
            WORD.bytes,
            DWORD.bytes,
            WORD.bytes,
            BYTE.bytes -> writeInternal(ea, ss, size, value, swap)
            3 -> {
                writeInternal(ea, ss, WORD.bytes, value, swap)
                writeInternal(ea + WORD.bytes, ss, BYTE.bytes, value ushr WORD.bits, swap)
            }
            5 -> {
                writeInternal(ea, ss, DWORD.bytes, value, swap)
                writeInternal(ea + DWORD.bytes, ss, BYTE.bytes, value ushr DWORD.bits, swap)
            }
            6 -> {
                writeInternal(ea, ss, DWORD.bytes, value, swap)
                writeInternal(ea + DWORD.bytes, ss, WORD.bytes, value ushr DWORD.bits, swap)
            }
            7 -> {
                writeInternal(ea, ss, DWORD.bytes, value, swap)
                writeInternal(ea + DWORD.bytes, ss, WORD.bytes, value ushr DWORD.bits, swap)
                writeInternal(
                    ea + DWORD.bytes + WORD.bytes,
                    ss,
                    BYTE.bytes,
                    value ushr (DWORD.bits + WORD.bits),
                    swap,
                )
            }
            else -> throw GeneralException("Unreachable size: $size")
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = try {
            writeInternal(ea, ss, size, value)
        }
        catch (ex: CrossPageAccessException) {
            val nextPage = (ea + size - 1u) and ex.mask
            val firstSize = (nextPage - ea).int
            val secondSize = size - firstSize

            val bigEndian = ex.order == ByteOrder.BIG_ENDIAN
            val swapped = if (bigEndian) {
                value.swap(size)
            } else {
                value
            }
            writeSupportedSize(ea, ss, firstSize, swapped, bigEndian)
            writeSupportedSize(nextPage, ss, secondSize, swapped ushr (firstSize * BYTE_BITS), bigEndian)
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
    inner class Slave constructor(name: String, size: ULong = BUS32) : APort(name, size, Type.Slave) {
        constructor(name: String, size: Int) : this(name, size.ulong_z)

        /**
         * {RU}
         * Этот метод добавляет регистр [Module.Register] на текущей порт. После этого к текущему
         * регистру можно будет получить доступ через этот порт.
         *
         * @param register регистр, который должен быть добавлен
         * {RU}
         *
         * {EN}
         * Adds register [Module.Register] to this port. After that register can be accessed using port.
         *
         * NOTE: [Module.initializePortsAndBuses] must be called before action takes effect for module
         *   (automatically performed in [Module.initializeAndResetAsTopInstance] or [Kopycat.open])
         * {EN}
         */
        fun add(register: Module.Register): Boolean = registers.add(register)

        /**
         * {RU}
         * Этот метод добавляет область памяти [Module.Area] на текущий порт. После этого к текущей области
         * памяти можно будет получить доступ через этот порт.
         *
         * @param area область памяти, которая должна быть добавлена
         * {RU}
         *
         * {EN}
         * Adds area [Module.Area] to this port. After that area can be accessed using port.
         *
         * NOTE: [Module.initializePortsAndBuses] must be called before action takes effect for module
         *   (automatically performed in [Module.initializeAndResetAsTopInstance] or [Kopycat.open])
         * {EN}
         */
        fun add(area: Module.Area): Boolean = areas.add(area)

        /**
         * {EN}
         * Remove area [Module.Area] from this port
         *
         * NOTE: [Module.initializePortsAndBuses] must be called before action takes effect for module
         * {EN}
         */
        @ExperimentalWarning("remove() method may not work correctly")
        fun remove(area: Module.Area): Boolean = areas.remove(area)
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
    inner class Proxy constructor(name: String, size: ULong = BUS32) : APort(name, size, Type.Proxy) {
        constructor(name: String, size: Int) : this(name, size.ulong_z)

        var innerBus: Bus? = null
            private set

        override fun connect(bus: Bus, offset: ULong) {
            // TODO: May be these checks should be in connectInner?
            ConnectionError.on(offset != 0uL) { "Error in connection $bus to $this, proxy port connection offset should be 0" }
            ConnectionError.on(bus.size != size) { "Size of inner bus $bus and port $this must be the same! [${bus.size.hex8} != ${size.hex8}]" }

            if (bus.module == module) {
                connectInner(bus)
            } else {
                connectOuter(bus)
            }
        }

        override fun disconnect(bus: Bus) {
            if (bus == innerBus) {
                innerBus = null
                bus.onPortDisconnect(this, 0u)
            } else {
                super.disconnect(bus)
            }
        }

        private fun connectInner(bus: Bus) {
            innerBus = bus
            bus.onPortConnected(this, 0u, ModuleBuses.ConnectionType.INNER)
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
            size: ULong,
            private val translator: AddressTranslator
    ) : APort(name, size, Type.Translator) {
        /**
         * {RU}
         * Метод используется для поиска примитивов сквозь сущность транслятора (от Slave-порта до Master-порта)
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
        internal fun find(source: MasterPort, ea: ULong, ss: Int, size: Int, LorS: AccessAction, value: ULong) =
                master.find(source, translator.translate(ea, ss, size, LorS), ss, size, LorS, value)
    }
}