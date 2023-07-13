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
package ru.inforion.lab403.kopycat.experimental.x86.funUtils

import org.jetbrains.kotlin.backend.common.pop
import org.jetbrains.kotlin.backend.common.push
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.experimental.common.capturable.Capturable
import ru.inforion.lab403.kopycat.experimental.common.capturable.CapturableInvoker
import ru.inforion.lab403.kopycat.experimental.common.capturable.CapturableNoBody
import ru.inforion.lab403.kopycat.experimental.common.capturable.addBody
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.abi.x86IAbi
import ru.inforion.lab403.kopycat.experimental.x86.stepOrFailWhile
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class x86funUtils(
    val x86: x86Core,
    val abi: x86IAbi
) {
    private val calledFunctions = mutableListOf<CalledFunctionData>()

    companion object {
        val log = logger(FINE)
    }

    data class CalledFunctionData(
        val returnAddress: ULong,
        val returnStackPtr: ULong,
        val functionName: String? = null,
    )

    protected fun applyCoreCall(functionName: String?, address: ULong) {
        val returnAddress = x86.cpu.regs.rip.value
        calledFunctions.push(
            CalledFunctionData(
                returnAddress = returnAddress,
                returnStackPtr = x86.cpu.regs.rsp.value,
                functionName = functionName,
            )
        )

        // Ret will return the CPU at the returnAddress
        abi.pushStack(returnAddress)

        // Jump to target
        x86.pc = address
    }

    protected fun runUtilCallExit() {
        // Execute
        x86.stepOrFailWhile {
            require(calledFunctions.isNotEmpty()) {
                "Why there is no called functions?"
            }

            calledFunctions.last().let { data ->
                val rip = x86.cpu.regs.rip.value

                (data.returnStackPtr != x86.cpu.regs.rsp.value).let {
                    if (!it) {
                        require(data.returnAddress == rip) {
                            "What the hell, why the is address mismatch? Broken stack?"
                        }
                    };

                    it
                }
            }
        }

        calledFunctions.pop()
    }

    /**
     * Pipeline:
     * 1. Allocates local variables if needed
     * 2. Allocates space for arguments
     * 3. Puts the arguments
     * 4. Prepares the call
     * 5. Executes call
     * 6. Received the result
     * 7. Clears the arguments allocation
     * 8. Clears the local allocations
     */
    fun callNamed(functionName: String?, address: ULong, vararg args: FunArg): ULong = argsCapturable(*args).addBody {
        callNamed(functionName, address)
    }.execute(this)

    fun callNamed(functionName: String?, address: ULong): ULong {
        applyCoreCall(functionName, address)
        runUtilCallExit()

        return abi.getResult()
    }

    fun argsCapturable(vararg args: FunArg): CapturableNoBody = object : CapturableNoBody {
        lateinit var allocas: List<StackAllocation>

        override fun initialize() {
            allocas = abi.allocAndPutArgs(args.toList())
        }

        override fun destroy() {
            allocas.reversed().forEach {
                abi.clearStackAllocation(it)
            }
        }
    }

    fun call(addr: ULong, vararg args: FunArg): ULong = callNamed(null, addr, *args)

    fun <T> abiStateCapture(block: x86funUtils.() -> T): T {
        val state = abi.saveState();
        val result: T = try {
            this.block()
        } finally {
            abi.restoreState(state);
        }

        return result
    }

    private fun <T> innerExecuteCapturable(capturable: Capturable<T>): T = abiStateCapture {
        capturable.initialize()

        val result: T = try {
            capturable.body()
        } finally {
            capturable.destroy()
        }

        return@abiStateCapture result;
    }

    /**
     * State-safe capturable call
     */
    fun <T> executeCapturable(capturable: Capturable<T>): T =
        innerExecuteCapturable(CapturableInvoker(capturable))
}
