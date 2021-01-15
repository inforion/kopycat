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
package ru.inforion.lab403.kopycat.veos.kernel

import kotlinx.coroutines.Deferred
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINER
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable
import ru.inforion.lab403.kopycat.modules.memory.VirtualMemory
import ru.inforion.lab403.kopycat.veos.deferred.DeferredOperation


open class Process constructor(
        val sys: System,
        val id: Int,
        val memory: VirtualMemory // TODO: Ugly, make default & move outside ctor, or lateinit var
): IAutoSerializable, IConstructorSerializable {

    companion object {
        @Transient val log = logger(FINER)
    }

    open val processType = "Process"

    var contextInitialized = false
        private set

    // REVIEW: Add variant of function that works by ABIBase-argument
    fun initContext(pc: Long, sp: Long, ra: Long) {
        context.programCounterValue = pc
        context.stackPointerValue = sp
        context.returnAddressValue = ra
        contextInitialized = true
    }

    open infix fun cloneOf(process: Process): Process {
        check(sys.currentProcess != this) { "Can't clone from current process" }

        handlers.clear()
        handlers.putAll(process.handlers)

        allocator = Allocator(process.allocator)

        context.save()

        if (sys.currentProcess == process)
            initContext(sys.abi.programCounterValue, sys.abi.stackPointerValue, sys.abi.returnAddressValue)
        else
            initContext(process.context.programCounterValue, process.context.stackPointerValue, process.context.returnAddressValue)

        return this
    }

    open infix fun aliasOf(process: Process): Process {
        check(sys.currentProcess != this) { "Can't alias from current process" }

        // Possible bug:
        //  1. Create thread
        //  2. Load library
        //  It will cause of different handlers on loader thread and other ones
        handlers.clear()
        handlers.putAll(process.handlers)

        allocator = process.allocator

        // aliases don't have to init context? (always differs from original)
        context.save()
        if (sys.currentProcess == process)
            initContext(sys.abi.programCounterValue, sys.abi.stackPointerValue, sys.abi.returnAddressValue)
        else
            initContext(process.context.programCounterValue, process.context.stackPointerValue, process.context.returnAddressValue)

        return this
    }

    private val handlers = mutableMapOf<Long, Handler>()

    fun setHandler(address: Long, handler: Handler) {
        require(address !in handlers) { "Try of rewriting handler at address ${address.hex8}" }
        handlers[address] = handler
    }

    fun getHandler(address: Long) = handlers[address]
    fun hasHandler(address: Long) = address in handlers

    // Heap allocator
    // TODO: should not be used directly
    var allocator = Allocator(sys)
        private set

    var stackTop = 0L
        private set

    var stackBottom = 0L
        private set

    // TODO: move to corresponding function
    fun aligned(ea: Long): Long = (ea ushr sys.sizeOf.int) shl sys.sizeOf.int

    open fun initProcess(entryPoint: Long) {
        check(isReady) { "Wrong state $state (Ready expected)" }

        val stackRange = memory.allocateByAlignment("stack", sys.conf.stackSize.asInt) // TODO: make it int in config
        val heapRange = memory.allocateByAlignment("heap", sys.conf.heapSize.asInt) // TODO: make it int in config

        checkNotNull(stackRange) { "Not enough memory for stack allocation" }
        checkNotNull(heapRange) { "Not enough memory for heap allocation" }

        allocator = Allocator(sys, heapRange.first, heapRange.last)

        stackTop = stackRange.last
        stackBottom = stackRange.first

        initContext(entryPoint, aligned(stackRange.last), sys.processExitAddress)
    }

    enum class State {
        Ready,
        Blocked,
        Running,
        Segfault,
        Exited
    }
    private var state = State.Ready

    val isReady get() = state == State.Ready
    val isBlocked get() = state == State.Blocked
    val isRunning get() = state == State.Running
    val isExited get() = state == State.Exited
    val isSegfault get() = state == State.Segfault

    val context = sys.abi.createContext()

    fun saveState() {
        check(contextInitialized) { "Context wasn't initialized" }

        if (!isBlocked) // block() saves context
            context.save()

        if (isRunning)
            state = State.Ready
    }

    fun block() {
        // TODO: check, always Running
        check(isRunning) { "Wrong state for process '$this': '$state' but '${State.Running}' expected" }

        state = State.Blocked
        context.save()
    }

    fun unblock() {
        check(isBlocked) { "Wrong state for process '$this': '$state' but '${State.Blocked}' expected" }
        if (sys.currentProcess != this) state = State.Ready else restoreState()
    }

    fun unblock(result: Long) {
        context.setReturnValue(result)
        unblock()
    }

    fun restoreState() {
        check(contextInitialized) { "Context wasn't initialized" }
        check(isReady || isBlocked) { "Double restore" }
        check(sys.currentProcess == this) { "Restoration for non-current process" }
        context.load()
        state = State.Running
    }

    private fun cleanup() {
        // It allows to run more processes and not to catch java heap overflow
        // Also, after segfault memory is useless
        memory.unshare()
    }


    open fun exit() {
        state = State.Exited
        // TODO: double-save? Is it necessary?
        context.save() // Do not remove: it is a part of the blocking-api syntax
        cleanup()
    }

    open fun segfault() {
        state = State.Segfault
        cleanup()
    }

    @DontAutoSerialize
    private var delayedJob: Deferred<DeferredOperation<*>>? = null

    fun job(block: (Process) -> DeferredOperation<*>) {
        check(delayedJob == null) { "Double use of delayed job" }
        delayedJob = async { block(this@Process) }.apply { onComplete { sys.os.unblock(this@Process) } }
    }

    fun deferred() = delayedJob?.let { job ->
        check(job.isCompleted) { "Job isn't finished?" }
        job.onComplete {
            check(isRunning) { "Wrong state $state (Running expected)" }
            val result = it.deferred()
            sys.abi.setReturnValue(result)
        }
        delayedJob = null
    }

    override fun toString(): String = "$processType:$id(state=$state)"

    val hasDelayedJob get() = delayedJob != null
}