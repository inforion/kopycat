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
package ru.inforion.lab403.kopycat.cores.x86.instructions.fpu

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.exceptions.GeneralException
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Fst(core: x86Core, opcode: ByteArray, prefs: Prefixes, val popCount: Int, vararg operands: AOperand<x86Core>) :
    AFPUInstruction(core, opcode, prefs, *operands) {
    override val mnem = if (popCount == 0) "fst" else "fstp"

    override fun executeFPUInstruction() {
        val value = op2.extValue(core)

        op1.extValue(
            core,
            when (op1.dtyp) {
                Datatype.DWORD -> value.longDouble(core.fpu.fwr.FPUControlWord).float.ieee754AsUnsigned().bigint
                Datatype.QWORD -> value.longDouble(core.fpu.fwr.FPUControlWord).double.ieee754AsUnsigned().bigint
                Datatype.FPU80 -> value
                else -> throw GeneralException("Unknown type")
            }
        )

        core.fpu.pop(popCount)
    }
}
