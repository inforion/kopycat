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
package ru.inforion.lab403.kopycat.cores.base.debug

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.auxiliary.LimitedQueue
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.debug.impl.HexSymbolTranslator
import ru.inforion.lab403.kopycat.cores.base.debug.interfaces.IDebugSymbolTranslator
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.interfaces.IConstructorSerializable
import ru.inforion.lab403.kopycat.interfaces.IResettable
import ru.inforion.lab403.kopycat.settings
import java.io.Serializable
import java.util.*

class CoreInfo<C: AGenericCore>(val core: C): IAutoSerializable, IResettable {
    companion object {
        @Transient val log = logger()
    }

    private data class StackElement(val pc: ULong, val ra: ULong): IConstructorSerializable

    var translator: IDebugSymbolTranslator = HexSymbolTranslator()

    private val trace = LimitedQueue<ULong>(settings.traceItemsCapacity)
    private val stacktrace = Stack<StackElement>()

    /**
     * {EN}
     * Saved program counter before ```cpu.execute()``` using [saveProgramCounter] function
     * in [ACore.doExecuteInstruction]. Required to save PC if exception will occurred
     * {EN}
     */
    private var lastPC = 0uL

    var totalExecuted = 0uL
        private set

    var flagsAccessed = 0uL
        internal set

    var flagsChanged = 0uL
        internal set

    private fun doProgramCounterTrace(exception: Boolean) {
        if (!exception)
            trace.add(core.pc)
    }

    private fun doStackTrace(exception: Boolean) {
        if (!core.cpu.hasInstruction) {
            log.warning { "Something weired happen: no instructions executed -> can't handle stack trace..." }
            return
        }

        if (core.pc != lastPC + core.cpu.insn.size.uint) { // Jump occurred
            if (exception || core.cpu.callOccurred) { // Sub call
                stacktrace.push(StackElement(lastPC, lastPC + core.cpu.insn.size.uint))
            } else if (stacktrace.isNotEmpty() && stacktrace.peek().ra == core.pc) { // Return from call
                stacktrace.pop()
            }
        }
    }

    fun printTrace() {
        log.info { "-------------------- Program counter trace: --------------------" }
        trace.forEach { log.info { translator.translate(it) } }
        log.info { translator.translate(core.pc) }
    }

    fun printStackTrace() {
        log.info { "-------------------- Stack trace: ------------------------------" }
        stacktrace.forEach { log.info { translator.translate(it.pc) } }
    }

    fun printCpuState() {
        log.info { "-------------------- CPU state: --------------------------------" }

        log.info { "Total executed instructions: $totalExecuted" }

        log.info { core.stringify() }
    }

    fun dump(cpu: Boolean = true, stack: Boolean = true, trace: Boolean = true) {
        if (cpu) printCpuState()
        if (stack) printStackTrace()
        if (trace) printTrace()
    }

    fun saveProgramCounter() {
        lastPC = core.pc
    }

    fun trace(exception: Boolean) {
        totalExecuted++
        doProgramCounterTrace(exception)
        doStackTrace(exception)
    }

    fun epilog() {
        flagsAccessed = 0u
        flagsChanged = 0u
    }

    override fun reset() {
        lastPC = 0u
        totalExecuted = 0u
        trace.clear()
        stacktrace.clear()
    }
}