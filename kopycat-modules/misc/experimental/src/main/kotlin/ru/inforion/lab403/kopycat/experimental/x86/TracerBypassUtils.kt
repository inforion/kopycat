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
package ru.inforion.lab403.kopycat.experimental.x86

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.plus
import ru.inforion.lab403.common.logging.FINE
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SKIP
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SUCCESS
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.experimental.tracer.TracerUtils
import ru.inforion.lab403.kopycat.experimental.x86.funUtils.x86funUtils
import ru.inforion.lab403.kopycat.modules.cores.x86Core
import java.util.logging.Level

class TracerBypassUtils(val funUtils: x86funUtils) {
    companion object {
        @Transient
        val log = logger(FINE)
    }

    val core get() = funUtils.x86

    fun log(level: Level, functionName: String, vararg argumentNames: String): ULong {

        log.log(level) {
            buildString {
                append("[0x${core.pc.hex}] Called $functionName. ")
                append(argumentNames.mapIndexed { i, v ->
                    "[Argument $i name=${v} value=0x${funUtils.abi.getArgument(i).hex}]"
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
        val returnOffsetCallback: (rsp: ULong, rbp: ULong) -> ULong = { rsp, _ -> rsp },
    )

    /**
     * Generic bypass by any function.
     * Must be located at the function beginning (before any stack changes)
     */
    fun genericFunctionBypass(data: BypassData): ULong {
        val returnPtrStack = data.returnOffsetCallback(core.cpu.regs.rsp.value, core.cpu.regs.rbp.value)
        val returnPtr = core.read(returnPtrStack, 0x0, data.stackSize.bytes)

        log.warning {
            "[0x${core.pc.hex}] ${data.name}: Bypassing the function " +
                    "returnAddr=0x${returnPtr.hex}"
        }
        (0 until data.showArgAmount).map { i ->
            funUtils.abi.getArgument(i)
        }.forEachIndexed { i, arg ->
            log.fine { "[0x${core.pc.hex}] ${data.name}: Argument ${i}: 0x${arg.hex}" }
        }

        // pop
        core.cpu.regs.gpr(x86GPR.RSP, data.stackSize).value += data.stackSize.bytes
        // set pc
        core.cpu.regs.gpr(x86GPR.RIP, data.stackSize).value = returnPtr

        // Skip current instruction
        return TRACER_STATUS_SKIP
    }
}