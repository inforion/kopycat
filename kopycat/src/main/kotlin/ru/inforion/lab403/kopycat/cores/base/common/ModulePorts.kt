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
import ru.inforion.lab403.common.proposal.swapIfBE
import ru.inforion.lab403.kopycat.Kopycat
import ru.inforion.lab403.kopycat.annotations.ExperimentalWarning
import ru.inforion.lab403.kopycat.cores.base.common.Module.Companion.RESERVED_NAMES
import ru.inforion.lab403.kopycat.cores.base.common.Module.Companion.log
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses.Bus
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts.ErrorAction.EXCEPTION
import ru.inforion.lab403.kopycat.cores.base.common.ModulePorts.ErrorAction.LOGGING
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.*
import ru.inforion.lab403.kopycat.interfaces.IFetchReadWrite
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
    enum class Type { Port, Proxy, Translator }

    /**
     * {RU}
     * Функция позволяет создать массив [Proxy] портов
     *
     * @param count количество портов
     * @param prefix префикс для каждого порта
     * {RU}
     */
    fun proxies(count: Int, prefix: String) = Array(count) { Proxy("$prefix$it") }

    /**
     * {RU}
     * Функция позволяет создать массив [Port] портов
     *
     * @param count количество портов
     * @param prefix префикс для каждого порта
     * {RU}
     */
    fun ports(count: Int, prefix: String, onError: ErrorAction = EXCEPTION)
            = Array(count) { Port("$prefix$it", onError) }

    internal fun createDummyBuses() = container.values.forEach {
        if (it.connections.isEmpty()) {
            it.createDummyConnection()
        }
    }

    internal fun hasWarnings(logging: Boolean) = container.values.map {
        var hasWarnings = false
        if (it !is Port && !it.hasConnection) {
            if (logging) {
                log.warning { "${it.type} port $it has no connections!" }
            }
            hasWarnings = true
        }
        hasWarnings
    }.any { it }

    /**
     * {EN}Port to bus connection (there is also similar class for bus to port connection in [ModuleBuses]){EN}
     */
    data class Connection(val bus: Bus, val offset: ULong): Serializable {
        override fun toString() = "$bus@${offset.hex}"
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
     * @param type тип порта ([Port], [Proxy], [Translator])
     * {RU}
     */
    abstract inner class APort(val name: String, val type: Type): Serializable {
        val module = this@ModulePorts.module

        internal val connections = mutableListOf<Connection>()
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
         * @param endian - Bus endianness
         *
         * For example, if you call port.connect(bus, 0x8000_0000L), so port will be at offset at 0x8000_0000 on bus
         * {EN}
         *
         * {RU}
         * Этот метод осуществляет соединение текущего порта и шины по заданному смещению.
         *
         * @param bus - Шина, с которой осуществляется соединение.
         * @param offset - Значение смещения, по которому будет присоединен текущий порт.
         * @param endian - Порядок байтов порта
         *
         * Например, если вызвать метод port.connect(bus, 0x8000_0000L),
         * то порт будет находиться на шине со смещением 0x8000_0000
         * {RU}
         */
        open fun connect(bus: Bus, offset: ULong = 0u, endian: ByteOrder = ByteOrder.LITTLE_ENDIAN) {
            connectOuter(bus, offset, endian)
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
        fun disconnect(bus: Bus) {
            connections.filter { it.bus == bus }.forEach { disconnect(it) }
        }

        /**
         * {RU}
         * Свойство возвращает, подключен или нет порт к шине
         *
         * Именно с помощью этого свойства должно проверяться наличие подключения у всех типов портов.
         * {RU}
         */
        val hasConnection get() = connections.isNotEmpty() &&
                connections.firstOrNull()?.bus?.dummy == false

        /**
         * {EN}
         * Method returns connection with index [index] from outerBuses
         *
         * @param index index of connection to get
         *
         * @return connection [Connection] with specified index
         * {EN}
         */
        fun connection(index: Int = 0) = connections[index]

        override fun toString() = "$module:$name[${type.name}]"

        protected fun connectOuter(bus: Bus, offset: ULong = 0u, endian: ByteOrder = ByteOrder.LITTLE_ENDIAN) {
            val connection = connections.find { it.bus == bus && it.offset == offset }
            ConnectionError.on(connection != null) {
                "Port $this already has the same connection to $bus offset=${offset.hex}!"
            }

            // connect
            bus.onPortConnected(this, offset, endian)

            val dummy = connections.find { it.bus.dummy }
            connections.add(Connection(bus, offset))
            if (dummy != null) {
                disconnect(dummy)
                dummy.bus.module.buses.disconnect(dummy.bus)
            }
        }

        internal fun createDummyConnection() {
            val bus = module.buses.Bus("Dummy bus for $name", dummy = true)
            connect(bus)
        }

        /**
         * {EN}
         * Removes specified port-bus connection also call [Bus.onPortDisconnect] method
         *
         * @param connection connection to remove
         * {EN}
         */
        private fun disconnect(connection: Connection) {
            connections.remove(connection)
            connection.bus.onPortDisconnect(this, connection.offset)
        }

        private inline fun errorIf(condition: Boolean, message: () -> String) {
            if (condition) throw PortDefinitionError(message())
        }

        init {
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
     * @param onError определяет действие порта в том случае, если не было найдено никакого примитива для заданного адреса
     * (по умолчанию выбрасывается исключение)
     * {RU}
     */
    open inner class Port(name: String, val onError: ErrorAction = EXCEPTION) :
            APort(name, Type.Port), IFetchReadWrite {
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
                find(this, ea, ss, size, LorS, 0u, ByteOrder.LITTLE_ENDIAN) != null

        fun moduleName(ea: ULong, ss: Int = 0) = find(
            this,
            ea,
            ss,
            1,
            LOAD,
            0u,
            ByteOrder.LITTLE_ENDIAN,
        )?.toString()

        /**
         * {RU}
         * Выполняет поиск региона по заданного адреса [ea] и сегмента селектора [ss] и на заданное действие [LorS]
         *
         * @param source порт источник запроса
         * @param ea адрес запроса
         * @param ss селектор сегмента
         * @param size размер запрашиваемой области
         * @param LorS действие
         * @param value значение для beforeFetch/beforeRead/beforeWrite
         * @param endian порядок байтов
         *
         * @return область для чтения/записи или null, если не найдено
         * {RU}
         */
        internal fun find(
            source: Port,
            ea: ULong,
            ss: Int,
            size: Int,
            LorS: AccessAction,
            value: ULong,
            endian: ByteOrder,
        ) = connections.firstNotNullOfOrNull {
            it.bus.cache.find(source, ea + it.offset, ss, size, LorS, value, endian)
        }

        override fun beforeFetch(from: Port, ea: ULong, size: Int): Boolean =
                throw IllegalAccessError("This method should not be called")

        override fun beforeRead(from: Port, ea: ULong, size: Int): Boolean =
                throw IllegalAccessError("This method should not be called")

        override fun beforeWrite(from: Port, ea: ULong, size: Int, value: ULong): Boolean =
                throw IllegalAccessError("This method should not be called")

        private fun fetchInternal(ea: ULong, ss: Int, size: Int, endian: ByteOrder = ByteOrder.LITTLE_ENDIAN): ULong {
            val found = find(this, ea, ss, size, FETCH, 0u, endian)
                ?: throw MemoryAccessError(ULONG_MAX, ea, FETCH, "Nothing connected at $ss:${ea.hex16} port $this")
            return found.fetch(ss, size).run {
                if (found.cached.dstPort === this@Port) {
                    this
                } else {
                    swapIfBE(found.endian, size)
                }
            }
        }

        private inline fun readFetchSupportedSize(
            ea: ULong,
            ss: Int,
            size: Int,
            cpuEndian: ByteOrder,
            crossinline readFn: (ULong, Int, Int, ByteOrder) -> ULong,
        ) = when (size) {
            WORD.bytes,
            DWORD.bytes,
            WORD.bytes,
            BYTE.bytes -> readFn(ea, ss, size, cpuEndian)
            3 -> {
                readFn(ea, ss, WORD.bytes, cpuEndian) or
                        ((readFn(ea + WORD.bytes, ss, BYTE.bytes, cpuEndian)) shl WORD.bits)
            }
            5 -> {
                readFn(ea, ss, DWORD.bytes, cpuEndian) or
                        (readFn(ea + DWORD.bytes, ss, BYTE.bytes, cpuEndian) shl DWORD.bits)
            }
            6 -> {
                readFn(ea, ss, DWORD.bytes, cpuEndian) or
                        (readFn(ea + DWORD.bytes, ss, WORD.bytes, cpuEndian) shl DWORD.bits)
            }
            7 -> {
                readFn(ea, ss, DWORD.bytes, cpuEndian) or
                        (readFn(ea + DWORD.bytes, ss, WORD.bytes, cpuEndian) shl DWORD.bits) or
                        (readFn(ea + DWORD.bytes + WORD.bytes, ss, BYTE.bytes, cpuEndian) shl (DWORD.bits + WORD.bits))
            }
            else -> throw GeneralException("Unreachable size: $size")
        }

        override fun fetch(ea: ULong, ss: Int, size: Int) = try {
            fetchInternal(ea, ss, size)
        } catch (ex: CrossPageAccessException) {
            val nextPage = (ea + size - 1u) and ex.mask
            val firstSize = (nextPage - ea).int
            val secondSize = size - firstSize

            val firstPage = readFetchSupportedSize(ea, ss, firstSize, ex.order, this::fetchInternal)
            val secondPage = readFetchSupportedSize(nextPage, ss, secondSize, ex.order, this::fetchInternal)
            (firstPage or (secondPage shl (firstSize * BYTE_BITS))).swapIfBE(ex.order, size)
        } catch (ex: CrossPrimitiveAccessException) {
            val result = ByteArray(size)

            (0 until size).forEach {
                result[it] = fetchInternal(ea + it, ss, 1).byte
            }

            result.getUInt(0, size, ex.order)
        }

        private fun readInternal(
            ea: ULong,
            ss: Int,
            size: Int,
            endian: ByteOrder = ByteOrder.LITTLE_ENDIAN,
        ): ULong {
            val found = find(this, ea, ss, size, LOAD, 0u, endian) ?: return when (onError) {
                EXCEPTION ->
                    throw MemoryAccessError(ULONG_MAX, ea, LOAD, "Nothing connected at $ss:${ea.hex16} port $this")
                LOGGING -> {
                    log.severe { "LOAD ignored ea=$ss:${ea.hex16} port=$this result=0x00000000" }
                    0u
                }
                else -> 0u
            }
            return found.read(ss, size).run {
                if (found.cached.dstPort === this@Port) {
                    this
                } else {
                    swapIfBE(found.endian, size)
                }
            }
        }

        override fun read(ea: ULong, ss: Int, size: Int) = try {
            readInternal(ea, ss, size)
        } catch (ex: CrossPageAccessException) {
            val nextPage = (ea + size - 1u) and ex.mask
            val firstSize = (nextPage - ea).int
            val secondSize = size - firstSize

            val firstPage = readFetchSupportedSize(ea, ss, firstSize, ex.order, this::readInternal)
            val secondPage = readFetchSupportedSize(nextPage, ss, secondSize, ex.order, this::readInternal)
            (firstPage or (secondPage shl (firstSize * BYTE_BITS))).swapIfBE(ex.order, size)
        } catch (ex: CrossPrimitiveAccessException) {
            load(ea, size, ss).getUInt(0, size, ex.order)
        }

        private fun writeInternal(
            ea: ULong,
            ss: Int,
            size: Int,
            value: ULong,
            endian: ByteOrder = ByteOrder.LITTLE_ENDIAN,
        ) {
            // No need to swap value yet
            val found = find(this, ea, ss, size, STORE, value, endian) ?: return when (onError) {
                EXCEPTION ->
                    throw MemoryAccessError(ULONG_MAX, ea, STORE, "Nothing connected at $ss:${ea.hex16} port $this")
                LOGGING ->
                    log.severe { "STORE ignored ea=$ss:${ea.hex16} port=$this value=0x${value.hex16}" }
                else -> return
            }
            found.write(
                ss,
                size,
                if (found.cached.dstPort === this@Port) {
                    value
                } else {
                    value.swapIfBE(found.endian, size)
                }
            )
        }

        private fun writeSupportedSize(ea: ULong, ss: Int, size: Int, value: ULong, cpuEndian: ByteOrder) = when (size) {
            WORD.bytes,
            DWORD.bytes,
            WORD.bytes,
            BYTE.bytes -> writeInternal(ea, ss, size, value, cpuEndian)
            3 -> {
                writeInternal(ea, ss, WORD.bytes, value, cpuEndian)
                writeInternal(ea + WORD.bytes, ss, BYTE.bytes, value ushr WORD.bits, cpuEndian)
            }
            5 -> {
                writeInternal(ea, ss, DWORD.bytes, value, cpuEndian)
                writeInternal(ea + DWORD.bytes, ss, BYTE.bytes, value ushr DWORD.bits, cpuEndian)
            }
            6 -> {
                writeInternal(ea, ss, DWORD.bytes, value, cpuEndian)
                writeInternal(ea + DWORD.bytes, ss, WORD.bytes, value ushr DWORD.bits, cpuEndian)
            }
            7 -> {
                writeInternal(ea, ss, DWORD.bytes, value, cpuEndian)
                writeInternal(ea + DWORD.bytes, ss, WORD.bytes, value ushr DWORD.bits, cpuEndian)
                writeInternal(
                    ea + DWORD.bytes + WORD.bytes,
                    ss,
                    BYTE.bytes,
                    value ushr (DWORD.bits + WORD.bits),
                    cpuEndian,
                )
            }
            else -> throw GeneralException("Unreachable size: $size")
        }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = try {
            writeInternal(ea, ss, size, value)
        } catch (ex: CrossPageAccessException) {
            val nextPage = (ea + size - 1u) and ex.mask
            val firstSize = (nextPage - ea).int
            val secondSize = size - firstSize

            val swapped = value.swapIfBE(ex.order, size)
            writeSupportedSize(ea, ss, firstSize, swapped, ex.order)
            writeSupportedSize(nextPage, ss, secondSize, swapped ushr (firstSize * BYTE_BITS), ex.order)
        } catch (ex: CrossPrimitiveAccessException) {
            store(ea, value.pack(size, ex.order), ss)
        }

        /**
         * {RU}
         * Этот метод добавляет регистр [Module.Register] на текущий порт. После этого к текущему
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
     * Порт используется для того, чтобы соединить две шины
     *
     * Порт проксирует все области с внутренней шины на внешнюю шину и наоборот.
     * Фактически выступает соединителем двух шин.
     *
     * @param name имя порта (должно совпадать с именем переменной!)
     * {RU}
     */
    inner class Proxy(name: String) : APort(name, Type.Proxy) {
        override fun connect(bus: Bus, offset: ULong, endian: ByteOrder) {
            ConnectionError.on(offset != 0uL) {
                "Error in connection $bus to $this, proxy port connection offset should be 0"
            }
            connectOuter(bus, endian = endian)
        }
    }

    /**
     * {RU}
     * Специальный порт, используется для трансляции адресов.
     *
     * @param name имя порта (должно совпадать с именем переменной!)
     * {RU}
     */
    inner class Translator(
            name: String,
            val master: Port,
            private val translator: AddressTranslator
    ) : APort(name, Type.Translator) {
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
         * @param endian - порядок байтов
         *
         * @return BusCache.Entry в случае, если был найден примитив по другую сторону прокси порта, null - в случае, если не найден
         * {RU}
         */
        internal fun find(
            source: Port,
            ea: ULong,
            ss: Int,
            size: Int,
            LorS: AccessAction,
            value: ULong,
            endian: ByteOrder,
        ) = master.find(source, translator.translate(ea, ss, size, LorS), ss, size, LorS, value, endian)
    }
}