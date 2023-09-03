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

import ru.inforion.lab403.common.extensions.hex
import ru.inforion.lab403.common.extensions.hex8
import ru.inforion.lab403.common.extensions.uint
import ru.inforion.lab403.common.extensions.ulong_z
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class CpuId(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "cpuid"

    override fun execute() = with (core.cpu.regs) {
        val index = eax.value.uint
        log.warning { "[0x${core.pc.hex}] ECX=0x${ecx.value.hex} Reading CPUID index = 0x${index.hex8}" }
        if (index == 0x69696969u) {
            eax.value = 0x8000_000Du
            ebx.value = 0x8000_000Du
            ecx.value = 0x8000_000Du
            edx.value = 0x8000_000Du
        } else {
            // see Table 3-8. Information Returned by CPUID Instruction of Vol2-abcd (page 293)
            val cpuid = core.config.cpuid(index, ecx.value.uint)
                ?: throw GeneralException(
                    "CPUID index=0x${index.hex8} " +
                            "ECX=0x${ecx.value.hex} not configured"
                )
            eax.value = cpuid.eax.ulong_z
            ebx.value = cpuid.ebx.ulong_z
            ecx.value = cpuid.ecx.ulong_z
            edx.value = cpuid.edx.ulong_z
        }
    }
}