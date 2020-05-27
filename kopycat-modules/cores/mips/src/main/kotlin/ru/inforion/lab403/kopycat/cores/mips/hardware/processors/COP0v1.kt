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
package ru.inforion.lab403.kopycat.cores.mips.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.mips.enums.Cause
import ru.inforion.lab403.kopycat.cores.mips.enums.ExcCode
import ru.inforion.lab403.kopycat.cores.mips.enums.Status
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class COP0v1(core: MipsCore, name: String) : ACOP0(core, name) {
    override fun processInterrupts() {
        super.processInterrupts()

        // Process interrupt at once it requested (i.e. external interrupt controller set it to pending and active state)
        // Don't wait while EI bit will be set by MIPS core (if firmware not hurry up then interrupt will be discarded)
        // TODO: IE set always true for backward MIPS compat. but reconsideration required
        val interrupt = pending(true)
        if (interrupt != null) {
            // Merge external interrupt with pending internal software interrupt (or required, see EMUKOT-106)
            regs.Cause = regs.Cause or regs.Cause.insert(interrupt.cause.asULong, Cause.IP7.pos..Cause.IP0.pos)
            interrupt.onInterrupt()
        }

        // Interrupt taken only if these condition are true
        // see vol.3 MIPS32 (Rev. 0.95) Architecture (5.1)
        val ip = regs.Cause[Cause.IP7.pos..Cause.IP0.pos]
        val im = regs.Status[Status.IM7.pos..Status.IM0.pos]
        // log.config { "IP = %08X IM = %08X IRQ = %08X".format(ip, im, ip and im) }
        if ((ip and im) != 0L &&
                //      regs.Debug[Debug.DM.pos] == 0L &&
                regs.Status[Status.IE.pos] == 1L &&
                regs.Status[Status.EXL.pos] == 0L &&
                regs.Status[Status.ERL.pos] == 0L) {
            // Software and hardware interrupts both exceptions,
            // see vol.3 MIPS32 (Rev. 0.95) Architecture (5.2.23)
            throw MipsHardwareException.INT(core.pc, interrupt)
        }
    }

    override fun handleException(exception: GeneralException?): GeneralException? {
        if (exception !is HardwareException)
            return exception

        // Written by General Exception Processing pseudo-code
        // see vol.3 MIPS32 (Rev. 0.95) Architecture (5.2.2)
        val vectorOffset: Int

        // TLB analysis, really in processor not here (not know where)
        val isTlbRefillFlag = isTlbRefill(exception)
        if (isTlbRefillFlag) {
            exception as MipsHardwareException
            val vAddr = exception.vAddr
            regs.BadVAddr = vAddr
            regs.EntryHi = regs.EntryHi.insert(vAddr[31..13], 31..13)
            regs.Context = regs.Context.insert(vAddr[31..13], 22..4)
        }

        // if StatusEXL = 0...
        if (regs.Status[Status.EXL.pos] == 0L) {
            if (core.cpu.branchCntrl.isDelaySlot) {
                regs.Cause = setBit(regs.Cause, Cause.BD.pos)
                regs.EPC = core.cpu.pc - 4
            } else {
                regs.Cause = clearBit(regs.Cause, Cause.BD.pos)
                regs.EPC = core.cpu.pc
            }

            val isInterruptPending = isInterrupt(exception) && regs.Cause[Cause.IV.pos] == 1L

            vectorOffset = when {
                isTlbRefillFlag -> 0x000
                isInterruptPending -> 0x200 * (1 + VIntPriorityEncoder())
                else -> 0x180
            }
        } else {
            vectorOffset = 0x180
        }

        if (exception is MipsHardwareException && exception.excCode != ExcCode.INT)
            setExcCode(exception.excCode as ExcCode)

        // CauseCE <- FaultingCoprocessorNumber
        // CauseExcCode <- ExceptionType
        // StatusEXL <- 1
        regs.Status = setBit(regs.Status, Status.EXL.pos)

        // if StatusBEV = 1 then ...
        if (regs.Status[Status.BEV.pos] == 1L) {
            core.cpu.branchCntrl.setIp(0xBFC0_0200 + vectorOffset)
        } else {
            core.cpu.branchCntrl.setIp(0x8000_0000 + vectorOffset)
        }

        return null
    }
}