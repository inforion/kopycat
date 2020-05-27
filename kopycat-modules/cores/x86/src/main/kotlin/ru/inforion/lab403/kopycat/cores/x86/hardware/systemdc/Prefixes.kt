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
package ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc

import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.DWORD
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype.WORD
import ru.inforion.lab403.kopycat.cores.x86.enums.StringPrefix
import ru.inforion.lab403.kopycat.cores.x86.hardware.processors.x86CPU
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register
import ru.inforion.lab403.kopycat.cores.x86.operands.x86Register.SSR.ds
import ru.inforion.lab403.kopycat.modules.cores.x86Core


class Prefixes(
        val core: x86Core,
        var lock: Boolean = false,
        var string: StringPrefix = StringPrefix.NO,
        var segmentOverride: x86Register = ds,
        var operandOverride: Boolean = false,
        var addressOverride: Boolean = false) {

    val opsize: Datatype get() = if ((core.cpu.mode == x86CPU.Mode.R16) xor operandOverride) WORD else DWORD
    val addrsize: Datatype get() = if ((core.cpu.mode == x86CPU.Mode.R16) xor addressOverride) WORD else DWORD
    val is16BitAddressMode: Boolean get() = (core.cpu.mode == x86CPU.Mode.R16) xor addressOverride
    val is16BitOperandMode: Boolean get() = opsize == WORD
    val ssr: x86Register get() = segmentOverride

//    val eax get() = x86Register.gpr(opsize, x86GPR.EAX.id)
//    val ecx get() = x86Register.gpr(opsize, x86GPR.ECX.id)
//    val ebx get() = x86Register.gpr(opsize, x86GPR.EBX.id)
//    val edx get() = x86Register.gpr(opsize, x86GPR.EDX.id)
//    val esp get() = x86Register.gpr(opsize, x86GPR.ESP.id)
//    val ebp get() = x86Register.gpr(opsize, x86GPR.EBP.id)
//    val esi get() = x86Register.gpr(opsize, x86GPR.ESI.id)
//    val edi get() = x86Register.gpr(opsize, x86GPR.EDI.id)
}