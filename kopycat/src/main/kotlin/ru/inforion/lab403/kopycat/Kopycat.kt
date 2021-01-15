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
package ru.inforion.lab403.kopycat

import net.sourceforge.argparse4j.inf.ArgumentParser
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.CONFIG
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
import ru.inforion.lab403.kopycat.gdbstub.GDB_BPT
import ru.inforion.lab403.kopycat.interfaces.IDebugger
import ru.inforion.lab403.kopycat.interfaces.IInteractive
import ru.inforion.lab403.kopycat.interfaces.ITracer
import ru.inforion.lab403.kopycat.library.ModuleLibraryRegistry
import ru.inforion.lab403.kopycat.serializer.Serializer
import java.lang.reflect.InvocationTargetException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

@Suppress("unused")
/**
 * {EN}
 * Basic constructor of Kopycat emulator
 *
 * @param registry modules library registry
 * {EN}
 */
class Kopycat constructor(var registry: ModuleLibraryRegistry?): IInteractive, IDebugger {

    companion object {
        @Transient val log = logger(CONFIG)

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
            val env = System.getenv(settings.envHomeVariableName)

            val home = if (env != null) env else {
                log.info { "System environment variable '${settings.envHomeVariableName}' isn't set using parent of 'user.dir'" }
                System.getProperty("user.dir")
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

    enum class STATE { WORKING, DISABLED, AWAIT, MALFORMED }

    /**
     * {EN}Default folder to store and load snapshots{EN}
     */
    var snapshotsDir = getWorkingDir()
        private set (value) {
            log.info { "Change snapshots directory to '$value'" }
            field = value
        }

    var working: Boolean = true
        private set

    private var topModule: Module? = null
    private var serializer: GenericSerializer? = null
    private var gdbServer: GDBServer? = null

    val top get() = topModule!!
    val gdb get() = gdbServer!!
    val debugger get() = top.debugger
    val tracer get() = top.tracer
    val core get() = top.core

    fun setSnapshotsDirectory(path: String?) {
        snapshotsDir = getWorkingDir(path)
    }

    fun open(top: Module, traceable: Boolean, gdb: GDBServer?) {
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
        return
    }

    fun open(
            name: String,
            library: String,
            snapshot: String?,
            parameters: String?,
            traceable: Boolean,
            gdb: GDBServer?
    ) {
        val top = instantiate(name, library, parameters)

        open(top, traceable, gdb)

        if (snapshot != null) {
            val path = snapshotsDir / snapshot
            serializer = Serializer(topModule!!, false).deserialize(path)
        }
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
        error.cause?.printStackTrace()
        throw InitializeKopycatException("Can't instantiate top module")
    }

    class Hook(val onStep: (step: Long, core: AGenericCore) -> Boolean) : AGenericTracer(null, "hook") {
        var steps: Long = 0
            private set
        var startTime: Long = 0
            private set
        var stopTime: Long = 0
            private set

        override fun preExecute(core: AGenericCore) =
                if (!onStep(steps, core)) TRACER_STATUS_STOP else TRACER_STATUS_SUCCESS

        override fun postExecute(core: AGenericCore, status: Status) = TRACER_STATUS_SUCCESS.also { steps += 1 }

        override fun onStart(core: AGenericCore) {
            steps = 0
            startTime = System.currentTimeMillis()
            log.info { "Emulation started with hook..." }
        }

        override fun onStop() {
            stopTime = System.currentTimeMillis()
            val deltaTimeSec = (stopTime - startTime) / 1000 + 1
            val mips = steps / deltaTimeSec
            log.info { "Emulation running on %,d sec., IPS = %,d".format(deltaTimeSec, mips) }
        }
    }

    fun <T: AGenericCore> hook(vararg newTracers: ITracer<T>): Boolean {
        if (top.isTracerPresent && top.tracer is ComponentTracer) {
            val tracer = top.tracer as ComponentTracer
            @Suppress("UNCHECKED_CAST")
            return tracer.addTracer(*newTracers as Array<out ITracer<AGenericCore>>)
        } else {
            log.warning { "Can't add hook because platform has no tracer or tracer is not Component tracer!" }
            return false
        }
    }

    fun hook(onStep: (step: Long, core: AGenericCore) -> Boolean): Hook? {
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

    fun profileOn(): Hook? {
//        var callCount = 0
//        var memoryAccessCount = 0

        val profiler = hook { step, core ->
//            val insn = core.cpu.insn
//            if (insn.isCall)
//                callCount += 1
//
//            if (insn.any { it is Memory || it is Displacement })
//                memoryAccessCount += 1

            return@hook true
        }

        return profiler
    }

    fun profileOff(hook: Hook?) {
        if (hook != null)
            unhook(hook)
    }

    fun reset() = topModule!!.reset()

    fun run(predicate: (step: Long, core: AGenericCore) -> Boolean): Long {
        val hook = hook(predicate)
        if (hook != null) {
            debugger.cont()
            unhook(hook)
            return hook.steps
        } else {
            var steps: Long = 0
            log.finest { "Component tracer or/and debugger not presented running using while-loop..." }
            while (predicate(steps, top.core)) {
                if (!top.core.step().resume)
                    break
                steps += 1
            }
            return steps
        }
    }

    fun info() {
        println(core.stringify())
    }

    private fun makeSnapshotAutoname(): String {
        val date = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val pc = core.cpu.pc.hex

        return "snapshot_$date@$pc.zip"
    }

    // Specially for Jep (Java Embedded Python) - overloaded method with no arguments
    fun save(): Boolean = save(null)

    fun save(snapshotPath: String?, comment: String? = null): Boolean {
        if (!isTopModulePresented) {
            log.warning { "No target specified. Current targetCommand will not be executed" }
            return false
        }

        val wasRunning = isRunning

        if (serializer == null)
            serializer = Serializer(top, false)

        if (wasRunning) {
            log.config { "Target running, waiting while target stopped..." }
            halt()
        }

        log.config { "Making emulator snapshot, please wait..." }

        val name = snapshotPath ?: makeSnapshotAutoname()
        val path = (snapshotsDir / name).addExtension(".zip")

        if (serializer!!.serialize(path, comment, core.pc))
            log.config { "Out file is $path" }

        if (wasRunning) {
            log.config { "Continue target execution..." }
            start()
        }

        return true
    }

    // Load snapshot from last loaded file
    fun restore(): Boolean = load(null)

    fun load(): Boolean {
        val filename = snapshotsDir.listdir { it.startsWith("snapshot") }.maxOrNull()
        if (filename == null) {
            log.severe { "No snapshot file found in '$snapshotsDir'" }
            return false
        }
        return load(filename.path)
    }

    fun load(snapshot: ByteArray): Boolean {
        serializer = Serializer(topModule!!, false).deserialize(snapshot)
        return true
    }

    fun load(snapshotPath: String?): Boolean {
        if (!isTopModulePresented) {
            log.warning { "No target specified. Current targetCommand will not be executed" }
            return false
        }

        val wasRunning = isRunning

        if (serializer == null)
            serializer = Serializer(top, false)

        val serializer = serializer!!

        if (wasRunning) {
            log.config { "Target running, waiting while target stopped..." }
            halt()
        }

        if (snapshotPath == null) {
            log.config { "Target restoration started, please wait..." }
            serializer.restore()
        } else {
            val path = snapshotsDir / snapshotPath
            log.config { "Target deserialization from '$path', please wait..." }
            serializer.deserialize(path)
        }

        log.config { "Target has been restored to deserialization state" }
        if (wasRunning) start()

        return true
    }

    @Deprecated("Method returns incorrect information about state and will be removed in 0.3.40")
    fun state() = when {
        !isGdbServerPresented && !isTopModulePresented -> STATE.DISABLED
        isGdbServerPresented && isTopModulePresented -> if (gdb.clientProcessing) STATE.WORKING else STATE.AWAIT
        else -> STATE.MALFORMED
    }

    fun close() {
        gdbServer?.close()
        gdbServer = null
        topModule?.terminate()
        topModule = null
        serializer = null
        System.gc()
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
    val gdbClientProcessing get(): Boolean = gdbServer?.clientProcessing ?: false

    // required setter/getter due to debugger may not be initialized
    override var isRunning
        get() = debugger.isRunning
        set(value) {
            debugger.isRunning = value
        }

    override fun step() = debugger.step()
    override fun cont() = throw UnsupportedOperationException(
            "cont() routine is synchronous! To run emulation in async use start(). " +
                    "If you really want to run synchronous use debugger interface: kc.debugger.cont()")
    override fun halt() = debugger.halt()

    override fun bptSet(bpType: GDB_BPT, address: Long, comment: String?) = debugger.bptSet(bpType, address)
    override fun bptClr(address: Long) = debugger.bptClr(address)

    override fun dbgLoad(address: Long, size: Int) = debugger.dbgLoad(address, size)
    override fun dbgStore(address: Long, data: ByteArray) = debugger.dbgStore(address, data)

    override fun ident() = "kopycat"

    override fun registers() = debugger.registers()

    override fun exception() = core.exception()

    override fun regRead(index: Int): Long = core.reg(index)
    override fun regWrite(index: Int, value: Long): Unit = core.reg(index, value)

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
    fun bptSet(address: Long, access: String): Boolean {
        var code = 0
        val tmp = access.toLowerCase()
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
    fun bptSet(address: Long, access: Int): Boolean {
        var type: GDB_BPT? = null
        if (access[0] == 1)
            type = GDB_BPT.READ
        if (access[1] == 1)
            type = if (type == GDB_BPT.READ) GDB_BPT.ACCESS else GDB_BPT.WRITE
        if (access[2] == 1) {
            require(type == GDB_BPT.READ || type == GDB_BPT.WRITE || type == GDB_BPT.ACCESS) {
                "Can't set breakpoint at address ${address.hex8} -> access should be [r|w] or [x]"
            }
            type = GDB_BPT.READ
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
    fun memLoad(address: Long, size: Int, ss: Int): ByteArray = core.load(address, size, ss)

    /**
     * {RU}
     * Сохранить массив байт [data] в заданный адрес [ss]:[address] по шине 'mem' процессора 'cpu'
     *
     * @param address адрес начала загрузки
     * @param data массив байт для сохранения
     * @param ss дополнительная часть адреса (segment selector)
     * {RU}
     */
    fun memStore(address: Long, data: ByteArray, ss: Int) = core.store(address, data, ss)

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
    fun memRead(address: Long, size: Int, ss: Int = 0) = core.read(address, ss, size)

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
    fun memWrite(address: Long, size: Int, value: Long, ss: Int = 0) = core.write(address, ss, size, value)

    /**
     * {RU}
     * Прочитать значение регистра процессора 'cpu' с именем [name]
     *
     * @param name имея регистра для чтения
     *
     * @return прочитанное значение регистра
     * {RU}
     */
    fun regRead(name: String): Long = TODO("Not implemented yet")

    /**
     * {RU}
     * Записать значение регистра процессора 'cpu' с именем [name]
     *
     * @param name имея регистра для записи
     * @param value значение регистра для записи
     * {RU}
     */
    fun regWrite(name: String, value: Long): Unit = TODO("Not implemented yet")

    /**
     * {RU}
     * Прочитать значение регистра счетчика команд 'pc' процессора 'cpu'
     *
     * @return прочитанное значение регистра 'pc'
     * {RU}
     */
    fun pcRead(): Long = core.pc

    /**
     * {RU}
     * Записать значение регистра счетчика команд 'pc' процессора 'cpu'
     *
     * @param value значение регистра 'pc' для записи
     * {RU}
     */
    fun pcWrite(value: Long) {
        core.pc = value
    }

    /**
     * {RU}
     * Вызывает исключение процессора с заданным именем для заданного адреса [pc], адреса обработки [vAddr]
     * и типом доступа [access].
     * {RU}
     */
    fun throwException(name: String, where: Long, vAddr: Long, access: String) {
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
    fun timestamp(): Long = core.clock.time()

    /**
     * {RU}Общее количество выполненных инструкций от старта эмулятора{RU}
     */
    fun ticks(): Long = core.clock.totalTicks

    fun translate(ea: Long, ss: Int, size: Int, LorS: AccessAction) = core.mmu!!.translate(ea, ss, size, LorS)

    fun translate(ea: Long, ss: Int, size: Int, LorS: String): Long {
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
    fun <T : Module> find(name: String): T? = topModule!!.findComponentByInstanceName<Module>(name) as T?

    /**
     * {RU}
     * Получить первый попавшийся с заданным именем [name] по всей иерархии модулей
     * Если не нашелся ни один модуль бросить исключение.
     *
     * @return найденный модуль
     * {RU}
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Module> first(name: String): T = topModule!!.firstComponentByInstanceName<Module>(name) as T

    /**
     * {RU}Получить коллекцию всех модулей, которые в настоящий момент используются в эмуляторе{RU}
     */
    fun modules(): MutableCollection<Module> = topModule!!.getComponentsByClass<Module>().also { it.add(topModule!!) }

    override fun describe(): String = "Kopycat utils for emulator"

    override fun command(): String = "Kopycat"

    override fun configure(parent: ArgumentParser?, useParent: Boolean): ArgumentParser? =
            super.configure(parent, useParent)?.apply {
                subparser("save").apply {
                    variable<String>("path", help = "Path to snapshot file to save it")
                }
                subparser("load").apply {
                    variable<String>("path", help = "Path to snapshot file from where to load snapshot")
                }
                subparser("step")
                subparser("reset")
                subparser("restore")
                topModule?.configure(parent, true)
            }

    override fun process(context: IInteractive.Context): Boolean {
        if (super.process(context))
            return true
        val moduleName = context.command()
        if (moduleName != command())
            return false
        context.pop()
        return when (context.command()) {
            "load" -> {
                val path = context.getString("path")
                load(path)
            }
            "save" -> {
                val path = context.getString("path")
                save(path)
            }
            "step" -> {
                val debugger: AGenericDebugger? = topModule?.debugger
                if (debugger != null) {
                    debugger.step()
                } else {
                    context.result = "Debugger is not initialized. No action will follow"
                    false
                }
            }
            "reset" -> {
                reset()
                true
            }
            "restore" -> {
                serializer!!.restore()
                true
            }
            else -> false
        }
    }
}