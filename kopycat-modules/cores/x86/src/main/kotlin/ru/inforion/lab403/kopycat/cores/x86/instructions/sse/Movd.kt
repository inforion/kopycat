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
package ru.inforion.lab403.kopycat.cores.x86.instructions.sse

import ru.inforion.lab403.common.extensions.bigint
import ru.inforion.lab403.common.extensions.get
import ru.inforion.lab403.common.extensions.ulong
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Movd(core: x86Core, opcode: ByteArray, prefs: Prefixes, val movq: Boolean, vararg operands: AOperand<x86Core>) :
    ASSEInstruction(core, opcode, prefs, *operands) {

    // Сходится с IDA, может не сойтись с radare если используется mmx регистр
    // IDA: 48 0F 6E movq mm3, rax
    // Radare:
    // $ rz-asm -b64 -d '48 0F 6E D8'
    // movd mm3, rax
    // Отличие только в названии инструкции, по сути это movq
    override val mnem = if (movq) "movq" else "movd"

    override fun executeSSEInstruction() {
        val a2 = when (op2.dtyp) {
            Datatype.XMMWORD -> {
                val tmp = op2.extValue(core)
                if (movq) tmp[63..0].ulong else tmp[31..0].ulong
            }
            else -> {
                val tmp = op2.value(core)
                if (movq) tmp else tmp[31..0]
            }
        }

        when (op1.dtyp) {
            Datatype.XMMWORD -> op1.extValue(core, a2.bigint)
            else -> op1.value(core, a2)
        }
    }
}
