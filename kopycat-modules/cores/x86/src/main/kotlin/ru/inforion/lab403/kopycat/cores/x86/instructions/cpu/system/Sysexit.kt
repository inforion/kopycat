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
import ru.inforion.lab403.common.extensions.truth
import ru.inforion.lab403.common.extensions.untruth
import ru.inforion.lab403.kopycat.cores.x86.MSR_IA32_SYSENTER_CS
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86MMU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Sysexit(core: x86Core, opcode: ByteArray, prefs: Prefixes):
    AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "sysexit"

    override fun execute() {
        val ia32SysenterCS = core.config.msr[MSR_IA32_SYSENTER_CS] ?: 0uL

        if (ia32SysenterCS[15..2].untruth || !core.cpu.cregs.cr0.pe || core.cpu.sregs.cs.cpl.truth) {
            throw x86HardwareException.GeneralProtectionFault(core.pc, 0uL)
        }

        if (prefs.rexW) {
            // Return to 64-bit mode
            core.cpu.regs.rsp.value = core.cpu.regs.rcx.value
            core.cpu.regs.rip.value = core.cpu.regs.rdx.value

            // CS.Selector := IA32_SYSENTER_CS[15:0] + 32;
            // CS.Selector := CS.Selector OR 3; RPL forced to 3
            core.cpu.sregs.cs.value = (ia32SysenterCS[15..0] + 32uL) or 3uL
        } else {
            // Return to protected mode or compatibility mode
            core.cpu.regs.rsp.value = core.cpu.regs.ecx.value
            core.cpu.regs.rip.value = core.cpu.regs.edx.value

            // CS.Selector := IA32_SYSENTER_CS[15:0] + 16;
            // CS.Selector := CS.Selector OR 3; RPL forced to 3
            core.cpu.sregs.cs.value = (ia32SysenterCS[15..0] + 16uL) or 3uL
        }

        core.mmu.cs = x86MMU.SegmentDescriptor32.createGdtEntry(
            0u, // CS.Base := 0; Flat segment
            0xFFFFFu, // CS.Limit := FFFFFH; With 4-KByte granularity, implies a 4-GByte limit
            11u, // CS.Type := 11; Execute/read code, accessed
            true, // CS.S := 1
            3u, // CS.DPL := 3;
            true, // CS.P := 1;
            prefs.rexW, // IF operand size is 64-bit THEN CS.L := 1; ELSE CS.L := 0;
            !prefs.rexW, // IF operand size is 64-bit THEN CS.D := 0; ELSE CS.D := 1;
            true, // CS.G := 1; 4-KByte granularity
        )

        core.cpu.sregs.cs.cpl = 3u

        // IF ShadowStackEnabled(CPL) { ... }

        // SS just above CS
        core.cpu.sregs.ss.value = core.cpu.sregs.cs.value + 8uL

        core.mmu.ss = x86MMU.SegmentDescriptor32.createGdtEntry(
            0u, // SS.Base := 0; Flat segment
            0xFFFFFuL, // SS.Limit := FFFFFH; With 4-KByte granularity, implies a 4-GByte limit
            3u, // SS.Type := 3; Read/write data, accessed
            true, // SS.S := 1;
            3u, // SS.DPL := 3;
            true, // SS.P := 1;
            false,
            true, // SS.B := 1; 32-bit stack segment
            true, // SS.G := 1; 4-KByte granularity
        )
    }
}
