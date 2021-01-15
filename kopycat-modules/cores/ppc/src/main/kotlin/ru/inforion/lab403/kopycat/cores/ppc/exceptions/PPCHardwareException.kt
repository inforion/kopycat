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
package ru.inforion.lab403.kopycat.cores.ppc.exceptions

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.exceptions.HardwareException
import ru.inforion.lab403.kopycat.cores.ppc.enums.eIrq
import ru.inforion.lab403.kopycat.cores.ppc.enums.eMSR
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_Embedded
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_e500v2
import ru.inforion.lab403.kopycat.modules.cores.PPCCore


abstract class PPCHardwareException(
        irq: eIrq,
        where: Long,
        val what: String = "undefined"): HardwareException(irq, where) {


    fun address(core: PPCCore): Long = PPCRegister_Embedded.OEAext.IVPR.value(core)[31..16] or when (excCode as eIrq) {
        eIrq.CriticalInput -> PPCRegister_e500v2.OEAext.IVOR0           // Ignored: no information
        eIrq.MachineCheck -> PPCRegister_e500v2.OEAext.IVOR1            // Ignored: no need
        eIrq.DataStorage -> PPCRegister_e500v2.OEAext.IVOR2             // Implemented
        eIrq.InstStorage -> PPCRegister_e500v2.OEAext.IVOR3             // Implemented
        eIrq.ExternalInput -> PPCRegister_e500v2.OEAext.IVOR4           // Ignored: no need
        eIrq.Alignment -> PPCRegister_e500v2.OEAext.IVOR5               // Ignored: no need
        eIrq.Program -> PPCRegister_e500v2.OEAext.IVOR6
        eIrq.FPUnavailable -> PPCRegister_e500v2.OEAext.IVOR7
        eIrq.SystemCall -> PPCRegister_e500v2.OEAext.IVOR8              // Not implemented yet
        eIrq.APUnavailable -> PPCRegister_e500v2.OEAext.IVOR9
        eIrq.Decrementer -> PPCRegister_e500v2.OEAext.IVOR10            // Not implemented yet
        eIrq.FIT -> PPCRegister_e500v2.OEAext.IVOR11
        eIrq.Watchdog -> PPCRegister_e500v2.OEAext.IVOR12
        eIrq.DataTLBError -> PPCRegister_e500v2.OEAext.IVOR13           // Implemented
        eIrq.InstTLBError -> PPCRegister_e500v2.OEAext.IVOR14           // Implemented
        eIrq.Debug -> PPCRegister_e500v2.OEAext.IVOR15
        eIrq.SPEEmbedded -> PPCRegister_e500v2.OEAext.IVOR32
        eIrq.EmbeddedFPData -> PPCRegister_e500v2.OEAext.IVOR33
        eIrq.EmbeddedFPRound -> PPCRegister_e500v2.OEAext.IVOR34
        eIrq.EmbeddedPerfMonitor -> PPCRegister_e500v2.OEAext.IVOR35
        //eIrq.ProcessorDoorbell.irq -> PPCRegister_e500v2.OEAext.IVOR36
        //eIrq.ProcessorCritDoorbell.irq -> PPCRegister_e500v2.OEAext.IVOR37
        else -> throw GeneralException("Wrong interrupt irq ${excCode.irq}")
    }.value(core) and 0xFFFF_FFF0 // Clear lower 4 bit

    override fun toString() = "$prefix[${where.hex8}] $what"
    open fun interrupt(core: PPCCore) {
        val msr = PPCRegister.OEA.MSR.value(core)
        PPCRegister.OEA.SRR0.value(core, where)
        PPCRegister.OEA.SRR1.value(core, msr)

        PPCRegister.OEA.MSR.value(core, 0L)
        core.cpu.msrBits.CE = msr[eMSR.CE.bit].toBool()
        core.cpu.msrBits.ME = msr[eMSR.ME.bit].toBool()
        core.cpu.msrBits.DE = msr[eMSR.DE.bit].toBool()
        core.pc = address(core)
    }
}