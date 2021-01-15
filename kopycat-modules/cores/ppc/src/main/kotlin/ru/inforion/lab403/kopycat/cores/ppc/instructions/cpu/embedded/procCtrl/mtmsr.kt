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
package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.embedded.procCtrl

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.operands.PPCRegister
import ru.inforion.lab403.kopycat.modules.cores.PPCCore


//Move to special purpose register
class mtmsr(core: PPCCore, val fieldA: Int, val fieldB: Int, val fieldC: Int, val flag: Boolean):
        APPCInstruction(core, Type.VOID) {
    override val mnem = "mtmsr"

    val rs = PPCRegister.gpr(fieldA)
    val L = fieldB[0]

    override fun execute() {
        val value = rs.value(core)
        if (L == 0) {
            core.cpu.msrBits.EE = value[15].toBool() || value[14].toBool()
            core.cpu.msrBits.IS = value[5].toBool() || value[14].toBool()
            core.cpu.msrBits.DS = value[4].toBool() || value[14].toBool()
            core.cpu.msrBits.bits(31..16, value[31..16])
            core.cpu.msrBits.bits(14..13, value[14..13])
            core.cpu.msrBits.bits(11..6, value[11..6])
            core.cpu.msrBits.bits(3..1, value[3..1])
        }
        else {
            core.cpu.msrBits.bits(15..1, value[15..1])
        }
    }
}