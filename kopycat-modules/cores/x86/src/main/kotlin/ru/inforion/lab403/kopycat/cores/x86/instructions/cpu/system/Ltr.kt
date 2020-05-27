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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.system

import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Ltr(core: x86Core, opcode: ByteArray, prefs: Prefixes, vararg operands: AOperand<x86Core>):
        AX86Instruction(core, Type.VOID, opcode, prefs, *operands) {
    override val mnem = "ltr"

    override fun execute() {
        val ss = op1.value(core)

        if (ss == 0L) throw x86HardwareException.GeneralProtectionFault(core.pc, 0)

        if (ss > core.mmu.gdtr.limit) throw x86HardwareException.GeneralProtectionFault(core.pc, ss)

        val desc = core.mmu.readSegmentDescriptor(ss)

        if (!desc.isForAnAvailableTSS) throw x86HardwareException.GeneralProtectionFault(core.pc, ss)
        if (!desc.isPresent) throw x86HardwareException.SegmentNotPresent(core.pc, ss)

        // TSSSegmentDescriptor.Busy = 1;

        core.cop.tssr = ss
    }
}