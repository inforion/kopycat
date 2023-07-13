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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.stack

import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.pageFault
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Pusha(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "pusha"

    override fun execute() {
        pageFault(core) {
            (0 until 8).forEach { _ -> push(prefs.opsize, prefs) }
        }

        val eax = core.cpu.regs.gpr(x86GPR.RAX, prefs.opsize).value
        val ecx = core.cpu.regs.gpr(x86GPR.RCX, prefs.opsize).value
        val edx = core.cpu.regs.gpr(x86GPR.RDX, prefs.opsize).value
        val ebx = core.cpu.regs.gpr(x86GPR.RBX, prefs.opsize).value
        val esp = core.cpu.regs.gpr(x86GPR.RSP, prefs.opsize).value
        val ebp = core.cpu.regs.gpr(x86GPR.RBP, prefs.opsize).value
        val esi = core.cpu.regs.gpr(x86GPR.RSI, prefs.opsize).value
        val edi = core.cpu.regs.gpr(x86GPR.RDI, prefs.opsize).value
        x86utils.push(core, eax, prefs.opsize, prefs)
        x86utils.push(core, ecx, prefs.opsize, prefs)
        x86utils.push(core, edx, prefs.opsize, prefs)
        x86utils.push(core, ebx, prefs.opsize, prefs)
        x86utils.push(core, esp, prefs.opsize, prefs)
        x86utils.push(core, ebp, prefs.opsize, prefs)
        x86utils.push(core, esi, prefs.opsize, prefs)
        x86utils.push(core, edi, prefs.opsize, prefs)
    }
}