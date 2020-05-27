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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.stack

import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Popa(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "popa"

    override fun execute() {
        val edi = x86utils.pop(core, prefs.opsize, prefs)
        val esi = x86utils.pop(core, prefs.opsize, prefs)
        val ebp = x86utils.pop(core, prefs.opsize, prefs)
        x86Register.gpr(prefs.opsize, x86GPR.ESP).plus(core, prefs.opsize.bytes)
        val ebx = x86utils.pop(core, prefs.opsize, prefs)
        val edx = x86utils.pop(core, prefs.opsize, prefs)
        val ecx = x86utils.pop(core, prefs.opsize, prefs)
        val eax = x86utils.pop(core, prefs.opsize, prefs)
        x86Register.gpr(prefs.opsize, x86GPR.EDI).value(core, edi)
        x86Register.gpr(prefs.opsize, x86GPR.ESI).value(core, esi)
        x86Register.gpr(prefs.opsize, x86GPR.EBP).value(core, ebp)
        x86Register.gpr(prefs.opsize, x86GPR.EBX).value(core, ebx)
        x86Register.gpr(prefs.opsize, x86GPR.EDX).value(core, edx)
        x86Register.gpr(prefs.opsize, x86GPR.ECX).value(core, ecx)
        x86Register.gpr(prefs.opsize, x86GPR.EAX).value(core, eax)
    }
}