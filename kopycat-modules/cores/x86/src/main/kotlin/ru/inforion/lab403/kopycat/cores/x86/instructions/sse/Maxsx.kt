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

import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.kopycat.cores.base.enums.Datatype
import ru.inforion.lab403.kopycat.cores.base.like
import ru.inforion.lab403.kopycat.cores.base.operands.AOperand
import ru.inforion.lab403.kopycat.cores.x86.hardware.systemdc.Prefixes
import ru.inforion.lab403.kopycat.modules.cores.x86Core

class Maxsx(
    core: x86Core,
    opcode: ByteArray,
    prefs: Prefixes,
    private val typ: Datatype,
    vararg operands: AOperand<x86Core>
) : ASSEInstruction(core, opcode, prefs, *operands) {
    override val mnem = when (typ) {
        Datatype.DWORD -> "maxss"
        Datatype.QWORD -> "maxsd"
        else -> TODO("maxs* variant")
    }

    override fun executeSSEInstruction() {
        val a1 = op1.extValue(core)
        val a2 = when (op2.dtyp) {
            Datatype.XMMWORD -> op2.extValue(core).ulong
            else -> op2.value(core)
        } like typ

        val movDstSrc = when (typ) {
            Datatype.DWORD -> {
                val f1 = a1.ulong[typ.msb..typ.lsb].uint.ieee754()
                val f2 = a2.uint.ieee754()
                f1 == 0.0f && f2 == 0.0f || f1.isNaN() || f2.isNaN() || f1 <= f2
            }
            Datatype.QWORD -> {
                val f1 = a1.ulong[typ.msb..typ.lsb].ieee754()
                val f2 = a2.ieee754()
                f1 == 0.0 && f2 == 0.0 || f1.isNaN() || f2.isNaN() || f1 <= f2
            }
            else -> TODO("maxs* variant")
        }

        if (movDstSrc) {
            op1.extValue(core, a1.insert(a2, typ.msb..typ.lsb))
        }
    }
}
