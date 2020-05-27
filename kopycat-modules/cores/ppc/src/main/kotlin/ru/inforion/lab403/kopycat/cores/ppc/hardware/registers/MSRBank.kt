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
package ru.inforion.lab403.kopycat.cores.ppc.hardware.registers

import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.common.extensions.toInt
import ru.inforion.lab403.kopycat.cores.base.abstracts.ARegistersBank
import ru.inforion.lab403.kopycat.cores.ppc.enums.eMSR
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



class MSRBank(core : PPCCore) : ARegistersBank<PPCCore, eMSR>(core, arrayOf(), bits = 32) {
    override val name: String = "MSR Register"

    //Computation mode (0 - 32 bit, 1 - 64 bit)
    var CM by bitOf(PPCRegister.OEA.MSR, eMSR.CM.bit)

    //30 - reserved
    //29 - implementation-dependent

    //Guest state
    var GS by bitOf(PPCRegister.OEA.MSR, eMSR.GS.bit)

    //27 - implementation-dependent

    //User cache locking enable (0 - cache locking's are privileged, 1 - can be exec in user mode)
    var UCLE by bitOf(PPCRegister.OEA.MSR, eMSR.UCLE.bit)

    //SP/Embedded floating-point/Vector available
    var SPV by bitOf(PPCRegister.OEA.MSR, eMSR.SPV.bit)

    //24 - reserved

    //VSX available (1 - the thread can execute VSX instructions)
    var VSX by bitOf(PPCRegister.OEA.MSR, eMSR.VSX.bit)

    //22-18 - reserved

    //Critical enable (1 - critical interrupts are enabled)
    var CE by bitOf(PPCRegister.OEA.MSR, eMSR.CE.bit)

    //16 - reserved

    //External enable (1 - external interrupts are enabled)
    var EE by bitOf(PPCRegister.OEA.MSR, eMSR.EE.bit)

    //Problem state (0 - the thread is in privileged state, 1 - in problem state)
    var PR by bitOf(PPCRegister.OEA.MSR, eMSR.PR.bit)

    //Floating-point available (1 - the thread can execute floating-point instructions)
    var FP by bitOf(PPCRegister.OEA.MSR, eMSR.FP.bit)

    //Machine check enable (1 - machine check interrupts are enabled)
    var ME by bitOf(PPCRegister.OEA.MSR, eMSR.ME.bit)

    //Floating-point exception mode 0
    var FE0 by bitOf(PPCRegister.OEA.MSR, eMSR.FE0.bit)

    //10 - implementation-dependent

    //Debug interrupt enable (1 - debug interrupts are enabled if DBCR0[IDM] == 1)
    var DE by bitOf(PPCRegister.OEA.MSR, eMSR.DE.bit)

    //Floating-point exception mode 1
    var FE1 by bitOf(PPCRegister.OEA.MSR, eMSR.FE1.bit)

    //7 - reserved
    //6 - reserved

    //Instruction address space
    var IS by bitOf(PPCRegister.OEA.MSR, eMSR.IS.bit)

    //Data address space
    var DS by bitOf(PPCRegister.OEA.MSR, eMSR.DS.bit)

    //3 - implementation-dependent

    //Performance monitor mark (1 - enable statistics gathering on marked processes)
    var PMM by bitOf(PPCRegister.OEA.MSR, eMSR.PMM.bit)

    //1 - reserved
    //0 - reserved

    //Bits operations
    fun bit(index: Int, value: Boolean) = PPCRegister.OEA.MSR.bit(core, index, value.toInt())
    fun bit(index: Int) = PPCRegister.OEA.MSR.bit(core, index).toBool()
    fun bits(range: IntRange, value: Long) = PPCRegister.OEA.MSR.bits(core, range, value)
    fun bits(range: IntRange) = PPCRegister.OEA.MSR.bits(core, range)
}