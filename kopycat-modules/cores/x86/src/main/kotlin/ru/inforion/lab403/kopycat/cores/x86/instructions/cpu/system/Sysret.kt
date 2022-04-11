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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.system

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.untruth
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86MMU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Sysret(core: x86Core, opcode: ByteArray, prefs: Prefixes):
    AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "sysret"

    companion object {
        const val CSE: Int = 0
        const val LMA: Int = 10
    }

    override fun execute() {
        if (!core.cpu.csl /*|| core.config.efer[LMA].untruth */|| core.config.efer[CSE].untruth)
            throw x86HardwareException.InvalidOpcode(core.pc)

        if (core.cpu.sregs.cs.cpl != 0uL)
            throw x86HardwareException.GeneralProtectionFault(core.pc, 0u)

        if (prefs.opsize == Datatype.QWORD) {
            //IF (RCX is not canonical) THEN #GP(0);
            core.cpu.regs.rip.value = core.cpu.regs.rcx.value
        }
        else {
            TODO("Compatibility mode")
            core.cpu.regs.rip.value = core.cpu.regs.ecx.value
        }
        // Clear RF, VM, reserved bits; set bit 1
        core.cpu.flags.rflags.value = (core.cpu.regs.r11.value and 0x3C7FD7uL) or 2uL

        core.cpu.sregs.cs.value = (core.config.star[63..48] + if (prefs.opsize == Datatype.QWORD) 16u else 0u) or 3u // RPL forced to 3

        // Set rest of CS to a fixed value
        val (l, d) = if (prefs.opsize == Datatype.QWORD) true to false else TODO("Compatibility mode: false to true")
        // TODO: cache override???
        core.mmu.cs = x86MMU.SegmentDescriptor32.createGdtEntry(
            0u,    //  Flat segment
            0xFFFFFu, // With 4-KByte granularity, implies a 4-GByte limit
            11u,  // Execute/read code, accessed
            true,
            3u,
            true,
            l,
            d,
            true  // 4-KByte granularity
        )
        core.cpu.sregs.cs.cpl = 3u

        // IF ShadowStackEnabled(CPL) { ... }

        core.cpu.sregs.ss.value = (core.config.star[63..48] + 8u) or 3u // RPL forced to 3
        // Set rest of SS to a fixed value
        // TODO: cache override???
        core.mmu.ss = x86MMU.SegmentDescriptor32.createGdtEntry(
            0u,    //  Flat segment
            0xFFFFFu, // With 4-KByte granularity, implies a 4-GByte limit
            3u,  // Execute/read code, accessed
            true,
            3u,
            true,
            false,
            true, // 32-bit stack segment
            true  // 4-KByte granularity
        )
    }
}