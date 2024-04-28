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
import ru.inforion.lab403.kopycat.cores.x86.MSR_IA32_SYSENTER_CS
import ru.inforion.lab403.kopycat.cores.x86.MSR_IA32_SYSENTER_EIP
import ru.inforion.lab403.kopycat.cores.x86.MSR_IA32_SYSENTER_ESP
import ru.inforion.lab403.kopycat.cores.x86.exceptions.x86HardwareException
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86MMU
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Sysenter(core: x86Core, opcode: ByteArray, prefs: Prefixes):
    AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "sysenter"

    override fun execute() {
        val ia32SysenterCS = core.config.msr[MSR_IA32_SYSENTER_CS] ?: 0uL
        val ia32SysenterIP = core.config.msr[MSR_IA32_SYSENTER_EIP] ?: 0uL
        val ia32SysenterSP = core.config.msr[MSR_IA32_SYSENTER_ESP] ?: 0uL

        if (!core.cpu.cregs.cr0.pe || ia32SysenterCS[15..2].untruth) {
            throw x86HardwareException.GeneralProtectionFault(core.pc, 0uL)
        }

        core.cpu.flags.rflags.vm = false // Ensures protected mode execution
        core.cpu.flags.rflags.ifq = false // Mask interrupts

        if (core.cpu.is64BitCompatibilityMode) {
            core.cpu.regs.rsp.value = ia32SysenterSP
            core.cpu.regs.rip.value = ia32SysenterIP
        } else {
            core.cpu.regs.esp.value = ia32SysenterSP[31..0]
            core.cpu.regs.eip.value = ia32SysenterIP[31..0]
        }

        // Operating system provides CS; RPL forced to 0
        core.cpu.sregs.cs.value = ia32SysenterCS[15..0] and 0xFFFCuL
        core.mmu.cs = x86MMU.SegmentDescriptor32.createGdtEntry(
            0u, // CS.Base := 0; Flat segment
            0xFFFFFu, // CS.Limit := FFFFFH; With 4-KByte granularity, implies a 4-GByte limit
            11u, // CS.Type := 11; Execute/read code, accessed
            true, // CS.S := 1
            0u, // CS.DPL := 0;
            true, // CS.P := 1;
            core.cpu.is64BitCompatibilityMode, // IF in IA-32e mode THEN CS.L := 1; ELSE CS.L := 0;
            !core.cpu.is64BitCompatibilityMode, // IF in IA-32e mode THEN CS.D := 0; ELSE CS.D := 1;
            true, // CS.G := 1; 4-KByte granularity
        )

        // IF ShadowStackEnabled(CPL) { ... }

        core.cpu.sregs.cs.cpl = 0u

        // IF ShadowStackEnabled(CPL) { ... }
        // IF EndbranchEnabled(CPL) { ... }

        // SS just above CS
        core.cpu.sregs.ss.value = (ia32SysenterCS[15..0] and 0xFFFCuL) + 8uL
        core.mmu.ss = x86MMU.SegmentDescriptor32.createGdtEntry(
            0u, // SS.Base := 0; Flat segment
            0xFFFFFuL, // SS.Limit := FFFFFH; With 4-KByte granularity, implies a 4-GByte limit
            3u, // SS.Type := 3; Read/write data, accessed
            true, // SS.S := 1;
            0u, // SS.DPL := 0;
            true, // SS.P := 1;
            false,
            true, // SS.B := 1; 32-bit stack segment
            true // SS.G := 1; 4-KByte granularity
        )
    }
}
