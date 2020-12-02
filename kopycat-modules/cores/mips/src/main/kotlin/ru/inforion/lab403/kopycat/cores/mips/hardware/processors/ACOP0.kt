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

import ru.inforion.lab403.common.extensions.asInt
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.kopycat.cores.base.GenericSerializer
import ru.inforion.lab403.kopycat.cores.base.abstracts.ACOP
import ru.inforion.lab403.kopycat.cores.base.enums.AccessAction
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.mips.enums.ExcCode
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException
import ru.inforion.lab403.kopycat.cores.mips.exceptions.MipsHardwareException.*
import ru.inforion.lab403.kopycat.cores.mips.hardware.registers.CPRBank
import ru.inforion.lab403.kopycat.cores.mips.hardware.registers.RSVDBank
import ru.inforion.lab403.kopycat.modules.cores.MipsCore
import ru.inforion.lab403.kopycat.serializer.loadValue
import java.util.logging.Level


abstract class ACOP0(core: MipsCore, name: String) : ACOP<ACOP0, MipsCore>(core, name) {
    companion object {
        @Transient val log = logger(Level.INFO)
    }

    val cntrls = RSVDBank()
    val regs = CPRBank(core)

    override fun createException(name: String, where: Long, vAddr: Long, action: AccessAction) = when (name) {
        "TLBInvalid" -> TLBInvalid(action, where, vAddr)
        "TLBMiss" -> TLBMiss(action, where, vAddr)
        "TLBModified" -> TLBModified(where, vAddr)
        else -> throw IllegalArgumentException("Exception $name not implemented here!")
    }

    fun setCountCompareTimerBits(oldCnt: Long, newCnt: Long) {
        if (regs.Compare.value in oldCnt until newCnt) {
            if (core.ArchitectureRevision > 1) {
                val IPTI = regs.IntCtl.IPTI.asInt
                if (IPTI >= 2) {
                    regs.Cause.IP7_0 = regs.Cause.IP7_0 or (1L shl IPTI)
                    regs.Cause.TI = true
                }
            } else {
                regs.Cause.IP7 = true
            }
        }
    }

    fun clearCountCompareTimerBits() {
        if (core.ArchitectureRevision > 1) {
            regs.Cause.TI = false
            val IPTI = regs.IntCtl.IPTI.asInt
            if (IPTI >= 2) {
                regs.Cause.IP7_0 = regs.Cause.IP7_0 and (1L shl IPTI).inv()
            }
        } else {
            regs.Cause.IP7 = false
        }
    }

    // Because instructionPerCycle is float and may be < 1.0 then Count register won't be grow
    private var countCompareCycles = 0.0
    private val countCompareInc = core.countRateFactor * core.ipc

    override fun processInterrupts() {
        /*
        The Compare register acts in conjunction with the Count register to implement a timer and timer interrupt function.

        The Compare register maintains a stable value and does not change on its own.

        When the value of the Count register equals the value of the Compare register, an interrupt request is made. In
        Release 1 of the architecture, this request is combined in an implementation-dependent way with hardware interrupt 5
        to set interrupt bit IP(7) in the Cause register. In Release 2 (and subsequent releases) of the Architecture, the presence
        of the interrupt is visible to software via the CauseTI bit and is combined in an implementation-dependent way with a
        hardware or software interrupt. For Vectored Interrupt Mode, the interrupt is at the level specified by the IntCtlIPTI
        field.

        For diagnostic purposes, the Compare register is a read/write register. In normal use however, the Compare register is
        write-only. Writing a value to the Compare register, as a side effect, clears the timer interrupt. Figure 9.31 shows the
        format of the Compare register; Table 9.46 describes the Compare register fields.
         */

        // i.e ipc = 0.4 and regs.Count = 8000
        // countCompareCycles += 0.4, = 0.4  // first interrupt proc.
        // countCompareCycles += 0.4, = 0.8  // second
        // countCompareCycles += 0.4, = 1.2  // third
        //   decimal = 1
        //   countCompareCycles = 0.2
        //   oldCnt = 8000
        //   newCnt = 8001
        //   regs.Count = 8001
        countCompareCycles += countCompareInc

        if (countCompareCycles >= 1) {
            val decimal = countCompareCycles.asInt
            countCompareCycles -= decimal

            val oldCnt = regs.Count.value
            val newCnt = oldCnt + decimal

            regs.Count.value = newCnt

            // Due to countCompareCycles >= 1 -> oldCnt != newCnt
            if (core.countCompareSupported)
                setCountCompareTimerBits(oldCnt, newCnt)
        }
    }

    /* =============================== Interrupt support mechanism =============================== */

    protected fun VIntPriorityEncoder(): Int {
        // See Table 6.3 Relative Interrupt Priority for Vectored Interrupt Mode
        if (regs.Cause.IP7 && !regs.Status.IM7) {  // Hardware interrupt 5
            regs.Cause.IP7 = false
            return 7 // 0x1000
        } else if (regs.Cause.IP6 && !regs.Status.IM6) {  // Hardware interrupt 4
            regs.Cause.IP6 = false
            return 6 // 0x0E00
        } else if (regs.Cause.IP5 && !regs.Status.IM5) {  // Hardware interrupt 3
            regs.Cause.IP5 = false
            return 5 // 0x0C00
        } else if (regs.Cause.IP4 && !regs.Status.IM4) {  // Hardware interrupt 2
            regs.Cause.IP4 = false
            return 4 // 0x0A00
        } else if (regs.Cause.IP3 && !regs.Status.IM3) {  // Hardware interrupt 1
            regs.Cause.IP3 = false
            return 3 // 0x0800
        } else if (regs.Cause.IP2 && !regs.Status.IM2) {  // Hardware interrupt 0
            regs.Cause.IP2 = false
            return 2 // 0x0600
        } else if (regs.Cause.IP1 && !regs.Status.IM1) {  // Software interrupt 1
            regs.Cause.IP1 = false
            return 1 // 0x0400
        } else if (regs.Cause.IP0 && !regs.Status.IM0) {  // Software interrupt 0
            regs.Cause.IP0 = false
            return 0 // 0x0200
        }
        throw GeneralException("Wrong IP7:IP0 value: %02X".format(regs.Cause.IP7_0))
    }

    protected fun ShadowSetEncoder(vecNum: Int) = when (vecNum) {
        0 -> regs.SRSMap.SSV0
        1 -> regs.SRSMap.SSV1
        2 -> regs.SRSMap.SSV2
        3 -> regs.SRSMap.SSV3
        4 -> regs.SRSMap.SSV4
        5 -> regs.SRSMap.SSV5
        6 -> regs.SRSMap.SSV6
        7 -> regs.SRSMap.SSV7
        else -> throw GeneralException("Wrong vecNum = $vecNum value for shadow set!")
    }

    protected fun isTlbRefill(exception: HardwareException): Boolean = exception is MipsHardwareException.TLBMiss

    protected fun isInterrupt(exception: HardwareException): Boolean = exception.excCode == ExcCode.INT

    /* =============================== Overridden methods =============================== */

    override fun reset() {
        super.reset()
        regs.reset()
    }

    override fun stringify() = buildString {
        appendLine("COP0:")
        append(regs.stringify())
    }

    override fun serialize(ctxt: GenericSerializer): Map<String, Any> {
        return mapOf(
                "name" to name,
                "regs" to regs.serialize(ctxt),
                "cntrls" to cntrls.serialize(ctxt),
                "countCompareCycles" to countCompareCycles
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(ctxt: GenericSerializer, snapshot: Map<String, Any>) {
        val snapsnotName = snapshot["name"] as String
        if (name != snapsnotName) {
            throw IllegalStateException("Wrong module name %s != %s".format(name, snapsnotName))
        }
        regs.deserialize(ctxt, snapshot["regs"] as Map<String, Any>)
        cntrls.deserialize(ctxt, snapshot["cntrls"] as Map<String, Any>)
        countCompareCycles = loadValue(snapshot, "countCompareCycles")
    }
}