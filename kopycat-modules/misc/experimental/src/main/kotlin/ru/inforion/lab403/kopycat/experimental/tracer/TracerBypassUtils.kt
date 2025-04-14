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
package ru.inforion.lab403.kopycat.experimental.tracer

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.LogLevel
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SKIP
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SUCCESS
import ru.inforion.lab403.kopycat.runtime.abi.IAbi

class TracerBypassUtils(val abi: IAbi) {
    companion object {
        @Transient
        val log = logger(FINE)
    }

    val core get() = abi.core

    fun log(level: LogLevel, functionName: String, vararg argumentNames: String): ULong {
        log.log(level) {
            buildString {
                append("[0x${core.pc.hex}] Called $functionName. ")
                append(argumentNames.mapIndexed { i, v ->
                    "[Argument $i name=${v} value=0x${abi.getArgument(i).hex}]"
                }.joinToString(" "))
            }
        }

        return TRACER_STATUS_SUCCESS
    }

    data class BypassData(
        val pc: ULong,
        val name: String,
        val showArgAmount: Int = 0,
        val returnValue: ULong = 0uL,
        val stackSize: Datatype = Datatype.QWORD,
        val returnOffsetCallback: (rsp: ULong) -> ULong = { rsp -> rsp },
    )

    /**
     * Generic bypass by any function.
     * Must be located at the function beginning (before any stack changes)
     */
    fun genericFunctionBypass(data: BypassData): ULong {
        val returnPtrStack = data.returnOffsetCallback(abi.sp)
        val returnPtr = core.read(returnPtrStack, 0x0, data.stackSize.bytes)

        log.warning {
            "[0x${core.pc.hex}] ${data.name}: Bypassing the function " +
                    "returnAddr=0x${returnPtr.hex}"
        }
        (0 until data.showArgAmount).map { i ->
            abi.getArgument(i)
        }.forEachIndexed { i, arg ->
            log.fine { "[0x${core.pc.hex}] ${data.name}: Argument ${i}: 0x${arg.hex}" }
        }

        // pop
        abi.popStack()
        // set pc
        abi.pc = returnPtr

        abi.setResult(0, data.returnValue)

        // Skip current instruction
        return TRACER_STATUS_SKIP
    }
}