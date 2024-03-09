/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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
@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.kopycat.cores.base.common

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.intervalmap.Interval
import ru.inforion.lab403.common.intervalmap.PriorityTreeIntervalMap
import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.logging.logger.Logger
import ru.inforion.lab403.kopycat.annotations.ExperimentalWarning
import ru.inforion.lab403.kopycat.cores.base.*
import ru.inforion.lab403.kopycat.cores.base.enums.*
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryDeserializeSizeMismatchException
import ru.inforion.lab403.kopycat.cores.base.extensions.*
import ru.inforion.lab403.kopycat.interfaces.*
import ru.inforion.lab403.kopycat.serializer.*
import ru.inforion.lab403.kopycat.settings
import java.io.InputStream
import java.nio.ByteOrder
import java.util.*
import java.util.logging.Level

/**
 * {RU}
 * Модуль представляет собой комплексный элемент для построения вычислительных блоков и систем.
 * Включает в себя и предоставляет доступ к таким важным элементам,
 * как ядро [core], отладчик [debugger] и трассировщик [tracer].
 *
 * Внимание: во избежание ошибок использования, обязательно ознакомтесь с описанием [Component]
 *
 *
 * @param parent родительский компонент (необязательный параметр)
 * @param name произвольное имя объекта модуля
 * @param plugin имя плагина (необязательный параметр)
 *
 * @property isCorePresent флаг наличия компонента ядра
 * @property isDebuggerPresent флаг наличия компонента отладчика
 * @property isTracerPresent флаг наличия компонента трассировщика
 * @property core компонент ядра в эмуляторе
 * @property debugger компонент отладчика в эмуляторе
 * @property tracer компонент трассировщика в эмуляторе
 * @property variables переменные модуля
 * @property buses доступные шины модуля
 * @property ports доступные порты модуля
 * @property registers доступные регистры модуля
 * @property areas доступные адресные пространства модуля
 * {RU}
 *
 * {EN}
 * The module is an element for building an emulator system.
 * Includes and provides access to such important elements as the [core], the [debugger] and the [tracer].
 *
 * @param parent parent component (can be null if it doesn't have parent)
 * @param name module name
 * @param plugin plugin name (can be null)
 *
 * @property isCorePresent Flag [core] contains in this component
 * @property isDebuggerPresent Flag [debugger] contains in this component
 * @property isTracerPresent Flag [tracer] contains in this component
 * @property core component [core] of emulator system
 * @property debugger component [debugger] of emulator system
 * @property tracer component [tracer] of emulator system
 * @property variables module variables
 * @property buses all buses of current module
 * @property ports all ports of current module
 * @property registers registers in current module
 * @property areas areas in current module
 * {EN}
 */
open class Module(
    parent: Component?,
    name: String,
    plugin: String? = null
) : Component(parent, name, plugin) {

    companion object {
        val RESERVED_NAMES = arrayOf("ports", "buses")
        @Transient
        val log = logger(CONFIG)
    }

    val isCorePresent get() = ::core.isInitialized
    val isDebuggerPresent get() = ::debugger.isInitialized
    val isTracerPresent get() = ::tracer.isInitialized

    lateinit var core: AGenericCore
        private set

    lateinit var debugger: AGenericDebugger
        private set

    lateinit var tracer: AGenericTracer
        private set

    open val variables = AVariables()

    @Suppress("LeakingThis")
    open val buses = ModuleBuses(this)

    @Suppress("LeakingThis")
    open val ports = ModulePorts(this)

    // it's very experimental
    inline fun <T> reconnect(action: () -> T): T {
        log.finest { "Reconnect port and buses..." }
        val result = action()
        (root as Module).initializePortsAndBuses()
        return result
    }

    /**
     * {EN}
     * In normal usage (when Core and other key components present) [initializeAndResetAsTopInstance] must be used
     *
     * This method initialize bus, which include creating buses cache for all memory access
     * Cache creating include 2 steps:
     * 1. Initialization buses cache ([ModuleBuses.resolveSlaves]) - for all buses all primitives puts from Slave-ports to cache
     * 2. Resolve proxy-ports ([ModuleBuses.resolveProxies]) - for all buses, which connected to each other via proxy-ports, clone cache
     * to all buses, connected to current via proxy ports.
     *
     * Method is open to make possible use it in test when module has no core.
     *
     * @return true if any warnings occurred (true/false)
     * {EN}
     *
     * {RU}
     * При создании устройства в нормальном состоянии должен использоваться метод [initializeAndResetAsTopInstance]
     *
     * Инициализация шин, которая включает в себя создание кэша шин, через который осуществляется работа.
     * Создание кэша происходит в два этапа:
     * 1. Инициализация кэша шин ([ModuleBuses.resolveSlaves]) - для каждой шины все примитивы со Slave-портов переносится на шины
     * 2. Разрешение прокси-портов ([ModuleBuses.resolveProxies]) - для всех шин, которые соединены друг с другом при помощи
     *  прокси-портов, происходит копирование кэша (который был изначально инициализирован на первом этапе) друг другу,
     *  тем самым все примитивы оказываются на всех шинах, соединенных друг с другом, благодоря чему происходит
     *  разрешение прокси-портов.
     *  ```
     *                                                                                 PR_1       PR_1       PR_1
     *    SLAVE_1    SLAVE_2      SLAVE_3                                              PR_2       PR_2       PR_2
     *      |    ___    |    ___    |    Step 1   PR_1  ___  PR_2  ___  PR_3  Step 2   PR_3  ___  PR_3  ___  PR_3
     *  ____|___|P1|____|___|P2|____|___ ===>   ____|__|P1|____|__|P2|____|__ ===>   ____|__|P1|____|__|P2|____|__
     *   bus_1 |___| bus_2 |___| bus_3           bus_1|___| bus_2|___| bus_3          bus_1|___| bus_2|___| bus_3
     *   Где SLAVE_# - Slave-порт, PR_# - примитивы, P# - прокси порты
     *
     * ```
     *
     * Метод сделан открытым, чтобы можно было использовать его в тестах при отсутствии ядра.
     *
     * @return возникли или нет предупреждения по подключению шин (true/false)
     * {RU}
     **/
    fun initializePortsAndBuses(): Boolean {
        // WARNING: Don't touch -> order is significant for bus initializations
        val modules = getComponentsByClass<Module>().also { it.add(this) }

        return with(modules) {
            val hasWarnings = any { it.ports.hasWarnings(false) }
            forEach { it.buses.resolveSlaves() }
            forEach { it.buses.resolveProxies() }
            hasWarnings
        }
    }

    /**
     * {EN}
     * Initialize object as top module of emulator
     * @return initialization result (true/false)
     * {EN}
     *
     * {RU}
     * Инициализация объекта в качестве модуля верхнего уровня
     * Результат инициализации (true/false)
     * {RU}
     **/
    fun initializeAndResetAsTopInstance(): Boolean {
        if (!isTopInstance) {
            log.severe { "Only top instance can be started!" }
            return false
        }

        val lCore = findComponentByClass<AGenericCore>()
        if (lCore == null) {
            log.severe { "Core not found in $this!" }
            return false
        }
        log.config { "Setup core to $lCore for $this" }
        core = lCore

        val lDebugger = findComponentByClass<AGenericDebugger>()
        if (lDebugger != null) {
            log.config { "Setup debugger to $lDebugger for $this" }
            debugger = lDebugger
        } else log.warning { "Debugger wasn't found in $this..." }

        val lComponentTracer = findComponentByClass<ComponentTracer<AGenericCore>>()

        if (lComponentTracer != null) {
            log.config { "Setup tracer to $lComponentTracer for $this" }
            tracer = lComponentTracer
        } else {
            //search for any tracer if component tracer was not found
            val lTracer = findComponentByClass<AGenericTracer>()

            if (lTracer != null) {
                log.config { "Setup tracer to $lTracer for $this" }
                tracer = lTracer
            } else log.warning { "Tracer wasn't found in $this..." }
        }

        if (!initialize()) {
            log.severe { "Can't initialize top instance!" }
            return false
        }

        log.config { "Initializing ports and buses..." }
        if (initializePortsAndBuses()) {
            log.warning { "Some ports has warning use printModulesPortsWarnings to see it..." }
        }

        val modules = getAllComponents().size

        require(!settings.hasConstraints || modules >= settings.maxPossibleModules) {
            "Max possible modules value have exceeded [$modules <= ${settings.maxPossibleModules}]"
        }

        // Reset should be called after initialize
        reset()

        log.config { "Module $this is successfully initialized and reset as a top cell!" }

        return true
    }

    /**
     * {EN}
     * Callback called when any port [port] of module connected to bus [bus] with [offset]
     *
     * @param port connected port
     * @param bus target bus
     * @param offset connection offset
     * {EN}
     */
    open fun onPortConnected(port: APort, bus: Bus, offset: ULong) = Unit

    /**
     * {EN}
     * Callback called when any port [port] of module disconnected from bus [bus] with [offset]
     *
     * @param port connected port
     * @param bus target bus
     * @param offset connection offset
     * {EN}
     */
    open fun onPortDisconnect(port: APort, bus: Bus, offset: ULong) = Unit

    /**
     * {RU}
     * Инициализация корневого модуля.
     * Установка ядра, отладчика и трассировщика.
     * Результат инициализации (true/false)
     * {RU}
     *
     * {EN}
     * Initialize module. Set core, debugger and traces for this module.
     *
     * @return initialization result (true/false)
     * {EN}
     */
    override fun initialize(): Boolean {
        (root as Module).let { root ->
            if (root.isCorePresent) core = root.core  // should always be executed...
            if (root.isDebuggerPresent) debugger = root.debugger
            if (root.isTracerPresent) tracer = root.tracer
        }

        return super.initialize()
    }

    protected val registers = ArrayList<Register>()
    protected val areas = ArrayList<Area>()

    class AreaDefinitionError(message: String) : Exception(message)

    /**
     * {RU}
     * Внутренний класс Адресное Пространство.
     * Предназначен для хранения информации о диапазоне адресов и контроля доступа к этим адресам.
     *
     * @property port Порт, связанный с адресным пространством
     * @property start Начальный адрес пространства
     * @property endInclusively Конечный адрес пространства
     * @property name Произвольное имя объекта адресного пространства
     * @property access Тип доступа
     * @property verbose Флаг "Подробности"
     *
     * @property size Размер адресного пространства
     * {RU}
     *
     * {EN}
     * Inner class Area (for address space).
     * Designed to store information about the range of addresses and control access to these addresses.
     *
     * @property port port connected to the address space
     * @property start address space start address
     * @property endInclusively address space end address
     * @property name area address space
     * @property access access type
     * @property verbose verbose flag
     *
     * @property size size of address space in bytes
     * {EN}
     */
    abstract inner class Area constructor(
        val port: SlavePort,
        val start: ULong,
        val endInclusively: ULong,
        final override val name: String,
        val access: ACCESS = ACCESS.R_W,
        private val verbose: Boolean = false
    ) : IFetchReadWrite, ICoreUnit {
        val module = this@Module

        val size = endInclusively - start + 1u

        constructor(port: SlavePort, name: String, access: ACCESS = ACCESS.R_W, verbose: Boolean = false) :
                this(port, 0u, port.size - 1u, name, access, verbose)

        /**
         * {EN}
         * Function executes before area removed from port
         * {EN}
         */
        open fun beforeRemove() = Unit

        /**
         * {EN}
         * Remove area from the [port] but not completely disconnect it
         *
         * NOTE: [Module.initializePortsAndBuses] must be called before action takes effect for module
         * {EN}
         */
        @ExperimentalWarning(message = "remove() method may not work correctly")
        fun remove() {
            beforeRemove()
            areas.remove(this)
            port.remove(this)
        }

        fun range() = ULongRange(start, endInclusively)

        /**
         * {RU}
         * Сохранение состояния (сериализация)
         *
         * @param ctxt Контекст объекта-сериализатора
         *
         * @return Отображение сохраняемых свойств объекта
         * {RU}
         *
         * {EN}
         * Save object state to snapshot (Serialize)
         *
         * @param ctxt Serializer context
         *
         * @return map of object properties
         * {EN}
         */
        override fun serialize(ctxt: GenericSerializer) = storeValues(
            "name" to name,
            "start" to start.hex8,
            "end" to endInclusively.hex8,
            "access" to access
        )

        /**
         * {RU}
         * Восстановление состояния (десериализация)
         *
         * @param ctxt Контекст объекта-сериализатора
         * @param snapshot Отображение восстанавливаемых свойств объекта
         * {RU}
         *
         * {EN}
         * Restore object state to snapshot state (Deserialize)
         *
         * @param ctxt Serializer context
         * @param snapshot map of object properties
         * {EN}
         */
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            val pAddrStart = loadHex(snapshot, "start", ULONG_MAX)
            check(pAddrStart == start) { "start: %08X != %08X".format(start, pAddrStart) }
        }

        private fun beforeFetchOrRead(from: MasterPort, ea: ULong): Boolean {
            if (verbose) log.config { "$name: [0x${core.cpu.pc.hex8}]  READ[$access] -> Read access to verbose area [${ea.hex8}]" }
            when (access.read) {
                GRANT -> return true
                BREAK -> {
                    if (isDebuggerPresent) debugger.isRunning = false
                    return true
                }
                ERROR -> throw MemoryAccessError(core.pc, ea, LOAD, message = "[$access] The area $name can't be read")
            }
            return false
        }

        override fun beforeFetch(from: MasterPort, ea: ULong): Boolean = beforeFetchOrRead(from, ea)

        /**
         * {RU}
         * Проверка доступности операции записи
         * Примечание: этот метод должен быть вызван перед выполнением записи в адресное пространство
         *
         * @param from Порт который инициировал запись
         * @param ea Адрес для проверки
         *
         * @return Доступность операции записи (true/false)
         * {RU}
         *
         * {EN}
         * Check whether area is writable according to [access] property, specified [ea]
         * NOTE: This function MUST BE called for each write method before do any actions!
         *
         * @param from port that requested write operation
         * @param ea address to check
         *
         * @return if true then write can be proceed
         * {EN}
         */
        override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong): Boolean {
            if (verbose) log.config { "$name: [0x${core.cpu.pc.hex8}] WRITE[$access] -> Write access to verbose area [${ea.hex8}]" }
            when (access.write) {
                GRANT -> return true
                BREAK -> {
                    if (isDebuggerPresent) debugger.isRunning = false
                    return true
                }
                ERROR -> throw MemoryAccessError(
                    core.pc,
                    ea,
                    STORE,
                    message = "[$access] The area $name can't be written"
                )
            }
            return false
        }

        /**
         * {RU}
         * Проверка доступности операции чтения
         * Примечание: этот метод должен быть вызван перед выполнением чтения из адресного пространства
         *
         * @param from Порт, который инициировал чтение
         * @param ea Адрес для проверки
         * @return Доступность операции записи (true/false)
         * {RU}
         *
         * {EN}
         * Check whether area is readable according to [access] property and specified [ea]
         * NOTE: This function MUST BE called for each read method before do any actions!
         *
         * @param from port that requested read operation
         * @param ea address to check
         * @return if true then read can be proceed else 0 should be returned
         * {EN}
         */
        override fun beforeRead(from: MasterPort, ea: ULong) = beforeFetchOrRead(from, ea)

        override fun fetch(ea: ULong, ss: Int, size: Int): ULong =
            throw IllegalAccessException("Area isn't fetchable by default!")

        override fun toString(): String = "$port->$name[${start.hex8}..${endInclusively.hex8}]"

        fun contains(ea: ULong) = ea in start..endInclusively

        private inline fun errorIf(condition: Boolean, message: () -> String) {
            if (condition) throw AreaDefinitionError(message())
        }

        init {
            errorIf(start > endInclusively) { "$this -> area start > Area end: [${start.hex8} > ${endInclusively.hex8}]" }
            errorIf(start >= port.size) { "$this -> area start >= port size [${start.hex8} >= ${port.size.hex8}]" }
            errorIf(endInclusively >= port.size) { "$this -> area end >= port size [${endInclusively.hex8} >= ${port.size.hex8}]" }

            @Suppress("LeakingThis")
            port.add(this)

            errorIf(areas.any { it.name == name }) {
                "$this -> can't create area $this because name ${name} already exists at module ${this@Module.fullname()}"
            }

            areas.add(this)
        }
    }

    /**
     * {RU}
     * Внутренний класс Пустого Адресного Пространства.
     * При чтении возвращается всегда 0, запись эффекта не имеет
     *
     * Имеет порт и границы, но не содержит данных.
     * @property port Порт, связанный с адресным пространством
     * @property start Начальный адрес пространства
     * @property endInclusively Конечный адрес пространства (включительно)
     * @property name Произвольное имя объекта адресного пространства
     * @property access Тип доступа
     * @property verbose Флаг "Подробности"
     * {RU}
     *
     * {EN}
     * Empty address space (it has port and address boundaries, but doesn't have any data).
     * It returns 0 on read. Write has no affect
     *
     * @property port port connected to the address space
     * @property start address space start address
     * @property endInclusively address space end address (inclusively in range)
     * @property name area address space
     * @property access access type
     * @property verbose verbose flag
     * {EN}
     */
    open inner class Void(
        port: SlavePort,
        start: ULong,
        endInclusively: ULong,
        name: String,
        access: ACCESS = ACCESS.R_W,
        verbose: Boolean = false
    ) : Area(port, start, endInclusively, name, access, verbose) {
        override fun fetch(ea: ULong, ss: Int, size: Int): ULong = 0u
        override fun read(ea: ULong, ss: Int, size: Int): ULong = 0u
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = Unit

        override fun load(ea: ULong, size: Int, ss: Int, onError: HardwareErrorHandler?): ByteArray = ByteArray(size)
        override fun store(ea: ULong, data: ByteArray, ss: Int, onError: HardwareErrorHandler?) = Unit
    }

    /**
     * {RU}
     * Базовый класс для памяти. Унаследован от класса [Area]
     * Имеет порт и границы адресного пространства и содержит хранилище данных.
     *
     * @property port Порт, связанный с адресным пространством
     * @property start Начальный адрес пространства
     * @property endInclusively Конечный адрес пространства (включительно)
     * @property name Произвольное имя объекта адресного пространства
     * @property access Тип доступа (по умолчанию, ACCESS.I_I)
     * @property verbose Флаг "Подробности" (по умолчанию, false)
     * @property endian Тип порядка байт (по умолчанию, LITTLE_ENDIAN)
     *
     * @property content Байтовый буфер, для хранения данных
     * @property pageSize Размер страницы памяти (по умолчанию, 0x2000)
     * @property dirtyPages Набор измененных страниц памяти [THashSet]
     * @property pageMask Маска адреса страницы памяти
     * @property emptyPage Пустая страница памяти (используется для инициализации)
     * @property snapshotName имя файла для хранения снимка памяти (для сериализации)
     * {RU}
     *
     * {EN}
     * It is class with memory. It inherited from [Area].
     * It has port and address boundaries and have data storage.
     *
     * @property port port connected to the address space
     * @property start address space start address
     * @property endInclusively address space end address (inclusively in range)
     * @property name area address space
     * @property access access type (ACCESS.I_I by default)
     * @property verbose verbose flag (false by default)
     * @property endian endian type (LITTLE_ENDIAN by default)
     *
     * @property content byte buffer for data storage
     * @property pageSize Buffer page size (0x2000 by default)
     * @property dirtyPages Set of modified pages [THashSet]
     * @property pageMask mask for memory page
     * @property emptyPage empty page (it is use for initialization)
     * @property snapshotName name for serialization
     * {EN}
     */
    inner class Memory(
        port: SlavePort,
        start: ULong,
        endInclusively: ULong,
        name: String,
        access: ACCESS,
        verbose: Boolean = false,
        endian: ByteOrder = ByteOrder.LITTLE_ENDIAN
    ) : Area(port, start, endInclusively, name, access, verbose) {
        override fun stringify(): String = dump(16, 1, '#', '-')

        private var content = runCatching { byteBuffer(size.int, settings.directedMemory, endian) }
            .onFailure { "Can't allocate $size bytes for memory buffer" }
            .getOrThrow()

        private val pageSize = 0x2000u
        private val pageCount = size / pageSize + ((size % pageSize) and 1u)
        private val dirtyPages = HashSet<UInt>(pageCount.int)
        private val pageMask = (pageSize - 1u).inv()
        private val emptyPage = ByteArray(pageSize.int)
        var endian: ByteOrder
            get() = content.order()
            set(value) {
                content.order(value)
            }

        /**
         * {EN}Reset memory object (clear dirty pages, rewind byte buffer){EN}
         *
         * {RU}Сброс объекта памяти (очистка занятых страниц, откат позиции байтового буфера){RU}
         */
        override fun reset() {
            super.reset()
            dirtyPages.sorted().forEach { pageAddress ->
                content.position(pageAddress.int)
                if (pageAddress + pageSize < content.limit().ulong_z)
                    content.put(emptyPage)
                else
                    content.put(ByteArray(content.limit() - pageAddress.int))
            }
            content.rewind()
        }

        private inline fun indexOf(ea: ULong) = (ea - start).int

        // isReadable check when bus select an area
        private inline fun fetchOrRead(ea: ULong, ss: Int, size: Int, access: AccessAction) = with (content) {
            val index = indexOf(ea)
            require(index + size <= content.limit()) { "Index out of bound: index=$index, " +
                    "size=${size.hex}, content limit=${content.limit().hex}, port: ${this@Memory.port}" }
            when (size) {
                QWORD.bytes -> getLong(index).ulong
                BYTES7.bytes -> getLong(index).ulong like BYTES7
                FWORD.bytes -> getLong(index).ulong like FWORD
                BYTES5.bytes -> getLong(index).ulong like BYTES5

                DWORD.bytes -> getInt(index).ulong_z
                TRIBYTE.bytes -> getInt(index).ulong_z like TRIBYTE

                WORD.bytes -> getShort(index).ulong_z

                BYTE.bytes -> get(index).ulong_z

                else -> throw MemoryAccessError(core.pc, ea, access, "Unsupported read size $size bytes")
            }
        }

        override fun fetch(ea: ULong, ss: Int, size: Int): ULong = fetchOrRead(ea, ss, size, FETCH)

        /**
         * {RU}
         * Чтение значения из памяти
         *
         * @param ea Адрес в памяти
         * @param ss Адрес сегмента памяти
         * @param size Размер данных в байтах для считывания (поддерживаются QWORD, FWORD, DWORD, TRIBYTE, WORD, BYTE)
         *
         * @return Прочитанное значение
         * @throws MemoryAccessError при неизвестном типе данных для считывания
         * {RU}
         *
         * {EN}
         * Read value from memory
         *
         * @param ea address in memory
         * @param ss segment selector value in memory
         * @param size Number of bytes to read (you can use QWORD, FWORD, DWORD, TRIBYTE, WORD, BYTE)
         *
         * @return Received value
         * {EN}
         */
        override fun read(ea: ULong, ss: Int, size: Int) = fetchOrRead(ea, ss, size, LOAD)

        /**
         * {RU}
         * Запись значения в память
         *
         * @param ea Адрес в памяти
         * @param ss Адрес сегмента памяти
         * @param size Размер данных в байтах для считывания (поддерживаются QWORD, FWORD, DWORD, TRIBYTE, WORD, BYTE)
         * @param value Значение для записи в память
         *
         * @throws MemoryAccessError при неизвестном типе данных для считывания
         * {RU}
         *
         * {EN}
         * Write value to memory
         *
         * @param ea address in memory
         * @param ss segment selector value in memory
         * @param size Number of bytes to read (you can use QWORD, FWORD, DWORD, TRIBYTE, WORD, BYTE)
         * @param value value to write to the memory
         * {EN}
         */
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            // isWritable check when bus select a area
            val index = indexOf(ea)
            require (index + size <= content.limit()) { "Index out of bound: index=$index, " +
                    "size=${size.hex}, content limit=${content.limit().hex}, port: ${this@Memory.port}" }
            dirtyPages.add(index.uint and pageMask)
            with(content) {
                when (size) {
                    QWORD.bytes -> putLong(index, value.long)
                    FWORD.bytes -> {
                        putLong(index + 0, value[31..0].long) // lo
                        putShort(index + 4, value[47..32].short) // hi
                    }
                    DWORD.bytes -> putInt(index, value.int)
                    WORD.bytes -> putShort(index, value.short)
                    BYTE.bytes -> put(index, value.byte)

                    else -> throw MemoryAccessError(core.pc, ea, LOAD, "Unsupported write size $size bytes")
                }
            }
        }

        /**
         * {RU}
         * Чтение блока данных из памяти.
         * Основное назначение - считывание памяти отладчиком.
         *
         * @param ea Адрес в памяти
         * @param ss Адрес сегмента памяти (не используется)
         * @param size Количество байт для чтения
         * @param onError Обработчик ошибки доступа к памяти, по умолчанию будет выброшено исключение [MemoryAccessError]
         * {RU}
         *
         * {EN}
         * Read data block from memory. Designed to be read by the debugger etc.
         *
         * @param ea address in memory
         * @param ss segment selector value in memory (doesn't use)
         * @param size bytes count to read
         * @param onError error handler on memory access (throws [MemoryAccessError] by default)
         * {EN}
         */
        override fun load(ea: ULong, size: Int, ss: Int, onError: HardwareErrorHandler?): ByteArray {
            val offset = (ea - start).int
            val pos = content.position()
            content.position(offset)
            val result = ByteArray(size).apply { content.get(this) }
            content.position(pos)
            return result
        }

        /**
         * {RU}
         * Запись блока данных в память.
         * Основное назначение - считывание памяти отладчиком или инициализация.
         *
         * @param ea Адрес в памяти
         * @param ss Адрес сегмента памяти (не используется)
         * @param data Массив байт для записи в память
         * @param onError Обработчик ошибки доступа к памяти, по умолчанию будет выброшено исключение [MemoryAccessError]
         * {RU}
         *
         * {EN}
         * Write byte array to memory. Designed to initialization and for debugger.
         *
         * @param ea address in memory
         * @param ss segment selector value in memory (doesn't use)
         * @param data byte array to store
         * @param onError error handler on memory access (throws [MemoryAccessError] by default)
         * {EN}
         */
        override fun store(ea: ULong, data: ByteArray, ss: Int, onError: HardwareErrorHandler?) {
            if (data.isEmpty()) {
                return
            }
            val offset = (ea - start).int
            content.position(offset)
            content.put(data)
            val endEa = ea + data.size.uint - 1u
            (ea until endEa step pageSize.long_z).forEach {
                val pageOffset = (it - start).uint
                dirtyPages.add(pageOffset and pageMask)
            }
        }

        /**
         * {RU}
         * Запись блока данных в память.
         * Основное назначение - считывание памяти отладчиком или инициализация.
         *
         * @param ea Адрес в памяти
         * @param stream Поток данных для записи
         * {RU}
         *
         * {EN}
         * Write byte array to memory. Designed to initialization and for debugger.
         *
         * @param ea address in memory
         * @param stream read memory stream for data access
         * {EN}
         */
        fun write(ea: ULong, stream: InputStream) {
            val offset = (ea - start).int
            val count = stream.readBufferData(content, offset)
            if (count == 0) {
                return
            }
            val endEa = ea + count - 1u
            (ea until endEa step pageSize.long_z).forEach {
                val pageOffset = it - start
                dirtyPages.add((pageOffset and pageMask.ulong_z).uint)
            }
        }

        /**
         * {RU}
         * Визуальное отображение занятых и свободных страниц памяти. Не является HEX-отображением памяти.
         *
         * @param cols Количество столбцов
         * @param rows Количество строк
         * @param fillDirty Заполнитель для занятных страниц памяти
         * @param fillClear Заполнитель для свободных страниц памяти
         *
         * @return Строка, разделенная пробелами
         * {RU}
         *
         * {EN}
         * Representation  modified and free memory pages (it is not hex representation)
         *
         * @param cols number of columns
         * @param rows number of rows
         * @param fillDirty filler for occupied memory pages
         * @param fillClear filler for free memory pages
         *
         * @return string separated by spaces
         * {EN}
         */
        fun dump(cols: Int, rows: Int, fillDirty: Char, fillClear: Char): String {
            val blockSize = size / (cols * rows).uint
            val lines = Array(rows) { CharArray(cols) { fillClear } }
            dirtyPages.sorted().forEach {
                val start = it.int
                val end = (it + pageSize - 1u).int
                for (k in start until end) {
                    val row = k / cols
                    val col = k % cols
                    lines[row][col] = fillDirty
                }
            }

            return lines
                .mapIndexed { k, line -> "\t%08X: [ %s ]".format(k.uint * blockSize * cols.uint, String(line)) }
                .joinToString("\n")
        }

        val snapshotName = "${fullname()}_$name.bin"

        /**
         * {RU}
         * Сохранение состояния (сериализация)
         *
         * @param ctxt Контекст объекта-сериализатора
         *
         * @return Отображение сохраняемых свойств объекта
         * {RU}
         *
         * {EN}
         * Save object state to snapshot (Serialize)
         *
         * @param ctxt Serializer context
         *
         * @return map of object properties
         * {EN}
         */
        override fun serialize(ctxt: GenericSerializer) =
            storeValues(snapshotName to ctxt.storeBinary(snapshotName, content))

        /**
         * {RU}
         * Восстановление состояния (десериализация)
         *
         * @param ctxt Контекст объекта-сериализатора
         * @param snapshot Отображение восстанавливаемых свойств объекта
         * {RU}
         *
         * {EN}
         * Restore object state to snapshot state (Deserialize)
         *
         * @param ctxt Serializer context
         * @param snapshot map of object properties
         * {EN}
         */
        @Suppress("UNCHECKED_CAST")
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            if (!ctxt.doRestore) {
                try {
                    if (!ctxt.loadBinary(snapshot, snapshotName, content))
                        log.warning { "Can't load $snapshotName -> perhaps snapshot has old version!" }
                } catch (e: MemoryDeserializeSizeMismatchException) {
                    log.warning { "[${this@Module.name}] ByteBuffer limit is smaller, than in the snapshot, " +
                            "oldLimit=0x${e.actualSize.hex} snapshotLimit=0x${e.snapshotSize.hex}" }

                    // Pass BufferOverflow exception
                    content = byteBuffer(e.snapshotSize, settings.directedMemory, endian)
                    if (!ctxt.loadBinary(snapshot, snapshotName, content))
                        log.warning { "Can't load $snapshotName -> perhaps snapshot has old version!" }
                }
            } else if (access == ACCESS.R_W && dirtyPages.isNotEmpty()) {
                ctxt.restoreBinary(snapshot, snapshotName, content, dirtyPages, pageSize.int)
                dirtyPages.clear()
            }
        }
    }

    internal fun interface Translator : IConstructorSerializable {
        fun translate(ea: ULong): ULong
    }

    private class OffsetTranslator(val start: ULong = 0u, val offset: ULong = 0u) : Translator {
        override fun translate(ea: ULong) = ea - start + offset

        override fun toString() = "Basic[offset=0x${offset.hex8}]"
    }

    private class PhonyTranslator : Translator {
        override fun translate(ea: ULong) = ea

        override fun toString() = "Phony"
    }

    internal data class Region constructor(
        // order in which regions added to mapping,
        // in fact it defines regions priority and required to deserialization
        val ord: Int,
        val port: MasterPort,  // output port
        val interval: Interval,
        val rights: Int,
        val translator: Translator
    ) : IFetchReadWrite {
        private inline val ULong.translated get() = translator.translate(this)

        override fun fetch(ea: ULong, ss: Int, size: Int) =
            port.fetch(ea.translated, ss, size)

        override fun read(ea: ULong, ss: Int, size: Int) =
            port.read(ea.translated, ss, size)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) =
            port.write(ea.translated, ss, size, value)

        override fun toString() = "from $interval to $port with translation $translator"

        val readable = rights[2].truth
        val writable = rights[1].truth
        val fetchable = rights[0].truth
    }

    inner class MappingArea constructor(
        port: SlavePort,
        name: String = "Mapping",
        private val outputs: Map<Int, MasterPort>,
        val default: Int,
    ) : Area(port, 0u, port.size - 1u, name) {

        private val map = PriorityTreeIntervalMap(name)
        private val initialOrd = 0
        private val maxRegionsCount = bitMask32(MAPPING_SS_OUTPUT_RANGE.length)
        private val regions = Array<Region?>(maxRegionsCount) { null }

        init {
            reinitialize()
        }

        private fun nextOrd() = regions.filterNotNull().maxOf { it.ord } + 1

        internal fun addMapping(first: ULong, last: ULong, output: Int, rights: Int, translator: Translator) {
            val port = outputs[output] ?: run {
                log.warning { "Can't map [0x${first.hex8}..0x${last.hex8}] from $port because no output port with number = $output" }
                return
            }

            val interval = map.add(output, first, last)
            regions[output] = Region(nextOrd(), port, interval, rights, translator)
        }

        internal fun removeMapping(output: Int) {
            regions[output] = null
            map.remove(output)
        }

        private fun translate(ea: ULong): Region {
            val interval = map[ea]
            return regions[interval.id] ?: error("Bogus MappingArea state for id = ${interval.id}")
        }

        internal fun reinitialize() {
            map.clear()
            regions.fill(null)

            val prt = outputs[default] ?: error("Can't map init region because no output port with number = $default")
            val interval = map.init(default, 0u, endInclusively)
            val translator = PhonyTranslator()
            regions[default] = Region(initialOrd, prt, interval, MAPPING_RIGHTS_RWE, translator)
        }

        override fun fetch(ea: ULong, ss: Int, size: Int) = translate(ea).fetch(ea, ss, size)

        override fun read(ea: ULong, ss: Int, size: Int) = translate(ea).read(ea, ss, size)

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) = translate(ea).write(ea, ss, size, value)

        override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + mapOf(
            "regions" to regions.filterNotNull().map { region ->
                storeValues(
                    "ord" to region.ord,
                    "output" to region.interval.id,
                    "first" to region.interval.first,
                    "last" to region.interval.last,
                    "rights" to region.rights,
                    "translator" to ctxt.serializeItem(region.translator, "translator${region.ord}")
                )
            }
        )

        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            super.deserialize(ctxt, snapshot)

            reinitialize()

            snapshot["regions"]
                .cast<List<Map<String, Any>>>()
                .sortedBy { loadValue<Int>(it, "ord") }
                .drop(1)
                .forEach {
                    val ord = loadValue<Int>(it, "ord")
                    val first = ctxt.deserializePrimitive(it["first"], ULong::class.java) as ULong
                    val last = ctxt.deserializePrimitive(it["last"], ULong::class.java) as ULong
                    val output = loadValue<Int>(it, "output")
                    val rights = loadValue<Int>(it, "rights")
                    val translator = ctxt.deserializeItem(it["translator"].cast(), "translator${ord}")

                    addMapping(first, last, output, rights, translator as Translator)
                }
        }

        override fun stringify() = "$name: $map"
    }

    inner class Mapper constructor(
        port: SlavePort,
        name: String = "Mapper",
        private val areas: Map<Int, MappingArea>
    ) : Area(port, 0u, port.size - 1u, name) {
        override fun fetch(ea: ULong, ss: Int, size: Int) = throw IllegalAccessException("$name may not be fetched!")
        override fun read(ea: ULong, ss: Int, size: Int) = throw IllegalAccessException("$name may not be read!")

        private fun area(index: Int) = requireNotNull(areas[index]) { "Area with index $index not found in Mapper" }

        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            val operation = ss[MAPPING_SS_OPERATION_RANGE]
            val area = ss[MAPPING_SS_AREA_RANGE]
            val output = ss[MAPPING_SS_OUTPUT_RANGE]

            when (operation) {
                MAPPING_MAP_CMD -> {
                    val width = ss[MAPPING_SS_WIDTH_RANGE]  // width not check because range should limit it

                    val last = ea + ubitMask64(width)

                    require(last > ea) { "Region width=$width overflow 64 bits space from first=0x${ea.hex8}" }

                    val rights = ss[MAPPING_SS_RIGHTS_RANGE]

                    val translator = when (val translation = ss[MAPPING_SS_TRANSLATION_RANGE]) {
                        MAPPING_TRANSLATION_OFFSET -> OffsetTranslator(ea, value)
                        else -> error("Unsupported translation type = $translation")
                    }

                    area(area).addMapping(ea, last, output, rights, translator)
                }

                MAPPING_UNMAP_CMD -> area(area).removeMapping(output)

                else -> error("Unsupported mapping operation = $operation")
            }
        }
    }


    class RegisterDefinitionError(message: String) : Exception(message)

    /**
     * {RU}
     * Аппаратный регистр для описания периферийных устройств и модулей.
     *
     * Регистр представляет собой область памяти, описываемую адресом и типом данных.
     *
     * @property port порт, связанный с регистром
     * @property address адрес расположения регистра
     * @param name произвольное имя объекта регистра
     * @property default значение по умолчанию (по умолчанию, 0)
     * @property readable флаг возможности чтения по умолчанию (по умолчанию, true)
     * @property writable флаг возможности записи по умолчанию (по умолчанию, true)
     * @property level уровен логгирования (по умолчанию, FINE)
     *
     * @property data значение регистра по умолчанию [default]
     * @property name имя регистра
     * {RU}
     */
    open inner class Register constructor(
        val port: SlavePort,
        val address: ULong,
        val datatype: Datatype,
        name: String,
        val default: ULong = 0uL,
        val writable: Boolean = true,
        val readable: Boolean = true,
        val level: Level = Level.FINE
    ) : IFetchReadWrite, IValuable, ICoreUnit {
        val module = this@Module

        final override var data = default

        override val name: String = "$name@${address.hex}[$datatype]"

        /**
         * {RU}
         * Строковое представление регистра.
         *
         * @return строковое представление регистра.
         * {RU}
         */
        override fun toString(): String = "$port->$name"

        override fun stringify(): String = "%s.%s %08X".format(fullname(), name, data.long)

        /**
         * {RU}Сброс регистра.{RU}
         */
        override fun reset() {
            super.reset()
            data = default
        }

        /**
         * {RU}
         * Сохранение состояния (сериализация)
         *
         * @param ctxt контекст объекта-сериализатора
         *
         * @return отображение сохраняемых свойств объекта
         * {RU}
         */
        override fun serialize(ctxt: GenericSerializer): Map<String, Any> = mapOf(
            "pName" to port.name,
            "pAddr" to address.hex8,
            "data" to data.hex16
        )

        /**
         * {RU}
         * Восстановление состояния (десериализация)
         * @param ctxt контекст объекта-сериализатора
         * @param snapshot отображение восстанавливаемых свойств объекта
         * {RU}
         */
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            val pNameSnapshot = loadValue<String>(snapshot, "pName") { port.name }
            val pAddrSnapshot = loadValue<String>(snapshot, "pAddr").ulongByHex

            if (pNameSnapshot != port.name || pAddrSnapshot != address) {
                log.severe { "$name: Try to update snapshot or check registers order in source code, it should be same with snapshot. Skipping" }
                return
            }

            data = loadValue<String>(snapshot, "data").ulongByHex
        }

        fun Logger.read(level: Level) = log(level) { "[0x%08X] RD <- %s".format(core.cpu.pc.long, stringify()) }

        fun Logger.write(level: Level) = log(level) { "[0x%08X] WR -> %s".format(core.cpu.pc.long, stringify()) }

        final override fun fetch(ea: ULong, ss: Int, size: Int) =
            throw IllegalAccessException("Register may not be executed")

        /**
         * {RU}
         * Чтение значения регистра
         *
         * @param ea адрес (не используется)
         * @param ss сегмент памяти (не используется)
         * @param size размер (не используется)
         *
         * @return значение регистра
         * {RU}
         **/
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            log.read(level)
            return data
        }

        /**
         * {RU}
         * Запись значения регистра
         *
         * @param ea адрес (не используется)
         * @param ss сегмент памяти (не используется)
         * @param size размер (не используется)
         * @param value новое значение для регистра
         * {RU}
         */
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            data = value
            log.write(level)
        }

        final override fun beforeFetch(from: MasterPort, ea: ULong): Boolean = false

        /**
         * {RU}
         * Проверка возможности чтения из регистра
         *
         * @param from порт порт который инициировал запись
         * @param ea адрес (не используется)
         *
         * @return результат (true/false)
         * {RU}
         */
        override fun beforeRead(from: MasterPort, ea: ULong): Boolean = readable

        /**
         * {RU}
         * Проверка возможности записи в регистр
         * @param from порт порт который инициировал запись
         * @param ea адрес (не используется)
         * @return результат (true/false)
         * {RU}
         *
         * {EN}
         * Check if register can be written
         * Note: data in register was changed temporary to new value to may possible use bit fields
         * {EN}
         */
        override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong): Boolean = writable

        private inline fun errorIf(condition: Boolean, message: () -> String) {
            if (condition) throw RegisterDefinitionError(message())
        }

        init {
            errorIf(address >= port.size) {
                "$this -> register address >= port size [${address.hex} >= ${port.size.hex}]"
            }

            @Suppress("LeakingThis")
            port.add(this)

            errorIf(registers.any { it.name == this.name }) {
                "$this -> can't create register because name '${this.name}' already exists at module ${this@Module.fullname()}"
            }

            @Suppress("LeakingThis")
            registers.add(this)
        }
    }

    /**
     * {RU}
     * Регистр с возможностью доступа к каждому его байту по отдельности.
     * Обычный регистр не дает возможности доступа по другим адресам за исключением базового адреса.
     *
     * Регистр представляет собой область памяти, описываемую адресом и типом данных.
     *
     * @param name произвольное имя объекта регистра
     * @property port порт, связанный с регистром
     * @property address адрес расположения регистра
     * @property datatype тип данных регистра [Datatype]
     * @property level уровен логгирования (по умолчанию, FINE)
     * @property readable флаг возможности чтения по умолчанию (по умолчанию, true)
     * @property writable флаг возможности записи по умолчанию (по умолчанию, true)
     * @property default значение по умолчанию (по умолчанию, 0)
     *
     * @property data значение регистра по умолчанию [default]
     * @property name имя регистра
     * {RU}
     **/
    open inner class ByteAccessRegister(
        val port: SlavePort,
        val address: ULong,
        val datatype: Datatype,
        name: String,
        val default: ULong = 0u,
        private val writable: Boolean = true,
        private val readable: Boolean = true,
        val level: Level = Level.FINE
    ) : IReadWrite, IValuable, ICoreUnit {

        private inner class Cell(address: ULong, name: String) : Register(port, address, BYTE, name) {
            override fun read(ea: ULong, ss: Int, size: Int) =
                this@ByteAccessRegister.read(ea, ss, size)
            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) =
                this@ByteAccessRegister.write(ea, ss, size, value)

            override fun beforeRead(from: MasterPort, ea: ULong) =
                this@ByteAccessRegister.beforeRead(from, ea)
            override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong) =
                this@ByteAccessRegister.beforeWrite(from, ea, value)

            override fun reset() {
                val current = address
                with (this@ByteAccessRegister) {
                    if (current == address + datatype.bytes - 1u) reset()
                }
            }

            override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
                data = readInternal(address, 0, datatype.bytes)
                return super.serialize(ctxt)
            }

            override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
                super.deserialize(ctxt, snapshot)
                writeInternal(address, 0, datatype.bytes, data)
            }
        }

        private val cells = Array(datatype.bytes) { Cell(address + it.uint, "${name}_$it") }

        final override var data: ULong = default

        override val name: String = "$name@${address.hex}"

        /**
         * {RU}
         * Строковое представление регистра.
         * @return строковое представление регистра.
         * {RU}
         */
        override fun toString(): String = "$port->$name"

        override fun stringify(): String = "%s.%s %08X".format(this@Module.name, name, data.long)

        private fun Logger.read(level: Level) = log(level) { "[0x%08X] RD <- %s".format(core.cpu.pc.long, stringify()) }

        private fun Logger.write(level: Level) = log(level) { "[0x%08X] WR -> %s".format(core.cpu.pc.long, stringify()) }

        /**
         * {RU}Сброс регистра (очистка значения){RU}
         */
        override fun reset() {
            super.reset()
            data = default
        }

        /**
         * {RU}
         * Сохранение состояния (сериализация)
         *
         * @param ctxt контекст объекта-сериализатора
         *
         * @return отображение сохраняемых свойств объекта
         * {RU}
         */
        override fun serialize(ctxt: GenericSerializer): Map<String, Any> = mapOf(
            "pAddr" to address.hex8,
            "data" to data.hex8
        )

        /**
         * {RU}
         * Восстановление состояния (десериализация)
         * @param ctxt контекст объекта-сериализатора
         * @param snapshot отображение восстанавливаемых свойств объекта
         * {RU}
         */
        override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            val pAddrSnapshot = (snapshot["pAddr"] as String).ulongByHex

            check(pAddrSnapshot == address) { "pAddr: %08X != %08X".format(address, pAddrSnapshot) }

            data = (snapshot["data"] as String).ulongByHex
        }

        fun readInternal(ea: ULong, ss: Int, size: Int): ULong {
            require(ea + size.uint <= address + datatype.bytes.uint) {
                "$name read out of range ea=${ea.hex8} size=$size address=${address.hex8} datasize=${datatype.bytes}"
            }
            return data[range(ea, size)]
        }

        /**
         * {RU}
         * Чтение значения (только BYTE и DWORD)
         *
         * @param ea адрес
         * @param ss адрес сегмента
         * @param size размер данных для чтения
         *
         * @return вычитанное значение
         * {RU}
         */
        override fun read(ea: ULong, ss: Int, size: Int): ULong {
            log.read(level)
            return readInternal(ea, ss, size)
        }

        fun writeInternal(ea: ULong, ss: Int, size: Int, value: ULong) {
            require(ea + size.uint <= address + datatype.bytes.uint) {
                "$name write out of range ea=${ea.hex8} size=$size address=${address.hex8} size=${datatype.bytes}"
            }
            data = data.insert(value, range(ea, size))
        }

        /**
         * {RU}
         * Запись значения в регистр (только BYTE и DWORD)
         *
         * @param ea адрес
         * @param ss адрес сегмента
         * @param size размер данных для чтения
         * @param value значение для записи в регистр
         * {RU}
         */
        override fun write(ea: ULong, ss: Int, size: Int, value: ULong) {
            writeInternal(ea, ss, size, value)
            log.write(level)
        }

        /**
         * {RU}
         * Функция возвращает диапазон битов для чтения/записи в регистр в зависимости
         * от начального адреса записи и текущего размера записи в регистр
         *
         * @param ea начальный адрес доступа к регистру (адрес выставленный на шину)
         * @param size размер доступа к регистру
         *
         * @return bit range for read/modify register
         * {RU}
         *
         * {EN}
         * Returns bit range for read/write access to register depends on
         * starting access address [ea] and size of current access [size]
         *
         * @param ea current access start address
         * @param size size of current access
         *
         * @return bit range for read/modify register
         * {EN}
         */
        private fun range(ea: ULong, size: Int): IntRange {
            val off = offset(ea).int
            val lsb = off * 8
            val msb = (off + size) * 8 - 1
            return msb..lsb
        }

        fun offset(ea: ULong) = ea - address

        override fun beforeRead(from: MasterPort, ea: ULong) = readable
        override fun beforeWrite(from: MasterPort, ea: ULong, value: ULong) = writable

        override fun load(ea: ULong, size: Int, ss: Int, onError: HardwareErrorHandler?) =
            throw IllegalAccessException("not implemented")

        override fun store(ea: ULong, data: ByteArray, ss: Int, onError: HardwareErrorHandler?) =
            throw IllegalAccessException("not implemented")
    }

    /**
     * {RU}
     * Сохранение состояния (сериализация)
     *
     * @param ctxt контекст объекта-сериализатора
     *
     * @return отображение сохраняемых свойств объекта
     * {RU}
     **/
    override fun serialize(ctxt: GenericSerializer) = super.serialize(ctxt) + storeValues(
        "areas" to areas.map { it.serialize(ctxt) },
        "registers" to registers.map { it.serialize(ctxt) }
    )

    /**
     * {RU}
     * Восстановление состояния (десериализация)
     *
     * @param ctxt контекст объекта-сериализатора
     * @param snapshot отображение восстанавливаемых свойств объекта
     * {RU}
     */
    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        super.deserialize(ctxt, snapshot)

        val snapshotName = snapshot["name"] as? String ?: "UNDEFINED"
        // For snapshot backward compatibility
        val registersInfo = snapshot["registers"]
        if (registersInfo != null) {
            registersInfo as ArrayList<Map<String, Any>>
            if (registersInfo.size != registers.size) {
                log.warning { "Number of registers in snapshot [${registersInfo.size}] not equal to actual count [${registers.size}]" }
                registersInfo.forEach { rInfo ->
                    val pAddr = (rInfo["pAddr"] as String).ulongByHex
                    val reg = registers.find { it.address == pAddr }
                    if (reg != null) {
                        log.finer { "Register ${reg.name} found in module $name -> loading" }
                        runCatching {
                            reg.deserialize(ctxt, rInfo)
                        }.onFailure {
                            log.severe { "$name: Can't deserialize: $it" }
                            it.printStackTrace()
                        }
                    } else {
                        log.warning { "Can't load register $rInfo -> omitted" }
                    }
                }
            } else {
                registers.deserialize(ctxt, registersInfo)
            }
        }
        if (name != snapshotName && !ctxt.suppressWarnings) {
            log.warning { "Wrong module name expected '$name' but have '$snapshotName' in snapshot -> omit" }
        }

        val areasInfo = snapshot["areas"]
        if (areasInfo != null) {
            areasInfo as ArrayList<Map<String, Any>>
            if (areasInfo.size != areas.size) {
                log.warning { "Number of areas in snapshot [${areasInfo.size}] not equal to actual count [${areas.size}]" }
            } else {
                areas.deserialize(ctxt, areasInfo)
            }
        }

    }

    open inner class ComplexRegister(port: SlavePort, vAddr: ULong, name: String, default: ULong = 0u) :
        Register(port, vAddr, DWORD, name, default) {
        // Result from reading SET, CLR, INV should be undefined but for debugging return data value 0
        val BASE get() = this
        val CLR = object : Register(port, vAddr + 4u, DWORD, name + "CLR", 0x0000_0000u) {
            override fun read(ea: ULong, ss: Int, size: Int): ULong = 0u
            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) =
                BASE.write(ea, ss, size, BASE.data and value.inv())
        }
        val SET = object : Register(port, vAddr + 8u, DWORD, name + "SET", 0x0000_0000u) {
            override fun read(ea: ULong, ss: Int, size: Int): ULong = 0u
            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) =
                BASE.write(ea, ss, size, BASE.data or value)
        }
        val INV = object : Register(port, vAddr + 12u, DWORD, name + "INV", 0x0000_0000u) {
            override fun read(ea: ULong, ss: Int, size: Int): ULong = 0u
            override fun write(ea: ULong, ss: Int, size: Int, value: ULong) =
                BASE.write(ea, ss, size, BASE.data xor value)
        }
    }

    /**
     * {RU}Базовый метод отключения периферийных устройств{RU}
     */
    override fun terminate() {
        log.fine { "Terminate peripheral device %s".format(name) }
        super.terminate()
        areas.forEach { it.terminate() }
        registers.forEach { it.terminate() }
    }

    /**
     * {RU}
     * Сброс модуля (со сбросом регистров и областей)
     *
     * @return строковое представление объекта
     * {RU}
     */
    override fun reset() {
        super.reset()
        areas.forEach { it.reset() }
        registers.forEach { it.reset() }
    }
}