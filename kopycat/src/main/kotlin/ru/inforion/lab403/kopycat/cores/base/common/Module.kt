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

import gnu.trove.set.hash.THashSet
import net.sourceforge.argparse4j.inf.ArgumentParser
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.CONFIG
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.logging.logger.Logger
import ru.inforion.lab403.common.proposal.byteBuffer
import ru.inforion.lab403.common.proposal.toSerializable
import ru.inforion.lab403.kopycat.annotations.ExperimentalWarning
import ru.inforion.lab403.kopycat.cores.base.*
import ru.inforion.lab403.kopycat.cores.base.enums.*
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.interfaces.*
import ru.inforion.lab403.kopycat.serializer.deserialize
import ru.inforion.lab403.kopycat.serializer.loadHex
import ru.inforion.lab403.kopycat.serializer.loadValue
import ru.inforion.lab403.kopycat.serializer.storeValues
import ru.inforion.lab403.kopycat.settings
import java.io.InputStream
import java.nio.ByteOrder
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
 * @property debugger component [debugger] of emulator sysem
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
        @Transient val log = logger(CONFIG)
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
            log.warning { "ATTENTION: Some ports has warning use printModulesPortsWarnings to see it..." }
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
    open fun onPortConnected(port: APort, bus: Bus, offset: Long) = Unit

    /**
     * {EN}
     * Callback called when any port [port] of module disconnected from bus [bus] with [offset]
     *
     * @param port connected port
     * @param bus target bus
     * @param offset connection offset
     * {EN}
     */
    open fun onPortDisconnect(port: APort, bus: Bus, offset: Long) = Unit

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
     * @property end Конечный адрес пространства
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
     * @property end address space end address
     * @property name area address space
     * @property access access type
     * @property verbose verbose flag
     *
     * @property size size of address space in bytes
     * {EN}
     */
    abstract inner class Area constructor(
            val port: SlavePort,
            val start: Long,
            val end: Long,
            final override val name: String,
            val access: ACCESS = ACCESS.R_W,
            private val verbose: Boolean = false
    ) : IFetchReadWrite, ICoreUnit {
        val module = this@Module

        val size = end - start + 1

        constructor(port: SlavePort, name: String, access: ACCESS = ACCESS.R_W, verbose: Boolean = false) :
                this(port, 0, port.size - 1, name, access, verbose)

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

        fun range() = LongRange(start, end)

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
                "end" to end.hex8,
                "access" to access)

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
            val pAddrStart = loadHex(snapshot, "start", -1)
            check(pAddrStart == start) { "start: %08X != %08X".format(start, pAddrStart) }
        }

        private fun beforeFetchOrRead(from: MasterPort, ea: Long): Boolean {
            if (verbose) log.warning { "$name: [${core.cpu.pc.hex8}]  READ[$access] -> Read access to verbose area [${ea.hex8}]" }
            when (access.read) {
                GRANT -> return true
                BREAK -> if (isDebuggerPresent) debugger.isRunning = false
                ERROR -> throw MemoryAccessError(core.pc, ea, LOAD, message = "[$access] The area $name can't be read")
            }
            return false
        }

        override fun beforeFetch(from: MasterPort, ea: Long): Boolean = beforeFetchOrRead(from, ea)

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
        override fun beforeWrite(from: MasterPort, ea: Long, value: Long): Boolean {
            if (verbose) log.warning { "$name: [${core.cpu.pc.hex8}] WRITE[$access] -> Write access to verbose area [${ea.hex8}]" }
            when (access.write) {
                GRANT -> return true
                BREAK -> if (isDebuggerPresent) debugger.isRunning = false
                ERROR -> throw MemoryAccessError(core.pc, ea, STORE, message = "[$access] The area $name can't be written")
            }
            return false
        }

        /**
         * {RU}
         * Проверка доступности операции чтения
         * Примечание: этот метод должен быть вызван перед выполнением чтения из адресного пространства
         *
         * @param from Порт который инициировал чтение
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
        override fun beforeRead(from: MasterPort, ea: Long) = beforeFetchOrRead(from, ea)

        override fun fetch(ea: Long, ss: Int, size: Int): Long = throw IllegalAccessException("Area isn't fetchable by default!")

        override fun toString(): String = "$port->$name[${start.hex8}..${end.hex8}]"

        fun contains(ea: Long) = ea in start..end

        private inline fun errorIf(condition: Boolean, message: () -> String) {
            if (condition) throw AreaDefinitionError(message())
        }

        init {
            errorIf(start > end) { "$this -> area start > Area end: [${start.hex8} > ${end.hex8}]" }
            errorIf(start >= port.size) { "$this -> area start >= port size [${start.hex8} >= ${port.size.hex8}]" }
            errorIf(end >= port.size) { "$this -> area end >= port size [${end.hex8} >= ${port.size.hex8}]" }

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
     * @property end Конечный адрес пространства
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
     * @property end address space end address
     * @property name area address space
     * @property access access type
     * @property verbose verbose flag
     * {EN}
     */
    inner class Void(
            port: SlavePort,
            start: Long,
            end: Long,
            name: String,
            access: ACCESS = ACCESS.R_W,
            verbose: Boolean = false
    ) : Area(port, start, end, name, access, verbose) {
        override fun fetch(ea: Long, ss: Int, size: Int): Long = 0
        override fun read(ea: Long, ss: Int, size: Int): Long = 0
        override fun write(ea: Long, ss: Int, size: Int, value: Long) = Unit
    }

    /**
     * {RU}
     * Базовый класс для памяти. Унаследован от класса [Area]
     * Имеет порт и границы адресного пространства и содержит хранилище данных.
     *
     * @property port Порт, связанный с адресным пространством
     * @property start Начальный адрес пространства
     * @property end Конечный адрес пространства
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
     * @property end address space end address
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
            start: Long,
            end: Long,
            name: String,
            access: ACCESS,
            verbose: Boolean = false,
            endian: ByteOrder = ByteOrder.LITTLE_ENDIAN
    ): Area(port, start, end, name, access, verbose) {
        override fun stringify(): String = dump(16, 1, '#', '-')

        private val content = byteBuffer(size.asInt, endian, settings.directedMemory).toSerializable()

        private val pageSize = 0x2000
        private val pageCount = size / pageSize + ((size % pageSize) and 1)
        private val dirtyPages = THashSet<Int>(pageCount.asInt)
        private val pageMask = (pageSize - 1).inv()
        private val emptyPage = ByteArray(pageSize)
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
                content.position(pageAddress)
                if (pageAddress + pageSize < content.limit())
                    content.put(emptyPage)
                else
                    content.put(ByteArray(content.limit() - pageAddress))
            }
            content.rewind()
        }

        override fun inb(ea: Long, ss: Int): Long = content.get((ea - start).asInt).asULong
        override fun inw(ea: Long, ss: Int): Long = content.getShort((ea - start).asInt).asULong
        override fun inl(ea: Long, ss: Int): Long = content.getInt((ea - start).asInt).asULong
        override fun inq(ea: Long, ss: Int): Long = content.getLong((ea - start).asInt)

        override fun outb(ea: Long, value: Long, ss: Int) {
            val offset = (ea - start).asInt
            dirtyPages.add(offset and pageMask)
            content.put(offset, value.asByte)
        }

        override fun outw(ea: Long, value: Long, ss: Int) {
            val offset = (ea - start).asInt
            dirtyPages.add(offset and pageMask)
            content.putShort(offset, value.asShort)
        }

        override fun outl(ea: Long, value: Long, ss: Int) {
            val offset = (ea - start).asInt
            dirtyPages.add(offset and pageMask)
            content.putInt(offset, value.asInt)
        }

        override fun outq(ea: Long, value: Long, ss: Int) {
            val offset = (ea - start).asInt
            dirtyPages.add(offset and pageMask)
            content.putLong(offset, value.asLong)
        }

        private fun fetchOrRead(ea: Long, ss: Int, size: Int, access: AccessAction): Long {
            // isReadable check when bus select a area

            return when (size) {
                QWORD.bytes -> inq(ea, ss)
                BYTES7.bytes -> inq(ea, ss) like BYTES7
                FWORD.bytes -> inq(ea, ss) like FWORD
                BYTES5.bytes -> inq(ea, ss) like BYTES5

                DWORD.bytes -> inl(ea, ss)
                TRIBYTE.bytes -> inl(ea, ss) like TRIBYTE

                WORD.bytes -> inw(ea, ss)
                BYTE.bytes -> inb(ea, ss)

                else -> throw MemoryAccessError(core.pc, ea, access, "Unsupported read size $size bytes")
            }
        }

        override fun fetch(ea: Long, ss: Int, size: Int): Long = fetchOrRead(ea, ss, size, FETCH)

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
        override fun read(ea: Long, ss: Int, size: Int) = fetchOrRead(ea, ss, size, LOAD)

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
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            // isWritable check when bus select a area

            when (size) {
                QWORD.bytes -> outq(ea, value, ss)
                FWORD.bytes -> {
                    outl(ea + 0, value[31..0], ss) // lo
                    outw(ea + 4, value[47..32], ss) // hi
                }
                DWORD.bytes -> outl(ea, value, ss)
                WORD.bytes -> outw(ea, value, ss)
                BYTE.bytes -> outb(ea, value, ss)

                else -> throw MemoryAccessError(core.pc, ea, LOAD, "Unsupported write size $size bytes")
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
        override fun load(ea: Long, size: Int, ss: Int, onError: HardwareErrorHandler?): ByteArray {
            val offset = (ea - start).toInt()
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
        override fun store(ea: Long, data: ByteArray, ss: Int, onError: HardwareErrorHandler?) {
            val offset = (ea - start).toInt()
            content.position(offset)
            content.put(data)
            val endEa = ea + data.size - 1
            (ea until endEa step pageSize.asLong).forEach {
                val pageOffset = (it - start).toInt()
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
        fun write(ea: Long, stream: InputStream) {
            val offset = (ea - start).toInt()
            val count = stream.readInto(content.obj, offset)
            val endEa = ea + count - 1
            (ea until endEa step pageSize.asLong).forEach {
                val pageOffset = (it - start).toInt()
                dirtyPages.add(pageOffset and pageMask)
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
            val blockSize = size / (cols * rows)
            val lines = Array(rows) { CharArray(cols) { fillClear } }
            dirtyPages.sorted().forEach {
                val start = it.toInt()
                val end = it + pageSize - 1
                for (k in start until end) {
                    val row = k / cols
                    val col = k % cols
                    lines[row][col] = fillDirty
                }
            }

            return lines
                    .mapIndexed { k, line -> "\t%08X: [ %s ]".format(k * blockSize * cols, String(line)) }
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
                storeValues(snapshotName to ctxt.storeBinary(snapshotName, content.obj))

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
            if (!ctxt.loadBinary(snapshot, snapshotName, content.obj))
                log.warning { "Can't load $snapshotName -> perhaps snapshot has old version!" }
        }

        /**
         * {RU}
         * Восстановление объекта к последнему десериализованному состоянию
         *
         * @param ctxt Контекст объекта-сериализатора
         * @param snapshot Отображение восстанавливаемых свойств объекта
         * {RU}
         *
         * {EN}
         * Restore state to last deserialized state
         *
         * @param ctxt Serializer context
         * @param snapshot map of object properties
         * {EN}
         */
        override fun restore(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
            if (access == ACCESS.R_W) {
                if (dirtyPages.isNotEmpty()) {
                    ctxt.restoreBinary(snapshot, snapshotName, content.obj, dirtyPages, pageSize)
                    dirtyPages.clear()
                }
            }
        }

        /**
         * {RU}
         * Настройка парсера аргументов командной строки. Для использования команд в консоли эмулятора.
         *
         * @param parent родительский парсер, к которому будут добавлены новые аргументы
         * @param useParent необходимость использования родительского парсера
         *
         * @return парсер аргументов
         * {RU}
         *
         * {EN}
         * Configuring parser for command line arguments. It is used to customize commands for this component/
         *
         * @param parent родительский парсер, к которому будут добавлены новые аргументы
         * @param useParent необходимость использования родительского парсера
         *
         * @return парсер аргументов
         * {EN}
         */
        override fun configure(parent: ArgumentParser?, useParent: Boolean): ArgumentParser? =
                super.configure(parent, useParent)?.apply {
                    subparser("read").apply {
                        variable<Int>("-a", "--address", required = true, help = "Port address")
                        variable<Int>("-s", "--segment", required = true, help = "Segment address")
                        variable<Int>("-z", "--size", required = true, help = "Size of data")
                    }
                    subparser("write").apply {
                        variable<Int>("-a", "--address", required = true, help = "Port address")
                        variable<Int>("-s", "--segment", required = true, help = "Segment address")
                        variable<Int>("-z", "--size", required = true, help = "Size of data")
                        variable<Int>("-v", "--value", required = true, help = "Value to write")
                    }
                }

        /**
         * {RU}
         * Обработка аргументов командной строки.
         * Для использования команд в консоли эмулятора.
         *
         * @param context Контекст интерактивной консоли
         *
         * @return Результат обработки команд (true/false)
         * {RU}
         *
         * {EN}
         * Processing command line arguments.
         *
         * @param context context of interactive command line interface
         *
         * @return result of processing
         * {EN}
         */
        override fun process(context: IInteractive.Context): Boolean {
            if (super.process(context))
                return true

            when (context.command()) {
                "read" -> {
                    val ea: Long = context["ea"] ?: 0
                    val ss: Int = context["ss"] ?: 0
                    val size: Int = context["size"] ?: 1
                    val value = read(ea, ss, size)
                    context.result = "read(${ea.hex}, $ss, $size) => $value"
                }
                "write" -> {
                    val ea: Long = context["ea"] ?: 0
                    val ss: Int = context["ss"] ?: 0
                    val size: Int = context["size"] ?: 1
                    val value: Long = context["value"]
                    write(ea, ss, size, value)
                    context.result = "write(${ea.hex}, $ss, $size, $value) => ok"
                }
            }

            context.pop()

            return true
        }

        /**
         * {RU}
         * Имя команды для текущего класса в интерактивной консоли эмулятора.
         * Для использования команд в консоли эмулятора.
         *
         * @return строковое имя команды
         * {RU}
         *
         * {EN}
         * Name of command for interactive emulator console.
         * {EN}
         */
        override fun command(): String? = port.name
    }

    class RegisterDefinitionError(message: String) : Exception(message)

    /**
     * {RU}
     * Аппаратный регистр для описание периферийных устройств и модулей.
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
            val address: Long,
            val datatype: Datatype,
            name: String,
            val default: Long = 0,
            val writable: Boolean = true,
            val readable: Boolean = true,
            val level: Level = Level.FINE
    ) : IFetchReadWrite, IValuable, ICoreUnit {
        val module = this@Module

        final override var data: Long = default

        override val name: String = "$name@${address.hex}[$datatype]"

        /**
         * {RU}
         * Строковое представление регистра.
         *
         * @return строковое представление регистра.
         * {RU}
         */
        override fun toString(): String = "$port->$name"

        override fun stringify(): String = "%s.%s %08X".format(fullname(), name, data)

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
            val pAddrSnapshot = loadValue<String>(snapshot, "pAddr").hexAsULong

            check(pNameSnapshot == port.name) {
                "port: ${port.name} != $pNameSnapshot. " +
                        "Try to update snapshot or check registers order in source code, it should be same with snapshot."
            }
            check(pAddrSnapshot == address) { "pAddr: %08X != %08X".format(address, pAddrSnapshot) }
            data = loadValue<String>(snapshot, "data").hexAsULong
        }

        fun Logger.read(level: Level) = log(level) { "[%08X] RD <- %s".format(core.cpu.pc, stringify()) }

        fun Logger.write(level: Level) = log(level) { "[%08X] WR -> %s".format(core.cpu.pc, stringify()) }

        final override fun fetch(ea: Long, ss: Int, size: Int) =
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
        override fun read(ea: Long, ss: Int, size: Int): Long {
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
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            data = value
            log.write(level)
        }

        final override fun beforeFetch(from: MasterPort, ea: Long): Boolean = false

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
        override fun beforeRead(from: MasterPort, ea: Long): Boolean = readable

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
        override fun beforeWrite(from: MasterPort, ea: Long, value: Long): Boolean = writable

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
            val address: Long,
            val datatype: Datatype,
            name: String,
            val default: Long = 0,
            private val writable: Boolean = true,
            private val readable: Boolean = true,
            val level: Level = Level.FINE
    ) : IReadWrite, IValuable, ICoreUnit {

        private inner class Cell(address: Long, name: String) : Register(port, address, BYTE, name) {
            override fun read(ea: Long, ss: Int, size: Int): Long = this@ByteAccessRegister.read(ea, ss, size)
            override fun write(ea: Long, ss: Int, size: Int, value: Long) = this@ByteAccessRegister.write(ea, ss, size, value)

            override fun beforeRead(from: MasterPort, ea: Long): Boolean = this@ByteAccessRegister.beforeRead(from, ea)
            override fun beforeWrite(from: MasterPort, ea: Long, value: Long): Boolean = this@ByteAccessRegister.beforeWrite(from, ea, value)
        }

        private val cells = Array(datatype.bytes) { Cell(address + it, "${name}_$it") }

        final override var data: Long = default

        override val name: String = "$name@${address.hex}"

        /**
         * {RU}
         * Строковое представление регистра.
         * @return строковое представление регистра.
         * {RU}
         */
        override fun toString(): String = "$port->$name"

        override fun stringify(): String = "%s.%s %08X".format(this@Module.name, name, data)

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
        override fun serialize(ctxt: GenericSerializer) = mapOf(
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
            val pAddrSnapshot = (snapshot["pAddr"] as String).hexAsULong

            check(pAddrSnapshot == address) { "pAddr: %08X != %08X".format(address, pAddrSnapshot) }

            data = (snapshot["data"] as String).hexAsULong
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
        override fun read(ea: Long, ss: Int, size: Int): Long {
            require(ea + size <= address + datatype.bytes) {
                "$name read out of range ea=${ea.hex8} size=$size address=${address.hex8} size=${datatype.bytes}"
            }
            return data[range(ea, size)]
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
        override fun write(ea: Long, ss: Int, size: Int, value: Long) {
            require(ea + size <= address + datatype.bytes) {
                "$name write out of range ea=${ea.hex8} size=$size address=${address.hex8} size=${datatype.bytes}"
            }
            data = data.insert(value, range(ea, size))
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
        private fun range(ea: Long, size: Int): IntRange {
            val off = offset(ea).asInt
            val lsb = off * 8
            val msb = (off + size) * 8 - 1
            return msb..lsb
        }

        fun offset(ea: Long) = ea - address

        override fun beforeRead(from: MasterPort, ea: Long) = readable
        override fun beforeWrite(from: MasterPort, ea: Long, value: Long) = writable

        override fun load(ea: Long, size: Int, ss: Int, onError: HardwareErrorHandler?) = throw IllegalAccessException("not implemented")
        override fun store(ea: Long, data: ByteArray, ss: Int, onError: HardwareErrorHandler?) = throw IllegalAccessException("not implemented")
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
                    val pAddr = (rInfo["pAddr"] as String).hexAsULong
                    log.warning { "Trying to load register at ${pAddr.hex8} for module $name..." }
                    val reg = registers.find { it.address == pAddr }
                    if (reg != null) {
                        log.finer { "Register ${reg.name} found in module $name -> loading" }
                        reg.deserialize(ctxt, rInfo)
                    } else {
                        log.warning { "Register omitted -> $rInfo" }
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

    open inner class ComplexRegister(port: SlavePort, vAddr: Long, name: String, default: Long = 0) :
            Register(port, vAddr, DWORD, name, default) {
        // Result from reading SET, CLR, INV should be undefined but for debugging return data value 0
        val BASE get() = this
        val CLR = object : Register(port, vAddr + 4, DWORD, name + "CLR", 0x0000_0000) {
            override fun read(ea: Long, ss: Int, size: Int): Long = 0
            override fun write(ea: Long, ss: Int, size: Int, value: Long) = BASE.write(ea, ss, size, BASE.data and value.inv())
        }
        val SET = object : Register(port, vAddr + 8, DWORD, name + "SET", 0x0000_0000) {
            override fun read(ea: Long, ss: Int, size: Int): Long = 0
            override fun write(ea: Long, ss: Int, size: Int, value: Long) = BASE.write(ea, ss, size, BASE.data or value)
        }
        val INV = object : Register(port, vAddr + 12, DWORD, name + "INV", 0x0000_0000) {
            override fun read(ea: Long, ss: Int, size: Int): Long = 0
            override fun write(ea: Long, ss: Int, size: Int, value: Long) = BASE.write(ea, ss, size, BASE.data xor value)
        }
    }

    /**
     * {RU}
     * Обработка аргументов командной строки.
     * Для использования команд в консоли эмулятора.
     *
     * @param context контекст интерактивной консоли
     *
     * @return результат обработки команд (true/false)
     * {RU}
     **/
    override fun process(context: IInteractive.Context): Boolean {
        if (super.process(context))
            return true

        if (context.isNotEmpty()) {
            if (areas.find { it.command() == context.command() }?.process(context) == true)
                return true

            if (registers.find { it.command() == context.command() }?.process(context) == true)
                return true
        }

        return false
    }

    /**
     * {RU}Базовый метод отключения периферийных устройств{RU}
     */
    override fun terminate() {
        log.config { "Terminate peripheral device %s".format(name) }
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