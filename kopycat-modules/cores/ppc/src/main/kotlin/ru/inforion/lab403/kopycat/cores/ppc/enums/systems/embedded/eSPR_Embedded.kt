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
package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded

import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.cores.ppc.operands.SPR
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_Embedded



enum class eSPR_Embedded(val id: Int,
                               val dest: PPCRegister,
                               val moveTo: SPR.Access,
                               val moveFrom: SPR.Access) {

    // Interrupt registers

    // Interrupt vector prefix
    IVPR(eOEA_Embedded.IVPR.id, PPCRegister_Embedded.OEAext.IVPR, SPR.Access.yes, SPR.Access.yes),

    // Process ID register 0
    PID0(eOEA_Embedded.PID0.id, PPCRegister_Embedded.OEAext.PID0, SPR.Access.yes, SPR.Access.yes),

    // Timer status register
    TSR(eOEA_Embedded.TSR.id, PPCRegister_Embedded.OEAext.TSR, SPR.Access.yes, SPR.Access.yes),

    // Timer control
    TCR(eOEA_Embedded.TCR.id, PPCRegister_Embedded.OEAext.TCR, SPR.Access.yes, SPR.Access.yes),

    // Decrementer auto-reload register
    DECAR(eOEA_Embedded.DECAR.id, PPCRegister_Embedded.OEAext.DECAR, SPR.Access.yes, SPR.Access.no);

    fun toSPR() : SPR = SPR(this.name, this.id, this.dest, this.moveTo, this.moveFrom)

    companion object {
        fun toList() : List<SPR> = values().map { it.toSPR() }
    }
}