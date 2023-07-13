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
package ru.inforion.lab403.kopycat.experimental.x86.funUtils.queued

import org.jetbrains.kotlin.backend.common.pop
import org.jetbrains.kotlin.backend.common.push
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.experimental.common.capturable.Capturable
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.abi.x86IAbi
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable


data class x86funQueuedUtilsData(
    @DontAutoSerialize
    val isReadyToCall: x86funQueuedUtils.() -> Boolean,

    /**
     * The capturable object will be called lazily
     */
    @DontAutoSerialize
    val capturable: x86funQueuedUtils.() -> Capturable<Unit>,

    val functionName: String? = null,
) : IAutoSerializable {
    internal var called: Boolean = false
}

data class x86funQueuedUtilsState(
    val returnAddress: ULong,
    val returnStackPtr: ULong,
    val registerState: List<ULong>,

    val capturable: Capturable<Unit>,

    val functionName: String? = null,
) : IAutoSerializable

class x86funQueuedUtils(@DontAutoSerialize val abi: x86IAbi) : IAutoSerializable {
    companion object {
        @Transient
        private val log = logger(FINE)
    }

    @DontAutoSerialize
    val x86 get() = abi.x86

    /**
     * Queue-like list
     */
    val functionsQueue = mutableListOf<x86funQueuedUtilsData>()

    /**
     * Stack-like list
     */
    val savedState = mutableListOf<x86funQueuedUtilsState>()

    @DontAutoSerialize
    val currentState get() = savedState.endOrNull

    @DontAutoSerialize
    val isInProcessing get() = currentState != null

    private fun syncFunctionsQueue() {
        functionsQueue.removeAll { it.called }
    }

    private fun x86funQueuedUtilsData.start() {
        val state = abi.saveState()

        // WARN: capturable must allocate vars and arguments
        val capturable = this.capturable(this@x86funQueuedUtils)
        capturable.initialize()
        log.config { "Initialized capturable" }

        val returnAddress = x86.cpu.regs.rip.value
        val returnStackPtr = x86.cpu.regs.rsp.value

        savedState.push(
            x86funQueuedUtilsState(
                returnAddress = returnAddress,
                returnStackPtr = returnStackPtr,
                registerState = state,
                capturable = capturable,
                functionName = this.functionName
            )
        )
        log.config { "Saved the state" }

        // Ret will return the CPU at the returnAddress
        abi.pushStack(returnAddress)

        // WARN: Jump to target??? who will jump?
        capturable.body()
        log.config { "Called capturable body" }

        this.called = true
    }

    fun callEverythingAvailable(): Boolean {
        val queueMask = functionsQueue
            .map { data ->
                // Do not use filter+map
                // Due to isReadyToCall behavior
                if (data.isReadyToCall(this)) {
                    log.config { "[0x${x86.core.pc.hex}] x86funQueuedUtils: Called '${data.functionName}'" }
                    data.start()
                    true
                } else {
                    false
                }
            }

        syncFunctionsQueue()
        return queueMask.any { it }
    }

    private fun restoreLastState() {
        val state = savedState.pop()
        state.capturable.destroy()
        abi.restoreState(state.registerState)
        log.config { "Restored the last state" }
    }

    fun checkAndRestoreTheState() {
        val state = currentState
        if (state?.returnStackPtr == x86.cpu.regs.rsp.value) {
            if (state.returnAddress != x86.cpu.regs.rip.value) {
                log.warning {
                    "[0x${x86.pc.hex}] funQueuedUtils: " +
                            "for '${state.functionName}' PC mismatch. " +
                            "Expected 0x${state.returnAddress.hex}, " +
                            "got 0x${x86.cpu.regs.rip.value}. " +
                            "No state has been restored"
                }
                return
            }

            restoreLastState()
        }
    }

    fun forceClearState() {
        savedState.clear()
    }
}
