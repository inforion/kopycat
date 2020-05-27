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
package ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl

import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.cores.ppc.operands.SPR
import ru.inforion.lab403.kopycat.cores.ppc.operands.systems.PPCRegister_EmbeddedMMUFSL



enum class eSPR_EmbeddedMMUFSL(val id: Int,
                       val dest: PPCRegister,
                       val moveTo: SPR.Access,
                       val moveFrom: SPR.Access) {


    //MMU assist registers
    MAS0(eOEA_EmbeddedMMUFSL.MAS0.id, PPCRegister_EmbeddedMMUFSL.OEAext.MAS0, SPR.Access.yes, SPR.Access.yes),
    MAS1(eOEA_EmbeddedMMUFSL.MAS1.id, PPCRegister_EmbeddedMMUFSL.OEAext.MAS1, SPR.Access.yes, SPR.Access.yes),
    MAS2(eOEA_EmbeddedMMUFSL.MAS2.id, PPCRegister_EmbeddedMMUFSL.OEAext.MAS2, SPR.Access.yes, SPR.Access.yes),
    MAS3(eOEA_EmbeddedMMUFSL.MAS3.id, PPCRegister_EmbeddedMMUFSL.OEAext.MAS3, SPR.Access.yes, SPR.Access.yes),
    MAS4(eOEA_EmbeddedMMUFSL.MAS4.id, PPCRegister_EmbeddedMMUFSL.OEAext.MAS4, SPR.Access.yes, SPR.Access.yes),
    MAS6(eOEA_EmbeddedMMUFSL.MAS6.id, PPCRegister_EmbeddedMMUFSL.OEAext.MAS6, SPR.Access.yes, SPR.Access.yes),
    MAS7(eOEA_EmbeddedMMUFSL.MAS7.id, PPCRegister_EmbeddedMMUFSL.OEAext.MAS7, SPR.Access.yes, SPR.Access.yes),

    PID1(eOEA_EmbeddedMMUFSL.PID1.id, PPCRegister_EmbeddedMMUFSL.OEAext.PID1, SPR.Access.yes, SPR.Access.yes),
    PID2(eOEA_EmbeddedMMUFSL.PID2.id, PPCRegister_EmbeddedMMUFSL.OEAext.PID2, SPR.Access.yes, SPR.Access.yes),

    //MMU control and status(Read/Write)
    MMUCSR0(eOEA_EmbeddedMMUFSL.MMUCSR0.id, PPCRegister_EmbeddedMMUFSL.OEAext.MMUCSR0, SPR.Access.yes, SPR.Access.yes);

    /*
    TLB0CFG(688),
    TLB1CFG(689),
    TLB2CFG(690),
    TLB3CFG(691),

    MMUCFG(1015)
    * */

    fun toSPR() : SPR = SPR(this.name, this.id, this.dest, this.moveTo, this.moveFrom)

    companion object {
        fun toList() : List<SPR> = values().map { it.toSPR() }
    }
}