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
@file:Suppress("LocalVariableName")

package ru.inforion.lab403.kopycat.cores.mips.hardware.processors

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.mips.enums.ExcCode
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException.*
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class COP0v2(core: MipsCore, name: String) : ACOP0(core, name) {
    override fun reset() {
        super.reset()
        regs.EBase.value = 0x8000_0000
    }

    override fun processInterrupts() {
        super.processInterrupts()

        val DebugDM = false  // regs.Debug.DM
        val StatusIE = regs.Status.IE
        val StatusEXL = regs.Status.EXL
        val StatusERL = regs.Status.ERL
        val Config3VEIC = regs.Config3.VEIC

        if (!DebugDM && StatusIE && StatusEXL && StatusERL) {
            if (Config3VEIC) {
//                if (regs.Cause.IP7_2 != 0L)
//                    throw UnsupportedOperationException("Hardware EIC IPL bits shouldn't be here...")

                val IPL = regs.Status.IM7_2
                // TODO: IE set always true for backward MIPS compat. but reconsideration required
                val interrupt = pending(true)
                if (interrupt != null) {
                    val RIPL = interrupt.cause.asULong
                    if (RIPL > IPL) {
                        regs.Cause.IP7_2 = RIPL
                        interrupt.onInterrupt()
                        throw INT(core.pc, interrupt)
                    }
                }  // If throw then we need no else

                // Note: Regard to IM1..IM0
                // In implementations of Release 2 of the Architecture in which EIC interrupt mode is enabled
                // (Config3VEIC = 1), these bits are writable, but have no effect on the interrupt system.
                val swInterrupt = when {
                    // TODO: Sort out software interrupts mess
                    regs.Cause.IP1 -> TODO() //(dev as PIC32MZ2048EFH144).system.swInterrupt1
                    regs.Cause.IP0 -> TODO() //(dev as PIC32MZ2048EFH144).system.swInterrupt0
                    else -> return
                }
//                if (swInterrupt.cause > IPL)
//                    throw MipsHardwareException.INT(swInterrupt)
            } else {
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
                if (regs.Cause.IP7_0 and regs.Status.IM7_0 != 0L) {
                    // Software and hardware interrupts both exceptions,
                    // see vol.3 MIPS32 (Rev. 0.95) Architecture (5.2.23)
                    throw MipsHardwareException.INT(core.pc, interrupt)
                }
            }
        }
    }

    override fun handleException(exception: GeneralException?): GeneralException? {
        if (exception !is HardwareException)
            return exception

        // See MIPS® Architecture For Programmers
        // Vol. III: MIPS32® Privileged Resource Architecture
        // Rev. 6.02, Page 87
        val vectorOffset: Int
        val vectorBase: Long

        val StatusBEV = regs.Status.BEV
        val StatusEXL = regs.Status.EXL
        val CauseIV = regs.Cause.IV

        if (StatusEXL) {
            vectorOffset = 0x180
        } else {
            if (core.cpu.branchCntrl.isDelaySlot) {
                regs.EPC.value = core.cpu.pc - 4
                regs.Cause.BD = true
            } else {
                regs.EPC.value = core.cpu.pc
                regs.Cause.BD = false
            }

            val VecNum: Int

            val SRSCtlHSS = regs.SRSCtl.HSS
            val SRSCtlCSS = regs.SRSCtl.CSS
            val SRSCtlESS = regs.SRSCtl.ESS
            val SRSCtlEICSS = regs.SRSCtl.EICSS

            /* Compute vector offsets as a function of the type of exception */
            var NewShadowSet = SRSCtlESS     /* Assume exception, Release 2 only */
            if (exception is TLBInvalid || exception is TLBMiss || exception is TLBModified) {
                exception as MipsHardwareException
                regs.BadVAddr.value = exception.vAddr
                regs.EntryHi.VPN2 = exception.vpn2
                regs.Context.BadVPN2 = exception.vpn2
//                log.warning { "[${core.cpu.pc.hex8}] ${exception.excCode} -> BadVAddr = ${exception.vAddr.hex8}" }
                if (exception.vAddr == 0L) {
                    log.severe { "Null-pointer exception occurred... halting CPU core!" }
                    core.debugger.isRunning = false
                }
                vectorOffset = if (exception is TLBMiss) 0x000 else 0x180
            } else if (isInterrupt(exception)) {
                exception as MipsHardwareException.INT
                if (!CauseIV) {
                    vectorOffset = 0x180
                } else { // CauseIV = 1
                    // Vector Spacing
                    // 0x00 0x000 0
                    // 0x01 0x020 32
                    // 0x02 0x040 64
                    // 0x04 0x080 128
                    // 0x08 0x100 256
                    // 0x10 0x200 512
                    val IntCtlVS = regs.IntCtl.VS.asInt
                    val Config3VEIC = regs.Config3.VEIC

                    if (StatusBEV || IntCtlVS == 0) {
                        vectorOffset = 0x200
                    } else { // StatusBEV = 0
                        if (Config3VEIC) {
                            // 6.1.1.3 External Interrupt Controller Mode
                            // For VEIC = 1 IP field is RIPL (Requested interrupt level)
                            val CauseRIPL = regs.Cause.IP7_2.asInt

                            VecNum = when {
                                core.EIC_option1 -> CauseRIPL
                                core.EIC_option2 -> exception.interrupt!!.irq // EIC_VecNum_Signal
                                core.EIC_option3 -> -1  // unused
                                else -> throw GeneralException("Wrong EIC options configuration...")
                            }
                            NewShadowSet = SRSCtlEICSS
                        } else { // Config3VEIC = 0
                            VecNum = VIntPriorityEncoder()
                            NewShadowSet = ShadowSetEncoder(VecNum)
                        }

                        vectorOffset = if (Config3VEIC && core.EIC_option3) {
                            exception.interrupt!!.vector.asInt // EIC_VectorOffset_Signal
                        } else {
                            0x200 + VecNum * (IntCtlVS shl 5)
                        }
                    } /* if (StatusBEV = 1) or (IntCtlVS = 0) then */
                } /* if (CauseIV = 0) then */
            } else { /* elseif (ExceptionType = Interrupt) then */
                // All others general exceptions goes to 0x180 vector offset
                // NOTE: this isn't specified directly in mips vol3.
                vectorOffset = 0x180
            }

            /* Update the shadow set information for an implementation of */
            /* Release 2 of the architecture */
            if (core.ArchitectureRevision >= 2 && SRSCtlHSS > 0 && !StatusBEV) {
                regs.SRSCtl.PSS = SRSCtlCSS
                regs.SRSCtl.CSS = NewShadowSet
            }
        } /* if StatusEXL = 1 then */

        // CauseCE <- FaultingCoprocessorNumber
        // CauseExcCode <- ExceptionType
        // StatusEXL <- 1
        if (exception is MipsHardwareException) {
            val code = exception.excCode as ExcCode
            regs.Cause.EXC = code.id
        }
        regs.Status.EXL = true

        /* Calculate the vector base address */
        if (StatusBEV) {
            vectorBase = 0xBFC0_0200
        } else {
            if (core.ArchitectureRevision >= 2)
                vectorBase = regs.EBase.value bzero 12..0
            else
                vectorBase = 0x8000_0000
        }

        /* Exception PC is the sum of vectorBase and vectorOffset. Vector */
        /* offsets > 0xFFF (vectored or EIC interrupts only), require */
        /* that EBase15..12 have zeros in each bit position less than or */
        /* equal to the most significant bit position of the vector offset */
        val offset = vectorBase[29..0] + vectorOffset[29..0]  /* No carry between bits 29 and 30 */
        val PC = cat(vectorBase[31..30], offset, 29)
        core.cpu.branchCntrl.setIp(PC)

        return null
    }
}