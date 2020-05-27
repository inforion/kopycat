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
package ru.inforion.lab403.kopycat.cores.ppc.instructions.cpu.base.branch

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.toBool
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Branch conditional to link register
class bclrx(core: PPCCore, val options: Int, val condition: Int, val fieldC: Int, val linkage: Boolean):
        APPCInstruction(core, Type.COND_CALL) {
    override val mnem = "bclr${if (linkage) "l" else ""}"

    //Now BH is not used

    override fun execute() {
        if (!options[2].toBool())
            --core.cpu.regs.CTR

        val cia = core.cpu.regs.PC

        val ctr_ok = options[2].toBool() or ((core.cpu.regs.CTR != 0L) xor options[1].toBool())
        val cond_ok = options[4].toBool() or (core.cpu.crBits.bit(condition) == options[3].toBool())
        if (ctr_ok && cond_ok)
            core.cpu.regs.PC = core.cpu.regs.LR and 0xFFFF_FFFC //cut off 2 lsb

        //Not sure, that it really have to be done every time
        if (linkage)
            core.cpu.regs.LR = cia //+ 4 // PC already incremented
    }
}