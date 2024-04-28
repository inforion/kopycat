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
package ru.inforion.lab403.kopycat.runtime.funcall

import ru.inforion.lab403.common.extensions.endOrNull
import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.annotations.DontAutoSerialize
import ru.inforion.lab403.kopycat.auxiliary.capturable.Capturable
import ru.inforion.lab403.kopycat.interfaces.IAutoSerializable
import ru.inforion.lab403.kopycat.runtime.abi.IAbi


data class FunQueuedUtilsData(
    @DontAutoSerialize
    val isReadyToCall: FunQueuedUtils.() -> Boolean,

    /**
     * The capturable object will be called lazily
     */
    @DontAutoSerialize
    val capturable: FunQueuedUtils.() -> Capturable<Unit>,

    val functionName: String? = null,
) : IAutoSerializable {
    internal var called: Boolean = false
}

data class FunQueuedUtilsState(
    val returnAddress: ULong,
    val returnStackPtr: ULong,
    val registerState: List<ULong>,

    val capturable: Capturable<Unit>,

    val functionName: String? = null,
) : IAutoSerializable

class FunQueuedUtils(@DontAutoSerialize val abi: IAbi) : IAutoSerializable {
    companion object {
        @Transient
        private val log = logger(FINE)
    }

    @DontAutoSerialize
    val core get() = abi.core

    /**
     * Queue-like list
     */
    val functionsQueue = mutableListOf<FunQueuedUtilsData>()

    /**
     * Stack-like list
     */
    val savedState = mutableListOf<FunQueuedUtilsState>()

    @DontAutoSerialize
    val currentState get() = savedState.endOrNull

    @DontAutoSerialize
    val isInProcessing get() = currentState != null

    private fun syncFunctionsQueue() {
        functionsQueue.removeAll { it.called }
    }

    private fun FunQueuedUtilsData.start() {
        val state = abi.saveState()

        // WARN: capturable must allocate vars and arguments
        val capturable = this.capturable(this@FunQueuedUtils)
        capturable.initialize()
        log.config { "Initialized capturable" }

        val returnAddress = abi.pc
        val returnStackPtr = abi.sp

        savedState.add(
            FunQueuedUtilsState(
                returnAddress = returnAddress,
                returnStackPtr = returnStackPtr,
                registerState = state,
                capturable = capturable,
                functionName = this.functionName
            )
        )
        log.config { "Saved the state" }

        // WARN: capturable must call
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
                    log.config { "[0x${core.core.pc.hex}] FunQueuedUtils: Called '${data.functionName}'" }
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
        val state = savedState.removeAt(savedState.size - 1)
        state.capturable.destroy()
        abi.restoreState(state.registerState)
        log.config { "Restored the last state" }
    }

    fun checkAndRestoreTheState() {
        val state = currentState
        if (state?.returnStackPtr == abi.sp) {
            if (state.returnAddress != abi.pc) {
                log.warning {
                    "[0x${core.pc.hex}] funQueuedUtils: " +
                            "for '${state.functionName}' PC mismatch. " +
                            "Expected 0x${state.returnAddress.hex}, " +
                            "got 0x${abi.pc.hex}. " +
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
