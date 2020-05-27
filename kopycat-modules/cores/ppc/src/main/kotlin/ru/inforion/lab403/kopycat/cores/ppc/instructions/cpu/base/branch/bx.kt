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

import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.kopycat.cores.ppc.instructions.APPCInstruction
import ru.inforion.lab403.kopycat.cores.ppc.instructions.ssext
import ru.inforion.lab403.kopycat.modules.cores.PPCCore



//Branch
class bx(core: PPCCore, val address: Long, val absolute: Boolean, val linkage: Boolean):
        APPCInstruction(core, Type.CALL) {
    override val mnem = "b${if (linkage) "l" else ""}${if (absolute) "a" else ""}"
    override fun toString(): String = "$mnem ${address.hex8}"

    override fun execute() {
        if (linkage)
            core.cpu.regs.LR = core.cpu.regs.PC // + 4 // PC already incremented

        val extAddr = (address shl 2).ssext(25)
        if (absolute)
            core.cpu.regs.PC = extAddr
        else
            core.cpu.regs.PC += extAddr - 4 // PC already incremented
    }
}