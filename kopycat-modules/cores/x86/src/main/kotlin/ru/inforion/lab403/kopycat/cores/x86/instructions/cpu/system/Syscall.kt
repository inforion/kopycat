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
package ru.inforion.lab403.kopycat.cores.x86.instructions.cpu.system

import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.untruth
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86MMU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Syscall(core: x86Core, opcode: ByteArray, prefs: Prefixes):
    AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "syscall"

    companion object {
        const val CSE: Int = 0
        const val LMA: Int = 10
    }

    override fun execute() {
        if (!core.cpu.csl /*|| core.config.efer[LMA].untruth */|| core.config.efer[CSE].untruth)
            throw x86HardwareException.InvalidOpcode(core.pc)

        core.cpu.regs.rcx.value = core.cpu.regs.rip.value
        core.cpu.regs.rip.value = core.config.lstar
        core.cpu.regs.r11.value = core.cpu.flags.rflags.value
        core.cpu.flags.rflags.value = core.cpu.flags.rflags.value and core.config.fmask.inv()

        // TODO: synthesise it into a data-constant
        core.cpu.sregs.cs.value = core.config.star[47..32] and 0xFFFCuL
        // Set rest of CS to a fixed value
        // TODO: cache override???
        core.mmu.cs = x86MMU.SegmentDescriptor32.createGdtEntry(
            0u,    //  Flat segment
            0xFFFFFu, // With 4-KByte granularity, implies a 4-GByte limit
            11u,  // Execute/read code, accessed
            true,
            0u,
            true,
            true,   // Entry is to 64-bit mode
            false, // Required if CS.L = 1
            true  // 4-KByte granularity
        )

        // IF ShadowStackEnabled(CPL) { ... }

        core.cpu.sregs.cs.cpl = 0u

        // IF ShadowStackEnabled(CPL) { ... }
        // IF EndbranchEnabled(CPL) { ... }

        core.cpu.sregs.ss.value = core.config.star[47..32] + 8u
        // Set rest of SS to a fixed value
        // TODO: cache override???
        core.mmu.ss = x86MMU.SegmentDescriptor32.createGdtEntry(
            0u,    //  Flat segment
            0xFFFFFu, // With 4-KByte granularity, implies a 4-GByte limit
            3u,  // Execute/read code, accessed
            true,
            0u,
            true,
            false,
            true, // 32-bit stack segment
            true  // 4-KByte granularity
        )
    }
}