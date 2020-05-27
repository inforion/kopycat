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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.control

import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core



class Lahf(core: x86Core, opcode: ByteArray, prefs: Prefixes): AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "lahf"
    override fun execute() {
        val flags = core.cpu.flags
        val value = (if(flags.sf) 1L.shl(7) else 0) or
                    (if(flags.zf) 1L.shl(6) else 0) or
                    (if(flags.af) 1L.shl(4) else 0) or
                    (if(flags.pf) 1L.shl(2) else 0) or
                    (if(flags.cf) 1L.shl(0) else 0) or
                    0b00000010
        core.cpu.regs.ah = value
    }
}