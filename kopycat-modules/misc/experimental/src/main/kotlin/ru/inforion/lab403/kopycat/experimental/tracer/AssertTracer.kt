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
import ru.inforion.lab403.kopycat.cores.base.AGenericCore
import ru.inforion.lab403.kopycat.cores.base.abstracts.ATracer
import ru.inforion.lab403.kopycat.cores.base.common.Module
import ru.inforion.lab403.kopycat.cores.base.operands.Memory as OpMemory
import ru.inforion.lab403.kopycat.cores.base.enums.Status
import ru.inforion.lab403.kopycat.cores.base.extensions.TRACER_STATUS_SUCCESS
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.base.operands.APhrase
import ru.inforion.lab403.kopycat.cores.base.operands.Displacement
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch.Call
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.branch.Ret
import ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory.Mov
import ru.inforion.lab403.kopycat.modules.cores.x86Core

/**
 * Dynamic per-instruction analysis
 * Checks the memory access of various instructions
 *
 * **Warning**: can significantly decrease the performance
 */
class AssertTracer(
    parent: Module?,
    name: String,
    val wrongAreas: MutableList<ULongRange> = mutableListOf(
        0uL..0x1_0000uL,
        0xFFFF_FFFF_FFF0_0000uL..0xFFFFFFFF_FFFFFFFFuL,
    ),
    var breakDebugger: Boolean = false,
) : ATracer<x86Core>(parent, name) {

    // TODO: duplicates with `ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.memory.Lea`
    private fun <T : AGenericCore> AOperand<T>.addressOrNull(core: T) = when (this) {
        is Displacement -> this.effectiveAddress(core)
        is OpMemory -> this.effectiveAddress(core)
        is APhrase -> this.effectiveAddress(core)
        else -> null
    }

    companion object {
        private const val NOT_OPERAND = -2;
    }

    override fun preExecute(core: x86Core): ULong {
        val insn = core.cpu.insn
        val addresses: Iterable<Pair<Int, ULong>> = when (insn) {
            is Call -> if (!insn.isRelative) {
                insn
                    .mapIndexed { i, it -> i to it }
                    .map { (i, op) -> op.value(core).let { addr -> i to addr } }
            } else {
                emptyList()
            }

            is Mov -> {
                insn
                    .mapIndexed { i, it -> i to it }
                    .mapNotNull { (i, op) -> op.addressOrNull(core)?.let { addr -> i to addr } }
            }

            is Ret -> {
                // TODO: check real modes etc etc
                val sp = core.cpu.regs.gpr(x86GPR.RSP, insn.prefs.opsize);
                val returnAddr = core.read(sp.value, core.cpu.sregs.ss.id, insn.prefs.opsize.bytes)
                listOf(NOT_OPERAND to returnAddr)
            }

            else -> emptyList()
        }

        addresses
            .firstOrNull { (_, addr) -> wrongAreas.any { area -> addr in area } }
            ?.also { (i, addr) ->
                log.warning {
                    "[0x${core.pc.hex}] ru.inforion.lab403.kopycat.experimental.tracer.AssertTracer: " +
                            "wrong access, " +
                            "instruction='${insn}' operand=${i + 1} " +
                            "address=0x${addr.hex}"
                }

                if (breakDebugger && isDebuggerPresent) {
                    log.info {
                        "[0x${core.pc.hex}] ru.inforion.lab403.kopycat.experimental.tracer.AssertTracer: " +
                                "Breaking due to enabled 'breakDebugger'"
                    }
                    debugger.isRunning = false
                }
            }

        return TRACER_STATUS_SUCCESS
    }

    override fun postExecute(core: x86Core, status: Status) = TRACER_STATUS_SUCCESS;
}
