/*
 *
 * This file is part of Kopycat emulator software.
 *
 * Copyright (C) 2023 INFORION, LLC
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

import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.pageFault
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Popa(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "popa"

    override fun execute() {
        pageFault(core) {
            (0 until 3).forEach { _ -> pop(prefs.opsize, prefs) }
            core.cpu.regs.gpr(x86GPR.RSP, prefs.opsize).value += prefs.opsize.bytes.ulong_z
            (0 until 4).forEach { _ -> pop(prefs.opsize, prefs) }
        }

        val edi = x86utils.pop(core, prefs.opsize, prefs)
        val esi = x86utils.pop(core, prefs.opsize, prefs)
        val ebp = x86utils.pop(core, prefs.opsize, prefs)
        core.cpu.regs.gpr(x86GPR.RSP, prefs.opsize).value += prefs.opsize.bytes.ulong_z
        val ebx = x86utils.pop(core, prefs.opsize, prefs)
        val edx = x86utils.pop(core, prefs.opsize, prefs)
        val ecx = x86utils.pop(core, prefs.opsize, prefs)
        val eax = x86utils.pop(core, prefs.opsize, prefs)
        core.cpu.regs.gpr(x86GPR.RDI, prefs.opsize).value = edi
        core.cpu.regs.gpr(x86GPR.RSI, prefs.opsize).value = esi
        core.cpu.regs.gpr(x86GPR.RBP, prefs.opsize).value = ebp
        core.cpu.regs.gpr(x86GPR.RBX, prefs.opsize).value = ebx
        core.cpu.regs.gpr(x86GPR.RDX, prefs.opsize).value = edx
        core.cpu.regs.gpr(x86GPR.RCX, prefs.opsize).value = ecx
        core.cpu.regs.gpr(x86GPR.RAX, prefs.opsize).value = eax
    }
}