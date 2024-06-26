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
package ru.inforion.lab403.kopycat.cores.mips.hardware.processors.mips64

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.base.exceptions.MemoryAccessError
import ru.inforion.lab403.kopycat.cores.mips.enums.ExcCode
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException.*
import ru.inforion.lab403.kopycat.cores.mips.hardware.processors.ACOP0
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import kotlin.math.max

/**
 * System Control Coprocessor
 *
 * CP0 преобразует виртуальные адреса в физические, управляет исключениями
 * и обрабатывает переключение между режимами ядра, супервизора и пользователя.
 * Управляет подсистемой кэша и предоставляет средства контроля ошибок.
 * */

class COP064(core: MipsCore, name: String) : ACOP0(core, name) {
    override fun reset() {
        super.reset()
        regs.EBase.value = 0xFFFF_FFFF_8000_0000uL // p 250 PRA
    }

    override fun processInterrupts() {
        val countCompareInterrupt = super.processCountCompare()

        // Vol III chapter 6 p.80
        // An interrupt is only taken when all of the following are true:
        // - A specific request for interrupt service is made, as a function of the interrupt mode, described bel w.
        // - The IE bit in the Status register is a one.
        // - The DM bit in the Debug register is a zero (for processors implementing EJTAG)
        // - The EXL and ERL bits in the Status register are both zero
        val debugDM = false // regs.Debug.DM
        if (regs.Status.IE && !debugDM && !regs.Status.EXL && !regs.Status.ERL) {
            if (regs.Config3.VEIC) {
                TODO("EIC")
            }

            val unmaskedCountCompareCause = if (countCompareInterrupt) {
                val cause = countCompareCause
                if (cause != null && regs.Status.IM7_0[cause].truth) {
                    cause
                } else {
                    null
                }
            } else {
                null
            }

            val unmaskedPendingInterrupt = pending(true) { regs.Status.IM7_0[it.cause].truth }
            if (unmaskedPendingInterrupt?.cause == null && unmaskedCountCompareCause == null) {
                regs.Cause.value = 0uL
                return
            }

            val highestPriorityCause = max(
                unmaskedPendingInterrupt?.cause ?: -1,
                unmaskedCountCompareCause ?: -1
            )

            if (highestPriorityCause == unmaskedCountCompareCause) {
                regs.Cause.value = 0uL
                raiseCountCompareCause()
            } else {
                unmaskedPendingInterrupt?.apply {
                    pending = false
                    inService = true
                }
                regs.Cause.IP7_0 = 0uL set highestPriorityCause
            }

            handleException(INT(core.pc, highestPriorityCause, 0 /* TODO: EIC */))
        }
    }

    /**
     * EIC - external interrupt controller
     * p. 102 PRA ???
     */
    override fun handleException(exception: GeneralException?): GeneralException? {
        if (exception !is HardwareException)
            return exception

        // See MIPS® Architecture For Programmers
        // p. 101 Vol. III: MIPS64® Privileged Resource Architecture
        //log.info { "Exception, pc=0x${core.pc.hex}; $exception" }

        if (exception is MemoryAccessError) {
            throw exception
        }

        val vectorOffset: Int
        val vectorBase: ULong

        val StatusBEV = regs.Status.BEV
        val StatusEXL = regs.Status.EXL
        val CauseIV = regs.Cause.IV

        if (StatusEXL) {
            vectorOffset = 0x180
        } else {
            if (core.cpu.branchCntrl.isDelaySlot) {
                regs.EPC.value = core.cpu.pc - 4u
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

                regs.EntryHi.run {
                    ASID = if (regs.Config5.mi) 0u else regs.EntryHi.ASID
                    VPN2X = 0uL
                    VPN2 = 0uL
                    value = value.insert(exception.vAddr[63..13], 63..13) and core.segmask
                }

                regs.Context.BadVPN2 = exception.vAddr[31..13]

                regs.XContext.run {
                    R = exception.vAddr[63..62]
                    BadVPN2 = exception.vAddr[core.SEGBITS - 1 .. 13]
                    zero = 0u
                }

//                log.warning { "[${core.cpu.pc.hex8}] ${exception.excCode} -> BadVAddr = ${exception.vAddr.hex16}" }
                if (exception.vAddr == 0uL) {
                    log.severe { "Null-pointer exception occurred... halting CPU core!" }
                    // It's ok. Linux kernel is doing that:)
//                    core.debugger.isRunning = false
                }

                vectorOffset = when (exception) {
                    // TODO: а если это 32-битный TLBRefill?
                    is TLBMiss -> 0x080 // aka XTLBRefill
                    else -> 0x180
                }
            } else if (isInterrupt(exception)) {
                exception as INT
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
                    val IntCtlVS = regs.IntCtl.VS.int
                    val Config3VEIC = regs.Config3.VEIC

                    if (StatusBEV || IntCtlVS == 0) {
                        vectorOffset = 0x200
                    } else { // StatusBEV = 0
                        if (Config3VEIC) {
                            // 6.1.1.3 External Interrupt Controller Mode
                            // For VEIC = 1 IP field is RIPL (Requested interrupt level)
                            val CauseRIPL = regs.Cause.IP7_2.int

                            VecNum = when {
                                core.EIC_option1 -> CauseRIPL
                                core.EIC_option2 -> exception.irq!! // EIC_VecNum_Signal
                                core.EIC_option3 -> -1  // unused
                                else -> throw GeneralException("Wrong EIC options configuration...")
                            }
                            NewShadowSet = SRSCtlEICSS
                        } else { // Config3VEIC = 0
                            VecNum = VIntPriorityEncoder()
                            NewShadowSet = ShadowSetEncoder(VecNum)
                        }
                        vectorOffset = if (Config3VEIC && core.EIC_option3) {
                            exception.vector!! // EIC_VectorOffset_Signal
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
            if (core.ArchitectureRevision >= 2 && SRSCtlHSS > 0u && !StatusBEV) {
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

        /* Calculate the vector base address, p.102 PRA */
        vectorBase = when {
            StatusBEV -> 0xFFFF_FFFF_BFC0_0200uL
            else -> when {
                core.ArchitectureRevision < 2 -> 0xFFFF_FFFF_8000_0000uL
                else -> cat(0xFFFF_FFFFuL, (regs.EBase.value bzero 12..0), 31)
            }
        }

        /* Exception PC is the sum of vectorBase and vectorOffset. Vector */
        /* offsets > 0xFFF (vectored or EIC interrupts only), require */
        /* that EBase15..12 have zeros in each bit position less than or */
        /* equal to the most significant bit position of the vector offset */
        val offset = vectorBase[29..0] + vectorOffset[29..0]  /* No carry between bits 29 and 30 */
        val PC = cat(vectorBase[63..30], offset, 29)
        core.cpu.branchCntrl.setIp(PC)

        //log.info { "New PC: ${PC.hex16}" }
        return null
    }
}
