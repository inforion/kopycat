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
package ru.inforion.lab403.kopycat

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.INFO
import ru.inforion.lab403.common.logging.logStackTrace
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.AGenericDebugger
import ru.inforion.lab403.kopycat.cores.base.AGenericTracer
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.AInterrupt
import ru.inforion.lab403.kopycat.cores.base.common.ComponentTracer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_STOP
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SUCCESS
import ru.inforion.lab403.kopycat.gdbstub.GDBServer
import ru.inforion.lab403.kopycat.cores.base.enums.BreakpointType
import ru.inforion.lab403.kopycat.cores.base.enums.BreakpointType.*
import ru.inforion.lab403.kopycat.interfaces.IDebugger
import ru.inforion.lab403.kopycat.interfaces.ITracer
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import ru.inforion.lab403.kopycat.serializer.Serializer
import ru.inforion.lab403.kopycat.settings.snapshotFileExtension
import java.io.Closeable
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

/**
 * {EN}
 * Basic constructor of Kopycat emulator
 *
 * @param registry modules library registry
 * {EN}
 */
@Suppress("INAPPLICABLE_JVM_NAME")
class Kopycat constructor(var registry: ModuleLibraryRegistry?) : IDebugger, Closeable {

    companion object {
        @Transient
        val log = logger(INFO)

        /**
         * {EN}
         * Returns home directory of emulator. First try to get it from environment
         * variable [settings.envHomeVariableName].
         * If [settings.envHomeVariableName] wasn't set then use Java property user.dir.
         *
         * @since 0.3.21
         *
         * @return absolute path of Kopycat home directory
         * {EN}
         */
        fun getHomeDir(): String {
            val env = environment[settings.envHomeVariableName]

            val home = if (env != null) env else {
                log.info { "System environment variable '${settings.envHomeVariableName}' isn't set using parent of 'user.dir'" }
                javaWorkingDirectory
            }

            return home.toFile().absolutePath
        }

        /**
         * {EN}
         * Get true working directory of emulator, i.e. where emulator was run
         *
         * @since 0.3.21
         *
         * @return absolute path of working directory
         * {EN}
         */
        fun getWorkingDir(): String = "".toFile().absolutePath

        /**
         * {EN}
         * Get true working directory of emulator with [child] postfix
         *
         * @since 0.3.21
         *
         * @param child postfix to append to working directory path
         *
         * @return absolute path of working directory
         * {EN}
         */
        fun getWorkingDir(child: String?): String = when {
            child == null -> getWorkingDir()
            child.isAbsolute() -> child
            else -> "".toFile(child).absolutePath
        }
    }

    class InitializeKopycatException(description: String) : Exception(description)

    /**
     * {EN}Default folder to store and load snapshots{EN}
     */
    var snapshotsDir = getWorkingDir()
        private set(value) {
            log.info { "Change snapshots directory to '$value'" }
            field = value
        }

    var working: Boolean = true
        private set

    private var serializer: GenericSerializer? = null

    private var topModule: Module? = null
    private var gdbServer: GDBServer? = null

    val top get() = requireNotNull(topModule) { "Top module wasn't initialized" }
    val gdb get() = requireNotNull(gdbServer) { "GDB wasn't initialized" }

    val debugger get() = top.debugger
    val tracer get() = top.tracer
    val core get() = top.core

    fun setSnapshotsDirectory(path: String?) {
        snapshotsDir = getWorkingDir(path)
    }

    fun open(top: Module, gdb: GDBServer?, traceable: Boolean) {
        if (traceable) {
            // isDebuggerPresented can't be used before initializeAndResetAsTopInstance() called
            val debugger = top.findComponentByClass<AGenericDebugger>()
            if (debugger != null) {
                val lTracer = top.findComponentByClass<AGenericTracer>()
                if (lTracer == null) {
                    // parent for tracer and debugger must be the same otherwise
                    // during bus connection we will get error in validatePortConnection function
                    val whereDebuggerLive = debugger.parent
                    check(whereDebuggerLive is Module) { "Parent of the debugger module must be not null and must be 'Module' so we can add tracer to it" }
                    val tracer = ComponentTracer<AGenericCore>(whereDebuggerLive, "tracer")
                    whereDebuggerLive.buses.connect(tracer.ports.trace, debugger.ports.trace)
                    log.info { "Added default component tracer -> 'run' with predicate parameter can be used" }
                } else log.warning { "Can't add default component tracer to top module because it already has tracer: $lTracer" }
            } else log.warning { "Can't add default component tracer because debugger not presented!" }
        }

        if (!top.initializeAndResetAsTopInstance()) {
            throw InitializeKopycatException("Can't initialize target... Some boring error occurred perhaps...")
        }

        val boardInstanceName = top.name
        val boardModuleName = top.plugin
        val coreInstanceName = top.core.designator
        val coreModuleName = top.core.plugin

        log.info { "Board $boardInstanceName[$boardModuleName] with $coreInstanceName[$coreModuleName] is ready" }

        topModule = top

        if (gdb != null) {
            if (!top.isDebuggerPresent) {
                log.warning { "GDB server was created but debugger module not found!" }
                return
            }
            gdbServer = gdb.also { it.debuggerModule(top.debugger) }
        }
    }

    fun open(
        name: String,
        library: String,
        snapshot: String?,
        parameters: String?,
        gdb: GDBServer?,
        traceable: Boolean
    ) {
        val top = instantiate(name, library, parameters)
        open(top, gdb, traceable)
        if (snapshot != null) load(snapshot)
    }

    private fun instantiate(
        name: String,
        library: String,
        parameters: String?
    ) = try {
        registry!![library].instantiate(null, name, "top", parameters ?: "")
    } catch (error: InvocationTargetException) {
        val prms = if (parameters != null) " with $parameters" else ""
        log.severe { "Can't create module top[$name]$prms, see stack trace below if available..." }
        error.cause?.logStackTrace(log)
        throw InitializeKopycatException("Can't instantiate top module")
    }

    class Hook(val onStep: (step: ULong, core: AGenericCore) -> Boolean) : AGenericTracer(null, "hook") {
        var steps: ULong = 0u
            private set
        var startTime: Long = 0
            private set
        var stopTime: Long = 0
            private set

        override fun preExecute(core: AGenericCore) =
            if (!onStep(steps, core)) TRACER_STATUS_STOP else TRACER_STATUS_SUCCESS

        override fun postExecute(core: AGenericCore, status: Status) = TRACER_STATUS_SUCCESS.also { steps++ }

        override fun onStart(core: AGenericCore) {
            steps = 0u
            startTime = currentTimeMillis
            log.info { "Emulation started with hook..." }
        }

        override fun onStop() {
            stopTime = currentTimeMillis
            val deltaTimeSec = (stopTime - startTime) / 1000 + 1
            val mips = steps / deltaTimeSec
            log.info { "Emulation running on %,d sec., IPS = %,d".format(deltaTimeSec, mips.long) }
        }
    }

    fun <T : AGenericCore> hook(vararg newTracers: ITracer<T>): Boolean {
        if (top.isTracerPresent && top.tracer is ComponentTracer) {
            val tracer = top.tracer as ComponentTracer
            @Suppress("UNCHECKED_CAST")
            return tracer.addTracer(*newTracers as Array<out ITracer<AGenericCore>>)
        } else {
            log.warning { "Can't add hook because platform has no tracer or tracer is not Component tracer!" }
            return false
        }
    }

    fun hook(onStep: (step: ULong, core: AGenericCore) -> Boolean): Hook? {
        if (top.isTracerPresent && top.tracer is ComponentTracer) {
            val tracer = top.tracer as ComponentTracer
            val newTracer = Hook(onStep)

            if (!tracer.addTracer(newTracer))
                return null

            return newTracer
        } else {
            log.finest { "Can't add hook because platform has no tracer or tracer is not Component tracer!" }
            return null
        }
    }

    fun unhook(hook: AGenericTracer): Boolean {
        if (top.isTracerPresent && top.tracer is ComponentTracer) {
            val tracer = top.tracer as ComponentTracer
            return tracer.removeTracer(hook)
        } else {
            log.warning { "Can't remove hook because platform has no tracer or tracer is not Component tracer!" }
            return false
        }
    }

    fun reset() = top.reset()

    fun run(predicate: (step: ULong, core: AGenericCore) -> Boolean): ULong {
        val hook = hook(predicate)
        if (hook != null) {
            debugger.cont()
            unhook(hook)
            return hook.steps
        } else {
            var steps: ULong = 0u
            log.finest { "Component tracer or/and debugger not presented running using while-loop..." }
            while (predicate(steps, top.core)) {
                if (!top.core.step().resume)
                    break
                steps += 1u
            }
            return steps
        }
    }

    fun info() {
        println(core.stringify())
    }

    private val sdf = SimpleDateFormat("yyyyMMddHHmmss")

    private fun makeSnapshotAutoname(): String {
        val date = sdf.format(Date())
        val pc = core.cpu.pc.hex
        return "snapshot_$date@$pc.$snapshotFileExtension"
    }

    private fun getLastSnapshot(): File {
        val file = snapshotsDir.listdir { it.extension == snapshotFileExtension }.maxByOrNull { it.lastModified() }
        return requireNotNull(file) { "No snapshot file found in '$snapshotsDir'" }
    }

    private fun getFullSnapshotPath(path: String) = (snapshotsDir / path).addExtension(".zip").toFile()

    private fun checkNotRunning(action: String) {
        if (isTopModulePresented) require(!isRunning) { "Target should be stopped for $action" }
    }

    private fun checkTopPresented() = require(isTopModulePresented) { "No target top module presented in Kopycat" }

    fun save(path: String? = null, comment: String? = null): String {
        checkTopPresented()
        checkNotRunning("save")

        if (serializer == null)
            serializer = Serializer(top, false)

        val serializer = serializer!!

        val name = path ?: makeSnapshotAutoname()
        val file = getFullSnapshotPath(name)

        if (file.parentFile.mkdirs())
            log.info { "Creating directory ${file.parent}" }

        serializer.serialize(file, comment)

        return name
    }

    fun restore() {
        checkTopPresented()
        checkNotRunning("restore")
        val serializer = requireNotNull(serializer) { "Can't restore without Serializer being initialized already" }
        serializer.restore()
    }

    fun load(snapshot: ByteArray) {
        val file = snapshot.toTemporaryFile("snapshot_", sdf.format(Date()))
        serializer = Serializer(top, false).deserialize(file)
    }

    fun load(path: String? = null) {
        checkTopPresented()
        checkNotRunning("load")

        serializer ifNotNull {
            // if path is null restore target or load from specified snapshot path
            if (path != null) deserialize(getFullSnapshotPath(path)) else restore()
        } otherwise {
            // If serializer was null we definitely can't restore target
            serializer = Serializer(top, false).apply {
                val file = if (path != null) getFullSnapshotPath(path) else getLastSnapshot()
                deserialize(file)
            }
        }
    }

    override fun close() {
        gdbServer?.close()
        gdbServer = null
        topModule?.terminate()
        topModule = null
        serializer = null
        log.fine { "Target for line interface was closed!" }
    }

    fun exit() {
        close()
        working = false
    }

    /**
     * {RU}
     * Проверить было ли создано и готово устройство для эмуляции
     *
     * @return true - если устройство создано и может выполняться эмуляция
     * {RU}
     */
    val isTopModulePresented get(): Boolean = topModule != null

    /**
     * {RU}
     * Проверить был ли создан сериализатор
     *
     * @return true - если сериализатор присутствует в эмуляторе
     * {RU}
     */
    val isSerializerPresented get(): Boolean = serializer != null

    /**
     * {RU}
     * Проверить был ли создан GDB-сервер
     *
     * @return true - если GDB-сервер присутствует в эмуляторе
     * {RU}
     */
    val isGdbServerPresented get(): Boolean = gdbServer != null

    /**
     * {RU}
     * Проверить подключен или нет клиент к GDB-серверу
     *
     * @return true - если к GDB-серверу подключен клиент
     * {RU}
     */
    val gdbClientProcessing get(): Boolean = gdbServer?.hasClient ?: false

    // required setter/getter due to debugger may not be initialized
    override var isRunning
        get() = debugger.isRunning
        set(value) {
            debugger.isRunning = value
        }

    override fun step() = debugger.step()
    override fun cont() = throw UnsupportedOperationException(
        "cont() routine is synchronous! To run emulation in async use start(). " +
                "If you really want to run synchronous use debugger interface: kc.debugger.cont()"
    )

    override fun halt() = debugger.halt()

    override fun bptSet(bpType: BreakpointType, address: ULong, comment: String?) = debugger.bptSet(bpType, address)
    override fun bptClr(address: ULong) = debugger.bptClr(address)

    override fun dbgLoad(address: ULong, size: Int) = debugger.dbgLoad(address, size)
    override fun dbgStore(address: ULong, data: ByteArray) = debugger.dbgStore(address, data)

    override fun ident() = "kopycat"

    override fun registers() = debugger.registers()
    override fun sizes() = debugger.sizes()
    override fun regSize(index: Int) = debugger.regSize(index)

    override fun exception() = core.exception()

    override fun regRead(index: Int): ULong = core.reg(index)
    override fun regWrite(index: Int, value: ULong): Unit = core.reg(index, value)

    /**
     * {RU}
     * Запустить без блокировки выполнения программы эмуляцию
     *
     * @param action действие выполняемое после остановки эмулятора
     * {RU}
     */
    fun start(action: () -> Unit) = thread {
        debugger.cont()
        action()
    }

    /**
     * {RU}
     * Запустить без блокировки выполнения программы эмуляцию
     * {RU}
     */
    fun start() = thread { debugger.cont() }

    /**
     * {RU}
     * Проверить наличия исключительного состояния процессора 'cpu'
     *
     * @return true - если в процессоре произошло исключение
     * {RU}
     */
    fun hasException(): Boolean = exception() != null

    /**
     * {RU}
     * Установить точку останова в эмуляторе с помощью текстового режима доступа
     *
     * @param address адрес точки останова
     * @param access режим срабатывания точки останова в формате (x - execute, w - write, r - read)
     *
     * @return true - если точка останова была установлена
     * {RU}
     */
    fun bptSet(address: ULong, access: String): Boolean {
        var code = 0
        val tmp = access.lowercase()
        if ('r' in tmp) code = code or 0b001
        if ('w' in tmp) code = code or 0b010
        if ('x' in tmp) code = code or 0b100
        return bptSet(address, code)
    }

    /**
     * {RU}
     * Установить точку останова в эмуляторе с помощью числового режима доступа
     *
     * @param address адрес точки останова
     * @param access режим срабатывания точки останова в формате POSIX (100 - execute, 010 - write, 001 - read)
     *
     * @return true - если точка останова была установлена
     * {RU}
     */
    fun bptSet(address: ULong, access: Int): Boolean {
        var type: BreakpointType? = null
        if (access[0] == 1)
            type = READ
        if (access[1] == 1)
            type = if (type == READ) HARDWARE else WRITE
        if (access[2] == 1) {
            type = SOFTWARE
        }

        requireNotNull(type) { "Can't set breakpoint at address ${address.hex8} -> access should contain chars [rwx]" }

        return bptSet(type, address)
    }

    /**
     * {RU}
     * Загрузить массив байт размером [size] с заданного адреса [ss]:[address] с шины 'mem' процессора 'cpu'
     *
     * @param address адрес начала загрузки
     * @param size количество байт для загрузки
     * @param ss дополнительная часть адреса (segment selector)
     *
     * @return массив загруженных байт
     * {RU}
     */
    fun memLoad(address: ULong, size: Int, ss: Int): ByteArray = core.load(address, size, ss)

    /**
     * {RU}
     * Сохранить массив байт [data] в заданный адрес [ss]:[address] по шине 'mem' процессора 'cpu'
     *
     * @param address адрес начала загрузки
     * @param data массив байт для сохранения
     * @param ss дополнительная часть адреса (segment selector)
     * {RU}
     */
    fun memStore(address: ULong, data: ByteArray, ss: Int) = core.store(address, data, ss)

    /**
     * {RU}
     * Прочитать данные размером [size] с заданного адреса [ss]:[address] с шины 'mem' процессора 'cpu'
     * Может быть прочитано не более 8 байт
     * Чтение производится в целевого endian-устройства
     *
     * @param address адрес начала загрузки
     * @param size количество байт для загрузки
     * @param ss дополнительная часть адреса (segment selector)
     *
     * @return прочитанные данные
     * {RU}
     */
    fun memRead(address: ULong, size: Int, ss: Int = 0) = core.read(address, ss, size)

    /**
     * {RU}
     * Записать данные размером [size] в заданный адрес [ss]:[address] по шине 'mem' процессора 'cpu'
     * Может быть записано не более 8 байт
     * Запись производится в целевого endian-устройства
     *
     * @param address адрес начала загрузки
     * @param size количество байт для загрузки
     * @param value значение для записи
     * @param ss дополнительная часть адреса (segment selector)
     * {RU}
     */
    fun memWrite(address: ULong, size: Int, value: ULong, ss: Int = 0) = core.write(address, ss, size, value)

    /**
     * {RU}
     * Прочитать значение регистра процессора 'cpu' с именем [name]
     *
     * @param name имея регистра для чтения
     *
     * @return прочитанное значение регистра
     * {RU}
     */
    fun regRead(name: String): ULong = TODO("Not implemented yet")

    /**
     * {RU}
     * Записать значение регистра процессора 'cpu' с именем [name]
     *
     * @param name имея регистра для записи
     * @param value значение регистра для записи
     * {RU}
     */
    fun regWrite(name: String, value: ULong): Unit = TODO("Not implemented yet")

    /**
     * {RU}
     * Прочитать значение регистра счетчика команд 'pc' процессора 'cpu'
     *
     * @return прочитанное значение регистра 'pc'
     * {RU}
     */
    fun pcRead(): ULong = core.pc

    /**
     * {RU}
     * Записать значение регистра счетчика команд 'pc' процессора 'cpu'
     *
     * @param value значение регистра 'pc' для записи
     * {RU}
     */
    fun pcWrite(value: ULong) {
        core.pc = value
    }

    /**
     * {RU}
     * Вызывает исключение процессора с заданным именем для заданного адреса [pc], адреса обработки [vAddr]
     * и типом доступа [access].
     * {RU}
     */
    fun throwException(name: String, where: ULong, vAddr: ULong, access: String) {
        val LorS = AccessAction.valueOf(access)
        val exception = core.cop.createException(name, where, vAddr, LorS)
        core.cpu.exception = exception
    }

    /**
     * {RU}
     * Вызывает прерывание процессора с заданым номером [irq], вектором [vector] и приоритетом [priority]
     * Первоначальная обработка прервывания выполняется копроцессором (системным контроллером прерываний)
     * в функции [processInterrupts]
     *
     * Примечание: функция создает новый анонимный объект прерывания, чтобы поставить его в обработку
     *             копроцессору, при нормальной работе эмулятора формирование прерываний должно
     *             осуществляться с помощью модуля [APIC] программируемого контроллера прерываний,
     *             который должен быть встроен в систему.
     *
     * Примечание: использование каждого из параметров определяется реализацией копроцессора
     *             и может не использоваться или быть использовано не по назначению!
     *
     * @param irq номер прерывания процессора/копроцессора
     * @param vector адрес или смещение обработчика прерывания
     * @param priority приоритет прерывания
     * @param cause дополнительное поле (используется только в MIPS)
     * @return true если прерывание было добавлено в обработку
     * {RU}
     */
    fun requestInterrupt(irq: Int, vector: Int = 0, priority: Int = 0, cause: Int = 0): Boolean {
        val interrupt = object : AInterrupt(irq, "STUB", "INT", true) {
            override val cop = core.cop
            override val vector = vector
            override val priority = priority
            override val cause = cause
            override fun onInterrupt() {
                super.onInterrupt()
                log.warning { "$this was taken!" }
            }
        }
        return core.cop.interrupt(interrupt, true)
    }

    /**
     * {RU}Время прошедшее в микросекундах от начала выполнения программы{RU}
     */
    fun timestamp(): ULong = core.clock.time()

    /**
     * {RU}Общее количество выполненных инструкций от старта эмулятора{RU}
     */
    fun ticks(): ULong = core.clock.totalTicks

    fun translate(ea: ULong, ss: Int, size: Int, LorS: AccessAction) = core.mmu!!.translate(ea, ss, size, LorS)

    fun translate(ea: ULong, ss: Int, size: Int, LorS: String): ULong {
        val action = first<AccessAction> { it.name == LorS }
        return translate(ea, ss, size, action)
    }

    /**
     * {RU}Выводит в логгер информацию о всех загруженных модулях{RU}
     */
    fun printModulesRegistryInfo(top: Boolean) {
        val modules = if (top) registry?.getAvailableTopModules() else registry?.getAvailableAllModules()
        log.info { modules?.joinToString("\n") ?: "none" }
    }

    /**
     * {RU}Выводит в логгер информацию о соединениях между модулями{RU}
     */
    fun printModulesConnectionsInfo() = modules().forEach { it.buses.logMemoryMap() }

    /**
     * {RU}Выводит в логгер информацию о предупреждениях портов{RU}
     */
    fun printModulesPortsWarnings() = modules().forEach { it.ports.hasWarnings(true) }

    /**
     * {RU}
     * Найти компонент от модуля верхнего уровня с заданным именем [name] по всей иерархии модулей
     *
     * @return null - если модуль не был найден, иначе найденный модуль
     * {RU}
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Module> find(name: String): T? = top.findComponentByInstanceName<Module>(name) as T?

    /**
     * {RU}
     * Получить первый попавшийся с заданным именем [name] по всей иерархии модулей
     * Если не нашелся ни один модуль бросить исключение.
     *
     * @return найденный модуль
     * {RU}
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Module> first(name: String): T = top.firstComponentByInstanceName<Module>(name) as T

    /**
     * {RU}Получить коллекцию всех модулей, которые в настоящий момент используются в эмуляторе{RU}
     */
    fun modules(): MutableCollection<Module> = top.getComponentsByClass()
}