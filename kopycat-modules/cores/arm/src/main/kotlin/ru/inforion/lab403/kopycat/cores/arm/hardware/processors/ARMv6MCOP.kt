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
package ru.inforion.lab403.kopycat.cores.arm.hardware.processors

import ru.inforion.lab403.common.extensions.clr
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.arm.enums.Mode
import ru.inforion.lab403.kopycat.cores.arm.enums.VectorTable
import ru.inforion.lab403.kopycat.cores.arm.exceptions.ARMHardwareException.Unknown
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.modules.cores.ARMv6MCore
import java.util.logging.Level.FINE



class ARMv6MCOP(val cpu: ARMv6MCore, name: String) : AARMCOP(cpu, name) {
    companion object {
        @Transient val log = logger(FINE)
    }

    fun ExceptionEntry(core: ARMv6MCore, exceptionType: VectorTable) {
        PushStack(core, exceptionType)
        ExceptionTaken(core, exceptionType)
    }

    fun XPSR(core: ARMv6MCore, framePtrAlign: Long): Long {
        var result = core.cpu.sregs.apsr.value or core.cpu.sregs.ipsr.value or core.cpu.sregs.epsr.value
        result = result.insert(framePtrAlign, 9)
        return result
    }

    fun PushStack(core: ARMv6MCore, exceptionType: VectorTable) {
        val framePtrAlign = core.cpu.regs.sp.value[2]
        core.cpu.regs.sp.value = (core.cpu.regs.sp.value - 0x20) clr 2
        val framePtr = core.cpu.regs.sp.value

        core.outl(framePtr, core.cpu.regs.r0.value)
        core.outl(framePtr + 0x4, core.cpu.regs.r1.value)
        core.outl(framePtr + 0x8, core.cpu.regs.r2.value)
        core.outl(framePtr + 0xC, core.cpu.regs.r3.value)
        core.outl(framePtr + 0x10, core.cpu.regs.r12.value)
        core.outl(framePtr + 0x14, core.cpu.regs.lr.value)
        core.outl(framePtr + 0x18, ReturnAddress(core, exceptionType))
        core.outl(framePtr + 0x1C, XPSR(core, framePtrAlign))

        if (core.cpu.CurrentMode == Mode.Handler) {
            core.cpu.regs.lr.value = 0xFFFF_FFF1
        } else {
            core.cpu.regs.lr.value = if (!core.cpu.spr.control.spsel) 0xFFFF_FFF9 else 0xFFFF_FFFD
        }
    }

    fun ExceptionTaken(core: ARMv6MCore, exceptionType: VectorTable) {
        core.cpu.CurrentMode = Mode.Handler
        core.cpu.sregs.ipsr.value = exceptionType.exceptionNumber
        core.cpu.spr.control.spsel = false
        val start = core.inl(core.cpu.VTOR + 4 * exceptionType.exceptionNumber)
        core.cpu.BLXWritePC(start)
    }

    fun ReturnAddress(core: ARMv6MCore, exceptionType: VectorTable): Long =
            if(exceptionType.exceptionNumber in 2..47) core.cpu.pc clr 0
            else throw Unknown

    override fun handleException(exception: GeneralException?): GeneralException? = exception

    override fun processInterrupts() {
        if (core.cpu.CurrentMode() == Mode.Thread) {
            val interrupt = pending(true)
            if (interrupt != null) {
//                log.fine { "ARMv6M: ${interrupt.stringify()}" }
                ExceptionEntry(cpu, VectorTable.fromOffset(interrupt.vector))
                interrupt.onInterrupt()
            }
        }
    }
}
