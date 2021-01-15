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

import ru.inforion.lab403.common.extensions.toULong
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.x86utils
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Popf(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "popf"

    override fun execute() {
        // Dunno what to do with it...
        if (x86Register.eflags.vm(core)) {
            val iopl = x86Register.eflags.iopl(core)
            if (iopl != 0) throw x86HardwareException.GeneralProtectionFault(core.pc, iopl.toULong())
        }

        if (!prefs.is16BitOperandMode) {
            val eflags = x86utils.pop(core, Datatype.DWORD, prefs)
            x86Register.eflags.value(core, eflags)
        } else {
            val flags = x86utils.pop(core, Datatype.WORD, prefs)
            x86Register.flags.value(core, flags)
        }
    }
}