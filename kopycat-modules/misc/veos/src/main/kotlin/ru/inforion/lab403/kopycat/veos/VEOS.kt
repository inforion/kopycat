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
package ru.inforion.lab403.kopycat.veos

import gnu.trove.map.hash.THashMap
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINEST
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.proposal.lazyTransient
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ABI
import ru.inforion.lab403.kopycat.cores.base.abstracts.ATracer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.common.ModuleBuses
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SKIP
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_STOP
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SUCCESS
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.modules.BUS32
import ru.inforion.lab403.kopycat.modules.memory.VirtualMemory
import ru.inforion.lab403.kopycat.veos.api.abstracts.API
import ru.inforion.lab403.kopycat.veos.api.interfaces.APIResult
import ru.inforion.lab403.kopycat.veos.deferred.Definition
import ru.inforion.lab403.kopycat.veos.filesystems.impl.FileSystem
import ru.inforion.lab403.kopycat.veos.filesystems.impl.IOSystem
import ru.inforion.lab403.kopycat.veos.filesystems.impl.SocketSystem
import ru.inforion.lab403.kopycat.veos.kernel.*
import ru.inforion.lab403.kopycat.veos.loader.ALoader
import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


/**
 * {EN}
 * Virtual Emulation Operating System (VEOS)
 * Base class for OS emulation in the purpose of running standalone binaries
 * {EN}
 */
abstract class VEOS<C : AGenericCore> constructor(
        parent: Module,
        name: String,
        val bus: Long = BUS32
): ATracer<C>(parent, name, bus), IAutoSerializable {

    companion object {
        @Transient val log = logger(FINEST)
    }

    open inner class Buses : ModuleBuses(this) {
        val mem = Bus("mem", bus)
    }

    @DontAutoSerialize
    override val buses = Buses()

    val conf = Configuration()

    val sys = System(this)

    lateinit var abi: ABI<C>
    abstract val loader: ALoader  // TODO: make non abstract
    val ioSystem = IOSystem(sys)
    val filesystem = FileSystem(sys)
    val network = SocketSystem(sys)

    private val apiList = ArrayList<API>()

    fun addApi(api: API) {
        if (apiList.find { it::class == api::class } == null)
            apiList.add(api)
    }

    fun addApi(vararg apis: API) = apis.forEach { addApi(it) }

    fun initApi(argc: Long, argv: Long, envp: Long) = apiList.forEach { it.init(argc, argv, envp) }

    // TODO: somehow move it to Process
    private fun setHandler(symbol: Symbol) {
        val api = apiList.find { api -> symbol.name in api.functions }
        if (api != null) {
            val func = api.functions.getValue(symbol.name)
            val addr = func.address ?: symbol.address

            currentProcess.setHandler(addr, Handler(symbol.name, addr, func))

        } else if (symbol.isExternal && symbol.isFunction && !currentProcess.hasHandler(symbol.address)) {
            currentProcess.setHandler(symbol.address,
                    sys.undefinedReferenceHandler(symbol.name, symbol.address)
            )
            log.severe { "Undefined reference: ${symbol.name}" }
        }
        // TODO: find api variables and print is it resolved or not
    }

    // TODO: somehow move it to Process
    fun setHandlers(symbols: Collection<Symbol>) = symbols.forEach { setHandler(it) }

    override fun initialize(): Boolean {
        if (!super.initialize())
            return false

        ports.mem.connect(buses.mem)

        @Suppress("UNCHECKED_CAST")
        abi = core.abi() as ABI<C>
        return true
    }

    /**
     * Early access memory (reserved for system use)
     * Should not be reset directly, use [systemDataAllocatorReset] instead
     */
    var systemData = Allocator(sys, conf.systemDataStart, conf.systemDataEnd - 1)

    // TODO: move it to System
    fun load(filename: String, argv: Array<String>) {
        currentMemory.reset()
        currentMemory.allocate("system", systemData.startAddr.asLong, systemData.size.asInt)

        loader.load(filesystem.absolutePath(filename))

        // Now we can restore state
        currentProcess.restoreState()

        // And load arguments
        // TODO: move outside loader?
        // TODO: make possible to work without restoration of state
        loader.loadArguments(argv)
    }

    // Use it only during setup
    // Do not use it due runtime?
    // It's like fork() + exec()
    open fun initProcess(filename: String, vararg args: String) {

        // We use name without extension as identifier
        val shortFilename = File(filename).nameWithoutExtension

        // Save old one if exists (and yield them)
        val savedProcess = processes.firstOrNull()?.also { it.saveState() }

        // Create new process
        val newProcess = createProcess(shortFilename)

        // Replace but not restore - context isn't initialized yet
        currentProcess = newProcess

        // Reconnect memory
        // Since now we can use memory operations
        reconnect { connectCurrentMemory() }

        // Call loader to init memory
        load(filename, arrayOf(filename, *args))

        // If we call this function more than once
        if (savedProcess != null) {
            currentProcess.saveState()
            currentProcess = savedProcess
            currentProcess.restoreState()
        }
    }

    // Use it only during setup
    // Do not use it due runtime?
    fun loadLibrary(filename: String) = loader.loadLibrary(filesystem.absolutePath(filename))

    private val processes = mutableListOf<Process>()

    var currentProcess: Process
        get() = processes.first()
        set(value) {
            processes.remove(value)
            processes.add(0, value)
        }

    val currentMemory get() = currentProcess.memory

    private val scheduling = ReentrantLock()

    private fun exitProcess(process: Process) {
        process.exit()
        if (process.memory.isUnused)
            components.remove(process.memory.name)
    }

    private fun resetProcesses() = processes.onEach { exitProcess(it) }.clear()

    override fun reset() {
        loader.reset()
        ioSystem.reset()
        filesystem.reset()
        network.reset()

        resetProcesses()

        systemData.reset()

        processIds.reset()
    }

    // TODO: refactor this
    var timestamp: Long = 0
        private set

    private val idleProcess by lazyTransient {
        val memory = VirtualMemory(this@VEOS, "idleMemory", 0, 0)

        object : Process(sys, -1, memory) {
            override val processType = "idle"

            init {
                initContext(sys.idleProcessAddress, 0L, sys.idleProcessAddress)
            }
        }
    }

    private fun schedule() {
        check(scheduling.isLocked) { "Scheduler was called without scheduling lock is locked!" }

        // Save current process
        currentProcess.saveState()

        // Save id
        val previousId = currentProcess.id

        // Put it at the end of queue
        processes.add(processes.removeFirst())

        // Get rid of exited processes
        // Offed: exited thread detection problem
//        removeExitedProcesses()

        // Looking for next one
        val readyProcess = processes.find { it.isReady }

        val nextProcess = when {
            // At least 1 process is ready
            readyProcess != null -> {
                readyProcess
            }
            // Only blocked for while processes -> waiting with blocked queue
            else -> {
                check(lostUnblockedProcess.isEmpty()) {
                    "Process unblock before scheduling started but scheduler didn't see them: ${lostUnblockedProcess.joinToString(", ")}"
                }

                // Do we need them now?
                // This was necessary for legacy code where calls to currentProcess were used
                // while blocking a thread with a scheduler. Now it is probably no longer necessary,
                // but it should be checked
                currentProcess = idleProcess // To make possible non-blocking API

                scheduling.unlock()

                val unblockedProcess = lostUnblockedProcess.take()

                scheduling.lock()

                check(unblockedProcess.isReady) { "Expected ${Process.State.Ready} state for $unblockedProcess" }

                processes.remove(idleProcess)

                unblockedProcess
            }
        }

        // Exit from blocking -> clear queue
        lostUnblockedProcess.clear()

        // Remove new process from queue
        processes.remove(nextProcess)

        // Set them as current
        currentProcess = nextProcess

        // Restore process
        currentProcess.restoreState()

        reconnect { reconnectMemoryIfRequired(previousId) }

        currentProcess.deferred()
    }

    /**
     * The processes that unblocked after [schedule] started
    */
    val lostUnblockedProcess = LinkedBlockingQueue<Process>()

    // note that withLock may occasionally eat the exception
    fun <T> block(actions: Definition<T>.() -> Unit) = scheduling.withLock {
        currentProcess.block()

        // better do not move it inside job if you wanna to refactor something
        val deferred = Definition(actions).build()

        currentProcess.job {
            deferred.apply {
                runCatching { execute() }
                        .onSuccess { succeed(it) }
                        .onFailure { failed(it) }
            }
        }
    }

    // note that withLock may occasionally eat the exception
    fun unblock(process: Process) = scheduling.withLock {
        process.unblock()
        if (process.isReady)
            lostUnblockedProcess.add(process)
    }

    enum class State { Started, Running, Exception, Exit }

    var state: State = State.Started
        private set

    final override fun preExecute(core: C): Long {

        while (true) {
            val handler = currentProcess.getHandler(abi.programCounterValue)
                    ?: sys.getHandler(abi.programCounterValue)
                    ?: break

            check(!currentProcess.hasDelayedJob) { "Something gone wrong" }

            // TODO: merge with APIFunction
            val args = abi.getCArgs(handler.func.args)

            val result = try {
                handler.func.exec(handler.name, *args.toLongArray())
            } catch (error: MemoryAccessError) {
                return scheduling.withLock {
                    // TODO: Wrong implementation
                    if (processes.any { it.isReady || it.isBlocked }) {
                        log.warning { "[0x${abi.programCounterValue.hex8}] $currentProcess -> Segmentation fault" }
                        currentProcess.segfault() /** REVIEW: as [exitProcess] */
                        schedule()
                        TRACER_STATUS_SKIP
                    } else {
                        log.finest { "[0x${abi.programCounterValue.hex8}] Application exited (Segmentation fault)" }
                        state = State.Exception
                        log.severe { "$this -> $error" }
                        error.printStackTrace()
                        TRACER_STATUS_STOP
                    }
                }
            } catch (error: Throwable) {
                state = State.Exception
                log.severe { "[0x${abi.returnAddressValue.hex8}] $this -> $error" }
                error.printStackTrace()
                return TRACER_STATUS_STOP
            }

            state = State.Running

            return scheduling.withLock {
                when (result) {
                    // TODO: not to use separate values for void and result?
                    is APIResult.Value -> {
                        check(!currentProcess.hasDelayedJob) { "Use block only with void: ${handler.name}" }
                        if (currentProcess.isBlocked) {
                            currentProcess.context.setReturnValue(result.data, result.type)
                            currentProcess.context.ret() // REVIEW: X86Abi
                            schedule()
                        } else {
                            abi.setReturnValue(result.data, result.type)
                            abi.ret() // REVIEW: X86Abi
                        }
                    }

                    is APIResult.Void -> {
                        if (currentProcess.isBlocked) {
                            currentProcess.context.ret() // REVIEW: X86Abi
                            schedule()
                        } else {
                            currentProcess.deferred()
                            abi.ret() // REVIEW: X86Abi
                        }
                    }

                    is APIResult.Redirect -> {
                        abi.programCounterValue = result.address
                    }

                    is APIResult.ThreadExited -> {
                        log.warning { "$currentProcess -> exited" }
                        exitProcess(currentProcess)
                        schedule()
                    }

                    is APIResult.Terminate -> {
                        // TODO: Wrong implementation
                        if (processes.any { it.isReady || it.isBlocked }) {
                            log.warning { "$currentProcess -> exited" }
                            exitProcess(currentProcess)
                            schedule()
                        } else {
                            log.finest { "Application exited" }
                            state = State.Exit
                            return@withLock TRACER_STATUS_STOP
                        }
                    }

                    else -> error("Wrong state: $result")
                }
                return@withLock TRACER_STATUS_SKIP
            }

        }
        return TRACER_STATUS_SUCCESS
    }

    // TODO: maybe merge with preExecute?
    final override fun postExecute(core: C, status: Status): Long {
        if (status != Status.CORE_EXECUTED) {
            // may be this check is wrong but I think VEOS must have always core in 'clean' state
            log.severe { "CPU core in faulty state -> should not happen for VEOS: ${core.cpu.exception}" }
            return TRACER_STATUS_STOP
        }

        val currentTime = core.clock.time()
        if (currentTime - timestamp > conf.processSwitchPeriod) {
            timestamp = currentTime
            scheduling.withLock { schedule() }
        }
        return TRACER_STATUS_SUCCESS
    }

    // This function does not initializes memory

    // Can't be used in api
    protected abstract fun newProcess(memory: VirtualMemory): Process

    // Can't be used in api
    fun createProcess(name: String) = newProcess(createMemory(name)).also { processes.add(it) }

    fun copyProcess(postfix: String) = newProcess(copyMemory(postfix)).cloneOf(currentProcess).also { processes.add(it) }

    // TODO: execve() currently not supported in threads and may cause wierd crashes
    fun createThread() = newProcess(currentMemory).aliasOf(currentProcess).also { currentMemory.share(); processes.add(it) }

    private var copyId = 0

    private fun createMemory(name: String) = VirtualMemory(this, name, 0, bus, abi.bigEndian)

    private fun copyMemory(postfix: String): VirtualMemory {
        val memory = VirtualMemory(this, "${currentMemory.name}_$postfix${copyId++}", currentMemory.start, currentMemory.size, currentMemory.bigEndian)

        currentMemory.areasAsImmutable.forEach {
            check(it is Memory) { "copyMemory() supports only Memory areas!" }
            val data = it.load(it.start, it.size.asInt)
            memory.allocate(it.name, it.start, it.size.asInt, it.access, data)
        }

        return memory
    }

    // For mmu, like in WindowsVEOS TODO: consider to use bus-arch rather then inheritance
    open val memBus get() = buses.mem

    open fun disconnectMemories() = processes.forEach { it.memory.ports.mem.disconnect(memBus)  }

    open fun connectCurrentMemory() = currentMemory.ports.mem.connect(memBus)

    open fun reconnectMemoryIfRequired(id: Int) = processes.first { it.id == id }.let { previous ->
        if (currentMemory != previous.memory) {
            previous.memory.ports.mem.disconnect(memBus)
            currentMemory.ports.mem.connect(memBus)
        }
    }

    // TODO: reorganize structure for example, id procedures

    // First process id is 1
    // TODO: in Unix, but what about Windows?
    protected val processIds = IdAllocator(1)

    fun findProcessById(id: Int) = processes.find { it.id == id }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        val backup = THashMap(components)
        components.clear()

        return super<IAutoSerializable>.serialize(ctxt).also {
            components.putAll(backup)
        }
    }

    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        reset()
        reconnect {
            disconnectMemories()
            components.filter { it.value is VirtualMemory }.keys.forEach { components.remove(it) }
            super<IAutoSerializable>.deserialize(ctxt, snapshot)
            connectCurrentMemory()
        }
    }
}