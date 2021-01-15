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

import ru.inforion.lab403.common.extensions.asULong
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.mips.enums.ExcCode
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class COP0v1(core: MipsCore, name: String) : ACOP0(core, name) {
    // TODO: refactor to ARegisterBankNG-fields
    override fun processInterrupts() {
        super.processInterrupts()

        // Process interrupt at once it requested (i.e. external interrupt controller set it to pending and active state)
        // Don't wait while EI bit will be set by MIPS core (if firmware not hurry up then interrupt will be discarded)
        // TODO: IE set always true for backward MIPS compat. but reconsideration required
        val interrupt = pending(true)
        if (interrupt != null) {
            // Merge external interrupt with pending internal software interrupt (or required, see EMUKOT-106)
            regs.Cause.IP7_0 = interrupt.cause.asULong
            interrupt.onInterrupt()
        }

        // Interrupt taken only if these condition are true
        // see vol.3 MIPS32 (Rev. 0.95) Architecture (5.1)
        // log.config { "IP = %08X IM = %08X IRQ = %08X".format(ip, im, ip and im) }
        if ((regs.Cause.IP7_0 and regs.Status.IM7_0) != 0L
//                && !regs.Debug.DM
                && regs.Status.IE
                && !regs.Status.EXL
                && !regs.Status.ERL) {
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
            regs.BadVAddr.value = exception.vAddr
            regs.EntryHi.VPN2 = exception.vpn2
            regs.Context.BadVPN2 = exception.vpn2
        }

        // if StatusEXL = 0...
        if (!regs.Status.EXL) {
            if (core.cpu.branchCntrl.isDelaySlot) {
                regs.Cause.BD = true
                regs.EPC.value = core.cpu.pc - 4
            } else {
                regs.Cause.BD = false
                regs.EPC.value = core.cpu.pc
            }

            val isInterruptPending = isInterrupt(exception) && regs.Cause.IV

            vectorOffset = when {
                isTlbRefillFlag -> 0x000
                isInterruptPending -> 0x200 * (1 + VIntPriorityEncoder())
                else -> 0x180
            }
        } else {
            vectorOffset = 0x180
        }

        if (exception is MipsHardwareException && exception.excCode != ExcCode.INT)
            regs.Cause.EXC = (exception.excCode as ExcCode).id

        // CauseCE <- FaultingCoprocessorNumber
        // CauseExcCode <- ExceptionType
        // StatusEXL <- 1
        regs.Status.EXL = true

        // if StatusBEV = 1 then ...
        if (regs.Status.BEV) {
            core.cpu.branchCntrl.setIp(0xBFC0_0200 + vectorOffset)
        } else {
            core.cpu.branchCntrl.setIp(0x8000_0000 + vectorOffset)
        }

        return null
    }
}