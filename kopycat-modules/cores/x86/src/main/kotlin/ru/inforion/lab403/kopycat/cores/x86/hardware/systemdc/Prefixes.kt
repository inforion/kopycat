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
package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.*
import ru.inforion.lab403.kopycat.cores.x86.enums.StringPrefix
import ru.inforion.lab403.kopycat.cores.x86.enums.x86GPR
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU.Mode
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Prefixes(
    val core: x86Core,
    var lock: Boolean = false,
    var string: StringPrefix = StringPrefix.NO,
    var segmentOverride: x86Register? = null,
    var operandOverride: Boolean = false,
    var addressOverride: Boolean = false,
    var rexW: Boolean = false,
    var rexR: Boolean = false,
    var rexX: Boolean = false,
    var rexB: Boolean = false,
    var rex: Boolean = false
) {

    private fun datatypeByMode(override: Boolean, isAddress: Boolean) = when (core.cpu.mode) {
        Mode.R16 -> if (override) DWORD else WORD
        Mode.R32 -> if (override) WORD else DWORD
        Mode.R64 -> when {
            isAddress -> if (override) DWORD else QWORD
            else -> when {
                rexW && !override -> QWORD
                override -> WORD
                else -> DWORD
            }
        }
    }

    // TODO: as lazy
    val opsize get() = datatypeByMode(operandOverride, false)
    val addrsize get() = datatypeByMode(addressOverride, true)
    val is16BitAddressMode get() = addrsize == WORD
    val is16BitOperandMode get() = opsize == WORD

    // if it will be used only in ssr then doesn't matter what type of none
    private val none = core.cpu.regs.none32.toOperand()

    fun ssr(reg: x86Register = none) = when (segmentOverride) {
        null -> when (core.cpu.regs[reg.reg].extra) {
            x86GPR.RBP.index,
            x86GPR.RSP.index -> core.cpu.sregs.ss
            else -> core.cpu.sregs.ds
        }.toOperand()
        else -> segmentOverride!!
    }
}