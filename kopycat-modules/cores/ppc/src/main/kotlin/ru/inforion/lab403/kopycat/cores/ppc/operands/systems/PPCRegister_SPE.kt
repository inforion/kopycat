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
import ru.inforion.lab403.kopycat.cores.ppc.enums.systems.eUISA_SPE
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



abstract class PPCRegister_SPE(
        reg: Int,
        rtyp: Regtype,
        access: AOperand.Access = AOperand.Access.ANY) :
        PPCRegister(reg, rtyp, access) {

    override fun toString() = when (rtyp) {
        Regtype.SPE -> first<eUISA_SPE> { it.id == reg }.name
        else -> super.toString()
    }.toLowerCase()

    sealed class UISAext(id: Int) : PPCRegister_SPE(id, Regtype.SPE) {
        override fun value(core: PPCCore): Long = core.cpu.sprRegs.readIntern(reg)
        override fun value(core: PPCCore, data: Long) = core.cpu.sprRegs.writeIntern(reg, data)

        open class REG_DBG_DENIED(id: Int) : UISAext(id) {
            override fun value(core: PPCCore) = denied_read(reg)
            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }

        open class REG_DBG_READ(id: Int) : UISAext(id) {
            override fun value(core: PPCCore, data: Long) = denied_write(reg)
        }

        open class REG_DBG_WRITE(id: Int) : UISAext(id) {
            override fun value(core: PPCCore) = denied_read(reg)
        }

        //object Accumulator : REG_DBG_DENIED(eUISA_SPE.Accumulator.id)

        object SPEFSCR : UISAext(eUISA_SPE.SPEFSCR.id) {

            override fun value(core: PPCCore): Long {
                log.warning { "Read from SPEFSCR" }
                return super.value(core)
            }

            override fun value(core: PPCCore, data: Long) {
                super.value(core, data)
                log.warning { "Write to SPEFSCR" }
            }
        }
    }
}