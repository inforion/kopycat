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
import ru.inforion.lab403.kopycat.cores.mips.enums.*
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException.*
import ru.inforion.lab403.kopycat.modules.cores.MipsCore

class COP0v2(core: MipsCore, name: String) : ACOP0(core, name) {
    override fun reset() {
        super.reset()
        regs.EBase = 0x8000_0000
    }

    override fun processInterrupts() {
        super.processInterrupts()

        val DebugDM = 0  // regs.Debug[Debug.DM.pos].asInt
        val StatusIE = regs.Status[Status.IE.pos].asInt
        val StatusEXL = regs.Status[Status.EXL.pos].asInt
        val StatusERL = regs.Status[Status.ERL.pos].asInt
        val Config3VEIC = regs.Config3[Config3.VEIC.pos].asInt

        if (DebugDM == 0 && StatusIE == 1 && StatusEXL == 0 && StatusERL == 0) {
            if (Config3VEIC == 1) {
//                if (regs.Cause[Cause.IP7.pos..Cause.IP2.pos] != 0L)
//                    throw UnsupportedOperationException("Hardware EIC IPL bits shouldn't be here...")

                val IPL = regs.Status[Status.IM7.pos..Status.IM2.pos]
                // TODO: IE set always true for backward MIPS compat. but reconsideration required
                val interrupt = pending(true)
                if (interrupt != null) {
                    val RIPL = interrupt.cause.asULong
                    if (RIPL > IPL) {
                        setIPValue(RIPL)
                        interrupt.onInterrupt()
                        throw MipsHardwareException.INT(core.pc, interrupt)
                    }
                }  // If throw then we need no else

                // Note: Regard to IM1..IM0
                // In implementations of Release 2 of the Architecture in which EIC interrupt mode is enabled
                // (Config3VEIC = 1), these bits are writable, but have no effect on the interrupt system.
                val swInterrupt = when {
                    // TODO: Sort out software interrupts mess
                    regs.Cause[Cause.IP1.pos] == 1L -> TODO() //(dev as PIC32MZ2048EFH144).system.swInterrupt1
                    regs.Cause[Cause.IP0.pos] == 1L -> TODO() //(dev as PIC32MZ2048EFH144).system.swInterrupt0
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
                    insertIPBits(interrupt.cause.asULong)
                    interrupt.onInterrupt()
                }

                // Interrupt taken only if these condition are true
                // see vol.3 MIPS32 (Rev. 0.95) Architecture (5.1)
                val ip = regs.Cause[Cause.IP7.pos..Cause.IP0.pos]
                val im = regs.Status[Status.IM7.pos..Status.IM0.pos]
                // log.config { "IP = %08X IM = %08X IRQ = %08X".format(ip, im, ip and im) }
                if ((ip and im) != 0L) {
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

        val StatusBEV = regs.Status[Status.BEV.pos].asInt
        val StatusEXL = regs.Status[Status.EXL.pos].asInt
        val CauseIV = regs.Cause[Cause.IV.pos].asInt

        if (StatusEXL == 1) {
            vectorOffset = 0x180
        } else {
            if (core.cpu.branchCntrl.isDelaySlot) {
                regs.EPC = core.cpu.pc - 4
                regs.Cause = setBit(regs.Cause, Cause.BD.pos)
            } else {
                regs.EPC = core.cpu.pc
                regs.Cause = clearBit(regs.Cause, Cause.BD.pos)
            }

            val VecNum: Int
            val SRSCtlHSS = regs.SRSCtl[SRSCtl.HSS.range]
            val SRSCtlCSS = regs.SRSCtl[SRSCtl.CSS.range]
            val SRSCtlESS = regs.SRSCtl[SRSCtl.ESS.range]
            val SRSCtlEICSS = regs.SRSCtl[SRSCtl.EICSS.range]

            /* Compute vector offsets as a function of the type of exception */
            var NewShadowSet = SRSCtlESS     /* Assume exception, Release 2 only */
            if (exception is TLBInvalid || exception is TLBMiss || exception is TLBModified) {
                exception as MipsHardwareException
                regs.BadVAddr = exception.vAddr
                regs.EntryHi = regs.EntryHi.insert(exception.vAddr[31..13], 31..13)
                regs.Context = regs.Context.insert(exception.vAddr[31..13], 22..4)
//                log.warning { "[${core.cpu.pc.hex8}] ${exception.excCode} -> BadVAddr = ${exception.vAddr.hex8}" }
                if (exception.vAddr == 0L) {
                    log.severe { "Null-pointer exception occurred... halting CPU core!" }
                    core.debugger.isRunning = false
                }
                vectorOffset = if (exception is TLBMiss) 0x000 else 0x180
            } else if (isInterrupt(exception)) {
                exception as MipsHardwareException.INT
                if (CauseIV == 0) {
                    vectorOffset = 0x180
                } else { // CauseIV = 1
                    // Vector Spacing
                    // 0x00 0x000 0
                    // 0x01 0x020 32
                    // 0x02 0x040 64
                    // 0x04 0x080 128
                    // 0x08 0x100 256
                    // 0x10 0x200 512
                    val IntCtlVS = regs.IntCtl[IntCtl.VS.range].asInt
                    val Config3VEIC = regs.Config3[Config3.VEIC.pos].asInt

                    if (StatusBEV == 1 || IntCtlVS == 0) {
                        vectorOffset = 0x200
                    } else { // StatusBEV = 0
                        if (Config3VEIC == 1) {
                            // 6.1.1.3 External Interrupt Controller Mode
                            // For VEIC = 1 IP field is RIPL (Requested interrupt level)
                            val CauseRIPL = regs.Cause[Cause.IP7.pos..Cause.IP2.pos].asInt

                            VecNum = when {
                                core.EIC_option1 -> CauseRIPL
                                core.EIC_option2 -> exception.interrupt!!.irq // EIC_VecNum_Signal
                                core.EIC_option3 -> -1  // unused
                                else -> throw GeneralException("Wrong EIC options configuration...")
                            }
                            NewShadowSet = SRSCtlEICSS
                        } else { // Config3VEIC = 0
                            VecNum = VIntPriorityEncoder()
                            NewShadowSet = regs.SRSMap[4*VecNum+3..4*VecNum]
                        }

                        if (Config3VEIC == 1 && core.EIC_option3) {
                            vectorOffset = exception.interrupt!!.vector.asInt // EIC_VectorOffset_Signal
                        } else {
                            vectorOffset = 0x200 + VecNum * (IntCtlVS shl 5)
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
            if (core.ArchitectureRevision >= 2 && SRSCtlHSS > 0 && StatusBEV == 0) {
                regs.SRSCtl = regs.SRSCtl.insert(SRSCtlCSS, SRSCtl.PSS.range)
                regs.SRSCtl = regs.SRSCtl.insert(NewShadowSet, SRSCtl.CSS.range)
            }
        } /* if StatusEXL = 1 then */

        // CauseCE <- FaultingCoprocessorNumber
        // CauseExcCode <- ExceptionType
        // StatusEXL <- 1
        if (exception is MipsHardwareException) {
            val code = exception.excCode as ExcCode
            regs.Cause = regs.Cause.insert(code.id, Cause.EXC_H.pos..Cause.EXC_L.pos)
        }
        regs.Status = setBit(regs.Status, Status.EXL.pos)

        /* Calculate the vector base address */
        if (StatusBEV == 1) {
            vectorBase = 0xBFC0_0200
        } else {
            vectorBase = if (core.ArchitectureRevision >= 2) {
                /* The fixed value of EBase31..30 forces the base to be in kseg0 or kseg1 */
                regs.EBase bzero 12..0  // Exception Base
            } else {
                0x8000_0000
            }
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