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
package ru.inforion.lab403.kopycat.cores.ppc.enums.systems

import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.cores.ppc.operands.SPR
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_SPE





enum class eSPR_SPE(val id: Int,
                    val dest: PPCRegister,
                    val moveTo: SPR.Access,
                    val moveFrom: SPR.Access) {

    // Signal Processing
    //Accumulator(eUISA_SPE.Accumulator.id, PPCRegister_SPE.UISAext.Accumulator, SPR.Access.yes, SPR.Access.yes),
    SPEFSCR(eUISA_SPE.SPEFSCR.id, PPCRegister_SPE.UISAext.SPEFSCR, SPR.Access.yes, SPR.Access.yes);

    fun toSPR() : SPR = SPR(this.name, this.id, this.dest, this.moveTo, this.moveFrom)

    companion object {
        fun toList() : List<SPR> = values().map { it.toSPR() }
    }

}