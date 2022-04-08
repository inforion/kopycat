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
import ru.inforion.lab403.common.extensions.insert
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.cores.x86.instructions.AX86Instruction
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Wrmsr(core: x86Core, opcode: ByteArray, prefs: Prefixes):
        AX86Instruction(core, Type.VOID, opcode, prefs) {
    override val mnem = "wrmsr"

    override fun execute() = with (core.cpu.regs) {
        val register = ecx.value

//        if (register !in core.cpu.msr)
//            throw GeneralException("MSR[0x${register.hex}] not available for write, because not initialized")

//        if (register == 0xC0000080uL) { // EFER
////            throw GeneralException("LONG MODE MAY BE ENABLED!")
//            core.debugger.isRunning = false
//        }

        val value = insert(edx.value, 63..32)
            .insert(eax.value, 31..0)

        core.config.wrmsr(register, value)

        log.info { "[0x${core.cpu.pc.hex}] MSR[0x${register.hex}] written -> 0x${value.hex}" }
    }
}