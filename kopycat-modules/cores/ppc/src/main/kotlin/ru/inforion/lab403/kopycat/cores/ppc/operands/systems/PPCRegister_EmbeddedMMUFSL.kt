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
package ru.inforion.lab403.kopycat.cores.ppc.operands.systems

import ru.inforion.lab403.common.extensions.first
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.ppc.enums.Regtype
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.embedded.mmufsl.eOEA_EmbeddedMMUFSL
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore

abstract class PPCRegister_EmbeddedMMUFSL(
        reg: Int,
        rtyp: Regtype,
        access: AOperand.Access = AOperand.Access.ANY) :
        PPCRegister(reg, rtyp, access) {

    override fun toString() = when (rtyp) {
        Regtype.EmbeddedMMUFSL -> first<eOEA_EmbeddedMMUFSL> { it.id == reg }.name
        else -> super.toString()
    }.toLowerCase()

    sealed class OEAext(id: Int) : PPCRegister_EmbeddedMMUFSL(id, Regtype.EmbeddedMMUFSL) {
        override fun value(core: PPCCore): Long = core.cpu.sprRegs.readIntern(reg)
        override fun value(core: PPCCore, data: Long) = core.cpu.sprRegs.writeIntern(reg, data)

        open class REG_DBG_DENIED(id: Int) : OEAext(id) {
            override fun value(core: PPCCore) = denied_read(reg)
            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }

        open class REG_DBG_READ(id: Int) : OEAext(id) {
            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }

        open class REG_DBG_WRITE(id: Int) : OEAext(id) {
            override fun value(core: PPCCore) = denied_read(reg)
        }

        //MMU control and status
        //No need because we don't use
        //Waiting for read point
        object MMUCSR0 : REG_DBG_WRITE(eOEA_EmbeddedMMUFSL.MMUCSR0.id) {
            override fun value(core: PPCCore, data: Long) {
                core.mmu.MMUCSR0Update(data)
                //super.value(core, value) //We don't change value, only apply MMU commands
            }
        }

        //MMU assist registers
        object MAS0 : OEAext(eOEA_EmbeddedMMUFSL.MAS0.id)
        object MAS1 : OEAext(eOEA_EmbeddedMMUFSL.MAS1.id)
        object MAS2 : OEAext(eOEA_EmbeddedMMUFSL.MAS2.id)
        object MAS3 : OEAext(eOEA_EmbeddedMMUFSL.MAS3.id)
        object MAS4 : OEAext(eOEA_EmbeddedMMUFSL.MAS4.id)
        object MAS6 : OEAext(eOEA_EmbeddedMMUFSL.MAS6.id)
        object MAS7 : OEAext(eOEA_EmbeddedMMUFSL.MAS7.id)

        //Process ID registers
        object PID1 : OEAext(eOEA_EmbeddedMMUFSL.PID1.id)
        object PID2 : OEAext(eOEA_EmbeddedMMUFSL.PID2.id)


        /*
        TLB0CFG(688),
        TLB1CFG(689),
        TLB2CFG(690),
        TLB3CFG(691),

        MMUCFG(1015)
        * */

    }

}
