/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2022 INFORION, LLC
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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.modules.cores.PPCCore


//Branch conditional
class bcx(core: PPCCore, val options: ULong, val condition: Int, val address: ULong, val absolute: Boolean, val linkage: Boolean):
        APPCInstruction(core, Type.COND_CALL) {
    override val mnem = "bc${if (linkage) "l" else ""}${if (absolute) "a" else ""}"

    override fun execute() {
        if (options[2].untruth)
            --core.cpu.regs.CTR

        //Not sure, that it really have to be done every time
        if (linkage)
            core.cpu.regs.LR = core.cpu.regs.PC // + 4 // PC already incremented

        val ctr_ok = options[2].truth or ((core.cpu.regs.CTR != 0uL) xor options[1].truth)
        val cond_ok = options[4].truth or (core.cpu.crBits.bit(condition) == options[3].truth)
        if (ctr_ok && cond_ok) {
            val extAddr = (address shl 2).signextRenameMeAfter(15)
            if (absolute)
                core.cpu.regs.PC = extAddr
            else
                core.cpu.regs.PC += extAddr - 4u // PC already incremented
        }
    }
}